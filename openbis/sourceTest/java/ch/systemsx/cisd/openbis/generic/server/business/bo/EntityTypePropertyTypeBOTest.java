/*
 * Copyright 2007 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;

import java.util.ArrayList;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for corresponding {@link EntityTypePropertyTypeBO} class.
 * 
 * @author Izabela Adamczyk
 */
public final class EntityTypePropertyTypeBOTest extends AbstractBOTest
{

    @Test
    public void testCreateAssignment()
    {

        final EntityKind entityKind = EntityKind.EXPERIMENT;
        final String propertyTypeCode = "USER.DISTANCE";
        final String entityTypeCode = "ARCHERY";
        boolean mandatory = true;
        final String defaultValue = "50";

        final ExperimentPE experiment = new ExperimentPE();
        experiment.setCode("MAN");
        experiment.setProject(new ProjectPE());
        final ExperimentPropertyPE property = new ExperimentPropertyPE();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getEntityTypeDAO(entityKind);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(entityTypeCode);
                    final ExperimentTypePE experimentType = new ExperimentTypePE();
                    experimentType.setCode(entityTypeCode);
                    experimentType.setDatabaseInstance(new DatabaseInstancePE());
                    will(returnValue(experimentType));

                    one(daoFactory).getPropertyTypeDAO();
                    will(returnValue(propertyTypeDAO));

                    one(propertyTypeDAO).tryFindPropertyTypeByCode(propertyTypeCode);
                    final PropertyTypePE propertyType = new PropertyTypePE();
                    propertyType.setCode(propertyTypeCode);
                    will(returnValue(propertyType));

                    allowing(daoFactory).getEntityPropertyTypeDAO(entityKind);
                    will(returnValue(entityPropertyTypeDAO));

                    one(entityPropertyTypeDAO).tryFindAssignment(experimentType, propertyType);
                    will(returnValue(null));

                    one(entityPropertyTypeDAO).createEntityPropertyTypeAssignment(
                            with(any(ExperimentTypePropertyTypePE.class)));
                    property.setEntityTypePropertyType(new ExperimentTypePropertyTypePE());

                    one(entityPropertyTypeDAO).listEntities(experimentType);
                    final ArrayList<ExperimentPE> experimets = new ArrayList<ExperimentPE>();

                    experimets.add(experiment);
                    will(returnValue(experimets));

                    one(propertiesConverter).createProperty(with(propertyType),
                            with(any(ExperimentTypePropertyTypePE.class)),
                            with(any(PersonPE.class)), with(defaultValue));
                    will(returnValue(property));
                }
            });
        final EntityTypePropertyTypeBO bo = createEntityTypePropertyTypeBO(EntityKind.EXPERIMENT);
        bo.createAssignment(propertyTypeCode, entityTypeCode, mandatory, defaultValue);
        assertTrue(experiment.getProperties().size() == 1);
        assertEquals(property, experiment.getProperties().toArray()[0]);
        assertEquals(experiment, property.getEntity());

        context.assertIsSatisfied();
    }

    private final EntityTypePropertyTypeBO createEntityTypePropertyTypeBO(EntityKind entityKind)
    {
        return new EntityTypePropertyTypeBO(daoFactory, EXAMPLE_SESSION, entityKind,
                propertiesConverter);
    }

}
