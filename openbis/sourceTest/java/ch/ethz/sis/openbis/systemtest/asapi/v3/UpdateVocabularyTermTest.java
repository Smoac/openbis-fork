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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyTermCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.VocabularyTermUpdate;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author pkupczyk
 */
@Test(groups = { "before remote api" })
public class UpdateVocabularyTermTest extends AbstractVocabularyTermTest
{

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Vocabulary term id cannot be null.*")
    public void testUpdateWithVocabularyIdNull()
    {
        VocabularyTermUpdate update = new VocabularyTermUpdate();
        updateTerms(TEST_USER, PASSWORD, update);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Object with VocabularyTermPermId = \\[IDONTEXIST \\(MENEITHER\\)\\] has not been found.*")
    public void testUpdateWithVocabularyIdNonexistent()
    {
        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(new VocabularyTermPermId("IDONTEXIST", "MENEITHER"));
        updateTerms(TEST_USER, PASSWORD, update);
    }

    @Test
    public void testUpdateWithOfficalTermAndAuthorizedUser()
    {
        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(getOfficialTermId());
        update.setDescription("Updated offical term");

        List<VocabularyTerm> terms = updateTerms(TEST_USER, PASSWORD, update);

        assertEquals(terms.get(0).getDescription(), update.getDescription().getValue());
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*None of method roles '\\[PROJECT_POWER_USER, PROJECT_ADMIN, SPACE_ADMIN, INSTANCE_ADMIN, SPACE_POWER_USER, SPACE_ETL_SERVER, INSTANCE_ETL_SERVER\\]' could be found in roles of user 'observer'.*")
    public void testUpdateWithOfficalTermAndUnauthorizedUser()
    {
        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(getOfficialTermId());
        update.setDescription("Updated offical term");

        updateTerms(TEST_GROUP_OBSERVER, PASSWORD, update);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Offical vocabulary term DOG \\(ORGANISM\\) cannot be updated to be unofficial.*")
    public void testUpdateWithOfficialTermMadeUnofficial()
    {
        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(getOfficialTermId());
        update.setOfficial(false);

        updateTerms(TEST_USER, PASSWORD, update);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*None of method roles '\\[PROJECT_POWER_USER, PROJECT_ADMIN, SPACE_ADMIN, INSTANCE_ADMIN, SPACE_POWER_USER, SPACE_ETL_SERVER, INSTANCE_ETL_SERVER\\]' could be found in roles of user 'observer'.*")
    public void testUpdateWithOfficialTermMadeUnofficialUnauthorized()
    {
        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(getOfficialTermId());
        update.setOfficial(false);

        updateTerms(TEST_GROUP_OBSERVER, PASSWORD, update);
    }

    @Test
    public void testUpdateWithUnofficalTermAndAuthorizedUser()
    {
        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(getUnofficialTermId());
        update.setDescription("Updated unofficial term");

        List<VocabularyTerm> terms = updateTerms(TEST_USER, PASSWORD, update);

        assertEquals(terms.get(0).getDescription(), update.getDescription().getValue());
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*None of method roles '\\[PROJECT_USER, PROJECT_POWER_USER, PROJECT_ADMIN, SPACE_ADMIN, INSTANCE_ADMIN, SPACE_POWER_USER, SPACE_USER, SPACE_ETL_SERVER, INSTANCE_ETL_SERVER\\]' could be found in roles of user 'observer'.*")
    public void testUpdateWithUnofficalTermAndUnauthorizedUser()
    {
        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(getUnofficialTermId());
        update.setDescription("Updated unofficial term");

        List<VocabularyTerm> terms = updateTerms(TEST_GROUP_OBSERVER, PASSWORD, update);

        assertEquals(terms.get(0).getDescription(), update.getDescription().getValue());
    }

    @Test
    public void testUpdateWithUnofficialTermMadeOfficial()
    {
        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(getUnofficialTermId());
        update.setOfficial(true);

        List<VocabularyTerm> terms = updateTerms(TEST_USER, PASSWORD, update);

        assertEquals(terms.get(0).isOfficial(), Boolean.TRUE);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*None of method roles '\\[PROJECT_POWER_USER, PROJECT_ADMIN, SPACE_ADMIN, INSTANCE_ADMIN, SPACE_POWER_USER, SPACE_ETL_SERVER, INSTANCE_ETL_SERVER\\]' could be found in roles of user 'observer'.*")
    public void testUpdateWithUnofficialTermMadeOfficialUnauthorized()
    {
        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(getUnofficialTermId());
        update.setOfficial(true);

        updateTerms(TEST_GROUP_OBSERVER, PASSWORD, update);
    }

    @Test
    public void testUpdateWithPreviousTermIdNull()
    {
        String vocabularyCode = "ORGANISM";

        List<VocabularyTerm> termsBefore = searchTerms(vocabularyCode);
        assertTerms(termsBefore, "RAT", "DOG", "HUMAN", "GORILLA", "FLY");

        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(new VocabularyTermPermId("DOG", vocabularyCode));
        update.setPreviousTermId(null);

        updateTerms(TEST_USER, PASSWORD, update);

        List<VocabularyTerm> termsAfter = searchTerms(vocabularyCode);
        assertTerms(termsAfter, "DOG", "RAT", "HUMAN", "GORILLA", "FLY");
    }

    @Test
    public void testUpdateWithPreviousTermIdNotNull()
    {
        String vocabularyCode = "ORGANISM";

        List<VocabularyTerm> termsBefore = searchTerms(vocabularyCode);
        assertTerms(termsBefore, "RAT", "DOG", "HUMAN", "GORILLA", "FLY");

        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(new VocabularyTermPermId("DOG", vocabularyCode));
        update.setPreviousTermId(new VocabularyTermPermId("GORILLA", vocabularyCode));

        updateTerms(TEST_USER, PASSWORD, update);

        List<VocabularyTerm> termsAfter = searchTerms(vocabularyCode);
        assertTerms(termsAfter, "RAT", "HUMAN", "GORILLA", "DOG", "FLY");
    }

    @Test
    public void testUpdateWithPreviousTermIdAndMultipleTerms()
    {
        String vocabularyCode = "ORGANISM";

        List<VocabularyTerm> termsBefore = searchTerms(vocabularyCode);
        assertTerms(termsBefore, "RAT", "DOG", "HUMAN", "GORILLA", "FLY");

        VocabularyTermUpdate updateDog = new VocabularyTermUpdate();
        updateDog.setVocabularyTermId(new VocabularyTermPermId("DOG", vocabularyCode));
        updateDog.setPreviousTermId(new VocabularyTermPermId("GORILLA", vocabularyCode));

        VocabularyTermUpdate updateGorilla = new VocabularyTermUpdate();
        updateGorilla.setVocabularyTermId(new VocabularyTermPermId("GORILLA", vocabularyCode));
        updateGorilla.setPreviousTermId(new VocabularyTermPermId("RAT", vocabularyCode));

        updateTerms(TEST_USER, PASSWORD, updateDog, updateGorilla);

        List<VocabularyTerm> termsAfter = searchTerms(vocabularyCode);
        assertTerms(termsAfter, "RAT", "GORILLA", "HUMAN", "DOG", "FLY");
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Object with VocabularyTermPermId = \\[IDONTEXIST \\(ORGANISM\\)\\] has not been found.*")
    public void testUpdateWithPreviousTermIdNonexistent()
    {
        String vocabularyCode = "ORGANISM";

        List<VocabularyTerm> termsBefore = searchTerms(vocabularyCode);
        assertTerms(termsBefore, "RAT", "DOG", "HUMAN", "GORILLA", "FLY");

        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(new VocabularyTermPermId("DOG", vocabularyCode));
        update.setPreviousTermId(new VocabularyTermPermId("IDONTEXIST", vocabularyCode));

        updateTerms(TEST_USER, PASSWORD, update);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Position of term DOG \\(ORGANISM\\) could not be found as the specified previous term MALE \\(GENDER\\) is in a different vocabulary \\(GENDER\\).*")
    public void testUpdateWithPreviousTermIdFromDifferentVocabulary()
    {
        String vocabularyCode = "ORGANISM";

        List<VocabularyTerm> termsBefore = searchTerms(vocabularyCode);
        assertTerms(termsBefore, "RAT", "DOG", "HUMAN", "GORILLA", "FLY");

        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(new VocabularyTermPermId("DOG", vocabularyCode));
        update.setPreviousTermId(new VocabularyTermPermId("MALE", "GENDER"));

        updateTerms(TEST_USER, PASSWORD, update);
    }

    @Test
    public void testUpdateWithLabel()
    {
        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(new VocabularyTermPermId("HUMAN", "ORGANISM"));
        update.setLabel("a brand new label");

        List<VocabularyTerm> terms = updateTerms(TEST_USER, PASSWORD, update);

        assertEquals(terms.get(0).getLabel(), update.getLabel().getValue());
    }

    @Test
    public void testUpdateWithDescription()
    {
        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(new VocabularyTermPermId("HUMAN", "ORGANISM"));
        update.setDescription("a brand new description");

        List<VocabularyTerm> terms = updateTerms(TEST_USER, PASSWORD, update);

        assertEquals(terms.get(0).getDescription(), update.getDescription().getValue());
    }

    @Test
    public void testUpdateWithInternallyManagedVocabularyAndAuthorized()
    {
        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(getInternallyManagedTermId());
        update.setDescription("a brand new description");

        List<VocabularyTerm> terms = updateTerms(TEST_USER, PASSWORD, update);

        assertEquals(terms.get(0).getDescription(), update.getDescription().getValue());
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Not allowed to update terms of an internally managed vocabulary.*")
    public void testUpdateWithInternallyManagedVocabularyAndUnauthorized()
    {
        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(getInternallyManagedTermId());
        update.setDescription("a brand new description");

        updateTerms(TEST_GROUP_OBSERVER, PASSWORD, update);
    }

    @Test
    public void testUpdateWithMultipleTerms()
    {
        // TODO
    }

    private List<VocabularyTerm> updateTerms(String user, String password, VocabularyTermUpdate... updates)
    {
        String sessionToken = v3api.login(user, password);

        List<IVocabularyTermId> ids = new ArrayList<IVocabularyTermId>();
        for (VocabularyTermUpdate update : updates)
        {
            ids.add(update.getVocabularyTermId());
        }

        v3api.updateVocabularyTerms(sessionToken, Arrays.asList(updates));

        List<VocabularyTerm> terms = searchTerms(ids, new VocabularyTermFetchOptions());

        return terms;
    }

    private VocabularyTermPermId getUnofficialTermId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyTermCreation creation = new VocabularyTermCreation();
        creation.setCode("IAMUNOFFICIAL");
        creation.setVocabularyId(new VocabularyPermId("ORGANISM"));
        creation.setOfficial(false);

        List<VocabularyTermPermId> permIds = v3api.createVocabularyTerms(sessionToken, Arrays.asList(creation));
        return permIds.get(0);
    }

    private VocabularyTermPermId getOfficialTermId()
    {
        return new VocabularyTermPermId("DOG", "ORGANISM");
    }

    private VocabularyTermPermId getInternallyManagedTermId()
    {
        return new VocabularyTermPermId("96_WELLS_8X12", "$PLATE_GEOMETRY");
    }

}
