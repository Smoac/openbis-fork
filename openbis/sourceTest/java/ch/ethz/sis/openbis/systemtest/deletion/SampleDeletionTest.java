package ch.ethz.sis.openbis.systemtest.deletion;

import java.util.Arrays;

import org.springframework.test.annotation.Rollback;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

public class SampleDeletionTest extends DeletionTest
{

    @Test
    @Rollback(false)
    public void changeSpace() throws Exception
    {
        SpacePermId space1 = createSpace("SPACE1");
        SpacePermId space2 = createSpace("SPACE2");

        SamplePermId sample = createSample(null, space1, "SAMPLE_1");

        newTx();

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sample);
        update.setSpaceId(space2);
        v3api.updateSamples(sessionToken, Arrays.asList(update));

        newTx();

        delete(sample);
        delete(space1, space2);

        assertHistory(sample.getPermId(), "OWNED", set(space1.getPermId()), set(space2.getPermId()));
    }

    @Test
    @Rollback(false)
    public void changeExperiment() throws Exception
    {
        SpacePermId space1 = createSpace("SPACE1");
        SpacePermId space2 = createSpace("SPACE2");
        ProjectPermId project1 = createProject(space1, "PROJECT1");
        ProjectPermId project2 = createProject(space2, "PROJECT2");
        ExperimentPermId experiment1 = createExperiment(project1, "EXPERIMENT1");
        ExperimentPermId experiment2 = createExperiment(project2, "EXPERIMENT2");

        SamplePermId sample = createSample(experiment1, space1, "SAMPLE");

        newTx();

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sample);
        update.setExperimentId(experiment2);
        update.setSpaceId(space2);
        v3api.updateSamples(sessionToken, Arrays.asList(update));

        newTx();

        delete(sample);
        delete(experiment1, experiment2);
        delete(project1, project2);
        delete(space1, space2);

        assertHistory(sample.getPermId(), "OWNED", set(experiment1.getPermId()), set(experiment2.getPermId()));
    }

    @Test
    @Rollback(false)
    public void changeChildren() throws Exception
    {

        SpacePermId space = createSpace("SPACE");

        SamplePermId sample = createSample(null, space, "SAMPLE");
        SamplePermId child1 = createSample(null, space, "CHILD1");
        SamplePermId child2 = createSample(null, space, "CHILD2");

        newTx();
        updateChildren(sample, child1);

        newTx();
        updateChildren(sample, child1, child2);

        newTx();
        updateChildren(sample, child2);

        newTx();

        delete(sample);
        delete(child1, child2);
        delete(space);

        assertHistory(sample.getPermId(), "PARENT",
                set(child1.getPermId()),
                set(child1.getPermId(), child2.getPermId()),
                set(child2.getPermId()),
                set());
    }

    @Test
    @Rollback(false)
    public void changeParents() throws Exception
    {
        SpacePermId space = createSpace("SPACE");

        SamplePermId parent1 = createSample(null, space, "PARENT1");
        SamplePermId parent2 = createSample(null, space, "PARENT2");
        SamplePermId sample = createSample(null, space, "SAMPLE");

        newTx();
        updateParents(sample, parent1);

        newTx();
        updateParents(sample, parent1, parent2);

        newTx();
        updateParents(sample, parent2);

        newTx();

        delete(sample);

        newTx();

        delete(parent1, parent2);
        delete(space);

        assertHistory(sample.getPermId(), "CHILD",
                set(parent1.getPermId()),
                set(parent1.getPermId(), parent2.getPermId()),
                set(parent2.getPermId()),
                set());
    }

    @Test
    @Rollback(false)
    public void changeContainer() throws Exception
    {
        SpacePermId space = createSpace("SPACE");

        SamplePermId sample = createSample(null, space, "SAMPLE");
        SamplePermId container1 = createSample(null, space, "CONTAINER1");
        SamplePermId container2 = createSample(null, space, "CONTAINER2");

        newTx();
        updateContainer(sample, container1);

        newTx();
        updateContainer(sample, container2);

        newTx();
        delete(sample);
        delete(container1, container2);
        delete(space);

        assertHistory(sample.getPermId(), "CONTAINED",
                set(container1.getPermId()),
                set(container2.getPermId()));

    }

    @Test
    @Rollback(false)
    public void changeComponents() throws Exception
    {

        SpacePermId space = createSpace("SPACE");

        SamplePermId sample = createSample(null, space, "SAMPLE");
        SamplePermId component1 = createSample(null, space, "COMPONENT1");
        SamplePermId component2 = createSample(null, space, "COMPONENT2");

        newTx();
        updateComponents(sample, component1);

        newTx();
        updateComponents(sample, component1, component2);

        newTx();
        updateComponents(sample, component2);

        newTx();
        updateComponents(sample);

        newTx();

        delete(sample);

        newTx();

        delete(component1, component2);
        delete(space);

        newTx();

        assertHistory(sample.getPermId(), "CONTAINER",
                set(component1.getPermId()),
                set(component1.getPermId(), component2.getPermId()),
                set(component2.getPermId()),
                set());
    }

    @Test
    @Rollback(false)
    public void changeProperties() throws Exception
    {
        SpacePermId space = createSpace("SPACE");

        SamplePermId sample = createSample(null, space, "SAMPLE",
                "DESCRIPTION", "desc", "ORGANISM", "FLY", "BACTERIUM", "BACTERIUM-X");

        newTx();
        setProperties(sample, "DESCRIPTION", "desc2", "ORGANISM", "GORILLA", "BACTERIUM", "BACTERIUM-Y");

        newTx();
        setProperties(sample, "DESCRIPTION", null, "ORGANISM", null, "BACTERIUM", null);

        newTx();
        setProperties(sample, "DESCRIPTION", "desc3", "ORGANISM", "DOG", "BACTERIUM", "BACTERIUM2");

        newTx();
        delete(sample);
        delete(space);

        assertHistory(sample.getPermId(), "DESCRIPTION", "desc", "", "desc2", "", "desc3");
        assertHistory(sample.getPermId(), "ORGANISM", "FLY [ORGANISM]", "", "GORILLA [ORGANISM]", "", "DOG [ORGANISM]");
        assertHistory(sample.getPermId(), "BACTERIUM", "BACTERIUM-X [BACTERIUM]", "", "BACTERIUM-Y [BACTERIUM]", "", "BACTERIUM2 [BACTERIUM]");
    }

}
