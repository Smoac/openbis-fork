package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IIdentifierHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.TransactionConfiguration;
import ch.ethz.sis.openbis.generic.server.asapi.v3.TransactionParticipantApi;
import ch.ethz.sis.transaction.IDatabaseTransactionProvider;
import ch.ethz.sis.transaction.ISessionTokenProvider;
import ch.ethz.sis.transaction.ITransactionParticipant;
import ch.ethz.sis.transaction.Transaction;
import ch.ethz.sis.transaction.TransactionCoordinator;
import ch.ethz.sis.transaction.TransactionLog;
import ch.ethz.sis.transaction.TransactionParticipant;
import ch.ethz.sis.transaction.TransactionStatus;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;

public class TransactionTest extends AbstractTest
{

    private static final String OPERATION_CREATE_SPACES = "createSpaces";

    private static final String OPERATION_CREATE_PROJECTS = "createProjects";

    private static final String OPERATION_SEARCH_SPACES = "searchSpaces";

    private static final String OPERATION_SEARCH_PROJECTS = "searchProjects";

    private static final String TEST_PARTICIPANT_1_ID = "test-participant-1";

    private static final String TEST_PARTICIPANT_2_ID = "test-participant-2";

    private static final String TEST_COORDINATOR_LOG_FOLDER_NAME = "test-coordinator";

    private static final String TEST_PARTICIPANT_1_LOG_FOLDER_NAME = "test-participant-1";

    private static final String TEST_PARTICIPANT_2_LOG_FOLDER_NAME = "test-participant-2";

    /* These keys need to match keys defined in service.properties */
    private static final String TEST_COORDINATOR_KEY = "test-transaction-coordinator-key";

    private static final String TEST_INTERACTIVE_SESSION_KEY = "test-interactive-session-key";

    @Autowired
    private TransactionConfiguration transactionConfiguration;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private DatabaseConfigurationContext databaseContext;

    @Autowired
    private IApplicationServerApi applicationServerApi;

    private TransactionCoordinator coordinator;

    private TestTransactionParticipant participant1;

    private TestTransactionParticipant participant2;

    @BeforeMethod
    private void init()
    {
        participant1 =
                new TestTransactionParticipant(
                        new TransactionParticipantApi(transactionConfiguration, transactionManager, daoFactory, databaseContext, applicationServerApi,
                                TEST_PARTICIPANT_1_ID, TEST_PARTICIPANT_1_LOG_FOLDER_NAME));
        participant2 =
                new TestTransactionParticipant(
                        new TransactionParticipantApi(transactionConfiguration, transactionManager, daoFactory, databaseContext, applicationServerApi,
                                TEST_PARTICIPANT_2_ID, TEST_PARTICIPANT_2_LOG_FOLDER_NAME));

        coordinator = new TransactionCoordinator(TEST_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, new ISessionTokenProvider()
        {
            @Override public boolean isValid(final String sessionToken)
            {
                return v3api.isSessionActive(sessionToken);
            }
        }, Arrays.asList(participant1, participant2), new TransactionLog(new File("targets/transaction-logs"), TEST_COORDINATOR_LOG_FOLDER_NAME), 60,
                10);
    }

    @Test
    public void testBeginTransactionFails()
    {
        // "begin" should fail
        participant2.getDatabaseTransactionProvider().setBeginException(new RuntimeException("Test begin exception"));

        assertTransactions(coordinator.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        UUID trId = UUID.randomUUID();

        try
        {
            coordinator.beginTransaction(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);
            fail();
        } catch (Exception e)
        {
            assertEquals(e, participant2.getDatabaseTransactionProvider().getBeginException());
        }

        assertTransactions(coordinator.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());
    }

    @Test
    public void testExecuteOperationFails()
    {
        assertTransactions(coordinator.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        UUID trId = UUID.randomUUID();

        coordinator.beginTransaction(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        SpaceCreation spaceCreation1 = new SpaceCreation();
        spaceCreation1.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(), OPERATION_CREATE_SPACES,
                new Object[] { sessionToken, Collections.singletonList(spaceCreation1) });

        try
        {
            coordinator.executeOperation(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant2.getParticipantId(), OPERATION_CREATE_SPACES,
                    new Object[] { sessionToken, Collections.singletonList(new SpaceCreation()) });
            fail();
        } catch (Exception e)
        {
            AssertionUtil.assertContains("Code cannot be empty", e.getMessage());
        }

        assertTransactions(coordinator.getTransactionMap(), new Transaction(trId, TransactionStatus.BEGIN_FINISHED));
        assertTransactions(participant1.getTransactionMap(), new Transaction(trId, TransactionStatus.BEGIN_FINISHED));
        assertTransactions(participant2.getTransactionMap(), new Transaction(trId, TransactionStatus.BEGIN_FINISHED));
    }

    @Test
    public void testPrepareTransactionFails()
    {
        // "prepare" should fail
        participant2.getDatabaseTransactionProvider().setPrepareException(new RuntimeException("Test prepare exception"));

        assertTransactions(coordinator.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        UUID trId = UUID.randomUUID();

        coordinator.beginTransaction(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        SpaceCreation spaceCreation1 = new SpaceCreation();
        spaceCreation1.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(), OPERATION_CREATE_SPACES,
                new Object[] { sessionToken, Collections.singletonList(spaceCreation1) });

        SpaceCreation spaceCreation2 = new SpaceCreation();
        spaceCreation2.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant2.getParticipantId(), OPERATION_CREATE_SPACES,
                new Object[] { sessionToken, Collections.singletonList(spaceCreation2) });

        try
        {
            coordinator.commitTransaction(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);
            fail();
        } catch (Exception e)
        {
            assertEquals(e, participant2.getDatabaseTransactionProvider().getPrepareException());
        }

        assertTransactions(coordinator.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());
    }

    @Test
    public void testTransactionWithCommit()
    {
        testTransaction(false);
    }

    @Test
    public void testTransactionWithRollback()
    {
        testTransaction(true);
    }

    private void testTransaction(boolean rollback)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        UUID trId = UUID.randomUUID();

        coordinator.beginTransaction(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        // participants create new spaces
        SearchResult<Space> participant1SpacesBeforeCreation =
                coordinator.executeOperation(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                        OPERATION_SEARCH_SPACES, new Object[] { sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions() });
        SearchResult<Space> participant2SpacesBeforeCreation =
                coordinator.executeOperation(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant2.getParticipantId(),
                        OPERATION_SEARCH_SPACES, new Object[] { sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions() });
        SearchResult<Space> noTrSpacesBeforeCreation = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions());

        SpaceCreation spaceCreation1 = new SpaceCreation();
        spaceCreation1.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                OPERATION_CREATE_SPACES, new Object[] { sessionToken, Collections.singletonList(spaceCreation1) });

        SpaceCreation spaceCreation2 = new SpaceCreation();
        spaceCreation2.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant2.getParticipantId(),
                OPERATION_CREATE_SPACES, new Object[] { sessionToken, Collections.singletonList(spaceCreation2) });

        SearchResult<Space> participant1SpacesAfterCreation =
                coordinator.executeOperation(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                        OPERATION_SEARCH_SPACES, new Object[] { sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions() });
        SearchResult<Space> participant2SpacesAfterCreation =
                coordinator.executeOperation(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant2.getParticipantId(),
                        OPERATION_SEARCH_SPACES, new Object[] { sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions() });
        SearchResult<Space> noTrSpacesAfterCreation = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions());

        // participants see their own new spaces, outside the transaction they are not visible
        assertEquals(Collections.singleton(spaceCreation1.getCode().toUpperCase()),
                difference(codes(participant1SpacesAfterCreation.getObjects()), codes(participant1SpacesBeforeCreation.getObjects())));
        assertEquals(Collections.singleton(spaceCreation2.getCode().toUpperCase()),
                difference(codes(participant2SpacesAfterCreation.getObjects()), codes(participant2SpacesBeforeCreation.getObjects())));
        assertEquals(Collections.emptySet(), difference(codes(noTrSpacesAfterCreation.getObjects()), codes(noTrSpacesBeforeCreation.getObjects())));

        // participants create projects in their new spaces
        SearchResult<Project> participant1ProjectsBeforeCreation =
                coordinator.executeOperation(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                        OPERATION_SEARCH_PROJECTS, new Object[] { sessionToken, new ProjectSearchCriteria(), new ProjectFetchOptions() });
        SearchResult<Project> participant2ProjectsBeforeCreation =
                coordinator.executeOperation(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant2.getParticipantId(),
                        OPERATION_SEARCH_PROJECTS, new Object[] { sessionToken, new ProjectSearchCriteria(), new ProjectFetchOptions() });
        SearchResult<Project> noTrProjectsBeforeCreation =
                v3api.searchProjects(sessionToken, new ProjectSearchCriteria(), new ProjectFetchOptions());

        ProjectCreation projectCreation1 = new ProjectCreation();
        projectCreation1.setSpaceId(new SpacePermId(spaceCreation1.getCode()));
        projectCreation1.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                OPERATION_CREATE_PROJECTS, new Object[] { sessionToken, Collections.singletonList(projectCreation1) });

        ProjectCreation projectCreation2 = new ProjectCreation();
        projectCreation2.setSpaceId(new SpacePermId(spaceCreation2.getCode()));
        projectCreation2.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant2.getParticipantId(),
                OPERATION_CREATE_PROJECTS, new Object[] { sessionToken, Collections.singletonList(projectCreation2) });

        SearchResult<Project> participant1ProjectsAfterCreation =
                coordinator.executeOperation(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                        OPERATION_SEARCH_PROJECTS, new Object[] { sessionToken, new ProjectSearchCriteria(), new ProjectFetchOptions() });
        SearchResult<Project> participant2ProjectsAfterCreation =
                coordinator.executeOperation(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant2.getParticipantId(),
                        OPERATION_SEARCH_PROJECTS, new Object[] { sessionToken, new ProjectSearchCriteria(), new ProjectFetchOptions() });
        SearchResult<Project> noTrProjectsAfterCreation =
                v3api.searchProjects(sessionToken, new ProjectSearchCriteria(), new ProjectFetchOptions());

        // participants see their own new projects, outside the transaction they are not visible
        assertEquals(Collections.singleton("/" + spaceCreation1.getCode().toUpperCase() + "/" + projectCreation1.getCode().toUpperCase()),
                difference(identifiers(participant1ProjectsAfterCreation.getObjects()),
                        identifiers(participant1ProjectsBeforeCreation.getObjects())));
        assertEquals(Collections.singleton("/" + spaceCreation2.getCode().toUpperCase() + "/" + projectCreation2.getCode().toUpperCase()),
                difference(identifiers(participant2ProjectsAfterCreation.getObjects()),
                        identifiers(participant2ProjectsBeforeCreation.getObjects())));
        assertEquals(Collections.emptySet(),
                difference(identifiers(noTrProjectsAfterCreation.getObjects()), identifiers(noTrProjectsBeforeCreation.getObjects())));

        if (rollback)
        {
            coordinator.rollbackTransaction(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);
        } else
        {
            coordinator.commitTransaction(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);
        }

        SearchResult<Space> noTrSpacesAfterCommit = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions());
        SearchResult<Project> noTrProjectsAfterCommit =
                v3api.searchProjects(sessionToken, new ProjectSearchCriteria(), new ProjectFetchOptions());

        if (rollback)
        {
            // both the spaces and projects are rolled back and are not visible outside the transaction
            assertEquals(codes(noTrSpacesAfterCommit.getObjects()), codes(noTrSpacesBeforeCreation.getObjects()));
            assertEquals(identifiers(noTrProjectsAfterCommit.getObjects()), identifiers(noTrProjectsBeforeCreation.getObjects()));
        } else
        {
            // both the spaces and projects are committed and are visible outside the transaction
            assertEquals(Set.of(spaceCreation1.getCode().toUpperCase(), spaceCreation2.getCode().toUpperCase()),
                    difference(codes(noTrSpacesAfterCommit.getObjects()), codes(noTrSpacesBeforeCreation.getObjects())));
            assertEquals(Set.of("/" + spaceCreation1.getCode().toUpperCase() + "/" + projectCreation1.getCode().toUpperCase(),
                            "/" + spaceCreation2.getCode().toUpperCase() + "/" + projectCreation2.getCode().toUpperCase()),
                    difference(identifiers(noTrProjectsAfterCommit.getObjects()), identifiers(noTrProjectsBeforeCreation.getObjects())));
        }
    }

    @Test
    public void testMultipleTransactions()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        UUID tr1Id = UUID.randomUUID();
        UUID tr2Id = UUID.randomUUID();

        // begin tr1 and tr2
        coordinator.beginTransaction(tr1Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY);
        coordinator.beginTransaction(tr2Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        SearchResult<Space> tr1SpacesBeforeCreations =
                coordinator.executeOperation(tr1Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                        OPERATION_SEARCH_SPACES, new Object[] { sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions() });
        SearchResult<Space> tr2SpacesBeforeCreations =
                coordinator.executeOperation(tr2Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                        OPERATION_SEARCH_SPACES, new Object[] { sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions() });
        SearchResult<Space> noTrSpacesBeforeCreations = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions());

        // create space1 in tr1
        SpaceCreation tr1Creation = new SpaceCreation();
        tr1Creation.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(tr1Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                OPERATION_CREATE_SPACES, new Object[] { sessionToken, Collections.singletonList(tr1Creation) });

        // create space2 in tr2
        SpaceCreation tr2Creation = new SpaceCreation();
        tr2Creation.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(tr2Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                OPERATION_CREATE_SPACES, new Object[] { sessionToken, Collections.singletonList(tr2Creation) });

        // create space3 in noTr
        SpaceCreation noTrCreation = new SpaceCreation();
        noTrCreation.setCode(UUID.randomUUID().toString());
        v3api.createSpaces(sessionToken, Collections.singletonList(noTrCreation));

        SearchResult<Space> tr1SpacesAfterCreations =
                coordinator.executeOperation(tr1Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                        OPERATION_SEARCH_SPACES, new Object[] { sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions() });
        SearchResult<Space> tr2SpacesAfterCreations =
                coordinator.executeOperation(tr2Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                        OPERATION_SEARCH_SPACES, new Object[] { sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions() });
        SearchResult<Space> noTrSpacesAfterCreations = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions());

        // check that tr1 sees only space1, tr2 sees only space2, noTr sees space3
        assertEquals(Collections.singleton(tr1Creation.getCode().toUpperCase()),
                difference(codes(tr1SpacesAfterCreations.getObjects()), codes(tr1SpacesBeforeCreations.getObjects())));
        assertEquals(Collections.singleton(tr2Creation.getCode().toUpperCase()),
                difference(codes(tr2SpacesAfterCreations.getObjects()), codes(tr2SpacesBeforeCreations.getObjects())));
        assertEquals(Collections.singleton(noTrCreation.getCode().toUpperCase()),
                difference(codes(noTrSpacesAfterCreations.getObjects()), codes(noTrSpacesBeforeCreations.getObjects())));

        coordinator.commitTransaction(tr1Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        try
        {
            coordinator.executeOperation(tr1Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                    OPERATION_SEARCH_SPACES, new Object[] { sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions() });
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Transaction '" + tr1Id + "' does not exist.");
        }

        // after tr1 commit, tr2 sees space1 and space2, noTr sees space1 and space3
        SearchResult<Space> tr2SpacesAfterTr1Commit =
                coordinator.executeOperation(tr2Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                        OPERATION_SEARCH_SPACES, new Object[] { sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions() });
        SearchResult<Space> noTrSpacesAfterTr1Commit = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions());

        assertEquals(Collections.singleton(tr1Creation.getCode().toUpperCase()),
                difference(codes(tr2SpacesAfterTr1Commit.getObjects()), codes(tr2SpacesAfterCreations.getObjects())));
        assertEquals(Collections.singleton(tr1Creation.getCode().toUpperCase()),
                difference(codes(noTrSpacesAfterTr1Commit.getObjects()), codes(noTrSpacesAfterCreations.getObjects())));

        coordinator.rollbackTransaction(tr2Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        try
        {
            coordinator.executeOperation(tr2Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                    OPERATION_SEARCH_SPACES, new Object[] { sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions() });
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Transaction '" + tr2Id + "' does not exist.");
        }

        // after tr1 commit and tr2 rollback, noTr sees space1 and space3
        noTrSpacesAfterTr1Commit = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions());
        assertEquals(Collections.singleton(tr1Creation.getCode().toUpperCase()),
                difference(codes(noTrSpacesAfterTr1Commit.getObjects()), codes(noTrSpacesAfterCreations.getObjects())));
    }

    private static void assertTransactions(Map<UUID, ? extends Transaction> actualTransactions, Transaction... expectedTransactions)
    {
        Map<UUID, Transaction> expectedTransactionsMap = new HashMap<>();

        for (Transaction expectedTransaction : expectedTransactions)
        {
            expectedTransactionsMap.put(expectedTransaction.getTransactionId(), expectedTransaction);
        }

        assertEquals(actualTransactions.keySet(), expectedTransactionsMap.keySet());

        for (Transaction actualTransaction : actualTransactions.values())
        {
            Transaction expectedTransaction = expectedTransactionsMap.get(actualTransaction.getTransactionId());
            assertEquals(actualTransaction.getTransactionStatus(), expectedTransaction.getTransactionStatus());
        }
    }

    private static Set<String> codes(Collection<? extends ICodeHolder> objectsWithCodes)
    {
        return objectsWithCodes.stream().map(ICodeHolder::getCode).collect(Collectors.toSet());
    }

    private static Set<String> identifiers(Collection<? extends IIdentifierHolder> objectsWithIdentifiers)
    {
        return objectsWithIdentifiers.stream().map(o -> o.getIdentifier().getIdentifier()).collect(Collectors.toSet());
    }

    private static <T> Set<T> difference(Set<T> s1, Set<T> s2)
    {
        Set<T> temp = new HashSet<>(s1);
        temp.removeAll(s2);
        return temp;
    }

    private static class TestTransactionParticipant implements ITransactionParticipant
    {

        // Map the original transaction id coming from the coordinator to a unique transaction id for each participant,
        // this way we can have multiple participants preparing the transaction on the same test database.

        private final Map<UUID, UUID> originalToInternalId = new HashMap<>();

        private final Map<UUID, UUID> internalToOriginalId = new HashMap<>();

        private final TransactionParticipant participant;

        private final TestDatabaseTransactionProvider databaseTransactionProvider;

        public TestTransactionParticipant(TransactionParticipantApi participantApi)
        {
            // replace the original database transaction provider with a test counterpart that allows to throw test exceptions

            this.participant = participantApi.getTransactionParticipant();
            this.databaseTransactionProvider = new TestDatabaseTransactionProvider(participantApi.getTransactionParticipant().getDatabaseTransactionProvider());
            this.participant.setDatabaseTransactionProvider(databaseTransactionProvider);
        }

        public TestDatabaseTransactionProvider getDatabaseTransactionProvider(){
            return this.databaseTransactionProvider;
        }

        private UUID mapTransactionId(UUID originalTransactionId)
        {
            synchronized (originalToInternalId)
            {
                if (originalToInternalId.containsKey(originalTransactionId))
                {
                    return originalToInternalId.get(originalTransactionId);
                } else
                {
                    UUID internalTransactionId = UUID.randomUUID();
                    originalToInternalId.put(originalTransactionId, internalTransactionId);
                    internalToOriginalId.put(internalTransactionId, originalTransactionId);
                    return internalTransactionId;
                }
            }
        }

        public Map<UUID, ? extends Transaction> getTransactionMap()
        {
            Map<UUID, Transaction> transactionMap = new HashMap<>();

            for (Map.Entry<UUID, ? extends Transaction> entry : this.participant.getTransactionMap().entrySet())
            {
                Transaction internalTransaction = entry.getValue();
                Transaction originalTransaction =
                        new Transaction(internalToOriginalId.get(internalTransaction.getTransactionId()), internalTransaction.getTransactionStatus());
                transactionMap.put(originalTransaction.getTransactionId(), originalTransaction);
            }

            return transactionMap;
        }

        @Override public String getParticipantId()
        {
            return participant.getParticipantId();
        }

        @Override public void beginTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            this.participant.beginTransaction(mapTransactionId(transactionId), sessionToken, interactiveSessionKey, transactionCoordinatorKey);
        }

        @Override public <T> T executeOperation(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String operationName, final Object[] operationArguments)
        {
            return participant.executeOperation(mapTransactionId(transactionId), sessionToken, interactiveSessionKey, operationName,
                    operationArguments);
        }

        @Override public void prepareTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            participant.prepareTransaction(mapTransactionId(transactionId), sessionToken, interactiveSessionKey, transactionCoordinatorKey);
        }

        @Override public void commitTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
        {
            participant.commitTransaction(mapTransactionId(transactionId), sessionToken, interactiveSessionKey);
        }

        @Override public void commitTransaction(final UUID transactionId, final String transactionCoordinatorKey)
        {
            participant.commitTransaction(mapTransactionId(transactionId), transactionCoordinatorKey);
        }

        @Override public void rollbackTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
        {
            participant.rollbackTransaction(mapTransactionId(transactionId), sessionToken, interactiveSessionKey);
        }

        @Override public void rollbackTransaction(final UUID transactionId, final String transactionCoordinatorKey)
        {
            participant.rollbackTransaction(mapTransactionId(transactionId), transactionCoordinatorKey);
        }

        @Override public List<UUID> recoverTransactions(final String transactionCoordinatorKey)
        {
            return participant.recoverTransactions(transactionCoordinatorKey);
        }

    }

    private static class TestDatabaseTransactionProvider implements IDatabaseTransactionProvider {

        private IDatabaseTransactionProvider databaseTransactionProvider;

        private RuntimeException beginException;

        private RuntimeException prepareException;

        public TestDatabaseTransactionProvider(IDatabaseTransactionProvider databaseTransactionProvider){
            this.databaseTransactionProvider = databaseTransactionProvider;
        }

        @Override public Object beginTransaction(final UUID transactionId) throws Exception
        {
            if(beginException != null){
                throw beginException;
            }
            return databaseTransactionProvider.beginTransaction(transactionId);
        }

        @Override public void prepareTransaction(final UUID transactionId, final Object transaction) throws Exception
        {
            if(prepareException != null){
                throw prepareException;
            }
            databaseTransactionProvider.prepareTransaction(transactionId, transaction);
        }

        @Override public void rollbackTransaction(final UUID transactionId, final Object transaction) throws Exception
        {
            databaseTransactionProvider.rollbackTransaction(transactionId, transaction);
        }

        @Override public void commitTransaction(final UUID transactionId, final Object transaction) throws Exception
        {
            databaseTransactionProvider.commitTransaction(transactionId, transaction);
        }

        public void setBeginException(final RuntimeException beginException)
        {
            this.beginException = beginException;
        }

        public RuntimeException getBeginException()
        {
            return beginException;
        }

        public void setPrepareException(final RuntimeException prepareException)
        {
            this.prepareException = prepareException;
        }

        public RuntimeException getPrepareException()
        {
            return prepareException;
        }
    }

}
