package ch.ethz.sis.openbis.systemtest.asapi.v3;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

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

    protected void createNewPropertyType(String sessionToken, String entityTypeCode, String propertyTypeCode)
    {
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(propertyTypeCode);
        propertyType.setDescription(propertyTypeCode + " description");
        propertyType.setLabel(propertyTypeCode + " label");
        propertyType.setDataType(new DataType(DataTypeCode.VARCHAR));

        NewETPTAssignment assignment = new NewETPTAssignment();
        assignment.setPropertyTypeCode(propertyType.getCode());
        assignment.setEntityTypeCode(entityTypeCode);
        assignment.setEntityKind(EntityKind.SAMPLE);
        assignment.setOrdinal(1000L);

        commonServer.registerAndAssignPropertyType(sessionToken, propertyType, assignment);
    }

}
