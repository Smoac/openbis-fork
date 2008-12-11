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
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
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
        for (final DataTypePE dataType : list)
        {
            checkDataType(dataType, true);
        }
    }

}