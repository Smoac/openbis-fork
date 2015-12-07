package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.datasetfile.download.DataSetFileDownload;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.datasetfile.download.DataSetFileDownloadReader;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.datasetfile.id.DataSetFilePermId;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.datasetfile.id.IDataSetFileId;

public class DownloadFileTest extends AbstractFileTest
{

    @Test
    public void testDownloadAllFiles() throws Exception
    {
        IDataSetFileId root = new DataSetFilePermId(new DataSetPermId(dataSetCode));
        DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();

        Map<String, String> contentMap = download(Arrays.asList(root), options);

        assertEquals(filesAndDirectories.size() + 3, contentMap.size());
        assertEmptyContent(contentMap, getPath("subdir1"));
        assertContent(contentMap, getPath("subdir1/file3.txt"));
    }

    @Test
    public void testDownloadMultipleFiles() throws Exception
    {
        IDataSetFileId file1 = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("file1.txt"));
        IDataSetFileId file6 = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("subdir3/file6.txt"));

        DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();

        Map<String, String> contentMap = download(Arrays.asList(file1, file6), options);

        assertEquals(2, contentMap.size());
        assertContent(contentMap, getPath("file1.txt"));
        assertContent(contentMap, getPath("subdir3/file6.txt"));
    }

    @Test
    public void testDownloadFolderRecursively() throws Exception
    {
        IDataSetFileId folder = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("subdir1/subdir2"));
        DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();
        options.setRecursive(true);

        Map<String, String> contentMap = download(Arrays.asList(folder), options);

        assertEquals(2, contentMap.size());
        assertEmptyContent(contentMap, getPath("subdir1/subdir2"));
        assertContent(contentMap, getPath("subdir1/subdir2/file5.txt"));
    }

    @Test
    public void testDownloadFolderOnly() throws Exception
    {
        IDataSetFileId folder = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("subdir1/subdir2"));
        DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();
        options.setRecursive(false);

        Map<String, String> contentMap = download(Arrays.asList(folder), options);

        assertEquals(1, contentMap.size());
        assertEmptyContent(contentMap, getPath("subdir1/subdir2"));
    }

    @Test(expectedExceptions = { IllegalStateException.class }, expectedExceptionsMessageRegExp = "Input stream no longer valid")
    public void testDownloadStreamReturnedByOldRead() throws Exception
    {
        IDataSetFileId file1 = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("file1.txt"));
        IDataSetFileId file2 = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("file2.txt"));
        DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();

        InputStream stream = dss.downloadFiles(sessionToken, Arrays.asList(file1, file2), options);
        DataSetFileDownloadReader reader = new DataSetFileDownloadReader(stream);

        DataSetFileDownload download1 = reader.read();
        reader.read();

        IOUtils.toString(download1.getInputStream());
    }

    @Test
    public void testDownloadStreamReturnedAfterUnconsumedReads() throws Exception
    {
        IDataSetFileId file1 = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("file1.txt"));
        IDataSetFileId file2 = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("file2.txt"));
        IDataSetFileId file3 = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("subdir1/file3.txt"));

        DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();

        InputStream stream = dss.downloadFiles(sessionToken, Arrays.asList(file1, file2, file3), options);
        DataSetFileDownloadReader reader = new DataSetFileDownloadReader(stream);

        reader.read();
        reader.read();
        DataSetFileDownload download3 = reader.read();

        assertContent(IOUtils.toString(download3.getInputStream()), getPath("subdir1/file3.txt"));
    }

    private Map<String, String> download(@SuppressWarnings("hiding")
    List<IDataSetFileId> files, DataSetFileDownloadOptions options)
    {
        try
        {
            InputStream stream = dss.downloadFiles(sessionToken, files, options);
            DataSetFileDownloadReader reader = new DataSetFileDownloadReader(stream);

            Map<String, String> contentMap = new HashMap<>();
            DataSetFileDownload download = null;

            while ((download = reader.read()) != null)
            {
                contentMap.put(download.getDataSetFile().getPath(), IOUtils.toString(download.getInputStream()));
            }

            return contentMap;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void assertEmptyContent(Map<String, String> contentMap, String filePath)
    {
        assertEquals("", contentMap.get(filePath));
    }

    private void assertContent(Map<String, String> contentMap, String filePath)
    {
        assertContent(contentMap.get(filePath), filePath);
    }

    private void assertContent(String content, String filePath)
    {
        String relativePath = filePath.substring(getPathPrefix().length());
        assertEquals("file content of " + relativePath, content);
    }

    private String getPath(String path)
    {
        return getPathPrefix() + path;
    }

    private String getPathPrefix()
    {
        return "original/" + dataSetCode + "/";
    }
}