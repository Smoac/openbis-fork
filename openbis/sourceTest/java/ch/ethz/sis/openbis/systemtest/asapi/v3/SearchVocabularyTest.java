/*
 * Copyright 2018 ETH Zuerich, SIS
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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularySearchCriteria;

/**
 * @author Franz-Josef Elmer
 */
public class SearchVocabularyTest extends AbstractTest
{
    @Test
    public void testWithoutFetchingTerms()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        VocabularySearchCriteria criteria = new VocabularySearchCriteria();
        criteria.withCode().thatContains("GANI");

        // When
        List<Vocabulary> vocabularies =
                v3api.searchVocabularies(sessionToken, criteria, new VocabularyFetchOptions()).getObjects();

        // Then
        assertEquals(vocabularies.get(0).getCode(), "ORGANISM");
        assertEquals(vocabularies.get(0).getDescription(), "available-organism");
        assertEquals(vocabularies.get(0).isManagedInternally(), false);
        assertEquals(vocabularies.get(0).isInternalNameSpace(), false);
    }

    @Test
    public void testWithFetchingTerms()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        VocabularySearchCriteria criteria = new VocabularySearchCriteria();
        criteria.withCode().thatContains("GANI");
        VocabularyFetchOptions fetchOptions = new VocabularyFetchOptions();
        fetchOptions.withTerms();

        // When
        List<Vocabulary> vocabularies =
                v3api.searchVocabularies(sessionToken, criteria, fetchOptions).getObjects();

        // Then
        assertEquals(vocabularies.get(0).getCode(), "ORGANISM");
        assertEquals(vocabularies.get(0).getDescription(), "available-organism");
        assertEquals(vocabularies.get(0).getTerms().toString(),
                "[VocabularyTerm DOG, VocabularyTerm FLY, VocabularyTerm GORILLA, VocabularyTerm HUMAN, VocabularyTerm RAT]");
        assertEquals(vocabularies.size(), 1);
    }
}
