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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.SampleTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Test cases for corresponding {@link SampleTypeDAO} class.
 * 
 * @author Christian Ribeaud
 */
@Test(groups =
    { "db", "sampleType" })
@Friend(toClasses = SampleTypeDAO.class)
public final class SampleTypeDAOTest extends AbstractDAOTest
{
    static final String DEFAULT_SAMPLE_TYPE = SampleTypeCode.MASTER_PLATE.getCode();

    private BufferedAppender logRecorder;

    private Level previousLevel;

    static final void checkSampleType(final SampleTypePE type)
    {
        assertNotNull(type);
        assertNotNull(type.getId());
        assertNotNull(type.getCode());
        assertNotNull(type.getDescription());
    }

    //
    // AbstractDAOTest
    //

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        logRecorder = new BufferedAppender("%m%n", Level.DEBUG);
        final Logger sampleTypeLogger = SampleTypeDAO.operationLog;
        previousLevel = sampleTypeLogger.getLevel();
        assertNull(previousLevel);
        sampleTypeLogger.setLevel(Level.DEBUG);
    }

    @Override
    @AfterMethod
    public void tearDown()
    {
        super.tearDown();
        logRecorder.reset();
        final Logger sampleTypeLogger = SampleTypeDAO.operationLog;
        sampleTypeLogger.setLevel(previousLevel);
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
        for (final SampleTypePE sampleTypePE : samples)
        {
            assertTrue(HibernateUtils.isInitialized(sampleTypePE.getSampleTypePropertyTypes()));
            for (final SampleTypePropertyTypePE stpt : sampleTypePE.getSampleTypePropertyTypes())
            {
                final VocabularyPE vocabulary = stpt.getPropertyType().getVocabulary();
                if (vocabulary != null)
                {
                    assertTrue(HibernateUtils.isInitialized(vocabulary.getTerms()));
                }
            }
        }
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

    @Test
    public final void testTryFindByExample()
    {
        final ISampleTypeDAO sampleTypeDAO = daoFactory.getSampleTypeDAO();
        boolean fail = true;
        try
        {
            sampleTypeDAO.tryFindSampleTypeByExample(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        final SampleTypePE example = new SampleTypePE();
        example.setCode(DEFAULT_SAMPLE_TYPE);
        logRecorder.resetLogContent();
        final SampleTypePE sampleType = sampleTypeDAO.tryFindSampleTypeByExample(example);
        checkSampleType(sampleType);
        assertEquals(
                "SampleTypeDAO.tryFindSampleTypeByExample(SampleTypePE{"
                        + "code=MASTER_PLATE,description=<null>,databaseInstance=<null>,listable=<null>,"
                        + "containerHierarchyDepth=<null>,generatedFromHierarchyDepth=<null>}): Sample type "
                        + "'SampleTypePE{code=MASTER_PLATE,description=Master Plate,"
                        + "databaseInstance=DatabaseInstancePE{code=CISD},listable=true,"
                        + "containerHierarchyDepth=0,generatedFromHierarchyDepth=0}' found.",
                logRecorder.getLogContent());
    }
}
