package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;

@Component
public class TransactionParticipant implements ITransactionParticipant
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, TransactionParticipant.class);

    private static final String TRANSACTION_COORDINATOR_KEY = "i_am_transaction_coordinator_key";

    private static final String INTERACTIVE_SESSION_KEY = "i_am_interactive_session_key";

    private static final String TRANSACTION_LOG_PATH = "transaction-logs";

    private final Map<UUID, TransactionThread> threadMap = new HashMap<>();

    private final String participantId;

    private final ISessionTokenProvider sessionTokenProvider;

    private final IDatabaseTransactionProvider databaseTransactionProvider;

    private final ITransactionOperationExecutor operationExecutor;

    private final ITransactionLog transactionLog;

    TransactionParticipant(String participantId, IDatabaseTransactionProvider databaseTransactionProvider, ISessionTokenProvider sessionTokenProvider,
            ITransactionOperationExecutor operationExecutor, ITransactionLog transactionLog)
    {
        this.participantId = participantId;
        this.sessionTokenProvider = sessionTokenProvider;
        this.databaseTransactionProvider = databaseTransactionProvider;
        this.operationExecutor = operationExecutor;
        this.transactionLog = transactionLog;
    }

    @Autowired
    public TransactionParticipant(final PlatformTransactionManager transactionManager, final IDAOFactory daoFactory,
            final DatabaseConfigurationContext databaseContext, final IApplicationServerApi applicationServerApi)
    {
        this.participantId = null;
        this.sessionTokenProvider = new ISessionTokenProvider()
        {
            @Override public boolean isValid(final String sessionToken)
            {
                return true;
            }
        };
        this.databaseTransactionProvider = new IDatabaseTransactionProvider()
        {
            @Override public Object beginTransaction(final UUID transactionId)
            {
                return transactionManager.getTransaction(new DefaultTransactionDefinition());
            }

            @Override public void prepareTransaction(final UUID transactionId, final Object transaction)
            {
                Session session = daoFactory.getSessionFactory().getCurrentSession();
                session.flush();
                session.doWork(connection ->
                {
                    try (PreparedStatement prepareStatement = connection.prepareStatement("PREPARE TRANSACTION '" + transactionId + "'"))
                    {
                        prepareStatement.execute();
                        operationLog.info("Database transaction '" + transactionId + "' was prepared.");
                    }
                });
            }

            @Override public void rollbackTransaction(final UUID transactionId, final Object transaction)
                    throws Exception
            {
                try (Connection connection = databaseContext.getDataSource().getConnection();
                        PreparedStatement countStatement = connection.prepareStatement(
                                "SELECT count(*) AS count FROM pg_prepared_xacts WHERE gid = ?");
                        PreparedStatement rollbackStatement = connection.prepareStatement("ROLLBACK PREPARED '" + transactionId + "'"))
                {
                    countStatement.setString(1, transactionId.toString());

                    try (ResultSet countResult = countStatement.executeQuery())
                    {
                        if (countResult.next())
                        {
                            int count = countResult.getInt("count");

                            if (count > 0)
                            {
                                rollbackStatement.execute();
                                operationLog.info(
                                        "Prepared database transaction '" + transactionId + "' was rolled back.");
                            } else
                            {
                                operationLog.info(
                                        "Nothing to rollback in the database. Prepared database transaction '" + transactionId + "' was not found.");
                            }
                        }
                    }
                } finally
                {
                    try
                    {
                        transactionManager.rollback((org.springframework.transaction.TransactionStatus) transaction);
                    } catch (Exception e)
                    {
                        // nothing to do
                    }
                }
            }

            @Override public void commitTransaction(final UUID transactionId, final Object transaction) throws Exception
            {
                try (Connection connection = databaseContext.getDataSource().getConnection();
                        PreparedStatement commitStatement = connection.prepareStatement("COMMIT PREPARED '" + transactionId + "'"))
                {
                    commitStatement.execute();
                    operationLog.info("Database prepared transaction '" + transactionId + "' was committed.");
                }
            }
        };
        this.operationExecutor = new ITransactionOperationExecutor()
        {
            @Override public Object executeOperation(String sessionToken, String operationName, Object[] operationArguments)
            {
                for (Method method : applicationServerApi.getClass().getMethods())
                {
                    if (method.getName().equals(operationName))
                    {
                        try
                        {
                            return method.invoke(applicationServerApi, operationArguments);
                        } catch (Exception e)
                        {
                            throw new TransactionOperationException(e);
                        }
                    }
                }

                throw new IllegalArgumentException("Unknown operation  '" + operationName + "'.");
            }
        };
        this.transactionLog = new TransactionLog(new File(TRANSACTION_LOG_PATH));
    }

    @Override public String getParticipantId()
    {
        return participantId;
    }

    @Override public void beginTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        executeInTransactionThread(transactionId, sessionToken, interactiveSessionKey, null, TransactionConst.BEGIN_TRANSACTION_METHOD, null);
    }

    @Override public Object executeOperation(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
            final String operationName, final Object[] operationArguments)
    {
        return executeInTransactionThread(transactionId, sessionToken, interactiveSessionKey, null, operationName, operationArguments);
    }

    @Override public void prepareTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
            final String transactionCoordinatorKey)
    {
        executeInTransactionThread(transactionId, sessionToken, interactiveSessionKey, null, TransactionConst.PREPARE_TRANSACTION_METHOD, null);
    }

    @Override public List<UUID> getTransactions(final String transactionCoordinatorKey)
    {
        Map<UUID, TransactionStatus> lastStatuses = transactionLog.getLastStatuses();

        List<UUID> preparedTransactions = new ArrayList<>();
        for (Map.Entry<UUID, TransactionStatus> lastStatusEntry : lastStatuses.entrySet())
        {
            if (TransactionStatus.PREPARE_FINISHED.equals(lastStatusEntry.getValue()))
            {
                preparedTransactions.add(lastStatusEntry.getKey());
            }
        }

        return preparedTransactions;
    }

    @Override public void commitTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        executeInTransactionThread(transactionId, sessionToken, interactiveSessionKey, null, TransactionConst.COMMIT_TRANSACTION_METHOD, null);
    }

    @Override public void commitTransaction(final UUID transactionId, final String transactionCoordinatorKey)
    {
        executeInTransactionThread(transactionId, null, null, transactionCoordinatorKey, TransactionConst.COMMIT_TRANSACTION_METHOD, null);
    }

    @Override public void rollbackTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        executeInTransactionThread(transactionId, sessionToken, interactiveSessionKey, null, TransactionConst.ROLLBACK_TRANSACTION_METHOD, null);
    }

    @Override public void rollbackTransaction(final UUID transactionId, final String transactionCoordinatorKey)
    {
        executeInTransactionThread(transactionId, null, null, transactionCoordinatorKey, TransactionConst.ROLLBACK_TRANSACTION_METHOD, null);
    }

    private Object executeInTransactionThread(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
            final String transactionCoordinatorKey, final String operationName, final Object[] operationArguments)
    {
        if (transactionId == null)
        {
            throw new IllegalArgumentException("Transaction id cannot be null");
        }

        TransactionThread thread;

        operationLog.info("Transaction '" + transactionId + "' execute operation '" + operationName + "' started.");

        synchronized (this)
        {
            thread = threadMap.get(transactionId);

            if (thread == null)
            {
                if (threadMap.size() >= TransactionConst.THREAD_COUNT_LIMIT)
                {
                    throw new RuntimeException(
                            "Cannot handle transaction '" + transactionId + "' as there are too many other transactions running already.");
                }

                operationLog.info("Creating a new thread for transaction '" + transactionId + "'.");

                thread = new TransactionThread(transactionId);
                threadMap.put(transactionId, thread);
                thread.startThread();
            } else
            {
                operationLog.info("Found existing thread for transaction '" + transactionId + "'.");
            }
        }

        try
        {
            Object result =
                    thread.executeOperation(sessionToken, interactiveSessionKey, transactionCoordinatorKey, operationName, operationArguments);
            operationLog.info("Transaction '" + transactionId + "' execute operation '" + operationName + "' finished successfully.");
            return result;
        } catch (Exception e)
        {
            operationLog.info("Transaction '" + transactionId + "' execute operation '" + operationName + "' failed.", e);
            throw e;
        } finally
        {
            synchronized (this)
            {
                if (thread.isThreadFinished())
                {
                    operationLog.info("Transaction '" + transactionId + "' finished. Removing its thread.");
                    threadMap.remove(transactionId);
                }
            }
        }
    }

    public boolean isRunningTransaction(UUID transactionId)
    {
        synchronized (this)
        {
            TransactionThread thread = threadMap.get(transactionId);
            return thread != null && !thread.isThreadFinished();
        }
    }

    class TransactionThread
    {

        private final Object lock = new Object();

        private final Thread thread;

        private boolean threadFinished;

        private Object transactionObject;

        private TransactionStatus transactionStatus = TransactionStatus.NEW;

        private final UUID transactionId;

        private String sessionToken;

        private String operationName;

        private Object[] operationArguments;

        private Object operationResult;

        private Throwable operationException;

        public TransactionThread(UUID transactionId)
        {
            this.transactionId = transactionId;
            this.thread = new Thread(new Runnable()
            {
                @Override public void run()
                {
                    synchronized (lock)
                    {
                        while (true)
                        {
                            try
                            {
                                if (operationName != null)
                                {
                                    operationLog.info("Transaction '" + transactionId + "' thread executing operation '" + operationName + "'.");

                                    if (TransactionConst.BEGIN_TRANSACTION_METHOD.equals(operationName))
                                    {
                                        checkTransactionStatus(transactionStatus, TransactionStatus.NEW);

                                        changeTransactionStatus(TransactionStatus.BEGIN_STARTED);
                                        operationResult = transactionObject = databaseTransactionProvider.beginTransaction(transactionId);
                                        changeTransactionStatus(TransactionStatus.BEGIN_FINISHED);
                                    } else if (TransactionConst.PREPARE_TRANSACTION_METHOD.equals(operationName))
                                    {
                                        checkTransactionStatus(transactionStatus, TransactionStatus.BEGIN_FINISHED);

                                        changeTransactionStatus(TransactionStatus.PREPARE_STARTED);
                                        databaseTransactionProvider.prepareTransaction(transactionId, transactionObject);
                                        changeTransactionStatus(TransactionStatus.PREPARE_FINISHED);
                                    } else if (TransactionConst.COMMIT_TRANSACTION_METHOD.equals(operationName))
                                    {
                                        checkTransactionStatus(transactionStatus, TransactionStatus.NEW, TransactionStatus.PREPARE_FINISHED);

                                        if (transactionStatus != TransactionStatus.NEW)
                                        {
                                            changeTransactionStatus(TransactionStatus.COMMIT_STARTED);
                                            databaseTransactionProvider.commitTransaction(transactionId, transactionObject);
                                            changeTransactionStatus(TransactionStatus.COMMIT_FINISHED);
                                        }
                                    } else if (TransactionConst.ROLLBACK_TRANSACTION_METHOD.equals(operationName))
                                    {
                                        checkTransactionStatus(transactionStatus, TransactionStatus.NEW, TransactionStatus.BEGIN_STARTED,
                                                TransactionStatus.BEGIN_FINISHED, TransactionStatus.PREPARE_STARTED,
                                                TransactionStatus.PREPARE_FINISHED, TransactionStatus.COMMIT_STARTED);

                                        if (transactionStatus != TransactionStatus.NEW)
                                        {
                                            changeTransactionStatus(TransactionStatus.ROLLBACK_STARTED);
                                            databaseTransactionProvider.rollbackTransaction(transactionId, transactionObject);
                                            changeTransactionStatus(TransactionStatus.ROLLBACK_FINISHED);
                                        }
                                    } else
                                    {
                                        checkTransactionStatus(transactionStatus, TransactionStatus.BEGIN_FINISHED);
                                        operationResult = operationExecutor.executeOperation(sessionToken, operationName, operationArguments);
                                    }

                                    operationName = null;
                                    operationArguments = null;
                                    operationException = null;

                                    if (transactionStatus == TransactionStatus.NEW || transactionStatus == TransactionStatus.COMMIT_FINISHED
                                            || transactionStatus == TransactionStatus.ROLLBACK_FINISHED)
                                    {
                                        threadFinished = true;
                                        return;
                                    }
                                } else
                                {
                                    operationLog.info("Transaction '" + transactionId + "' thread waiting for the next call.");
                                    lock.wait();
                                }

                            } catch (Throwable e)
                            {
                                operationName = null;
                                operationArguments = null;
                                operationResult = null;
                                operationException = e;

                                if (transactionStatus == TransactionStatus.NEW)
                                {
                                    threadFinished = true;
                                    return;
                                }
                            } finally
                            {
                                lock.notifyAll();
                            }
                        }
                    }
                }

                private void checkTransactionStatus(TransactionStatus actualStatus, TransactionStatus... expectedStatuses)
                {
                    for (final TransactionStatus expectedStatus : expectedStatuses)
                    {
                        if (actualStatus == expectedStatus)
                        {
                            return;
                        }
                    }

                    throw new IllegalStateException(
                            "Transaction '" + transactionId + "' unexpected status '" + actualStatus + "'. Expected statuses '"
                                    + Arrays.toString(expectedStatuses) + "'.");
                }

                private void changeTransactionStatus(TransactionStatus status)
                {
                    transactionLog.logStatus(transactionId, status);
                    TransactionThread.this.transactionStatus = status;
                }

            });
        }

        public void startThread()
        {
            operationLog.info("Transaction '" + transactionId + "' thread '" + thread.getName() + "' started.");
            thread.start();
        }

        public Object executeOperation(String newSessionToken, String newInteractiveSessionKey, String newTransactionCoordinatorKey,
                String newOperationName, Object[] newOperationArguments)
        {
            synchronized (lock)
            {
                try
                {
                    if (operationName != null)
                    {
                        throw new IllegalStateException(
                                "Cannot schedule transaction '" + transactionId + "' operation '" + newOperationName
                                        + "' execution as the previous operation '"
                                        + operationName + "' hasn't finished yet.");
                    }

                    sessionToken = newSessionToken;
                    operationName = newOperationName;
                    operationArguments = newOperationArguments;
                    operationResult = null;
                    operationException = null;
                    lock.notifyAll();

                    operationLog.info("Transaction '" + transactionId + "' thread scheduled operation '" + newOperationName + "' execution.");

                    while (operationName != null)
                    {
                        lock.wait();
                    }

                } catch (Exception e)
                {
                    operationLog.error(
                            "Scheduling of transaction '" + operationName + "' operation '" + operationName
                                    + "' execution in thread failed or got interrupted.", e);
                    throw new RuntimeException(e);
                }

                if (operationException != null)
                {
                    if (operationException instanceof TransactionOperationException)
                    {
                        throw (TransactionOperationException) operationException;
                    } else
                    {
                        throw new TransactionOperationException(operationException);
                    }
                } else
                {
                    return operationResult;
                }
            }
        }

        private boolean isThreadFinished()
        {
            return threadFinished;
        }

    }

}
