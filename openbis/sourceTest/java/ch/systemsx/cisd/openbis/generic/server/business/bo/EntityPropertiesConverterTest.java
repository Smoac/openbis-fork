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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityDataType;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityPropertyValue;

/**
 * Test cases for corresponding {@link EntityPropertiesConverter} class.
 * 
 * @author Tomasz Pylak
 */
public class EntityPropertiesConverterTest
{
    private static final int MATERIAL_TYPE_ID = 985;

    private static final String MATERIAL_TYPE_CODE = "materialTypeCode";

    private static final String REAL_PROP = "realProp".toUpperCase();

    private static final String STRING_PROP = "stringProp".toUpperCase();

    private static final String INT_PROP = "intProp".toUpperCase();

    private static final String DATE_PROP = "dateProp".toUpperCase();

    private static final String VOCABULARY_PROP = "vocabularyProp".toUpperCase();

    private static final String VOCABULARY_CODE = "vocabularyCode".toUpperCase();

    private Mockery context;

    private IDAOFactory daoFactory;

    private IEntityPropertyTypeDAO entityPropertyTypeDAO;

    private IEntityTypeDAO entityTypeDAO;

    private IPropertyTypeDAO propertyTypeDAO;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        daoFactory = context.mock(IDAOFactory.class);
        entityPropertyTypeDAO = context.mock(IEntityPropertyTypeDAO.class);
        entityTypeDAO = context.mock(IEntityTypeDAO.class);
        propertyTypeDAO = context.mock(IPropertyTypeDAO.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    private EntityPropertiesConverter createEntityPropertiesConverter()
    {
        return new EntityPropertiesConverter(EntityKind.MATERIAL, daoFactory);
    }

    private static class TestPropertyDescriptor extends AbstractHashable
    {
        private final SimpleEntityProperty simpleProperty;

        private final long entityPropertyTypeId;

        private final String vocabularyCodeOrNull;

        private TestPropertyDescriptor(final SimpleEntityProperty simpleProperty,
                final long entityPropertyTypeId, final String vocabularyCodeOrNull)
        {
            this.simpleProperty = simpleProperty;
            this.entityPropertyTypeId = entityPropertyTypeId;
            this.vocabularyCodeOrNull = vocabularyCodeOrNull;
        }

        public TestPropertyDescriptor(final SimpleEntityProperty simpleProperty,
                final long entityPropertyTypeId)
        {
            this(simpleProperty, entityPropertyTypeId, null);
        }
    }

    private static TestPropertyDescriptor createIntPropertyDesc(final String name)
    {
        final SimpleEntityProperty prop =
                new SimpleEntityProperty(name, name, EntityDataType.INTEGER, 123);
        return new TestPropertyDescriptor(prop, 1);
    }

    private static TestPropertyDescriptor createRealPropertyDesc(final String name)
    {
        final SimpleEntityProperty prop =
                new SimpleEntityProperty(name, name, EntityDataType.REAL, 123.32);
        return new TestPropertyDescriptor(prop, 2);
    }

    private static TestPropertyDescriptor createStringPropertyDesc(final String name)
    {
        final SimpleEntityProperty prop =
                new SimpleEntityProperty(name, name, EntityDataType.VARCHAR, "ala");
        return new TestPropertyDescriptor(prop, 4);
    }

    private static TestPropertyDescriptor createDatePropertyDesc(final String name)
    {
        final SimpleEntityProperty prop =
                new SimpleEntityProperty(name, name, EntityDataType.TIMESTAMP, new Date(0));
        return new TestPropertyDescriptor(prop, 5);
    }

    private static TestPropertyDescriptor createVocabularyPropertyDesc(final String name)
    {
        final String vocabularyTermCode = "FROG";
        final SimpleEntityProperty prop =
                new SimpleEntityProperty(name, name, EntityDataType.CONTROLLEDVOCABULARY,
                        vocabularyTermCode);
        return new TestPropertyDescriptor(prop, 3, VOCABULARY_CODE);
    }

    private static Set<TestPropertyDescriptor> createAllPropertyTypeDescriptors()
    {
        final Set<TestPropertyDescriptor> set = new HashSet<TestPropertyDescriptor>();
        set.add(createRealPropertyDesc(REAL_PROP));
        set.add(createIntPropertyDesc(INT_PROP));
        set.add(createStringPropertyDesc(STRING_PROP));
        set.add(createDatePropertyDesc(DATE_PROP));
        set.add(createVocabularyPropertyDesc(VOCABULARY_PROP));
        return set;
    }

    private final static List<EntityTypePropertyTypePE> convert(
            final Set<TestPropertyDescriptor> descs)
    {
        final List<EntityTypePropertyTypePE> result = new ArrayList<EntityTypePropertyTypePE>();
        for (final TestPropertyDescriptor desc : descs)
        {
            result.add(convert(desc));
        }
        return result;
    }

    private final static EntityTypePropertyTypePE convert(final TestPropertyDescriptor desc)
    {
        final EntityDataType dataType = desc.simpleProperty.getDataType();
        final boolean isMandatory = false;
        final String propertyCode = desc.simpleProperty.getCode();
        final PropertyTypePE propertyType = new PropertyTypePE();
        propertyType.setCode(propertyCode);
        final DataTypePE dataTypePE = new DataTypePE();
        dataTypePE.setCode(dataType);
        propertyType.setLabel(desc.simpleProperty.getLabel());
        propertyType.setDescription("description");
        if (desc.vocabularyCodeOrNull != null)
        {
            final VocabularyPE vocabularyPE = new VocabularyPE();
            vocabularyPE.setCode(desc.vocabularyCodeOrNull);
            final VocabularyTermPE vocabularyTerm = new VocabularyTermPE();
            vocabularyTerm.setCode("FROG");
            vocabularyTerm.setId(42L);
            vocabularyPE.setTerms(Collections.singletonList(vocabularyTerm));
            propertyType.setVocabulary(vocabularyPE);
        }
        propertyType.setType(dataTypePE);
        final EntityTypePropertyTypePE etpt = new SampleTypePropertyTypePE();
        etpt.setId(desc.entityPropertyTypeId);
        etpt.setMandatory(isMandatory);
        etpt.setPropertyType(propertyType);
        return etpt;
    }

    @DataProvider(name = "dbProperties")
    Object[][] provideDbProperties()
    {
        final Set<TestPropertyDescriptor> descs = createAllPropertyTypeDescriptors();
        final Object[][] result = new Object[descs.size()][];
        final Iterator<TestPropertyDescriptor> iterator = descs.iterator();
        int i = 0;
        while (iterator.hasNext())
        {
            result[i] = new Object[]
                { iterator.next() };
            i++;
        }
        return result;
    }

    private void prepareGetDAOForConvertion(final Expectations exp)
    {
        exp.one(daoFactory).getEntityPropertyTypeDAO(EntityKind.MATERIAL);
        exp.will(Expectations.returnValue(entityPropertyTypeDAO));

        exp.one(daoFactory).getEntityTypeDAO(EntityKind.MATERIAL);
        exp.will(Expectations.returnValue(entityTypeDAO));

        exp.allowing(daoFactory).getPropertyTypeDAO();
        exp.will(Expectations.returnValue(propertyTypeDAO));

        exp.one(entityTypeDAO).listEntityTypes();
        exp.will(Expectations.returnValue(Collections.singletonList(createMaterialType(
                MATERIAL_TYPE_ID, MATERIAL_TYPE_CODE))));

    }

    private final static MaterialTypePE createMaterialType(final long materialTypeId,
            final String materialTypeCode)
    {
        final MaterialTypePE materialTypePE = new MaterialTypePE();
        materialTypePE.setId(materialTypeId);
        materialTypePE.setCode(materialTypeCode);
        materialTypePE.setDescription("");
        DatabaseInstancePE databaseInstancePE = new DatabaseInstancePE();
        databaseInstancePE.setCode("db");
        materialTypePE.setDatabaseInstance(databaseInstancePE);
        return materialTypePE;
    }

    @Test(dataProvider = "dbProperties")
    public void testConvertPropertiesFromMaterial(final TestPropertyDescriptor propertyDesc)
    {
        final long materialTypeId = MATERIAL_TYPE_ID;
        final PersonPE personPE = ManagerTestTool.EXAMPLE_PERSON;
        final String materialTypeCode = MATERIAL_TYPE_CODE;
        final Set<TestPropertyDescriptor> allSchemaDescs = createAllPropertyTypeDescriptors();
        final NewMaterial material = createMaterial(materialTypeCode, allSchemaDescs);
        final MaterialTypePE materialType = createMaterialType(materialTypeId, materialTypeCode);

        context.checking(new Expectations()
            {
                {
                    prepareGetDAOForConvertion(this);

                    for (final TestPropertyDescriptor property : allSchemaDescs)
                    {
                        one(propertyTypeDAO).tryFindPropertyTypeByCode(
                                property.simpleProperty.getCode());
                        will(returnValue(convert(property).getPropertyType()));
                    }

                    one(entityPropertyTypeDAO).listEntityPropertyTypes(materialType);
                    will(returnValue(convert(allSchemaDescs)));
                }
            });

        final EntityPropertiesConverter entityPropertiesConverter =
                createEntityPropertiesConverter();

        // check if cache is working by calling the conversion several times
        for (int i = 0; i < 3; i++)
        {
            final SimpleEntityProperty[] props = material.getProperties();
            final String typeCode = material.getEntityType().getCode();
            final List<MaterialPropertyPE> properties =
                    entityPropertiesConverter.convertProperties(props, typeCode, personPE);
            assertPropertiesEqual(properties, allSchemaDescs);
        }
        context.assertIsSatisfied();
    }

    @SuppressWarnings("cast")
    private void assertPropertiesEqual(final List<MaterialPropertyPE> properties,
            final Set<TestPropertyDescriptor> allSchemaDescs)
    {
        AssertJUnit.assertEquals(allSchemaDescs.size(), properties.size());
        for (int i = 0; i < properties.size(); i++)
        {
            final EntityPropertyPE entityPropertyValueDTO = properties.iterator().next();
            boolean matched = false;
            for (final TestPropertyDescriptor propertyDescriptor : allSchemaDescs)
            {
                final String expectedValue = tryGetUntypedValue(propertyDescriptor.simpleProperty);
                final String convertedValue = entityPropertyValueDTO.tryGetUntypedValue();
                final boolean firstCondition = equals(expectedValue, convertedValue);
                final boolean secondCondition =
                        equals((Long) propertyDescriptor.entityPropertyTypeId,
                                entityPropertyValueDTO.getEntityTypePropertyType().getId());
                if (firstCondition && secondCondition)
                {
                    matched = true;
                }
            }
            AssertJUnit.assertTrue(matched);
        }
    }

    boolean equals(final Object expected, final Object actual)
    {
        if (expected == null && actual == null)
        {
            return true;
        }
        if (expected != null && expected.equals(actual))
        {
            return true;
        }
        return false;
    }

    private static String tryGetUntypedValue(final SimpleEntityProperty simpleProperty)
    {
        return EntityPropertyValue.createFromSimple(simpleProperty).tryGetUntypedValue();
    }

    private static NewMaterial createMaterial(final String materialTypeCode,
            final Set<TestPropertyDescriptor> allSchemaDescs)
    {
        final EntityType materialType = new EntityType(materialTypeCode, "");
        final NewMaterial material = new NewMaterial("materialCode", materialType);
        material.setProperties(convertToSimpleProperties(allSchemaDescs));
        return material;
    }

    private static SimpleEntityProperty[] convertToSimpleProperties(
            final Set<TestPropertyDescriptor> allSchemaDescs)
    {
        final SimpleEntityProperty[] res = new SimpleEntityProperty[allSchemaDescs.size()];
        final Iterator<TestPropertyDescriptor> iterator = allSchemaDescs.iterator();
        int i = 0;
        while (iterator.hasNext())
        {
            res[i] = iterator.next().simpleProperty;
            i++;
        }
        return res;
    }

    @DataProvider(name = "WrongTypeValues")
    public Object[][] provideWrongTypeValues()
    {
        return new Object[][]
            {
                { EntityDataType.INTEGER, "ala" },
                { EntityDataType.INTEGER, "123.32" },
                { EntityDataType.REAL, "xxx" } };
    }

}
