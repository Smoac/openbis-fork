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

import static ch.systemsx.cisd.common.Constants.IS_FINISHED_PREFIX;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.python.core.PyFunction;
import org.python.util.PythonInterpreter;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.mail.From;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm;
import ch.systemsx.cisd.etlserver.IStorageProcessor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class JythonTopLevelDataSetRegistratorTest extends AbstractFileSystemTestCase
{
    private static final String SCRIPTS_FOLDER =
            "sourceTest/java/ch/systemsx/cisd/etlserver/registrator/";

    private static final String SHARED_SCRIPT_PATH = SCRIPTS_FOLDER + "script.py";

    private static final String DATA_SET_CODE = "data-set-code";

    private static final String DATABASE_INSTANCE_UUID = "db-uuid";

    private static final DataSetType DATA_SET_TYPE = new DataSetType("O1");

    private JythonTopLevelDataSetHandler<DataSetInformation> handler;

    private Mockery context;

    private IEncapsulatedOpenBISService openBisService;

    private IMailClient mailClient;

    private IDataSetValidator dataSetValidator;

    private File incomingDataSetFile;

    private File markerFile;

    private File subDataSet1;

    private File subDataSet2;

    private boolean didDataSetRollbackHappen;

    private boolean didServiceRollbackHappen;

    private BufferedAppender logAppender;

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

        logAppender = new BufferedAppender();

        didDataSetRollbackHappen = false;
        didServiceRollbackHappen = false;
    }

    @Test
    public void testSimpleTransaction()
    {
        setUpHomeDataBaseExpectations();
        Properties properties = createThreadProperties(SCRIPTS_FOLDER + "simple-transaction.py");
        final File stagingDir = new File(workingDirectory, "staging");
        properties.setProperty(DataSetRegistrationService.STAGING_DIR, stagingDir.getPath());
        createHandler(properties, false, true);
        createData();
        ExperimentBuilder builder = new ExperimentBuilder().identifier("/SPACE/PROJECT/EXP");
        final Experiment experiment = builder.getExperiment();
        final RecordingMatcher<DataSetInformation> dataSetInfo =
                new RecordingMatcher<DataSetInformation>();
        final RecordingMatcher<NewExternalData> dataSet = new RecordingMatcher<NewExternalData>();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE));
                    atLeast(1).of(openBisService).tryToGetExperiment(
                            new ExperimentIdentifierFactory(experiment.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment));

                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                            new File(new File(stagingDir, DATA_SET_CODE), "sub_data_set_1"));
                    one(openBisService).registerDataSet(with(dataSetInfo), with(dataSet));
                }
            });

        handler.handle(markerFile);

        assertEquals(1, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(DATA_SET_CODE, dataSetInfo.recordedObject().getDataSetCode());
        assertEquals(DATA_SET_TYPE, dataSetInfo.recordedObject().getDataSetType());
        assertEquals(experiment.getIdentifier(), dataSetInfo.recordedObject()
                .getExperimentIdentifier().toString());
        assertEquals(DATA_SET_CODE, dataSet.recordedObject().getCode());
        assertEquals(DATA_SET_TYPE, dataSet.recordedObject().getDataSetType());
        File datasetLocation =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, DATA_SET_CODE,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        DATABASE_INSTANCE_UUID);
        assertEquals(FileUtilities.getRelativeFile(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID), datasetLocation), dataSet.recordedObject()
                .getLocation());
        assertEquals(1, MockStorageProcessor.instance.calledCommitCount);
        assertEquals(datasetLocation, MockStorageProcessor.instance.rootDirs.get(0));
        File incomingDir = MockStorageProcessor.instance.incomingDirs.get(0);
        assertEquals(new File(new File(stagingDir, DATA_SET_CODE), "sub_data_set_1"), incomingDir);
        assertEquals("hello world1",
                FileUtilities.loadToString(new File(datasetLocation, "read1.me")).trim());
        context.assertIsSatisfied();
    }

    @Test
    public void testSimpleTransactionRollback()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadProperties(SCRIPTS_FOLDER + "simple-transaction-rollback.py");
        final File stagingDir = new File(workingDirectory, "staging");
        properties.setProperty(DataSetRegistrationService.STAGING_DIR, stagingDir.getPath());
        createHandler(properties, false, true);
        createData();
        ExperimentBuilder builder = new ExperimentBuilder().identifier("/SPACE/PROJECT/EXP");
        final Experiment experiment = builder.getExperiment();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE));
                    atLeast(1).of(openBisService).tryToGetExperiment(
                            new ExperimentIdentifierFactory(experiment.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment));
                }
            });

        handler.handle(markerFile);
        assertEquals(0, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(0, MockStorageProcessor.instance.calledCommitCount);
        assertEquals("[]", Arrays.asList(stagingDir.list()).toString());
        assertEquals(
                "hello world1",
                FileUtilities.loadToString(
                        new File(workingDirectory, "data_set/sub_data_set_1/read1.me")).trim());
        assertEquals(
                "hello world2",
                FileUtilities.loadToString(
                        new File(workingDirectory, "data_set/sub_data_set_2/read2.me")).trim());

        context.assertIsSatisfied();
    }

    @Test
    public void testTwoSimpleTransactions()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadProperties(SCRIPTS_FOLDER + "two-simple-transactions.py");
        final File stagingDir = new File(workingDirectory, "staging");
        properties.setProperty(DataSetRegistrationService.STAGING_DIR, stagingDir.getPath());
        createHandler(properties, false, true);
        createData();
        ExperimentBuilder builder1 = new ExperimentBuilder().identifier("/SPACE/PROJECT/EXP1");
        final Experiment experiment1 = builder1.getExperiment();
        ExperimentBuilder builder2 = new ExperimentBuilder().identifier("/SPACE/PROJECT/EXP2");
        final Experiment experiment2 = builder2.getExperiment();
        final RecordingMatcher<DataSetInformation> dataSetInfo1 =
                new RecordingMatcher<DataSetInformation>();
        final RecordingMatcher<NewExternalData> dataSet1 = new RecordingMatcher<NewExternalData>();
        final RecordingMatcher<DataSetInformation> dataSetInfo2 =
                new RecordingMatcher<DataSetInformation>();
        final RecordingMatcher<NewExternalData> dataSet2 = new RecordingMatcher<NewExternalData>();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE + 1));

                    atLeast(1).of(openBisService).tryToGetExperiment(
                            new ExperimentIdentifierFactory(experiment1.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment1));

                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                            new File(new File(stagingDir, DATA_SET_CODE + 1), "sub_data_set_1"));
                    one(openBisService).registerDataSet(with(dataSetInfo1), with(dataSet1));

                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE + 2));

                    atLeast(1).of(openBisService).tryToGetExperiment(
                            new ExperimentIdentifierFactory(experiment2.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment2));

                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                            new File(new File(stagingDir, DATA_SET_CODE + 2), "sub_data_set_2"));
                    one(openBisService).registerDataSet(with(dataSetInfo2), with(dataSet2));
                }
            });

        handler.handle(markerFile);

        assertEquals(2, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(2, MockStorageProcessor.instance.calledCommitCount);
        assertEquals(DATA_SET_CODE + 1, dataSetInfo1.recordedObject().getDataSetCode());
        assertEquals(DATA_SET_TYPE, dataSetInfo1.recordedObject().getDataSetType());
        assertEquals(experiment1.getIdentifier(), dataSetInfo1.recordedObject()
                .getExperimentIdentifier().toString());
        assertEquals(DATA_SET_CODE + 1, dataSet1.recordedObject().getCode());
        assertEquals(DATA_SET_TYPE, dataSet1.recordedObject().getDataSetType());
        File datasetLocation1 =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, DATA_SET_CODE + 1,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID, DATABASE_INSTANCE_UUID);
        assertEquals(FileUtilities.getRelativeFile(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID), datasetLocation1), dataSet1.recordedObject()
                .getLocation());
        assertEquals(datasetLocation1, MockStorageProcessor.instance.rootDirs.get(0));
        File incomingDir1 = MockStorageProcessor.instance.incomingDirs.get(0);
        assertEquals(new File(new File(stagingDir, DATA_SET_CODE + 1), "sub_data_set_1"),
                incomingDir1);
        assertEquals("hello world1",
                FileUtilities.loadToString(new File(datasetLocation1, "read1.me")).trim());
        assertEquals(DATA_SET_CODE + 2, dataSetInfo2.recordedObject().getDataSetCode());
        assertEquals(DATA_SET_TYPE, dataSetInfo2.recordedObject().getDataSetType());
        assertEquals(experiment2.getIdentifier(), dataSetInfo2.recordedObject()
                .getExperimentIdentifier().toString());
        assertEquals(DATA_SET_CODE + 2, dataSet2.recordedObject().getCode());
        assertEquals(DATA_SET_TYPE, dataSet2.recordedObject().getDataSetType());
        File datasetLocation2 =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, DATA_SET_CODE + 2,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID, DATABASE_INSTANCE_UUID);
        assertEquals(FileUtilities.getRelativeFile(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID), datasetLocation2), dataSet2.recordedObject()
                .getLocation());
        assertEquals(datasetLocation2, MockStorageProcessor.instance.rootDirs.get(1));
        File incomingDir2 = MockStorageProcessor.instance.incomingDirs.get(1);
        assertEquals(new File(new File(stagingDir, DATA_SET_CODE + 2), "sub_data_set_2"),
                incomingDir2);
        assertEquals("hello world2",
                FileUtilities.loadToString(new File(datasetLocation2, "read2.me")).trim());
        context.assertIsSatisfied();
    }

    @Test
    public void testDataSetRegistration()
    {
        setUpOpenBisExpectations();

        createHandler();
        createData();

        setUpDataSetValidatorExpectations();
        setUpMailClientExpectations();

        handler.handle(markerFile);

        // Causes problems in Hudson
        // assertTrue(
        // logAppender.getLogContent(),
        // logAppender.getLogContent().endsWith(
        // ".MARKER_is_finished_data_set' has been removed."));

        assertEquals(2, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(2, MockStorageProcessor.instance.calledCommitCount);
        assertEquals(
                "DataSetInformation{sampleCode=<null>,properties={},dataSetType=O1,shareId=1,instanceUUID=db-uuid,instanceCode=<null>,spaceCode=<null>,experimentIdentifier=/SPACE/PROJECT/EXP-CODE,isCompleteFlag=U,extractableData=ExtractableData{productionDate=<null>,dataProducerCode=<null>,parentDataSetCodes=[data-set-code1],dataSetProperties=[],code=data-set-code2},uploadingUserEmailOrNull=<null>,uploadingUserIdOrNull=<null>}",
                MockStorageProcessor.instance.dataSetInfoString);
    }

    @Test
    public void testScriptDies()
    {
        setUpOpenBisExpectations();

        Properties threadProperties =
                createThreadProperties("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/dying-script.py");

        createHandler(threadProperties, false);
        createData();

        setUpDataSetValidatorExpectations();
        setUpMailClientExpectations();

        handler.handle(markerFile);

        assertTrue(
                logAppender.getLogContent(),
                logAppender
                        .getLogContent()
                        .endsWith(
                                "Error in jython dropbox has occured:\n"
                                        + "Traceback (innermost last):\n"
                                        + "  File \"<string>\", line 15, in ?\n"
                                        + "AttributeError: 'NoneType' object has no attribute 'non_existant_function'"));

        assertEquals(0, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(0, MockStorageProcessor.instance.calledCommitCount);
    }

    @Test
    public void testRegistrationFails()
    {
        setUpOpenBisExpectations();

        // Create a handler that throws an exception during registration
        Properties threadProperties = createThreadProperties(SHARED_SCRIPT_PATH);
        createHandler(threadProperties, true);

        createData();

        setUpDataSetValidatorExpectations();
        setUpMailClientExpectations();

        didDataSetRollbackHappen = false;
        didServiceRollbackHappen = false;

        handler.handle(markerFile);

        // Causes problems in Hudson
        // assertTrue(
        // logAppender.getLogContent(),
        // logAppender.getLogContent().endsWith(
        // ".MARKER_is_finished_data_set' has been removed."));

        assertEquals(2, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(0, MockStorageProcessor.instance.calledCommitCount);
        assertTrue("Data set rollback should have been invoked", didDataSetRollbackHappen);
        assertFalse("Service rollback should not have been invoked", didServiceRollbackHappen);
    }

    private void createData()
    {
        incomingDataSetFile = createDirectory(workingDirectory, "data_set");

        subDataSet1 = createDirectory(incomingDataSetFile, "sub_data_set_1");
        subDataSet2 = createDirectory(incomingDataSetFile, "sub_data_set_2");

        FileUtilities.writeToFile(new File(subDataSet1, "read1.me"), "hello world1");
        FileUtilities.writeToFile(new File(subDataSet2, "read2.me"), "hello world2");

        markerFile = new File(workingDirectory, IS_FINISHED_PREFIX + "data_set");
        FileUtilities.writeToFile(markerFile, "");
    }

    @Test
    public void testRollbackDataSetRegistration()
    {
        setUpOpenBisExpectations();

        // Create a handler that throws an exception during registration
        Properties threadProperties = createThreadProperties(SCRIPTS_FOLDER + "rollback-script.py");
        createHandler(threadProperties, true);

        createData();

        setUpDataSetValidatorExpectations();
        setUpMailClientExpectations();

        handler.handle(markerFile);

        assertEquals(2, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(0, MockStorageProcessor.instance.calledCommitCount);

        TestingDataSetHandler theHandler = (TestingDataSetHandler) handler;
        assertTrue("Python data set rollback should have run",
                theHandler.didRollbackDataSetRegistrationFunctionRun);
        assertFalse("Python service rollback should not have run",
                theHandler.didRollbackServiceFunctionRun);
    }

    @Test
    public void testRollbackService()
    {
        setUpOpenBisExpectations();

        // Create a handler that throws an exception during registration
        Properties threadProperties =
                createThreadProperties(SCRIPTS_FOLDER + "rollback-dying-script.py");
        createHandler(threadProperties, false);

        createData();

        setUpDataSetValidatorExpectations();
        setUpMailClientExpectations();

        handler.handle(markerFile);

        assertEquals(0, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(0, MockStorageProcessor.instance.calledCommitCount);

        assertTrue(
                logAppender.getLogContent(),
                logAppender
                        .getLogContent()
                        .endsWith(
                                "Error in jython dropbox has occured:\n"
                                        + "Traceback (innermost last):\n"
                                        + "  File \"<string>\", line 23, in ?\n"
                                        + "AttributeError: 'NoneType' object has no attribute 'non_existant_function'"));

        TestingDataSetHandler theHandler = (TestingDataSetHandler) handler;
        assertFalse(theHandler.didRollbackDataSetRegistrationFunctionRun);
        assertTrue(theHandler.didRollbackServiceFunctionRun);
    }

    @Test
    public void testScriptPathDeletedLater()
    {
        setUpHomeDataBaseExpectations();
        String scriptPath = "foo.py";
        Properties threadProperties = createThreadProperties(scriptPath);

        // test the situation where script has been deleted later
        File scriptFile = new File(scriptPath);
        FileUtilities.writeToFile(scriptFile, "x");
        createHandler(threadProperties, false);
        FileUtilities.delete(scriptFile);

        createData();

        handler.handle(markerFile);

        assertFalse(didDataSetRollbackHappen);
        assertTrue(didServiceRollbackHappen);
    }

    @Test
    public void testScriptPathMissing()
    {
        setUpHomeDataBaseExpectations();
        String scriptPath = "foo.py";
        Properties threadProperties = createThreadProperties(scriptPath);

        // it should not be possible to create a handler if script does not exist
        try
        {
            createHandler(threadProperties, false);
            fail("The script should does not exist");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals(ex.getMessage(), "Script file 'foo.py' does not exist!");
        }
    }

    private Properties createThreadProperties(String scriptPath)
    {
        Properties threadProperties = new Properties();
        threadProperties.put(ThreadParameters.INCOMING_DIR, "incoming");
        threadProperties.put(ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION,
                ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE);
        threadProperties.put(ThreadParameters.DELETE_UNIDENTIFIED_KEY, "false");
        threadProperties.put(IStorageProcessor.STORAGE_PROCESSOR_KEY,
                MockStorageProcessor.class.getName());
        threadProperties.put(JythonTopLevelDataSetHandler.SCRIPT_PATH_KEY, scriptPath);
        return threadProperties;
    }

    @Test
    public void testNoScriptPath()
    {
        setUpHomeDataBaseExpectations();

        // omit the script path
        Properties threadProperties = new Properties();
        threadProperties.put(ThreadParameters.INCOMING_DIR, "incoming");
        threadProperties.put(ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION,
                ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE);
        threadProperties.put(ThreadParameters.DELETE_UNIDENTIFIED_KEY, "false");
        threadProperties.put(IStorageProcessor.STORAGE_PROCESSOR_KEY,
                MockStorageProcessor.class.getName());

        try
        {
            createHandler(threadProperties, false);
            fail("Should not be able to create the handler without specifiying a script");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals(
                    "Given key 'script-path' not found in properties '[delete-unidentified, storage-processor, incoming-data-completeness-condition, incoming-dir]'",
                    ex.getMessage());
        }
    }

    private File createDirectory(File parentDir, String directoryName)
    {
        final File file = new File(parentDir, directoryName);
        file.mkdir();
        return file;
    }

    private void createHandler()
    {
        Properties threadProperties = createThreadProperties(SHARED_SCRIPT_PATH);

        createHandler(threadProperties, false);
    }

    private void createHandler(Properties threadProperties, final boolean registrationShouldFail)
    {
        createHandler(threadProperties, registrationShouldFail, false);
    }

    private void createHandler(Properties threadProperties, final boolean registrationShouldFail,
            boolean shouldReThrowException)
    {
        TopLevelDataSetRegistratorGlobalState globalState = createGlobalState(threadProperties);

        handler =
                new TestingDataSetHandler(globalState, registrationShouldFail,
                        shouldReThrowException);
    }

    private TopLevelDataSetRegistratorGlobalState createGlobalState(Properties threadProperties)
    {
        ThreadParameters threadParameters =
                new ThreadParameters(threadProperties, "jython-handler-test");

        TopLevelDataSetRegistratorGlobalState globalState =
                new TopLevelDataSetRegistratorGlobalState("dss", ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        workingDirectory, openBisService, mailClient, dataSetValidator, true,
                        threadParameters);
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

    private void setUpOpenBisExpectations()
    {
        setUpHomeDataBaseExpectations();
        context.checking(new Expectations()
            {
                {
                    oneOf(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE + 1));

                    oneOf(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE + 2));

                    Experiment experiment = new Experiment();
                    experiment.setIdentifier("/SPACE/PROJECT/EXP-CODE");
                    experiment.setCode("EXP-CODE");
                    Person registrator = new Person();
                    registrator.setEmail("email@email.com");
                    experiment.setRegistrator(registrator);

                    exactly(3).of(openBisService).tryToGetExperiment(
                            new ExperimentIdentifierFactory("/SPACE/PROJECT/EXP-CODE")
                                    .createIdentifier());
                    will(returnValue(experiment));

                    exactly(2).of(openBisService).registerDataSet(
                            with(any(DataSetInformation.class)), with(any(NewExternalData.class)));
                }
            });
    }

    private void setUpDataSetValidatorExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE, subDataSet1);
                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE, subDataSet2);
                }
            });
    }

    private void setUpMailClientExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    exactly(2).of(mailClient).sendMessage(with(any(String.class)),
                            with(any(String.class)), with(aNull(String.class)),
                            with(aNull(From.class)), with(any(String[].class)));
                }
            });
    }

    public static final class MockStorageProcessor implements IStorageProcessor
    {
        static MockStorageProcessor instance;

        int calledGetStoreRootDirectoryCount = 0;

        int calledCommitCount = 0;

        File storeRootDirectory;

        String dataSetInfoString;

        private List<File> incomingDirs = new ArrayList<File>();

        private List<File> rootDirs = new ArrayList<File>();

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
            incomingDirs.add(incomingDataSetDirectory);
            rootDirs.add(rootDir);
            dataSetInfoString = dataSetInformation.toString();
            try
            {
                FileUtils.copyDirectory(incomingDataSetDirectory, rootDir);
            } catch (IOException ex)
            {
                throw new IOExceptionUnchecked(ex);
            }
            return rootDir;
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

    private class TestingDataSetHandler extends JythonTopLevelDataSetHandler<DataSetInformation>
    {
        private final boolean shouldRegistrationFail;

        private final boolean shouldReThrowRollbackException;

        private boolean didRollbackServiceFunctionRun = false;

        private boolean didRollbackDataSetRegistrationFunctionRun = false;

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
                DataSetRegistrationAlgorithm registrationAlgorithm, Throwable throwable)
        {
            super.rollback(service, registrationAlgorithm, throwable);
            didDataSetRollbackHappen = true;
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
            }
            throwable.printStackTrace();
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
                    (Boolean) interpreter.get("didRollbackServiceFunctionRun", Boolean.class);
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
                    (Boolean) interpreter.get("didRollbackServiceFunctionRun", Boolean.class);
        }

    }
}
