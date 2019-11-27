package ch.ethz.sis.openbis.systemtest.plugin.excelimport;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
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

import java.io.IOException;
import java.nio.file.Paths;

import static org.testng.Assert.assertNotNull;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional(transactionManager = "transaction-manager")
@Rollback
public class ImportFromCsvTest extends AbstractImportTest {

    @Autowired
    private IApplicationServerInternalApi v3api;

    private static final String TEST_USER = "test";

    private static final String PASSWORD = "password";

    private static final String FULL_TYPES_CSV = "csv/types.csv";

    @BeforeClass
    public void setupClass() throws IOException {
        String f = ImportExperimentTypesTest.class.getName().replace(".", "/");
        FILES_DIR = f.substring(0, f.length() - ImportExperimentTypesTest.class.getSimpleName().length()) + "/test_files/";
    }

    @Test
    @DirtiesContext
    public void testNormalExperimentTypesAreCreated() throws Exception {
        // GIVEN
        TestUtils.createFromCsv(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, FULL_TYPES_CSV)));
        // WHEN
        Vocabulary detection = TestUtils.getVocabulary(v3api, sessionToken, "DETECTION");
        // THEN
        assertNotNull(detection);
    }


}
