package ch.ethz.sis.openbis.systemtest.asapi.v3;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SampleCommonEntityTest extends AbstractCommonEntityTest<SampleTypeCreation, SampleType, SampleTypeUpdate, SampleCreation, Sample, SampleUpdate, ISampleId>
{

    @Override
    protected SampleTypeCreation newTypeCreation()
    {
        return new SampleTypeCreation();
    }


    @Override
    protected SampleCreation newEntityCreation(SampleType type, String code)
    {
        final SampleCreation creation = new SampleCreation();
        creation.setTypeId(type.getPermId());
        creation.setCode(code);
        return creation;
    }

    @Override
    protected SampleTypeUpdate newTypeUpdate(String typeCode)
    {
        SampleTypeUpdate update = new SampleTypeUpdate();
        update.setTypeId(new EntityTypePermId(typeCode));
        return update;
    }

    @Override
    protected SampleUpdate newEntityUpdate(String permId)
    {
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId(permId));
        return update;
    }

    @Override
    protected void createType(String sessionToken, SampleTypeCreation creations)
    {
        v3api.createSampleTypes(sessionToken, Arrays.asList(creations));
    }

    @Override
    protected SampleType getType(String sessionToken, String typeCode)
    {
        SampleTypeFetchOptions fo = new SampleTypeFetchOptions();
        fo.withPropertyAssignments().withPropertyType();
        fo.withPropertyAssignments().withRegistrator();

        final EntityTypePermId permId = new EntityTypePermId(typeCode);
        return v3api.getSampleTypes(sessionToken, Collections.singletonList(permId), fo)
                .get(permId);
    }

    @Override
    protected ISampleId createEntity(String sessionToken, SampleCreation creation)
    {
        List<SamplePermId> permIds = v3api.createSamples(sessionToken, Arrays.asList(creation));
        return permIds.get(0);
    }

    @Override
    protected Sample getEntity(String sessionToken, ISampleId id)
    {
        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withProperties();
        Map<ISampleId, Sample> samples = v3api.getSamples(sessionToken, Arrays.asList(id), fo);
        return samples.values().iterator().next();
    }

    @Override
    protected void updateType(String sessionToken, SampleTypeUpdate update)
    {
        v3api.updateSampleTypes(sessionToken, Arrays.asList(update));
    }

    @Override
    protected void updateEntity(String sessionToken, SampleUpdate update)
    {
        v3api.updateSamples(sessionToken, Arrays.asList(update));
    }

    @Test
    public void testDummy() {
        //dummy test for a simple test run
    }
}
