package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownload;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadReader;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.DataSetFilePermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;

public class DownloadFileTest extends AbstractFileTest
{

    @Override
    @BeforeClass
    protected void beforeClass() throws Exception
    {
        super.beforeClass();
        registerDataSet();
    }

    @Test
    public void testDownloadUnauthorized()
    {
        String spaceSessionToken = gis.tryToAuthenticateForAllServices(TEST_SPACE_USER, PASSWORD);

        InputStream stream = dss.downloadFiles(spaceSessionToken, Arrays.asList(new DataSetFilePermId(new DataSetPermId(dataSetCode))),
                new DataSetFileDownloadOptions());

        DataSetFileDownloadReader reader = new DataSetFileDownloadReader(stream);

        assertNull(reader.read());
    }

    @Test
    public void testDownloadAllFiles() throws Exception
    {
        IDataSetFileId root = new DataSetFilePermId(new DataSetPermId(dataSetCode));
        DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();
        options.setRecursive(true);

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

        String sessionToken = gis.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);
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

        String sessionToken = gis.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);
        InputStream stream = dss.downloadFiles(sessionToken, Arrays.asList(file1, file2, file3), options);
        DataSetFileDownloadReader reader = new DataSetFileDownloadReader(stream);

        reader.read();
        reader.read();
        DataSetFileDownload download3 = reader.read();

        assertContent(IOUtils.toString(download3.getInputStream()), getPath("subdir1/file3.txt"));
    }

    @Test
    public void testDownloadWithFileLengths() throws Exception
    {
        IDataSetFileId file1 = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("file1.txt"));
        IDataSetFileId file2 = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("file2.txt"));

        DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();

        String sessionToken = gis.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);
        InputStream stream = dss.downloadFiles(sessionToken, Arrays.asList(file1, file2), options);
        DataSetFileDownloadReader reader = new DataSetFileDownloadReader(stream);

        DataSetFileDownload download1 = reader.read();
        DataSetFileDownload download2 = reader.read();

        assertEquals(getContent(download1.getDataSetFile().getPath()).length(), download1.getDataSetFile().getFileLength());
        assertEquals(getContent(download2.getDataSetFile().getPath()).length(), download2.getDataSetFile().getFileLength());
    }

    private Map<String, String> download(@SuppressWarnings("hiding") List<IDataSetFileId> files, DataSetFileDownloadOptions options)
    {
        try
        {
            String sessionToken = gis.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);

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
        assertEquals(getContent(filePath), content);
    }

    private String getContent(String path)
    {
        String relativePath = path.substring(getPathPrefix().length());
        return "file content of " + relativePath;
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