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
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.Space;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.SpaceCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class CreateSpaceTest extends AbstractTest
{

    @Test
    public void testCreateWithCodeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SpaceCreation space = new SpaceCreation();

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSpaces(sessionToken, Arrays.asList(space));
                }
            }, "Code cannot be empty");
    }

    @Test
    public void testCreateWithCodeExisting()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SpaceCreation space = new SpaceCreation();
        space.setCode("CISD");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSpaces(sessionToken, Arrays.asList(space));
                }
            }, "Space already exists in the database and needs to be unique");
    }

    @Test
    public void testCreateWithCodeIncorrect()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SpaceCreation space = new SpaceCreation();
        space.setCode("?!*");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSpaces(sessionToken, Arrays.asList(space));
                }
            }, "The code '?!*' contains illegal characters");
    }

    @Test
    public void testCreateWithInstanceAdmin()
    {
        testCreateWithUser(TEST_USER);
    }

    @Test
    public void testCreateWithSpaceAdmin()
    {
        testCreateWithUser(TEST_SPACE_USER);
    }

    @Test
    public void testCreateWithSpaceObserver()
    {
        assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testCreateWithUser(TEST_GROUP_OBSERVER);
                }
            });
    }

    private void testCreateWithUser(String userId)
    {
        final String sessionToken = v3api.login(userId, PASSWORD);

        SpaceCreation creation = new SpaceCreation();
        creation.setCode("NEW_SPACE");
        creation.setDescription("a new description");

        List<SpacePermId> permIds = v3api.createSpaces(sessionToken, Arrays.asList(creation));
        Map<ISpaceId, Space> map = v3api.mapSpaces(sessionToken, permIds, new SpaceFetchOptions());

        assertEquals(map.size(), 1);

        Space space = map.values().iterator().next();
        assertEquals(space.getCode(), creation.getCode());
        assertEquals(space.getDescription(), creation.getDescription());
    }

}
