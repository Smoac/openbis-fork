package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.ITransactionCoordinatorApi;
import ch.ethz.sis.openbis.generic.asapi.v3.ITransactionParticipantApi;
import ch.ethz.sis.transaction.IDatabaseTransactionProvider;
import ch.ethz.sis.transaction.ITransactionOperationExecutor;
import ch.ethz.sis.transaction.TransactionLog;
import ch.ethz.sis.transaction.TransactionParticipant;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;

@Component
public class TransactionParticipantApi implements ITransactionParticipantApi, ApplicationListener<ApplicationEvent>
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, TransactionParticipant.class);

    private static final String TRANSACTION_LOG_FOLDER_NAME = "participant-application-server";

    private static final String FINISH_TRANSACTIONS_THREAD_NAME = "participant-application-server-finish-transactions";

    private final TransactionConfiguration transactionConfiguration;

    private final TransactionParticipant transactionParticipant;

    @Autowired
    public TransactionParticipantApi(final TransactionConfiguration transactionConfiguration, final PlatformTransactionManager transactionManager,
            final IDAOFactory daoFactory, final DatabaseConfigurationContext databaseContext, final IApplicationServerApi applicationServerApi, final
    IOpenBisSessionManager sessionManager)
    {
        this(transactionConfiguration, transactionManager, daoFactory, databaseContext, applicationServerApi, sessionManager,
                ITransactionCoordinatorApi.APPLICATION_SERVER_PARTICIPANT_ID, TRANSACTION_LOG_FOLDER_NAME);
    }

    public TransactionParticipantApi(final TransactionConfiguration transactionConfiguration, final PlatformTransactionManager transactionManager,
            final IDAOFactory daoFactory, final DatabaseConfigurationContext databaseContext, final IApplicationServerApi applicationServerApi,
            final IOpenBisSessionManager sessionManager,
            final String participantId, final String logFolderName)
    {
        this.transactionConfiguration = transactionConfiguration;
        this.transactionParticipant = new TransactionParticipant(
                participantId,
                transactionConfiguration.getCoordinatorKey(),
                transactionConfiguration.getInteractiveSessionKey(),
                new ApplicationServerSessionTokenProvider(sessionManager),
                new ApplicationServerDatabaseTransactionProvider(transactionManager, daoFactory, databaseContext),
                new ApplicationServerTransactionOperationExecutor(applicationServerApi),
                new TransactionLog(new File(transactionConfiguration.getTransactionLogFolderPath()), logFolderName),
                transactionConfiguration.getTransactionTimeoutInSeconds(),
                transactionConfiguration.getTransactionCountLimit()
        );
    }

    @Override public void onApplicationEvent(final ApplicationEvent event)
    {
        Object source = event.getSource();
        if (source instanceof AbstractApplicationContext)
        {
            AbstractApplicationContext appContext = (AbstractApplicationContext) source;
            if ((event instanceof ContextStartedEvent) || (event instanceof ContextRefreshedEvent))
            {
                if (appContext.getParent() != null)
                {
                    this.transactionParticipant.recoverTransactionsFromTransactionLog();

                    new Timer(FINISH_TRANSACTIONS_THREAD_NAME, true).schedule(new TimerTask()
                                                                              {
                                                                                  @Override public void run()
                                                                                  {
                                                                                      TransactionParticipantApi.this.transactionParticipant.finishFailedOrAbandonedTransactions();
                                                                                  }
                                                                              },
                            transactionConfiguration.getFinishTransactionsIntervalInSeconds() * 1000L,
                            transactionConfiguration.getFinishTransactionsIntervalInSeconds() * 1000L);
                }
            }
        }
    }

    @Override public String getParticipantId()
    {
        return transactionParticipant.getParticipantId();
    }

    @Override public void beginTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
            final String transactionCoordinatorKey)
    {
        transactionParticipant.beginTransaction(transactionId, sessionToken, interactiveSessionKey, transactionCoordinatorKey);
    }

    @Override public <T> T executeOperation(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
            final String operationName, final Object[] operationArguments)
    {
        return transactionParticipant.executeOperation(transactionId, sessionToken, interactiveSessionKey, operationName, operationArguments);
    }

    @Override public void prepareTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
            final String transactionCoordinatorKey)
    {
        transactionParticipant.prepareTransaction(transactionId, sessionToken, interactiveSessionKey, transactionCoordinatorKey);
    }

    @Override public void commitTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        transactionParticipant.commitTransaction(transactionId, sessionToken, interactiveSessionKey);
    }

    @Override public void commitRecoveredTransaction(final UUID transactionId, final String interactiveSessionKey,
            final String transactionCoordinatorKey)
    {
        transactionParticipant.commitRecoveredTransaction(transactionId, interactiveSessionKey, transactionCoordinatorKey);
    }

    @Override public void rollbackTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        transactionParticipant.rollbackTransaction(transactionId, sessionToken, interactiveSessionKey);
    }

    @Override public void rollbackRecoveredTransaction(final UUID transactionId, final String interactiveSessionKey,
            final String transactionCoordinatorKey)
    {
        transactionParticipant.rollbackRecoveredTransaction(transactionId, interactiveSessionKey, transactionCoordinatorKey);
    }

    @Override public List<UUID> recoverTransactions(final String interactiveSessionKey, final String transactionCoordinatorKey)
    {
        return transactionParticipant.recoverTransactions(interactiveSessionKey, transactionCoordinatorKey);
    }

    @Override public int getMajorVersion()
    {
        return 1;
    }

    @Override public int getMinorVersion()
    {
        return 0;
    }

    private static class ApplicationServerDatabaseTransactionProvider implements IDatabaseTransactionProvider
    {
        private final PlatformTransactionManager transactionManager;

        private final IDAOFactory daoFactory;

        private final DatabaseConfigurationContext databaseContext;

        public ApplicationServerDatabaseTransactionProvider(final PlatformTransactionManager transactionManager, final IDAOFactory daoFactory,
                final DatabaseConfigurationContext databaseContext)
        {
            this.transactionManager = transactionManager;
            this.daoFactory = daoFactory;
            this.databaseContext = databaseContext;
        }

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

        @Override public void rollbackTransaction(final UUID transactionId, final Object transaction, final boolean isTwoPhaseTransaction)
                throws Exception
        {
            if (isTwoPhaseTransaction)
            {
                try
                {
                    if (isTransactionPreparedInDatabase(transactionId))
                    {
                        try (Connection connection = databaseContext.getDataSource().getConnection();
                                PreparedStatement rollbackStatement = connection.prepareStatement("ROLLBACK PREPARED '" + transactionId + "'"))
                        {
                            rollbackStatement.execute();
                            operationLog.info("Prepared database transaction '" + transactionId + "' was rolled back.");
                        }
                    } else
                    {
                        operationLog.info(
                                "Prepared database transaction '" + transactionId + "' was not found in the database. Nothing to roll back.");
                    }
                } finally
                {
                    if (transaction != null)
                    {
                        try
                        {
                            transactionManager.rollback((TransactionStatus) transaction);
                        } catch (Exception e)
                        {
                            operationLog.warn(
                                    "Prepared database transaction '" + transactionId + "' could not be rolled back in the transaction manager.",
                                    e);
                        }
                    }
                }
            } else
            {
                if (transaction != null)
                {
                    transactionManager.rollback((TransactionStatus) transaction);
                    operationLog.info("Database transaction '" + transactionId + "' was rolled back.");
                } else
                {
                    RuntimeException exception = new IllegalStateException(
                            "Database transaction '" + transactionId + "' could not be rolled back because of missing transaction status object.");
                    operationLog.error(exception.getMessage());
                    throw exception;
                }
            }
        }

        @Override public void commitTransaction(final UUID transactionId, final Object transaction, final boolean isTwoPhaseTransaction)
                throws Exception
        {
            if (isTwoPhaseTransaction)
            {
                try
                {
                    if (isTransactionPreparedInDatabase(transactionId))
                    {
                        try (Connection connection = databaseContext.getDataSource().getConnection();
                                PreparedStatement commitStatement = connection.prepareStatement("COMMIT PREPARED '" + transactionId + "'"))
                        {
                            commitStatement.execute();
                            operationLog.info("Prepared database transaction '" + transactionId + "' was committed.");
                        }
                    } else
                    {
                        RuntimeException exception = new IllegalStateException(
                                "Prepared database transaction '" + transactionId + "' was not found in the database and could not be committed.");
                        operationLog.error(exception.getMessage());
                        throw exception;
                    }
                }finally{
                    if (transaction != null)
                    {
                        try
                        {
                            transactionManager.commit((TransactionStatus) transaction);
                        } catch (Exception e)
                        {
                            operationLog.warn(
                                    "Prepared database transaction '" + transactionId + "' could not be committed in the transaction manager.",
                                    e);
                        }
                    }
                }
            } else
            {
                if (transaction != null)
                {
                    transactionManager.commit((TransactionStatus) transaction);
                    operationLog.info("Database transaction '" + transactionId + "' was committed.");
                } else
                {
                    RuntimeException exception = new IllegalStateException(
                            "Database transaction '" + transactionId + "' could not be committed because of missing transaction status object.");
                    operationLog.error(exception.getMessage());
                    throw exception;
                }
            }
        }

        boolean isTransactionPreparedInDatabase(final UUID transactionId) throws Exception
        {
            try (Connection connection = databaseContext.getDataSource().getConnection();
                    PreparedStatement countStatement = connection.prepareStatement("SELECT count(*) AS count FROM pg_prepared_xacts WHERE gid = ?"))
            {
                countStatement.setString(1, transactionId.toString());

                try (ResultSet countResult = countStatement.executeQuery())
                {
                    if (countResult.next())
                    {
                        int count = countResult.getInt("count");
                        return count > 0;
                    }
                }
            }

            return false;
        }
    }

    private static class ApplicationServerTransactionOperationExecutor implements ITransactionOperationExecutor
    {

        private final IApplicationServerApi applicationServerApi;

        public ApplicationServerTransactionOperationExecutor(final IApplicationServerApi applicationServerApi)
        {
            this.applicationServerApi = applicationServerApi;
        }

        @Override public <T> T executeOperation(String sessionToken, String operationName, Object[] operationArguments)
        {
            for (Method method : applicationServerApi.getClass().getMethods())
            {
                if (method.getName().equals(operationName))
                {
                    try
                    {
                        return (T) method.invoke(applicationServerApi, operationArguments);
                    } catch (InvocationTargetException e)
                    {
                        Throwable originalException = e.getTargetException();

                        if (originalException instanceof RuntimeException)
                        {
                            throw (RuntimeException) originalException;
                        } else
                        {
                            throw new RuntimeException(originalException);
                        }
                    } catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }

            throw new IllegalArgumentException("Unknown operation  '" + operationName + "'.");
        }
    }

    public TransactionParticipant getTransactionParticipant()
    {
        return transactionParticipant;
    }

}
