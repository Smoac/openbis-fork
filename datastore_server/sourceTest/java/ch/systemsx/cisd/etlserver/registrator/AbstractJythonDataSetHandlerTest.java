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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.lemnik.eodsql.DynamicTransactionQuery;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.python.core.PyFunction;
import org.python.util.PythonInterpreter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm;
import ch.systemsx.cisd.etlserver.DynamicTransactionQueryFactory;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler.JythonDataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IDataSourceQueryService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
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

    protected JythonTopLevelDataSetHandler<? extends DataSetInformation> handler;

    protected Mockery context;

    protected IEncapsulatedOpenBISService openBisService;

    protected IMailClient mailClient;

    protected IDataSetValidator dataSetValidator;

    protected IDataSourceQueryService dataSourceQueryService;

    protected DynamicTransactionQuery dynamicTransactionQuery;

    protected File stagingDirectory;

    protected File incomingDataSetFile;

    protected File markerFile;

    protected File subDataSet1;

    protected File subDataSet2;

    protected boolean didDataSetRollbackHappen;

    protected boolean didServiceRollbackHappen;

    @BeforeTest
    public void init()
    {
        QueueingPathRemoverService.start();
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

        stagingDirectory = new File(workingDirectory, "staging");
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

        handler =
                new TestingDataSetHandler(globalState, registrationShouldFail,
                        shouldReThrowException);
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
                        workingDirectory, openBisService, mailClient, dataSetValidator,
                        dataSourceQueryService, myFactory, true, threadParameters);
        return globalState;
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
        return createThreadProperties(getRegistrationScriptsFolderPath() + scriptPath, null);
    }

    protected Properties createThreadPropertiesRelativeToScriptsFolder(String scriptPath,
            String validationScriptPath)
    {
        return createThreadProperties(getRegistrationScriptsFolderPath() + scriptPath,
                validationScriptPath);
    }

    private Properties createThreadProperties(String scriptPath,
            String validationScriptPropertyOrNull)
    {
        Properties threadProperties = new Properties();
        threadProperties.put(ThreadParameters.INCOMING_DIR, "incoming");
        threadProperties.put(ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION,
                ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE);
        threadProperties.put(ThreadParameters.DELETE_UNIDENTIFIED_KEY, "false");
        threadProperties.put(IStorageProcessorTransactional.STORAGE_PROCESSOR_KEY,
                MockStorageProcessor.class.getName());
        threadProperties.put(JythonTopLevelDataSetHandler.SCRIPT_PATH_KEY, scriptPath);
        if (null != validationScriptPropertyOrNull)
        {
            threadProperties.put(ch.systemsx.cisd.etlserver.ThreadParameters.VALIDATION_SCRIPT_KEY,
                    validationScriptPropertyOrNull);
        }
        threadProperties.setProperty(DataSetRegistrationService.STAGING_DIR,
                stagingDirectory.getPath());
        return threadProperties;
    }

    public static final class MockStorageProcessor implements IStorageProcessorTransactional
    {
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

        public IStorageProcessorTransaction createTransaction()
        {
            return new IStorageProcessorTransaction()
                {

                    private File storedFolder;

                    public void storeData(DataSetInformation dataSetInformation,
                            ITypeExtractor typeExtractor, IMailClient mailClient,
                            File incomingDataSetFile, File rootDir)
                    {
                        incomingDirs.add(incomingDataSetFile);
                        rootDirs.add(rootDir);
                        dataSetInfoString = dataSetInformation.toString();
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
                        return null;
                    }

                    public File getStoredDataDirectory()
                    {
                        return storedFolder;
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

    protected class TestingDataSetHandler extends JythonTopLevelDataSetHandler<DataSetInformation>
    {
        protected final boolean shouldRegistrationFail;

        protected final boolean shouldReThrowRollbackException;

        protected boolean didRollbackServiceFunctionRun = false;

        protected boolean didRollbackDataSetRegistrationFunctionRun = false;

        protected boolean didTransactionRollbackHappen = false;

        protected boolean didRollbackTransactionFunctionRunHappen = false;

        protected boolean didCommitTransactionFunctionRunHappen = false;

        public TestingDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState,
                boolean shouldRegistrationFail, boolean shouldReThrowRollbackException)
        {
            super(globalState);
            this.shouldRegistrationFail = shouldRegistrationFail;
            this.shouldReThrowRollbackException = shouldReThrowRollbackException;
        }

        @Override
        public void registerDataSetInApplicationServer(DataSetInformation dataSetInformation,
                NewExternalData data) throws Throwable
        {
            if (shouldRegistrationFail)
            {
                throw new UserFailureException("Didn't work.");
            } else
            {
                super.registerDataSetInApplicationServer(dataSetInformation, data);
            }
        }

        @Override
        public void rollback(DataSetRegistrationService<DataSetInformation> service,
                Throwable throwable)
        {
            super.rollback(service, throwable);
            didServiceRollbackHappen = true;
            if (shouldReThrowRollbackException)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(throwable);
            } else
            {
                throwable.printStackTrace();
            }
        }

        @Override
        public void didRollbackTransaction(DataSetRegistrationService<DataSetInformation> service,
                DataSetRegistrationTransaction<DataSetInformation> transaction,
                DataSetStorageAlgorithmRunner<DataSetInformation> algorithmRunner,
                Throwable throwable)
        {
            super.didRollbackTransaction(service, transaction, algorithmRunner, throwable);

            didTransactionRollbackHappen = true;
            if (shouldReThrowRollbackException)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(throwable);
            } else
            {
                throwable.printStackTrace();
            }
        }

        @Override
        protected void invokeRollbackServiceFunction(PyFunction function,
                DataSetRegistrationService<DataSetInformation> service, Throwable throwable)
        {
            super.invokeRollbackServiceFunction(function, service, throwable);
            PythonInterpreter interpreter =
                    ((JythonDataSetRegistrationService<DataSetInformation>) service)
                            .getInterpreter();
            didRollbackServiceFunctionRun =
                    interpreter.get("didRollbackServiceFunctionRun", Boolean.class);
        }

        @Override
        protected void invokeRollbackDataSetRegistrationFunction(PyFunction function,
                DataSetRegistrationService<DataSetInformation> service,
                DataSetRegistrationAlgorithm registrationAlgorithm, Throwable throwable)
        {
            super.invokeRollbackDataSetRegistrationFunction(function, service,
                    registrationAlgorithm, throwable);

            PythonInterpreter interpreter =
                    ((JythonDataSetRegistrationService<DataSetInformation>) service)
                            .getInterpreter();
            didRollbackDataSetRegistrationFunctionRun =
                    interpreter.get("didRollbackServiceFunctionRun", Boolean.class);
        }

        @Override
        protected void invokeRollbackTransactionFunction(PyFunction function,
                DataSetRegistrationService<DataSetInformation> service,
                DataSetRegistrationTransaction<DataSetInformation> transaction,
                DataSetStorageAlgorithmRunner<DataSetInformation> algorithmRunner,
                Throwable throwable)
        {
            super.invokeRollbackTransactionFunction(function, service, transaction,
                    algorithmRunner, throwable);

            PythonInterpreter interpreter =
                    ((JythonDataSetRegistrationService<DataSetInformation>) service)
                            .getInterpreter();
            didRollbackTransactionFunctionRunHappen =
                    interpreter.get("didTransactionRollbackHappen", Boolean.class);
        }

        @Override
        protected void invokeCommitTransactionFunction(PyFunction function,
                DataSetRegistrationService<DataSetInformation> service,
                DataSetRegistrationTransaction<DataSetInformation> transaction)
        {
            super.invokeCommitTransactionFunction(function, service, transaction);

            PythonInterpreter interpreter =
                    ((JythonDataSetRegistrationService<DataSetInformation>) service)
                            .getInterpreter();
            didCommitTransactionFunctionRunHappen =
                    interpreter.get("didTransactionCommitHappen", Boolean.class);
        }

        @Override
        protected JythonDataSetRegistrationService<DataSetInformation> createJythonDataSetRegistrationService(
                File aDataSetFile, DataSetInformation userProvidedDataSetInformationOrNull,
                IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
                ITopLevelDataSetRegistratorDelegate delegate, PythonInterpreter interpreter)
        {
            JythonDataSetRegistrationService<DataSetInformation> service =
                    new TestDataRegistrationService(this, aDataSetFile,
                            userProvidedDataSetInformationOrNull, cleanAfterwardsAction,
                            interpreter, shouldRegistrationFail);
            return service;
        }

    }

    protected static class TestDataRegistrationService extends
            JythonDataSetRegistrationService<DataSetInformation>
    {
        private final boolean shouldRegistrationFail;

        /**
         * @param registrator
         * @param globalCleanAfterwardsAction
         * @param interpreter
         */
        public TestDataRegistrationService(
                JythonTopLevelDataSetHandler<DataSetInformation> registrator, File aDataSetFile,
                DataSetInformation userProvidedDataSetInformationOrNull,
                IDelegatedActionWithResult<Boolean> globalCleanAfterwardsAction,
                PythonInterpreter interpreter, boolean shouldRegistrationFail)
        {
            super(registrator, aDataSetFile, userProvidedDataSetInformationOrNull,
                    globalCleanAfterwardsAction,
                    new AbstractOmniscientTopLevelDataSetRegistrator.NoOpDelegate(), interpreter);
            this.shouldRegistrationFail = shouldRegistrationFail;
        }

        @Override
        public IEntityOperationService<DataSetInformation> getEntityRegistrationService()
        {
            return new TestEntityOperationService(getRegistrator(), shouldRegistrationFail);
        }

    }

    protected static class TestEntityOperationService extends
            DefaultEntityOperationService<DataSetInformation>
    {

        private final boolean shouldRegistrationFail;

        /**
         * @param registrator
         */
        public TestEntityOperationService(
                AbstractOmniscientTopLevelDataSetRegistrator<DataSetInformation> registrator,
                boolean shouldRegistrationFail)
        {
            super(registrator, new AbstractOmniscientTopLevelDataSetRegistrator.NoOpDelegate());
            this.shouldRegistrationFail = shouldRegistrationFail;
        }

        @Override
        public AtomicEntityOperationResult performOperationsInApplcationServer(
                AtomicEntityOperationDetails<DataSetInformation> registrationDetails)
        {
            if (shouldRegistrationFail)
            {
                assert false;
            }
            return super.performOperationsInApplcationServer(registrationDetails);
        }

    }
}
