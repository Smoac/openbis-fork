/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.systemtest.plugin.excelimport;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyTermCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional(transactionManager = "transaction-manager")
@Rollback
public class ImportVocabularyTypesTest extends AbstractImportTest
{

    private static final String VOCABULARIES_TYPES_XLS = "vocabularies/normal_vocab.xls";

    private static final String VOCABULARIES_NO_CODE = "vocabularies/vocab_no_code.xlsx";

    private static final String VOCABULARIES_NO_DESCRIPTION = "vocabularies/vocab_no_desc.xlsx";

    private static final String VOCABULARIES_NO_TERM_CODE = "vocabularies/vocab_no_term_code.xlsx";

    private static final String VOCABULARIES_NO_TERM_DESCRIPTION = "vocabularies/vocab_no_term_desc.xlsx";

    private static final String VOCABULARIES_NO_TERM_LABEL = "vocabularies/vocab_no_term_label.xlsx";

    private static final String VOCABULARIES_NO_TERMS = "vocabularies/vocab_no_term_label.xlsx";

    private static final String EXIST_VOCABULARIES = "vocabularies/exist_vocab_type.xlsx";

    private static final String VOCABULARY_WITH_TERM_TO_TAKE_OVER = "vocabularies/vocab_with_term_to_take_over.xlsx";

    @Autowired
    private IApplicationServerInternalApi v3api;

    private static String FILES_DIR;

    @BeforeClass
    public void setupClass() throws IOException
    {
        String f = ImportVocabularyTypesTest.class.getName().replace(".", "/");
        FILES_DIR = f.substring(0, f.length() - ImportVocabularyTypesTest.class.getSimpleName().length()) + "test_files/";
    }

    @Test
    @DirtiesContext
    public void testNormalVocabularyCreationIsCreated() throws IOException
    {
        // the Excel contains internally managed vocabularies which can be only manipulated by the system user
        sessionToken = v3api.loginAsSystem();

        // GIVEN
        final String sessionWorkspaceFilePath = uploadToAsSessionWorkspace(sessionToken, FilenameUtils.concat(FILES_DIR, VOCABULARIES_TYPES_XLS));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(sessionWorkspaceFilePath));
        // WHEN
        Vocabulary detection = TestUtils.getVocabulary(v3api, sessionToken, "DETECTION");
        // THEN
        assertNotNull(detection);
        assertEquals(detection.getDescription(), "Protein detection system");
    }

    @Test
    @DirtiesContext
    public void testNormalVocabularyHasFirstTermCreated() throws IOException
    {
        // the Excel contains internally managed vocabularies which can be only manipulated by the system user
        sessionToken = v3api.loginAsSystem();

        // GIVEN
        final String sessionWorkspaceFilePath = uploadToAsSessionWorkspace(sessionToken, FilenameUtils.concat(FILES_DIR, VOCABULARIES_TYPES_XLS));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(sessionWorkspaceFilePath));

        // WHEN
        Vocabulary detection = TestUtils.getVocabulary(v3api, sessionToken, "DETECTION");

        // THEN
        VocabularyTerm term = detection.getTerms().get(0);
        assertEquals(term.getCode(), "HRP");
        assertEquals(term.getDescription(), "The antibody is conjugated with the horseradish peroxydase");
        assertEquals(term.getLabel(), "horseradish peroxydase");
    }

    @Test
    @DirtiesContext
    public void testNormalVocabularyCreatedNoExtraVocabulary() throws IOException
    {
        // the Excel contains internally managed vocabularies which can be only manipulated by the system user
        sessionToken = v3api.loginAsSystem();

        // GIVEN
        final String sessionWorkspaceFilePath = uploadToAsSessionWorkspace(sessionToken, FilenameUtils.concat(FILES_DIR, VOCABULARIES_TYPES_XLS));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(sessionWorkspaceFilePath));

        // WHEN
        List<Vocabulary> vocabularies = TestUtils.getAllVocabularies(v3api, sessionToken);

        // THEN
        assertEquals(vocabularies.size(), 3); // 2 created + 1 default
    }

    @Test
    @DirtiesContext
    public void testNormalVocabularyHasSecondTermCreated() throws IOException
    {
        // the Excel contains internally managed vocabularies which can be only manipulated by the system user
        sessionToken = v3api.loginAsSystem();

        // GIVEN
        final String sessionWorkspaceFilePath = uploadToAsSessionWorkspace(sessionToken, FilenameUtils.concat(FILES_DIR, VOCABULARIES_TYPES_XLS));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(sessionWorkspaceFilePath));

        // WHEN
        Vocabulary detection = TestUtils.getVocabulary(v3api, sessionToken, "DETECTION");

        // THEN
        VocabularyTerm term = detection.getTerms().get(1);
        assertEquals(term.getCode(), "TEST_VOC");
        assertEquals(term.getDescription(), "some focabulary that is used in tests and nothing else");
        assertEquals(term.getLabel(), "vocabulary for tests");
    }

    @Test
    @DirtiesContext
    public void testVocabularyWithNoTermDescriptionShouldBeCreated() throws IOException
    {
        // GIVEN
        final String sessionWorkspaceFilePath = uploadToAsSessionWorkspace(sessionToken,
                FilenameUtils.concat(FILES_DIR, VOCABULARIES_NO_TERM_DESCRIPTION));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(sessionWorkspaceFilePath));

        // WHEN
        Vocabulary detection = TestUtils.getVocabulary(v3api, sessionToken, "DETECTION");

        // THEN
        assertNotNull(detection);
        assertNull(detection.getTerms().get(0).getDescription());
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "(s?).*Mandatory field is missing or empty: Code.*")
    public void shouldThrowExceptionIfNoVocabularyCode() throws IOException
    {
        final String sessionWorkspaceFilePath = uploadToAsSessionWorkspace(sessionToken, FilenameUtils.concat(FILES_DIR, VOCABULARIES_NO_CODE));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(sessionWorkspaceFilePath));
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "(s?).*Mandatory field is missing or empty: Code.*")
    public void shouldThrowExceptionIfNoTermCode() throws IOException
    {
        final String sessionWorkspaceFilePath = uploadToAsSessionWorkspace(sessionToken, FilenameUtils.concat(FILES_DIR, VOCABULARIES_NO_TERM_CODE));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(sessionWorkspaceFilePath));
    }

    @Test
    @DirtiesContext
    public void shouldNotThrowExceptionIfNoVocabularyDescription() throws IOException
    {
        // GIVEN
        final String sessionWorkspaceFilePath = uploadToAsSessionWorkspace(sessionToken,
                FilenameUtils.concat(FILES_DIR, VOCABULARIES_NO_DESCRIPTION));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(sessionWorkspaceFilePath));

        // WHEN
        Vocabulary detection = TestUtils.getVocabulary(v3api, sessionToken, "DETECTION");

        // THEN
        assertNotNull(detection);
        assertNull(detection.getDescription());
    }

    @Test
    @DirtiesContext
    public void shouldNotThrowExceptionIfNoTermLabel() throws IOException
    {
        // GIVEN
        final String sessionWorkspaceFilePath = uploadToAsSessionWorkspace(sessionToken, FilenameUtils.concat(FILES_DIR, VOCABULARIES_NO_TERM_LABEL));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(sessionWorkspaceFilePath));

        // WHEN
        Vocabulary detection = TestUtils.getVocabulary(v3api, sessionToken, "DETECTION");

        // THEN
        assertNotNull(detection);
        assertNull(detection.getTerms().get(0).getLabel());
    }

    @Test
    @DirtiesContext
    public void shouldNotThrowExceptionIfNoTerms() throws IOException
    {
        // GIVEN
        final String sessionWorkspaceFilePath = uploadToAsSessionWorkspace(sessionToken, FilenameUtils.concat(FILES_DIR, VOCABULARIES_NO_TERMS));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(sessionWorkspaceFilePath));

        // WHEN
        Vocabulary detection = TestUtils.getVocabulary(v3api, sessionToken, "DETECTION");

        // THEN
        assertNotNull(detection);
    }

    @Test
    @DirtiesContext
    public void updateVocabularyInDBThatIsNotInJson() throws IOException
    {
        TestUtils.createVocabulary(v3api, sessionToken, "TEST_VOCABULARY_TYPE", "Test desc");
        // there should be no exceptions
        final String sessionWorkspaceFilePath = uploadToAsSessionWorkspace(sessionToken, FilenameUtils.concat(FILES_DIR, EXIST_VOCABULARIES));
        TestUtils.createFrom(v3api, sessionToken, UpdateMode.UPDATE_IF_EXISTS, Paths.get(sessionWorkspaceFilePath));
        Vocabulary test = TestUtils.getVocabulary(v3api, sessionToken, "TEST_VOCABULARY_TYPE");
        assertNotNull(test);
    }

    @Test
    @DirtiesContext
    public void testTakeOverTerm() throws IOException
    {
        String instanceAdminSessionToken = v3api.login(TEST_USER, PASSWORD);
        String systemSessionToken = v3api.loginAsSystem();

        VocabularyCreation vocabularyCreation = new VocabularyCreation();
        vocabularyCreation.setCode("VOCABULARY-WITH-TERM-TO-TAKE-OVER");
        vocabularyCreation.setManagedInternally(true);

        VocabularyPermId vocabularyId = v3api.createVocabularies(systemSessionToken, Arrays.asList(vocabularyCreation)).get(0);

        VocabularyTermCreation termCreation = new VocabularyTermCreation();
        termCreation.setVocabularyId(vocabularyId);
        termCreation.setCode("TERM-TO-TAKE-OVER");
        termCreation.setLabel("Original Label");
        termCreation.setDescription("Original Description");

        v3api.createVocabularyTerms(instanceAdminSessionToken, Arrays.asList(termCreation));

        VocabularyFetchOptions fetchOptions = new VocabularyFetchOptions();
        fetchOptions.withTerms().withRegistrator();

        Vocabulary beforeVocabulary = v3api.getVocabularies(instanceAdminSessionToken, Arrays.asList(vocabularyId), fetchOptions).get(vocabularyId);
        VocabularyTerm beforeTerm = beforeVocabulary.getTerms().get(0);

        assertEquals(beforeTerm.getLabel(), "Original Label");
        assertEquals(beforeTerm.getDescription(), "Original Description");
        assertEquals(beforeTerm.getRegistrator().getUserId(), TEST_USER);

        final String sessionWorkspaceFilePath = uploadToAsSessionWorkspace(systemSessionToken,
                FilenameUtils.concat(FILES_DIR, VOCABULARY_WITH_TERM_TO_TAKE_OVER));
        TestUtils.createFrom(v3api, systemSessionToken, UpdateMode.UPDATE_IF_EXISTS, Paths.get(sessionWorkspaceFilePath));

        Vocabulary afterVocabulary = v3api.getVocabularies(instanceAdminSessionToken, Arrays.asList(vocabularyId), fetchOptions).get(vocabularyId);
        VocabularyTerm afterTerm = afterVocabulary.getTerms().get(0);

        assertEquals(afterTerm.getLabel(), "Updated Label");
        assertEquals(afterTerm.getDescription(), "Updated Description");
        assertEquals(afterTerm.getRegistrator().getUserId(), SYSTEM_USER);
    }

}