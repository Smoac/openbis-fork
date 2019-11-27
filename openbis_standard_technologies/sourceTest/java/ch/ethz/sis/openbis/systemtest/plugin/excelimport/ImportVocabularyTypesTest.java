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

package ch.ethz.sis.openbis.systemtest.plugin.excelimport;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional(transactionManager = "transaction-manager")
@Rollback
public class ImportVocabularyTypesTest extends AbstractImportTest {

    private static final String VOCABULARIES_TYPES_XLS = "vocabularies/normal_vocab.xls";

    private static final String VOCABULARIES_NO_CODE = "vocabularies/vocab_no_code.xlsx";

    private static final String VOCABULARIES_NO_DESCRIPTION = "vocabularies/vocab_no_desc.xlsx";

    private static final String VOCABULARIES_NO_TERM_CODE = "vocabularies/vocab_no_term_code.xlsx";

    private static final String VOCABULARIES_NO_TERM_DESCRIPTION = "vocabularies/vocab_no_term_desc.xlsx";

    private static final String VOCABULARIES_NO_TERM_LABEL = "vocabularies/vocab_no_term_label.xlsx";

    private static final String VOCABULARIES_NO_TERMS = "vocabularies/vocab_no_term_label.xlsx";

    @Autowired
    private IApplicationServerInternalApi v3api;

    private static final String TEST_USER = "test";

    private static final String PASSWORD = "password";

    private static String FILES_DIR;

    @BeforeClass
    public void setupClass() throws IOException {
        String f = ImportVocabularyTypesTest.class.getName().replace(".", "/");
        FILES_DIR = f.substring(0, f.length() - ImportVocabularyTypesTest.class.getSimpleName().length()) + "/test_files/";
    }

    @Test
    @DirtiesContext
    public void testNormalVocabularyCreationIsCreated() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, VOCABULARIES_TYPES_XLS)));
        // WHEN
        Vocabulary detection = TestUtils.getVocabulary(v3api, sessionToken, "DETECTION");
        // THEN
        assertNotNull(detection);
        assertEquals(detection.getDescription(), "Protein detection system");
    }

    @Test
    @DirtiesContext
    public void testNormalVocabularyHasFirstTermCreated() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, VOCABULARIES_TYPES_XLS)));
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
    public void testNormalVocabularyCreatedNoExtraVocabulary() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, VOCABULARIES_TYPES_XLS)));
        // WHEN
        List<Vocabulary> vocabularies = TestUtils.getAllVocabularies(v3api, sessionToken);
        // THEN
        assertEquals(vocabularies.size(), 3); // 2 created + 1 default
    }

    @Test
    @DirtiesContext
    public void testNormalVocabularyHasSecondTermCreated() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, VOCABULARIES_TYPES_XLS)));
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
    public void testVocabularyWithNoTermDescriptionShouldBeCreated() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, VOCABULARIES_NO_TERM_DESCRIPTION)));
        // WHEN
        Vocabulary detection = TestUtils.getVocabulary(v3api, sessionToken, "DETECTION");
        // THEN
        assertNotNull(detection);
        assertNull(detection.getTerms().get(0).getDescription());
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfNoVocabularyCode() throws IOException {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, VOCABULARIES_NO_CODE)));
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfNoTermCode() throws IOException {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, VOCABULARIES_NO_TERM_CODE)));
    }

    @Test
    @DirtiesContext
    public void shouldNotThrowExceptionIfNoVocabularyDescription() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, VOCABULARIES_NO_DESCRIPTION)));
        // WHEN
        Vocabulary detection = TestUtils.getVocabulary(v3api, sessionToken, "DETECTION");
        // THEN
        assertNotNull(detection);
        assertNull(detection.getDescription());
    }

    @Test
    @DirtiesContext
    public void shouldNotThrowExceptionIfNoTermLabel() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, VOCABULARIES_NO_TERM_LABEL)));
        // WHEN
        Vocabulary detection = TestUtils.getVocabulary(v3api, sessionToken, "DETECTION");
        // THEN
        assertNotNull(detection);
        assertNull(detection.getTerms().get(0).getLabel());
    }

    @Test
    @DirtiesContext
    public void shouldNotThrowExceptionIfNoTerms() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, VOCABULARIES_NO_TERMS)));
        // WHEN
        Vocabulary detection = TestUtils.getVocabulary(v3api, sessionToken, "DETECTION");
        // THEN
        assertNotNull(detection);
    }

}