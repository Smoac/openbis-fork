package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;

public class SearchFileTest extends AbstractFileTest
{

    @Test
    public void allFilesOfGivenDatasetsAreReturned() throws Exception
    {
        DataSetFileSearchCriteria sc = new DataSetFileSearchCriteria();
        sc.withDataSet().withPermId().thatEquals(dataSetCode);

        List<DataSetFile> searchFiles = dss.searchFiles(sessionToken, sc);

        assertThat(searchFiles, containsAll(filesAndDirectories));
        /* directory structure [dataset id]/original/[root folder] */
        assertThat(searchFiles.size(), is(filesAndDirectories.size() + 3));
    }

}