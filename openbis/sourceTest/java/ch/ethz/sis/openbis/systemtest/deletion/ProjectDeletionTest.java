package ch.ethz.sis.openbis.systemtest.deletion;

import java.util.Arrays;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

public class ProjectDeletionTest extends DeletionTest
{

    @Test
    public void moveProjectToAnotherSpace() throws Exception
    {
        SpacePermId space1 = createSpace("SPACE1");
        SpacePermId space2 = createSpace("SPACE2");

        ProjectPermId project = createProject(space1, "PROJECT");

        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(project);
        projectUpdate.setSpaceId(space2);
        v3api.updateProjects(sessionToken, Arrays.asList(projectUpdate));

        delete(project);

        assertHistory(project.getPermId(), "OWNED", spaceSet("SPACE1"), spaceSet("SPACE2"));
    }

    @Test
    public void assignExperimentsToProject() throws Exception
    {
        SpacePermId space = createSpace("SPACE");

        ProjectPermId project1 = createProject(space, "PROJECT1");
        ProjectPermId project2 = createProject(space, "PROJECT2");

        ExperimentPermId experiment1 = createExperiment(project1, "EXPERIMENT1");
        ExperimentPermId experiment2 = createExperiment(project1, "EXPERIMENT2");

        ExperimentUpdate experimentUpdate1 = new ExperimentUpdate();
        experimentUpdate1.setExperimentId(experiment1);
        experimentUpdate1.setProjectId(project2);
        ExperimentUpdate experimentUpdate2 = new ExperimentUpdate();
        experimentUpdate2.setExperimentId(experiment2);
        experimentUpdate2.setProjectId(project2);

        v3api.updateExperiments(sessionToken, Arrays.asList(experimentUpdate1, experimentUpdate2));

        delete(project1);

        assertHistory(project1.getPermId(), "OWNER", experimentSet(experiment1.getPermId()), 
                experimentSet(experiment1.getPermId(), experiment2.getPermId()), set());
    }
}
