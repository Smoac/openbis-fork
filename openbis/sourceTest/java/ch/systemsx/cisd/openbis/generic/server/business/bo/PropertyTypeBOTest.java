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

import org.jmock.Expectations;
import org.springframework.dao.DataIntegrityViolationException;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityDataType;

/**
 * Test cases for corresponding {@link PropertyTypeBO} class.
 * 
 * @author Christian Ribeaud
 */
public final class PropertyTypeBOTest extends AbstractBOTest

{
    private static final String DATA_TYPE_DESCRIPTION = "A string data type.";

    private static final String PROPERTY_TYPE_LABEL = "Color";

    private static final String PROPERTY_TYPE_DESCRIPTION = "A color.";

    private static final String PROPERTY_TYPE_CODE = "USER.COLOR";

    private final PropertyTypeBO createPropertyTypeBO()
    {
        return new PropertyTypeBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION);
    }

    static final PropertyType createPropertyType(final DataTypeCode dataTypeCode)
    {
        final PropertyType propertyType = new PropertyType();
        propertyType.setCode(PROPERTY_TYPE_CODE);
        propertyType.setDescription(PROPERTY_TYPE_DESCRIPTION);
        propertyType.setDataType(createDataType(dataTypeCode));
        propertyType.setLabel(PROPERTY_TYPE_LABEL);
        return propertyType;
    }

    final static DataType createDataType(final DataTypeCode dataTypeCode)
    {
        final DataType dataType = new DataType();
        dataType.setCode(dataTypeCode);
        dataType.setDescription(DATA_TYPE_DESCRIPTION);
        return dataType;
    }

    final static void assertPropertyTypeEquals(final PropertyType propertyType,
            final PropertyTypePE propertyTypePE)
    {
        assertEquals(propertyType.getCode(), propertyTypePE.getCode());
        assertEquals(propertyType.getLabel(), propertyTypePE.getLabel());
        assertEquals(propertyType.getDescription(), propertyTypePE.getDescription());
        assertEquals(propertyType.getDescription(), propertyTypePE.getDescription());
        assertEquals(propertyType.getDataType().getCode().name(), propertyTypePE.getType()
                .getCode().name());
    }

    @Test
    public final void testWithNull()
    {
        boolean fail = true;
        try
        {
            createPropertyTypeBO().define((PropertyType) null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        fail = true;
        try
        {
            createPropertyTypeBO().save();
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @Test
    public final void testDefineAndSave()
    {
        final DataTypePE dataTypePE = new DataTypePE();
        dataTypePE.setCode(EntityDataType.VARCHAR);
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getHomeDatabaseInstance();
                    will(returnValue(ManagerTestTool.EXAMPLE_DATABASE_INSTANCE));

                    allowing(daoFactory).getPropertyTypeDAO();
                    will(returnValue(propertyTypeDAO));

                    one(propertyTypeDAO).getDataTypeByCode(EntityDataType.VARCHAR);
                    will(returnValue(dataTypePE));

                    one(propertyTypeDAO).createPropertyType(with(aNonNull(PropertyTypePE.class)));
                }
            });
        final PropertyTypeBO propertyTypeBO = createPropertyTypeBO();
        final PropertyType propertyType = createPropertyType(DataTypeCode.VARCHAR);
        propertyTypeBO.define(propertyType);
        final PropertyTypePE propertyTypePE = propertyTypeBO.getPropertyType();
        assertPropertyTypeEquals(propertyType, propertyTypePE);
        propertyTypeBO.save();
        context.assertIsSatisfied();
    }

    @Test
    public final void testDefineAndSaveWithException()
    {
        final DataTypePE dataTypePE = new DataTypePE();
        dataTypePE.setCode(EntityDataType.VARCHAR);
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getHomeDatabaseInstance();
                    will(returnValue(ManagerTestTool.EXAMPLE_DATABASE_INSTANCE));

                    allowing(daoFactory).getPropertyTypeDAO();
                    will(returnValue(propertyTypeDAO));

                    one(propertyTypeDAO).getDataTypeByCode(EntityDataType.VARCHAR);
                    will(returnValue(dataTypePE));

                    one(propertyTypeDAO).createPropertyType(with(aNonNull(PropertyTypePE.class)));
                    will(throwException(new DataIntegrityViolationException(null)));
                }
            });
        final PropertyTypeBO propertyTypeBO = createPropertyTypeBO();
        final PropertyType propertyType = createPropertyType(DataTypeCode.VARCHAR);
        propertyTypeBO.define(propertyType);
        final PropertyTypePE propertyTypePE = propertyTypeBO.getPropertyType();
        assertPropertyTypeEquals(propertyType, propertyTypePE);
        try
        {
            propertyTypeBO.save();
            fail(String.format("'%s' expected.", UserFailureException.class.getSimpleName()));
        } catch (final UserFailureException ex)
        {
            // Nothing to do here.
        }
        context.assertIsSatisfied();
    }

    @Test
    public final void testDefineWithExistingVocabulary()
    {
        final DataTypePE dataTypePE = new DataTypePE();
        dataTypePE.setCode(EntityDataType.CONTROLLEDVOCABULARY);
        final Vocabulary vocabulary = VocabularyBOTest.createVocabulary();
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getHomeDatabaseInstance();
                    will(returnValue(ManagerTestTool.EXAMPLE_DATABASE_INSTANCE));

                    one(daoFactory).getPropertyTypeDAO();
                    will(returnValue(propertyTypeDAO));

                    one(propertyTypeDAO).getDataTypeByCode(EntityDataType.CONTROLLEDVOCABULARY);
                    will(returnValue(dataTypePE));

                    one(vocabularyDAO).tryFindVocabularyByCode(vocabulary.getCode());
                    will(returnValue(new VocabularyPE()));
                }
            });
        final PropertyTypeBO propertyTypeBO = createPropertyTypeBO();
        final PropertyType propertyType = createPropertyType(DataTypeCode.CONTROLLEDVOCABULARY);
        propertyType.setVocabulary(vocabulary);
        propertyTypeBO.define(propertyType);
        final PropertyTypePE propertyTypePE = propertyTypeBO.getPropertyType();
        assertPropertyTypeEquals(propertyType, propertyTypePE);
        context.assertIsSatisfied();
    }

    @Test
    public final void testDefineWithNewVocabulary()
    {
        final DataTypePE dataTypePE = new DataTypePE();
        dataTypePE.setCode(EntityDataType.CONTROLLEDVOCABULARY);
        final Vocabulary vocabulary = VocabularyBOTest.createVocabulary();
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getHomeDatabaseInstance();
                    will(returnValue(ManagerTestTool.EXAMPLE_DATABASE_INSTANCE));

                    one(daoFactory).getPropertyTypeDAO();
                    will(returnValue(propertyTypeDAO));

                    one(propertyTypeDAO).getDataTypeByCode(EntityDataType.CONTROLLEDVOCABULARY);
                    will(returnValue(dataTypePE));

                    allowing(daoFactory).getVocabularyDAO();
                    will(returnValue(vocabularyDAO));

                    one(vocabularyDAO).tryFindVocabularyByCode(vocabulary.getCode());

                    one(vocabularyDAO).createVocabulary(with(aNonNull(VocabularyPE.class)));
                }
            });
        final PropertyTypeBO propertyTypeBO = createPropertyTypeBO();
        final PropertyType propertyType = createPropertyType(DataTypeCode.CONTROLLEDVOCABULARY);
        propertyType.setVocabulary(vocabulary);
        propertyTypeBO.define(propertyType);
        final PropertyTypePE propertyTypePE = propertyTypeBO.getPropertyType();
        assertPropertyTypeEquals(propertyType, propertyTypePE);
        context.assertIsSatisfied();
    }
}
