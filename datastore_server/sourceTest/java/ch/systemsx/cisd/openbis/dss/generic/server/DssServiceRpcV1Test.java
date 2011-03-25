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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.common.io.ConcatenatedContentInputStream;
import ch.systemsx.cisd.common.io.FileBasedContent;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistrator;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.api.v1.PutDataSetService;
import ch.systemsx.cisd.etlserver.api.v1.TestDataSetTypeToTopLevelRegistratorMapper;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.server.DssServiceRpcAuthorizationAdvisor.DssServiceRpcAuthorizationMethodInterceptor;
import ch.systemsx.cisd.openbis.dss.generic.server.api.v1.DssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.DssSessionAuthorizationHolder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.IDssServiceRpcGenericInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.DataSetFileDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DssServiceRpcV1Test extends AbstractFileSystemTestCase
{
    private static final String SHARE_ID = "share-1";

    private static final String NEW_DATA_SET_EXP = "E1";

    private static final String NEW_DATA_SET_OWNER_ID = "TEST:/TEST-SPACE/S1";

    private static final String NEW_DATA_SET_PROJECT = "TEST-PROJECT";

    private static final String NEW_DATA_SET_SPACE = "TEST-SPACE";

    private static final String SESSION_TOKEN = "DummySessionToken";

    private static final String DATA_SET_CODE = "code";

    private static final String DB_INSTANCE_UUID = "UUID";

    private static final String NEW_DATA_SET_CODE = "NEW-DATA-SET-CODE";

    private Mockery context;

    private DssServiceRpcGeneric rpcService;

    private IEncapsulatedOpenBISService openBisService;

    private ITopLevelDataSetRegistrator dataSetRegistrator;

    private IMailClient mailClient;

    private IDataSetValidator validator;

    private IShareIdManager shareIdManager;

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        DssSessionAuthorizationHolder.setAuthorizer(new DatasetSessionAuthorizer());
        final StaticListableBeanFactory applicationContext = new StaticListableBeanFactory();
        ServiceProviderTestWrapper.setApplicationContext(applicationContext);
        context = new Mockery();
        openBisService = context.mock(IEncapsulatedOpenBISService.class);
        shareIdManager = context.mock(IShareIdManager.class);
        applicationContext.addBean("openBIS-service", openBisService);
        mailClient = context.mock(IMailClient.class);

        validator = context.mock(IDataSetValidator.class);
        dataSetRegistrator = context.mock(ITopLevelDataSetRegistrator.class);

        File storeDir = new File(workingDirectory, "store/");
        File incomingDir = new File(workingDirectory, "incoming/");
        initializeDirectories(storeDir, incomingDir);

        setupGetExpectations();

        PutDataSetService putService =
                new PutDataSetService(openBisService, LogFactory.getLogger(LogCategory.OPERATION,
                        DssServiceRpcV1Test.class), storeDir, incomingDir,
                        new TestDataSetTypeToTopLevelRegistratorMapper(dataSetRegistrator),
                        mailClient, "TEST", validator);
        rpcService = new DssServiceRpcGeneric(openBisService, shareIdManager, putService);
        rpcService.setStoreDirectory(storeDir);
        rpcService.setIncomingDirectory(incomingDir);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown()
    {
        ServiceProviderTestWrapper.restoreApplicationContext();
    }

    private void initializeDirectories(File storeDir, File incomingDir) throws IOException
    {
        File location =
                DatasetLocationUtil.getDatasetLocationPath(storeDir, DATA_SET_CODE, SHARE_ID,
                        DB_INSTANCE_UUID);
        if (!location.mkdirs())
            return;

        createDummyFile(location, "foo.txt", 100);

        File dummyDir = new File(location, "stuff/");
        dummyDir.mkdir();

        createDummyFile(dummyDir, "bar.txt", 110);

        // Don't do this -- the rpcService should do this
        // incomingDir.mkdirs();
    }

    /**
     * Create a dummy file of size <code>length</code> bytes.
     */
    private File createDummyFile(File dir, String name, int length) throws IOException
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

        return dummyFile;
    }

    private DatabaseInstance getDatabaseInstance()
    {
        final DatabaseInstance homeDatabaseInstance = new DatabaseInstance();
        homeDatabaseInstance.setCode("TEST");
        homeDatabaseInstance.setUuid(DB_INSTANCE_UUID);
        return homeDatabaseInstance;
    }

    private void setupGetExpectations()
    {
        // Expectations for get
        final DatabaseInstance homeDatabaseInstance = getDatabaseInstance();

        context.checking(new Expectations()
            {
                {
                    // Expectations for getting
                    allowing(openBisService).checkDataSetAccess(SESSION_TOKEN, DATA_SET_CODE);
                    allowing(openBisService).checkDataSetCollectionAccess(SESSION_TOKEN,
                            Arrays.asList(DATA_SET_CODE));
                    allowing(openBisService).getHomeDatabaseInstance();
                    will(returnValue(homeDatabaseInstance));
                }
            });
    }

    private void setupPutExpectations()
    {
        // Expectations for put
        final SpaceIdentifier spaceIdentifier =
                new SpaceIdentifier(new DatabaseInstanceIdentifier("TEST"), NEW_DATA_SET_SPACE);
        final SessionContextDTO session = new SessionContextDTO();
        final Sample sample = new Sample();
        Experiment experiment = new Experiment();
        Project project = new Project();
        Space space = new Space();
        space.setCode(NEW_DATA_SET_SPACE);
        project.setCode(NEW_DATA_SET_PROJECT);
        project.setSpace(space);
        experiment.setProject(project);
        experiment.setCode(NEW_DATA_SET_EXP);

        Person registrator = new Person();
        registrator.setEmail("test@test.test");
        registrator.setUserId("test");
        registrator.setFirstName("Test First Name");
        registrator.setLastName("Test Last Name");
        experiment.setRegistrator(registrator);
        sample.setExperiment(experiment);

        final RecordingMatcher<File> fileMatcher = new RecordingMatcher<File>();
        final RecordingMatcher<DataSetInformation> dataSetInfoMatcher =
                new RecordingMatcher<DataSetInformation>();
        final RecordingMatcher<ITopLevelDataSetRegistratorDelegate> delegateMatcher =
                new RecordingMatcher<ITopLevelDataSetRegistratorDelegate>();
        context.checking(new Expectations()
            {
                {
                    atLeast(1).of(openBisService).checkSpaceAccess(with(SESSION_TOKEN),
                            with(spaceIdentifier));
                    oneOf(dataSetRegistrator).handle(with(fileMatcher), with(dataSetInfoMatcher),
                            with(delegateMatcher));
                    will(new CustomAction("Notify the delegate")
                        {
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                List<DataSetInformation> dataSetInfos =
                                        dataSetInfoMatcher.getRecordedObjects();
                                DataSetInformation dataSetInfo = dataSetInfos.get(0);
                                dataSetInfo.setDataSetCode(NEW_DATA_SET_CODE);
                                List<ITopLevelDataSetRegistratorDelegate> delegates =
                                        delegateMatcher.getRecordedObjects();
                                delegates.get(0).didRegisterDataSets(dataSetInfos);

                                return null;
                            }
                        });
                    allowing(openBisService).tryGetSession(SESSION_TOKEN);
                    will(returnValue(session));
                    allowing(openBisService).createDataSetCode();
                    will(returnValue(NEW_DATA_SET_CODE));
                }
            });
    }

    @Test
    public void testDataSetListingNonRecursive()
    {
        prepareGetShareId();
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
        prepareGetShareId();
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
        prepareGetShareId();
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
        prepareGetShareId();
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
        prepareGetShareId();
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
    public void testDataSetListingWithSneakyPath()
    {
        prepareGetShareId();
        prepareGetShareId();
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
        prepareGetShareId();
        prepareGetShareId();
        FileInfoDssDTO[] fileInfos =
                rpcService
                        .listFilesForDataSet(SESSION_TOKEN, DATA_SET_CODE, "stuff/bar.txt", false);
        assertEquals(1, fileInfos.length);

        InputStream is =
                rpcService.getFileForDataSet(SESSION_TOKEN, DATA_SET_CODE,
                        fileInfos[0].getPathInDataSet());

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

    @Test
    public void testDataSetUpload() throws IOException
    {
        prepareLockDataSet();
        setupPutExpectations();
        QueueingPathRemoverService.start();
        File fileToUpload = createDummyFile(workingDirectory, "to-upload.txt", 80);

        ArrayList<FileInfoDssDTO> fileInfos = getFileInfosForPath(fileToUpload);

        NewDataSetDTO newDataSet = getNewDataSet(fileToUpload);
        ConcatenatedContentInputStream fileInputStream =
                new ConcatenatedContentInputStream(true, getContentForFileInfos(
                        fileToUpload.getPath(), fileInfos));

        TestMethodInterceptor testMethodInterceptor = new TestMethodInterceptor(shareIdManager);
        IDssServiceRpcGenericInternal service = getAdvisedService(testMethodInterceptor);

        String result = service.putDataSet(SESSION_TOKEN, newDataSet, fileInputStream);
        assertEquals(NEW_DATA_SET_CODE, result);
        assertTrue("Advice should have been invoked.", testMethodInterceptor.methodInvoked);

        context.assertIsSatisfied();
    }

    // Used for the authorization test
    private static class TestMethodInterceptor extends DssServiceRpcAuthorizationMethodInterceptor
            implements MethodInterceptor
    {
        public TestMethodInterceptor(IShareIdManager shareIdManager)
        {
            super(shareIdManager);
        }

        private boolean methodInvoked = false;

        @Override
        public Object invoke(MethodInvocation methodInvocation) throws Throwable
        {
            Object result = super.invoke(methodInvocation);
            methodInvoked = true;
            return result;
        }
    }

    private IDssServiceRpcGenericInternal getAdvisedService(
            TestMethodInterceptor testMethodInterceptor)
    {
        ProxyFactory pf = new ProxyFactory();
        pf.addAdvisor(new DssServiceRpcAuthorizationAdvisor(testMethodInterceptor));
        pf.setTarget(rpcService);
        pf.addInterface(IDssServiceRpcGenericInternal.class);
        return (IDssServiceRpcGenericInternal) pf.getProxy();
    }

    @Test
    public void testAuthorizationForStringCode()
    {
        prepareGetShareId();
        prepareLockDataSet(DATA_SET_CODE);
        TestMethodInterceptor testMethodInterceptor = new TestMethodInterceptor(shareIdManager);
        IDssServiceRpcGenericInternal service = getAdvisedService(testMethodInterceptor);
        service.listFilesForDataSet(SESSION_TOKEN, DATA_SET_CODE, "/", false);
        assertTrue("Advice should have been invoked.", testMethodInterceptor.methodInvoked);
    }

    @Test
    public void testAuthorizationDataSetFile()
    {
        prepareGetShareId();
        prepareLockDataSet(DATA_SET_CODE);
        TestMethodInterceptor testMethodInterceptor = new TestMethodInterceptor(shareIdManager);
        IDssServiceRpcGenericInternal service = getAdvisedService(testMethodInterceptor);
        DataSetFileDTO dataSetFile = new DataSetFileDTO(DATA_SET_CODE, "/", false);
        service.listFilesForDataSet(SESSION_TOKEN, dataSetFile);
        assertTrue("Advice should have been invoked.", testMethodInterceptor.methodInvoked);
    }

    private List<IContent> getContentForFileInfos(String filePath, List<FileInfoDssDTO> fileInfos)
    {
        List<IContent> files = new ArrayList<IContent>();
        File parent = new File(filePath);
        if (false == parent.isDirectory())
        {
            return Collections.<IContent> singletonList(new FileBasedContent(parent));
        }

        for (FileInfoDssDTO fileInfo : fileInfos)
        {
            File file = new File(parent, fileInfo.getPathInDataSet());
            if (false == file.exists())
            {
                throw new IllegalArgumentException("File does not exist " + file);
            }
            // Skip directories
            if (false == file.isDirectory())
            {
                files.add(new FileBasedContent(file));
            }
        }

        return files;
    }

    private NewDataSetDTO getNewDataSet(File fileToUpload) throws IOException
    {
        DataSetOwnerType ownerType = DataSetOwnerType.SAMPLE;
        String ownerIdentifier = NEW_DATA_SET_OWNER_ID;
        DataSetOwner owner = new NewDataSetDTO.DataSetOwner(ownerType, ownerIdentifier);

        File file = fileToUpload;
        ArrayList<FileInfoDssDTO> fileInfos = getFileInfosForPath(file);

        // Get the parent
        String parentNameOrNull = null;
        if (file.isDirectory())
        {
            parentNameOrNull = file.getName();
        }

        NewDataSetDTO dataSet = new NewDataSetDTO(owner, parentNameOrNull, fileInfos);
        dataSet.setDataSetTypeOrNull("PROPRIATARY");
        return dataSet;
    }

    private ArrayList<FileInfoDssDTO> getFileInfosForPath(File file) throws IOException
    {
        ArrayList<FileInfoDssDTO> fileInfos = new ArrayList<FileInfoDssDTO>();
        if (false == file.exists())
        {
            return fileInfos;
        }

        String path = file.getCanonicalPath();
        if (false == file.isDirectory())
        {
            path = file.getParentFile().getCanonicalPath();
        }

        FileInfoDssBuilder builder = new FileInfoDssBuilder(path, path);
        builder.appendFileInfosForFile(file, fileInfos, true);
        return fileInfos;
    }

    private void prepareGetShareId()
    {
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).getShareId(DATA_SET_CODE);
                    will(returnValue(SHARE_ID));
                }
            });
    }

    private void prepareLockDataSet(final String... dataSetCodes)
    {
        context.checking(new Expectations()
            {
                {
                    for (String dataSetCode : dataSetCodes)
                    {
                        one(shareIdManager).lock(dataSetCode);
                    }
                    one(shareIdManager).releaseLocks();
                }
            });
    }

}
