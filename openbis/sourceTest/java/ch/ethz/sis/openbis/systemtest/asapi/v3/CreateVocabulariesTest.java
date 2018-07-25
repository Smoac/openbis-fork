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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyTermCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author Franz-Josef Elmer
 */
public class CreateVocabulariesTest extends AbstractTest
{
    @Test
    public void testCreateVocabulary()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        VocabularyCreation vocabularyCreation = new VocabularyCreation();
        vocabularyCreation.setCode("CREATION_TEST");
        vocabularyCreation.setDescription("creation test");
        vocabularyCreation.setManagedInternally(true);
        vocabularyCreation.setInternalNameSpace(true);
        vocabularyCreation.setChosenFromList(true);
        vocabularyCreation.setUrlTemplate("https://en.wikipedia.org/wiki/${term}");
        VocabularyTermCreation term1 = new VocabularyTermCreation();
        term1.setCode("OMEGA");
        VocabularyTermCreation term2 = new VocabularyTermCreation();
        term2.setCode("ALPHA");
        vocabularyCreation.setTerms(Arrays.asList(term1, term2));
        VocabularyCreation vocabularyCreation2 = new VocabularyCreation();
        vocabularyCreation2.setCode(vocabularyCreation.getCode());

        // When
        List<VocabularyPermId> vocabularies = v3api.createVocabularies(sessionToken,
                Arrays.asList(vocabularyCreation, vocabularyCreation2));

        // Then
        VocabularyFetchOptions fetchOptions = new VocabularyFetchOptions();
        fetchOptions.withTerms();
        fetchOptions.withRegistrator();
        Map<IVocabularyId, Vocabulary> map = v3api.getVocabularies(sessionToken, vocabularies, fetchOptions);
        Vocabulary vocabulary = map.get(vocabularies.get(0));
        assertEquals(vocabulary.getCode(), "$" + vocabularyCreation.getCode());
        assertEquals(vocabulary.getDescription(), vocabularyCreation.getDescription());
        assertEquals(vocabulary.isManagedInternally(), vocabularyCreation.isManagedInternally());
        assertEquals(vocabulary.isInternalNameSpace(), vocabularyCreation.isInternalNameSpace());
        assertEquals(vocabulary.isChosenFromList(), vocabularyCreation.isChosenFromList());
        assertEquals(vocabulary.getUrlTemplate(), vocabularyCreation.getUrlTemplate());
        List<VocabularyTerm> terms = vocabulary.getTerms();
        assertEquals(terms.toString(), "[VocabularyTerm ALPHA, VocabularyTerm OMEGA]");
        assertEquals(map.get(vocabularies.get(1)).getCode(), vocabularyCreation2.getCode());
        assertEquals(map.get(vocabularies.get(1)).isInternalNameSpace(), false);
        assertEquals(map.get(vocabularies.get(1)).isManagedInternally(), false);
        assertEquals(vocabularies.size(), 2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testCreateVocabularyWithMissingCode()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        VocabularyCreation vocabularyCreation = new VocabularyCreation();

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    v3api.createVocabularies(sessionToken, Arrays.asList(vocabularyCreation));
                }
            },
                // Then
                "Code cannot be empty.");

        v3api.logout(sessionToken);
    }

    @Test
    public void testCreateVocabularyWithInvalidCode()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        VocabularyCreation vocabularyCreation = new VocabularyCreation();
        vocabularyCreation.setCode("invalid code");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    v3api.createVocabularies(sessionToken, Arrays.asList(vocabularyCreation));
                }
            },
                // Then
                "Given code 'INVALID CODE' contains illegal characters (allowed: A-Z, a-z, 0-9 and _, -, .");

        v3api.logout(sessionToken);
    }

    @Test(dataProvider = "usersNotAllowedToCreateVocabularies")
    public void testCreateWithUserCausingAuthorizationFailure(final String user)
    {
        assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    VocabularyCreation vocabularyCreation = new VocabularyCreation();
                    vocabularyCreation.setCode("AUTHORIZATION_TEST_VOCABULARY");
                    v3api.createVocabularies(sessionToken, Arrays.asList(vocabularyCreation));
                }
            });
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyCreation creation = new VocabularyCreation();
        creation.setCode("LOG_TEST_1");

        VocabularyCreation creation2 = new VocabularyCreation();
        creation2.setCode("LOG_TEST_2");

        v3api.createVocabularies(sessionToken, Arrays.asList(creation, creation2));

        assertAccessLog(
                "create-vocabularies  NEW_VOCABULARIES('[VocabularyCreation[code=LOG_TEST_1], VocabularyCreation[code=LOG_TEST_2]]')");
    }

    @DataProvider
    Object[][] usersNotAllowedToCreateVocabularies()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER,
                TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD, TEST_SPACE_USER);
    }
}
