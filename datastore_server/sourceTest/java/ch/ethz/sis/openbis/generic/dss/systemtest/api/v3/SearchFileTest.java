package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.dss.api.v3.dto.entity.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.search.DataSetFileSearchCriterion;

public class SearchFileTest extends AbstractFileTest
{

    @Test
    public void allFilesOfGivenDatasetsAreReturned() throws Exception
    {
        DataSetFileSearchCriterion sc = new DataSetFileSearchCriterion();
        sc.withDataSet().withPermId().thatEquals(dataSetCode);

        List<DataSetFile> searchFiles = dss.searchFiles(sessionToken, sc);

        assertThat(searchFiles, containsAll(filesAndDirectories));
        /* directory structure [dataset id]/original/[root folder] */
        assertThat(searchFiles.size(), is(filesAndDirectories.size() + 3));
    }

}