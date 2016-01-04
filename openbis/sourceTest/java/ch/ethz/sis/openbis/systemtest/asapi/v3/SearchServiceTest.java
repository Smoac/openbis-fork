/*
 * Copyright 2015 ETH Zuerich, SIS
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

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.Service;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.ServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.ServiceSearchCriteria;

/**
 * @author Franz-Josef Elmer
 */
public class SearchServiceTest extends AbstractTest
{

    @Test(enabled = false)
    public void testSearchServices()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SearchResult<Service> result = v3api.searchServices(sessionToken, new ServiceSearchCriteria(), new ServiceFetchOptions());

        assertEquals(result.getTotalCount(), 1);
    }
    
    @Test(enabled = false)
    public void testSearchAllServicesSortedPage2()
    {
        ServiceFetchOptions fetchOptions = new ServiceFetchOptions();
        fetchOptions.from(2).count(1).sortBy();
        SearchResult<Service> result = v3api.searchServices(systemSessionToken, new ServiceSearchCriteria(), 
                fetchOptions);
        
        assertEquals(result.getObjects().toString(), "[Service code: service3]");
        assertEquals(result.getTotalCount(), 4);
    }
    
    @Test(enabled = false)
    public void testSearchServiceByCode()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ServiceSearchCriteria searchCriteria = new ServiceSearchCriteria();
        searchCriteria.withCode().thatStartsWith("simple");
        
        SearchResult<Service> result = v3api.searchServices(sessionToken, searchCriteria, new ServiceFetchOptions());
        
        assertEquals(result.getObjects().toString(), "[Service code: simple-service]");
        assertEquals(result.getTotalCount(), 1);
    }
    
}