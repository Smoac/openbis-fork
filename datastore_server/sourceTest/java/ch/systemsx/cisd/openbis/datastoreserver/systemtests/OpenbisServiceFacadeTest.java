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
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.client.api.v1.OpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author Chandrasekhar Ramakrishnan
 */
@Test(groups = "slow")
public class OpenbisServiceFacadeTest extends SystemTestCase
{
    private static final Comparator<FileInfoDssDTO> FILE_INFO_COMPARATOR =
            new Comparator<FileInfoDssDTO>()
                {
                    public int compare(FileInfoDssDTO f1, FileInfoDssDTO f2)
                    {
                        return f1.getPathInDataSet().compareTo(f2.getPathInDataSet());
                    }
                };

    private static final String OPENBIS_URL = "http://localhost:8888";

    private IOpenbisServiceFacade serviceFacade;

    private File store;

    @BeforeMethod
    public void beforeMethod()
    {
        store = new File(rootDir, "store");
        store.mkdirs();
        serviceFacade = createServiceFacade("test");
    }

    @Test
    public void testPutDataSet() throws Exception
    {
        File exampleDataSet = new File(workingDirectory, "my-data");
        NewDataSetDTO newDataset = createNewDataSetDTO(exampleDataSet);
        DataSet dataSet = serviceFacade.putDataSet(newDataset, exampleDataSet);
        checkDataSet(dataSet);
    }

    @Test(dependsOnMethods = "testPutDataSet")
    public void testPutDataSetWithParent() throws Exception
    {
        String code = getCodeOfLatestDataSet().getDataSetCode();

        File exampleDataSet = new File(workingDirectory, "my-data");
        NewDataSetDTO newDataset = createNewDataSetDTO(exampleDataSet);
        newDataset.setParentDataSetCodes(Arrays.asList(code));
        DataSet dataSet = serviceFacade.putDataSet(newDataset, exampleDataSet);
        checkDataSet(dataSet);

        // We need to take a different route to get the data set we just registered to check if it
        // has a parent.
        List<Sample> samples = serviceFacade.getSamples(Arrays.asList("/CISD/CP-TEST-1"));
        List<DataSet> dataSets =
                serviceFacade.listDataSets(samples, EnumSet.allOf(Connections.class));

        List<String> parentCodes = null;
        for (DataSet sampleDataSet : dataSets)
        {
            if (dataSet.getCode().equals(sampleDataSet.getCode()))
            {
                parentCodes = sampleDataSet.getParentCodes();
                break;
            }
        }

        assertEquals(Arrays.asList(code), parentCodes);
    }

    @Test(dependsOnMethods = "testPutDataSet", expectedExceptions = IllegalArgumentException.class)
    public void testFailureAccessingParentFromSearchResult() throws Exception
    {
        String code = getCodeOfLatestDataSet().getDataSetCode();
        DataSet dataSet = serviceFacade.getDataSet(code);
        dataSet.getParentCodes();
    }

    @Test(dependsOnMethods = "testPutDataSet")
    public void testGetDataSetGetFile() throws Exception
    {
        DataSet ds = getLatestDataSet();

        String code = getCodeOfLatestDataSet().getDataSetCode();
        assertEquals(code, ds.getCode());
        checkDataSet(ds);
        String path = "original/my-data/data";
        FileInfoDssDTO[] files = ds.listFiles(path, false);
        Arrays.sort(files, FILE_INFO_COMPARATOR);
        assertEquals(2, files.length);
        assertEquals(fileInfoString(path, "1.data", 5), files[0].toString());
        assertEquals(fileInfoString(path, "2.data", 7), files[1].toString());

        files = ds.listFiles("/", true);
        Arrays.sort(files, FILE_INFO_COMPARATOR);
        assertEquals(7, files.length);
        assertEquals(fileInfoString("original", -1), files[0].toString());
        assertEquals(fileInfoString("original/my-data", -1), files[1].toString());
        assertEquals(fileInfoString("original/my-data/data", -1), files[2].toString());
        assertEquals(fileInfoString("original/my-data/data-set.properties", 28),
                files[3].toString());
        assertEquals(fileInfoString("original/my-data/data.log", 11), files[4].toString());
        assertEquals(fileInfoString("original/my-data/data/1.data", 5), files[5].toString());
        assertEquals(fileInfoString("original/my-data/data/2.data", 7), files[6].toString());
    }

    private static String fileInfoString(String startPath, String pathInListing, long length)
    {
        return String.format("FileInfoDssDTO[%s/%s,%s,%d]", startPath, pathInListing,
                pathInListing, length);
    }

    private static String fileInfoString(String pathInListing, long length)
    {
        return String.format("FileInfoDssDTO[%s,%s,%d]", pathInListing, pathInListing, length);
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

        DataSet ds = serviceFacade.getDataSet(code);

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

        DataSet ds = serviceFacade.getDataSet(code);

        assertEquals(null, ds.tryLinkToContents("blabla"));
        File file = ds.getLinkOrCopyOfContents("blabla", workingDirectory);
        assertContent("hello world", file, "data.log");
        assertContent("1 2 3", file, "data/1.data");
        assertContent("4 5 6 7", file, "data/2.data");
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testObserverHasNoWritePermissions() throws Exception
    {
        serviceFacade = createServiceFacade("observer");
        File exampleDataSet = new File(workingDirectory, "observer-data");
        NewDataSetDTO newDataset = createNewDataSetDTO(exampleDataSet);
        serviceFacade.putDataSet(newDataset, exampleDataSet);
    }

    @Test(dependsOnMethods = "testPutDataSet", expectedExceptions = AuthorizationFailureException.class)
    public void testObserverHasNoReadPermissions() throws Exception
    {
        serviceFacade = createServiceFacade("observer");
        SimpleDataSetInformationDTO dataSetInfo = getCodeOfLatestDataSet();
        String code = dataSetInfo.getDataSetCode();
        DataSet dataSet = serviceFacade.getDataSet(code);
        dataSet.listFiles("/", true);
    }

    private IOpenbisServiceFacade createServiceFacade(String userName)
    {
        return OpenbisServiceFacadeFactory.tryCreate(userName, "a", OPENBIS_URL,
                5 * DateUtils.MILLIS_PER_MINUTE);
    }

    private NewDataSetDTO createNewDataSetDTO(File exampleDataSet) throws IOException
    {
        DataSetOwner dataSetOwner =
                new DataSetOwner(DataSetOwnerType.SAMPLE, "CISD:/CISD/CP-TEST-1");
        exampleDataSet.mkdirs();
        FileUtilities.writeToFile(new File(exampleDataSet, "data.log"), "hello world");
        FileUtilities.writeToFile(new File(exampleDataSet, "data-set.properties"),
                "property\tvalue\nCOMMENT\thello");
        File subFolder = new File(exampleDataSet, "data");
        subFolder.mkdirs();
        FileUtilities.writeToFile(new File(subFolder, "1.data"), "1 2 3");
        FileUtilities.writeToFile(new File(subFolder, "2.data"), "4 5 6 7");
        String rootPath = exampleDataSet.getCanonicalPath();
        FileInfoDssBuilder builder = new FileInfoDssBuilder(rootPath, rootPath);
        ArrayList<FileInfoDssDTO> list = new ArrayList<FileInfoDssDTO>();
        builder.appendFileInfosForFile(exampleDataSet, list, true);
        NewDataSetDTO newDataset = new NewDataSetDTO(dataSetOwner, exampleDataSet.getName(), list);
        return newDataset;
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
                public int compare(SimpleDataSetInformationDTO d1, SimpleDataSetInformationDTO d2)
                {
                    return d2.getDataSetCode().compareTo(d1.getDataSetCode());
                }
            });
        return dataSets.get(0);
    }

    private DataSet getLatestDataSet()
    {
        SearchCriteria sc = new SearchCriteria();
        List<DataSet> dataSets = serviceFacade.searchForDataSets(sc);
        Collections.sort(dataSets, new Comparator<DataSet>()
            {
                public int compare(DataSet o1, DataSet o2)
                {
                    // sort decreasing
                    return o2.getCode().compareTo(o1.getCode());
                }
            });
        return dataSets.get(0);
    }

    private void checkDataSet(DataSet dataSet) throws IOException
    {
        assertEquals("hello world", getContent(dataSet, "data.log"));
        assertEquals("1 2 3", getContent(dataSet, "data/1.data"));
        assertEquals("4 5 6 7", getContent(dataSet, "data/2.data"));
    }

    private String getContent(DataSet dataSet, String path) throws IOException
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
