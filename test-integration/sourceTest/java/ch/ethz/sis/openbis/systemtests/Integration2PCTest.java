package ch.ethz.sis.openbis.systemtests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.OpenBIS;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.systemtests.common.AbstractIntegrationTest;
import ch.systemsx.cisd.common.test.AssertionUtil;

public class Integration2PCTest extends AbstractIntegrationTest
{

    private static final String CODE_PREFIX = "TRANSACTION_TEST_";

    private static final String OWNER_PREFIX = "test-owner-";

    private static final String SOURCE_PREFIX = "test-source-";

    private static final String CONTENT = "test-content";

    @Test
    public void testTransactionCommit()
    {
        testTransaction(false);
    }

    @Test
    public void testTransactionRollback()
    {
        testTransaction(true);
    }

    private void testTransaction(boolean rollback)
    {
        OpenBIS openBISWithTr = createOpenBIS();
        OpenBIS openBISWithNoTr = createOpenBIS();

        openBISWithTr.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);

        openBISWithTr.login(USER, PASSWORD);
        openBISWithNoTr.login(USER, PASSWORD);

        openBISWithTr.beginTransaction();

        String owner = OWNER_PREFIX + UUID.randomUUID();
        String source = SOURCE_PREFIX + UUID.randomUUID();
        byte[] bytesToWrite = CONTENT.getBytes(StandardCharsets.UTF_8);

        openBISWithTr.getAfsServerFacade().write(owner, source, 0L, bytesToWrite, calculateMD5(bytesToWrite));

        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode(CODE_PREFIX + UUID.randomUUID());

        SpacePermId spaceId = openBISWithTr.createSpaces(List.of(spaceCreation)).get(0);

        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setSpaceId(spaceId);
        projectCreation.setCode(CODE_PREFIX + UUID.randomUUID());

        ProjectPermId projectId = openBISWithTr.createProjects(List.of(projectCreation)).get(0);

        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setProjectId(projectId);
        experimentCreation.setCode(CODE_PREFIX + UUID.randomUUID());

        try
        {
            openBISWithTr.createExperiments(List.of(experimentCreation));
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Operation 'createExperiments' failed.");
            AssertionUtil.assertContains("Type id cannot be null", e.getCause().getMessage());
        }

        ExperimentCreation experimentCreation2 = new ExperimentCreation();
        experimentCreation2.setTypeId(new EntityTypePermId("UNKNOWN"));
        experimentCreation2.setProjectId(projectId);
        experimentCreation2.setCode(CODE_PREFIX + UUID.randomUUID());

        ExperimentPermId experimentId = openBISWithTr.createExperiments(List.of(experimentCreation2)).get(0);

        // the transaction session sees created entities before they are committed (except for afs changes with are not visible until commit)
        Space trSpaceBefore = openBISWithTr.getSpaces(Collections.singletonList(spaceId), new SpaceFetchOptions()).get(spaceId);
        Project trProjectBefore = openBISWithTr.getProjects(Collections.singletonList(projectId), new ProjectFetchOptions()).get(projectId);
        Experiment trExperimentBefore =
                openBISWithTr.getExperiments(Collections.singletonList(experimentId), new ExperimentFetchOptions()).get(experimentId);

        assertNotNull(trSpaceBefore);
        assertNotNull(trProjectBefore);
        assertNotNull(trExperimentBefore);

        // the non-transaction session does not see created entities before they are committed
        Space noTrSpaceBefore = openBISWithNoTr.getSpaces(Collections.singletonList(spaceId), new SpaceFetchOptions()).get(spaceId);
        Project noTrProjectBefore = openBISWithNoTr.getProjects(Collections.singletonList(projectId), new ProjectFetchOptions()).get(projectId);
        Experiment noTrExperimentBefore =
                openBISWithNoTr.getExperiments(Collections.singletonList(experimentId), new ExperimentFetchOptions()).get(experimentId);

        assertNull(noTrSpaceBefore);
        assertNull(noTrProjectBefore);
        assertNull(noTrExperimentBefore);

        if (rollback)
        {
            openBISWithTr.rollbackTransaction();

        } else
        {
            openBISWithTr.commitTransaction();
        }

        Space trSpaceAfter = openBISWithTr.getSpaces(Collections.singletonList(spaceId), new SpaceFetchOptions()).get(spaceId);
        Project trProjectAfter = openBISWithTr.getProjects(Collections.singletonList(projectId), new ProjectFetchOptions()).get(projectId);
        Experiment trExperimentAfter =
                openBISWithTr.getExperiments(Collections.singletonList(experimentId), new ExperimentFetchOptions()).get(experimentId);

        Space noTrSpaceAfter = openBISWithNoTr.getSpaces(Collections.singletonList(spaceId), new SpaceFetchOptions()).get(spaceId);
        Project noTrProjectAfter = openBISWithNoTr.getProjects(Collections.singletonList(projectId), new ProjectFetchOptions()).get(projectId);
        Experiment noTrExperimentAfter =
                openBISWithNoTr.getExperiments(Collections.singletonList(experimentId), new ExperimentFetchOptions()).get(experimentId);

        if (rollback)
        {
            // neither the transaction session nor the non-transaction session see the created entities after the rollback
            assertNull(trSpaceAfter);
            assertNull(trProjectAfter);
            assertNull(trExperimentAfter);

            assertNull(noTrSpaceAfter);
            assertNull(noTrProjectAfter);
            assertNull(noTrExperimentAfter);
        } else
        {
            // both the transaction session and the non-transaction session see the created entities after the commit
            assertNotNull(trSpaceAfter);
            assertNotNull(trProjectAfter);
            assertNotNull(trExperimentAfter);

            assertNotNull(noTrSpaceAfter);
            assertNotNull(noTrProjectAfter);
            assertNotNull(noTrExperimentAfter);

            byte[] trBytesRead = openBISWithTr.getAfsServerFacade().read(owner, source, 0L, bytesToWrite.length);
            byte[] noTrBytesRead = openBISWithNoTr.getAfsServerFacade().read(owner, source, 0L, bytesToWrite.length);

            assertEquals(new String(trBytesRead, StandardCharsets.UTF_8), CONTENT);
            assertEquals(new String(noTrBytesRead, StandardCharsets.UTF_8), CONTENT);
        }
    }

    public static byte[] calculateMD5(byte[] data)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);
            return md.digest();
        } catch (Exception e)
        {
            throw new RuntimeException("Checksum calculation failed", e);
        }
    }

}
