package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.dss.api.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.entity.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.search.FileSearchCriterion;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.SystemTestCase;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;

public class SearchFileTest extends SystemTestCase
{

    @Override
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming-simple-dataset-test");
    }

    @Override
    protected int dataSetImportWaitDurationInSeconds()
    {
        return 280;
    }

    @Test
    public void allFilesOfGivenDatasetsAreReturned() throws Exception
    {
        IDataStoreServerApi dss = (IDataStoreServerApi) ServiceProvider.getDssServiceV3().getService();

        String dataSetCode = UUID.randomUUID().toString().toUpperCase();

        File dataSetDir = new File(workingDirectory, dataSetCode);
        dataSetDir.mkdirs();
        FileUtilities.writeToFile(new File(dataSetDir, "file1.txt"), "hello world");
        FileUtilities.writeToFile(new File(dataSetDir, "file2.txt"), "hello world");
        moveFileToIncoming(dataSetDir);
        waitUntilDataSetImported();
        waitUntilIndexUpdaterIsIdle();
        waitUntilDataSetPostRegistrationCompleted(dataSetCode);

        IGeneralInformationService generalInformationService = ServiceProvider.getGeneralInformationService();
        String sessionToken = generalInformationService.tryToAuthenticateForAllServices("test", "test");

        FileSearchCriterion sc = new FileSearchCriterion();
        sc.withDataSet().withPermId().thatEquals(dataSetCode);

        List<DataSetFile> searchFiles = dss.searchFiles(sessionToken, sc);
        AssertionUtil.assertSize(searchFiles, 1);
    }

}
