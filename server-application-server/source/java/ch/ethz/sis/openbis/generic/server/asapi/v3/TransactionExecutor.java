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
public class TransactionExecutor implements ITransactionExecutor
{

    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, TransactionExecutor.class);

    private final Map<String, TransactionThread> threadMap = new HashMap<>();

    private final ITransactionProvider provider;

    TransactionExecutor(ITransactionProvider provider)
    {
        this.provider = provider;
    }

    @Autowired
    public TransactionExecutor(final PlatformTransactionManager transactionManager, final IDAOFactory daoFactory,
            final DatabaseConfigurationContext databaseContext)
    {
        this.provider = new ITransactionProvider()
        {
            @Override public Object beginTransaction(final String transactionId) throws Exception
            {
                return transactionManager.getTransaction(new DefaultTransactionDefinition());
            }

            @Override public void prepareTransaction(final String transactionId, final Object transaction) throws Exception
            {
                Session session = daoFactory.getSessionFactory().getCurrentSession();
                session.flush();
                session.doWork(connection ->
                {
                    PreparedStatement statement = connection.prepareStatement("PREPARE TRANSACTION '" + transactionId + "'");
                    statement.execute();
                });
            }

            @Override public void rollbackTransaction(final String transactionId, final Object transaction) throws Exception
            {
                transactionManager.rollback((TransactionStatus) transaction);
            }

            @Override public void rollbackPreparedTransaction(final String transactionId, final Object transaction) throws Exception
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
                }
            }

            @Override public void commitPreparedTransaction(final String transactionId, final Object transaction) throws Exception
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
    }

    @Override public void beginTransaction(final String transactionId, final String transactionManagerSecret) throws Throwable
    {
        executeOperation(transactionId, transactionManagerSecret, new BeginTransactionOperation());
    }

    @Override public void prepareTransaction(final String transactionId, final String transactionManagerSecret) throws Throwable
    {
        executeOperation(transactionId, transactionManagerSecret, new PrepareTransactionOperation());
    }

    @Override public void commitTransaction(final String transactionId, final String transactionManagerSecret) throws Throwable
    {
        executeOperation(transactionId, transactionManagerSecret, new CommitTransactionOperation());
    }

    @Override public void rollbackTransaction(final String transactionId, final String transactionManagerSecret) throws Throwable
    {
        executeOperation(transactionId, transactionManagerSecret, new RollbackTransactionOperation());
    }

    public Object executeOperation(String transactionId, String transactionManagerSecret, ITransactionOperation operation) throws Throwable
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
                return thread.execute(transactionManagerSecret, operation);
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

    enum TransactionThreadStatus
    {
        NEW, STARTED, PREPARED, COMMITTED, ROLLED_BACK
    }

    class TransactionThread
    {

        private final Object lock = new Object();

        private final Thread thread;

        private final String transactionId;

        private String transactionManagerSecret;

        private Object transaction;

        private TransactionThreadStatus status = TransactionThreadStatus.NEW;

        private ITransactionOperation invocation;

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
                                        checkTransactionStatus(TransactionThreadStatus.NEW);
                                        checkTransactionManagerSecret(transactionManagerSecret);

                                        result = transaction = provider.beginTransaction(transactionId);
                                        status = TransactionThreadStatus.STARTED;
                                    } else if (invocation instanceof PrepareTransactionOperation)
                                    {
                                        checkTransactionStatus(TransactionThreadStatus.STARTED);
                                        checkTransactionManagerSecret(transactionManagerSecret);

                                        provider.prepareTransaction(transactionId, transaction);
                                        status = TransactionThreadStatus.PREPARED;
                                    } else if (invocation instanceof CommitTransactionOperation)
                                    {
                                        checkTransactionStatus(TransactionThreadStatus.PREPARED);
                                        checkTransactionManagerSecret(transactionManagerSecret);

                                        provider.commitPreparedTransaction(transactionId, transaction);
                                        status = TransactionThreadStatus.COMMITTED;
                                    } else if (invocation instanceof RollbackTransactionOperation)
                                    {
                                        checkTransactionStatus(TransactionThreadStatus.STARTED, TransactionThreadStatus.PREPARED);
                                        checkTransactionManagerSecret(transactionManagerSecret);

                                        if (TransactionThreadStatus.STARTED.equals(status))
                                        {
                                            provider.rollbackTransaction(transactionId, transaction);
                                        } else if (TransactionThreadStatus.PREPARED.equals(status))
                                        {
                                            provider.rollbackPreparedTransaction(transactionId, transaction);
                                        }

                                        status = TransactionThreadStatus.ROLLED_BACK;
                                    } else
                                    {
                                        checkTransactionStatus(TransactionThreadStatus.STARTED);
                                        result = invocation.executeOperation();
                                    }

                                    operationLog.info("Two phase transaction " + transactionId + " method " + invocation.getOperationName()
                                            + " executed.");

                                    exception = null;
                                    invocation = null;

                                    if (status == TransactionThreadStatus.COMMITTED || status == TransactionThreadStatus.ROLLED_BACK)
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

                                if (status == TransactionThreadStatus.NEW)
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

                private void checkTransactionStatus(TransactionThreadStatus... expectedStatuses)
                {
                    if (!Arrays.asList(expectedStatuses).contains(status))
                    {
                        throw new IllegalStateException(
                                "Two phase transaction " + transactionId + " unexpected status " + status + ". Expected statuses "
                                        + Arrays.toString(expectedStatuses) + ".");
                    }
                }

                private void checkTransactionManagerSecret(String secret)
                {
                    if (secret == null || secret.isBlank())
                    {
                        throw new IllegalStateException("Two phase transaction manager secret missing.");
                    }
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

        public Object execute(String newTransactionManagerSecret, ITransactionOperation newInvocation) throws Throwable
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

                    transactionManagerSecret = newTransactionManagerSecret;
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
