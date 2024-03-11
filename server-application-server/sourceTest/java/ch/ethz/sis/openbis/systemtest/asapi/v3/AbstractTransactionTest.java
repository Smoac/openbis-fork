package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IIdentifierHolder;
import ch.ethz.sis.openbis.generic.server.asapi.v3.ApplicationServerSessionTokenProvider;
import ch.ethz.sis.openbis.generic.server.asapi.v3.TransactionConfiguration;
import ch.ethz.sis.openbis.generic.server.asapi.v3.TransactionParticipantApi;
import ch.ethz.sis.transaction.AbstractTransaction;
import ch.ethz.sis.transaction.IDatabaseTransactionProvider;
import ch.ethz.sis.transaction.ITransactionParticipant;
import ch.ethz.sis.transaction.TransactionCoordinator;
import ch.ethz.sis.transaction.TransactionLog;
import ch.ethz.sis.transaction.TransactionParticipant;
import ch.ethz.sis.transaction.TransactionStatus;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;

public class AbstractTransactionTest extends AbstractTest
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, AbstractTransactionTest.class);

    public static final String TEST_COORDINATOR_KEY = "test-transaction-coordinator-key";

    public static final String TEST_INTERACTIVE_SESSION_KEY = "test-interactive-session-key";

    public static final String TEST_PARTICIPANT_1_ID = "test-participant-1";

    public static final String TEST_PARTICIPANT_2_ID = "test-participant-2";

    public static final String TRANSACTION_LOG_ROOT_FOLDER = "targets/transaction-logs";

    public static final String TRANSACTION_LOG_COORDINATOR_FOLDER = "test-coordinator";

    public static final String TRANSACTION_LOG_PARTICIPANT_1_FOLDER = "test-participant-1";

    public static final String TRANSACTION_LOG_PARTICIPANT_2_FOLDER = "test-participant-2";

    public static final String OPERATION_CREATE_SPACES = "createSpaces";

    public static final String OPERATION_CREATE_PROJECTS = "createProjects";

    public static final String OPERATION_SEARCH_SPACES = "searchSpaces";

    public static final String OPERATION_SEARCH_PROJECTS = "searchProjects";

    public static final String CODE_PREFIX = "TRANSACTION_TEST_";

    @Autowired
    public TransactionConfiguration transactionConfiguration;

    @Autowired
    public PlatformTransactionManager transactionManager;

    @Autowired
    public IDAOFactory daoFactory;

    @Autowired
    public DatabaseConfigurationContext databaseContext;

    @Autowired
    public IOpenBisSessionManager sessionManager;

    public TransactionCoordinator createCoordinator(List<ITransactionParticipant> participants, int transactionTimeoutInSeconds,
            int transactionCountLimit)
    {
        return new TransactionCoordinator(TEST_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY,
                new ApplicationServerSessionTokenProvider(sessionManager),
                participants, new TransactionLog(new File(TRANSACTION_LOG_ROOT_FOLDER), TRANSACTION_LOG_COORDINATOR_FOLDER),
                transactionTimeoutInSeconds, transactionCountLimit);
    }

    public TestTransactionParticipant createParticipant(TransactionConfiguration configuration, String participantId, String logFolderName)
    {
        return new TestTransactionParticipant(
                new TransactionParticipantApi(configuration, transactionManager, daoFactory, databaseContext, v3api, sessionManager, participantId,
                        logFolderName));
    }

    public TransactionConfiguration createConfiguration(final int transactionTimeoutInSeconds, final int transactionCountLimit)
    {
        return new TransactionConfiguration()
        {
            @Override public boolean isEnabled()
            {
                return true;
            }

            public int getTransactionTimeoutInSeconds()
            {
                return transactionTimeoutInSeconds;
            }

            public int getFinishTransactionsIntervalInSeconds()
            {
                return transactionConfiguration.getFinishTransactionsIntervalInSeconds();
            }

            public String getInteractiveSessionKey()
            {
                return transactionConfiguration.getInteractiveSessionKey();
            }

            public String getCoordinatorKey()
            {
                return transactionConfiguration.getCoordinatorKey();
            }

            public String getTransactionLogFolderPath()
            {
                return transactionConfiguration.getTransactionLogFolderPath();
            }

            public int getTransactionCountLimit()
            {
                return transactionCountLimit;
            }

            public String getApplicationServerUrl()
            {
                return transactionConfiguration.getApplicationServerUrl();
            }

            public int getApplicationServerTimeoutInSeconds()
            {
                return transactionConfiguration.getApplicationServerTimeoutInSeconds();
            }

            public String getAfsServerUrl()
            {
                return transactionConfiguration.getAfsServerUrl();
            }

            public int getAfsServerTimeoutInSeconds()
            {
                return transactionConfiguration.getAfsServerTimeoutInSeconds();
            }
        };
    }

    public void rollbackPreparedDatabaseTransactions() throws Exception
    {
        try (Connection connection = databaseContext.getDataSource().getConnection(); Statement statement = connection.createStatement())
        {
            List<String> preparedTransactionIds = new ArrayList<>();

            ResultSet preparedTransactions = statement.executeQuery("SELECT gid FROM pg_prepared_xacts");
            while (preparedTransactions.next())
            {
                preparedTransactionIds.add(preparedTransactions.getString(1));
            }

            for (String preparedTransactionId : preparedTransactionIds)
            {
                statement.execute("ROLLBACK PREPARED '" + preparedTransactionId + "'");
            }
        }
    }

    public void deleteCreatedSpacesAndProjects() throws Exception
    {
        try (Connection connection = databaseContext.getDataSource().getConnection(); Statement statement = connection.createStatement())
        {
            statement.execute("DELETE FROM projects WHERE code LIKE '" + CODE_PREFIX + "%'");
            statement.execute("DELETE FROM spaces WHERE code LIKE '" + CODE_PREFIX + "%'");
        }
    }

    public static class TestTransactionParticipant implements ITransactionParticipant
    {

        // Map the original transaction id coming from the coordinator to a unique transaction id for each participant,
        // this way we can have multiple participants preparing the transaction on the same test database.

        private final Map<UUID, UUID> originalToInternalId = new HashMap<>();

        private final Map<UUID, UUID> internalToOriginalId = new HashMap<>();

        private boolean mapTransactions = false;

        private final TransactionParticipant participant;

        private final TestDatabaseTransactionProvider databaseTransactionProvider;

        public TestTransactionParticipant(TransactionParticipantApi participantApi)
        {
            // replace the original database transaction provider with a test counterpart that allows to throw test exceptions
            this.participant = participantApi.getTransactionParticipant();
            this.databaseTransactionProvider =
                    new TestDatabaseTransactionProvider(participantApi.getTransactionParticipant().getDatabaseTransactionProvider());
            this.participant.setDatabaseTransactionProvider(databaseTransactionProvider);
        }

        public TestDatabaseTransactionProvider getDatabaseTransactionProvider()
        {
            return this.databaseTransactionProvider;
        }

        @Override public String getParticipantId()
        {
            return participant.getParticipantId();
        }

        @Override public void beginTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            this.participant.beginTransaction(mapOriginalToInternalId(transactionId), sessionToken, interactiveSessionKey,
                    transactionCoordinatorKey);
        }

        @Override public <T> T executeOperation(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String operationName, final Object[] operationArguments)
        {
            return participant.executeOperation(mapOriginalToInternalId(transactionId), sessionToken, interactiveSessionKey, operationName,
                    operationArguments);
        }

        @Override public void prepareTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            participant.prepareTransaction(mapOriginalToInternalId(transactionId), sessionToken, interactiveSessionKey, transactionCoordinatorKey);
        }

        @Override public void commitTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
        {
            participant.commitTransaction(mapOriginalToInternalId(transactionId), sessionToken, interactiveSessionKey);
        }

        @Override public void commitRecoveredTransaction(final UUID transactionId, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            participant.commitRecoveredTransaction(mapOriginalToInternalId(transactionId), interactiveSessionKey, transactionCoordinatorKey);
        }

        @Override public void rollbackTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
        {
            participant.rollbackTransaction(mapOriginalToInternalId(transactionId), sessionToken, interactiveSessionKey);
        }

        @Override public void rollbackRecoveredTransaction(final UUID transactionId, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            participant.rollbackRecoveredTransaction(mapOriginalToInternalId(transactionId), interactiveSessionKey, transactionCoordinatorKey);
        }

        @Override public List<UUID> recoverTransactions(final String interactiveSessionKey, final String transactionCoordinatorKey)
        {
            List<UUID> transactionIds = new ArrayList<>();

            for (UUID internalTransactionId : participant.recoverTransactions(interactiveSessionKey, transactionCoordinatorKey))
            {
                transactionIds.add(mapInternalToOriginalId(internalTransactionId));
            }

            return transactionIds;
        }

        public void recoverTransactionsFromTransactionLog()
        {
            participant.recoverTransactionsFromTransactionLog();
        }

        public void finishFailedOrAbandonedTransactions()
        {
            participant.finishFailedOrAbandonedTransactions();
        }

        public Map<UUID, ? extends AbstractTransaction> getTransactionMap()
        {
            Map<UUID, AbstractTransaction> transactionMap = new HashMap<>();

            for (Map.Entry<UUID, ? extends AbstractTransaction> entry : this.participant.getTransactionMap().entrySet())
            {
                TransactionParticipant.Transaction internalTransaction = (TransactionParticipant.Transaction) entry.getValue();
                AbstractTransaction originalTransaction =
                        new TransactionParticipant.Transaction(mapInternalToOriginalId(internalTransaction.getTransactionId()),
                                internalTransaction.getSessionToken());
                originalTransaction.setTransactionStatus(internalTransaction.getTransactionStatus());
                originalTransaction.setLastAccessedDate(internalTransaction.getLastAccessedDate());
                transactionMap.put(originalTransaction.getTransactionId(), originalTransaction);
            }

            return transactionMap;
        }

        public void setTestTransactionMapping(Map<UUID, UUID> coordinatorIdToParticipantIdMap)
        {
            for (Map.Entry<UUID, UUID> entry : coordinatorIdToParticipantIdMap.entrySet())
            {
                originalToInternalId.put(entry.getKey(), entry.getValue());
                internalToOriginalId.put(entry.getValue(), entry.getKey());
            }

            mapTransactions = true;
        }

        public Map<UUID, UUID> getTestTransactionMapping()
        {
            return originalToInternalId;
        }

        private UUID mapInternalToOriginalId(UUID internalId)
        {
            if (mapTransactions)
            {
                return internalToOriginalId.get(internalId);
            } else
            {
                return internalId;
            }
        }

        private UUID mapOriginalToInternalId(UUID originalId)
        {
            if (mapTransactions)
            {
                return originalToInternalId.get(originalId);
            } else
            {
                return originalId;
            }
        }

        public void close()
        {
            for (TransactionParticipant.Transaction transaction : participant.getTransactionMap().values())
            {
                try
                {
                    transaction.lockOrFail(() ->
                    {
                        transaction.close();
                        return null;
                    }, false);
                } catch (Exception e)
                {
                    operationLog.warn("Could not close transaction '" + transaction.getTransactionId() + "'.", e);
                }
            }
        }
    }

    public static class TestDatabaseTransactionProvider implements IDatabaseTransactionProvider
    {

        private final IDatabaseTransactionProvider databaseTransactionProvider;

        private Runnable beginAction;

        private Runnable prepareAction;

        private Runnable commitAction;

        private Runnable rollbackAction;

        public TestDatabaseTransactionProvider(IDatabaseTransactionProvider databaseTransactionProvider)
        {
            this.databaseTransactionProvider = databaseTransactionProvider;
        }

        @Override public Object beginTransaction(final UUID transactionId) throws Exception
        {
            if (beginAction != null)
            {
                beginAction.run();
            }
            return databaseTransactionProvider.beginTransaction(transactionId);
        }

        @Override public void prepareTransaction(final UUID transactionId, final Object transaction) throws Exception
        {
            if (prepareAction != null)
            {
                prepareAction.run();
            }
            databaseTransactionProvider.prepareTransaction(transactionId, transaction);
        }

        @Override public void rollbackTransaction(final UUID transactionId, final Object transaction, final boolean isTwoPhaseTransaction)
                throws Exception
        {
            if (rollbackAction != null)
            {
                rollbackAction.run();
            }
            databaseTransactionProvider.rollbackTransaction(transactionId, transaction, isTwoPhaseTransaction);
        }

        @Override public void commitTransaction(final UUID transactionId, final Object transaction, final boolean isTwoPhaseTransaction)
                throws Exception
        {
            if (commitAction != null)
            {
                commitAction.run();
            }
            databaseTransactionProvider.commitTransaction(transactionId, transaction, isTwoPhaseTransaction);
        }

        public void setBeginAction(final Runnable beginAction)
        {
            this.beginAction = beginAction;
        }

        public void setPrepareAction(final Runnable prepareAction)
        {
            this.prepareAction = prepareAction;
        }

        public void setCommitAction(final Runnable commitAction)
        {
            this.commitAction = commitAction;
        }

        public void setRollbackAction(final Runnable rollbackAction)
        {
            this.rollbackAction = rollbackAction;
        }

    }

    public static void assertTransactions(Map<UUID, ? extends AbstractTransaction> actualTransactions, TestTransaction... expectedTransactions)
    {
        Map<String, String> actualTransactionsMap = new TreeMap<>();
        Map<String, String> expectedTransactionsMap = new TreeMap<>();

        for (AbstractTransaction actualTransaction : actualTransactions.values())
        {
            actualTransactionsMap.put(actualTransaction.getTransactionId().toString(), actualTransaction.getTransactionStatus().toString());
        }

        for (TestTransaction expectedTransaction : expectedTransactions)
        {
            expectedTransactionsMap.put(expectedTransaction.getTransactionId().toString(), expectedTransaction.getTransactionStatus().toString());
        }

        assertEquals(actualTransactionsMap.toString(), expectedTransactionsMap.toString());
    }

    public static class TestTransaction
    {

        private final UUID transactionId;

        private final TransactionStatus transactionStatus;

        TestTransaction(final UUID transactionId, final TransactionStatus transactionStatus)
        {
            this.transactionId = transactionId;
            this.transactionStatus = transactionStatus;
        }

        public UUID getTransactionId()
        {
            return transactionId;
        }

        public TransactionStatus getTransactionStatus()
        {
            return transactionStatus;
        }
    }

    public static Set<String> codes(Collection<? extends ICodeHolder> objectsWithCodes)
    {
        return objectsWithCodes.stream().map(ICodeHolder::getCode).collect(Collectors.toSet());
    }

    public static Set<String> identifiers(Collection<? extends IIdentifierHolder> objectsWithIdentifiers)
    {
        return objectsWithIdentifiers.stream().map(o -> o.getIdentifier().getIdentifier()).collect(Collectors.toSet());
    }

    public static <T> Set<T> difference(Set<T> s1, Set<T> s2)
    {
        Set<T> temp = new HashSet<>(s1);
        temp.removeAll(s2);
        return temp;
    }

}
