package ch.ethz.sis.openbis.systemtests;

import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.UUID;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

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
    public void testShuffle() throws Exception
    {
        OpenBIS openBIS = createOpenBIS();
        openBIS.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);

        openBIS.login(INSTANCE_ADMIN, PASSWORD);

        // create data at AFS (should be stored in the incoming share i.e. 1)
        Sample sample = createSample(openBIS, experimentShuffledToShare2.getPermId(), ENTITY_CODE_PREFIX + UUID.randomUUID());

        openBIS.getAfsServerFacade()
                .write(sample.getPermId().getPermId(), "test-file.txt", 0L, "test-content".getBytes());

        assertDataSetShareAndSize(openBIS, sample.getPermId().getPermId(), 1, null);

        Thread.sleep(WAITING_TIME_FOR_SHUFFLING);

        assertDataSetShareAndSize(openBIS, sample.getPermId().getPermId(), 2, (long) "test-content".getBytes().length);
    }

    private void assertDataSetShareAndSize(OpenBIS openBIS, String dataSetCode, int shareId, Long size)
    {
        IDataSetId dataSetId = new DataSetPermId(dataSetCode);

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withPhysicalData();

        DataSet dataSet = openBIS.getDataSets(List.of(dataSetId), fetchOptions).get(dataSetId);

        assertEquals(dataSet.getPhysicalData().getShareId(), String.valueOf(shareId));
        assertEquals(dataSet.getPhysicalData().getSize(), size);
    }

}
