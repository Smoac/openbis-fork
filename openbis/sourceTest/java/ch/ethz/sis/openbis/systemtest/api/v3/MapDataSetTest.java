/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.systemtest.api.v3;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.IDataSetId;

/**
 * @author pkupczyk
 */
public class MapDataSetTest extends AbstractDataSetTest
{

    @Test
    public void testMapByPermId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId1 = new DataSetPermId("20081105092159111-1");
        DataSetPermId permId2 = new DataSetPermId("20110509092359990-10");

        Map<IDataSetId, DataSet> map =
                v3api.mapDataSets(sessionToken, Arrays.asList(permId1, permId2),
                        new DataSetFetchOptions());

        assertEquals(map.size(), 2);

        Iterator<DataSet> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId2);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdsNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId1 = new DataSetPermId("20081105092159111-1");
        DataSetPermId permId2 = new DataSetPermId("NONEXISTENT");
        DataSetPermId permId3 = new DataSetPermId("20110509092359990-10");

        Map<IDataSetId, DataSet> map = v3api.mapDataSets(sessionToken, Arrays.asList(permId1, permId2, permId3), new DataSetFetchOptions());

        assertEquals(map.size(), 2);

        Iterator<DataSet> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId3);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId3).getPermId(), permId3);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdsDuplicated()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId1 = new DataSetPermId("20081105092159111-1");
        DataSetPermId permId2 = new DataSetPermId("20081105092159111-1");
        DataSetPermId permId3 = new DataSetPermId("20110509092359990-10");

        Map<IDataSetId, DataSet> map = v3api.mapDataSets(sessionToken, Arrays.asList(permId1, permId2, permId3), new DataSetFetchOptions());

        assertEquals(map.size(), 2);

        Iterator<DataSet> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId3);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId3).getPermId(), permId3);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdsUnauthorized()
    {
        DataSetPermId permId1 = new DataSetPermId("20081105092159111-1");
        DataSetPermId permId2 = new DataSetPermId("20120619092259000-22");

        List<? extends IDataSetId> ids = Arrays.asList(permId1, permId2);

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IDataSetId, DataSet> map = v3api.mapDataSets(sessionToken, ids, new DataSetFetchOptions());

        assertEquals(map.size(), 2);
        v3api.logout(sessionToken);

        sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        map = v3api.mapDataSets(sessionToken, ids, new DataSetFetchOptions());

        assertEquals(map.size(), 1);

        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

}
