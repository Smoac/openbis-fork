package ch.ethz.sis.openbis.systemtests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.reflect.Method;
import java.util.UUID;

import org.apache.logging.log4j.Level;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.afsserver.server.common.TestLogger;
import ch.ethz.sis.openbis.generic.OpenBIS;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.systemtests.common.AbstractIntegrationTest;
import ch.ethz.sis.openbis.systemtests.shuffling.TestSegmentedStoreShufflingTask;
import ch.ethz.sis.openbis.systemtests.shuffling.TestSegmentedStoreShufflingTask.TestChecksumProvider;

public class IntegrationShufflingTest extends AbstractIntegrationTest
{

    private static final String ENTITY_CODE_PREFIX = "SHUFFLING_TEST_";

    private static final String TEST_FILE_NAME = "test-file.txt";

    private static final String TEST_FILE_CONTENT = "test-content";

    private Experiment experimentShuffledToShare2;

    private Experiment experimentShuffledToShare3;

    @BeforeClass public void beforeClass() throws Exception
    {
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

    @BeforeMethod
    public void beforeMethod(Method method)
    {
        super.beforeMethod(method);
        TestLogger.startLogRecording(Level.TRACE);
    }

    @AfterMethod
    public void afterMethod(Method method) throws Exception
    {
        super.afterMethod(method);
        TestLogger.stopLogRecording();
    }

    @Test
    public void testAFSDataIsShuffledByAFS() throws Exception
    {
        OpenBIS openBIS = createOpenBIS();
        openBIS.login(INSTANCE_ADMIN, PASSWORD);

        // create data at AFS (should be stored in the incoming share i.e. 1)
        Sample sample = createSample(openBIS, experimentShuffledToShare2.getPermId(), ENTITY_CODE_PREFIX + UUID.randomUUID());

        openBIS.getAfsServerFacade()
                .write(sample.getPermId().getPermId(), TEST_FILE_NAME, 0L, TEST_FILE_CONTENT.getBytes());

        assertDataExistsInStoreInShare(sample.getPermId().getPermId(), true, 1);
        assertDataExistsInStoreInShare(sample.getPermId().getPermId(), false, 2);

        TestChecksumProvider checksumProvider = new TestChecksumProvider();
        TestSegmentedStoreShufflingTask.executeOnce(checksumProvider);

        assertDataExistsInStoreInShare(sample.getPermId().getPermId(), true, 2);
        assertDataExistsInStoreInShare(sample.getPermId().getPermId(), false, 1);
    }

    @Test
    public void testDSSDataIsNotShuffledByAFS() throws Exception
    {
        OpenBIS openBIS = createOpenBIS();
        openBIS.login(INSTANCE_ADMIN, PASSWORD);

        // create data at DSS (should be stored in the incoming share i.e. 1)
        DataSet dataSet = createDataSet(openBIS, experimentShuffledToShare2.getPermId(), ENTITY_CODE_PREFIX + UUID.randomUUID(), TEST_FILE_NAME,
                TEST_FILE_CONTENT.getBytes());

        assertDataExistsInStoreInShare(dataSet.getPermId().getPermId(), true, 1);
        assertDataExistsInStoreInShare(dataSet.getPermId().getPermId(), false, 2);

        TestChecksumProvider checksumProvider = new TestChecksumProvider();
        TestSegmentedStoreShufflingTask.executeOnce(checksumProvider);

        assertDataExistsInStoreInShare(dataSet.getPermId().getPermId(), true, 1);
        assertDataExistsInStoreInShare(dataSet.getPermId().getPermId(), false, 2);
    }

    @Test
    public void testDataIsLockedDuringShuffling() throws Exception
    {
        OpenBIS openBIS = createOpenBIS();
        openBIS.login(INSTANCE_ADMIN, PASSWORD);

        Sample sample = createSample(openBIS, experimentShuffledToShare2.getPermId(), ENTITY_CODE_PREFIX + UUID.randomUUID());

        openBIS.getAfsServerFacade()
                .write(sample.getPermId().getPermId(), TEST_FILE_NAME, 0L, TEST_FILE_CONTENT.getBytes());

        assertDataExistsInStoreInShare(sample.getPermId().getPermId(), true, 1);

        Thread shufflingThread = new Thread(() ->
        {
            TestChecksumProvider checksumProvider = new TestChecksumProvider();
            checksumProvider.setDelayByMillis(1000L);
            TestSegmentedStoreShufflingTask.executeOnce(checksumProvider);
        });
        shufflingThread.start();

        Thread.sleep(500L);
        try
        {
            byte[] content = openBIS.getAfsServerFacade()
                    .read(sample.getPermId().getPermId(), TEST_FILE_NAME, 0L, TEST_FILE_CONTENT.getBytes().length);
            assertEquals(new String(content), TEST_FILE_CONTENT);
            fail();
        } catch (Exception e)
        {
            assertTrue(e.getMessage().contains(TEST_FILE_NAME + " is currently being used"), e.getMessage());
        }

        shufflingThread.join();

        assertDataExistsInStoreInShare(sample.getPermId().getPermId(), true, 2);
    }

    @Test
    public void testFailedShufflingIsCleanedUp() throws Exception
    {
        OpenBIS openBIS = createOpenBIS();
        openBIS.login(INSTANCE_ADMIN, PASSWORD);

        Sample sample = createSample(openBIS, experimentShuffledToShare2.getPermId(), ENTITY_CODE_PREFIX + UUID.randomUUID());

        openBIS.getAfsServerFacade()
                .write(sample.getPermId().getPermId(), TEST_FILE_NAME, 0L, TEST_FILE_CONTENT.getBytes());

        assertDataExistsInStoreInShare(sample.getPermId().getPermId(), true, 1);
        assertDataExistsInStoreInShare(sample.getPermId().getPermId(), false, 2);

        TestChecksumProvider checksumProvider = new TestChecksumProvider();
        checksumProvider.setFailWithException(new RuntimeException("Test checksum exception"));
        TestSegmentedStoreShufflingTask.executeOnce(checksumProvider);

        assertDataExistsInStoreInShare(sample.getPermId().getPermId(), true, 1);
        assertDataExistsInStoreInShare(sample.getPermId().getPermId(), false, 2);
    }

}
