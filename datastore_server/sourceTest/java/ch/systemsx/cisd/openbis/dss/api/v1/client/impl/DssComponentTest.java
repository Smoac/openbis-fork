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

package ch.systemsx.cisd.openbis.dss.api.v1.client.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceDTO;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceVersionDTO;
import ch.systemsx.cisd.openbis.dss.api.v1.client.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.api.v1.client.impl.DssComponent;
import ch.systemsx.cisd.openbis.dss.api.v1.shared.FileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.api.v1.shared.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.api.v1.shared.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.rpc.client.IDssServiceRpcFactory;
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
        dssComponent = new DssComponent(openBisService, dssServiceFactory, null);
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
        FileInfoDssDTO[] fileInfos = dataSetProxy.listFiles("/", true);
        assertEquals(1, fileInfos.length);

        context.assertIsSatisfied();
    }

    @Test
    public void testListDataSetFilesNoLogin() throws IOException
    {
        dssComponent = new DssComponent(openBisService, dssServiceFactory, DUMMY_SESSSION_TOKEN);
        setupExpectationsNoLogin();
        IDataSetDss dataSetProxy = dssComponent.getDataSet("DummyDataSetCode");
        FileInfoDssDTO[] fileInfos = dataSetProxy.listFiles("/", true);
        assertEquals(1, fileInfos.length);

        context.assertIsSatisfied();
    }

    @Test
    public void testUnsupportedInterface() throws IOException
    {
        setupExpectations("Some Server Interface", true);

        dssComponent.login("foo", "bar");
        try
        {
            dssComponent.getDataSet("DummyDataSetCode");
            fail("Should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException e)
        {
            // correct behavior
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testGetFileContents() throws IOException
    {
        setupExpectations();

        dssComponent.login("foo", "bar");
        IDataSetDss dataSetProxy = dssComponent.getDataSet("DummyDataSetCode");
        FileInfoDssDTO[] fileInfos = dataSetProxy.listFiles("/", true);
        FileInfoDssDTO fileFileInfo = null;
        for (FileInfoDssDTO fid : fileInfos)
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

        InputStream is = dataSetProxy.getFile(fileFileInfo.getPathInDataSet());
        int byteCount = 0;
        while (is.read() >= 0)
        {
            ++byteCount;
        }

        assertEquals(fileFileInfo.getFileSize(), byteCount);
    }

    private void setupExpectations() throws IOException
    {
        setupExpectations(IDssServiceRpcGeneric.DSS_SERVICE_NAME, true);
    }

    private void setupExpectationsNoLogin() throws IOException
    {
        setupExpectations(IDssServiceRpcGeneric.DSS_SERVICE_NAME, false);
    }

    private void setupExpectations(String serviceName, final boolean needsLogin) throws IOException
    {
        final SessionContextDTO session = getDummySession();
        final ExternalData dataSetExternalData = new ExternalData();
        DataStore dataStore = new DataStore();
        dataStore.setDownloadUrl(DUMMY_DSS_URL);
        dataSetExternalData.setDataStore(dataStore);
        final IDssServiceRpcGeneric dssService = context.mock(IDssServiceRpcGeneric.class);
        ArrayList<FileInfoDssDTO> list = new ArrayList<FileInfoDssDTO>();
        FileInfoDssBuilder builder =
                new FileInfoDssBuilder(workingDirectory.getCanonicalPath(), workingDirectory
                        .getCanonicalPath());
        builder.appendFileInfosForFile(workingDirectory, list, true);
        final FileInfoDssDTO[] fileInfos = new FileInfoDssDTO[list.size()];
        list.toArray(fileInfos);

        final ArrayList<RpcServiceInterfaceDTO> ifaces = new ArrayList<RpcServiceInterfaceDTO>(1);
        final RpcServiceInterfaceDTO iface = new RpcServiceInterfaceDTO();
        final RpcServiceInterfaceVersionDTO ifaceVersion = new RpcServiceInterfaceVersionDTO();
        ifaceVersion.setInterfaceName(serviceName);
        ifaceVersion.setInterfaceUrlSuffix("/rpc/v1");
        ifaceVersion.setInterfaceMajorVersion(1);
        ifaceVersion.setInterfaceMinorVersion(0);

        iface.setInterfaceName(serviceName);
        iface.addVersion(ifaceVersion);

        ifaces.add(iface);

        context.checking(new Expectations()
            {
                {
                    final String dataSetCode = "DummyDataSetCode";

                    if (needsLogin)
                    {
                        one(openBisService).tryToAuthenticate("foo", "bar");
                        will(returnValue(session));
                    }
                    allowing(openBisService).tryGetDataSet(DUMMY_SESSSION_TOKEN, dataSetCode);
                    will(returnValue(dataSetExternalData));
                    allowing(dssServiceFactory).getSupportedInterfaces(DUMMY_DSS_URL, false);
                    will(returnValue(ifaces));
                    allowing(dssServiceFactory).getService(ifaceVersion,
                            IDssServiceRpcGeneric.class, DUMMY_DSS_URL, false);
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
