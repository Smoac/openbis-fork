package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.ITransactionCoordinatorApi;
import ch.ethz.sis.openbis.generic.asapi.v3.ITransactionParticipantApi;
import ch.ethz.sis.transaction.IDatabaseTransactionProvider;
import ch.ethz.sis.transaction.ISessionTokenProvider;
import ch.ethz.sis.transaction.ITransactionOperationExecutor;
import ch.ethz.sis.transaction.ITransactionParticipant;
import ch.ethz.sis.transaction.TransactionLog;
import ch.ethz.sis.transaction.TransactionOperationException;
import ch.ethz.sis.transaction.TransactionParticipant;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;

@Component
public class TransactionParticipantApi implements ITransactionParticipantApi
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, TransactionParticipant.class);

    private final ITransactionParticipant transactionParticipant;

    @Autowired
    public TransactionParticipantApi(final TransactionConfiguration transactionConfiguration, final PlatformTransactionManager transactionManager,
            final IDAOFactory daoFactory, final DatabaseConfigurationContext databaseContext, final IApplicationServerApi applicationServerApi)
    {
        this.transactionParticipant = new TransactionParticipant(
                ITransactionCoordinatorApi.APPLICATION_SERVER_PARTICIPANT_ID,
                transactionConfiguration.getCoordinatorKey(),
                transactionConfiguration.getInteractiveSessionKey(),
                new ApplicationServerSessionTokenProvider(applicationServerApi),
                new ApplicationServerDatabaseTransactionProvider(transactionManager, daoFactory, databaseContext),
                new ApplicationServerTransactionOperationExecutor(applicationServerApi),
                new TransactionLog(new File(transactionConfiguration.getTransactionLogFolderPath()), "application-server"),
                transactionConfiguration.getTransactionTimeoutInSeconds(),
                transactionConfiguration.getTransactionCountLimit()
        );
    }

    @Override public String getParticipantId()
    {
        return transactionParticipant.getParticipantId();
    }

    @Override public void beginTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey, final String transactionCoordinatorKey)
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

    @Override public void commitTransaction(final UUID transactionId, final String transactionCoordinatorKey)
    {
        transactionParticipant.commitTransaction(transactionId, transactionCoordinatorKey);
    }

    @Override public void rollbackTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        transactionParticipant.rollbackTransaction(transactionId, sessionToken, interactiveSessionKey);
    }

    @Override public void rollbackTransaction(final UUID transactionId, final String transactionCoordinatorKey)
    {
        transactionParticipant.rollbackTransaction(transactionId, transactionCoordinatorKey);
    }

    @Override public List<UUID> getTransactions(final String transactionCoordinatorKey)
    {
        return null;
    }

    @Override public int getMajorVersion()
    {
        return 1;
    }

    @Override public int getMinorVersion()
    {
        return 0;
    }

    private static class ApplicationServerSessionTokenProvider implements ISessionTokenProvider
    {

        private final IApplicationServerApi applicationServerApi;

        public ApplicationServerSessionTokenProvider(final IApplicationServerApi applicationServerApi)
        {
            this.applicationServerApi = applicationServerApi;
        }

        @Override public boolean isValid(final String sessionToken)
        {
            return applicationServerApi.isSessionActive(sessionToken);
        }
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
    }

    private static class ApplicationServerTransactionOperationExecutor implements ITransactionOperationExecutor
    {

        private final IApplicationServerApi applicationServerApi;

        public ApplicationServerTransactionOperationExecutor(final IApplicationServerApi applicationServerApi)
        {
            this.applicationServerApi = applicationServerApi;
        }

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
    }

}
