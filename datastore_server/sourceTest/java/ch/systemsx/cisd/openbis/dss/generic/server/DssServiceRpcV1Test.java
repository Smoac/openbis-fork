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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.api.v1.shared.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DssServiceRpcV1Test extends AbstractFileSystemTestCase
{
    private Mockery context;

    private DssServiceRpcGeneric rpcService;

    private IEncapsulatedOpenBISService openBisService;

    private static final String SESSION_TOKEN = "DummySessionToken";

    private static final String DATA_SET_CODE = "code";

    private static final String DB_INSTANCE_UUID = "UUID";

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        context = new Mockery();
        openBisService = context.mock(IEncapsulatedOpenBISService.class);
        rpcService = new DssServiceRpcGeneric(openBisService);
        rpcService.setStoreDirectory(workingDirectory);
        initializeDirectories();
    }

    private void initializeDirectories() throws IOException
    {
        File location =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, DATA_SET_CODE,
                        DB_INSTANCE_UUID);
        if (!location.mkdirs())
            return;

        createDummyFile(location, "foo.txt", 100);

        File dummyDir = new File(location, "stuff/");
        dummyDir.mkdir();

        createDummyFile(dummyDir, "bar.txt", 110);
    }

    /**
     * Create a dummy file of size <code>length</code> bytes.
     */
    private void createDummyFile(File dir, String name, int length) throws IOException
    {
        File dummyFile = new File(dir, name);
        dummyFile.createNewFile();
        PrintWriter out = new PrintWriter(dummyFile);
        for (int i = 0; i < length; ++i)
        {
            out.append('a');
        }
        out.flush();
        out.close();
    }

    private DatabaseInstance getDatabaseInstance()
    {
        final DatabaseInstance homeDatabaseInstance = new DatabaseInstance();
        homeDatabaseInstance.setCode("TEST");
        homeDatabaseInstance.setUuid(DB_INSTANCE_UUID);
        return homeDatabaseInstance;
    }

    private void setupStandardExpectations()
    {
        final DatabaseInstance homeDatabaseInstance = getDatabaseInstance();

        context.checking(new Expectations()
            {
                {
                    allowing(openBisService).checkDataSetAccess(SESSION_TOKEN, DATA_SET_CODE);
                    one(openBisService).getHomeDatabaseInstance();
                    will(returnValue(homeDatabaseInstance));
                }
            });
    }

    @Test
    public void testDataSetListingNonRecursive()
    {
        setupStandardExpectations();
        FileInfoDssDTO[] fileInfos =
                rpcService.listFilesForDataSet(SESSION_TOKEN, DATA_SET_CODE, "/", false);
        assertEquals(2, fileInfos.length);
        int dirCount = 0;
        int fileIndex = 0;
        int i = 0;
        for (FileInfoDssDTO fileInfo : fileInfos)
        {
            if (fileInfo.isDirectory())
            {
                ++dirCount;
            } else
            {
                fileIndex = i;
            }
            ++i;
        }
        assertEquals(1, dirCount);
        FileInfoDssDTO fileInfo = fileInfos[fileIndex];
        assertEquals("/foo.txt", fileInfo.getPathInDataSet());
        assertEquals("foo.txt", fileInfo.getPathInListing());
        assertEquals(100, fileInfo.getFileSize());

        context.assertIsSatisfied();
    }

    @Test
    public void testDataSetListingRecursive()
    {
        setupStandardExpectations();
        FileInfoDssDTO[] fileInfos =
                rpcService.listFilesForDataSet(SESSION_TOKEN, DATA_SET_CODE, "/", true);
        assertEquals(3, fileInfos.length);
        int dirCount = 0;
        int fileCount = 0;
        int[] fileIndices = new int[2];
        int i = 0;
        for (FileInfoDssDTO fileInfo : fileInfos)
        {
            if (fileInfo.isDirectory())
            {
                ++dirCount;
            } else
            {
                fileIndices[fileCount++] = i;
            }
            ++i;
        }
        assertEquals(1, dirCount);
        for (i = 0; i < 2; ++i)
        {
            FileInfoDssDTO fileInfo = fileInfos[fileIndices[i]];
            if ("/foo.txt".equals(fileInfo.getPathInDataSet()))
            {
                assertEquals("foo.txt", fileInfo.getPathInListing());
                assertEquals(100, fileInfo.getFileSize());
            } else if ("/stuff/bar.txt".equals(fileInfo.getPathInDataSet()))
            {
                assertEquals("stuff/bar.txt", fileInfo.getPathInListing());
                assertEquals(110, fileInfo.getFileSize());
            } else
            {
                fail("Received unexpected file.");
            }
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testDataSetListingOfChild()
    {
        setupStandardExpectations();
        FileInfoDssDTO[] fileInfos =
                rpcService.listFilesForDataSet(SESSION_TOKEN, DATA_SET_CODE, "/stuff/", false);
        assertEquals(1, fileInfos.length);
        int dirCount = 0;
        int fileIndex = 0;
        int i = 0;
        for (FileInfoDssDTO fileInfo : fileInfos)
        {
            if (fileInfo.isDirectory())
            {
                ++dirCount;
            } else
            {
                fileIndex = i;
            }
            ++i;
        }
        assertEquals(0, dirCount);
        FileInfoDssDTO fileInfo = fileInfos[fileIndex];
        assertEquals("/stuff/bar.txt", fileInfo.getPathInDataSet());
        assertEquals("bar.txt", fileInfo.getPathInListing());
        assertEquals(110, fileInfo.getFileSize());

        context.assertIsSatisfied();
    }

    @Test
    public void testDataSetListingOfRelativeChild()
    {
        setupStandardExpectations();
        FileInfoDssDTO[] fileInfos =
                rpcService.listFilesForDataSet(SESSION_TOKEN, DATA_SET_CODE, "stuff/", false);
        assertEquals(1, fileInfos.length);
        FileInfoDssDTO fileInfo = fileInfos[0];
        assertEquals("/stuff/bar.txt", fileInfo.getPathInDataSet());
        assertEquals("bar.txt", fileInfo.getPathInListing());

        context.assertIsSatisfied();
    }

    @Test
    public void testDataSetListingOfFile()
    {
        setupStandardExpectations();
        FileInfoDssDTO[] fileInfos =
                rpcService
                        .listFilesForDataSet(SESSION_TOKEN, DATA_SET_CODE, "stuff/bar.txt", false);
        assertEquals(1, fileInfos.length);
        FileInfoDssDTO fileInfo = fileInfos[0];
        assertEquals("/stuff/bar.txt", fileInfo.getPathInDataSet());
        assertEquals("bar.txt", fileInfo.getPathInListing());

        context.assertIsSatisfied();
    }

    @Test
    public void testInaccessibleDataSetListing()
    {
        context.checking(new Expectations()
            {
                {
                    one(openBisService).checkDataSetAccess(SESSION_TOKEN, DATA_SET_CODE);
                    will(throwException(new UserFailureException("Data set not accessible")));
                }
            });

        try
        {
            rpcService.listFilesForDataSet(SESSION_TOKEN, DATA_SET_CODE, "/", true);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException ex)
        {
            // correct
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testDataSetListingWithSneakyPath()
    {
        setupStandardExpectations();

        try
        {
            rpcService.listFilesForDataSet(SESSION_TOKEN, DATA_SET_CODE, "../", true);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e)
        {
            // correct
        }

        try
        {
            rpcService.listFilesForDataSet(SESSION_TOKEN, DATA_SET_CODE, "/../../", true);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e)
        {
            // correct
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testDataSetFileRetrieval() throws IOException
    {
        setupStandardExpectations();
        FileInfoDssDTO[] fileInfos =
                rpcService
                        .listFilesForDataSet(SESSION_TOKEN, DATA_SET_CODE, "stuff/bar.txt", false);
        assertEquals(1, fileInfos.length);

        InputStream is =
                rpcService.getFileForDataSet(SESSION_TOKEN, DATA_SET_CODE, fileInfos[0]
                        .getPathInDataSet());

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        int readChar;
        int charCount = 0;
        while ((readChar = reader.read()) >= 0)
        {
            // Wrote many 'a' characters into the file
            assertEquals(97, readChar);
            ++charCount;
        }

        assertEquals(fileInfos[0].getFileSize(), charCount);

        context.assertIsSatisfied();
    }
}
