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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityDataType;

/**
 * Test cases for corresponding {@link PropertyTypeDAO} class.
 * 
 * @author Christian Ribeaud
 */
@Test(groups =
    { "db", "propertyType" })
public final class PropertyTypeDAOTest extends AbstractDAOTest
{

    static final void checkDataType(final DataTypePE dataTypeDTO, final boolean checkDescription)
    {
        assertNotNull(dataTypeDTO.getId());
        assertNotNull(dataTypeDTO.getCode());
        if (checkDescription)
        {
            assertNotNull(dataTypeDTO.getDescription());
        }
    }

    final static void checkPropertyType(final PropertyTypePE propertyTypeDTO)
    {
        assertNotNull(propertyTypeDTO.getId());
        assertNotNull(propertyTypeDTO.getCode());
        assertNotNull(propertyTypeDTO.getDescription());
        assertNotNull(propertyTypeDTO.getLabel());
        checkDataType(propertyTypeDTO.getType(), false);
    }

    private final PropertyTypePE createPropertyType(final DataTypePE dataType, final String code,
            final VocabularyPE vocabularyOrNull)
    {
        final PropertyTypePE propertyTypePE = new PropertyTypePE();
        propertyTypePE.setCode(code);
        propertyTypePE.setLabel(code);
        propertyTypePE.setDescription(code);
        propertyTypePE.setRegistrator(getSystemPerson());
        propertyTypePE.setType(dataType);
        propertyTypePE.setDatabaseInstance(daoFactory.getHomeDatabaseInstance());
        if (EntityDataType.CONTROLLEDVOCABULARY.equals(dataType.getCode()))
        {
            assertNotNull(vocabularyOrNull);
            propertyTypePE.setVocabulary(vocabularyOrNull);
        }
        return propertyTypePE;
    }

    @Test
    public final void testListPropertyTypes()
    {
        final IPropertyTypeDAO propertyTypeDAO = daoFactory.getPropertyTypeDAO();
        final List<PropertyTypePE> list = propertyTypeDAO.listPropertyTypes();
        for (final PropertyTypePE propertyTypeDTO : list)
        {
            checkPropertyType(propertyTypeDTO);
            assertFalse(CodeConverter.isInternalNamespace(propertyTypeDTO.getCode()));
        }
        assertTrue(list.size() > 0);
        // Change database instance id.
        changeDatabaseInstanceId(propertyTypeDAO);
        assertEquals(0, propertyTypeDAO.listPropertyTypes().size());
        resetDatabaseInstanceId(propertyTypeDAO);
    }

    @Test
    public final void testListDataTypesAndEnumeration()
    {
        final List<DataTypePE> list = daoFactory.getPropertyTypeDAO().listDataTypes();
        assertEquals(EntityDataType.values().length, list.size());
        assertEquals(DataTypeCode.values().length, list.size());
        for (final DataTypePE dataType : list)
        {
            checkDataType(dataType, true);
        }
    }

    @Test
    public final void testTryFindDataTypeByCode()
    {
        final IPropertyTypeDAO propertyTypeDAO = daoFactory.getPropertyTypeDAO();
        boolean fail = true;
        try
        {
            propertyTypeDAO.getDataTypeByCode(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        assertNotNull(propertyTypeDAO.getDataTypeByCode(EntityDataType.INTEGER));
    }

    @SuppressWarnings("unused")
    @DataProvider
    private final Object[][] getEntityDataType()
    {
        return new Object[][]
            {
                { EntityDataType.BOOLEAN },
                { EntityDataType.INTEGER },
                { EntityDataType.REAL },
                { EntityDataType.TIMESTAMP },
                { EntityDataType.VARCHAR }, };
    }

    @Test(dataProvider = "getEntityDataType")
    public final void testCreatePropertyType(final EntityDataType entityDataType)
    {
        final IPropertyTypeDAO propertyTypeDAO = daoFactory.getPropertyTypeDAO();
        boolean fail = true;
        try
        {
            propertyTypeDAO.createPropertyType(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        try
        {
            propertyTypeDAO.createPropertyType(createPropertyType(propertyTypeDAO
                    .getDataTypeByCode(entityDataType), "code", null));
            fail(String.format("'%s' expected.", DataIntegrityViolationException.class
                    .getSimpleName()));
        } catch (final DataIntegrityViolationException ex)
        {
            // Nothing to do here.
        }
        propertyTypeDAO.createPropertyType(createPropertyType(propertyTypeDAO
                .getDataTypeByCode(entityDataType), "user.code", null));
        assertNotNull(propertyTypeDAO.tryFindPropertyTypeByCode("user.code"));
        assertNull(propertyTypeDAO.tryFindPropertyTypeByCode("code"));
    }
}