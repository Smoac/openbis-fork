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
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProcedureTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedureTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.ProcedureTypeCode;

/**
 * Test cases for corresponding {@link ProcedureTypeDAO} class.
 * 
 * @author Christian Ribeaud
 */
@Test(groups =
    { "db", "procedureType" })
public final class ProcedureTypeDAOTest extends AbstractDAOTest
{

    private static final int NUMBER_OF_DATA_ACQUSITION_PROCEDRE_TYPES_IN_DATABASE = 1;

    static void checkProcedureType(final ProcedureTypePE foundType)
    {
        assertNotNull(foundType);
        assertNotNull(foundType.getId());
        assertNotNull(foundType.getCode());
        assertNotNull(foundType.getDescription());
    }

    @Test
    public final void testProcedureTypeCode()
    {
        final IProcedureTypeDAO procedureTypeDAO = daoFactory.getProcedureTypeDAO();
        for (final ProcedureTypeCode procedureType : ProcedureTypeCode.values())
        {
            final String procedureTypeCode = procedureType.getCode();
            if (procedureTypeDAO.tryFindProcedureTypeByCode(procedureTypeCode) == null)
            {
                fail(String.format("Given procedure type code '%s' does not exist in the database",
                        procedureTypeCode));
            }
        }
        for (final ProcedureTypePE procedureType : procedureTypeDAO.listProcedureTypes())
        {
            ProcedureTypeCode.getProcedureTypeCode(procedureType.getCode());
        }
    }

    @Test
    public final void testListProcedureTypes()
    {
        final IProcedureTypeDAO procedureTypeDAO = daoFactory.getProcedureTypeDAO();
        final List<ProcedureTypePE> procedureTypes = procedureTypeDAO.listProcedureTypes();
        assertTrue(procedureTypes.size() > 0);
        int countDataAcquisitionProcedure = 0;
        for (final ProcedureTypePE procType : procedureTypes)
        {
            if (procType.isDataAcquisition())
            {
                ++countDataAcquisitionProcedure;
            }
        }
        assertEquals(NUMBER_OF_DATA_ACQUSITION_PROCEDRE_TYPES_IN_DATABASE,
                countDataAcquisitionProcedure);
        // Change database instance id.
        changeDatabaseInstanceId(procedureTypeDAO);
        assertEquals(0, procedureTypeDAO.listProcedureTypes().size());
        resetDatabaseInstanceId(procedureTypeDAO);
    }

    @Test
    public final void testFindProcedureTypeByCode()
    {
        final IProcedureTypeDAO procedureTypeDAO = daoFactory.getProcedureTypeDAO();
        try
        {
            procedureTypeDAO.tryFindProcedureTypeByCode(null);
            fail("Given code could not be null");
        } catch (final AssertionError ex)
        {
            // Nothing to do here
        }
        final String procedureTypeCode = ProcedureTypeCode.DATA_ACQUISITION.getCode();
        final ProcedureTypePE foundType =
                procedureTypeDAO.tryFindProcedureTypeByCode(procedureTypeCode);
        checkProcedureType(foundType);
        assertTrue(foundType.isDataAcquisition());
        assertEquals(foundType.getCode(), procedureTypeCode);
        // Change database instance id.
        changeDatabaseInstanceId(procedureTypeDAO);
        assertNull(procedureTypeDAO.tryFindProcedureTypeByCode(procedureTypeCode));
        resetDatabaseInstanceId(procedureTypeDAO);
    }
}
