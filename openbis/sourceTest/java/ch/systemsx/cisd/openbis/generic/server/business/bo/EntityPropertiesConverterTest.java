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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
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
public final class EntityPropertiesConverterTest
{
    private static final String VARCHAR_PROPERTY_TYPE_CODE = "color";

    private static final String SAMPLE_TYPE_CODE = "MASTER_PLATE";

    private Mockery context;

    private IDAOFactory daoFactory;

    private IEntityPropertyTypeDAO entityPropertyTypeDAO;

    private IEntityTypeDAO entityTypeDAO;

    private IPropertyTypeDAO propertyTypeDAO;

    private IPropertyValueValidator propertyValueValidator;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        daoFactory = context.mock(IDAOFactory.class);
        entityPropertyTypeDAO = context.mock(IEntityPropertyTypeDAO.class);
        entityTypeDAO = context.mock(IEntityTypeDAO.class);
        propertyTypeDAO = context.mock(IPropertyTypeDAO.class);
        propertyValueValidator = context.mock(IPropertyValueValidator.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    private final IEntityPropertiesConverter createEntityPropertiesConverter(
            final EntityKind entityKind)
    {
        return new EntityPropertiesConverter(entityKind, daoFactory, propertyValueValidator);
    }

    private void prepareForConvertion(final Expectations exp)
    {
        final SampleTypePE sampleType = createSampleType(SAMPLE_TYPE_CODE);
        final SampleTypePropertyTypePE sampleTypePropertyTypePE = new SampleTypePropertyTypePE();
        sampleTypePropertyTypePE.setEntityType(sampleType);
        final PropertyTypePE propertyType = new PropertyTypePE();
        propertyType.setCode(VARCHAR_PROPERTY_TYPE_CODE);
        sampleTypePropertyTypePE.setPropertyType(propertyType);

        exp.allowing(daoFactory).getEntityPropertyTypeDAO(EntityKind.SAMPLE);
        exp.will(Expectations.returnValue(entityPropertyTypeDAO));

        exp.allowing(daoFactory).getEntityTypeDAO(EntityKind.SAMPLE);
        exp.will(Expectations.returnValue(entityTypeDAO));

        exp.allowing(daoFactory).getPropertyTypeDAO();
        exp.will(Expectations.returnValue(propertyTypeDAO));

        exp.one(entityTypeDAO).listEntityTypes();
        exp.will(Expectations.returnValue(Collections.singletonList(sampleType)));

        exp.one(entityPropertyTypeDAO).listEntityPropertyTypes(sampleType);
        exp.will(Expectations.returnValue(Collections.singletonList(sampleTypePropertyTypePE)));
    }

    private final static SampleTypePE createSampleType(final String sampleTypeCode)
    {
        final SampleTypePE sampleType = new SampleTypePE();
        sampleType.setCode(sampleTypeCode);
        sampleType.setDatabaseInstance(ManagerTestTool.EXAMPLE_DATABASE_INSTANCE);
        return sampleType;
    }

    private final static SampleProperty createVarcharSampleProperty()
    {
        final SampleProperty sampleProperty = new SampleProperty();
        sampleProperty.setValue("blue");
        final SampleTypePropertyType sampleTypePropertyType = new SampleTypePropertyType();
        final PropertyType propertyType = new PropertyType();
        propertyType.setLabel(VARCHAR_PROPERTY_TYPE_CODE);
        propertyType.setCode(VARCHAR_PROPERTY_TYPE_CODE);
        final DataType dataType = new DataType();
        dataType.setCode(DataTypeCode.VARCHAR);
        propertyType.setDataType(dataType);
        sampleTypePropertyType.setPropertyType(propertyType);
        sampleProperty.setEntityTypePropertyType(sampleTypePropertyType);
        return sampleProperty;
    }

    private final SampleProperty[] createSampleProperties()
    {
        return new SampleProperty[]
            { createVarcharSampleProperty() };
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
        final List<EntityPropertyPE> properties =
                entityPropertiesConverter.convertProperties(SampleProperty.EMPTY_ARRAY,
                        SAMPLE_TYPE_CODE, ManagerTestTool.EXAMPLE_PERSON);
        assertEquals(0, properties.size());
        context.assertIsSatisfied();
    }

    @Test
    public final void testConvertProperties()
    {
        final IEntityPropertiesConverter entityPropertiesConverter =
                createEntityPropertiesConverter(EntityKind.SAMPLE);
        final PropertyTypePE propertyTypePE = new PropertyTypePE();
        propertyTypePE.setCode(VARCHAR_PROPERTY_TYPE_CODE);
        context.checking(new Expectations()
            {
                {
                    prepareForConvertion(this);

                    one(propertyTypeDAO).tryFindPropertyTypeByCode(VARCHAR_PROPERTY_TYPE_CODE);
                    will(returnValue(propertyTypePE));

                    one(propertyValueValidator).validatePropertyValue(propertyTypePE, "blue");
                }
            });
        final SampleProperty[] properties = createSampleProperties();
        final List<EntityPropertyPE> convertedProperties =
                entityPropertiesConverter.convertProperties(properties, SAMPLE_TYPE_CODE,
                        ManagerTestTool.EXAMPLE_PERSON);
        assertEquals(1, convertedProperties.size());
    }
}
