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

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwner;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.SampleTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Test cases for corresponding {@link SampleDAO} class.
 * 
 * @author Tomasz Pylak
 */
@Test(groups =
    { "db", "sample" })
public final class SampleDAOTest extends AbstractDAOTest
{

    @Test
    public final void testListGroupSamples()
    {
        final SamplePE sample = createGroupSample();
        final List<SamplePE> samples =
                daoFactory.getSampleDAO().listSamplesByTypeAndGroup(sample.getSampleType(),
                        sample.getGroup());
        assertEquals(1, samples.size());
        assertEquals(sample, samples.get(0));
    }

    @Test
    public final void testListSamplesFetchRelations()
    {
        final SampleTypePE type1 = getSampleType(SampleTypeCode.MASTER_PLATE);
        final SampleTypePE type2 = getSampleType(SampleTypeCode.DILUTION_PLATE);
        final SampleTypePE type3 = getSampleType(SampleTypeCode.CELL_PLATE);
        type3.setContainerHierarchyDepth(1);
        type3.setGeneratedFromHierarchyDepth(1);
        final SamplePE sampleA = createSample(type1, "grandParent", null);
        final SamplePE sampleB = createSample(type2, "parent", sampleA);
        SamplePE sampleC = createSample(type3, "child", sampleB);
        save(sampleA, sampleB, sampleC);
        final SamplePE well = createSample(type3, "well", null);
        final SamplePE container = createSample(type2, "container", null);
        final SamplePE superContainer = createSample(type2, "superContainer", null);
        well.setContainer(container);
        container.setContainer(superContainer);
        save(superContainer, container, well); // clear session to avoid using samples from first
        // level cache
        sessionFactory.getCurrentSession().clear();
        final List<SamplePE> samples = listSamplesFromHomeDatabase(type3);
        final SamplePE foundWell = findSample(well, samples);
        assertTrue(HibernateUtils.isInitialized(foundWell.getContainer()));
        final SamplePE foundContainer = foundWell.getContainer();
        assertFalse(HibernateUtils.isInitialized(foundContainer.getContainer()));
        sampleC = findSample(sampleC, samples);
        assertTrue(HibernateUtils.isInitialized(sampleC.getGeneratedFrom()));
        final SamplePE parent = sampleC.getGeneratedFrom();
        assertFalse(HibernateUtils.isInitialized(parent.getGeneratedFrom()));
    }

    @Test
    public final void testTryFindByCodeAndDatabaseInstance()
    {
        final SampleTypePE sampleType = getSampleType(SampleTypeCode.MASTER_PLATE);
        final List<SamplePE> samples = listSamplesFromHomeDatabase(sampleType);
        final DatabaseInstancePE homeDatabaseInstance = daoFactory.getHomeDatabaseInstance();
        final SamplePE sample = samples.get(0);
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        boolean fail = true;
        try
        {
            sampleDAO.tryFindByCodeAndDatabaseInstance(null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        assertEquals(sample, sampleDAO.tryFindByCodeAndDatabaseInstance(sample.getCode(),
                homeDatabaseInstance));
        assertNull(sampleDAO.tryFindByCodeAndDatabaseInstance("", homeDatabaseInstance));
    }

    @Test
    public final void testTryFindByCodeAndGroup()
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        final SamplePE sample = createGroupSample();
        boolean fail = true;
        try
        {
            sampleDAO.tryFindByCodeAndGroup(null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        assertEquals(sample, sampleDAO.tryFindByCodeAndGroup(sample.getCode(), sample.getGroup()));
        assertNull(sampleDAO.tryFindByCodeAndGroup("", sample.getGroup()));
    }

    @Test
    public final void testListSamplesByContainer()
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        try
        {
            sampleDAO.listSamplesByContainer(null);
            fail("AssertionError expected");
        } catch (final AssertionError e)
        {
            assertEquals("Unspecified container.", e.getMessage());
        }
        final String masterPlateCode = "MP";
        DatabaseInstancePE homeInstance = daoFactory.getHomeDatabaseInstance();
        final SamplePE sample =
                sampleDAO.tryFindByCodeAndDatabaseInstance(masterPlateCode, homeInstance);
        assertNotNull(sample);
        final List<SamplePE> samples = sampleDAO.listSamplesByContainer(sample);
        assertEquals(320, samples.size());
    }

    //
    // Private methods
    //

    private final List<SamplePE> listSamplesFromHomeDatabase(final SampleTypePE sampleType)
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        return sampleDAO.listSamplesByTypeAndDatabaseInstance(sampleType, daoFactory
                .getHomeDatabaseInstance());
    }

    private final SamplePE findSample(final SamplePE sample, final List<SamplePE> samples)
    {
        final int sampleIx = samples.indexOf(sample);
        assert sampleIx != -1 : "sample not found " + sample;
        return samples.get(sampleIx);
    }

    private final void save(final SamplePE... samples)
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        for (final SamplePE samplePE : samples)
        {
            sampleDAO.createSample(samplePE);
        }
    }

    private final SampleTypePE getSampleType(final SampleTypeCode sampleTypeCode)
    {
        final SampleTypePE sampleType =
                daoFactory.getSampleTypeDAO().tryFindSampleTypeByCode(sampleTypeCode.getCode());
        assert sampleType != null;
        return sampleType;
    }

    private final SamplePE createGroupSample()
    {
        final SampleTypePE sampleType = getSampleType(SampleTypeCode.MASTER_PLATE);
        final GroupPE group = createGroup("xxx");
        daoFactory.getGroupDAO().createGroup(group);
        final SamplePE sample =
                createSample(sampleType, "code", null, SampleOwner.createGroup(group));
        save(sample);
        assertNotNull(sample);
        assertNotNull(sample.getSampleType());
        assertNotNull(sample.getGroup());
        return sample;
    }

    private final SamplePE createSample(final SampleTypePE type, final String code,
            final SamplePE generatorOrNull)
    {
        final SampleOwner owner =
                SampleOwner.createDatabaseInstance(daoFactory.getHomeDatabaseInstance());
        return createSample(type, code, generatorOrNull, owner);
    }

    private final SamplePE createSample(final SampleTypePE type, final String code,
            final SamplePE generatorOrNull, final SampleOwner sampleOwner)
    {
        final SamplePE sample = new SamplePE();
        sample.setRegistrator(getSystemPerson());
        sample.setCode(code);
        sample.setSampleType(type);
        sample.setDatabaseInstance(sampleOwner.tryGetDatabaseInstance());
        sample.setGroup(sampleOwner.tryGetGroup());
        sample.setGeneratedFrom(generatorOrNull);
        return sample;
    }
}
