package ch.ethz.sis.openbis.systemtests;

import java.util.List;
import java.util.UUID;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.OpenBIS;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.systemtests.common.AbstractIntegrationTest;

public class IntegrationAfsDataSetTest extends AbstractIntegrationTest
{

    @BeforeClass
    public void beforeClass()
    {
    }

    @Test
    public void testDeleteExperimentWithAfsDataSet()
    {
        OpenBIS openBIS = createOpenBIS();

        openBIS.login(USER, PASSWORD);

        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode("SPACE_" + UUID.randomUUID());

        SpacePermId spaceId = openBIS.createSpaces(List.of(spaceCreation)).get(0);

        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setSpaceId(spaceId);
        projectCreation.setCode("PROJECT_" + UUID.randomUUID());

        ProjectPermId projectId = openBIS.createProjects(List.of(projectCreation)).get(0);

        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setTypeId(new EntityTypePermId("UNKNOWN"));
        experimentCreation.setProjectId(projectId);
        experimentCreation.setCode("EXPERIMENT_" + UUID.randomUUID());

        ExperimentPermId experimentId = openBIS.createExperiments(List.of(experimentCreation)).get(0);

        openBIS.getAfsServerFacade().write(experimentId.getPermId(), "test-file.txt", 0L, "test-content".getBytes());

        ExperimentDeletionOptions options = new ExperimentDeletionOptions();
        options.setReason("It is just a test");

        assertExperimentExistsAtAS(experimentId);
        assertDataSetExistsAtAS(experimentId.getPermId());
        assertDataSetExistsAtAFS(experimentId.getPermId());

        IDeletionId deletionId = openBIS.deleteExperiments(List.of(experimentId), options);
        openBIS.confirmDeletions(List.of(deletionId));

        assertExperimentDoesNotExistAtAS(experimentId);
        assertDataSetDoesNotExistAtAFS(experimentId.getPermId());
        assertDataSetDoesNotExistAtAFS(experimentId.getPermId());
    }

    private void assertExperimentExistsAtAS(IExperimentId experimentId)
    {

    }

    private void assertExperimentDoesNotExistAtAS(IExperimentId experimentId)
    {

    }

    private void assertDataSetExistsAtAFS(String code)
    {

    }

    private void assertDataSetDoesNotExistAtAFS(String code)
    {

    }

    private void assertDataSetExistsAtAS(String code)
    {

    }

    private void assertDataSetDoesNotExistAtAS(String code)
    {

    }

}
