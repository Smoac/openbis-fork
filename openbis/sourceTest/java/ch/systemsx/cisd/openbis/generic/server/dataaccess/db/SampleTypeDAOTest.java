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

import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.SampleTypeCode;

/**
 * Test cases for corresponding {@link SampleTypeDAO} class.
 * 
 * @author Christian Ribeaud
 */
@Test(groups =
    { "db", "sampleType" })
public final class SampleTypeDAOTest extends AbstractDAOTest
{

    static final String DEFAULT_SAMPLE_TYPE = SampleTypeCode.MASTER_PLATE.getCode();

    static final void checkSampleType(final SampleTypePE type)
    {
        assertNotNull(type);
        assertNotNull(type.getId());
        assertNotNull(type.getCode());
        assertNotNull(type.getDescription());
    }

    @Test
    public final void testSampleTypeCode()
    {
        final ISampleTypeDAO sampleDAO = daoFactory.getSampleTypeDAO();
        for (final SampleTypeCode sampleType : SampleTypeCode.values())
        {
            final String sampleTypeCode = sampleType.getCode();
            if (sampleDAO.tryFindSampleTypeByCode(sampleTypeCode) == null)
            {
                fail(String.format("Given sample type code '%s' does not exist in the database",
                        sampleTypeCode));
            }
        }
    }

    @Test
    public final void testListAllSampleTypes()
    {
        final ISampleTypeDAO sampleTypeDAO = daoFactory.getSampleTypeDAO();
        final List<SampleTypePE> samples = sampleTypeDAO.listSampleTypes(false);
        assert samples.size() > 0;
        // Change database instance id.
        changeDatabaseInstanceId(sampleTypeDAO);
        assertEquals(0, sampleTypeDAO.listSampleTypes(false).size());
        resetDatabaseInstanceId(sampleTypeDAO);
    }

    @Test
    public final void testListListableSampleTypes()
    {
        final ISampleTypeDAO sampleTypeDAO = daoFactory.getSampleTypeDAO();
        final List<SampleTypePE> samples = sampleTypeDAO.listSampleTypes(true);
        assert samples.size() > 0;
    }

    @Test
    public final void testFindSampleTypeByCode()
    {
        final ISampleTypeDAO sampleTypeDAO = daoFactory.getSampleTypeDAO();
        SampleTypePE sampleType;
        boolean exceptionThrown = false;
        try
        {
            sampleType = sampleTypeDAO.tryFindSampleTypeByCode(null);
        } catch (final AssertionError ex)
        {
            exceptionThrown = true;
        }
        assertTrue("Sample code can not be null.", exceptionThrown);
        sampleType = sampleTypeDAO.tryFindSampleTypeByCode("doesNotExist");
        assertNull(sampleType);
        checkSampleType(sampleTypeDAO.tryFindSampleTypeByCode(DEFAULT_SAMPLE_TYPE));
        // Change database instance id.
        changeDatabaseInstanceId(sampleTypeDAO);
        assertNull(sampleTypeDAO.tryFindSampleTypeByCode(DEFAULT_SAMPLE_TYPE));
        resetDatabaseInstanceId(sampleTypeDAO);
    }
}
