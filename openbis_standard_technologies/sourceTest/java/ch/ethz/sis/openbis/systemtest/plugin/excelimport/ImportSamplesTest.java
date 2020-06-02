package ch.ethz.sis.openbis.systemtest.plugin.excelimport;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Paths;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional(transactionManager = "transaction-manager")
@Rollback
public class ImportSamplesTest extends AbstractImportTest {
    @Autowired
    private IApplicationServerInternalApi v3api;

    private static final String TEST_USER = "test";

    private static final String PASSWORD = "password";

    private static final String SAMPLES_XLS = "samples/all_in.xls";

    private static final String SAMPLES_SPACE_ELSEWHERE = "samples/space_elsewhere.xls";

    private static final String SAMPLES_SAMPLE_TYPE_ELSWHERE = "samples/sample_type_elsewhere.xls";

    private static final String SAMPLES_SPACE_PROJECT_EXPERIMENT_ELSEWHERE = "samples/space_project_experiment_elsewhere.xls";

    private static final String SPACE = "samples/space.xls";

    private static final String SAMPLE_TYPE = "samples/sample_type.xls";

    private static final String VOCABULARY_TYPE = "samples/vocab_type.xls";

    private static final String CHILD_AS_CODE = "samples/child_as_code.xls";

    private static final String CHILD_AS_DOLLARTAG = "samples/child_as_dollartag.xls";

    private static final String PARENT_AS_CODE = "samples/parent_as_code.xls";

    private static final String PARENT_AS_DOLLARTAG = "samples/parent_as_dollartag.xls";

    private static final String MANDATORY_FIELD_MISSING = "samples/mandatory_field_missing.xls";

    private static final String NON_MANDATORY_FIELD_MISSING = "samples/non_mandatory_field_missing.xls";

    private static final String AUTO_GENERATED_SAMPLE_LEVEL = "samples/with_auto_generated_code_sample_level.xls";

    private static final String AUTO_GENERATED_SAMPLE_TYPE_LEVEL = "samples/with_auto_generated_code_sampletype_level.xls";

    private static final String GENERAL_ELN_SETTINGS = "samples/general_eln_settings.xlsx";

    private static final String GENERAL_ELN_SETTINGS_UPDATE = "samples/general_eln_settings_update.xlsx";

    private static String FILES_DIR;

    @BeforeClass
    public void setupClass() throws IOException {
        String f = ImportSamplesTest.class.getName().replace(".", "/");
        FILES_DIR = f.substring(0, f.length() - ImportSamplesTest.class.getSimpleName().length()) + "/test_files/";
    }

    @Test
    @DirtiesContext
    public void testSamplesAreCreated() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLES_XLS)));
        // WHEN
        Sample sample = TestUtils.getSample(v3api, sessionToken, "AAA", "TEST_SPACE");
        // THEN
        assertEquals(sample.getCode(), "AAA");
        assertEquals(sample.getProject(), null);
        assertEquals(sample.getExperiment().getCode(), "TEST_EXPERIMENT2");
        assertEquals(sample.getSpace().getCode(), "TEST_SPACE");
    }

    @Test
    @DirtiesContext
    public void testSamplesAreCreatedSecondSample() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLES_XLS)));
        // WHEN
        Sample sample = TestUtils.getSample(v3api, sessionToken, "VVV", "TEST_SPACE");
        // THEN
        assertEquals(sample.getCode(), "VVV");
        assertEquals(sample.getProject(), null);
        assertEquals(sample.getExperiment().getCode(), "TEST_EXPERIMENT");
        assertEquals(sample.getSpace().getCode(), "TEST_SPACE");
    }

    @Test
    @DirtiesContext
    public void testSamplesAreCreatedThirdSample() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLES_XLS)));
        // WHEN
        Sample sample = TestUtils.getSample(v3api, sessionToken, "S1", "TEST_SPACE");
        // THEN
        assertEquals(sample.getCode(), "S1");
        assertEquals(sample.getProject(), null);
        assertEquals(sample.getExperiment().getCode(), "TEST_EXPERIMENT");
        assertEquals(sample.getSpace().getCode(), "TEST_SPACE");
    }

    @Test
    @DirtiesContext
    public void testSamplesAreCreatedWhenSpaceOnServer() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLES_SPACE_ELSEWHERE)));
        // WHEN
        Sample sample = TestUtils.getSample(v3api, sessionToken, "VVV", "TEST_SPACE");
        // THEN
        assertNotNull(sample);
    }

    @Test
    @DirtiesContext
    public void testSamplesAreCreatedWhenSpaceInSeparateXls() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken,
                Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLES_SPACE_ELSEWHERE)),
                Paths.get(FilenameUtils.concat(FILES_DIR, SPACE)));
        // WHEN
        Sample sample = TestUtils.getSample(v3api, sessionToken, "VVV", "TEST_SPACE");
        // THEN
        assertNotNull(sample);
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfSpaceDoesntExist() throws IOException {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLES_SPACE_ELSEWHERE)));
    }

    @Test
    @DirtiesContext
    public void testSamplesAreCreatedWhenSampleTypeOnServer() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken,
                Paths.get(FilenameUtils.concat(FILES_DIR, VOCABULARY_TYPE)),
                Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLE_TYPE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLES_SAMPLE_TYPE_ELSWHERE)));
        // WHEN
        Sample sample = TestUtils.getSample(v3api, sessionToken, "VVV", "TEST_SPACE");
        // THEN
        assertNotNull(sample);
    }

    @Test
    @DirtiesContext
    public void testSamplesAreCreatedWhenSampleTypeInSeparateXls() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken,
                Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLES_SAMPLE_TYPE_ELSWHERE)),
                Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLE_TYPE)));
        // WHEN
        Sample sample = TestUtils.getSample(v3api, sessionToken, "VVV", "TEST_SPACE");
        // THEN
        assertNotNull(sample);
    }

    @Test
    @DirtiesContext
    public void testSamplesChildrenAreAssignedWhenAddressedByCodeInXls() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken,
                Paths.get(FilenameUtils.concat(FILES_DIR, CHILD_AS_CODE)));
        // WHEN
        Sample sample = TestUtils.getSample(v3api, sessionToken, "VVV", "TEST_SPACE");
        // THEN
        assertNotNull(sample);
        assertEquals(sample.getChildren().size(), 1);
        assertEquals(sample.getChildren().get(0).getCode(), "AAA");
        assertEquals(sample.getChildren().get(0).getSpace().getCode(), "TEST_SPACE");
        assertEquals(sample.getChildren().get(0).getExperiment().getCode(), "TEST_EXPERIMENT2");
    }

    @Test
    @DirtiesContext
    public void testSamplesParentsAreAssignedWhenAddressedByCodeInXls() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken,
                Paths.get(FilenameUtils.concat(FILES_DIR, PARENT_AS_CODE)));
        // WHEN
        Sample sample = TestUtils.getSample(v3api, sessionToken, "VVV", "TEST_SPACE");
        // THEN
        assertNotNull(sample);
        assertEquals(sample.getParents().size(), 1);
        assertEquals(sample.getParents().get(0).getCode(), "AAA");
        assertEquals(sample.getParents().get(0).getSpace().getCode(), "TEST_SPACE");
        assertEquals(sample.getParents().get(0).getExperiment().getCode(), "TEST_EXPERIMENT2");
    }

    @Test
    @DirtiesContext
    public void testSamplesChildrenAreAssignedWhenAddressedByDollartagInXls() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken,
                Paths.get(FilenameUtils.concat(FILES_DIR, CHILD_AS_DOLLARTAG)));
        // WHEN
        Sample sample = TestUtils.getSample(v3api, sessionToken, "VVV", "TEST_SPACE");
        // THEN
        assertNotNull(sample);
        assertEquals(sample.getChildren().size(), 1);
        assertEquals(sample.getChildren().get(0).getCode(), "AAA");
        assertEquals(sample.getChildren().get(0).getSpace().getCode(), "TEST_SPACE");
        assertEquals(sample.getChildren().get(0).getExperiment().getCode(), "TEST_EXPERIMENT2");
    }

    @Test
    @DirtiesContext
    public void testSamplesParentsAreAssignedWhenAddressedByDollartagInXls() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken,
                Paths.get(FilenameUtils.concat(FILES_DIR, PARENT_AS_DOLLARTAG)));
        // WHEN
        Sample sample = TestUtils.getSample(v3api, sessionToken, "VVV", "TEST_SPACE");
        // THEN
        assertNotNull(sample);
        assertEquals(sample.getParents().size(), 1);
        assertEquals(sample.getParents().get(0).getCode(), "AAA");
        assertEquals(sample.getParents().get(0).getSpace().getCode(), "TEST_SPACE");
        assertEquals(sample.getParents().get(0).getExperiment().getCode(), "TEST_EXPERIMENT2");
    }

    @Test
    @DirtiesContext
    public void testCreatesSampleWithNonMandatoryFieldsMissing() throws IOException {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken,
                Paths.get(FilenameUtils.concat(FILES_DIR, NON_MANDATORY_FIELD_MISSING)));
        // WHEN
        Sample sample = TestUtils.getSample(v3api, sessionToken, "AAA", "TEST_SPACE");
        // THEN
        assertNotNull(sample);
        assertEquals(sample.getProperties().get("FOR_WHAT"), null);
    }

    @Test
    @DirtiesContext
    public void testCreatesSampleWithAutogeneratedCodeWhenOnPerSampleLevel() throws IOException {
        // GIVEN
        String result = TestUtils.createFrom(v3api, sessionToken,
                Paths.get(FilenameUtils.concat(FILES_DIR, AUTO_GENERATED_SAMPLE_LEVEL)));
        String permId = TestUtils.extractSamplePermIdFromResults(result);
        // WHEN
        Sample sample = TestUtils.getSampleByPermId(v3api, sessionToken, permId);
        // THEN
        assertNotNull(sample.getCode());
        assertEquals(sample.getType().getCode(), "ANTIBODY");
    }

    @Test
    @DirtiesContext
    public void testCreatesSampleWithAutogeneratedCodeWhenOnSampleTypeLevel() throws IOException {
        // GIVEN
        String result = TestUtils.createFrom(v3api, sessionToken,
                Paths.get(FilenameUtils.concat(FILES_DIR, AUTO_GENERATED_SAMPLE_TYPE_LEVEL)));
        String permId = TestUtils.extractSamplePermIdFromResults(result);
        // WHEN
        Sample sample = TestUtils.getSampleByPermId(v3api, sessionToken, permId);
        // THEN
        assertNotNull(sample);
        assertEquals(sample.getType().getCode(), "ANTIBODY");
    }

    // TODO : Enable when we stop using the lucene index
    // @Test
    // @DirtiesContext
    // public void testSampleIsUpdateByXlsParser() throws IOException {
    //     TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, GENERAL_ELN_SETTINGS)));

    //     // test sample before update
    //     Sample sample = TestUtils.getSample(v3api, sessionToken, "GENERAL_ELN_SETTINGS", "ELN_SETTINGS");
    //     assertNotNull(sample);
    //     // properties are empty
    //     assertEquals(sample.getProperties().size(), 0);

    //     // test space before update
    //     Space space = TestUtils.getSpace(v3api, sessionToken, "ELN_SETTINGS");
    //     assertEquals(space.getDescription(), "ELN Settings");

    //     // test project before update
    //     Project project = TestUtils.getProject(v3api, sessionToken, "DEFAULT_PROJECT");
    //     assertEquals(project.getDescription(), "Default Project");

    //     // test experiment before update
    //     Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "DEFAULT_EXPERIMENT", "DEFAULT_PROJECT", "ELN_SETTINGS");
    //     assertEquals(experiment.getProperties().size(), 1);
    //     assertEquals(experiment.getProperties().containsKey("$NAME"), true);
    //     assertEquals(experiment.getProperties().get("$NAME"), "Default Experiment");

    //     TestUtils.createFrom(v3api, sessionToken, UpdateMode.UPDATE_IF_EXISTS, Paths.get(FilenameUtils.concat(FILES_DIR, GENERAL_ELN_SETTINGS_UPDATE)));

    //     // test sample after update
    //     sample = TestUtils.getSample(v3api, sessionToken, "GENERAL_ELN_SETTINGS", "ELN_SETTINGS");
    //     assertNotNull(sample);
    //     // properties have been updated
    //     assertEquals(sample.getProperties().size(), 1);
    //     assertEquals(sample.getProperties().containsKey("$ELN_SETTINGS"), true);
    //     assertEquals(sample.getProperties().get("$ELN_SETTINGS"), "{}");

    //     // test space after update
    //     space = TestUtils.getSpace(v3api, sessionToken, "ELN_SETTINGS");
    //     // test space before update
    //     assertEquals(space.getDescription(), "ELN Settings Updated");

    //     // test project after update
    //     project = TestUtils.getProject(v3api, sessionToken, "DEFAULT_PROJECT");
    //     assertEquals(project.getDescription(), "Default Project Updated");

    //     // test experiment after update
    //     experiment = TestUtils.getExperiment(v3api, sessionToken, "DEFAULT_EXPERIMENT", "DEFAULT_PROJECT", "ELN_SETTINGS");
    //     assertEquals(experiment.getProperties().size(), 1);
    //     assertEquals(experiment.getProperties().containsKey("$NAME"), true);
    //     assertEquals(experiment.getProperties().get("$NAME"), "Default Experiment Updated");
    // }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfSamplesSpaceProjectDoesntExist() throws IOException {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLES_SPACE_PROJECT_EXPERIMENT_ELSEWHERE)));
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfMandatoryPropertyIsMissing() throws IOException {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, MANDATORY_FIELD_MISSING)));
    }

}
