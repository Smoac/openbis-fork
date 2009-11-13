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

import static org.testng.AssertJUnit.assertNotNull;
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
 */
public class SampleListingWorkerTest extends AbstractDAOTest
{

    private final String BASE_INDEX_URL = "baseIndexURL";

    private SampleListerDAO sampleListerDAO;

    private SecondaryEntityDAO secondaryDAO;

    @BeforeMethod
    public void beforeMethod()
    {
        sampleListerDAO = SampleListingQueryTest.createSampleListerDAO(daoFactory);
        secondaryDAO = SecondaryEntityDAO.create(daoFactory);
    }

    @Test
    public void testListSamplesByIdGroupNotNull()
    {
        final LongSet sampleIds = new LongOpenHashSet();
        sampleIds.addAll(Arrays.asList(1L, 2L, 3L));
        final ListOrSearchSampleCriteria criteria = new ListOrSearchSampleCriteria(sampleIds);
        final SampleListingWorker worker =
                SampleListingWorker.create(criteria, BASE_INDEX_URL, sampleListerDAO, secondaryDAO);
        final List<Sample> list = worker.load();
        assertTrue(list.size() > 0);
        for (Sample s : list)
        {
            assertNotNull("ID:" + s.getId(), s.getGroup());
            assertNotNull("ID:" + s.getId(), s.getGroup().getInstance());
        }
    }

    @Test
    public void testListSamplesForContainerGroupNotNull()
    {
        final ListSampleCriteria baseCriteria =
                ListSampleCriteria.createForContainer(new TechId(997L));
        final ListOrSearchSampleCriteria criteria = new ListOrSearchSampleCriteria(baseCriteria);
        final SampleListingWorker worker =
                SampleListingWorker.create(criteria, BASE_INDEX_URL, sampleListerDAO, secondaryDAO);
        final List<Sample> list = worker.load();
        assertTrue(list.size() > 0);
        for (Sample s : list)
        {
            assertNotNull("ID:" + s.getId(), s.getGroup());
            assertNotNull("ID:" + s.getId(), s.getGroup().getInstance());
        }
    }

    @Test
    public void testListSamplesForExperimentGroupNotNull()
    {
        final ListSampleCriteria baseCriteria =
                ListSampleCriteria.createForExperiment(new TechId(2L));
        final ListOrSearchSampleCriteria criteria = new ListOrSearchSampleCriteria(baseCriteria);
        final SampleListingWorker worker =
                SampleListingWorker.create(criteria, BASE_INDEX_URL, sampleListerDAO, secondaryDAO);
        final List<Sample> list = worker.load();
        assertTrue(list.size() > 0);
        for (Sample s : list)
        {
            assertNotNull("ID:" + s.getId(), s.getGroup());
            assertNotNull("ID:" + s.getId(), s.getGroup().getInstance());
        }
    }
}
