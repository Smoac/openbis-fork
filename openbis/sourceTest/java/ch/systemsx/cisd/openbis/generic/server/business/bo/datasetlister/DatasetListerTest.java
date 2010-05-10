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

package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityDAO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityListingQueryTest;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { DatasetRecord.class })
@Test(groups =
    { "db", "dataset" })
public class DatasetListerTest extends AbstractDAOTest
{
    private IDatasetLister lister;

    private long sampleId;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        DatasetListerDAO datasetListerDAO =
                DatasetListingQueryTest.createDatasetListerDAO(daoFactory);
        SecondaryEntityDAO secondaryEntityDAO =
                SecondaryEntityListingQueryTest.createSecondaryEntityDAO(daoFactory);
        lister = DatasetLister.create(datasetListerDAO, secondaryEntityDAO, "url1", "url2");
        SamplePE sample =
                DatasetListingQueryTest.getSample("CISD", "CP-TEST-1", datasetListerDAO
                        .getDatabaseInstanceId(), daoFactory);
        sampleId = sample.getId();
    }

    @Test
    public void testListBySampleTechId()
    {
        List<ExternalData> datasets = lister.listBySampleTechId(new TechId(sampleId), true);
        assertEqualsOrGreater(1, datasets.size());
        ExternalData externalData = datasets.get(0);
        assertEquals(sampleId, externalData.getSample().getId().longValue());
        assertFalse(externalData.getProperties().isEmpty());
        AssertJUnit.assertNotNull(externalData.getExperiment());
    }
    
    @Test
    public void testListParents()
    {
        Map<Long, Set<Long>> map = lister.listParentIds(Arrays.<Long>asList(2L, 4L, 9L));
        System.out.println(map);
        
        assertEquals(null, map.get(2L));
        assertEquals("[2]", map.get(4L).toString());
        List<Long> list = new ArrayList<Long>(map.get(9L));
        Collections.sort(list);
        assertEquals("[2, 5, 6, 7]", list.toString());
    }

}
