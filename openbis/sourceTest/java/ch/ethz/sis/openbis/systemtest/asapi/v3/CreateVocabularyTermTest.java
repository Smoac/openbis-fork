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
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyTermCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;
import ch.systemsx.cisd.common.test.AssertionUtil;

/**
 * @author pkupczyk
 */
public class CreateVocabularyTermTest extends AbstractTest
{

    @Test
    public void testCreateWithVocabularyIdNull()
    {
        try
        {
            String sessionToken = v3api.login(TEST_USER, PASSWORD);

            VocabularyTermCreation creation = termCreation();
            creation.setVocabularyId(null);

            v3api.createVocabularyTerms(sessionToken, Arrays.asList(creation));

            fail();
        } catch (Exception e)
        {
            AssertionUtil.assertContains("Vocabulary term vocabulary id cannot be null", e.getMessage());
        }
    }

    @Test
    public void testCreateWithCodeNull()
    {
        try
        {
            String sessionToken = v3api.login(TEST_USER, PASSWORD);

            VocabularyTermCreation creation = termCreation();
            creation.setCode(null);

            v3api.createVocabularyTerms(sessionToken, Arrays.asList(creation));

            fail();
        } catch (Exception e)
        {
            AssertionUtil.assertContains("Vocabulary term code cannot be null or empty", e.getMessage());
        }
    }

    @Test
    public void testCreateWithCodeDuplicated()
    {
        try
        {
            String sessionToken = v3api.login(TEST_USER, PASSWORD);

            VocabularyTermCreation creation = termCreation();
            creation.setCode("HUMAN");

            v3api.createVocabularyTerms(sessionToken, Arrays.asList(creation));

            fail();
        } catch (Exception e)
        {
            AssertionUtil.assertContains("Vocabulary term HUMAN (ORGANISM) already exists", e.getMessage());
        }
    }

    @Test
    public void testCreateWithOfficalTermAndAuthorizedUser()
    {
        VocabularyTermCreation creation = termCreation();
        creation.setOfficial(true);
        createTerms(TEST_USER, PASSWORD, creation);
    }

    @Test
    public void testCreateWithOfficalTermAndUnauthorizedUser()
    {
        try
        {
            VocabularyTermCreation creation = termCreation();
            creation.setOfficial(true);
            createTerms(TEST_GROUP_OBSERVER, PASSWORD, creation);

            fail();
        } catch (Exception e)
        {
            AssertionUtil.assertContains(
                    "None of method roles '[SPACE_POWER_USER, SPACE_ADMIN, INSTANCE_ADMIN, SPACE_ETL_SERVER, INSTANCE_ETL_SERVER]' could be found in roles of user 'observer'",
                    e.getMessage());
        }
    }

    @Test
    public void testCreateWithOfficalTermAndPreviousTermNull()
    {
        VocabularyTermCreation creation = termCreation();
        creation.setOfficial(true);
        createWithPreviousTermNull(creation);
    }

    @Test
    public void testCreateWithOfficalTermAndPreviousTermNotNull()
    {
        VocabularyTermCreation creation = termCreation();
        creation.setOfficial(true);
        createWithPreviousTermNotNull(creation);
    }

    @Test
    public void testCreateWithUnofficalTermAndAuthorizedUser()
    {
        VocabularyTermCreation creation = termCreation();
        creation.setOfficial(false);
        createTerms(TEST_USER, PASSWORD, creation);
    }

    @Test
    public void testCreateWithUnofficalTermAndUnauthorizedUser()
    {
        try
        {
            VocabularyTermCreation creation = termCreation();
            creation.setOfficial(false);
            createTerms(TEST_GROUP_OBSERVER, PASSWORD, creation);

            fail();
        } catch (Exception e)
        {
            AssertionUtil.assertContains(
                    "None of method roles '[SPACE_USER, SPACE_POWER_USER, SPACE_ADMIN, INSTANCE_ADMIN, SPACE_ETL_SERVER, INSTANCE_ETL_SERVER]' could be found in roles of user 'observer'",
                    e.getMessage());
        }
    }

    @Test
    public void testCreateWithUnofficalTermAndPreviousTermNull()
    {
        VocabularyTermCreation creation = termCreation();
        creation.setOfficial(false);
        createWithPreviousTermNull(creation);
    }

    @Test
    public void testCreateWithUnofficalTermAndPreviousTermNotNull()
    {
        VocabularyTermCreation creation = termCreation();
        creation.setOfficial(false);
        createWithPreviousTermNotNull(creation);
    }

    @Test
    public void testCreateWithInternallyManagedVocabularyAndAuthorizedUser()
    {
        VocabularyTermCreation creation = termCreationInternallyManaged();
        createTerms(TEST_USER, PASSWORD, creation);
    }

    @Test
    public void testCreateWithInternallyManagedVocabularyAndUnauthorizedUser()
    {
        try
        {
            VocabularyTermCreation creation = termCreationInternallyManaged();
            createTerms(TEST_SPACE_USER, PASSWORD, creation);

            fail();
        } catch (Exception e)
        {
            AssertionUtil.assertContains("Not allowed to add terms to an internally managed vocabulary", e.getMessage());
        }
    }

    @Test
    public void testCreateWithPreviousTermNonexistent()
    {
        try
        {
            VocabularyTermCreation creation = termCreation();
            creation.setPreviousTermId(new VocabularyTermPermId("ORGANISM", "IDONTEXIST"));
            createTerms(TEST_USER, PASSWORD, creation);

            fail();
        } catch (Exception e)
        {
            AssertionUtil.assertContains(
                    "Position of term TIGER (ORGANISM) could not be found as the specified previous term IDONTEXIST (ORGANISM) does not exist",
                    e.getMessage());
        }
    }

    @Test
    public void testCreateWithPreviousTermFromDifferentDictionary()
    {
        try
        {
            VocabularyTermCreation creation = termCreation();
            creation.setPreviousTermId(new VocabularyTermPermId("GENDER", "MALE"));
            createTerms(TEST_USER, PASSWORD, creation);

            fail();
        } catch (Exception e)
        {
            AssertionUtil.assertContains(
                    "Position of term TIGER (ORGANISM) could not be found as the specified previous term MALE (GENDER) is in a different vocabulary (GENDER)",
                    e.getMessage());
        }
    }

    @Test
    public void testCreateWithPreviousTermNewlyCreated()
    {
        VocabularyTermCreation creation1 = termCreation();
        creation1.setCode("NEW1");
        creation1.setPreviousTermId(new VocabularyTermPermId("ORGANISM", "HUMAN"));

        VocabularyTermCreation creation2 = termCreation();
        creation2.setCode("NEW2");
        creation2.setPreviousTermId(new VocabularyTermPermId("ORGANISM", "NEW1"));

        List<VocabularyTerm> termsBefore = listTerms(creation1.getVocabularyId());
        assertTerms(termsBefore, "RAT", "DOG", "HUMAN", "GORILLA", "FLY");

        createTerms(TEST_USER, PASSWORD, creation1, creation2);

        List<VocabularyTerm> termsAfter = listTerms(creation1.getVocabularyId());
        assertTerms(termsAfter, "RAT", "DOG", "HUMAN", "NEW1", "NEW2", "GORILLA", "FLY");
    }

    @Test
    public void testCreateMultipleTerms()
    {
        VocabularyTermCreation creation1 = termCreation();
        creation1.setCode("NEW1");
        creation1.setPreviousTermId(new VocabularyTermPermId("ORGANISM", "HUMAN"));

        VocabularyTermCreation creation2 = termCreation();
        creation2.setCode("NEW2");
        creation2.setPreviousTermId(new VocabularyTermPermId("ORGANISM", "GORILLA"));

        VocabularyTermCreation creation3 = termCreation();
        creation3.setCode("NEW3");
        creation3.setOfficial(false);
        creation3.setPreviousTermId(new VocabularyTermPermId("ORGANISM", "NEW2"));

        VocabularyTermCreation creation4 = termCreation();
        creation4.setCode("NEW4");
        creation4.setPreviousTermId(null);

        List<VocabularyTerm> termsBefore = listTerms(creation1.getVocabularyId());
        assertTerms(termsBefore, "RAT", "DOG", "HUMAN", "GORILLA", "FLY");

        createTerms(TEST_USER, PASSWORD, creation1, creation2, creation3, creation4);

        List<VocabularyTerm> termsAfter = listTerms(creation1.getVocabularyId());
        assertTerms(termsAfter, "RAT", "DOG", "HUMAN", "NEW1", "GORILLA", "NEW2", "NEW3", "FLY", "NEW4");
    }

    private VocabularyTermCreation termCreation()
    {
        VocabularyTermCreation creation = new VocabularyTermCreation();
        creation.setVocabularyId(new VocabularyPermId("ORGANISM"));
        creation.setCode("TIGER");
        creation.setDescription("tiger's description");
        creation.setLabel("tiger's label");
        return creation;
    }

    private VocabularyTermCreation termCreationInternallyManaged()
    {
        VocabularyTermCreation creation = new VocabularyTermCreation();
        creation.setVocabularyId(new VocabularyPermId("$PLATE_GEOMETRY"));
        creation.setCode("1_WELL_1X1");
        creation.setDescription("geometry description");
        creation.setLabel("geometry label");
        return creation;
    }

    private List<VocabularyTerm> createTerms(String user, String password, VocabularyTermCreation... creations)
    {
        String sessionToken = v3api.login(user, password);

        // build criteria
        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withOrOperator();
        for (VocabularyTermCreation creation : creations)
        {
            String vocabularyCode = ((VocabularyPermId) creation.getVocabularyId()).getPermId();
            criteria.withId().thatEquals(new VocabularyTermPermId(vocabularyCode, creation.getCode()));
        }

        // build fetch options
        VocabularyTermFetchOptions fetchOptions = new VocabularyTermFetchOptions();
        fetchOptions.withRegistrator();
        fetchOptions.withVocabulary();

        // search before creation
        SearchResult<VocabularyTerm> resultsBefore = v3api.searchVocabularyTerms(sessionToken, criteria, fetchOptions);
        assertEquals(resultsBefore.getTotalCount(), 0);

        // create
        List<VocabularyTermPermId> permIds = v3api.createVocabularyTerms(sessionToken, Arrays.asList(creations));

        assertEquals(permIds.size(), creations.length);
        for (int i = 0; i < creations.length; i++)
        {
            VocabularyTermCreation creation = creations[i];
            VocabularyTermPermId permId = permIds.get(i);
            assertEquals(permId.getTermCode(), creation.getCode());
            assertEquals(permId.getVocabularyCode(), ((VocabularyPermId) creation.getVocabularyId()).getPermId());
        }

        // search after creation
        SearchResult<VocabularyTerm> resultsAfter = v3api.searchVocabularyTerms(sessionToken, criteria, fetchOptions);

        assertEquals(resultsAfter.getTotalCount(), creations.length);
        for (int i = 0; i < creations.length; i++)
        {
            VocabularyTermCreation creation = creations[i];
            VocabularyTerm term = resultsAfter.getObjects().get(i);
            assertEquals(term.getCode(), creation.getCode());
            assertEquals(term.getVocabulary().getCode(), ((VocabularyPermId) creation.getVocabularyId()).getPermId());
            assertEquals(term.getRegistrator().getUserId(), user);
            assertEquals(term.getLabel(), creation.getLabel());
            assertEquals(term.getDescription(), creation.getDescription());
            assertEquals(term.isOfficial(), creation.isOfficial());
        }

        v3api.logout(sessionToken);

        return resultsAfter.getObjects();
    }

    private void createWithPreviousTermNull(VocabularyTermCreation creation)
    {
        List<VocabularyTerm> termsBefore = listTerms(creation.getVocabularyId());
        assertTerms(termsBefore, "RAT", "DOG", "HUMAN", "GORILLA", "FLY");

        creation.setPreviousTermId(null);
        createTerms(TEST_USER, PASSWORD, creation);

        List<VocabularyTerm> termsAfter = listTerms(creation.getVocabularyId());
        assertTerms(termsAfter, "RAT", "DOG", "HUMAN", "GORILLA", "FLY", creation.getCode());
    }

    private void createWithPreviousTermNotNull(VocabularyTermCreation creation)
    {
        List<VocabularyTerm> termsBefore = listTerms(creation.getVocabularyId());
        assertTerms(termsBefore, "RAT", "DOG", "HUMAN", "GORILLA", "FLY");

        creation.setPreviousTermId(new VocabularyTermPermId("ORGANISM", "HUMAN"));
        createTerms(TEST_USER, PASSWORD, creation);

        List<VocabularyTerm> termsAfter = listTerms(creation.getVocabularyId());
        assertTerms(termsAfter, "RAT", "DOG", "HUMAN", creation.getCode(), "GORILLA", "FLY");
    }

    private List<VocabularyTerm> listTerms(IVocabularyId vocabularyId)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withVocabularyCode().thatEquals(((VocabularyPermId) vocabularyId).getPermId());

        VocabularyTermFetchOptions fetchOptions = new VocabularyTermFetchOptions();
        fetchOptions.sortBy().ordinal().asc();

        SearchResult<VocabularyTerm> results = v3api.searchVocabularyTerms(sessionToken, criteria, fetchOptions);

        v3api.logout(sessionToken);

        return results.getObjects();
    }

    private void assertTerms(List<VocabularyTerm> actualTerms, String... expectedCodes)
    {
        List<String> actualCodes = new ArrayList<String>();

        for (VocabularyTerm actualTerm : actualTerms)
        {
            actualCodes.add(actualTerm.getCode());
        }

        assertEquals(actualCodes, Arrays.asList(expectedCodes));
    }

}
