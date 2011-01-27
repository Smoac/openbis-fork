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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.mail.From;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.etlserver.IStorageProcessor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.AbstractOmniscientTopLevelDataSetRegistrator;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperimentImmutable;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetRegistrationTransactionTest extends AbstractTestWithRollbackStack
{
    private static final String EXPERIMENT_IDENTIFIER = "/SPACE/PROJECT/EXP-CODE";

    private static final String DATA_SET_CODE = "data-set-code";

    private static final String DATABASE_INSTANCE_UUID = "db-uuid";

    private static final DataSetType DATA_SET_TYPE = new DataSetType("O1");

    private DataSetRegistrationTransaction<DataSetInformation> tr;

    private File stagingDirectory;

    private Mockery context;

    private IEncapsulatedOpenBISService openBisService;

    private IMailClient mailClient;

    private IDataSetValidator dataSetValidator;

    private BufferedAppender logAppender;

    private TestingDataSetHandler handler;

    private File srcFile;

    private DataSetRegistrationService service;

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

        srcFile = new File(workingDirectory, "read.me");
        fillContentsOfSource();

        context = new Mockery();
        openBisService = context.mock(IEncapsulatedOpenBISService.class);
        dataSetValidator = context.mock(IDataSetValidator.class);
        mailClient = context.mock(IMailClient.class);

        logAppender = new BufferedAppender();

        stagingDirectory = new File(workingDirectory, "staging");
        stagingDirectory.mkdirs();

    }

    @Test
    public void testCommit()
    {
        setUpOpenBisExpectations(true);
        setUpDataSetValidatorExpectations();
        setUpMailClientExpectations();
        createTransaction();

        IDataSet newDataSet = tr.createNewDataSet();
        String dst = tr.moveFile(srcFile.getAbsolutePath(), newDataSet);
        IExperimentImmutable experiment = tr.getExperiment(EXPERIMENT_IDENTIFIER);
        newDataSet.setExperiment(experiment);
        newDataSet.setDataSetType(DATA_SET_TYPE.getCode());

        checkContentsOfFile(new File(dst));

        tr.commit();

        assertTrue(
                logAppender.getLogContent(),
                logAppender
                        .getLogContent()
                        .matches(
                                "No pre-registration script found, skipping execution.\n"
                                        + "No post-registration script found, skipping execution.\n"
                                        + "Identified that database knows experiment '/SPACE/PROJECT/EXP-CODE'.\n"
                                        + "Start storing data set for experiment '/SPACE/PROJECT/EXP-CODE'.\n"
                                        + "Finished storing data set for experiment '/SPACE/PROJECT/EXP-CODE', took .*\n"
                                        + "Successfully registered data set: .*\n"
                                        + "Successfully registered data set: .*\n"
                                        + "Dataset deleted in registration: clean up failed"));

        // Commented out for the moment.
        context.assertIsSatisfied();
    }

    @Test
    public void testRollback()
    {
        setUpOpenBisExpectations(false);
        createTransaction();

        IDataSet newDataSet = tr.createNewDataSet();
        String dst = tr.moveFile(srcFile.getAbsolutePath(), newDataSet);

        checkContentsOfFile(new File(dst));

        tr.rollback();

        checkContentsOfFile(srcFile);

        context.assertIsSatisfied();
    }

    private void createHandler()
    {
        Properties threadProperties = createThreadProperties();

        createHandler(threadProperties, false);
    }

    private Properties createThreadProperties()
    {
        Properties threadProperties = new Properties();
        threadProperties.put(ThreadParameters.INCOMING_DIR, "incoming");
        threadProperties.put(ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION,
                ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE);
        threadProperties.put(ThreadParameters.DELETE_UNIDENTIFIED_KEY, "false");
        threadProperties.put(IStorageProcessor.STORAGE_PROCESSOR_KEY,
                MockStorageProcessor.class.getName());
        return threadProperties;
    }

    private void createHandler(Properties threadProperties, final boolean registrationShouldFail)
    {
        TopLevelDataSetRegistratorGlobalState globalState = createGlobalState(threadProperties);

        handler = new TestingDataSetHandler(globalState);
        service = handler.createNewService();
    }

    private TopLevelDataSetRegistratorGlobalState createGlobalState(Properties threadProperties)
    {
        ThreadParameters threadParameters =
                new ThreadParameters(threadProperties, "jython-handler-test");

        TopLevelDataSetRegistratorGlobalState globalState =
                new TopLevelDataSetRegistratorGlobalState("dss", workingDirectory, openBisService,
                        mailClient, dataSetValidator, true, threadParameters);
        return globalState;
    }

    private void setUpHomeDataBaseExpectations()
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

    private void setUpOpenBisExpectations(final boolean willRegister)
    {
        setUpHomeDataBaseExpectations();
        context.checking(new Expectations()
            {
                {
                    oneOf(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE + 1));

                    if (willRegister)
                    {

                        Experiment experiment = new Experiment();
                        experiment.setIdentifier(EXPERIMENT_IDENTIFIER);
                        experiment.setCode("EXP-CODE");
                        Person registrator = new Person();
                        registrator.setEmail("email@email.com");
                        experiment.setRegistrator(registrator);

                        Project project = new Project();
                        project.setCode("PROJECT");

                        Space space = new Space();
                        space.setCode("SPACE");

                        project.setSpace(space);
                        experiment.setProject(project);

                        exactly(2).of(openBisService).tryToGetExperiment(
                                new ExperimentIdentifierFactory(EXPERIMENT_IDENTIFIER)
                                        .createIdentifier());
                        will(returnValue(experiment));

                        exactly(1).of(openBisService).registerDataSet(
                                with(any(DataSetInformation.class)),
                                with(any(NewExternalData.class)));
                    }
                }
            });
    }

    private void setUpDataSetValidatorExpectations()
    {
        final File dataSetDir = new File(stagingDirectory, DATA_SET_CODE + "1");
        // File dataSetFile = new File(dataSetDir, srcFile.getName());
        context.checking(new Expectations()
            {
                {
                    oneOf(dataSetValidator).assertValidDataSet(with(DATA_SET_TYPE),
                            with(dataSetDir));
                }
            });
    }

    private void setUpMailClientExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    exactly(1).of(mailClient).sendMessage(with(any(String.class)),
                            with(any(String.class)), with(aNull(String.class)),
                            with(aNull(From.class)), with(any(String[].class)));
                }
            });
    }

    public static final class MockStorageProcessor implements IStorageProcessor
    {
        static MockStorageProcessor instance;

        int calledGetStoreRootDirectoryCount = 0;

        int calledStoreDataCount = 0;

        int calledCommitCount = 0;

        File storeRootDirectory;

        String dataSetInfoString;

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

        public File storeData(DataSetInformation dataSetInformation, ITypeExtractor typeExtractor,
                IMailClient mailClient, File incomingDataSetDirectory, File rootDir)
        {
            calledStoreDataCount++;
            dataSetInfoString = dataSetInformation.toString();
            try
            {
                FileUtils.copyDirectory(incomingDataSetDirectory, rootDir);
            } catch (IOException ex)
            {
                throw new IOExceptionUnchecked(ex);
            }
            return new File(rootDir, incomingDataSetDirectory.getName());
        }

        public void commit(File incomingDataSetDirectory, File storedDataDirectory)
        {
            calledCommitCount++;
        }

        public UnstoreDataAction rollback(File incomingDataSetDirectory, File storedDataDirectory,
                Throwable exception)
        {
            return null;
        }

        public StorageFormat getStorageFormat()
        {
            return StorageFormat.PROPRIETARY;
        }

        public File tryGetProprietaryData(File storedDataDirectory)
        {
            return null;
        }
    }

    private class TestingDataSetHandler extends AbstractOmniscientTopLevelDataSetRegistrator
            implements IDataSetRegistrationDetailsFactory<DataSetInformation>
    {

        /**
         * @param globalState
         */
        protected TestingDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState)
        {
            super(globalState);
        }

        @Override
        protected void handleDataSet(File dataSetFile, DataSetRegistrationService aService)
                throws Throwable
        {

        }

        public DataSetRegistrationService createNewService()
        {
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction =
                    new IDelegatedActionWithResult<Boolean>()
                        {
                            public Boolean execute()
                            {
                                return true; // do nothing
                            }
                        };
            return createDataSetRegistrationService(cleanAfterwardsAction);
        }

        /**
         * Factory method that creates a new registration details object.
         */
        public DataSetRegistrationDetails<DataSetInformation> createDataSetRegistrationDetails()
        {
            DataSetRegistrationDetails<DataSetInformation> registrationDetails =
                    new DataSetRegistrationDetails<DataSetInformation>();
            registrationDetails.setDataSetInformation(createDataSetInformation());
            return registrationDetails;
        }

        /**
         * Factory method that creates a new data set information object.
         */
        public DataSetInformation createDataSetInformation()
        {
            DataSetInformation dataSetInfo = new DataSetInformation();
            dataSetInfo.setInstanceCode(getRegistratorState().getHomeDatabaseInstance().getCode());
            dataSetInfo.setInstanceUUID(getRegistratorState().getHomeDatabaseInstance().getUuid());

            return dataSetInfo;
        }
    }

    private void checkContentsOfFile(File dst)
    {
        assertTrue("The file should exist", dst.exists());
        assertEquals("hello world\n", FileUtilities.loadToString(dst));
    }

    private void fillContentsOfSource()
    {
        FileUtilities.writeToFile(srcFile, "hello world");
    }

    protected void createTransaction()
    {
        createHandler();
        tr =
                new DataSetRegistrationTransaction<DataSetInformation>(rollbackStack,
                        workingDirectory, stagingDirectory, service, handler);
    }
}
