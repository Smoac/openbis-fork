/*
 * Copyright 2016 ETH Zuerich, CISD
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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;

/**
 * @author pkupczyk
 */
@Test(groups = { "before remote api" })
public class SearchVocabularyTermTest extends AbstractVocabularyTermTest
{

    @Test
    public void testSearchWithEmptyFetchOptions()
    {
        VocabularyTermFetchOptions fetchOptions = new VocabularyTermFetchOptions();
        VocabularyTerm term = searchTerm(new VocabularyTermPermId("PROPRIETARY", "$STORAGE_FORMAT"), fetchOptions);

        assertEquals(term.getPermId(), new VocabularyTermPermId("PROPRIETARY", "$STORAGE_FORMAT"));
        assertEquals(term.getCode(), "PROPRIETARY");
        assertEquals(term.getDescription(), "proprietary description");
        assertEquals(term.getLabel(), "proprietary label");
        assertEquals(term.getOrdinal(), Long.valueOf(1));
        assertEquals(term.isOfficial(), Boolean.TRUE);
        assertEqualsDate(term.getModificationDate(), "2008-11-05 09:18:00");
        assertEqualsDate(term.getRegistrationDate(), "2008-11-05 09:18:00");
        assertRegistratorNotFetched(term);
        assertVocabularyNotFetched(term);
    }

    @Test
    public void testSearchWithVocabularyFetched()
    {
        VocabularyTermFetchOptions fetchOptions = new VocabularyTermFetchOptions();
        fetchOptions.withVocabulary().withRegistrator();

        VocabularyTerm term = searchTerm(new VocabularyTermPermId("PROPRIETARY", "$STORAGE_FORMAT"), fetchOptions);

        assertEquals(term.getPermId(), new VocabularyTermPermId("PROPRIETARY", "$STORAGE_FORMAT"));
        assertEquals(term.getCode(), "PROPRIETARY");
        assertEquals(term.getDescription(), "proprietary description");
        assertEquals(term.getLabel(), "proprietary label");
        assertEquals(term.getOrdinal(), Long.valueOf(1));
        assertEquals(term.isOfficial(), Boolean.TRUE);
        assertEqualsDate(term.getModificationDate(), "2008-11-05 09:18:00");
        assertEqualsDate(term.getRegistrationDate(), "2008-11-05 09:18:00");
        assertRegistratorNotFetched(term);

        assertEquals(term.getVocabulary().getCode(), "$STORAGE_FORMAT");
        assertEquals(term.getVocabulary().getDescription(), "The on-disk storage format of a data set");
        assertEqualsDate(term.getVocabulary().getRegistrationDate(), "2008-11-05 09:18:00");
        assertEqualsDate(term.getVocabulary().getModificationDate(), "2009-03-23 15:34:44");
        assertEquals(term.getVocabulary().getRegistrator().getUserId(), "system");
    }

    @Test
    public void testSearchWithRegistratorFetched()
    {
        VocabularyTermFetchOptions fetchOptions = new VocabularyTermFetchOptions();
        fetchOptions.withRegistrator();

        VocabularyTerm term = searchTerm(new VocabularyTermPermId("PROPRIETARY", "$STORAGE_FORMAT"), fetchOptions);

        assertEquals(term.getPermId(), new VocabularyTermPermId("PROPRIETARY", "$STORAGE_FORMAT"));
        assertEquals(term.getCode(), "PROPRIETARY");
        assertEquals(term.getDescription(), "proprietary description");
        assertEquals(term.getLabel(), "proprietary label");
        assertEquals(term.getOrdinal(), Long.valueOf(1));
        assertEquals(term.isOfficial(), Boolean.TRUE);
        assertEqualsDate(term.getModificationDate(), "2008-11-05 09:18:00");
        assertEqualsDate(term.getRegistrationDate(), "2008-11-05 09:18:00");
        assertEquals(term.getRegistrator().getUserId(), "system");
        assertVocabularyNotFetched(term);
    }

    @Test
    public void testSearchWithEmptyCriteria()
    {
        testSearch(new VocabularyTermSearchCriteria(), 17);
    }

    @Test
    public void testSearchWithIdSetToPermId()
    {
        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withId().thatEquals(new VocabularyTermPermId("HUMAN", "ORGANISM"));
        testSearch(criteria, new VocabularyTermPermId("HUMAN", "ORGANISM"));
    }

    @Test
    public void testSearchWithIdSetToNonexistentPermId()
    {
        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withId().thatEquals(new VocabularyTermPermId("IDONTEXIST", "MENEITHER"));
        testSearch(criteria, 0);
    }

    @Test
    public void testSearchWithPermIdThatEquals()
    {
        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withPermId().thatEquals("HUMAN (ORGANISM)");
        testSearch(criteria, new VocabularyTermPermId("HUMAN", "ORGANISM"));
    }

    @Test
    public void testSearchWithPermIdThatContains()
    {
        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withPermId().thatContains("MAN");
        testSearch(criteria, new VocabularyTermPermId("CHILD", "HUMAN"), new VocabularyTermPermId("HUMAN", "ORGANISM"),
                new VocabularyTermPermId("MAN", "HUMAN"), new VocabularyTermPermId("WOMAN", "HUMAN"));
    }

    @Test
    public void testSearchWithPermIdThatStartsWith()
    {
        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withPermId().thatStartsWith("MAN");
        testSearch(criteria, new VocabularyTermPermId("MAN", "HUMAN"));
    }

    @Test
    public void testSearchWithPermIdThatEndsWith()
    {
        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withPermId().thatEndsWith("N)");
        testSearch(criteria, new VocabularyTermPermId("CHILD", "HUMAN"), new VocabularyTermPermId("MAN", "HUMAN"),
                new VocabularyTermPermId("WOMAN", "HUMAN"));
    }

    @Test
    public void testSearchWithCodeThatEquals()
    {
        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withCode().thatEquals("MAN");
        testSearch(criteria, new VocabularyTermPermId("MAN", "HUMAN"));
    }

    @Test
    public void testSearchWithCodeThatContains()
    {
        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withCode().thatContains("MAN");
        testSearch(criteria, new VocabularyTermPermId("HUMAN", "ORGANISM"),
                new VocabularyTermPermId("MAN", "HUMAN"), new VocabularyTermPermId("WOMAN", "HUMAN"));
    }

    @Test
    public void testSearchWithCodeThatStartsWith()
    {
        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withCode().thatStartsWith("MAN");
        testSearch(criteria, new VocabularyTermPermId("MAN", "HUMAN"));
    }

    @Test
    public void testSearchWithCodeThatEndsWith()
    {
        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withCode().thatEndsWith("D");
        testSearch(criteria, new VocabularyTermPermId("CHILD", "HUMAN"));
    }

    @Test
    public void testSearchWithVocabularyCodeThatEquals()
    {
        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withVocabulary().withCode().thatEquals("HUMAN");
        testSearch(criteria, new VocabularyTermPermId("CHILD", "HUMAN"), new VocabularyTermPermId("MAN", "HUMAN"),
                new VocabularyTermPermId("WOMAN", "HUMAN"));
    }

    @Test
    public void testSearchWithVocabularyCodeThatContains()
    {
        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withVocabulary().withCode().thatContains("MA");
        testSearch(criteria, new VocabularyTermPermId("BDS_DIRECTORY", "$STORAGE_FORMAT"), new VocabularyTermPermId("CHILD", "HUMAN"),
                new VocabularyTermPermId("MAN", "HUMAN"), new VocabularyTermPermId("PROPRIETARY", "$STORAGE_FORMAT"),
                new VocabularyTermPermId("WOMAN", "HUMAN"));
    }

    @Test
    public void testSearchWithVocabularyCodeThatStartsWith()
    {
        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withVocabulary().withCode().thatStartsWith("G");
        testSearch(criteria, new VocabularyTermPermId("FEMALE", "GENDER"), new VocabularyTermPermId("MALE", "GENDER"));
    }

    @Test
    public void testSearchWithVocabularyCodeThatEndsWith()
    {
        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withVocabulary().withCode().thatEndsWith("T");
        testSearch(criteria, new VocabularyTermPermId("BDS_DIRECTORY", "$STORAGE_FORMAT"),
                new VocabularyTermPermId("PROPRIETARY", "$STORAGE_FORMAT"));
    }

    @Test
    public void testSearchWithAndOperator()
    {
        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withAndOperator();
        criteria.withCode().thatContains("384");
        criteria.withCode().thatContains("WELLS");
        testSearch(criteria, new VocabularyTermPermId("384_WELLS_16X24", "$PLATE_GEOMETRY"));
    }

    @Test
    public void testSearchWithOrOperator()
    {
        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("96_WELLS_8X12 ($PLATE_GEOMETRY)");
        criteria.withPermId().thatEquals("384_WELLS_16X24 ($PLATE_GEOMETRY)");
        testSearch(criteria, new VocabularyTermPermId("96_WELLS_8X12", "$PLATE_GEOMETRY"),
                new VocabularyTermPermId("384_WELLS_16X24", "$PLATE_GEOMETRY"));
    }

    private void testSearch(VocabularyTermSearchCriteria criteria, VocabularyTermPermId... expectedPermIds)
    {
        VocabularyTermFetchOptions fetchOptions = new VocabularyTermFetchOptions();
        fetchOptions.sortBy().code().asc();

        List<VocabularyTerm> terms = searchTerms(criteria, fetchOptions);

        assertVocabularyTermPermIds(terms, expectedPermIds);
    }

    private List<VocabularyTerm> testSearch(VocabularyTermSearchCriteria criteria, int expectedCount)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SearchResult<VocabularyTerm> searchResult =
                v3api.searchVocabularyTerms(sessionToken, criteria, new VocabularyTermFetchOptions());

        assertEquals(searchResult.getObjects().size(), expectedCount);

        v3api.logout(sessionToken);

        return searchResult.getObjects();
    }

}
