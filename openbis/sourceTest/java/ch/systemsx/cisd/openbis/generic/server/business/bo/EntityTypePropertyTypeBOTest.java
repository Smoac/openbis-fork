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

import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for corresponding {@link EntityTypePropertyTypeBO} class.
 * 
 * @author Izabela Adamczyk
 */
@Friend(toClasses = EntityTypePropertyTypeBO.class)
public final class EntityTypePropertyTypeBOTest extends AbstractBOTest
{
    @Test
    public void testDeleteAssignment()
    {
        final EntityKind entityKind = EntityKind.EXPERIMENT;
        final ExperimentTypePE experimentType = createExperimentType();
        final PropertyTypePE propertyType = createPropertyType();
        final ExperimentTypePropertyTypePE etpt = new ExperimentTypePropertyTypePE();
        prepareExperimentTypeAndPropertyType(entityKind, experimentType, propertyType);
        context.checking(new Expectations()
            {
                {
                    one(entityPropertyTypeDAO).tryFindAssignment(experimentType, propertyType);
                    will(returnValue(etpt));

                    one(entityPropertyTypeDAO).delete(etpt);
                }
            });

        IEntityTypePropertyTypeBO bo = createEntityTypePropertyTypeBO(entityKind);
        bo.loadAssignment(propertyType.getCode(), experimentType.getCode());
        bo.deleteLoadedAssignment();

        try
        {
            bo.getLoadedAssignment();
            fail("IllegalStateException expected.");
        } catch (IllegalStateException e)
        {
            assertEquals("No assignment loaded.", e.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testLoadAssignment()
    {
        final EntityKind entityKind = EntityKind.EXPERIMENT;
        final ExperimentTypePE experimentType = createExperimentType();
        final PropertyTypePE propertyType = createPropertyType();
        final ExperimentTypePropertyTypePE etpt = new ExperimentTypePropertyTypePE();
        prepareExperimentTypeAndPropertyType(entityKind, experimentType, propertyType);
        context.checking(new Expectations()
            {
                {
                    one(entityPropertyTypeDAO).tryFindAssignment(experimentType, propertyType);
                    will(returnValue(etpt));
                }
            });

        IEntityTypePropertyTypeBO bo = createEntityTypePropertyTypeBO(entityKind);
        bo.loadAssignment(propertyType.getCode(), experimentType.getCode());

        assertSame(etpt, bo.getLoadedAssignment());
        context.assertIsSatisfied();
    }

    @Test
    public void testCreateAssignment()
    {

        final EntityKind entityKind = EntityKind.EXPERIMENT;
        boolean mandatory = true;
        final String defaultValue = "50.00";
        final String validatedValue = "50.0";
        final String section = "section 1";

        final List<Long> experimentIds = Arrays.asList(1L, 2L, 3L);
        final ExperimentPropertyPE property = new ExperimentPropertyPE();
        final ExperimentTypePE experimentType = createExperimentType();
        final PropertyTypePE propertyType = createPropertyType();
        prepareExperimentTypeAndPropertyType(entityKind, experimentType, propertyType);
        context.checking(new Expectations()
            {
                {
                    one(entityPropertyTypeDAO).tryFindAssignment(experimentType, propertyType);
                    will(returnValue(null));

                    one(entityPropertyTypeDAO).increaseOrdinals(experimentType, 2L, 1);

                    one(entityPropertyTypeDAO).createEntityPropertyTypeAssignment(
                            with(any(ExperimentTypePropertyTypePE.class)));
                    property.setEntityTypePropertyType(new ExperimentTypePropertyTypePE());

                    one(entityPropertyTypeDAO).listEntityIds(experimentType);
                    will(returnValue(experimentIds));

                    one(propertiesConverter).tryCreateValidatedPropertyValue(with(propertyType),
                            with(any(ExperimentTypePropertyTypePE.class)), with(defaultValue));
                    will(returnValue(validatedValue));
                    one(propertiesConverter).createValidatedProperty(with(propertyType),
                            with(any(ExperimentTypePropertyTypePE.class)),
                            with(any(PersonPE.class)), with(validatedValue));
                    will(returnValue(property));

                    one(entityPropertyTypeDAO).createProperties(property, experimentIds);
                }
            });
        final EntityTypePropertyTypeBO bo = createEntityTypePropertyTypeBO(EntityKind.EXPERIMENT);
        NewETPTAssignment newAssignment =
                new NewETPTAssignment(
                        ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind.EXPERIMENT,
                        propertyType.getCode(), experimentType.getCode(), mandatory, defaultValue,
                        section, 1L, false, null);
        bo.createAssignment(newAssignment);

        context.assertIsSatisfied();
    }

    private void prepareExperimentTypeAndPropertyType(final EntityKind entityKind,
            final ExperimentTypePE experimentType, final PropertyTypePE propertyType)
    {
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getEntityTypeDAO(entityKind);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(experimentType.getCode());
                    will(returnValue(experimentType));

                    one(daoFactory).getPropertyTypeDAO();
                    will(returnValue(propertyTypeDAO));

                    one(propertyTypeDAO).tryFindPropertyTypeByCode(propertyType.getCode());
                    will(returnValue(propertyType));

                    allowing(daoFactory).getEntityPropertyTypeDAO(entityKind);
                    will(returnValue(entityPropertyTypeDAO));

                }
            });
    }

    private ExperimentTypePE createExperimentType()
    {
        final ExperimentTypePE experimentType = new ExperimentTypePE();
        experimentType.setCode("ARCHERY");
        experimentType.setDatabaseInstance(new DatabaseInstancePE());
        return experimentType;
    }

    private PropertyTypePE createPropertyType()
    {
        final PropertyTypePE propertyType = new PropertyTypePE();
        propertyType.setCode("USER.DISTANCE");
        return propertyType;
    }

    private final EntityTypePropertyTypeBO createEntityTypePropertyTypeBO(EntityKind entityKind)
    {
        return new EntityTypePropertyTypeBO(daoFactory, EXAMPLE_SESSION, entityKind,
                propertiesConverter);
    }

}
