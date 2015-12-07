package ch.ethz.sis.openbis.systemtest.api.v3;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.space.id.SpacePermId;

public class AbstractSampleTest extends AbstractTest
{

    protected static SampleCreation masterPlateCreation(String spaceCode, String code)
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode(code);
        creation.setTypeId(new EntityTypePermId("MASTER_PLATE"));
        creation.setSpaceId(new SpacePermId(spaceCode));
        creation.setProperty("$PLATE_GEOMETRY", "384_WELLS_16X24");
        return creation;
    }

    protected static SampleCreation wellCreation(String spaceCode, String code)
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode(code);
        creation.setTypeId(new EntityTypePermId("WELL"));
        creation.setSpaceId(new SpacePermId(spaceCode));
        return creation;
    }

}
