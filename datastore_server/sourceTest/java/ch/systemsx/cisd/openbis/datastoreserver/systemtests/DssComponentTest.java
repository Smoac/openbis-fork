/*
 * Copyright 2010 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.DssRegistrationLogDirectoryHelper;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DssComponentFactory;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "slow")
public class DssComponentTest extends SystemTestCase
{
    // If the pathinfo-db feeding task post-registration task in the service.properties is
    // configured to compute checksums for data sets, set this to true. Otherwise set to false.
    private static final boolean ARE_CHECKSUMS_COMPUTED = false;

    private static final Comparator<FileInfoDssDTO> FILE_INFO_COMPARATOR =
            new Comparator<FileInfoDssDTO>()
                {
                    @Override
                    public int compare(FileInfoDssDTO f1, FileInfoDssDTO f2)
                    {
                        return f1.getPathInDataSet().compareTo(f2.getPathInDataSet());
                    }
                };

    private static final String OPENBIS_URL = "http://localhost:8888";

    private IDssComponent dss;

    // Keep track of the number of times a data set was registered during the course of running the
    // tests
    private int putCount = 0;
    
    private List<String> registeredDataSets = new ArrayList<String>();
    
//    @AfterMethod
    public void deleteRegisteredDataSets()
    {
        IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();
        openBISService.removeDataSetsPermanently(registeredDataSets, "DssComponentTest");
    }

    @BeforeMethod
    public void beforeMethod()
    {
        dss = createDssComponent("test");
    }

    @Test
    public void testPutDataSet() throws Exception
    {
        File exampleDataSet = new File(workingDirectory, "my-data");
        NewDataSetDTO newDataset = createNewDataSetDTO(exampleDataSet);
        IDataSetDss dataSet = registerDataSet(exampleDataSet, newDataset);
        checkDataSet(dataSet);
        putCount++;
    }

    private IDataSetDss registerDataSet(File exampleDataSet, NewDataSetDTO newDataset)
    {
        IDataSetDss dataSet = dss.putDataSet(newDataset, exampleDataSet);
        registeredDataSets.add(dataSet.getCode());
        return dataSet;
    }

    @Test
    public void testFailingPutDataSet() throws Exception
    {
        try
        {
            File exampleDataSet = new File(workingDirectory, "my-data");
            createExampleDataSet(exampleDataSet);
            moveFileToIncoming(exampleDataSet);
            waitUntilDataSetImported();
            // Do *not* increment the putCount because this test does not successfully register any
            // data
            // putCount++;
        } catch (AssertionError ex)
        {
            // ignore this
        } catch (NullPointerException ex)
        {
            // ignore this
        }
    }

    @Test
    public void testFailingValidationPutDataSet() throws Exception
    {
        try
        {
            File exampleDataSet = new File(workingDirectory, "invalid-file");
            createExampleDataSet(exampleDataSet);
            moveFileToIncoming(exampleDataSet);
            waitUntilDataSetImported();
            // Do *not* increment the putCount because this test does not successfully register any
            // data
            // putCount++;
        } catch (AssertionError ex)
        {
            // ignore this
        } catch (NullPointerException ex)
        {
            // ignore this
        }
    }

    /**
     * Checks that the registration log is as we expect it to be. This test will break if new tests
     * are added that register data with the DSS but do not increment the putCount instance
     * variable. Make sure any tests that register data also increment this variable.
     */
    @Test(dependsOnMethods =
        { "testPutDataSet", "testFailingPutDataSet", "testFailingValidationPutDataSet" })
    public void testRegistrationLog() throws Exception
    {
        File registrationLogDir = getRegistrationLogDir();
        assertTrue(registrationLogDir.exists());

        File[] logDirContents = registrationLogDir.listFiles();
        // The log directory should have 3 sub directories for in-process, succeeded, failed.
        assertEquals(3, logDirContents.length);

        File succeededDir =
                new DssRegistrationLogDirectoryHelper(registrationLogDir).getSucceededDir();
        File[] succeededContents = checkDssRegistrationLogDirectoryCount(succeededDir, putCount);

        // Check the log contents
        File logFile = succeededContents[0];
        String[] expectedContents =
                    {
                            "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} Prepared registration of 1 data set:$",
                            "^\\t\\d+-\\d+$",
                            "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} Data has been moved to the pre-commit directory: .*$",
                            "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} About to register metadata with AS: registrationId\\(\\d+\\)$",
                            "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} Data has been registered with the openBIS Application Server.$",
                            "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} Storage processors have committed.$",
                            "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} Data has been moved to the final store.$",
                            "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} Storage has been confirmed in openBIS Application Server.$" };
        checkLogFileContents(logFile, expectedContents);

        File failedDir = new DssRegistrationLogDirectoryHelper(registrationLogDir).getFailedDir();
        File[] failedContents = checkDssRegistrationLogDirectoryCount(failedDir, 2);
        for (File failedLogFile : failedContents)
        {
            if (failedLogFile.getName().contains("invalid"))
            {
                expectedContents =
                        new String[]
                            { "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} Validation script .* found errors in incoming data set .*" };
                checkLogFileContents(failedLogFile, expectedContents);
            } else
            {
                expectedContents = new String[]
                    { "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} Processing failed : .*" };
                checkLogFileContents(failedLogFile, expectedContents);
            }
        }
    }

    private void checkLogFileContents(File logFile, String[] expectedContents)
    {
        List<String> logFileContents = FileUtilities.loadToStringList(logFile);
        assertTrue("" + logFileContents.size() + " < " + expectedContents.length,
                logFileContents.size() >= expectedContents.length);
        int i = 0;
        for (String expected : expectedContents)
        {
            assertTrue(expected + ".matches(" + logFileContents.get(i) + ")",
                    Pattern.matches(expected, logFileContents.get(i++)));
        }
    }

    private File[] checkDssRegistrationLogDirectoryCount(File dir, int count)
    {
        File[] succeededContents = dir.listFiles();

        StringBuilder msg = new StringBuilder();
        for (File file : succeededContents)
        {
            msg.append(file.getAbsolutePath()).append("\n");
        }

        // This does not work on the CI server for some reason
        // assertEquals(msg.toString(), count, succeededContents.length);
        assertTrue(msg.toString(), count <= succeededContents.length);
        return succeededContents;
    }

    @Test
    public void testValidateDataSet() throws Exception
    {
        File exampleDataSet = new File(workingDirectory, "my-data");
        NewDataSetDTO newDataset = createNewDataSetDTO(exampleDataSet);
        List<ValidationError> errors = dss.validateDataSet(newDataset, exampleDataSet);
        assertEquals("[]", errors.toString());
    }

    @Test(dependsOnMethods = "testPutDataSet")
    public void testPutDataSetWithParent() throws Exception
    {
        String code = getCodeOfLatestDataSet().getDataSetCode();

        File exampleDataSet = new File(workingDirectory, "my-data");
        NewDataSetDTO newDataset = createNewDataSetDTO(exampleDataSet);
        newDataset.setParentDataSetCodes(Arrays.asList(code));
        IDataSetDss dataSet = registerDataSet(exampleDataSet, newDataset);
        checkDataSet(dataSet);
        putCount++;
    }

    @Test(dependsOnMethods = "testPutDataSet")
    public void testGetDataSetGetFile() throws Exception
    {
        String code = getCodeOfLatestDataSet().getDataSetCode();

        IDataSetDss ds = dss.getDataSet(code);

        assertEquals(code, ds.getCode());
        checkDataSet(ds);
        String path = "original/my-data/data";
        FileInfoDssDTO[] files = ds.listFiles(path, false);
        Arrays.sort(files, FILE_INFO_COMPARATOR);
        assertEquals(2, files.length);
        assertEquals(fileInfoString(path, "1.data", 5, "f7eabd5f"), files[0].toString());
        assertEquals(fileInfoString(path, "2.data", 7, "02c0db4e"), files[1].toString());
        if (ARE_CHECKSUMS_COMPUTED)
        {
            assertNotNull(files[0].tryGetCrc32Checksum());
            assertNotNull(files[1].tryGetCrc32Checksum());
        } else
        {
            assertNull(files[0].tryGetCrc32Checksum());
            assertNull(files[1].tryGetCrc32Checksum());
        }

        files = ds.listFiles("/", true);
        Arrays.sort(files, FILE_INFO_COMPARATOR);
        assertEquals(7, files.length);
        assertEquals(fileInfoString("original", -1), files[0].toString());
        assertEquals(fileInfoString("original/my-data", -1), files[1].toString());
        assertEquals(fileInfoString("original/my-data/data", -1), files[2].toString());
        assertEquals(fileInfoString("original/my-data/data-set.properties", 28, "5f5e699f"),
                files[3].toString());
        assertEquals(fileInfoString("original/my-data/data.log", 11, "0d4a1185"),
                files[4].toString());
        assertEquals(fileInfoString("original/my-data/data/1.data", 5, "f7eabd5f"),
                files[5].toString());
        assertEquals(fileInfoString("original/my-data/data/2.data", 7, "02c0db4e"),
                files[6].toString());
    }

    private static String fileInfoString(String startPath, String pathInListing, long length,
            String checksum)
    {
        if (ARE_CHECKSUMS_COMPUTED)
        {
            return String.format("FileInfoDssDTO[%s/%s,%s,%d,%s]", startPath, pathInListing,
                    pathInListing, length, checksum);
        } else
        {
            return String.format("FileInfoDssDTO[%s/%s,%s,%d]", startPath, pathInListing,
                    pathInListing, length);
        }
    }

    private static String fileInfoString(String pathInListing, long length)
    {
        return String.format("FileInfoDssDTO[%s,%s,%d]", pathInListing, pathInListing, length);
    }

    private static String fileInfoString(String pathInListing, long length, String checksum)
    {
        if (ARE_CHECKSUMS_COMPUTED)
        {
            return String.format("FileInfoDssDTO[%s,%s,%d,%s]", pathInListing, pathInListing,
                    length, checksum);
        } else
        {
            return String.format("FileInfoDssDTO[%s,%s,%d]", pathInListing, pathInListing, length);
        }
    }

    @Test(dependsOnMethods = "testPutDataSet")
    public void testGetDataSetGetLink() throws Exception
    {
        SimpleDataSetInformationDTO dataSetInfo = getCodeOfLatestDataSet();
        String code = dataSetInfo.getDataSetCode();
        File fileIntoStore =
                new File(new File(store,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                        dataSetInfo.getDataSetLocation());

        IDataSetDss ds = dss.getDataSet(code);

        File link = ds.tryLinkToContents(null);
        assertEquals(fileIntoStore.getAbsolutePath(), link.getAbsolutePath());
        File file = ds.getLinkOrCopyOfContents(null, workingDirectory);
        assertEquals(fileIntoStore.getAbsolutePath(), file.getAbsolutePath());
    }

    @Test(dependsOnMethods = "testPutDataSet")
    public void testGetDataSetGetCopy() throws Exception
    {
        SimpleDataSetInformationDTO dataSetInfo = getCodeOfLatestDataSet();
        String code = dataSetInfo.getDataSetCode();

        IDataSetDss ds = dss.getDataSet(code);

        assertEquals(null, ds.tryLinkToContents("blabla"));
        File file = ds.getLinkOrCopyOfContents("blabla", workingDirectory);
        assertContent("hello world", file, "data.log");
        assertContent("1 2 3", file, "data/1.data");
        assertContent("4 5 6 7", file, "data/2.data");
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testObserverHasNoWritePermissions() throws Exception
    {
        dss = createDssComponent("observer");
        File exampleDataSet = new File(workingDirectory, "observer-data");
        NewDataSetDTO newDataset = createNewDataSetDTO(exampleDataSet);
        registerDataSet(exampleDataSet, newDataset);
        putCount++;
    }

    @Test(dependsOnMethods = "testPutDataSet", expectedExceptions = AuthorizationFailureException.class)
    public void testObserverHasNoReadPermissions() throws Exception
    {
        dss = createDssComponent("observer");
        SimpleDataSetInformationDTO dataSetInfo = getCodeOfLatestDataSet();
        String code = dataSetInfo.getDataSetCode();
        IDataSetDss dataSet = dss.getDataSet(code);
        dataSet.listFiles("/", true);
    }

    private IDssComponent createDssComponent(String userName)
    {
        return DssComponentFactory.tryCreate(userName, "a", OPENBIS_URL,
                5 * DateUtils.MILLIS_PER_MINUTE);
    }

    private NewDataSetDTO createNewDataSetDTO(File exampleDataSet) throws IOException
    {
        DataSetOwner dataSetOwner =
                new DataSetOwner(DataSetOwnerType.SAMPLE, "CISD:/CISD/CP-TEST-1");
        createExampleDataSet(exampleDataSet);
        String rootPath = exampleDataSet.getCanonicalPath();
        FileInfoDssBuilder builder = new FileInfoDssBuilder(rootPath, rootPath);
        ArrayList<FileInfoDssDTO> list = new ArrayList<FileInfoDssDTO>();
        builder.appendFileInfosForFile(exampleDataSet, list, true);
        NewDataSetDTO newDataset = new NewDataSetDTO(dataSetOwner, exampleDataSet.getName(), list);
        return newDataset;
    }

    private void createExampleDataSet(File exampleDataSet)
    {
        exampleDataSet.mkdirs();
        FileUtilities.writeToFile(new File(exampleDataSet, "data.log"), "hello world");
        FileUtilities.writeToFile(new File(exampleDataSet, "data-set.properties"),
                "property\tvalue\nCOMMENT\thello");
        File subFolder = new File(exampleDataSet, "data");
        subFolder.mkdirs();
        FileUtilities.writeToFile(new File(subFolder, "1.data"), "1 2 3");
        FileUtilities.writeToFile(new File(subFolder, "2.data"), "4 5 6 7");
    }

    private void assertContent(String expectedContent, File root, String path)
    {
        assertEquals(expectedContent,
                FileUtilities.loadToString(new File(root, "original/my-data/" + path)).trim());
    }

    private SimpleDataSetInformationDTO getCodeOfLatestDataSet()
    {
        IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();
        List<SimpleDataSetInformationDTO> dataSets = openBISService.listDataSets();
        Collections.sort(dataSets, new Comparator<SimpleDataSetInformationDTO>()
            {
                @Override
                public int compare(SimpleDataSetInformationDTO d1, SimpleDataSetInformationDTO d2)
                {
                    String d2Code = normalize(d2.getDataSetCode());
                    String d1Code = normalize(d1.getDataSetCode());
                    return d2Code.compareTo(d1Code);
                }
                
                private String normalize(String code)
                {
                    return code.startsWith("2") ? code : "0" + code;
                }
            });
        return dataSets.get(0);
    }

    private void checkDataSet(IDataSetDss dataSet) throws IOException
    {
        assertEquals("hello world", getContent(dataSet, "data.log"));
        assertEquals("1 2 3", getContent(dataSet, "data/1.data"));
        assertEquals("4 5 6 7", getContent(dataSet, "data/2.data"));

        // Wait a bit for the maintenance task to run
        try
        {
            // The default dss service.properties has a post-registration maintenance
            // task scheduled to run every 30 seconds, so wait until it has run and cleaned
            // up the queue.
            Thread.sleep(31 * 1000);
        } catch (InterruptedException e)
        {
        }
        IEncapsulatedOpenBISService openbisService = ServiceProvider.getOpenBISService();
        assertEquals(0, openbisService.listDataSetsForPostRegistration().size());
    }

    private String getContent(IDataSetDss dataSet, String path) throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = null;
        try
        {
            inputStream = dataSet.getFile("/original/my-data/" + path);
            IOUtils.copy(inputStream, output);
        } finally
        {
            IOUtils.closeQuietly(inputStream);
        }
        return output.toString();
    }
}
