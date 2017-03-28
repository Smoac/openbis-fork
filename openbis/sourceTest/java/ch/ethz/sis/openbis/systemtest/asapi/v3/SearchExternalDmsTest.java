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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.fetchoptions.ExternalDmsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.ExternalDmsSearchCriteria;

public class SearchExternalDmsTest extends AbstractExternalDmsTest
{

    @Test
    public void searchReturnsAllExternalDataManagementSystems()
    {
        ExternalDms edms1 = get(create(externalDms()));
        ExternalDms edms2 = get(create(externalDms()));

        ExternalDmsSearchCriteria criteria = new ExternalDmsSearchCriteria();
        ExternalDmsFetchOptions fetchOptions = new ExternalDmsFetchOptions();
        SearchResult<ExternalDms> result = v3api.searchExternalDataManagementSystems(session, criteria, fetchOptions);
        assertThat(result.getObjects(), hasItem(isSimilarTo(edms1)));
        assertThat(result.getObjects(), hasItem(isSimilarTo(edms2)));
    }
    
    @Test
    public void searchReturnsSpecifiedExternalDataManagementSystem()
    {
        get(create(externalDms()));
        ExternalDms edms2 = get(create(externalDms()));
        
        ExternalDmsSearchCriteria criteria = new ExternalDmsSearchCriteria();
        criteria.withCode().thatEquals(edms2.getCode());
        ExternalDmsFetchOptions fetchOptions = new ExternalDmsFetchOptions();
        List<ExternalDms> result = v3api.searchExternalDataManagementSystems(session, criteria, fetchOptions).getObjects();
        
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getCode(), edms2.getCode());
        assertEquals(result.get(0).getAddress(), edms2.getAddress());
        assertEquals(result.get(0).getAddressType(), edms2.getAddressType());
    }
}
