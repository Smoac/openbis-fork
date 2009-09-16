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

import static ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils.assertRecursiveEqual;
import static ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils.createSet;
import static org.testng.AssertJUnit.assertEquals;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.sql.SQLException;

import net.lemnik.eodsql.DataIterator;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;

/**
 * Test cases for {@link IDatasetSetListingQuery}.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { DatasetRecord.class, IDatasetListingQuery.class })
@Test(groups =
    { "db", "dataset" })
public class DatasetSetListingQueryTest extends AbstractDAOTest
{

    // the interface which is tested here
    private IDatasetSetListingQuery setQuery;

    // used only to validate setQuery results
    private IDatasetListingQuery query;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        DatasetListerDAO dao = DatasetListingQueryTest.createDatasetListerDAO(daoFactory);
        setQuery = dao.getIdSetQuery();
        query = dao.getQuery();
    }

    @Test
    public void testDatasets()
    {
        LongSet ids = createSet(2, 4);
        Iterable<DatasetRecord> datasets = setQuery.getDatasets(ids);
        int count = 0;
        for (DatasetRecord dataset : datasets)
        {
            DatasetRecord sameDataset = query.getDataset(dataset.id);
            assertRecursiveEqual(dataset, sameDataset);
            count++;
        }
        assertEquals(ids.size(), count);
    }

    @Test
    public void testDatasetChildren()
    {
        int datasetId = 4;
        DataIterator<Long> children = setQuery.getDatasetChildrenIds(createSet(datasetId));
        AssertJUnit.assertFalse(children.hasNext());
    }
}
