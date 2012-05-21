/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import net.lemnik.eodsql.DynamicTransactionQuery;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.etlserver.DynamicTransactionQueryFactory;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IDataSourceQueryService;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;

/**
 * Abstract superclass for tests that should run on jython data set handler code.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractJythonDataSetHandlerTest extends AbstractFileSystemTestCase
{

    /**
     * Return the path of the folder that contains the registration scripts.
     */
    protected abstract String getRegistrationScriptsFolderPath();

    protected static final String DATABASE_INSTANCE_UUID = "db-uuid";

    protected ITestingDataSetHandler handler;

    protected Mockery context;

    protected IEncapsulatedOpenBISService openBisService;

    protected IMailClient mailClient;

    protected IDataSetValidator dataSetValidator;

    protected IDataSourceQueryService dataSourceQueryService;

    protected DynamicTransactionQuery dynamicTransactionQuery;

    protected IDataSetStorageRecoveryManager storageRecoveryManager;

    protected File stagingDirectory;

    protected File prestagingDirectory;

    protected File precommitDirectory;

    protected File incomingDataSetFile;

    protected File markerFile;

    protected File subDataSet1;

    protected File subDataSet2;

    protected static final String DATA_SET_CODE = "data-set-code";

    protected static final String DATA_SET_CODE_1 = "data-set-code-1";

    protected static final String CONTAINER_DATA_SET_CODE = "container-data-set-code";

    protected static final DataSetType DATA_SET_TYPE = new DataSetType("O1");

    protected static final String EXPERIMENT_PERM_ID = "experiment-perm-id";

    protected static final String EXPERIMENT_IDENTIFIER = "/SPACE/PROJECT/EXP";

    protected static final String SAMPLE_PERM_ID = "sample-perm-id";

    @BeforeTest
    public void init()
    {
        QueueingPathRemoverService.start();
        ch.systemsx.cisd.etlserver.registrator.api.v1.impl.RollbackConfigurator
                .setFileSystemAvailabilityPollingWaitTimeAndWaitCount(10, 1);
    }

    @AfterTest
    public void finish()
    {
        QueueingPathRemoverService.stop();
    }

    @BeforeMethod
    @Override
    public void setUp() throws IOException
    {
        super.setUp();

        context = new Mockery();
        openBisService = context.mock(IEncapsulatedOpenBISService.class);
        dataSetValidator = context.mock(IDataSetValidator.class);
        mailClient = context.mock(IMailClient.class);
        dataSourceQueryService = context.mock(IDataSourceQueryService.class);
        dynamicTransactionQuery = context.mock(DynamicTransactionQuery.class);
        storageRecoveryManager = new DataSetStorageRecoveryManager();

        stagingDirectory = new File(workingDirectory, "staging");
        prestagingDirectory = new File(workingDirectory, "pre-staging");
        precommitDirectory = new File(workingDirectory, "pre-commit");
    }

    @AfterMethod
    public void tearDown() throws IOException
    {
        context.assertIsSatisfied();
    }

    protected File createDirectory(File parentDir, String directoryName)
    {
        final File file = new File(parentDir, directoryName);
        file.mkdir();
        return file;
    }

    protected void createHandler(Properties threadProperties, final boolean registrationShouldFail,
            boolean shouldReThrowException)
    {
        TopLevelDataSetRegistratorGlobalState globalState = createGlobalState(threadProperties);

        if (threadProperties.containsKey("TEST_V2_API"))
        {
            handler =
                    new TestingDataSetHandlerV2(globalState, registrationShouldFail,
                            shouldReThrowException);
        } else
        {
            handler =
                    new TestingDataSetHandler(globalState, registrationShouldFail,
                            shouldReThrowException);
        }
    }

    protected TopLevelDataSetRegistratorGlobalState createGlobalState(Properties threadProperties)
    {
        ThreadParameters threadParameters =
                new ThreadParameters(threadProperties, "jython-handler-test");

        DynamicTransactionQueryFactory myFactory = new DynamicTransactionQueryFactory()
            {
                @Override
                public DynamicTransactionQuery createDynamicTransactionQuery(String dataSourceName)
                {
                    return dynamicTransactionQuery;
                }
            };

        TopLevelDataSetRegistratorGlobalState globalState =
                new TopLevelDataSetRegistratorGlobalState("dss",
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        workingDirectory, workingDirectory, workingDirectory, workingDirectory,
                        openBisService, mailClient, dataSetValidator, dataSourceQueryService,
                        myFactory, true, threadParameters, storageRecoveryManager);
        return globalState;
    }

    protected void initializeStorageRecoveryManagerMock()
    {
        storageRecoveryManager = context.mock(IDataSetStorageRecoveryManager.class);
        
        context.checking(new Expectations()
            {
                {
                    one(storageRecoveryManager).setDropboxRecoveryStateDir(
                            new File(workingDirectory, "jython-handler-test"));
                    one(storageRecoveryManager).setRecoveryMarkerFilesDir(new File(
                            new File(workingDirectory, "recovery-marker"), "jython-handler-test"));
                }
            });
    }

    protected void setUpHomeDataBaseExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    DatabaseInstance databaseInstance = new DatabaseInstance();
                    databaseInstance.setUuid(DATABASE_INSTANCE_UUID);
                    one(openBisService).getHomeDatabaseInstance();
                    will(returnValue(databaseInstance));
                }
            });
    }

    /**
     * adds an extension to the Jython Path, so that all libraries in it will be visible to the
     * Jython environment.
     */
    protected void extendJythonLibPath(String pathExtension)
    {
        final String JYTHON_PATH_PROPNAME = "python.path";
        String pythonPath = System.getProperty(JYTHON_PATH_PROPNAME, "");
        String extendedPath = pythonPath + File.pathSeparator + pathExtension;
        System.setProperty("python.path", extendedPath);
    }

    protected Properties createThreadPropertiesRelativeToScriptsFolder(String scriptPath)
    {
        return createThreadProperties(getRegistrationScriptsFolderPath() + scriptPath, null, null);
    }

    protected Properties createThreadPropertiesRelativeToScriptsFolder(String scriptPath,
            HashMap<String, String> override)
    {
        return createThreadProperties(getRegistrationScriptsFolderPath() + scriptPath, null,
                override);
    }

    protected Properties createThreadPropertiesRelativeToScriptsFolder(String scriptPath,
            String validationScriptPath)
    {
        return createThreadProperties(getRegistrationScriptsFolderPath() + scriptPath,
                validationScriptPath, null);
    }

    private Properties createThreadProperties(String scriptPath,
            String validationScriptPropertyOrNull, HashMap<String, String> overrideOrNull)
    {
        Properties threadProperties = new Properties();
        threadProperties.setProperty(ThreadParameters.INCOMING_DIR, "incoming");
        threadProperties.setProperty(ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION,
                ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE);
        threadProperties.setProperty(ThreadParameters.DELETE_UNIDENTIFIED_KEY, "false");
        threadProperties.setProperty(IStorageProcessorTransactional.STORAGE_PROCESSOR_KEY,
                MockStorageProcessor.class.getName());
        threadProperties.setProperty(JythonTopLevelDataSetHandler.SCRIPT_PATH_KEY, scriptPath);
        if (null != validationScriptPropertyOrNull)
        {
            threadProperties.setProperty(
                    ch.systemsx.cisd.etlserver.ThreadParameters.VALIDATION_SCRIPT_KEY,
                    validationScriptPropertyOrNull);
        }
        threadProperties.setProperty(TopLevelDataSetRegistratorGlobalState.STAGING_DIR,
                stagingDirectory.getPath());
        threadProperties.setProperty(TopLevelDataSetRegistratorGlobalState.PRE_STAGING_DIR,
                prestagingDirectory.getPath());
        threadProperties.setProperty(TopLevelDataSetRegistratorGlobalState.PRE_COMMIT_DIR,
                precommitDirectory.getPath());

        if (overrideOrNull != null)
        {
            for (String key : overrideOrNull.keySet())
            {
                threadProperties.setProperty(key, overrideOrNull.get(key));
            }
        }

        return threadProperties;
    }

    public static class MockStorageProcessor implements IStorageProcessorTransactional,
            Serializable
    {
        private static final long serialVersionUID = 1L;

        static MockStorageProcessor instance;

        int calledGetStoreRootDirectoryCount = 0;

        int calledCommitCount = 0;

        File storeRootDirectory;

        String dataSetInfoString;

        protected List<File> incomingDirs = new ArrayList<File>();

        protected List<File> rootDirs = new ArrayList<File>();

        public MockStorageProcessor(ExtendedProperties props)
        {
            instance = this;
        }

        public File getStoreRootDirectory()
        {
            calledGetStoreRootDirectoryCount++;
            return storeRootDirectory;
        }

        public void setStoreRootDirectory(File storeRootDirectory)
        {
            this.storeRootDirectory = storeRootDirectory;
        }

        public StorageFormat getStorageFormat()
        {
            return StorageFormat.PROPRIETARY;
        }

        public UnstoreDataAction getDefaultUnstoreDataAction(Throwable exception)
        {
            return UnstoreDataAction.LEAVE_UNTOUCHED;
        }

        public IStorageProcessorTransaction createTransaction(
                StorageProcessorTransactionParameters parameters)
        {
            final File rootDir = parameters.getRootDir();
            dataSetInfoString = parameters.getDataSetInformation().toString();
            return new IStorageProcessorTransaction()
                {

                    private static final long serialVersionUID = 1L;

                    private File storedFolder = rootDir;

                    public void storeData(ITypeExtractor typeExtractor, IMailClient mailClient,
                            File incomingDataSetFile)
                    {

                        incomingDirs.add(incomingDataSetFile);
                        rootDirs.add(rootDir);
                        try
                        {
                            if (incomingDataSetFile.isDirectory())
                            {
                                FileUtils.copyDirectory(incomingDataSetFile, rootDir);
                            } else
                            {
                                FileUtils.copyFileToDirectory(incomingDataSetFile, rootDir);
                            }
                        } catch (IOException ex)
                        {
                            throw new IOExceptionUnchecked(ex);
                        }
                        storedFolder = rootDir;
                    }

                    public UnstoreDataAction rollback(Throwable exception)
                    {
                        FileOperations.getInstance().deleteRecursively(storedFolder);
                        return null;
                    }

                    public File getStoredDataDirectory()
                    {
                        return storedFolder;
                    }

                    public void setStoredDataDirectory(File folder)
                    {
                        storedFolder = folder;
                    }

                    public void commit()
                    {
                        calledCommitCount++;
                    }

                    public File tryGetProprietaryData()
                    {
                        return null;
                    }
                };
        }
    }

    protected void assertStorageProcess(AtomicEntityOperationDetails recordedObject,
            String dataSetCode, String dataSetDirectory, int testId)
    {
        assertEquals(1, recordedObject.getDataSetRegistrations().size());

        NewExternalData dataSet = recordedObject.getDataSetRegistrations().get(0);

        assertEquals(dataSetCode, dataSet.getCode());
        assertEquals(DATA_SET_TYPE, dataSet.getDataSetType());

        File datasetLocation =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, dataSetCode,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        DATABASE_INSTANCE_UUID);

        assertEquals(FileUtilities.getRelativeFilePath(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                datasetLocation), dataSet.getLocation());

        assertEquals(new File(stagingDirectory, dataSetCode + "-storage"),
                MockStorageProcessor.instance.rootDirs.get(testId));

        File incomingDir = MockStorageProcessor.instance.incomingDirs.get(testId);

        assertEquals(new File(new File(stagingDirectory, dataSetCode), dataSetDirectory),
                incomingDir);

        assertEquals("hello world" + (testId + 1),
                FileUtilities
                        .loadToString(new File(datasetLocation, "read" + (testId + 1) + ".me"))
                        .trim());
    }
    
    protected void assertDataSetNotStoredProcess(String dataSetCode)
    {
        File datasetLocation =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, dataSetCode,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        DATABASE_INSTANCE_UUID);
        assertFalse("The storage path of the dataset should noto exist", datasetLocation.exists());
    }
}
