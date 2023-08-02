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
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;

@Component
public class TransactionParticipantService implements ITransactionParticipantService
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, TransactionParticipant.class);

    private static final String TRANSACTION_COORDINATOR_KEY = "test-transaction-coordinator-key";

    private static final String INTERACTIVE_SESSION_KEY = "test-interactive-session-key";

    private static final String TRANSACTION_LOG_PATH = "transaction-logs";

    private final ITransactionParticipant transactionParticipant;

    @Autowired
    public TransactionParticipantService(final PlatformTransactionManager transactionManager, final IDAOFactory daoFactory,
            final DatabaseConfigurationContext databaseContext, final IApplicationServerApi applicationServerApi)
    {
        this.transactionParticipant = new TransactionParticipant(
                PARTICIPANT_ID,
                TRANSACTION_COORDINATOR_KEY,
                INTERACTIVE_SESSION_KEY,
                new ApplicationServerSessionTokenProvider(applicationServerApi),
                new ApplicationServerDatabaseTransactionProvider(transactionManager, daoFactory, databaseContext),
                new ApplicationServerTransactionOperationExecutor(applicationServerApi),
                new TransactionLog(new File(TRANSACTION_LOG_PATH))
        );
    }

    @Override public String getParticipantId()
    {
        return transactionParticipant.getParticipantId();
    }

    @Override public void beginTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        transactionParticipant.beginTransaction(transactionId, sessionToken, interactiveSessionKey);
    }

    @Override public Object executeOperation(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
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
