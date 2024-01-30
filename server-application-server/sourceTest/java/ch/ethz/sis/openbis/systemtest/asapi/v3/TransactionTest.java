package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.ITransactionCoordinatorApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IIdentifierHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;

public class TransactionTest extends AbstractTest
{

    private static final String TEST_INTERACTIVE_SESSION_KEY = "test-interactive-session-key";

    @Autowired
    private ITransactionCoordinatorApi coordinator;

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

        // transaction creates a space
        List<Space> trSpacesBeforeCreation =
                searchSpaces(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, new SpaceSearchCriteria(), new SpaceFetchOptions());
        List<Space> noTrSpacesBeforeCreation = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions()).getObjects();

        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode(UUID.randomUUID().toString());
        createSpace(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, spaceCreation);

        List<Space> trSpacesAfterCreation =
                searchSpaces(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, new SpaceSearchCriteria(), new SpaceFetchOptions());
        List<Space> noTrSpacesAfterCreation = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions()).getObjects();

        // transaction sees the space, outside transaction it is not visible
        assertEquals(Collections.singleton(spaceCreation.getCode().toUpperCase()),
                difference(codes(trSpacesAfterCreation), codes(trSpacesBeforeCreation)));
        assertEquals(Collections.emptySet(), difference(codes(noTrSpacesAfterCreation), codes(noTrSpacesBeforeCreation)));

        // transaction creates a project
        List<Project> trProjectsBeforeCreation =
                searchProjects(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, new ProjectSearchCriteria(), new ProjectFetchOptions());
        List<Project> noTrProjectsBeforeCreation =
                v3api.searchProjects(sessionToken, new ProjectSearchCriteria(), new ProjectFetchOptions()).getObjects();

        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setSpaceId(new SpacePermId(spaceCreation.getCode()));
        projectCreation.setCode(UUID.randomUUID().toString());
        createProject(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, projectCreation);

        List<Project> trProjectsAfterCreation =
                searchProjects(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY, new ProjectSearchCriteria(), new ProjectFetchOptions());
        List<Project> noTrProjectsAfterCreation =
                v3api.searchProjects(sessionToken, new ProjectSearchCriteria(), new ProjectFetchOptions()).getObjects();

        // transaction sees the project, outside transaction it is not visible
        assertEquals(Collections.singleton("/" + spaceCreation.getCode().toUpperCase() + "/" + projectCreation.getCode().toUpperCase()),
                difference(identifiers(trProjectsAfterCreation), identifiers(trProjectsBeforeCreation)));
        assertEquals(Collections.emptySet(), difference(identifiers(noTrProjectsAfterCreation), identifiers(noTrProjectsBeforeCreation)));

        if (rollback)
        {
            coordinator.rollbackTransaction(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);
        } else
        {
            coordinator.commitTransaction(trId, sessionToken, TEST_INTERACTIVE_SESSION_KEY);
        }

        List<Space> noTrSpacesAfterCommit = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions()).getObjects();
        List<Project> noTrProjectsAfterCommit =
                v3api.searchProjects(sessionToken, new ProjectSearchCriteria(), new ProjectFetchOptions()).getObjects();

        if (rollback)
        {
            // both the space and the project are rolled back and are not visible outside the transaction
            assertEquals(codes(noTrSpacesAfterCommit), codes(trSpacesBeforeCreation));
            assertEquals(identifiers(noTrProjectsAfterCommit), identifiers(trProjectsBeforeCreation));
        } else
        {
            // both the space and the project are committed and are visible outside the transaction
            assertEquals(Collections.singleton(spaceCreation.getCode().toUpperCase()),
                    difference(codes(noTrSpacesAfterCommit), codes(trSpacesBeforeCreation)));
            assertEquals(Collections.singleton("/" + spaceCreation.getCode().toUpperCase() + "/" + projectCreation.getCode().toUpperCase()),
                    difference(identifiers(noTrProjectsAfterCommit), identifiers(trProjectsBeforeCreation)));
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

        List<Space> tr1SpacesBeforeCreations =
                searchSpaces(tr1Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, new SpaceSearchCriteria(), new SpaceFetchOptions());
        List<Space> tr2SpacesBeforeCreations =
                searchSpaces(tr2Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, new SpaceSearchCriteria(), new SpaceFetchOptions());
        List<Space> noTrSpacesBeforeCreations = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions()).getObjects();

        // create space1 in tr1
        SpaceCreation tr1Creation = new SpaceCreation();
        tr1Creation.setCode(UUID.randomUUID().toString());
        createSpace(tr1Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, tr1Creation);

        // create space2 in tr2
        SpaceCreation tr2Creation = new SpaceCreation();
        tr2Creation.setCode(UUID.randomUUID().toString());
        createSpace(tr2Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, tr2Creation);

        // create space3 in noTr
        SpaceCreation noTrCreation = new SpaceCreation();
        noTrCreation.setCode(UUID.randomUUID().toString());
        v3api.createSpaces(sessionToken, Collections.singletonList(noTrCreation));

        List<Space> tr1SpacesAfterCreations =
                searchSpaces(tr1Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, new SpaceSearchCriteria(), new SpaceFetchOptions());
        List<Space> tr2SpacesAfterCreations =
                searchSpaces(tr2Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, new SpaceSearchCriteria(), new SpaceFetchOptions());
        List<Space> noTrSpacesAfterCreations = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions()).getObjects();

        // check that tr1 sees only space1, tr2 sees only space2, noTr sees space3
        assertEquals(Collections.singleton(tr1Creation.getCode().toUpperCase()),
                difference(codes(tr1SpacesAfterCreations), codes(tr1SpacesBeforeCreations)));
        assertEquals(Collections.singleton(tr2Creation.getCode().toUpperCase()),
                difference(codes(tr2SpacesAfterCreations), codes(tr2SpacesBeforeCreations)));
        assertEquals(Collections.singleton(noTrCreation.getCode().toUpperCase()),
                difference(codes(noTrSpacesAfterCreations), codes(noTrSpacesBeforeCreations)));

        coordinator.commitTransaction(tr1Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        try
        {
            searchSpaces(tr1Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, new SpaceSearchCriteria(), new SpaceFetchOptions());
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "java.lang.IllegalStateException: Transaction '" + tr1Id
                    + "' unexpected status 'NEW'. Expected statuses '[BEGIN_FINISHED]'.");
        }

        // after tr1 commit, tr2 sees space1 and space2, noTr sees space1 and space3
        List<Space> tr2SpacesAfterTr1Commit =
                searchSpaces(tr2Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, new SpaceSearchCriteria(), new SpaceFetchOptions());
        List<Space> noTrSpacesAfterTr1Commit = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions()).getObjects();

        assertEquals(Collections.singleton(tr1Creation.getCode().toUpperCase()),
                difference(codes(tr2SpacesAfterTr1Commit), codes(tr2SpacesAfterCreations)));
        assertEquals(Collections.singleton(tr1Creation.getCode().toUpperCase()),
                difference(codes(noTrSpacesAfterTr1Commit), codes(noTrSpacesAfterCreations)));

        coordinator.rollbackTransaction(tr2Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY);

        try
        {
            searchSpaces(tr2Id, sessionToken, TEST_INTERACTIVE_SESSION_KEY, new SpaceSearchCriteria(), new SpaceFetchOptions());
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "java.lang.IllegalStateException: Transaction '" + tr2Id
                    + "' unexpected status 'NEW'. Expected statuses '[BEGIN_FINISHED]'.");
        }

        // after tr1 commit and tr2 rollback, noTr sees space1 and space3
        noTrSpacesAfterTr1Commit = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions()).getObjects();
        assertEquals(Collections.singleton(tr1Creation.getCode().toUpperCase()),
                difference(codes(noTrSpacesAfterTr1Commit), codes(noTrSpacesAfterCreations)));
    }

    private List<Space> searchSpaces(UUID transactionId, String sessionToken, String interactiveSessionKey, SpaceSearchCriteria criteria,
            SpaceFetchOptions fetchOptions)
    {
        SearchResult<Space> searchResult = coordinator.executeOperation(transactionId, sessionToken, interactiveSessionKey,
                ITransactionCoordinatorApi.APPLICATION_SERVER_PARTICIPANT_ID, "searchSpaces", new Object[] { sessionToken, criteria, fetchOptions });
        return searchResult.getObjects();
    }

    private List<Project> searchProjects(UUID transactionId, String sessionToken, String interactiveSessionKey, ProjectSearchCriteria criteria,
            ProjectFetchOptions fetchOptions)
    {
        SearchResult<Project> searchResult = coordinator.executeOperation(transactionId, sessionToken, interactiveSessionKey,
                ITransactionCoordinatorApi.APPLICATION_SERVER_PARTICIPANT_ID, "searchProjects",
                new Object[] { sessionToken, criteria, fetchOptions });
        return searchResult.getObjects();
    }

    private SpacePermId createSpace(UUID transactionId, String sessionToken, String interactiveSessionKey, SpaceCreation creation)
    {
        List<SpacePermId> permIds = coordinator.executeOperation(transactionId, sessionToken, interactiveSessionKey,
                ITransactionCoordinatorApi.APPLICATION_SERVER_PARTICIPANT_ID, "createSpaces",
                new Object[] { sessionToken, Collections.singletonList(creation) });
        return permIds.get(0);
    }

    private ProjectPermId createProject(UUID transactionId, String sessionToken, String interactiveSessionKey, ProjectCreation creation)
    {
        List<ProjectPermId> permIds = coordinator.executeOperation(transactionId, sessionToken, interactiveSessionKey,
                ITransactionCoordinatorApi.APPLICATION_SERVER_PARTICIPANT_ID, "createProjects",
                new Object[] { sessionToken, Collections.singletonList(creation) });
        return permIds.get(0);
    }

    private Set<String> codes(Collection<? extends ICodeHolder> objectsWithCodes)
    {
        return objectsWithCodes.stream().map(ICodeHolder::getCode).collect(Collectors.toSet());
    }

    private Set<String> identifiers(Collection<? extends IIdentifierHolder> objectsWithIdentifiers)
    {
        return objectsWithIdentifiers.stream().map(o -> o.getIdentifier().getIdentifier()).collect(Collectors.toSet());
    }

    private <T> Set<T> difference(Set<T> s1, Set<T> s2)
    {
        Set<T> temp = new HashSet<>(s1);
        temp.removeAll(s2);
        return temp;
    }

}
