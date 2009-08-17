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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertTrue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ISampleListingQuery.CoVoSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ISampleListingQuery.GenericSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ISampleListingQuery.SampleRowVO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;

/**
 * Test cases for {@link ISampleSetListingQuery}.
 * 
 * @author Bernd Rinn
 */
@Test(groups =
    { "db", "sample" })
public class SampleSetListingQueryTest extends AbstractDAOTest
{

    private static final String SAMPLE_TYPE_CODE_MASTER_PLATE = "MASTER_PLATE";

    private static final String SAMPLE_TYPE_CODE_CELL_PLATE = "CELL_PLATE";

    private long dbInstanceId;

    private DatabaseInstancePE dbInstance;

    private GroupPE group;

    private LongSet masterPlateIds;

    private LongSet cellPlateIds;

    private ISampleSetListingQuery query;

    @BeforeClass(alwaysRun = true)
    public void init()
    {
        dbInstanceId = daoFactory.getSampleListerDAO().getDatabaseInstanceId();
        dbInstance = daoFactory.getDatabaseInstanceDAO().getByTechId(new TechId(dbInstanceId));
        group = daoFactory.getGroupDAO().listGroups().get(0);
        final SampleTypePE masterPlateType =
                daoFactory.getSampleTypeDAO()
                        .tryFindSampleTypeByCode(SAMPLE_TYPE_CODE_MASTER_PLATE);
        final List<SamplePE> masterPlates =
                daoFactory.getSampleDAO().listSamplesWithPropertiesByTypeAndDatabaseInstance(
                        masterPlateType, dbInstance);
        masterPlateIds = new LongOpenHashSet(masterPlates.size());
        for (SamplePE sample : masterPlates)
        {
            masterPlateIds.add(sample.getId());
        }
        final SampleTypePE cellPlateType =
                daoFactory.getSampleTypeDAO().tryFindSampleTypeByCode(SAMPLE_TYPE_CODE_CELL_PLATE);
        final List<SamplePE> cellPlates =
                daoFactory.getSampleDAO().listSamplesWithPropertiesByTypeAndGroup(cellPlateType, group);
        cellPlateIds = new LongOpenHashSet(cellPlates.size());
        for (SamplePE sample : cellPlates)
        {
            cellPlateIds.add(sample.getId());
        }
        query = daoFactory.getSampleListerDAO().getIdSetQuery();
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testQuerySamples()
    {
        int sampleCount = 0;
        for (@SuppressWarnings("unused")
        SampleRowVO sample : query.getSamples(masterPlateIds))
        {
            ++sampleCount;
        }
        assertTrue(sampleCount > 0);
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testSamplePropertyGenericValues()
    {
        int sampleCount = 0;
        for (@SuppressWarnings("unused")
        GenericSamplePropertyVO sampleProperty : query
                .getSamplePropertyGenericValues(cellPlateIds))
        {
            ++sampleCount;
        }
        assertTrue(sampleCount > 0);
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testSamplePropertyMaterialValues()
    {
        query.getSamplePropertyMaterialValues(masterPlateIds);
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testSamplePropertyVocabularyTermValues()
    {
        int sampleCount = 0;
        for (@SuppressWarnings("unused")
        CoVoSamplePropertyVO sampleProperty : query
                .getSamplePropertyVocabularyTermValues(masterPlateIds))
        {
            ++sampleCount;
        }
        assertTrue(sampleCount > 0);
    }

}
