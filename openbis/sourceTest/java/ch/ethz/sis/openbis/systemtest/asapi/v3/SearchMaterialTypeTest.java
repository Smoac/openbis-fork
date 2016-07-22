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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.CodeComparator;

/**
 * @author Franz-Josef Elmer
 */
public class SearchMaterialTypeTest extends AbstractTest
{
    @Test
    public void testSearchAll()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        MaterialTypeSearchCriteria searchCriteria = new MaterialTypeSearchCriteria();
        MaterialTypeFetchOptions fetchOptions = new MaterialTypeFetchOptions();
        fetchOptions.withPropertyAssignments();
        SearchResult<MaterialType> searchResult = v3api.searchMaterialTypes(sessionToken, searchCriteria, fetchOptions);

        List<MaterialType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(),
                "[BACTERIUM, CELL_LINE, COMPOUND, CONTROL, DELETION_TEST, GENE, OTHER_REF, SELF_REF, SIRNA, SLOW_GENE, VIRUS]");
        assertEquals(types.get(0).getFetchOptions().hasPropertyAssignments(), true);
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchExactCodeWithVocabularies()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        MaterialTypeSearchCriteria searchCriteria = new MaterialTypeSearchCriteria();
        searchCriteria.withCode().thatEquals("BACTERIUM");
        MaterialTypeFetchOptions fetchOptions = new MaterialTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType().withVocabulary();
        SearchResult<MaterialType> searchResult = v3api.searchMaterialTypes(sessionToken, searchCriteria, fetchOptions);

        List<MaterialType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[BACTERIUM]");
        assertEquals(types.get(0).getFetchOptions().hasPropertyAssignments(), true);

        List<String> vocabularyCodes = new ArrayList<String>();
        for (MaterialType type : types)
        {
            vocabularyCodes.add(type.getCode() + ":" + extractVocabularyCodes(type.getPropertyAssignments()));
        }
        Collections.sort(vocabularyCodes);
        assertEquals(vocabularyCodes.toString(), "[BACTERIUM:[ORGANISM]]");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithCodeThatStartsWithB()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        MaterialTypeSearchCriteria searchCriteria = new MaterialTypeSearchCriteria();
        searchCriteria.withCode().thatStartsWith("B");
        MaterialTypeFetchOptions fetchOptions = new MaterialTypeFetchOptions();

        SearchResult<MaterialType> searchResult = v3api.searchMaterialTypes(sessionToken, searchCriteria, fetchOptions);

        List<MaterialType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[BACTERIUM]");
        assertEquals(types.get(0).getFetchOptions().hasPropertyAssignments(), false);
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithPropertyAssignmentSortByLabelDesc()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        MaterialTypeSearchCriteria searchCriteria = new MaterialTypeSearchCriteria();
        searchCriteria.withCode().thatStartsWith("B");
        MaterialTypeFetchOptions fetchOptions = new MaterialTypeFetchOptions();
        fetchOptions.withPropertyAssignments().sortBy().label().desc();

        SearchResult<MaterialType> searchResult = v3api.searchMaterialTypes(sessionToken, searchCriteria, fetchOptions);

        List<MaterialType> types = searchResult.getObjects();
        Collections.sort(types, new CodeComparator<MaterialType>());
        List<String> codes = extractCodes(types);
        assertEquals(codes.toString(), "[BACTERIUM]");
        assertEquals(types.get(0).getFetchOptions().hasPropertyAssignments(), true);
        List<PropertyAssignment> propertyAssignments = types.get(0).getPropertyAssignments();
        assertOrder(propertyAssignments, "ORGANISM", "DESCRIPTION");
        v3api.logout(sessionToken);
    }

}
