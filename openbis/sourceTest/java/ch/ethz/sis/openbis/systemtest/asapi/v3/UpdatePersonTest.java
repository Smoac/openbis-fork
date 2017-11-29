/*
 * Copyright 2017 ETH Zuerich, SIS
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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.update.PersonUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class UpdatePersonTest extends AbstractTest
{
    @Test
    public void testUpdateUnaccesableHomeSpace()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PersonUpdate personUpdate = new PersonUpdate();
        PersonPermId personId = new PersonPermId("homeless");
        personUpdate.setPersonId(personId);
        personUpdate.setHomeSpaceId(new SpacePermId("TEST-SPACE"));
        
        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                // When
                v3api.updatePersons(sessionToken, Arrays.asList(personUpdate));
            }
            // Then
        }, "Can not set TEST-SPACE as home space for user 'homeless' because the user has no access rights.");
    }
    
    @Test
    public void testSpaceAdminUpdatesHomeSpaceOfAUserInSameSpace()
    {
        // Given
        String sessionToken = v3api.login(TEST_OBSERVER_CISD, PASSWORD);
        PersonUpdate personUpdate = new PersonUpdate();
        PersonPermId personId = new PersonPermId("homeless");
        personUpdate.setPersonId(personId);
        personUpdate.setHomeSpaceId(new SpacePermId("TESTGROUP"));
        
        // When
        v3api.updatePersons(sessionToken, Arrays.asList(personUpdate));
        
        // Then
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withSpace();
        Person person = v3api.getPersons(sessionToken, Arrays.asList(personId), fetchOptions).get(personId);
        assertEquals(person.getPermId().getPermId(), personId.getPermId());
        assertEquals(person.getSpace().getCode(), "TESTGROUP");
    }
    
    @Test
    public void testSpaceObserverUpdatesHomeSpaceOfAUserInSameSpace()
    {
        // Given
        String sessionToken = v3api.login(TEST_OBSERVER_CISD, PASSWORD);
        PersonUpdate personUpdate = new PersonUpdate();
        PersonPermId personId = new PersonPermId("homeless");
        personUpdate.setPersonId(personId);
        personUpdate.setHomeSpaceId(new SpacePermId("CISD"));
        
        assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    v3api.updatePersons(sessionToken, Arrays.asList(personUpdate));
                }
            });
    }
    
    @Test
    public void testSpaceObserverUpdatesItselfToSameHomeSpace()
    {
        // Given
        String sessionToken = v3api.login(TEST_GROUP_OBSERVER, PASSWORD);
        PersonUpdate personUpdate = new PersonUpdate();
        PersonPermId personId = new PersonPermId(TEST_GROUP_OBSERVER);
//        personUpdate.setPersonId(personId);
        personUpdate.setHomeSpaceId(new SpacePermId("TESTGROUP"));
        
        // When
        v3api.updatePersons(sessionToken, Arrays.asList(personUpdate));
        
        // Then
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withSpace();
        Person person = v3api.getPersons(sessionToken, Arrays.asList(personId), fetchOptions).get(personId);
        assertEquals(person.getPermId().getPermId(), personId.getPermId());
        assertEquals(person.getSpace().getCode(), "TESTGROUP");
    }
    
    @Test
    public void testDeactivateUser()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PersonUpdate personUpdate = new PersonUpdate();
        PersonPermId personId = new PersonPermId(TEST_GROUP_OBSERVER);
        personUpdate.setPersonId(personId);
        personUpdate.deactivate();
        
        // When
        v3api.updatePersons(sessionToken, Arrays.asList(personUpdate));
        
        // Then
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withSpace();
        Person person = v3api.getPersons(sessionToken, Arrays.asList(personId), fetchOptions).get(personId);
        assertEquals(person.getPermId().getPermId(), personId.getPermId());
        assertEquals(person.isActive(), Boolean.FALSE);
    }
    
    @Test(dataProvider = "usersNotAllowedToDeactivateUsers")
    public void testDeactivateUserWithUserCausingAuthorizationFailure(final String user)
    {
        assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // Given
                    String sessionToken = v3api.login(user, PASSWORD);
                    PersonUpdate personUpdate = new PersonUpdate();
                    personUpdate.setPersonId(new PersonPermId(TEST_GROUP_OBSERVER));
                    personUpdate.deactivate();
                    
                    // When
                    v3api.updatePersons(sessionToken, Arrays.asList(personUpdate));
                }
            });
    }

    @DataProvider
    Object[][] usersNotAllowedToDeactivateUsers()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER,
                TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD, TEST_SPACE_USER);
    }
}
