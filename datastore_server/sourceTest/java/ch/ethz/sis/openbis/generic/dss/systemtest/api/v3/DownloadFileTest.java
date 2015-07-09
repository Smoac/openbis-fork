package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.dss.api.v3.dto.download.DataSetFileDownload;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.download.DataSetFileDownloadReader;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.id.datasetfile.DataSetFilePermId;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.id.datasetfile.IDataSetFileId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;

public class DownloadFileTest extends AbstractFileTest
{

    @Test
    public void downloadAllFiles() throws Exception
    {
        IDataSetFileId root = new DataSetFilePermId(new DataSetPermId(dataSetCode));
        DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();

        InputStream stream = dss.downloadFiles(sessionToken, Arrays.asList(root), options);
        DataSetFileDownloadReader reader = new DataSetFileDownloadReader(stream);

        Map<String, String> contentMap = new HashMap<>();
        DataSetFileDownload download = null;

        while ((download = reader.read()) != null)
        {
            System.out.println("Downloaded file path: " + download.getDataSetFile().getPath());
            contentMap.put(download.getDataSetFile().getPath(), IOUtils.toString(download.getInputStream()));
        }

        assertEquals(filesAndDirectories.size() + 3, contentMap.size());
        assertEmptyContent(contentMap, "subdir1");
        assertContent(contentMap, "subdir1/file3.txt");
    }

    private void assertEmptyContent(Map<String, String> contentMap, String filePath)
    {
        String theFilePath = "original/" + dataSetCode + "/" + filePath;
        assertEquals("", contentMap.get(theFilePath));
    }

    private void assertContent(Map<String, String> contentMap, String filePath)
    {
        String theFilePath = "original/" + dataSetCode + "/" + filePath;
        assertEquals("file content of " + filePath, contentMap.get(theFilePath));
    }

}