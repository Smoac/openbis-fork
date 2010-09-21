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

package ch.systemsx.cisd.openbis.dss.client.api.v1.impl;

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
import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.api.IRpcService;
import ch.systemsx.cisd.common.api.IRpcServiceFactory;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceDTO;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceVersionDTO;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.impl.DssComponent;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDssServiceRpc;
import ch.systemsx.cisd.openbis.dss.generic.server.DssServiceRpcAuthorizationAdvisor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.DataSetFileDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.DataStoreApiUrlUtilities;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * A test of the DSS component.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssComponentTest extends AbstractFileSystemTestCase
{
    private static final String DUMMY_DATA_SET_CODE = "DummyDataSetCode";

    private Mockery context;

    private IETLLIMSService openBisService;

    private IRpcServiceFactory dssServiceFactory;

    private DssComponent dssComponent;

    private File randomDataFile;

    private static final String DUMMY_SESSSION_TOKEN = "DummySessionToken";

    private static final String DUMMY_DSS_URL =
            DataStoreApiUrlUtilities.getDataStoreUrlFromServerUrl("http://localhost/");

    private IDssServiceRpcGeneric dssService;

    @SuppressWarnings("unchecked")
    public static <T extends IRpcService> T getAdvisedDssService(final T service)
    {
        final Advisor advisor = new DssServiceRpcAuthorizationAdvisor();
        final BeanPostProcessor processor = new AbstractAutoProxyCreator()
            {
                private static final long serialVersionUID = 1L;

                //
                // AbstractAutoProxyCreator
                //
                @Override
                protected final Object[] getAdvicesAndAdvisorsForBean(final Class beanClass,
                        final String beanName, final TargetSource customTargetSource)
                        throws BeansException
                {
                    return new Object[]
                        { advisor };
                }
            };
        final Object proxy =
                processor.postProcessAfterInitialization(service, "proxy of "
                        + service.getClass().getName());
        return (T) proxy;
    }

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
        dssServiceFactory = context.mock(IRpcServiceFactory.class);
        dssComponent = new DssComponent(openBisService, dssServiceFactory, null);
        randomDataFile = getFileWithRandomData(1);
    }

    @AfterMethod
    public void tearDown()
    {
        // Clear the dss service
        dssService = null;
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
        IDataSetDss dataSetProxy = dssComponent.getDataSet(DUMMY_DATA_SET_CODE);
        FileInfoDssDTO[] fileInfos = dataSetProxy.listFiles("/", true);
        assertEquals(1, fileInfos.length);

        context.assertIsSatisfied();
    }

    @Test
    public void testListDataSetFilesNoLogin() throws IOException
    {
        dssComponent = new DssComponent(openBisService, dssServiceFactory, DUMMY_SESSSION_TOKEN);
        setupExpectationsNoLogin();
        IDataSetDss dataSetProxy = dssComponent.getDataSet(DUMMY_DATA_SET_CODE);
        FileInfoDssDTO[] fileInfos = dataSetProxy.listFiles("/", true);
        assertEquals(1, fileInfos.length);

        context.assertIsSatisfied();
    }

    @Test
    public void testListDataSetFilesUnauthorized() throws IOException
    {
        setupExpectations(false);
        dssComponent.login("foo", "bar");
        try
        {
            IDataSetDss dataSetProxy = dssComponent.getDataSet(DUMMY_DATA_SET_CODE);
            dataSetProxy.listFiles("/", true);
            fail("Unauthorized access to data set should have thrown an exception.");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals("Authorization failure: User does not have access to data set "
                    + DUMMY_DATA_SET_CODE + ".", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testUnsupportedInterface() throws IOException
    {
        setupExpectations("Some Server Interface", true, true);

        dssComponent.login("foo", "bar");
        try
        {
            dssComponent.getDataSet(DUMMY_DATA_SET_CODE);
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
        IDataSetDss dataSetProxy = dssComponent.getDataSet(DUMMY_DATA_SET_CODE);
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
        setupExpectations(true);
    }

    private void setupExpectations(boolean isDataSetAccessible) throws IOException
    {
        setupExpectations(IDssServiceRpcGeneric.DSS_SERVICE_NAME, true, isDataSetAccessible);
    }

    private void setupExpectationsNoLogin() throws IOException
    {
        setupExpectations(IDssServiceRpcGeneric.DSS_SERVICE_NAME, false, true);
    }

    private void setupExpectations(String serviceName, final boolean needsLogin,
            boolean isDataSetAccessible) throws IOException
    {
        final SessionContextDTO session = getDummySession();
        final ExternalData dataSetExternalData = new ExternalData();
        DataStore dataStore = new DataStore();
        dataStore.setDownloadUrl(DUMMY_DSS_URL);
        dataSetExternalData.setDataStore(dataStore);

        ArrayList<FileInfoDssDTO> list = new ArrayList<FileInfoDssDTO>();
        FileInfoDssBuilder builder =
                new FileInfoDssBuilder(workingDirectory.getCanonicalPath(), workingDirectory
                        .getCanonicalPath());
        builder.appendFileInfosForFile(workingDirectory, list, true);
        final FileInfoDssDTO[] fileInfos = new FileInfoDssDTO[list.size()];
        list.toArray(fileInfos);

        dssService =
                getAdvisedDssService(new MockDssServiceRpc(null, fileInfos, new FileInputStream(
                        randomDataFile), isDataSetAccessible));

        final ArrayList<RpcServiceInterfaceDTO> ifaces = new ArrayList<RpcServiceInterfaceDTO>(1);
        final RpcServiceInterfaceDTO iface = new RpcServiceInterfaceDTO(serviceName);
        final RpcServiceInterfaceVersionDTO ifaceVersion =
                new RpcServiceInterfaceVersionDTO(serviceName, "/rpc/v1", 1, 0);

        iface.addVersion(ifaceVersion);

        ifaces.add(iface);

        context.checking(new Expectations()
            {
                {
                    final String dataSetCode = DUMMY_DATA_SET_CODE;

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

    private static class MockDssServiceRpc extends AbstractDssServiceRpc implements
            IDssServiceRpcGeneric
    {
        private final FileInfoDssDTO[] fileInfos;

        private final FileInputStream fileInputStream;

        private final boolean isDataSetAccessible;

        /**
         * @param openBISService
         */
        public MockDssServiceRpc(IEncapsulatedOpenBISService openBISService,
                FileInfoDssDTO[] fileInfos, FileInputStream fileInputStream,
                boolean isDataSetAccessible)
        {
            super(openBISService);
            this.fileInfos = fileInfos;
            this.fileInputStream = fileInputStream;
            this.isDataSetAccessible = isDataSetAccessible;
        }

        public InputStream getFileForDataSet(String sessionToken, DataSetFileDTO fileOrFolder)
                throws IOExceptionUnchecked, IllegalArgumentException
        {
            return fileInputStream;
        }

        public InputStream getFileForDataSet(String sessionToken, String dataSetCode, String path)
                throws IOExceptionUnchecked, IllegalArgumentException
        {
            return fileInputStream;
        }

        public FileInfoDssDTO[] listFilesForDataSet(String sessionToken, DataSetFileDTO fileOrFolder)
                throws IOExceptionUnchecked, IllegalArgumentException
        {
            return fileInfos;
        }

        public FileInfoDssDTO[] listFilesForDataSet(String sessionToken, String dataSetCode,
                String path, boolean isRecursive) throws IOExceptionUnchecked,
                IllegalArgumentException
        {
            return fileInfos;
        }

        public String putDataSet(String sessionToken, NewDataSetDTO newDataset,
                InputStream inputStream) throws IOExceptionUnchecked, IllegalArgumentException
        {
            return null;
        }

        public int getMajorVersion()
        {
            return 1;
        }

        public int getMinorVersion()
        {
            return 0;
        }

        @Override
        protected boolean isDatasetAccessible(String sessionToken, String dataSetCode)
        {
            return isDataSetAccessible;
        }
    }

}
