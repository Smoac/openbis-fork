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
import ch.systemsx.cisd.common.filesystem.FileOperations;
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
import ch.systemsx.cisd.etlserver.registrator.api.v1.SecondaryTransactionFailure;
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

    protected File prestagingDirectory;

    protected File precommitDirectory;

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
                        workingDirectory, workingDirectory, workingDirectory, openBisService,
                        mailClient, dataSetValidator, dataSourceQueryService, myFactory, true,
                        threadParameters);
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

    protected class TestingDataSetHandler extends JythonTopLevelDataSetHandler<DataSetInformation>
    {
        protected final boolean shouldRegistrationFail;

        protected final boolean shouldReThrowRollbackException;

        protected boolean didRollbackServiceFunctionRun = false;

        protected boolean didTransactionRollbackHappen = false;

        protected boolean didRollbackTransactionFunctionRunHappen = false;

        protected boolean didCommitTransactionFunctionRunHappen = false;

        protected boolean didSecondaryTransactionErrorNotificationHappen = false;

        protected boolean didPostRegistrationFunctionRunHappen = false;

        protected boolean didPreRegistrationFunctionRunHappen = false;

        protected boolean didPostStorageFunctionRunHappen = false;

        protected String registrationContextError;

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
                    readBoolean(interpreter, "didTransactionRollbackHappen");
        }

        @Override
        protected void invokeServiceTransactionFunction(PyFunction function,
                DataSetRegistrationService<DataSetInformation> service,
                DataSetRegistrationTransaction<DataSetInformation> transaction)
        {
            super.invokeServiceTransactionFunction(function, service, transaction);

            PythonInterpreter interpreter =
                    ((JythonDataSetRegistrationService<DataSetInformation>) service)
                            .getInterpreter();
            didCommitTransactionFunctionRunHappen =
                    readBoolean(interpreter, "didTransactionCommitHappen");
        }

        @Override
        protected void invokeTransactionFunctionWithContext(PyFunction function,
                DataSetRegistrationService<DataSetInformation> service,
                DataSetRegistrationTransaction<DataSetInformation> transaction)
        {
            super.invokeTransactionFunctionWithContext(function, service, transaction);
            PythonInterpreter interpreter =
                    ((JythonDataSetRegistrationService<DataSetInformation>) service)
                            .getInterpreter();

            didPreRegistrationFunctionRunHappen =
                    readBoolean(interpreter, "didPreRegistrationFunctionRunHappen");

            didPostRegistrationFunctionRunHappen =
                    readBoolean(interpreter, "didPostRegistrationFunctionRunHappen");

            didPostStorageFunctionRunHappen =
                    readBoolean(interpreter, "didPostStorageFunctionRunHappen");

            registrationContextError = interpreter.get("contextTestFailed", String.class);
        }

        /**
         * reads boolean or false if null from interpreter
         */
        protected boolean readBoolean(PythonInterpreter interpreter, String variable)
        {
            Boolean retVal = interpreter.get(variable, Boolean.class);
            if (retVal == null)
                return false;
            return retVal;
        }

        @Override
        public void didEncounterSecondaryTransactionErrors(
                DataSetRegistrationService<DataSetInformation> service,
                DataSetRegistrationTransaction<DataSetInformation> transaction,
                List<SecondaryTransactionFailure> secondaryErrors)
        {
            super.didEncounterSecondaryTransactionErrors(service, transaction, secondaryErrors);

            PythonInterpreter interpreter =
                    ((JythonDataSetRegistrationService<DataSetInformation>) service)
                            .getInterpreter();
            didSecondaryTransactionErrorNotificationHappen =
                    interpreter
                            .get("didSecondaryTransactionErrorNotificationHappen", Boolean.class);
        }

        @Override
        protected JythonDataSetRegistrationService<DataSetInformation> createJythonDataSetRegistrationService(
                DataSetFile aDataSetFile, DataSetInformation userProvidedDataSetInformationOrNull,
                IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
                ITopLevelDataSetRegistratorDelegate delegate, PythonInterpreter interpreter,
                TopLevelDataSetRegistratorGlobalState globalState)
        {
            JythonDataSetRegistrationService<DataSetInformation> service =
                    new TestDataRegistrationService(this, aDataSetFile,
                            userProvidedDataSetInformationOrNull, cleanAfterwardsAction,
                            interpreter, shouldRegistrationFail, globalState);
            return service;
        }

    }

    protected static class TestDataRegistrationService extends
            JythonDataSetRegistrationService<DataSetInformation>
    {
        private final boolean shouldRegistrationFail;

        public TestDataRegistrationService(
                JythonTopLevelDataSetHandler<DataSetInformation> registrator,
                DataSetFile aDataSetFile, DataSetInformation userProvidedDataSetInformationOrNull,
                IDelegatedActionWithResult<Boolean> globalCleanAfterwardsAction,
                PythonInterpreter interpreter, boolean shouldRegistrationFail,
                TopLevelDataSetRegistratorGlobalState globalState)
        {
            super(registrator, aDataSetFile, userProvidedDataSetInformationOrNull,
                    globalCleanAfterwardsAction,
                    new AbstractOmniscientTopLevelDataSetRegistrator.NoOpDelegate(), interpreter,
                    globalState);
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
