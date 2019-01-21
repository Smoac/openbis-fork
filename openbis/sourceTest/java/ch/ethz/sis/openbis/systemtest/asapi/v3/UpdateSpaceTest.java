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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
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
    private static final String PREFIX = "UST-";

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

    @Test
    public void testFreezeForProjects()
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final String spaceCode1 = "CISD";
        final String spaceCode2 = "TEST-SPACE";
        final ISpaceId spaceId1 = new SpacePermId(spaceCode1);
        final ISpaceId spaceId2 = new SpacePermId(spaceCode2);
        Map<ISpaceId, Space> map = v3api.getSpaces(sessionToken, Arrays.asList(spaceId1, spaceId2), new SpaceFetchOptions());
        Space space1 = map.get(spaceId1);
        Space space2 = map.get(spaceId2);
        assertEquals(space1.getCode(), spaceCode1);
        assertEquals(space2.getCode(), spaceCode2);

        final SpaceUpdate update1 = new SpaceUpdate();
        update1.setSpaceId(spaceId1);
        update1.freeze();
        final SpaceUpdate update2 = new SpaceUpdate();
        update2.setSpaceId(spaceId2);
        update2.freezeForProjects();

        // When
        v3api.updateSpaces(sessionToken, Arrays.asList(update1, update2));

        // Then
        map = v3api.getSpaces(sessionToken, Arrays.asList(spaceId1, spaceId2), new SpaceFetchOptions());
        space1 = map.get(spaceId1);
        space2 = map.get(spaceId2);
        assertEquals(space1.getCode(), spaceCode1);
        assertEquals(space1.isFrozen(), true);
        assertEquals(space1.isFrozenForProjects(), false);
        assertEquals(space1.isFrozenForSamples(), false);
        assertEquals(space2.getCode(), spaceCode2);
        assertEquals(space2.isFrozen(), true);
        assertEquals(space2.isFrozenForProjects(), true);
        assertEquals(space2.isFrozenForSamples(), false);
    }

    @Test
    public void testFreezeForSamples()
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final String spaceCode1 = "CISD";
        final String spaceCode2 = "TEST-SPACE";
        final ISpaceId spaceId1 = new SpacePermId(spaceCode1);
        final ISpaceId spaceId2 = new SpacePermId(spaceCode2);
        Map<ISpaceId, Space> map = v3api.getSpaces(sessionToken, Arrays.asList(spaceId1, spaceId2), new SpaceFetchOptions());
        Space space1 = map.get(spaceId1);
        Space space2 = map.get(spaceId2);
        assertEquals(space1.getCode(), spaceCode1);
        assertEquals(space2.getCode(), spaceCode2);

        final SpaceUpdate update1 = new SpaceUpdate();
        update1.setSpaceId(spaceId1);
        update1.freeze();
        final SpaceUpdate update2 = new SpaceUpdate();
        update2.setSpaceId(spaceId2);
        update2.freezeForSamples();

        // When
        v3api.updateSpaces(sessionToken, Arrays.asList(update1, update2));

        // Then
        map = v3api.getSpaces(sessionToken, Arrays.asList(spaceId1, spaceId2), new SpaceFetchOptions());
        space1 = map.get(spaceId1);
        space2 = map.get(spaceId2);
        assertEquals(space1.getCode(), spaceCode1);
        assertEquals(space1.isFrozen(), true);
        assertEquals(space1.isFrozenForProjects(), false);
        assertEquals(space1.isFrozenForSamples(), false);
        assertEquals(space2.getCode(), spaceCode2);
        assertEquals(space2.isFrozen(), true);
        assertEquals(space2.isFrozenForProjects(), false);
        assertEquals(space2.isFrozenForSamples(), true);
    }

    @Test
    public void testFreezing()
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final String spaceCode1 = "CISD";
        final ISpaceId spaceId1 = new SpacePermId(spaceCode1);
        final SpaceUpdate update1 = new SpaceUpdate();
        update1.setSpaceId(spaceId1);
        update1.freeze();
        v3api.updateSpaces(sessionToken, Arrays.asList(update1));
        SpaceUpdate update2 = new SpaceUpdate();
        update2.setSpaceId(spaceId1);
        update2.setDescription("new description");

        // When
        assertUserFailureException(Void -> v3api.updateSpaces(sessionToken, Arrays.asList(update2)),
                // Then
                "ERROR: Operation UPDATE is not allowed because space CISD is frozen.");
    }

    @Test
    public void testFreezingForProjects()
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SpacePermId spaceId = new SpacePermId("CISD");
        SpaceUpdate spaceUpdate = new SpaceUpdate();
        spaceUpdate.setSpaceId(spaceId);
        spaceUpdate.freezeForProjects();
        v3api.updateSpaces(sessionToken, Arrays.asList(spaceUpdate));
        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setCode(PREFIX + "P1");
        projectCreation.setSpaceId(spaceId);

        // When
        assertUserFailureException(Void -> v3api.createProjects(sessionToken, Arrays.asList(projectCreation)),
                "ERROR: Operation SET SPACE is not allowed because space CISD is frozen for project " 
        + projectCreation.getCode() + ".");
    }

    @Test
    public void testFreezingForSamples()
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SpacePermId spaceId = new SpacePermId("CISD");
        SpaceUpdate spaceUpdate = new SpaceUpdate();
        spaceUpdate.setSpaceId(spaceId);
        spaceUpdate.freezeForSamples();
        v3api.updateSpaces(sessionToken, Arrays.asList(spaceUpdate));
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setSpaceId(spaceId);
        sampleCreation.setTypeId(new EntityTypePermId("NORMAL", EntityKind.SAMPLE));
        sampleCreation.setCode(PREFIX + "S1");
        
        // When
        assertUserFailureException(Void -> v3api.createSamples(sessionToken, Arrays.asList(sampleCreation)),
                // Then
                "ERROR: Operation SET SPACE is not allowed because space CISD is frozen for sample UST-S1.");
    }
    
    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpaceUpdate update = new SpaceUpdate();
        update.setSpaceId(new SpacePermId("CISD"));

        SpaceUpdate update2 = new SpaceUpdate();
        update2.setSpaceId(new SpacePermId("TEST-SPACE"));

        v3api.updateSpaces(sessionToken, Arrays.asList(update, update2));

        assertAccessLog("update-spaces  SPACE_UPDATES('[SpaceUpdate[spaceId=CISD], SpaceUpdate[spaceId=TEST-SPACE]]')");
    }

}
