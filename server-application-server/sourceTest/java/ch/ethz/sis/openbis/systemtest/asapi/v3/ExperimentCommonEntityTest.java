package ch.ethz.sis.openbis.systemtest.asapi.v3;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ExperimentCommonEntityTest extends AbstractCommonEntityTest<ExperimentTypeCreation, ExperimentType, ExperimentTypeUpdate, ExperimentCreation, Experiment, ExperimentUpdate, IExperimentId>
{

    @Override
    protected ExperimentTypeCreation newTypeCreation()
    {
        return new ExperimentTypeCreation();
    }


    @Override
    protected ExperimentCreation newEntityCreation(ExperimentType type, String code)
    {
        ExperimentCreation creation = new ExperimentCreation();
        creation.setTypeId(type.getPermId());
        creation.setCode(code);
        creation.setProjectId(new ProjectIdentifier("/TESTGROUP/TESTPROJ"));
        return creation;
    }

    @Override
    protected ExperimentTypeUpdate newTypeUpdate(String typeCode)
    {
        ExperimentTypeUpdate update = new ExperimentTypeUpdate();
        update.setTypeId(new EntityTypePermId(typeCode));
        return update;
    }

    @Override
    protected ExperimentUpdate newEntityUpdate(String permId)
    {
        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(new ExperimentPermId(permId));
        return update;
    }

    @Override
    protected void createType(String sessionToken, ExperimentTypeCreation creations)
    {
        v3api.createExperimentTypes(sessionToken, Arrays.asList(creations));
    }

    @Override
    protected ExperimentType getType(String sessionToken, String typeCode)
    {
        ExperimentTypeFetchOptions fo = new ExperimentTypeFetchOptions();
        fo.withPropertyAssignments().withPropertyType();
        fo.withPropertyAssignments().withRegistrator();

        final EntityTypePermId permId = new EntityTypePermId(typeCode);
        return v3api.getExperimentTypes(sessionToken, Collections.singletonList(permId), fo)
                .get(permId);
    }

    @Override
    protected IExperimentId createEntity(String sessionToken, ExperimentCreation creation)
    {
        List<ExperimentPermId> permIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));
        return permIds.get(0);
    }

    @Override
    protected Experiment getEntity(String sessionToken, IExperimentId id)
    {
        ExperimentFetchOptions fo = new ExperimentFetchOptions();
        fo.withProperties();
        Map<IExperimentId, Experiment> experiments = v3api.getExperiments(sessionToken, Arrays.asList(id), fo);
        return experiments.values().iterator().next();
    }

    @Override
    protected void updateType(String sessionToken, ExperimentTypeUpdate update)
    {
        v3api.updateExperimentTypes(sessionToken, Arrays.asList(update));
    }

    @Override
    protected void updateEntity(String sessionToken, ExperimentUpdate update)
    {
        v3api.updateExperiments(sessionToken, Arrays.asList(update));
    }

    @Test
    public void testDummy() {
        //dummy test for a simple test run
    }
}

