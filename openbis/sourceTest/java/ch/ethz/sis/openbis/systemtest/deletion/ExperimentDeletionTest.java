package ch.ethz.sis.openbis.systemtest.deletion;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.springframework.test.annotation.Rollback;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

public class ExperimentDeletionTest extends DeletionTest
{

    @Test
    @Rollback(false)
    public void testAttributes() throws Exception
    {
        SpacePermId space = createSpace("SPACE1");
        ProjectPermId project = createProject(space, "PROJECT1");
        Date after = new Date();

        ExperimentPermId exp = createExperiment(project, "EXPERIMENT");

        newTx();
        Date before = new Date();

        delete(exp);
        delete(project);
        delete(space);

        HashMap<String, String> expectations = new HashMap<String, String>();
        expectations.put("CODE", "EXPERIMENT");
        expectations.put("ENTITY_TYPE", "DELETION_TEST");
        expectations.put("REGISTRATOR", "test");
        expectations.put("IS_PUBLIC", "false");
        assertAttributes(exp.getPermId(), expectations);
        assertRegistrationTimestampAttribute(exp.getPermId(), after, before);
    }

    @Test
    public void moveExperimentToAnotherProject() throws Exception
    {
        SpacePermId space1 = createSpace("SPACE1");
        SpacePermId space2 = createSpace("SPACE2");

        ProjectPermId project1 = createProject(space1, "PROJECT1");
        ProjectPermId project2 = createProject(space2, "PROJECT2");

        ExperimentPermId experiment = createExperiment(project1, "EXPERIMENT");

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experiment);
        update.setProjectId(project2);
        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        delete(experiment);

        assertHistory(experiment.getPermId(), "OWNED", projectSet(project1.getPermId()), 
                projectSet(project2.getPermId()));
    }

    @Test
    @Rollback(false)
    public void changeProperties() throws Exception
    {
        SpacePermId space = createSpace("SPACE");
        ProjectPermId project = createProject(space, "PROJECT");
        ExperimentPermId experiment = createExperiment(project, "EXPERIMENT",
                "DESCRIPTION", "desc", "ORGANISM", "FLY", "BACTERIUM", "BACTERIUM-X");

        newTx();
        setProperties(experiment, "DESCRIPTION", "desc2", "ORGANISM", "GORILLA", "BACTERIUM", "BACTERIUM-Y");

        newTx();
        setProperties(experiment);

        newTx();
        setProperties(experiment, "DESCRIPTION", "desc3", "ORGANISM", "DOG", "BACTERIUM", "BACTERIUM2");

        delete(experiment);
        delete(project);
        delete(space);

        assertPropertiesHistory(experiment.getPermId(), "DESCRIPTION", "desc", "", "desc2", "", "desc3");
        assertPropertiesHistory(experiment.getPermId(), "ORGANISM", "FLY [ORGANISM]", "", "GORILLA [ORGANISM]", "", "DOG [ORGANISM]");
        assertPropertiesHistory(experiment.getPermId(), "BACTERIUM", "BACTERIUM-X [BACTERIUM]", "", "BACTERIUM-Y [BACTERIUM]", "", "BACTERIUM2 [BACTERIUM]");
    }

    @Test
    @Rollback(false)
    public void addDataSets() throws Exception
    {
        SpacePermId space = createSpace("SPACE");
        ProjectPermId project = createProject(space, "PROJECT");
        ExperimentPermId experiment = createExperiment(project, "EXPERIMENT");
        DataSetPermId dataset1 = createDataSet(experiment, "DATASET_1");
        DataSetPermId dataset2 = createDataSet(experiment, "DATASET_2");

        newTx();
        delete(dataset1);

        newTx();
        delete(dataset2);
        delete(experiment);
        delete(project);
        delete(space);

        assertHistory(experiment.getPermId(), "OWNER", unknownSet(dataset1.getPermId()), 
                unknownSet(dataset1.getPermId(), dataset2.getPermId()),
                unknownSet(dataset2.getPermId()),
                set());
    }

    @Test
    @Rollback(false)
    public void addSamples() throws Exception
    {
        SpacePermId space = createSpace("SPACE");
        ProjectPermId project = createProject(space, "PROJECT");
        ExperimentPermId experiment = createExperiment(project, "EXPERIMENT");

        newTx();
        SamplePermId sample1 = createSample(experiment, space, "SAMPLE_1");

        newTx();
        SamplePermId sample2 = createSample(experiment, space, "SAMPLE_2");

        newTx();
        delete(sample1);

        newTx();
        delete(sample2);
        delete(experiment);
        delete(project);
        delete(space);

        assertHistory(experiment.getPermId(), "OWNER",
                unknownSet(sample1.getPermId()),
                unknownSet(sample1.getPermId(), sample2.getPermId()),
                unknownSet(sample2.getPermId()),
                set());
    }

}
