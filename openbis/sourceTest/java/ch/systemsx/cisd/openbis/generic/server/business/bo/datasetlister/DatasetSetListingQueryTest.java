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

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

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

    private IDatasetSetListingQuery query;

    @BeforeClass(alwaysRun = true)
    public void init()
    {
        DatasetListerDAO dao = DatasetListerDAO.create(daoFactory);
        query = dao.getIdSetQuery();
    }

    @Test
    public void testDatasets()
    {
        // NOTE: test stub
        LongSet ids = getDatasetIds();
        query.getDatasets(ids);
    }

    @Test
    public void testDatasetParents()
    {
        // NOTE: test stub
        LongSet ids = getDatasetIds();
        query.getDatasetParents(ids);
    }

    private LongSet getDatasetIds()
    {
        // NOTE: get the real dataset ids!
        LongSet ids = new LongOpenHashSet();
        ids.add(1);
        ids.add(2);
        return ids;
    }
}
