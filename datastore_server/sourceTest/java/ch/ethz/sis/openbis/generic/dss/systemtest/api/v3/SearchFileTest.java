package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;

public class SearchFileTest extends AbstractFileTest
{

    @Override
    @BeforeClass
    protected void beforeClass() throws Exception
    {
        super.beforeClass();
        registerDataSet();
    }

    @Test
    public void allFilesOfGivenDatasetsAreReturned() throws Exception
    {
        DataSetFileSearchCriteria sc = new DataSetFileSearchCriteria();
        sc.withDataSet().withPermId().thatEquals(dataSetCode);

        String sessionToken = gis.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);
        SearchResult<DataSetFile> searchResult = dss.searchFiles(sessionToken, sc, new DataSetFileFetchOptions());
        List<DataSetFile> searchFiles = searchResult.getObjects();

        assertThat(searchFiles, containsAll(filesAndDirectories));
        /* directory structure [dataset id]/original/[root folder] */
        assertThat(searchFiles.size(), is(filesAndDirectories.size() + 3));

        for (DataSetFile dataSetFile : searchFiles)
        {
            if (dataSetFile.isDirectory() == false)
            {
                assertEquals(
                        dataSetFile.getPath().length() - 1 - dataSetFile.getPath().indexOf('/', "original/.".length()) + "file content of ".length(),
                        dataSetFile.getFileLength());
                System.out.println(dataSetFile+":"+dataSetFile.getChecksumCRC32());
            }
        }
    }
}