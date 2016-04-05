/*
 * Copyright 2016 ETH Zuerich, SIS
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

import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.search.EntityTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.CodeComparator;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SearchSampleTypeTest extends AbstractTest
{
    @Test
    public void testSearchWithCodeThatStartsWithD()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        EntityTypeSearchCriteria searchCriteria = new EntityTypeSearchCriteria();
        searchCriteria.withCode().thatStartsWith("D");
        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        
        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);
        
        List<SampleType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[DELETION_TEST, DILUTION_PLATE, DYNAMIC_PLATE]");
        assertEquals(types.get(0).getFetchOptions().hasPropertyAssignments(), false);
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithPropertyAssignmentSortByLabelDesc()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        EntityTypeSearchCriteria searchCriteria = new EntityTypeSearchCriteria();
        searchCriteria.withCode().thatStartsWith("D");
        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withPropertyAssignments().sortBy().label().desc();
        
        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);
        
        List<SampleType> types = searchResult.getObjects();
        Collections.sort(types, new CodeComparator<SampleType>());
        List<String> codes = extractCodes(types);
        assertEquals(codes.toString(), "[DELETION_TEST, DILUTION_PLATE, DYNAMIC_PLATE]");
        assertEquals(types.get(0).getFetchOptions().hasPropertyAssignments(), true);
        List<PropertyAssignment> propertyAssignments = types.get(0).getPropertyAssignments();
        assertOrder(propertyAssignments, "BACTERIUM", "ORGANISM", "DESCRIPTION");
        v3api.logout(sessionToken);
    }
    
}
