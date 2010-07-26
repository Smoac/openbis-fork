/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * Test cases for the {@link SampleListingWorker}.
 * 
 * @author Bernd Rinn
 * @author Piotr Buczek
 */
public class SampleListingWorkerTest extends AbstractDAOTest
{

    private final String BASE_INDEX_URL = "baseIndexURL";

    private final String SPACE_CODE = "CISD";

    private final long CONTAINER_ID = 997L;

    private final long PARENT_ID = 979L;

    private final Long[] CHILDREN_IDS =
        { 984L, 985L, 986L, 987L, 988L, 989L };

    private SampleListerDAO sampleListerDAO;

    private SecondaryEntityDAO secondaryDAO;

    @BeforeMethod
    public void beforeMethod()
    {
        sampleListerDAO = SampleListingQueryTest.createSampleListerDAO(daoFactory);
        secondaryDAO = SecondaryEntityDAO.create(daoFactory);
    }

    @Test
    public void testListSamplesBySpace()
    {
        final ListSampleCriteria baseCriteria = new ListSampleCriteria();
        baseCriteria.setSpaceCode(SPACE_CODE);
        baseCriteria.setIncludeSpace(true);
        final ListOrSearchSampleCriteria criteria = new ListOrSearchSampleCriteria(baseCriteria);
        final SampleListingWorker worker =
                SampleListingWorker.create(criteria, BASE_INDEX_URL, sampleListerDAO, secondaryDAO);
        final List<Sample> list = worker.load();
        assertTrue(list.size() > 0);
        for (Sample s : list)
        {
            checkSpace(s);
            if (Arrays.binarySearch(CHILDREN_IDS, s.getId()) >= 0)
            {
                checkGeneratedFrom(s);
            }
        }
    }

    @Test
    public void testListSamplesById()
    {
        final LongSet sampleIds = new LongOpenHashSet();
        sampleIds.addAll(Arrays.asList(CHILDREN_IDS));
        final ListOrSearchSampleCriteria criteria = new ListOrSearchSampleCriteria(sampleIds);
        final SampleListingWorker worker =
                SampleListingWorker.create(criteria, BASE_INDEX_URL, sampleListerDAO, secondaryDAO);
        final List<Sample> list = worker.load();
        assertEquals(CHILDREN_IDS.length, list.size());
        for (Sample s : list)
        {
            assertTrue(Arrays.binarySearch(CHILDREN_IDS, s.getId()) >= 0);
            checkSpace(s);
            checkGeneratedFrom(s);
        }
    }

    @Test
    public void testListSamplesForContainer()
    {
        final ListSampleCriteria baseCriteria =
                ListSampleCriteria.createForContainer(new TechId(CONTAINER_ID));
        final ListOrSearchSampleCriteria criteria = new ListOrSearchSampleCriteria(baseCriteria);
        final SampleListingWorker worker =
                SampleListingWorker.create(criteria, BASE_INDEX_URL, sampleListerDAO, secondaryDAO);
        final List<Sample> list = worker.load();
        assertTrue(list.size() > 0);
        for (Sample s : list)
        {
            checkSpace(s);
            assertNotNull("ID:" + s.getId(), s.getContainer());
            assertEquals("ID:" + s.getId(), CONTAINER_ID, s.getContainer().getId().longValue());
        }
    }

    @Test
    public void testListSamplesForParent()
    {
        final ListSampleCriteria baseCriteria =
                ListSampleCriteria.createForParent(new TechId(PARENT_ID));
        final ListOrSearchSampleCriteria criteria = new ListOrSearchSampleCriteria(baseCriteria);
        final SampleListingWorker worker =
                SampleListingWorker.create(criteria, BASE_INDEX_URL, sampleListerDAO, secondaryDAO);
        final List<Sample> list = worker.load();
        assertEquals(CHILDREN_IDS.length, list.size());
        for (Sample s : list)
        {
            assertTrue(Arrays.binarySearch(CHILDREN_IDS, s.getId()) >= 0);
            checkSpace(s);
            checkGeneratedFrom(s);
        }
    }

    @Test
    public void testListSamplesForExperiment()
    {
        final long expId = 2L;
        final ListSampleCriteria baseCriteria =
                ListSampleCriteria.createForExperiment(new TechId(expId));
        final ListOrSearchSampleCriteria criteria = new ListOrSearchSampleCriteria(baseCriteria);
        final SampleListingWorker worker =
                SampleListingWorker.create(criteria, BASE_INDEX_URL, sampleListerDAO, secondaryDAO);
        final List<Sample> list = worker.load();
        assertTrue(list.size() > 0);
        for (Sample s : list)
        {
            checkSpace(s);
            assertNotNull("ID:" + s.getId(), s.getExperiment());
            assertEquals("ID:" + s.getId(), expId, s.getExperiment().getId().longValue());
        }
    }

    private void checkSpace(Sample s)
    {
        assertNotNull("ID:" + s.getId(), s.getSpace());
        assertNotNull("ID:" + s.getId(), s.getSpace().getInstance());
        assertEquals("ID:" + s.getId(), SPACE_CODE, s.getSpace().getCode());
    }

    private void checkGeneratedFrom(Sample s)
    {
        assertNotNull("ID:" + s.getId(), s.getGeneratedFrom());
        assertEquals("ID:" + s.getId(), PARENT_ID, s.getGeneratedFrom().getId().longValue());
        assertNotNull("ID:" + s.getId(), s.getGeneratedFrom().getGeneratedFrom());
        assertNull("ID:" + s.getId(), s.getGeneratedFrom().getGeneratedFrom().getGeneratedFrom());
    }
}
