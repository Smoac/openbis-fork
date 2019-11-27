package ch.ethz.sis.openbis.systemtest.plugin.excelimport;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.delete.PropertyTypeDeletionOptions;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional(transactionManager = "transaction-manager")
@Rollback
public class ImportPropertyTypesTest extends AbstractImportTest {

    @Autowired
    private IApplicationServerInternalApi v3api;

    private static final String TEST_USER = "test";

    private static final String PASSWORD = "password";

    private static final String PROPERTY_TYPES_XLS = "property_types/normal_property_type.xls";

    private static final String PROPERTY_NO_CODE = "property_types/no_code.xls";

    private static final String PROPERTY_NO_DATA_TYPE = "property_types/no_data_type.xls";

    private static final String PROPERTY_NO_DESCRIPTION = "property_types/no_desc.xls";

    private static final String PROPERTY_NO_LABEL = "property_types/no_label.xls";

    private static final String PROPERTY_VOCAB_TYPE_NO_VOCABULARY_CODE = "property_types/no_vocab_code.xls";

    private static final String PROPERTY_NON_VOCAB_TYPE_VOCABULARY_CODE = "property_types/vocabcode_when_not_vocabtype.xls";

    private static final String PROPERTY_DUPLICATES_DIFFERENT = "property_types/duplicates_different.xls";

    private static final String PROPERTY_TYPES_DUPLICATES_SAME = "property_types/duplicates_same.xls";

    private static final String PROPERTY_VOCABULARY_ON_SERVER = "property_types/with_vocab_on_server.xls";

    private static final String PROPERTY_VOCAB_TYPE = "property_types/with_vocab.xls";

    private static final String VOCABULARY_DETECTION = "property_types/vocabulary_detection.xls";

    private static String FILES_DIR;

    @BeforeClass
    public void setupClass() throws IOException {
        String f = ImportPropertyTypesTest.class.getName().replace(".", "/");
        FILES_DIR = f.substring(0, f.length() - ImportPropertyTypesTest.class.getSimpleName().length()) + "/test_files/";
    }

    @Test
    @DirtiesContext
    public void testNormalPropertyTypesAreCreated() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROPERTY_TYPES_XLS)));
        // WHEN
        PropertyType notes = TestUtils.getPropertyType(v3api, sessionToken, "NOTES");
        // THEN
        assertEquals(notes.getCode(), "NOTES");
        assertEquals(notes.getLabel(), "Notes");
        assertEquals(notes.getDataType(), DataType.MULTILINE_VARCHAR);
        assertEquals(notes.getDescription(), "Notes Descripton");
        assertFalse(notes.isInternalNameSpace());
        assertFalse(notes.isManagedInternally());
        assertNull(notes.getVocabulary());
    }

    @Test
    @DirtiesContext
    public void testInternalPropertyTypesAreCreated() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROPERTY_TYPES_XLS)));
        // WHEN
        PropertyType notes = TestUtils.getPropertyType(v3api, sessionToken, "$INTERNAL_PROP");
        // THEN
        assertEquals(notes.getCode(), "$INTERNAL_PROP");
        assertEquals(notes.getLabel(), "Name");
        assertEquals(notes.getDataType(), DataType.VARCHAR);
        assertEquals(notes.getDescription(), "Name");
        assertTrue(notes.isInternalNameSpace());
        assertFalse(notes.isManagedInternally());
        assertNull(notes.getVocabulary());
    }

    @Test
    @DirtiesContext
    public void testDuplicatesPropertiesAreAllowedIfTheyAreTheSame() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROPERTY_TYPES_DUPLICATES_SAME)));
        // WHEN
        PropertyType notes = TestUtils.getPropertyType(v3api, sessionToken, "NOTES");
        // THEN
        assertEquals(notes.getCode(), "NOTES");
        assertEquals(notes.getLabel(), "Notes");
        assertEquals(notes.getDataType(), DataType.MULTILINE_VARCHAR);
        assertEquals(notes.getDescription(), "Notes Descripton");
        assertFalse(notes.isInternalNameSpace());
        assertFalse(notes.isManagedInternally());
        assertNull(notes.getVocabulary());
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testPropertyTypeNoCode() throws IOException {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROPERTY_NO_CODE)));
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testPropertyTypeNoLabel() throws IOException {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROPERTY_NO_LABEL)));
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testPropertyTypesDuplicatesAreDifferent() throws IOException {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROPERTY_DUPLICATES_DIFFERENT)));
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testPropertyTypeNoVocabularyCodeWhenVocabularyType() throws IOException {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROPERTY_VOCAB_TYPE_NO_VOCABULARY_CODE)));
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testPropertyTypeNoDataType() throws IOException {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROPERTY_NO_DATA_TYPE)));
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testPropertyTypeNoDescription() throws IOException {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROPERTY_NO_DESCRIPTION)));
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testPropertyTypeVocabularyCodeToNonVocabularyType() throws IOException {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROPERTY_NON_VOCAB_TYPE_VOCABULARY_CODE)));
    }

    @Test(expectedExceptions = Exception.class)
    @DirtiesContext
    public void deleteProjectFromDBButNotFromJSON() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROPERTY_TYPES_XLS)));
        // WHEN
        PropertyType type = TestUtils.getPropertyType(v3api, sessionToken, "$INTERNAL_PROP");
        PropertyTypeDeletionOptions deletionOptions = new PropertyTypeDeletionOptions();
        deletionOptions.setReason("test");
        v3api.deletePropertyTypes(sessionToken, Arrays.asList(type.getPermId()), deletionOptions);
        // THEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROPERTY_TYPES_XLS)));
    }

}
