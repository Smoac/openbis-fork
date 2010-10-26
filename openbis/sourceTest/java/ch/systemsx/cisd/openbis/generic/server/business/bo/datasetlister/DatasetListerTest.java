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
import static org.testng.AssertJUnit.assertSame;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;

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
        lister = DatasetLister.create(datasetListerDAO, secondaryEntityDAO, "url");
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
        Map<Long, Set<Long>> map = lister.listParentIds(Arrays.<Long> asList(2L, 4L, 9L));
        System.out.println(map);

        assertEquals(null, map.get(2L));
        assertEquals("[2]", map.get(4L).toString());
        List<Long> list = new ArrayList<Long>(map.get(9L));
        Collections.sort(list);
        assertEquals("[2, 5, 6, 7]", list.toString());
    }

    @Test
    public void testListAllDataSetsFor()
    {
        HashSet<String> samplePermIDs =
                new HashSet<String>(Arrays.asList("200811050946559-983", "200902091219327-1025"));
        List<SamplePE> samplePEs = daoFactory.getSampleDAO().listByPermID(samplePermIDs);
        List<Sample> samples = SampleTranslator.translate(samplePEs, "");
        
        Map<Sample, List<ExternalData>> dataSets = lister.listAllDataSetsFor(samples);
        
        StringBuilder builder = new StringBuilder();
        for (Sample sample : samples)
        {
            builder.append(sample.getCode());
            appendChildren(builder, dataSets.get(sample), "   ");
            builder.append('\n');
        }
        assertEquals("3VCP1\n   20081105092158673-1 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "     20081105092159188-3 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "     20081105092259000-9 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "       20081105092259900-0 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "         20081105092359990-2 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "       20081105092259900-1 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "         20081105092359990-2 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "CP-TEST-1\n"
                + "   20081105092159111-1 (HCS_IMAGE) [ANY_MATERIAL: 1000_C (SIRNA), "
                + "BACTERIUM: BACTERIUM1 (BACTERIUM), COMMENT: no comment, GENDER: FEMALE]\n"
                + "     20081105092259000-9 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "       20081105092259900-0 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "         20081105092359990-2 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "       20081105092259900-1 (HCS_IMAGE) [COMMENT: no comment]\n"
                + "         20081105092359990-2 (HCS_IMAGE) [COMMENT: no comment]\n",
                builder.toString());
        Map<String, ExternalData> dataSetsByCode = new HashMap<String, ExternalData>();
        for (Sample sample : samples)
        {
            List<ExternalData> rootDataSets = dataSets.get(sample);
            assertSameDataSetsForSameCode(dataSetsByCode, rootDataSets);
        }
    }

    private void assertSameDataSetsForSameCode(Map<String, ExternalData> dataSetsByCode,
            List<ExternalData> dataSets)
    {
        if (dataSets == null || dataSets.isEmpty())
        {
            return;
        }
        for (ExternalData dataSet : dataSets)
        {
            ExternalData previousDataSet = dataSetsByCode.put(dataSet.getCode(), dataSet);
            if (previousDataSet != null)
            {
                assertSame("Same data set object expected for " + dataSet.getCode(), previousDataSet,
                        dataSet);
            }
            List<ExternalData> children = dataSet.getChildren();
            assertSameDataSetsForSameCode(dataSetsByCode, children);
        }
    }

    private void appendChildren(StringBuilder builder, List<ExternalData> dataSets,
            String indentation)
    {
        if (dataSets.isEmpty() == false)
        {
            for (ExternalData dataSet : dataSets)
            {
                builder.append('\n').append(indentation).append(dataSet.getCode()).append(" (");
                builder.append(dataSet.getDataSetType().getCode()).append(") ");
                builder.append(getSortedProperties(dataSet));
                List<ExternalData> children = dataSet.getChildren();
                if (children != null && children.isEmpty() == false)
                {
                    appendChildren(builder, children, indentation + "  ");
                }
            }
        }
    }
}
