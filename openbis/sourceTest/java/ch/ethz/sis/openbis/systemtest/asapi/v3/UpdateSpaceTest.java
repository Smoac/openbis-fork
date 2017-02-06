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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.SpaceUpdate;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class UpdateSpaceTest extends AbstractTest
{

    @Test
    public void testUpdateWithSpaceUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISpaceId spaceId = new SpacePermId("CISD");
        final SpaceUpdate update = new SpaceUpdate();
        update.setSpaceId(spaceId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSpaces(sessionToken, Arrays.asList(update));
                }
            }, spaceId);
    }

    @Test
    public void testUpdateSpaceWithAdminUserInAnotherSpace()
    {
        final String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);
        final SpacePermId spaceId = new SpacePermId("TEST-SPACE");

        final SpaceUpdate update = new SpaceUpdate();
        update.setSpaceId(spaceId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSpaces(sessionToken, Arrays.asList(update));
                }
            }, spaceId);
    }

    @Test
    public void testUpdateWithSpaceNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISpaceId spaceId = new SpacePermId("IDONTEXIST");
        final SpaceUpdate update = new SpaceUpdate();
        update.setSpaceId(spaceId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSpaces(sessionToken, Arrays.asList(update));
                }
            }, spaceId);
    }

    @Test
    public void testUpdate()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final String spaceCode1 = "CISD";
        final String spaceCode2 = "TEST-SPACE";

        final ISpaceId spaceId1 = new SpacePermId(spaceCode1);
        final ISpaceId spaceId2 = new SpacePermId(spaceCode2);

        Map<ISpaceId, Space> map = v3api.getSpaces(sessionToken, Arrays.asList(spaceId1, spaceId2), new SpaceFetchOptions());

        Space space1 = map.get(spaceId1);
        Space space2 = map.get(spaceId2);

        assertEquals(space1.getCode(), spaceCode1);
        assertEquals(space1.getDescription(), null);
        assertEquals(space2.getCode(), spaceCode2);
        assertEquals(space2.getDescription(), "myDescription");

        final SpaceUpdate update1 = new SpaceUpdate();
        update1.setSpaceId(spaceId1);
        update1.setDescription("a new description 1");

        final SpaceUpdate update2 = new SpaceUpdate();
        update2.setSpaceId(spaceId2);
        update2.setDescription("a new description 2");

        v3api.updateSpaces(sessionToken, Arrays.asList(update1, update2));
        map = v3api.getSpaces(sessionToken, Arrays.asList(spaceId1, spaceId2), new SpaceFetchOptions());

        space1 = map.get(spaceId1);
        space2 = map.get(spaceId2);

        assertEquals(space1.getCode(), spaceCode1);
        assertEquals(space1.getDescription(), update1.getDescription().getValue());
        assertEquals(space2.getCode(), spaceCode2);
        assertEquals(space2.getDescription(), update2.getDescription().getValue());
    }
}
