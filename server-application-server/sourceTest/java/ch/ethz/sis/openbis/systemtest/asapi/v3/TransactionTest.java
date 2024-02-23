package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.testng.annotations.AfterMethod;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.TransactionConfiguration;
import ch.ethz.sis.openbis.generic.server.asapi.v3.TransactionParticipantApi;
import ch.ethz.sis.transaction.IDatabaseTransactionProvider;
import ch.ethz.sis.transaction.ITransactionLog;
import ch.ethz.sis.transaction.ITransactionParticipant;
import ch.ethz.sis.transaction.Transaction;
import ch.ethz.sis.transaction.TransactionCoordinator;
import ch.ethz.sis.transaction.TransactionLog;
import ch.ethz.sis.transaction.TransactionLogEntry;
import ch.ethz.sis.transaction.TransactionParticipant;
import ch.ethz.sis.transaction.TransactionStatus;
import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;

public class TransactionTest extends AbstractTest
{

    private static final String TEST_COORDINATOR_KEY = "test-transaction-coordinator-key";

    private static final String TEST_INTERACTIVE_SESSION_KEY = "test-interactive-session-key";

    private static final String TEST_PARTICIPANT_1_ID = "test-participant-1";

    private static final String TEST_PARTICIPANT_2_ID = "test-participant-2";

    private static final String TRANSACTION_LOG_ROOT_FOLDER = "targets/transaction-logs";

    private static final String TRANSACTION_LOG_COORDINATOR_FOLDER = "test-coordinator";

    private static final String TRANSACTION_LOG_PARTICIPANT_1_FOLDER = "test-participant-1";

    private static final String TRANSACTION_LOG_PARTICIPANT_2_FOLDER = "test-participant-2";

    private static final String OPERATION_CREATE_SPACES = "createSpaces";

    private static final String OPERATION_CREATE_PROJECTS = "createProjects";

    private static final String OPERATION_SEARCH_SPACES = "searchSpaces";

    private static final String OPERATION_SEARCH_PROJECTS = "searchProjects";

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

    private UUID coordinatorTrId;

    @BeforeMethod
    private void beforeMethod()
    {
        FileUtilities.deleteRecursively(new File(TRANSACTION_LOG_ROOT_FOLDER, TRANSACTION_LOG_COORDINATOR_FOLDER));
        FileUtilities.deleteRecursively(new File(TRANSACTION_LOG_ROOT_FOLDER, TRANSACTION_LOG_PARTICIPANT_1_FOLDER));
        FileUtilities.deleteRecursively(new File(TRANSACTION_LOG_ROOT_FOLDER, TRANSACTION_LOG_PARTICIPANT_2_FOLDER));

        coordinatorTrId = UUID.randomUUID();
        UUID participant1TrId = UUID.randomUUID();
        UUID participant2TrId = UUID.randomUUID();

        participant1 = createParticipant(TEST_PARTICIPANT_1_ID, TRANSACTION_LOG_PARTICIPANT_1_FOLDER);
        participant1.setTransactionMapping(Map.of(coordinatorTrId, participant1TrId));

        participant2 = createParticipant(TEST_PARTICIPANT_2_ID, TRANSACTION_LOG_PARTICIPANT_2_FOLDER);
        participant2.setTransactionMapping(Map.of(coordinatorTrId, participant2TrId));

        coordinator = createCoordinator(Arrays.asList(participant1, participant2), 60, 10);
    }

    private TransactionCoordinator createCoordinator(List<ITransactionParticipant> participants, int transactionTimeoutInSeconds,
            int transactionCountLimit)
    {
        return new TransactionCoordinator(TEST_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionToken -> v3api.isSessionActive(sessionToken),
                participants, new TransactionLog(new File(TRANSACTION_LOG_ROOT_FOLDER), TRANSACTION_LOG_COORDINATOR_FOLDER),
                transactionTimeoutInSeconds, transactionCountLimit);
    }

    private TestTransactionParticipant createParticipant(String participantId, String logFolderName)
    {
        return new TestTransactionParticipant(
                new TransactionParticipantApi(transactionConfiguration, transactionManager, daoFactory, databaseContext, applicationServerApi,
                        participantId, logFolderName));
    }

    @AfterMethod
    private void afterMethod() throws Exception
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

        participant1.close();
        participant2.close();
    }

    @Test
    public void testBeginTransactionFails()
    {
        // "begin" should fail
        RuntimeException exception = new RuntimeException("Test begin exception");
        participant2.getDatabaseTransactionProvider().setBeginAction(() ->
        {
            throw exception;
        });

        assertTransactions(coordinator.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        try
        {
            coordinator.beginTransaction(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(),
                    "Begin transaction '" + coordinatorTrId + "' failed for participant '" + participant2.getParticipantId() + "'.");
            assertEquals(e.getCause(), exception);
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

        coordinator.beginTransaction(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        SpaceCreation spaceCreation1 = new SpaceCreation();
        spaceCreation1.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                OPERATION_CREATE_SPACES,
                new Object[] { sessionToken, Collections.singletonList(spaceCreation1) });

        try
        {
            coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant2.getParticipantId(),
                    OPERATION_CREATE_SPACES,
                    new Object[] { sessionToken, Collections.singletonList(new SpaceCreation()) });
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Transaction '" + coordinatorTrId + "' execute operation 'createSpaces' failed for participant '"
                    + participant2.getParticipantId() + "'.");
            AssertionUtil.assertContains("Code cannot be empty", e.getCause().getMessage());
        }

        assertTransactions(coordinator.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.BEGIN_FINISHED));
        assertTransactions(participant1.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.BEGIN_FINISHED));
        assertTransactions(participant2.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.BEGIN_FINISHED));

        Map<ISpaceId, Space> createdSpaces = applicationServerApi.getSpaces(sessionToken,
                Collections.singletonList(new SpacePermId(spaceCreation1.getCode())), new SpaceFetchOptions());
        assertEquals(createdSpaces.size(), 0);
    }

    @Test
    public void testExecuteOperationTimesOut() throws Exception
    {
        coordinator = createCoordinator(Arrays.asList(participant1, participant2), 1, 10);

        assertTransactions(coordinator.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        coordinator.beginTransaction(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                OPERATION_CREATE_SPACES,
                new Object[] { sessionToken, Collections.singletonList(spaceCreation) });

        assertTransactions(coordinator.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.BEGIN_FINISHED));
        assertTransactions(participant1.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.BEGIN_FINISHED));
        assertTransactions(participant2.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.BEGIN_FINISHED));

        Thread.sleep(500);

        coordinator.finishFailedOrAbandonedTransactions();

        assertTransactions(coordinator.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.BEGIN_FINISHED));
        assertTransactions(participant1.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.BEGIN_FINISHED));
        assertTransactions(participant2.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.BEGIN_FINISHED));

        Thread.sleep(500);

        coordinator.finishFailedOrAbandonedTransactions();

        assertTransactions(coordinator.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        Map<ISpaceId, Space> createdSpaces = applicationServerApi.getSpaces(sessionToken,
                Collections.singletonList(new SpacePermId(spaceCreation.getCode())), new SpaceFetchOptions());
        assertEquals(createdSpaces.size(), 0);
    }

    @Test
    public void testPrepareTransactionFails()
    {
        // "prepare" should fail
        RuntimeException exception = new RuntimeException("Test prepare exception");
        participant2.getDatabaseTransactionProvider().setPrepareAction(() ->
        {
            throw exception;
        });

        assertTransactions(coordinator.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        coordinator.beginTransaction(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        SpaceCreation spaceCreation1 = new SpaceCreation();
        spaceCreation1.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                OPERATION_CREATE_SPACES,
                new Object[] { sessionToken, Collections.singletonList(spaceCreation1) });

        SpaceCreation spaceCreation2 = new SpaceCreation();
        spaceCreation2.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant2.getParticipantId(),
                OPERATION_CREATE_SPACES,
                new Object[] { sessionToken, Collections.singletonList(spaceCreation2) });

        try
        {
            coordinator.commitTransaction(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(),
                    "Prepare transaction '" + coordinatorTrId + "' failed for participant '" + participant2.getParticipantId() + "'.");
            assertEquals(e.getCause(), exception);
        }

        assertTransactions(coordinator.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        Map<ISpaceId, Space> createdSpaces = applicationServerApi.getSpaces(sessionToken,
                Arrays.asList(new SpacePermId(spaceCreation1.getCode()), new SpacePermId(spaceCreation2.getCode())), new SpaceFetchOptions());
        assertEquals(createdSpaces.size(), 0);
    }

    @Test
    public void testCommitTransactionFailsAndRecovers()
    {
        // "commit" should fail
        RuntimeException exception = new RuntimeException("Test commit exception");
        participant2.getDatabaseTransactionProvider().setCommitAction(() ->
        {
            throw exception;
        });

        assertTransactions(coordinator.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        coordinator.beginTransaction(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        SpaceCreation spaceCreation1 = new SpaceCreation();
        spaceCreation1.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                OPERATION_CREATE_SPACES,
                new Object[] { sessionToken, Collections.singletonList(spaceCreation1) });

        SpaceCreation spaceCreation2 = new SpaceCreation();
        spaceCreation2.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant2.getParticipantId(),
                OPERATION_CREATE_SPACES,
                new Object[] { sessionToken, Collections.singletonList(spaceCreation2) });

        coordinator.commitTransaction(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        assertTransactions(coordinator.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.COMMIT_STARTED));
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.COMMIT_STARTED));

        // "commit" should succeed
        participant2.getDatabaseTransactionProvider().setCommitAction(null);

        coordinator.finishFailedOrAbandonedTransactions();

        assertTransactions(coordinator.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        Map<ISpaceId, Space> createdSpaces = applicationServerApi.getSpaces(sessionToken,
                Arrays.asList(new SpacePermId(spaceCreation1.getCode()), new SpacePermId(spaceCreation2.getCode())), new SpaceFetchOptions());
        assertEquals(createdSpaces.size(), 2);
    }

    @Test
    public void testRollbackTransactionFailsAndRecovers()
    {
        // "rollback" should fail
        RuntimeException exception = new RuntimeException("Test rollback exception");
        participant2.getDatabaseTransactionProvider().setRollbackAction(() ->
        {
            throw exception;
        });

        assertTransactions(coordinator.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        coordinator.beginTransaction(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        SpaceCreation spaceCreation1 = new SpaceCreation();
        spaceCreation1.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                OPERATION_CREATE_SPACES,
                new Object[] { sessionToken, Collections.singletonList(spaceCreation1) });

        SpaceCreation spaceCreation2 = new SpaceCreation();
        spaceCreation2.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant2.getParticipantId(),
                OPERATION_CREATE_SPACES,
                new Object[] { sessionToken, Collections.singletonList(spaceCreation2) });

        coordinator.rollbackTransaction(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        assertTransactions(coordinator.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.ROLLBACK_STARTED));
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.ROLLBACK_STARTED));

        // "rollback" should succeed
        participant2.getDatabaseTransactionProvider().setRollbackAction(null);

        coordinator.finishFailedOrAbandonedTransactions();

        assertTransactions(coordinator.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        Map<ISpaceId, Space> createdSpaces = applicationServerApi.getSpaces(sessionToken,
                Arrays.asList(new SpacePermId(spaceCreation1.getCode()), new SpacePermId(spaceCreation2.getCode())), new SpaceFetchOptions());
        assertEquals(createdSpaces.size(), 0);
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

        coordinator.beginTransaction(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        // participants create new spaces
        SearchResult<Space> participant1SpacesBeforeCreation =
                coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                        OPERATION_SEARCH_SPACES, new Object[] { sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions() });
        SearchResult<Space> participant2SpacesBeforeCreation =
                coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant2.getParticipantId(),
                        OPERATION_SEARCH_SPACES, new Object[] { sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions() });
        SearchResult<Space> noTrSpacesBeforeCreation = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions());

        SpaceCreation spaceCreation1 = new SpaceCreation();
        spaceCreation1.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                OPERATION_CREATE_SPACES, new Object[] { sessionToken, Collections.singletonList(spaceCreation1) });

        SpaceCreation spaceCreation2 = new SpaceCreation();
        spaceCreation2.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant2.getParticipantId(),
                OPERATION_CREATE_SPACES, new Object[] { sessionToken, Collections.singletonList(spaceCreation2) });

        SearchResult<Space> participant1SpacesAfterCreation =
                coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                        OPERATION_SEARCH_SPACES, new Object[] { sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions() });
        SearchResult<Space> participant2SpacesAfterCreation =
                coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant2.getParticipantId(),
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
                coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                        OPERATION_SEARCH_PROJECTS, new Object[] { sessionToken, new ProjectSearchCriteria(), new ProjectFetchOptions() });
        SearchResult<Project> participant2ProjectsBeforeCreation =
                coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant2.getParticipantId(),
                        OPERATION_SEARCH_PROJECTS, new Object[] { sessionToken, new ProjectSearchCriteria(), new ProjectFetchOptions() });
        SearchResult<Project> noTrProjectsBeforeCreation =
                v3api.searchProjects(sessionToken, new ProjectSearchCriteria(), new ProjectFetchOptions());

        ProjectCreation projectCreation1 = new ProjectCreation();
        projectCreation1.setSpaceId(new SpacePermId(spaceCreation1.getCode()));
        projectCreation1.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                OPERATION_CREATE_PROJECTS, new Object[] { sessionToken, Collections.singletonList(projectCreation1) });

        ProjectCreation projectCreation2 = new ProjectCreation();
        projectCreation2.setSpaceId(new SpacePermId(spaceCreation2.getCode()));
        projectCreation2.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant2.getParticipantId(),
                OPERATION_CREATE_PROJECTS, new Object[] { sessionToken, Collections.singletonList(projectCreation2) });

        SearchResult<Project> participant1ProjectsAfterCreation =
                coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                        OPERATION_SEARCH_PROJECTS, new Object[] { sessionToken, new ProjectSearchCriteria(), new ProjectFetchOptions() });
        SearchResult<Project> participant2ProjectsAfterCreation =
                coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant2.getParticipantId(),
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
            coordinator.rollbackTransaction(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);
        } else
        {
            coordinator.commitTransaction(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);
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

        UUID coordinatorTr1Id = UUID.randomUUID();
        UUID coordinatorTr2Id = UUID.randomUUID();
        UUID participant1Tr1Id = UUID.randomUUID();
        UUID participant1Tr2Id = UUID.randomUUID();
        UUID participant2Tr1Id = UUID.randomUUID();
        UUID participant2Tr2Id = UUID.randomUUID();

        participant1.setTransactionMapping(Map.of(coordinatorTr1Id, participant1Tr1Id, coordinatorTr2Id, participant1Tr2Id));
        participant2.setTransactionMapping(Map.of(coordinatorTr1Id, participant2Tr1Id, coordinatorTr2Id, participant2Tr2Id));

        // begin tr1 and tr2
        coordinator.beginTransaction(coordinatorTr1Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY);
        coordinator.beginTransaction(coordinatorTr2Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        SearchResult<Space> tr1SpacesBeforeCreations =
                coordinator.executeOperation(coordinatorTr1Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                        OPERATION_SEARCH_SPACES, new Object[] { sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions() });
        SearchResult<Space> tr2SpacesBeforeCreations =
                coordinator.executeOperation(coordinatorTr2Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                        OPERATION_SEARCH_SPACES, new Object[] { sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions() });
        SearchResult<Space> noTrSpacesBeforeCreations = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions());

        // create space1 in tr1
        SpaceCreation tr1Creation = new SpaceCreation();
        tr1Creation.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(coordinatorTr1Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                OPERATION_CREATE_SPACES, new Object[] { sessionToken, Collections.singletonList(tr1Creation) });

        // create space2 in tr2
        SpaceCreation tr2Creation = new SpaceCreation();
        tr2Creation.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(coordinatorTr2Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                OPERATION_CREATE_SPACES, new Object[] { sessionToken, Collections.singletonList(tr2Creation) });

        // create space3 in noTr
        SpaceCreation noTrCreation = new SpaceCreation();
        noTrCreation.setCode(UUID.randomUUID().toString());
        v3api.createSpaces(sessionToken, Collections.singletonList(noTrCreation));

        SearchResult<Space> tr1SpacesAfterCreations =
                coordinator.executeOperation(coordinatorTr1Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                        OPERATION_SEARCH_SPACES, new Object[] { sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions() });
        SearchResult<Space> tr2SpacesAfterCreations =
                coordinator.executeOperation(coordinatorTr2Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                        OPERATION_SEARCH_SPACES, new Object[] { sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions() });
        SearchResult<Space> noTrSpacesAfterCreations = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions());

        // check that tr1 sees only space1, tr2 sees only space2, noTr sees space3
        assertEquals(Collections.singleton(tr1Creation.getCode().toUpperCase()),
                difference(codes(tr1SpacesAfterCreations.getObjects()), codes(tr1SpacesBeforeCreations.getObjects())));
        assertEquals(Collections.singleton(tr2Creation.getCode().toUpperCase()),
                difference(codes(tr2SpacesAfterCreations.getObjects()), codes(tr2SpacesBeforeCreations.getObjects())));
        assertEquals(Collections.singleton(noTrCreation.getCode().toUpperCase()),
                difference(codes(noTrSpacesAfterCreations.getObjects()), codes(noTrSpacesBeforeCreations.getObjects())));

        coordinator.commitTransaction(coordinatorTr1Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        try
        {
            coordinator.executeOperation(coordinatorTr1Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                    OPERATION_SEARCH_SPACES, new Object[] { sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions() });
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Transaction '" + coordinatorTr1Id + "' does not exist.");
        }

        // after tr1 commit, tr2 sees space1 and space2, noTr sees space1 and space3
        SearchResult<Space> tr2SpacesAfterTr1Commit =
                coordinator.executeOperation(coordinatorTr2Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                        OPERATION_SEARCH_SPACES, new Object[] { sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions() });
        SearchResult<Space> noTrSpacesAfterTr1Commit = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions());

        assertEquals(Collections.singleton(tr1Creation.getCode().toUpperCase()),
                difference(codes(tr2SpacesAfterTr1Commit.getObjects()), codes(tr2SpacesAfterCreations.getObjects())));
        assertEquals(Collections.singleton(tr1Creation.getCode().toUpperCase()),
                difference(codes(noTrSpacesAfterTr1Commit.getObjects()), codes(noTrSpacesAfterCreations.getObjects())));

        coordinator.rollbackTransaction(coordinatorTr2Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        try
        {
            coordinator.executeOperation(coordinatorTr2Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                    OPERATION_SEARCH_SPACES, new Object[] { sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions() });
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Transaction '" + coordinatorTr2Id + "' does not exist.");
        }

        // after tr1 commit and tr2 rollback, noTr sees space1 and space3
        noTrSpacesAfterTr1Commit = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions());
        assertEquals(Collections.singleton(tr1Creation.getCode().toUpperCase()),
                difference(codes(noTrSpacesAfterTr1Commit.getObjects()), codes(noTrSpacesAfterCreations.getObjects())));
    }

    @Test
    public void testMultipleConcurrentCallsToOneTransaction() throws InterruptedException
    {
        assertTransactions(coordinator.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        coordinator.beginTransaction(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        SpaceCreation spaceCreation1 = new SpaceCreation();
        spaceCreation1.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                OPERATION_CREATE_SPACES,
                new Object[] { sessionToken, Collections.singletonList(spaceCreation1) });

        MessageChannel messageChannel = new MessageChannel(1000);

        participant1.getDatabaseTransactionProvider().setCommitAction(() ->
        {
            messageChannel.send("committing");
            messageChannel.assertNextMessage("executed");
        });

        Thread committingThread = new Thread(() -> coordinator.commitTransaction(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY));
        committingThread.start();

        SpaceCreation spaceCreation2 = new SpaceCreation();
        spaceCreation2.setCode(UUID.randomUUID().toString());

        messageChannel.assertNextMessage("committing");

        try
        {
            // try to execute an operation while the commit is in progress
            coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant2.getParticipantId(),
                    OPERATION_CREATE_SPACES,
                    new Object[] { sessionToken, Collections.singletonList(spaceCreation2) });
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(),
                    "Cannot execute a new action on transaction '" + coordinatorTrId + "' as it is still busy executing a previous action.");
        }

        messageChannel.send("executed");

        committingThread.join();
    }

    @Test
    public void testTooManyTransactions()
    {
        UUID coordinatorTr1Id = UUID.randomUUID();
        UUID coordinatorTr2Id = UUID.randomUUID();
        UUID coordinatorTr3Id = UUID.randomUUID();

        UUID participant1Tr1Id = UUID.randomUUID();
        UUID participant1Tr2Id = UUID.randomUUID();
        UUID participant1Tr3Id = UUID.randomUUID();

        UUID participant2Tr1Id = UUID.randomUUID();
        UUID participant2Tr2Id = UUID.randomUUID();
        UUID participant2Tr3Id = UUID.randomUUID();

        participant1.setTransactionMapping(
                Map.of(coordinatorTr1Id, participant1Tr1Id, coordinatorTr2Id, participant1Tr2Id, coordinatorTr3Id, participant1Tr3Id));
        participant2.setTransactionMapping(
                Map.of(coordinatorTr1Id, participant2Tr1Id, coordinatorTr2Id, participant2Tr2Id, coordinatorTr3Id, participant2Tr3Id));

        coordinator = createCoordinator(Arrays.asList(participant1, participant2), 60, 2);

        assertTransactions(coordinator.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        coordinator.beginTransaction(coordinatorTr1Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY);
        coordinator.beginTransaction(coordinatorTr2Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        try
        {
            coordinator.beginTransaction(coordinatorTr3Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY);
        } catch (Exception e)
        {
            assertEquals(e.getMessage(),
                    "Cannot create transaction '" + coordinatorTr3Id + "' because transaction count limit (2) has been reached.");
        }
    }

    @Test
    public void testRecoveryOfCoordinatorWithTransactionToRollback()
    {
        TransactionCoordinator coordinatorBeforeCrash = createCoordinator(Arrays.asList(participant1, participant2), 60, 10);

        assertTransactions(coordinatorBeforeCrash.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        coordinatorBeforeCrash.beginTransaction(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode(UUID.randomUUID().toString());

        coordinatorBeforeCrash.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                OPERATION_CREATE_SPACES, new Object[] { sessionToken, Collections.singletonList(spaceCreation) });

        // new coordinator
        TransactionCoordinator coordinatorAfterCrash = createCoordinator(Arrays.asList(participant1, participant2), 60, 10);

        assertTransactions(coordinatorAfterCrash.getTransactionMap());
        assertTransactions(participant1.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.BEGIN_FINISHED));
        assertTransactions(participant2.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.BEGIN_FINISHED));

        coordinatorAfterCrash.recoverTransactionsFromTransactionLog();

        assertTransactions(coordinatorAfterCrash.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        Map<ISpaceId, Space> createdSpaces = applicationServerApi.getSpaces(sessionToken,
                Collections.singletonList(new SpacePermId(spaceCreation.getCode())), new SpaceFetchOptions());
        assertEquals(createdSpaces.size(), 0);
    }

    @Test
    public void testRecoveryOfCoordinatorWithTransactionToCommit()
    {
        // "commit" for both participants should fail
        RuntimeException exception = new RuntimeException("Test prepare exception");

        participant1.getDatabaseTransactionProvider().setCommitAction(() ->
        {
            throw exception;
        });
        participant2.getDatabaseTransactionProvider().setCommitAction(() ->
        {
            throw exception;
        });

        TransactionCoordinator coordinatorBeforeCrash = createCoordinator(Arrays.asList(participant1, participant2), 60, 10);

        assertTransactions(coordinatorBeforeCrash.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        coordinatorBeforeCrash.beginTransaction(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode(UUID.randomUUID().toString());

        coordinatorBeforeCrash.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                OPERATION_CREATE_SPACES, new Object[] { sessionToken, Collections.singletonList(spaceCreation) });

        coordinatorBeforeCrash.commitTransaction(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        assertTransactions(coordinatorBeforeCrash.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.COMMIT_STARTED));
        assertTransactions(participant1.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.COMMIT_STARTED));
        assertTransactions(participant2.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.COMMIT_STARTED));

        // new coordinator
        TransactionCoordinator coordinatorAfterCrash = createCoordinator(Arrays.asList(participant1, participant2), 60, 10);

        // "commit" for both participants should succeed
        participant1.getDatabaseTransactionProvider().setCommitAction(null);
        participant2.getDatabaseTransactionProvider().setCommitAction(null);

        assertTransactions(coordinatorAfterCrash.getTransactionMap());
        assertTransactions(participant1.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.COMMIT_STARTED));
        assertTransactions(participant2.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.COMMIT_STARTED));

        Map<ISpaceId, Space> createdSpaces = applicationServerApi.getSpaces(sessionToken,
                Collections.singletonList(new SpacePermId(spaceCreation.getCode())), new SpaceFetchOptions());
        assertEquals(createdSpaces.size(), 0);

        coordinatorAfterCrash.recoverTransactionsFromTransactionLog();

        assertTransactions(coordinatorAfterCrash.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        createdSpaces = applicationServerApi.getSpaces(sessionToken,
                Collections.singletonList(new SpacePermId(spaceCreation.getCode())), new SpaceFetchOptions());
        assertEquals(createdSpaces.size(), 1);
    }

    @Test
    public void testRecoveryOfParticipantWithTransactionToRollback()
    {
        UUID coordinatorTrId = UUID.randomUUID();
        UUID participant1TrId = UUID.randomUUID();
        UUID participant2TrId = UUID.randomUUID();

        participant1.setTransactionMapping(Map.of(coordinatorTrId, participant1TrId));
        participant2.setTransactionMapping(Map.of(coordinatorTrId, participant2TrId));

        List<ITransactionParticipant> participants = new ArrayList<>();
        participants.add(participant1);
        participants.add(participant2);

        TransactionCoordinator coordinator = createCoordinator(participants, 60, 10);

        assertTransactions(coordinator.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        coordinator.beginTransaction(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                OPERATION_CREATE_SPACES, new Object[] { sessionToken, Collections.singletonList(spaceCreation) });

        assertTransactions(coordinator.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.BEGIN_FINISHED));
        assertTransactions(participant1.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.BEGIN_FINISHED));
        assertTransactions(participant2.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.BEGIN_FINISHED));

        // replace original participant with a new instance
        TestTransactionParticipant participant1AfterCrash = createParticipant(TEST_PARTICIPANT_1_ID, TRANSACTION_LOG_PARTICIPANT_1_FOLDER);
        participant1AfterCrash.setTransactionMapping(Map.of(coordinatorTrId, participant1TrId));
        participants.set(0, participant1AfterCrash);

        assertTransactions(coordinator.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.BEGIN_FINISHED));
        assertTransactions(participant1AfterCrash.getTransactionMap());
        assertTransactions(participant2.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.BEGIN_FINISHED));

        Map<ISpaceId, Space> createdSpaces = applicationServerApi.getSpaces(sessionToken,
                Collections.singletonList(new SpacePermId(spaceCreation.getCode())), new SpaceFetchOptions());
        assertEquals(createdSpaces.size(), 0);

        participant1AfterCrash.recoverTransactionsFromTransactionLog();

        createdSpaces = applicationServerApi.getSpaces(sessionToken,
                Collections.singletonList(new SpacePermId(spaceCreation.getCode())), new SpaceFetchOptions());
        assertEquals(createdSpaces.size(), 0);

        assertTransactions(coordinator.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.BEGIN_FINISHED));
        assertTransactions(participant1AfterCrash.getTransactionMap());
        assertTransactions(participant2.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.BEGIN_FINISHED));

        try
        {
            coordinator.commitTransaction(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);
        } catch (Exception e)
        {
            // thrown by coordinator
            assertEquals(e.getMessage(),
                    "Prepare transaction '" + coordinatorTrId + "' failed for participant '" + participant1.getParticipantId() + "'.");
            // thrown by participant
            assertEquals(e.getCause().getMessage(), "Transaction '" + participant1TrId + "' does not exist.");
        }

        assertTransactions(coordinator.getTransactionMap());
        assertTransactions(participant1AfterCrash.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        createdSpaces = applicationServerApi.getSpaces(sessionToken,
                Collections.singletonList(new SpacePermId(spaceCreation.getCode())), new SpaceFetchOptions());
        assertEquals(createdSpaces.size(), 0);

        participant1AfterCrash.close();
    }

    @Test
    public void testRecoveryOfParticipantWithTransactionToCommit()
    {
        List<ITransactionParticipant> participants = new ArrayList<>();
        participants.add(participant1);
        participants.add(participant2);

        TransactionCoordinator coordinator = createCoordinator(participants, 60, 10);

        assertTransactions(coordinator.getTransactionMap());
        assertTransactions(participant1.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        coordinator.beginTransaction(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode(UUID.randomUUID().toString());

        coordinator.executeOperation(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, participant1.getParticipantId(),
                OPERATION_CREATE_SPACES, new Object[] { sessionToken, Collections.singletonList(spaceCreation) });

        // "prepare" should fail
        RuntimeException exception = new RuntimeException("Test commit exception");
        participant1.getDatabaseTransactionProvider().setCommitAction(() ->
        {
            throw exception;
        });

        coordinator.commitTransaction(coordinatorTrId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        assertTransactions(coordinator.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.COMMIT_STARTED));
        assertTransactions(participant1.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.COMMIT_STARTED));
        assertTransactions(participant2.getTransactionMap());

        TestTransactionParticipant participant1AfterCrash = createParticipant(TEST_PARTICIPANT_1_ID, TRANSACTION_LOG_PARTICIPANT_1_FOLDER);
        // replace original participant with a new instance
        participants.set(0, participant1AfterCrash);

        assertTransactions(coordinator.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.COMMIT_STARTED));
        assertTransactions(participant1AfterCrash.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        Map<ISpaceId, Space> createdSpaces = applicationServerApi.getSpaces(sessionToken,
                Collections.singletonList(new SpacePermId(spaceCreation.getCode())), new SpaceFetchOptions());
        assertEquals(createdSpaces.size(), 0);

        participant1AfterCrash.recoverTransactionsFromTransactionLog();

        createdSpaces = applicationServerApi.getSpaces(sessionToken,
                Collections.singletonList(new SpacePermId(spaceCreation.getCode())), new SpaceFetchOptions());
        assertEquals(createdSpaces.size(), 1);

        assertTransactions(coordinator.getTransactionMap(), new Transaction(coordinatorTrId, TransactionStatus.COMMIT_STARTED));
        assertTransactions(participant1AfterCrash.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        coordinator.finishFailedOrAbandonedTransactions();

        assertTransactions(coordinator.getTransactionMap());
        assertTransactions(participant1AfterCrash.getTransactionMap());
        assertTransactions(participant2.getTransactionMap());

        createdSpaces = applicationServerApi.getSpaces(sessionToken,
                Collections.singletonList(new SpacePermId(spaceCreation.getCode())), new SpaceFetchOptions());
        assertEquals(createdSpaces.size(), 1);

        participant1AfterCrash.close();
    }

    private static void assertTransactions(Map<UUID, ? extends Transaction> actualTransactions, Transaction... expectedTransactions)
    {
        Map<String, String> actualTransactionsMap = new TreeMap<>();
        Map<String, String> expectedTransactionsMap = new TreeMap<>();

        for (Transaction actualTransaction : actualTransactions.values())
        {
            actualTransactionsMap.put(actualTransaction.getTransactionId().toString(), actualTransaction.getTransactionStatus().toString());
        }

        for (Transaction expectedTransaction : expectedTransactions)
        {
            expectedTransactionsMap.put(expectedTransaction.getTransactionId().toString(), expectedTransaction.getTransactionStatus().toString());
        }

        assertEquals(actualTransactionsMap.toString(), expectedTransactionsMap.toString());
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

        private final TestTransactionLog transactionLog;

        public TestTransactionParticipant(TransactionParticipantApi participantApi)
        {
            // replace the original database transaction provider with a test counterpart that allows to throw test exceptions

            this.participant = participantApi.getTransactionParticipant();
            this.databaseTransactionProvider =
                    new TestDatabaseTransactionProvider(participantApi.getTransactionParticipant().getDatabaseTransactionProvider());
            this.transactionLog = new TestTransactionLog(participantApi.getTransactionParticipant().getTransactionLog());
            this.participant.setDatabaseTransactionProvider(databaseTransactionProvider);
            this.participant.setTransactionLog(transactionLog);
        }

        public TestDatabaseTransactionProvider getDatabaseTransactionProvider()
        {
            return this.databaseTransactionProvider;
        }

        public TestTransactionLog getTransactionLog()
        {
            return transactionLog;
        }

        @Override public String getParticipantId()
        {
            return participant.getParticipantId();
        }

        @Override public void beginTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            this.participant.beginTransaction(originalToInternalId.get(transactionId), sessionToken, interactiveSessionKey,
                    transactionCoordinatorKey);
        }

        @Override public <T> T executeOperation(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String operationName, final Object[] operationArguments)
        {
            return participant.executeOperation(originalToInternalId.get(transactionId), sessionToken, interactiveSessionKey, operationName,
                    operationArguments);
        }

        @Override public void prepareTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            participant.prepareTransaction(originalToInternalId.get(transactionId), sessionToken, interactiveSessionKey, transactionCoordinatorKey);
        }

        @Override public void commitTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
        {
            participant.commitTransaction(originalToInternalId.get(transactionId), sessionToken, interactiveSessionKey);
        }

        @Override public void commitRecoveredTransaction(final UUID transactionId, final String interactiveSessionKey, final String transactionCoordinatorKey)
        {
            participant.commitRecoveredTransaction(originalToInternalId.get(transactionId), interactiveSessionKey, transactionCoordinatorKey);
        }

        @Override public void rollbackTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
        {
            participant.rollbackTransaction(originalToInternalId.get(transactionId), sessionToken, interactiveSessionKey);
        }

        @Override public void rollbackRecoveredTransaction(final UUID transactionId, final String interactiveSessionKey, final String transactionCoordinatorKey)
        {
            participant.rollbackRecoveredTransaction(originalToInternalId.get(transactionId), interactiveSessionKey, transactionCoordinatorKey);
        }

        @Override public List<UUID> recoverTransactions(final String interactiveSessionKey, final String transactionCoordinatorKey)
        {
            List<UUID> transactionIds = new ArrayList<>();

            for (UUID internalTransactionId : participant.recoverTransactions(interactiveSessionKey, transactionCoordinatorKey))
            {
                transactionIds.add(internalToOriginalId.get(internalTransactionId));
            }

            return transactionIds;
        }

        public void recoverTransactionsFromTransactionLog()
        {
            participant.recoverTransactionsFromTransactionLog();
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

        public void setTransactionMapping(Map<UUID, UUID> coordinatorIdToParticipantIdMap)
        {
            for (Map.Entry<UUID, UUID> entry : coordinatorIdToParticipantIdMap.entrySet())
            {
                originalToInternalId.put(entry.getKey(), entry.getValue());
                internalToOriginalId.put(entry.getValue(), entry.getKey());
            }
        }

        public void close()
        {
            participant.close();
        }
    }

    private static class TestDatabaseTransactionProvider implements IDatabaseTransactionProvider
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

        @Override public void rollbackTransaction(final UUID transactionId, final Object transaction) throws Exception
        {
            if (rollbackAction != null)
            {
                rollbackAction.run();
            }
            databaseTransactionProvider.rollbackTransaction(transactionId, transaction);
        }

        @Override public void commitTransaction(final UUID transactionId, final Object transaction) throws Exception
        {
            if (commitAction != null)
            {
                commitAction.run();
            }
            databaseTransactionProvider.commitTransaction(transactionId, transaction);
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

    private static class TestTransactionLog implements ITransactionLog
    {

        private final ITransactionLog transactionLog;

        TestTransactionLog(ITransactionLog transactionLog)
        {
            this.transactionLog = transactionLog;
        }

        @Override public void logTransaction(final TransactionLogEntry transaction)
        {
            transactionLog.logTransaction(transaction);
        }

        @Override public Map<UUID, TransactionLogEntry> getTransactions()
        {
            return transactionLog.getTransactions();
        }
    }

}
