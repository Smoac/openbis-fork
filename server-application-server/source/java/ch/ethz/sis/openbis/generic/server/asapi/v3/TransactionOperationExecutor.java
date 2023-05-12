package ch.ethz.sis.openbis.generic.server.asapi.v3;

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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;

@Component
public class TransactionOperationExecutor implements ITransactionOperationExecutor
{

    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, TransactionOperationExecutor.class);

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private DatabaseConfigurationContext databaseContext;

    private final Map<String, TransactionThread> threadMap = new HashMap<>();

    public Object execute(String transactionId, String transactionManagerSecret, ITransactionOperation operation) throws Throwable
    {
        if (transactionId != null)
        {
            checkTransactionManagerSecret(transactionManagerSecret);

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
                return thread.execute(operation);
            } finally
            {
                synchronized (this)
                {
                    if (!thread.isAlive())
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

    enum TwoPhaseTransactionStatus
    {
        NEW, STARTED, PREPARED, COMMITTED, ROLLED_BACK
    }

    class TransactionThread
    {

        private final Object lock = new Object();

        private final Thread thread;

        private final String transactionId;

        private TransactionStatus transaction;

        private TwoPhaseTransactionStatus status = TwoPhaseTransactionStatus.NEW;

        private ITransactionOperation invocation;

        private Object result;

        private Throwable exception;

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
                                    if (TransactionConst.BEGIN_TRANSACTION_METHOD.equals(invocation.getOperationName()))
                                    {
                                        checkTransactionStatus(TwoPhaseTransactionStatus.NEW);
                                        transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
                                        status = TwoPhaseTransactionStatus.STARTED;
                                    } else if (TransactionConst.PREPARE_TRANSACTION_METHOD.equals(invocation.getOperationName()))
                                    {
                                        checkTransactionStatus(TwoPhaseTransactionStatus.STARTED);

                                        Session session = daoFactory.getSessionFactory().getCurrentSession();
                                        session.flush();
                                        session.doWork(connection ->
                                        {
                                            PreparedStatement statement = connection.prepareStatement("PREPARE TRANSACTION '" + transactionId + "'");
                                            statement.execute();
                                        });

                                        status = TwoPhaseTransactionStatus.PREPARED;
                                    } else if (TransactionConst.COMMIT_TRANSACTION_METHOD.equals(invocation.getOperationName()))
                                    {
                                        checkTransactionStatus(TwoPhaseTransactionStatus.PREPARED);

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

                                        status = TwoPhaseTransactionStatus.COMMITTED;
                                    } else if (TransactionConst.ROLLBACK_TRANSACTION_METHOD.equals(invocation.getOperationName()))
                                    {
                                        checkTransactionStatus(TwoPhaseTransactionStatus.PREPARED);

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
                                        }

                                        status = TwoPhaseTransactionStatus.ROLLED_BACK;
                                    } else
                                    {
                                        checkTransactionStatus(
                                                TwoPhaseTransactionStatus.NEW,
                                                TwoPhaseTransactionStatus.STARTED,
                                                TwoPhaseTransactionStatus.PREPARED);
                                        result = invocation.executeOperation();
                                    }

                                    operationLog.info("Two phase transaction " + transactionId + " method " + invocation.getOperationName()
                                            + " executed.");

                                    exception = null;
                                    invocation = null;
                                    lock.notifyAll();

                                    if (status == TwoPhaseTransactionStatus.COMMITTED
                                            || status == TwoPhaseTransactionStatus.ROLLED_BACK)
                                    {
                                        return;
                                    }
                                } else
                                {
                                    operationLog.info("Two phase transaction " + transactionId + " thread waiting for the next call.");
                                    lock.wait();
                                }

                            } catch (Throwable e)
                            {
                                operationLog.error(
                                        "Two phase transaction " + transactionId + " method " + (invocation != null ?
                                                invocation.getOperationName() : "") + " failed or got interrupted.",
                                        e);

                                if (transaction != null)
                                {
                                    transactionManager.rollback(transaction);
                                }

                                exception = e;
                                result = null;
                                invocation = null;
                                lock.notifyAll();

                                if (e instanceof Error)
                                {
                                    throw (Error) e;
                                } else
                                {
                                    return;
                                }
                            }
                        }
                    }
                }

                private void checkTransactionStatus(TwoPhaseTransactionStatus... expectedStatuses)
                {
                    if (!Arrays.asList(expectedStatuses).contains(status))
                    {
                        throw new IllegalStateException(
                                "Two phase transaction " + transactionId + " unexpected status " + status + ". Expected statuses "
                                        + Arrays.toString(expectedStatuses) + ".");
                    }
                }

            });
        }

        public void start()
        {
            thread.start();
        }

        public boolean isAlive()
        {
            return thread.isAlive();
        }

        public Object execute(ITransactionOperation newInvocation) throws Throwable
        {
            synchronized (lock)
            {
                try
                {
                    if (invocation != null)
                    {
                        throw new IllegalStateException(
                                "Cannot schedule another two phase transaction " + transactionId + " call as the previous execution for method "
                                        + invocation.getOperationName()
                                        + " hasn't finished yet.");
                    }

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
                                    + " call failed or got interrupted.",
                            e);
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

    private void checkTransactionManagerSecret(String secret)
    {
        if (secret == null || secret.isBlank())
        {
            throw new IllegalStateException("Two phase transaction manager secret missing.");
        }
    }

}
