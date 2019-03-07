package ch.ethz.sis.openbis.systemtest.plugin.excelimport;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional(transactionManager = "transaction-manager")
@Rollback
public class ImportExperimentsTest extends AbstractImportTest
{
    @Autowired
    private IApplicationServerInternalApi v3api;

    private static final String TEST_USER = "test";

    private static final String PASSWORD = "password";

    private static final String EXPERIMENT_XLS = "experiments/all_inside.xls";

    private static final String EXPERIMENTS_ALL_ELSEWHERE = "experiments/all_elsewhere.xls";

    private static final String EXPERIMENTS_WITH_TYPE_ELSEWHERE = "experiments/experiment_type_elsewhere.xls";

    private static final String EXPERIMENTS_NO_CODE = "experiments/no_code.xls";

    private static final String EXPERIMENTS_WITH_NON_MANDATORY_PROPERTY_MISSING = "experiments/no_non_mandatory_property.xls";

    private static final String EXPERIMENTS_NO_PROJECT_ATTRIBUTE = "experiments/no_project.xls";

    private static final String EXPERIMENTS_WITH_SPACE_AND_PROJECT_ELSEWHERE = "experiments/space_and_project_elsewhere.xls";

    private static final String EXPERIMENTS_SPACE_ELSEWHERE = "experiments/space_elsewhere.xls";

    private static final String EXPERIMENTS_WITH_TYPE_AND_SPACE_ELSEWHERE = "experiments/type_and_space_elsewhere.xls";

    private static final String EXPERIMENTS_WITH_MANDATORY_PROPERTY_MISSING = "experiments/with_mandatory_property_missing.xls";

    private static final String EXPERIMENTS_WITH_MANDATORY_PROPERTY_PRESENT = "experiments/with_mandatory_property.xls";

    private static final String EXPERIMENTS_PROPERTIES_COLUMNS_AS_LABELS = "experiments/with_properties_as_labels.xls";

    private static final String EXPERIMENTS_PROPERTIES_COLUMNS_AS_LABELS_TYPE_ON_SERVER = "experiments/with_properties_as_labels_type_elsewhere.xls";

    private static final String SPACE = "experiments/space.xls";

    private static final String PROJECT = "experiments/project.xls";

    private static final String EXPERIMENT_TYPE = "experiments/experiment_type.xls";

    private static final String EXPERIMENT_UPDATE = "experiments/update.xls";

    private static String FILES_DIR;

    private String sessionToken;

    @BeforeClass
    public void setupClass() throws IOException
    {
        String f = ImportExperimentsTest.class.getName().replace(".", "/");
        FILES_DIR = f.substring(0, f.length() - ImportExperimentsTest.class.getSimpleName().length()) + "/test_files/";
    }

    @BeforeMethod
    public void beforeTest()
    {
        sessionToken = v3api.login(TEST_USER, PASSWORD);
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreated() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_XLS)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedSecondExperiment() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_XLS)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT2", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT2");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Other Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "Random string");
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedWithEverythingOnServer() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_TYPE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROJECT)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_ALL_ELSEWHERE)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedWithEverythingOnServerAndInXls() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_TYPE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROJECT)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_XLS)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfExperimentTypeDoesntExist() throws IOException
    {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROJECT)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_ALL_ELSEWHERE)));
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfProjectDoesntExist() throws IOException
    {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_TYPE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_ALL_ELSEWHERE)));
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedWithTypeOnServer() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_TYPE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_WITH_TYPE_ELSEWHERE)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedWithTypeOnServerAndInXls() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_TYPE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROJECT)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_WITH_TYPE_ELSEWHERE)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfExperimentNoCode() throws IOException
    {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_NO_CODE)));
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedWhenNonMandatoryPropertiesAreNotProvided() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_WITH_NON_MANDATORY_PROPERTY_MISSING)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), null);
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfExperimentNoProject() throws IOException
    {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_NO_PROJECT_ATTRIBUTE)));
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedWithSpaceAndProjectOnServer() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROJECT)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_WITH_SPACE_AND_PROJECT_ELSEWHERE)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedWithSpaceOnServer() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_SPACE_ELSEWHERE)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedWithTypeAndSpaceOnServer() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_TYPE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_WITH_TYPE_AND_SPACE_ELSEWHERE)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfMandatoryPropertyMissing() throws IOException
    {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_WITH_MANDATORY_PROPERTY_MISSING)));
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedIfMandatoryPropertyArePresent() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_WITH_MANDATORY_PROPERTY_PRESENT)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedWhenPropertiesAreAddressedByLabelsWithTypeInXls() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_PROPERTIES_COLUMNS_AS_LABELS)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

    @Test
    @DirtiesContext
    public void testExperimentsAreCreatedWhenPropertiesAreAddressedByLabelsWithTypeOnServer() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_TYPE)));
        TestUtils.createFrom(v3api, sessionToken,
                Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENTS_PROPERTIES_COLUMNS_AS_LABELS_TYPE_ON_SERVER)));
        // WHEN
        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
        // THEN
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT");
        assertEquals(experiment.getProject().getCode(), "TEST_PROJECT");
        assertEquals(experiment.getProperties().get("$NAME"), "Value");
        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "OBJECT_TYPE");
    }

//    @Test
//    @DirtiesContext
//    public void testExperimentsUpdate() throws Exception
//    {
//        // GIVEN
//        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_XLS)));
//        // WHEN
//        TestUtils.createFrom(v3api, sessionToken, TestUtils.getDynamicPluginMap(), UpdateMode.UPDATE_IF_EXISTS,
//                Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_UPDATE)));
//        Experiment experiment = TestUtils.getExperiment(v3api, sessionToken, "TEST_EXPERIMENT", "TEST_PROJECT", "TEST_SPACE");
//        // THEN
//        assertEquals(experiment.getProperties().get("$NAME"), "NameUpdate");
//        assertEquals(experiment.getProperties().get("DEFAULT_OBJECT_TYPE"), "DefaultObjectTypeUpdate");
//    }

}
