package ch.ethz.sis.openbis.systemtests;

import static org.testng.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.openbis.generic.OpenBIS;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.systemtests.common.AbstractIntegrationTest;
import ch.ethz.sis.openbis.systemtests.shuffling.TestShuffling;

public class IntegrationShufflingTest extends AbstractIntegrationTest
{

    private static final String ENTITY_CODE_PREFIX = "SHUFFLING_TEST_";

    private static final long WAITING_TIME_FOR_SHUFFLING = 3000L;

    private Experiment experimentShuffledToShare2;

    private Experiment experimentShuffledToShare3;

    @BeforeSuite @Override public void beforeSuite() throws Exception
    {
        super.beforeSuite();

        OpenBIS openBIS = createOpenBIS();
        openBIS.login(INSTANCE_ADMIN, PASSWORD);

        Space space = createSpace(openBIS, "SHUFFLE");
        Project project = createProject(openBIS, space.getPermId(), "SHUFFLE");

        experimentShuffledToShare2 = createExperiment(openBIS, project.getPermId(), "SHUFFLE_TO_SHARE_2");
        experimentShuffledToShare3 = createExperiment(openBIS, project.getPermId(), "SHUFFLE_TO_SHARE_3");

        log("Created experiment " + experimentShuffledToShare2.getIdentifier() + " with perm id " + experimentShuffledToShare2.getPermId());
        log("Created experiment " + experimentShuffledToShare3.getIdentifier() + " with perm id " + experimentShuffledToShare3.getPermId());

        openBIS.logout();
    }

    @Test
    public void testAFSDataIsShuffledByAFS() throws Exception
    {
        OpenBIS openBIS = createOpenBIS();
        openBIS.login(INSTANCE_ADMIN, PASSWORD);

        // create data at AFS (should be stored in the incoming share i.e. 1)
        Sample sample = createSample(openBIS, experimentShuffledToShare2.getPermId(), ENTITY_CODE_PREFIX + UUID.randomUUID());

        openBIS.getAfsServerFacade()
                .write(sample.getPermId().getPermId(), "test-file.txt", 0L, "test-content".getBytes());

        assertDataSetShareAndSizeInDB(openBIS, sample.getPermId().getPermId(), 1, null);
        assertDataSetExistsInStore(openBIS, sample.getPermId().getPermId(), 1, true);
        assertDataSetExistsInStore(openBIS, sample.getPermId().getPermId(), 2, false);

        Thread.sleep(WAITING_TIME_FOR_SHUFFLING);

        assertDataSetShareAndSizeInDB(openBIS, sample.getPermId().getPermId(), 2, (long) "test-content".getBytes().length);
        assertDataSetExistsInStore(openBIS, sample.getPermId().getPermId(), 2, true);
        assertDataSetExistsInStore(openBIS, sample.getPermId().getPermId(), 1, false);
    }

    @Test
    public void testDSSDataIsNotShuffledByAFS() throws Exception
    {
        // TODO
    }

    @Test
    public void testDataIsLockedDuringShuffling() throws Exception
    {
        // TODO
    }

    @Test
    public void testFailedShufflingIsCleanedUp() throws Exception
    {
        try
        {
            TestShuffling.getDataSetMover().getChecksumProvider().setFailWithException(new RuntimeException("Test checksum exception"));

            OpenBIS openBIS = createOpenBIS();
            openBIS.login(INSTANCE_ADMIN, PASSWORD);

            Sample sample = createSample(openBIS, experimentShuffledToShare2.getPermId(), ENTITY_CODE_PREFIX + UUID.randomUUID());

            openBIS.getAfsServerFacade()
                    .write(sample.getPermId().getPermId(), "test-file.txt", 0L, "test-content".getBytes());

            assertDataSetShareAndSizeInDB(openBIS, sample.getPermId().getPermId(), 1, null);
            assertDataSetExistsInStore(openBIS, sample.getPermId().getPermId(), 1, true);
            assertDataSetExistsInStore(openBIS, sample.getPermId().getPermId(), 2, false);

            Thread.sleep(WAITING_TIME_FOR_SHUFFLING);

            assertDataSetShareAndSizeInDB(openBIS, sample.getPermId().getPermId(), 1, (long) "test-content".getBytes().length);
            assertDataSetExistsInStore(openBIS, sample.getPermId().getPermId(), 1, true);
            assertDataSetExistsInStore(openBIS, sample.getPermId().getPermId(), 2, false);
        } finally
        {
            TestShuffling.getDataSetMover().getChecksumProvider().setFailWithException(null);
        }
    }

    private static void assertDataSetShareAndSizeInDB(OpenBIS openBIS, String dataSetCode, int shareId, Long size)
    {
        IDataSetId dataSetId = new DataSetPermId(dataSetCode);

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withPhysicalData();

        DataSet dataSet = openBIS.getDataSets(List.of(dataSetId), fetchOptions).get(dataSetId);

        assertEquals(dataSet.getPhysicalData().getShareId(), String.valueOf(shareId));
        assertEquals(dataSet.getPhysicalData().getSize(), size);

    }

    private static void assertDataSetExistsInStore(OpenBIS openBIS, String dataSetCode, int shareId, boolean exists)
    {
        IDataSetId dataSetId = new DataSetPermId(dataSetCode);

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withPhysicalData();

        DataSet dataSet = openBIS.getDataSets(List.of(dataSetId), fetchOptions).get(dataSetId);

        String storageRoot = getAfsServerConfiguration().getStringProperty(AtomicFileSystemServerParameter.storageRoot);
        Path dataSetFolder = Paths.get(storageRoot, String.valueOf(shareId), dataSet.getPhysicalData().getLocation());

        assertEquals(Files.exists(dataSetFolder), exists);
    }

}
