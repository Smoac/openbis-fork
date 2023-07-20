package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;

@Component
public class TransactionParticipant implements ITransactionParticipant
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, TransactionParticipant.class);

    private static final String TRANSACTION_COORDINATOR_SECRET = "i_am_secret";

    private static final String TRANSACTION_LOG_PATH = "transaction-logs";

    private final Map<String, TransactionThread> threadMap = new HashMap<>();

    private final IDatabaseTransactionProvider databaseTransactionProvider;

    private final ITransactionLog transactionLog;

    TransactionParticipant(IDatabaseTransactionProvider databaseTransactionProvider, ITransactionLog transactionLog)
    {
        this.databaseTransactionProvider = databaseTransactionProvider;
        this.transactionLog = transactionLog;
    }

    @Autowired
    public TransactionParticipant(final PlatformTransactionManager transactionManager, final IDAOFactory daoFactory,
            final DatabaseConfigurationContext databaseContext)
    {
        this.databaseTransactionProvider = new IDatabaseTransactionProvider()
        {
            @Override public Object beginTransaction(final String transactionId)
            {
                return transactionManager.getTransaction(new DefaultTransactionDefinition());
            }

            @Override public void prepareTransaction(final String transactionId, final Object transaction)
            {
                Session session = daoFactory.getSessionFactory().getCurrentSession();
                session.flush();
                session.doWork(connection ->
                {
                    PreparedStatement statement = connection.prepareStatement("PREPARE TRANSACTION '" + transactionId + "'");
                    statement.execute();
                });
            }

            @Override public void rollbackTransaction(final String transactionId, final Object transaction)
                    throws Exception
            {
                Connection connection = null;
                Statement statement = null;

                try
                {
                    connection = databaseContext.getDataSource().getConnection();
                    statement = connection.createStatement();
                    statement.execute("ROLLBACK PREPARED '" + transactionId + "'");
                } catch (Exception e)
                {
                    if (statement != null)
                    {
                        try
                        {
                            statement.close();
                        } catch (Exception ignore)
                        {
                        }
                    }
                    if (connection != null)
                    {
                        try
                        {
                            connection.close();
                        } catch (SQLException ignore)
                        {
                        }
                    }

                    throw e;
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

            @Override public void commitTransaction(final String transactionId, final Object transaction) throws Exception
            {
                Connection connection = null;
                Statement statement = null;

                try
                {
                    connection = databaseContext.getDataSource().getConnection();
                    statement = connection.createStatement();
                    statement.execute("COMMIT PREPARED '" + transactionId + "'");
                } catch (Exception e)
                {
                    if (statement != null)
                    {
                        try
                        {
                            statement.close();
                        } catch (Exception ignore)
                        {
                        }
                    }
                    if (connection != null)
                    {
                        try
                        {
                            connection.close();
                        } catch (SQLException ignore)
                        {
                        }
                    }

                    throw e;
                }
            }
        };
        this.transactionLog = new TransactionLog(new File(TRANSACTION_LOG_PATH));
    }

    @Override public void beginTransaction(final String transactionId, final String transactionCoordinatorSecret) throws Throwable
    {
        executeOperation(transactionId, transactionCoordinatorSecret, new BeginTransactionOperation());
    }

    @Override public void prepareTransaction(final String transactionId, final String transactionCoordinatorSecret) throws Throwable
    {
        executeOperation(transactionId, transactionCoordinatorSecret, new PrepareTransactionOperation());
    }

    @Override public void commitTransaction(final String transactionId, final String transactionCoordinatorSecret) throws Throwable
    {
        executeOperation(transactionId, transactionCoordinatorSecret, new CommitTransactionOperation());
    }

    @Override public void rollbackTransaction(final String transactionId, final String transactionCoordinatorSecret) throws Throwable
    {
        executeOperation(transactionId, transactionCoordinatorSecret, new RollbackTransactionOperation());
    }

    public Object executeOperation(String transactionId, String transactionCoordinatorSecret, ITransactionOperation operation) throws Throwable
    {
        if (transactionId != null)
        {
            TransactionThread thread;

            synchronized (this)
            {
                operationLog.info("Two phase transaction id: " + transactionId);

                thread = threadMap.get(transactionId);

                if (thread == null)
                {
                    if (threadMap.size() >= TransactionConst.THREAD_COUNT_LIMIT)
                    {
                        throw new RuntimeException("Too many two phase transaction threads running");
                    }

                    operationLog.info("Creating two phase transaction thread");

                    thread = new TransactionThread(transactionId);
                    threadMap.put(transactionId, thread);
                    thread.start();
                } else
                {
                    operationLog.info("Found existing two phase transaction thread");
                }
            }

            try
            {
                return thread.execute(transactionCoordinatorSecret, operation);
            } finally
            {
                synchronized (this)
                {
                    if (thread.isFinished())
                    {
                        operationLog.info("Two phase transaction " + transactionId + " finished. Removing its thread.");
                        threadMap.remove(transactionId);
                    }
                }
            }
        } else
        {
            return operation.executeOperation();
        }
    }

    public boolean isRunningTransaction(String transactionId)
    {
        synchronized (this)
        {
            TransactionThread thread = threadMap.get(transactionId);
            return thread != null && !thread.isFinished();
        }
    }

    class TransactionThread
    {

        private final Object lock = new Object();

        private final Thread thread;

        private final String transactionId;

        private String transactionCoordinatorSecret;

        private Object transaction;

        private ITransactionOperation invocation;

        private TransactionStatus status = TransactionStatus.NEW;

        private Object result;

        private Throwable exception;

        private boolean finished;

        public TransactionThread(String transactionId)
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
                                if (invocation != null)
                                {
                                    if (invocation instanceof BeginTransactionOperation)
                                    {
                                        checkTransactionStatus(status, TransactionStatus.NEW);
                                        checkTransactionCoordinatorSecret(transactionCoordinatorSecret);

                                        changeTransactionStatus(TransactionStatus.BEGIN_STARTED);
                                        result = transaction = databaseTransactionProvider.beginTransaction(transactionId);
                                        changeTransactionStatus(TransactionStatus.BEGIN_FINISHED);
                                    } else if (invocation instanceof PrepareTransactionOperation)
                                    {
                                        checkTransactionStatus(status, TransactionStatus.BEGIN_FINISHED);
                                        checkTransactionCoordinatorSecret(transactionCoordinatorSecret);

                                        changeTransactionStatus(TransactionStatus.PREPARE_STARTED);
                                        databaseTransactionProvider.prepareTransaction(transactionId, transaction);
                                        changeTransactionStatus(TransactionStatus.PREPARE_FINISHED);
                                    } else if (invocation instanceof CommitTransactionOperation)
                                    {
                                        checkTransactionStatus(status, TransactionStatus.NEW, TransactionStatus.PREPARE_FINISHED);
                                        checkTransactionCoordinatorSecret(transactionCoordinatorSecret);

                                        if (status != TransactionStatus.NEW)
                                        {
                                            changeTransactionStatus(TransactionStatus.COMMIT_STARTED);
                                            databaseTransactionProvider.commitTransaction(transactionId, transaction);
                                            changeTransactionStatus(TransactionStatus.COMMIT_FINISHED);
                                        }
                                    } else if (invocation instanceof RollbackTransactionOperation)
                                    {
                                        checkTransactionStatus(status, TransactionStatus.NEW, TransactionStatus.BEGIN_STARTED,
                                                TransactionStatus.BEGIN_FINISHED, TransactionStatus.PREPARE_STARTED,
                                                TransactionStatus.PREPARE_FINISHED, TransactionStatus.COMMIT_STARTED);
                                        checkTransactionCoordinatorSecret(transactionCoordinatorSecret);

                                        if (status != TransactionStatus.NEW)
                                        {
                                            changeTransactionStatus(TransactionStatus.ROLLBACK_STARTED);
                                            databaseTransactionProvider.rollbackTransaction(transactionId, transaction);
                                            changeTransactionStatus(TransactionStatus.ROLLBACK_FINISHED);
                                        }
                                    } else
                                    {
                                        checkTransactionStatus(status, TransactionStatus.BEGIN_FINISHED);
                                        result = invocation.executeOperation();
                                    }

                                    operationLog.info("Two phase transaction " + transactionId + " method " + invocation.getOperationName()
                                            + " executed.");

                                    exception = null;
                                    invocation = null;

                                    if (status == TransactionStatus.NEW || status == TransactionStatus.COMMIT_FINISHED
                                            || status == TransactionStatus.ROLLBACK_FINISHED)
                                    {
                                        finished = true;
                                        return;
                                    }
                                } else
                                {
                                    operationLog.info("Two phase transaction " + transactionId + " thread waiting for the next call.");
                                    lock.wait();
                                }

                            } catch (Throwable e)
                            {
                                exception = e;
                                result = null;
                                invocation = null;

                                if (status == TransactionStatus.NEW)
                                {
                                    finished = true;
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
                            "Two phase transaction " + transactionId + " unexpected status " + actualStatus + ". Expected statuses "
                                    + Arrays.toString(expectedStatuses) + ".");
                }

                private void checkTransactionCoordinatorSecret(String secret)
                {
                    if (!TRANSACTION_COORDINATOR_SECRET.equals(secret))
                    {
                        throw new IllegalStateException("Two phase transaction coordinator secret is incorrect.");
                    }
                }

                private void changeTransactionStatus(TransactionStatus status)
                {
                    transactionLog.logStatus(transactionId, status);
                    TransactionThread.this.status = status;
                }

            });
        }

        public void start()
        {
            thread.start();
        }

        private boolean isFinished()
        {
            return finished;
        }

        public Object execute(String newTransactionCoordinatorSecret, ITransactionOperation newInvocation) throws Throwable
        {
            synchronized (lock)
            {
                try
                {
                    if (invocation != null)
                    {
                        throw new IllegalStateException(
                                "Cannot schedule another two phase transaction " + transactionId + " call as the previous execution for method "
                                        + invocation.getOperationName() + " hasn't finished yet.");
                    }

                    transactionCoordinatorSecret = newTransactionCoordinatorSecret;
                    invocation = newInvocation;
                    result = null;
                    exception = null;
                    lock.notifyAll();

                    operationLog.info("Two phase transaction " + transactionId + " method " + newInvocation.getOperationName() + " call scheduled.");

                    while (invocation != null)
                    {
                        lock.wait();
                    }

                } catch (Exception e)
                {
                    operationLog.error(
                            "Scheduling of the next two phase transaction method " + invocation.getOperationName()
                                    + " call failed or got interrupted.", e);
                    throw e;
                }

                if (exception != null)
                {
                    throw exception;
                } else
                {
                    return result;
                }
            }
        }

    }

    private static class BeginTransactionOperation implements ITransactionOperation
    {
        @Override public String getOperationName()
        {
            return TransactionConst.BEGIN_TRANSACTION_METHOD;
        }

        @Override public Object executeOperation() throws Throwable
        {
            throw new UnsupportedOperationException();
        }
    }

    private static class PrepareTransactionOperation implements ITransactionOperation
    {
        @Override public String getOperationName()
        {
            return TransactionConst.PREPARE_TRANSACTION_METHOD;
        }

        @Override public Object executeOperation() throws Throwable
        {
            throw new UnsupportedOperationException();
        }
    }

    private static class CommitTransactionOperation implements ITransactionOperation
    {
        @Override public String getOperationName()
        {
            return TransactionConst.COMMIT_TRANSACTION_METHOD;
        }

        @Override public Object executeOperation() throws Throwable
        {
            throw new UnsupportedOperationException();
        }
    }

    private static class RollbackTransactionOperation implements ITransactionOperation
    {
        @Override public String getOperationName()
        {
            return TransactionConst.ROLLBACK_TRANSACTION_METHOD;
        }

        @Override public Object executeOperation() throws Throwable
        {
            throw new UnsupportedOperationException();
        }
    }

}
