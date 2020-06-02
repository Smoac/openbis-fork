package ch.ethz.sis.openbis.systemtest.plugin.excelimport;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional(transactionManager = "transaction-manager")
@Rollback
public class ImportFromExcelTest extends AbstractImportTest {

    @Autowired
    private IApplicationServerInternalApi v3api;

    private static final String WITH_BLANKS = "xls/with_blanks.xlsx";

    @BeforeClass
    public void setupClass() throws IOException {
        String f = ImportExperimentTypesTest.class.getName().replace(".", "/");
        FILES_DIR = f.substring(0, f.length() - ImportExperimentTypesTest.class.getSimpleName().length()) + "/test_files/";
    }

    @Test
    @DirtiesContext
    public void testFileWithManyBlankRowsWasParsed() throws Exception {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, WITH_BLANKS)));

        ExperimentType propertyType = TestUtils.getExperimentType(v3api, sessionToken, "COLLECTION");
        assertNotNull(propertyType); // the last line on the first sheet was read
        assertEquals(propertyType.getPropertyAssignments().size(), 2);
        assertEquals(propertyType.getPropertyAssignments().get(1).getPropertyType().getCode(), "LAST_ROW_EXP_TYPE");

        Sample sample = TestUtils.getSample(v3api, sessionToken, "LAST_SAMPLE", "TEST_SPACE");
        assertNotNull(sample); // the last line on the second sheet was read
    }
}