/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.server.util.IPropertyValueValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for corresponding {@link EntityPropertiesConverter} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = EntityPropertiesConverter.class)
public final class EntityPropertiesConverterTest extends AbstractBOTest
{
    private static final String VARCHAR_PROPERTY_TYPE_CODE = "color";

    private static final String SAMPLE_TYPE_CODE = "MASTER_PLATE";

    private IPropertyValueValidator propertyValueValidator;

    @Override
    @BeforeMethod
    public final void beforeMethod()
    {
        super.beforeMethod();
        propertyValueValidator = context.mock(IPropertyValueValidator.class);
    }

    private final IEntityPropertiesConverter createEntityPropertiesConverter(
            final EntityKind entityKind)
    {
        return new EntityPropertiesConverter(entityKind, daoFactory, propertyValueValidator);
    }

    private void prepareForConvertion(final Expectations exp)
    {
        prepareForConvertion(exp, false);
    }

    private void prepareForConvertion(final Expectations exp, boolean mandatory)
    {
        final SampleTypePE sampleType = createSampleType(SAMPLE_TYPE_CODE);
        final SampleTypePropertyTypePE sampleTypePropertyTypePE = new SampleTypePropertyTypePE();
        sampleTypePropertyTypePE.setEntityType(sampleType);
        final PropertyTypePE propertyType = new PropertyTypePE();
        propertyType.setCode(VARCHAR_PROPERTY_TYPE_CODE);
        sampleTypePropertyTypePE.setPropertyType(propertyType);
        sampleTypePropertyTypePE.setMandatory(mandatory);

        exp.allowing(daoFactory).getEntityPropertyTypeDAO(EntityKind.SAMPLE);
        exp.will(Expectations.returnValue(entityPropertyTypeDAO));

        exp.allowing(daoFactory).getEntityTypeDAO(EntityKind.SAMPLE);
        exp.will(Expectations.returnValue(entityTypeDAO));

        exp.allowing(daoFactory).getPropertyTypeDAO();
        exp.will(Expectations.returnValue(propertyTypeDAO));

        exp.atLeast(1).of(entityTypeDAO).listEntityTypes();
        exp.will(Expectations.returnValue(Collections.singletonList(sampleType)));

        exp.allowing(entityPropertyTypeDAO).listEntityPropertyTypes(sampleType);
        exp.will(Expectations.returnValue(Collections.singletonList(sampleTypePropertyTypePE)));
    }

    private final static SampleTypePE createSampleType(final String sampleTypeCode)
    {
        final SampleTypePE sampleType = new SampleTypePE();
        sampleType.setCode(sampleTypeCode);
        sampleType.setDatabaseInstance(ManagerTestTool.EXAMPLE_DATABASE_INSTANCE);
        return sampleType;
    }

    private final static IEntityProperty createVarcharSampleProperty(final boolean lowerCase,
            String code)
    {
        final IEntityProperty sampleProperty = new EntityProperty();
        sampleProperty.setValue("blue");
        final PropertyType propertyType = new PropertyType();
        String newCode = code;
        if (lowerCase)
        {
            newCode = newCode.toLowerCase();
        }
        propertyType.setLabel(newCode);
        propertyType.setCode(newCode);
        final DataType dataType = new DataType();
        dataType.setCode(DataTypeCode.VARCHAR);
        propertyType.setDataType(dataType);
        sampleProperty.setPropertyType(propertyType);
        return sampleProperty;
    }

    private final IEntityProperty[] createSampleProperties(final boolean lowerCase)
    {
        return new IEntityProperty[]
            { createVarcharSampleProperty(lowerCase, VARCHAR_PROPERTY_TYPE_CODE) };
    }

    @Test
    public final void testConvertPropertiesFailed()
    {
        final IEntityPropertiesConverter entityPropertiesConverter =
                createEntityPropertiesConverter(EntityKind.MATERIAL);
        boolean fail = true;
        try
        {
            entityPropertiesConverter.convertProperties(null, null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @Test
    public final void testConvertPropertiesWithEmptyProperties()
    {
        final IEntityPropertiesConverter entityPropertiesConverter =
                createEntityPropertiesConverter(EntityKind.SAMPLE);
        context.checking(new Expectations()
            {
                {
                    prepareForConvertion(this);
                }
            });
        final List<EntityPropertyPE> properties =
                entityPropertiesConverter.convertProperties(IEntityProperty.EMPTY_ARRAY,
                        SAMPLE_TYPE_CODE, ManagerTestTool.EXAMPLE_PERSON);
        assertEquals(0, properties.size());
        context.assertIsSatisfied();
    }

    @Test
    public final void testConvertProperties()
    {
        final IEntityPropertiesConverter entityPropertiesConverter =
                createEntityPropertiesConverter(EntityKind.SAMPLE);
        final PropertyTypePE propertyTypePE = createPropertyType();
        context.checking(new Expectations()
            {
                {
                    prepareForConvertion(this);

                    one(propertyTypeDAO).tryFindPropertyTypeByCode(VARCHAR_PROPERTY_TYPE_CODE);
                    will(returnValue(propertyTypePE));

                    one(propertyValueValidator).validatePropertyValue(propertyTypePE, "blue");
                }
            });
        final IEntityProperty[] properties = createSampleProperties(false);
        final List<EntityPropertyPE> convertedProperties =
                entityPropertiesConverter.convertProperties(properties, SAMPLE_TYPE_CODE,
                        ManagerTestTool.EXAMPLE_PERSON);
        assertEquals(1, convertedProperties.size());
        context.assertIsSatisfied();
    }

    @Test
    public final void testConvertPropertiesWithLowerCase()
    {
        final IEntityPropertiesConverter entityPropertiesConverter =
                createEntityPropertiesConverter(EntityKind.SAMPLE);
        final PropertyTypePE propertyTypePE = createPropertyType();
        context.checking(new Expectations()
            {
                {
                    prepareForConvertion(this);

                    one(propertyTypeDAO).tryFindPropertyTypeByCode(VARCHAR_PROPERTY_TYPE_CODE);
                    will(returnValue(propertyTypePE));

                    one(propertyValueValidator).validatePropertyValue(propertyTypePE, "blue");
                }
            });
        final IEntityProperty[] properties = createSampleProperties(true);
        final List<EntityPropertyPE> convertedProperties =
                entityPropertiesConverter.convertProperties(properties, SAMPLE_TYPE_CODE
                        .toLowerCase(), ManagerTestTool.EXAMPLE_PERSON);
        assertEquals(1, convertedProperties.size());
        context.assertIsSatisfied();
    }

    private PropertyTypePE createPropertyType()
    {
        final PropertyTypePE propertyTypePE = new PropertyTypePE();
        propertyTypePE.setCode(VARCHAR_PROPERTY_TYPE_CODE.toLowerCase());
        DataTypePE type = new DataTypePE();
        type.setCode(DataTypeCode.VARCHAR);
        propertyTypePE.setType(type);
        return propertyTypePE;
    }

    @Test
    public void testCreateProperty() throws Exception
    {
        final IEntityPropertiesConverter entityPropertiesConverter =
                createEntityPropertiesConverter(EntityKind.SAMPLE);
        final PropertyTypePE propertyType = createPropertyType();
        EntityKind entityKind = EntityKind.EXPERIMENT;
        EntityTypePropertyTypePE assignment =
                EntityTypePropertyTypePE.createEntityTypePropertyType(entityKind);
        PersonPE registrator = new PersonPE();
        final String defaultValue = "val";
        context.checking(new Expectations()
            {
                {
                    one(propertyValueValidator).validatePropertyValue(propertyType, defaultValue);
                }
            });
        entityPropertiesConverter.tryCreateValidatedPropertyValue(propertyType, assignment,
                defaultValue);
        assertEquals(registrator, entityPropertiesConverter.createValidatedProperty(propertyType,
                assignment, registrator, defaultValue).getRegistrator());
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testCreateValidatedPropertyValueMandatoryWithNullGlobal() throws Exception
    {
        final IEntityPropertiesConverter entityPropertiesConverter =
                createEntityPropertiesConverter(EntityKind.SAMPLE);
        final PropertyTypePE propertyType = new PropertyTypePE();
        EntityKind entityKind = EntityKind.EXPERIMENT;
        EntityTypePropertyTypePE assignment =
                EntityTypePropertyTypePE.createEntityTypePropertyType(entityKind);
        assignment.setMandatory(true);
        final String defaultValue = null;

        entityPropertiesConverter.tryCreateValidatedPropertyValue(propertyType, assignment,
                defaultValue);
        context.assertIsSatisfied();
    }

    @Test
    public void testCreateValidatedPropertyValueNotMandatoryWithNullGlobal() throws Exception
    {
        final IEntityPropertiesConverter entityPropertiesConverter =
                createEntityPropertiesConverter(EntityKind.EXPERIMENT);
        final PropertyTypePE propertyType = new PropertyTypePE();
        EntityKind entityKind = EntityKind.EXPERIMENT;
        EntityTypePropertyTypePE assignment =
                EntityTypePropertyTypePE.createEntityTypePropertyType(entityKind);
        assignment.setMandatory(false);
        final String defaultValue = null;
        assertNull(entityPropertiesConverter.tryCreateValidatedPropertyValue(propertyType,
                assignment, defaultValue));
        context.assertIsSatisfied();
    }
}
