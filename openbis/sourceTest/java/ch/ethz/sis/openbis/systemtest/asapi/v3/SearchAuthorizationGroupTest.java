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

import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.search.AuthorizationGroupSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SearchAuthorizationGroupTest extends AbstractTest
{
    @Test
    public void testSearchAllFetchingOnlyBasic()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        AuthorizationGroupSearchCriteria searchCriteria = new AuthorizationGroupSearchCriteria();
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        
        // When
        SearchResult<AuthorizationGroup> result = v3api.searchAuthorizationGroups(sessionToken, searchCriteria, fetchOptions);
        
        // Then
        
        AuthorizationGroup authorizationGroup = result.getObjects().get(0);
        assertEquals(authorizationGroup.getCode(), "AGROUP");
        assertEquals(authorizationGroup.getDescription(), "myDescription");
        assertEquals(authorizationGroup.getFetchOptions().hasRegistrator(), false);
        assertEquals(authorizationGroup.getFetchOptions().hasUsers(), false);
        assertEquals(result.getTotalCount(), 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithCodeFetchingRegistratorAndUsers()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        AuthorizationGroupSearchCriteria searchCriteria = new AuthorizationGroupSearchCriteria();
        searchCriteria.withCode().thatStartsWith("A");
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withRegistrator();
        fetchOptions.withUsers().withSpace();

        // When
        SearchResult<AuthorizationGroup> result = v3api.searchAuthorizationGroups(sessionToken, searchCriteria, fetchOptions);
        
        // Then
        
        AuthorizationGroup authorizationGroup = result.getObjects().get(0);
        assertEquals(authorizationGroup.getCode(), "AGROUP");
        assertEquals(authorizationGroup.getDescription(), "myDescription");
        assertEquals(authorizationGroup.getFetchOptions().hasRegistrator(), true);
        assertEquals(authorizationGroup.getRegistrator().getUserId(), "test");
        assertEquals(authorizationGroup.getFetchOptions().hasUsers(), true);
        List<Person> users = authorizationGroup.getUsers();
        assertEquals(users.get(0).getUserId(), "test");
        assertEquals(users.get(0).getEmail(), "franz-josef.elmer@systemsx.ch");
        assertEquals(users.get(0).getSpace().getCode(), "CISD");
        assertEquals(users.size(), 1);
        assertEquals(result.getTotalCount(), 1);
        
        v3api.logout(sessionToken);
    }
    
    @Test
    public void testSearchWithPermIdFetchingRegistrator()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        AuthorizationGroupSearchCriteria searchCriteria = new AuthorizationGroupSearchCriteria();
        searchCriteria.withPermId().thatContains("G");
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withRegistrator();
        
        // When
        SearchResult<AuthorizationGroup> result = v3api.searchAuthorizationGroups(sessionToken, searchCriteria, fetchOptions);
        
        // Then
        
        AuthorizationGroup authorizationGroup = result.getObjects().get(0);
        assertEquals(authorizationGroup.getCode(), "AGROUP");
        assertEquals(authorizationGroup.getDescription(), "myDescription");
        assertEquals(authorizationGroup.getFetchOptions().hasRegistrator(), true);
        assertEquals(authorizationGroup.getRegistrator().getUserId(), "test");
        assertEquals(authorizationGroup.getFetchOptions().hasUsers(), false);
        assertEquals(result.getTotalCount(), 1);
        assertEquals(result.getObjects().size(), 1);
        
        v3api.logout(sessionToken);
    }
    
    @Test
    public void testSearchWithNonExistingCode()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        AuthorizationGroupSearchCriteria searchCriteria = new AuthorizationGroupSearchCriteria();
        searchCriteria.withCode().thatStartsWith("B");
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();

        // When
        SearchResult<AuthorizationGroup> result = v3api.searchAuthorizationGroups(sessionToken, searchCriteria, fetchOptions);
        
        // Then
        
        assertEquals(result.getTotalCount(), 0);
        assertEquals(result.getObjects().size(), 0);
        
        v3api.logout(sessionToken);
    }
}
