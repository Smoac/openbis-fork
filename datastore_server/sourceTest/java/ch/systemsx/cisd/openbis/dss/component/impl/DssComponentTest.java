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

package ch.systemsx.cisd.openbis.dss.component.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.openbis.dss.component.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.rpc.client.IDssServiceRpcFactory;
import ch.systemsx.cisd.openbis.dss.rpc.shared.DssServiceRpcInterface;
import ch.systemsx.cisd.openbis.dss.rpc.shared.FileInfoDss;
import ch.systemsx.cisd.openbis.dss.rpc.shared.FileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.rpc.shared.IDssServiceRpcV1;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DssComponentTest extends AbstractFileSystemTestCase
{
    private Mockery context;

    private IETLLIMSService openBisService;

    private IDssServiceRpcFactory dssServiceFactory;

    private DssComponent dssComponent;

    private File randomDataFile;

    private static final String DUMMY_SESSSION_TOKEN = "DummySessionToken";

    private static final String DUMMY_DSS_URL = "http://localhost/datastore_server";

    public DssComponentTest()
    {

    }

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        context = new Mockery();
        openBisService = context.mock(IETLLIMSService.class);
        dssServiceFactory = context.mock(IDssServiceRpcFactory.class);
        dssComponent = new DssComponent(openBisService, dssServiceFactory);
        randomDataFile = getFileWithRandomData(1);
    }

    @Test
    public void testLogin()
    {
        final SessionContextDTO session = getDummySession();

        context.checking(new Expectations()
            {
                {
                    one(openBisService).tryToAuthenticate("foo", "bar");
                    will(returnValue(session));
                }
            });

        dssComponent.login("foo", "bar");

        context.assertIsSatisfied();
    }

    @Test
    public void testListDataSetFiles() throws IOException
    {
        setupExpectations();

        dssComponent.login("foo", "bar");
        IDataSetDss dataSetProxy = dssComponent.getDataSet("DummyDataSetCode");
        FileInfoDss[] fileInfos = dataSetProxy.listFiles("/", true);
        System.err.println(Arrays.toString(fileInfos));
        assertEquals(1, fileInfos.length);

        context.assertIsSatisfied();
    }

    @Test
    public void testGetFileContents() throws IOException
    {
        setupExpectations();

        dssComponent.login("foo", "bar");
        IDataSetDss dataSetProxy = dssComponent.getDataSet("DummyDataSetCode");
        FileInfoDss[] fileInfos = dataSetProxy.listFiles("/", true);
        FileInfoDss fileFileInfo = null;
        for (FileInfoDss fid : fileInfos)
        {
            if (fid.isDirectory() == false)
            {
                fileFileInfo = fid;
                break;
            }
        }
        if (fileFileInfo == null)
        {
            fail("Could not find file info for random file");
            return;
        }

        InputStream is = dataSetProxy.getFile(fileFileInfo.getPath());
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        int charCount = 0;
        while (reader.read() >= 0)
        {
            ++charCount;
        }

        assertEquals(fileFileInfo.getFileSize(), charCount);
    }

    private void setupExpectations() throws IOException
    {
        final SessionContextDTO session = getDummySession();
        final ExternalData dataSetExternalData = new ExternalData();
        DataStore dataStore = new DataStore();
        dataStore.setDownloadUrl(DUMMY_DSS_URL);
        dataSetExternalData.setDataStore(dataStore);
        final IDssServiceRpcV1 dssService = context.mock(IDssServiceRpcV1.class);
        ArrayList<FileInfoDss> list = new ArrayList<FileInfoDss>();
        FileInfoDssBuilder builder = new FileInfoDssBuilder(workingDirectory.getCanonicalPath());
        builder.appendFileInfosForFile(workingDirectory, list, true);
        final FileInfoDss[] fileInfos = new FileInfoDss[list.size()];
        list.toArray(fileInfos);

        final DssServiceRpcInterface[] ifaces = new DssServiceRpcInterface[1];
        final DssServiceRpcInterface iface = new DssServiceRpcInterface();
        iface.setInterfaceName("V1");
        iface.setInterfaceUrlSuffix("/rpc/v1");
        ifaces[0] = iface;

        context.checking(new Expectations()
            {
                {
                    final String dataSetCode = "DummyDataSetCode";

                    one(openBisService).tryToAuthenticate("foo", "bar");
                    will(returnValue(session));
                    allowing(openBisService).tryGetDataSet(DUMMY_SESSSION_TOKEN, dataSetCode);
                    will(returnValue(dataSetExternalData));
                    allowing(dssServiceFactory).getSupportedInterfaces(DUMMY_DSS_URL, false);
                    will(returnValue(ifaces));
                    allowing(dssServiceFactory).getService(iface, IDssServiceRpcV1.class,
                            DUMMY_DSS_URL, false);
                    will(returnValue(dssService));
                    allowing(dssService).listFilesForDataSet(DUMMY_SESSSION_TOKEN, dataSetCode,
                            "/", true);
                    will(returnValue(fileInfos));
                    allowing(dssService).getFileForDataSet(DUMMY_SESSSION_TOKEN, dataSetCode,
                            "/random.txt");
                    will(returnValue(new FileInputStream(randomDataFile)));
                }
            });
    }

    private SessionContextDTO getDummySession()
    {
        final SessionContextDTO session = new SessionContextDTO();
        session.setSessionToken(DUMMY_SESSSION_TOKEN);
        return session;
    }

    private File getFileWithRandomData(long sizeInKB) throws IOException
    {
        File file = new File(workingDirectory, "random.txt");
        Random random = new Random();
        FileOutputStream outputStream = null;
        try
        {
            outputStream = new FileOutputStream(file);
            byte[] bytes = new byte[1024];
            for (int i = 0; i < sizeInKB; i++)
            {
                random.nextBytes(bytes);
                outputStream.write(bytes);
            }
        } catch (IOException ex)
        {
            throw ex;
        } finally
        {
            if (outputStream != null)
            {
                IOUtils.closeQuietly(outputStream);
            }
        }

        return file;
    }

}
