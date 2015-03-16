/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.Space;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;

/**
 * @author pkupczyk
 */
public class MapSpaceTest extends AbstractTest
{

    @Test
    public void testMapByPermId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpacePermId permId1 = new SpacePermId("CISD");
        SpacePermId permId2 = new SpacePermId("/TEST-SPACE");

        Map<ISpaceId, Space> map =
                v3api.mapSpaces(sessionToken, Arrays.asList(permId1, permId2),
                        new SpaceFetchOptions());

        assertEquals(2, map.size());

        Iterator<Space> iter = map.values().iterator();
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

        SpacePermId permId1 = new SpacePermId("/CISD");
        SpacePermId permId2 = new SpacePermId("NONEXISTENT_SPACE");
        SpacePermId permId3 = new SpacePermId("TEST-SPACE");

        Map<ISpaceId, Space> map =
                v3api.mapSpaces(sessionToken,
                        Arrays.asList(permId1, permId2, permId3),
                        new SpaceFetchOptions());

        assertEquals(2, map.size());

        Iterator<Space> iter = map.values().iterator();
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

        SpacePermId permId1 = new SpacePermId("CISD");
        SpacePermId permId2 = new SpacePermId("/CISD");

        Map<ISpaceId, Space> map =
                v3api.mapSpaces(sessionToken, Arrays.asList(permId1, permId2), new SpaceFetchOptions());

        assertEquals(1, map.size());

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdsUnauthorized()
    {
        SpacePermId permId1 = new SpacePermId("CISD");
        SpacePermId permId2 = new SpacePermId("TEST-SPACE");

        List<? extends ISpaceId> ids = Arrays.asList(permId1, permId2);

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<ISpaceId, Space> map = v3api.mapSpaces(sessionToken, ids, new SpaceFetchOptions());

        assertEquals(map.size(), 2);
        v3api.logout(sessionToken);

        sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        map = v3api.mapSpaces(sessionToken, ids, new SpaceFetchOptions());

        assertEquals(map.size(), 1);

        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

}
