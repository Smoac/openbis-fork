/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.ExpectationError;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.JavaMailProperties;
import ch.systemsx.cisd.common.test.LogMonitoringAppender;
import ch.systemsx.cisd.openbis.dss.generic.server.EncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.server.SessionTokenManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcessingInstructionDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.ProcedureTypeCode;

/**
 * Test cases for corresponding {@link TransferredDataSetHandler} class.
 * 
 * @author Franz-Josef Elmer
 */
@Friend(toClasses =
    { TransferredDataSetHandler.class, IdentifiedDataStrategy.class })
public final class TransferredDataSetHandlerTest extends AbstractFileSystemTestCase
{

    private static final String SAMPLE_CODE = "sample1";

    private static final String LOG_MSG_OF_DATA_FORMAT_MISMATCH =
            "Configuration Error: no processing initiated for data set";

    private static final String FOLDER_NAME = "folder";

    private static final String DATA2_NAME = "data2";

    private static final String DATA1_NAME = "data1";

    private static final String SESSION_TOKEN = "sessionToken";

    private static final String DATA_SET_CODE = "4711-42";

    private static final String PARENT_DATA_SET_CODE = "4711-1";

    private static final LocatorType LOCATOR_TYPE = new LocatorType("L1");

    private static final DataSetType DATA_SET_TYPE = new DataSetType("O1");

    private static final FileFormatType FILE_FORMAT_TYPE = new FileFormatType("FF1");

    private static final String DATA_PRODUCER_CODE = "microscope";

    private static final Date DATA_PRODUCTION_DATE = new Date(2001);

    private static final String EXAMPLE_PROCESSOR_ID = ProcedureTypeCode.DATA_ACQUISITION.getCode();

    private static final class ExternalDataMatcher extends BaseMatcher<ExternalData>
    {
        private final ExternalData expectedData;

        public ExternalDataMatcher(final ExternalData externalData)
        {
            this.expectedData = externalData;
        }

        public void describeTo(final Description description)
        {
            description.appendValue(expectedData);
        }

        public boolean matches(final Object item)
        {
            if (item instanceof ExternalData == false)
            {
                return false;
            }
            final ExternalData data = (ExternalData) item;
            assertEquals(expectedData.getCode(), data.getCode());
            assertEquals(expectedData.getDataProducerCode(), data.getDataProducerCode());
            assertEquals(expectedData.getLocation(), data.getLocation());
            assertEquals(expectedData.getLocatorType(), data.getLocatorType());
            assertEquals(expectedData.getFileFormatType(), data.getFileFormatType());
            assertEquals(expectedData.getDataSetType(), data.getDataSetType());
            assertEquals(expectedData.getParentDataSetCode(), data.getParentDataSetCode());
            assertEquals(expectedData.getProductionDate(), data.getProductionDate());
            assertEquals(expectedData.getStorageFormat(), data.getStorageFormat());
            return true;
        }

        // This method throws an ExpectationError instead of the usual AssertionError.
        // Reason: TransferredDataSetHandler catches AssertionError.
        private void assertEquals(final Object expected, final Object actual)
        {
            if (expected == null ? expected != actual : expected.equals(actual) == false)
            {
                throw new ExpectationError("Expecting <" + expected + "> but got <" + actual + ">",
                        this);
            }
        }
    }

    /**
     * This wrapper is needed because the class name of the wrapped class is not known.
     */
    private static final class MockDataSetInfoExtractor implements IDataSetInfoExtractor
    {
        private final IDataSetInfoExtractor codeExtractor;

        MockDataSetInfoExtractor(final IDataSetInfoExtractor codeExtractor)
        {
            this.codeExtractor = codeExtractor;
        }

        public DataSetInformation getDataSetInformation(final File incomingDataSetPath,
                IEncapsulatedOpenBISService openbisService) throws UserFailureException,
                EnvironmentFailureException
        {
            return codeExtractor.getDataSetInformation(incomingDataSetPath, null);
        }
    }

    private Mockery context;

    private IDataSetInfoExtractor dataSetInfoExtractor;

    private ITypeExtractor typeExtractor;

    private IStorageProcessor storageProcessor;

    private IETLLIMSService limsService;

    private TransferredDataSetHandler handler;

    private File data1;

    private File isFinishedData1;

    private DataSetInformation dataSetInformation;

    private File targetFolder;

    private ExternalData targetData1;

    private File folder;

    private File isFinishedFolder;

    private File data2;

    private IMailClient mailClient;

    private IProcessorFactory processorFactory;

    private IProcessor processor;

    private BufferedAppender logRecorder;

    private DatabaseInstancePE homeDatabaseInstance;

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

    @Override
    @BeforeMethod
    public final void setUp() throws IOException
    {
        super.setUp();
        LogInitializer.init();
        data1 = new File(workingDirectory, DATA1_NAME);
        FileUtils.touch(data1);
        isFinishedData1 =
                new File(workingDirectory, Constants.IS_FINISHED_PREFIX + data1.getName());
        FileUtils.touch(isFinishedData1);

        folder = new File(workingDirectory, FOLDER_NAME);
        folder.mkdir();
        data2 = new File(folder, DATA2_NAME);
        FileUtils.touch(data2);
        isFinishedFolder =
                new File(workingDirectory, Constants.IS_FINISHED_PREFIX + folder.getName());
        FileUtils.touch(isFinishedFolder);

        context = new Mockery();
        dataSetInfoExtractor = context.mock(IDataSetInfoExtractor.class);
        typeExtractor = context.mock(ITypeExtractor.class);
        final Properties properties = new Properties();
        properties.setProperty(JavaMailProperties.MAIL_SMTP_HOST, "host");
        properties.setProperty(JavaMailProperties.MAIL_FROM, "me");
        properties.setProperty(ETLDaemon.STOREROOT_DIR_KEY, workingDirectory.getPath());
        storageProcessor = context.mock(IStorageProcessor.class);
        limsService = context.mock(IETLLIMSService.class);
        mailClient = context.mock(IMailClient.class);
        processorFactory = context.mock(IProcessorFactory.class);
        processor = context.mock(IProcessor.class);
        final Map<String, IProcessorFactory> map = new HashMap<String, IProcessorFactory>();
        map.put(EXAMPLE_PROCESSOR_ID, processorFactory);
        final IETLServerPlugin plugin =
                new ETLServerPlugin(new MockDataSetInfoExtractor(dataSetInfoExtractor),
                        typeExtractor, storageProcessor);
        final EncapsulatedOpenBISService authorizedLimsService =
                new EncapsulatedOpenBISService(new SessionTokenManager(), limsService);
        authorizedLimsService.setUsername("u");
        authorizedLimsService.setPassword("p");
        handler =
                new TransferredDataSetHandler("dss", storageProcessor, plugin,
                        authorizedLimsService, mailClient, true, true, false);

        handler.setProcessorFactories(map);
        dataSetInformation = new DataSetInformation();
        final ExperimentIdentifier experimentIdentifier = new ExperimentIdentifier();
        experimentIdentifier.setExperimentCode("experiment1".toUpperCase());
        experimentIdentifier.setProjectCode("project1".toUpperCase());
        experimentIdentifier.setGroupCode("group1".toUpperCase());
        homeDatabaseInstance = new DatabaseInstancePE();
        homeDatabaseInstance.setCode("my-instance");
        homeDatabaseInstance.setUuid("1111-2222");
        dataSetInformation.setInstanceCode(homeDatabaseInstance.getCode());
        dataSetInformation.setInstanceUUID(homeDatabaseInstance.getUuid());
        dataSetInformation.setExperimentIdentifier(experimentIdentifier);
        dataSetInformation.setSampleCode(SAMPLE_CODE);
        dataSetInformation.setProducerCode(DATA_PRODUCER_CODE);
        dataSetInformation.setProductionDate(DATA_PRODUCTION_DATE);
        dataSetInformation.setDataSetCode(DATA_SET_CODE);
        dataSetInformation.setParentDataSetCode(PARENT_DATA_SET_CODE);
        targetFolder =
                IdentifiedDataStrategy.createBaseDirectory(workingDirectory, dataSetInformation);
        targetData1 = createTargetData(data1);
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.INFO);
    }

    private final String createLogMsgOfSuccess(final ExperimentIdentifier identifier,
            final SampleIdentifier sampleIdentifier)
    {
        return String.format(TransferredDataSetHandler.SUCCESSFULLY_REGISTERED_TEMPLATE,
                DATA_SET_CODE, sampleIdentifier, DATA_SET_TYPE.getCode(), identifier);
    }

    private final void assertLog(final String expectedLog)
    {
        assertEquals(expectedLog, normalize(logRecorder.getLogContent()));
    }

    private final String normalize(final String message)
    {
        return message.replace(workingDirectory.getAbsolutePath(), "/<wd>").replace(
                workingDirectory.getPath(), "<wd>").replace('\\', '/');
    }

    private final ExternalData createTargetData(final File dataSet)
    {
        final ExternalData data = new ExternalData();
        data.setLocation(getRelativeTargetFolder() + File.separator + dataSet.getName());
        data.setLocatorType(LOCATOR_TYPE);
        data.setDataSetType(DATA_SET_TYPE);
        data.setFileFormatType(FILE_FORMAT_TYPE);
        data.setStorageFormat(StorageFormat.BDS_DIRECTORY);
        data.setDataProducerCode(DATA_PRODUCER_CODE);
        data.setProductionDate(DATA_PRODUCTION_DATE);
        data.setCode(DATA_SET_CODE);
        data.setParentDataSetCode(PARENT_DATA_SET_CODE);
        return data;
    }

    private String getRelativeTargetFolder()
    {
        String absoluteTarget = targetFolder.getAbsolutePath();
        return absoluteTarget.substring(workingDirectory.getAbsolutePath().length() + 1);
    }

    // crates sample connected to an experiment
    private final static SamplePE createBaseSample(final DataSetInformation dataSetInformation)
    {
        final ExperimentPE baseExperiment = createExperiment(dataSetInformation);
        SamplePE sample = new SamplePE();
        sample.setCode("code");
        sample.setExperiment(baseExperiment);
        return sample;
    }

    private final static ExperimentPE createExperiment(final DataSetInformation dataSetInformation)
    {
        final ExperimentPE baseExperiment = new ExperimentPE();
        final ExperimentIdentifier experimentIdentifier =
                dataSetInformation.getExperimentIdentifier();
        baseExperiment.setCode(experimentIdentifier.getExperimentCode());
        final GroupPE group = new GroupPE();
        group.setCode(experimentIdentifier.getGroupCode());
        final ProjectPE project = new ProjectPE();
        project.setCode(experimentIdentifier.getProjectCode());
        project.setGroup(group);
        baseExperiment.setProject(project);
        final PersonPE person = new PersonPE();
        person.setEmail("john.doe@somewhere.com");
        baseExperiment.setRegistrator(person);
        baseExperiment.setProcessingInstructions(new ProcessingInstructionDTO[]
            { create() });
        return baseExperiment;
    }

    private final static ProcessingInstructionDTO create()
    {
        final ProcessingInstructionDTO processingInstruction = new ProcessingInstructionDTO();
        processingInstruction.setProcedureTypeCode(EXAMPLE_PROCESSOR_ID);
        return processingInstruction;
    }

    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    private final void prepareForStrategy(final File dataSet, final SamplePE samplePE)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataSetInfoExtractor).getDataSetInformation(dataSet, null);
                    will(returnValue(dataSetInformation));

                    one(limsService).getHomeDatabaseInstance(SESSION_TOKEN);
                    will(returnValue(homeDatabaseInstance));

                    one(storageProcessor).getStoreRootDirectory();
                    will(returnValue(workingDirectory));

                    one(limsService).tryToAuthenticate("u", "p");
                    will(returnValue(createSession()));

                    one(limsService).registerDataStoreServer(with(equal(SESSION_TOKEN)),
                            with(any(DataStoreServerInfo.class)));

                    atLeast(1).of(limsService).tryGetSampleWithExperiment(SESSION_TOKEN,
                            dataSetInformation.getSampleIdentifier());
                    will(returnValue(samplePE));

                    allowing(typeExtractor).getDataSetType(dataSet);
                    will(returnValue(DATA_SET_TYPE));
                }
            });
    }

    private final void prepareForStrategyIDENTIFIED(final File dataSet,
            final ExternalData targetData, final SamplePE samplePE)
    {
        prepareForStrategy(dataSet, samplePE);
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryToGetPropertiesOfTopSampleRegisteredFor(SESSION_TOKEN,
                            dataSetInformation.getSampleIdentifier());
                    will(returnValue(new SamplePropertyPE[0]));
                }
            });
    }

    private final void prepareForRegistration(final File dataSet)
    {
        context.checking(new Expectations()
            {
                {
                    one(typeExtractor).getLocatorType(dataSet);
                    will(returnValue(LOCATOR_TYPE));

                    one(typeExtractor).getFileFormatType(dataSet);
                    will(returnValue(FILE_FORMAT_TYPE));

                    one(typeExtractor).getProcessorType(dataSet);
                    will(returnValue(EXAMPLE_PROCESSOR_ID));

                    one(typeExtractor).isMeasuredData(dataSet);
                    will(returnValue(true));
                }
            });
    }

    private final String getNotificationEmailContent(final DataSetInformation dataset,
            final String dataSetCode)
    {
        final StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(createLogMsgOfSuccess(dataset.getExperimentIdentifier(), dataset
                .getSampleIdentifier())
                + OSUtilities.LINE_SEPARATOR + OSUtilities.LINE_SEPARATOR);
        stringBuffer.append("Experiment Identifier:\t"
                + dataSetInformation.getExperimentIdentifier() + OSUtilities.LINE_SEPARATOR);
        stringBuffer.append("Producer Code:\t" + dataset.getProducerCode()
                + OSUtilities.LINE_SEPARATOR);
        if (dataset.getProductionDate() != null)
        {
            stringBuffer.append("Production Date:\t" + dataset.getProductionDate()
                    + OSUtilities.LINE_SEPARATOR);
        }
        if (StringUtils.isNotBlank(dataset.getParentDataSetCode()))
        {
            stringBuffer.append("Parent Data Set:\t" + dataset.getParentDataSetCode()
                    + OSUtilities.LINE_SEPARATOR);
        }
        stringBuffer.append("Is complete:\t" + dataset.getIsCompleteFlag()
                + OSUtilities.LINE_SEPARATOR);

        return stringBuffer.toString();
    }

    private final void checkSuccessEmailNotification(final Expectations expectations,
            final DataSetInformation dataSet, final String dataSetCode, final String recipient)
    {
        expectations.one(mailClient).sendMessage(
                String.format(TransferredDataSetHandler.EMAIL_SUBJECT_TEMPLATE, dataSet
                        .getExperimentIdentifier().getExperimentCode()),
                getNotificationEmailContent(dataSet, dataSetCode), null, recipient);
    }

    @Test
    public final void testDataSetFileIsReadOnly()
    {
        data1.setReadOnly();
        context.checking(new Expectations()
            {
                {
                    one(storageProcessor).getStoreRootDirectory();
                    will(returnValue(workingDirectory));
                }
            });

        try
        {
            handler.handle(isFinishedData1);
            fail("EnvironmentFailureException expected");
        } catch (final EnvironmentFailureException e)
        {
            final String normalizedMessage = normalize(e.getMessage());
            assertEquals("Error moving path 'data1' from '<wd>' to '<wd>': "
                    + "Incoming data set directory '<wd>/data1' is not writable.",
                    normalizedMessage);
        }

        context.assertIsSatisfied();
    }

    @Test
    public final void testNoDataSetInfoCouldBeExtractedFromDataSetFileName()
    {
        context.checking(new Expectations()
            {
                {
                    one(dataSetInfoExtractor).getDataSetInformation(data1, null);
                    will(returnValue(new DataSetInformation()));
                }
            });

        try
        {
            handler.handle(isFinishedData1);
            fail("ConfigurationFailureException expected.");
        } catch (final ConfigurationFailureException e)
        {
            final String normalizedMessage = normalize(e.getMessage());
            assertEquals("Data Set Information Extractor 'MockDataSetInfoExtractor' extracted "
                    + "no sample code for incoming data set '<wd>/data1' "
                    + "(extractor contract violation).", normalizedMessage);
        }

        context.assertIsSatisfied();
    }

    @Test
    public final void testBaseDirectoryCouldNotBeCreated() throws IOException
    {
        FileUtils.touch(IdentifiedDataStrategy.createBaseDirectory(workingDirectory,
                dataSetInformation));
        prepareForStrategyIDENTIFIED(data1, null, createBaseSample(dataSetInformation));
        try
        {
            handler.handle(isFinishedData1);
            fail("Base directory could not be created because"
                    + " there is already a file with the same name.");
        } catch (final EnvironmentFailureException ex)
        {
            assertTrue(ex.getMessage().indexOf(
                    IdentifiedDataStrategy.STORAGE_LAYOUT_ERROR_MSG_PREFIX) > -1);
        }
        assertLog("INFO  OPERATION.DataStrategyStore - "
                + "Identified that database knows experiment '/GROUP1/PROJECT1/EXPERIMENT1' "
                + "and sample 'MY-INSTANCE:/sample1'.");
        context.assertIsSatisfied();
    }

    @Test
    public final void testMoveIdentifiedDataSetFile()
    {
        final SamplePE baseSample = createBaseSample(dataSetInformation);
        final ExperimentPE baseExperiment = baseSample.getExperiment();
        baseExperiment.setProcessingInstructions(new ProcessingInstructionDTO[]
            { create() });
        final File baseDir = targetFolder;
        prepareForStrategyIDENTIFIED(data1, targetData1, baseSample);
        prepareForRegistration(data1);
        context.checking(new Expectations()
            {
                {
                    one(limsService).registerDataSet(with(equal(SESSION_TOKEN)),
                            with(equal(dataSetInformation.getSampleIdentifier())),
                            with(new ExternalDataMatcher(targetData1)));

                    checkSuccessEmailNotification(this, dataSetInformation, DATA_SET_CODE,
                            baseExperiment.getRegistrator().getEmail());

                    allowing(storageProcessor).getStorageFormat();
                    will(returnValue(StorageFormat.BDS_DIRECTORY));
                    one(storageProcessor).storeData(baseSample, dataSetInformation, typeExtractor,
                            mailClient, data1, baseDir);
                    final File finalDataSetPath = new File(baseDir, DATA1_NAME);
                    will(returnValue(finalDataSetPath));

                    one(processorFactory).createProcessor();
                    will(returnValue(processor));
                    allowing(processor).getRequiredInputDataFormat();
                    will(returnValue(StorageFormat.BDS_DIRECTORY));
                    one(processor).initiateProcessing(
                            baseExperiment.getProcessingInstructions()[0], dataSetInformation,
                            finalDataSetPath);
                }
            });
        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, String
                        .format(createLogMsgOfSuccess(dataSetInformation.getExperimentIdentifier(),
                                dataSetInformation.getSampleIdentifier())));
        handler.handle(isFinishedData1);
        final File dataSetPath = createDatasetDir(baseDir);
        assertEquals(true, dataSetPath.isDirectory());
        appender.verifyLogHasHappened();
        context.assertIsSatisfied();
    }

    private File createDatasetDir(final File baseDir)
    {
        return new File(baseDir.getParentFile(), DATA_SET_CODE);
    }

    @Test
    public final void testMoveIdentifiedDataSetFileToBDSContainerWithOriginalData()
    {
        final SamplePE baseSample = createBaseSample(dataSetInformation);
        final ExperimentPE baseExperiment = baseSample.getExperiment();
        baseExperiment.setProcessingInstructions(new ProcessingInstructionDTO[]
            { create() });
        final File baseDir = targetFolder;
        prepareForStrategyIDENTIFIED(data1, targetData1, baseSample);
        prepareForRegistration(data1);
        context.checking(new Expectations()
            {
                {
                    one(limsService).registerDataSet(with(equal(SESSION_TOKEN)),
                            with(equal(dataSetInformation.getSampleIdentifier())),
                            with(new ExternalDataMatcher(targetData1)));

                    checkSuccessEmailNotification(this, dataSetInformation, DATA_SET_CODE,
                            baseExperiment.getRegistrator().getEmail());

                    allowing(storageProcessor).getStorageFormat();
                    will(returnValue(StorageFormat.BDS_DIRECTORY));
                    one(storageProcessor).storeData(baseSample, dataSetInformation, typeExtractor,
                            mailClient, data1, baseDir);
                    final File finalDataSetPath = new File(baseDir, DATA1_NAME);
                    will(returnValue(finalDataSetPath));

                    one(processorFactory).createProcessor();
                    will(returnValue(processor));
                    allowing(processor).getRequiredInputDataFormat();
                    will(returnValue(StorageFormat.PROPRIETARY));
                    one(storageProcessor).tryGetProprietaryData(finalDataSetPath);
                    final File finalOriginalDataSetPath = new File(finalDataSetPath, "original");
                    will(returnValue(finalOriginalDataSetPath));
                    one(processor).initiateProcessing(
                            baseExperiment.getProcessingInstructions()[0], dataSetInformation,
                            finalOriginalDataSetPath);
                }
            });
        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, createLogMsgOfSuccess(
                        dataSetInformation.getExperimentIdentifier(), dataSetInformation
                                .getSampleIdentifier()));
        handler.handle(isFinishedData1);
        final File dataSetPath = createDatasetDir(baseDir);
        assertEquals(true, dataSetPath.isDirectory());
        appender.verifyLogHasHappened();
        context.assertIsSatisfied();
    }

    @Test
    public final void testMoveIdentifiedDataSetFileButMismatchOfDataFormat()
    {
        final SamplePE baseSample = createBaseSample(dataSetInformation);
        final ExperimentPE baseExperiment = baseSample.getExperiment();
        baseExperiment.setProcessingInstructions(new ProcessingInstructionDTO[]
            { create() });
        final File baseDir = targetFolder;
        targetData1.setStorageFormat(StorageFormat.PROPRIETARY);
        prepareForStrategyIDENTIFIED(data1, targetData1, baseSample);
        prepareForRegistration(data1);
        context.checking(new Expectations()
            {
                {
                    one(limsService).registerDataSet(with(equal(SESSION_TOKEN)),
                            with(equal(dataSetInformation.getSampleIdentifier())),
                            with(new ExternalDataMatcher(targetData1)));

                    checkSuccessEmailNotification(this, dataSetInformation, DATA_SET_CODE,
                            baseExperiment.getRegistrator().getEmail());

                    allowing(storageProcessor).getStorageFormat();
                    will(returnValue(StorageFormat.PROPRIETARY));
                    one(storageProcessor).storeData(baseSample, dataSetInformation, typeExtractor,
                            mailClient, data1, baseDir);
                    final File finalDataSetPath = new File(baseDir, DATA1_NAME);
                    will(returnValue(finalDataSetPath));

                    one(processorFactory).createProcessor();
                    will(returnValue(processor));
                    allowing(processor).getRequiredInputDataFormat();
                    will(returnValue(StorageFormat.BDS_DIRECTORY));

                }
            });
        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.NOTIFY,
                        LOG_MSG_OF_DATA_FORMAT_MISMATCH);
        handler.handle(isFinishedData1);
        final File dataSetPath = createDatasetDir(baseDir);
        assertEquals(true, dataSetPath.isDirectory());
        appender.verifyLogHasHappened();
        context.assertIsSatisfied();
    }

    @Test
    public final void testMoveIdentifiedDataSetFileButMismatchOfDataFormat2()
    {
        final SamplePE baseSample = createBaseSample(dataSetInformation);
        final ExperimentPE baseExperiment = baseSample.getExperiment();
        baseExperiment.setProcessingInstructions(new ProcessingInstructionDTO[]
            { create() });
        final File baseDir = targetFolder;
        prepareForStrategyIDENTIFIED(data1, targetData1, baseSample);
        prepareForRegistration(data1);
        context.checking(new Expectations()
            {
                {
                    one(limsService).registerDataSet(with(equal(SESSION_TOKEN)),
                            with(equal(dataSetInformation.getSampleIdentifier())),
                            with(new ExternalDataMatcher(targetData1)));

                    checkSuccessEmailNotification(this, dataSetInformation, DATA_SET_CODE,
                            baseExperiment.getRegistrator().getEmail());

                    allowing(storageProcessor).getStorageFormat();
                    will(returnValue(StorageFormat.BDS_DIRECTORY));
                    one(storageProcessor).storeData(baseSample, dataSetInformation, typeExtractor,
                            mailClient, data1, baseDir);
                    final File finalDataSetPath = new File(baseDir, DATA1_NAME);
                    will(returnValue(finalDataSetPath));

                    one(storageProcessor).tryGetProprietaryData(finalDataSetPath);
                    will(returnValue(null));

                    one(processorFactory).createProcessor();
                    will(returnValue(processor));

                    allowing(processor).getRequiredInputDataFormat();
                    will(returnValue(StorageFormat.PROPRIETARY));

                }
            });
        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.NOTIFY,
                        LOG_MSG_OF_DATA_FORMAT_MISMATCH);
        handler.handle(isFinishedData1);
        final File dataSetPath = createDatasetDir(baseDir);
        assertEquals(true, dataSetPath.isDirectory());
        appender.verifyLogHasHappened();
        context.assertIsSatisfied();
    }

    @Test
    public final void testMoveInvalidDataSetFile()
    {
        assertEquals(new File(workingDirectory, DATA1_NAME), data1);
        assertEquals(new File(new File(workingDirectory, FOLDER_NAME), DATA2_NAME), data2);
        assert data1.exists() && data2.exists();
        final SamplePE baseSample = createBaseSample(dataSetInformation);
        final ExperimentPE baseExperiment = baseSample.getExperiment();
        prepareForStrategy(data1, baseSample);
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryToGetPropertiesOfTopSampleRegisteredFor(SESSION_TOKEN,
                            dataSetInformation.getSampleIdentifier());
                    will(returnValue(null));

                    final ExperimentIdentifier experimentIdentifier =
                            dataSetInformation.getExperimentIdentifier();
                    final String subject =
                            String.format(DataStrategyStore.SUBJECT_FORMAT, experimentIdentifier);
                    final String body =
                            DataStrategyStore.createInvalidSampleCodeMessage(dataSetInformation);
                    final String email = baseExperiment.getRegistrator().getEmail();
                    one(mailClient).sendMessage(subject, body, null, email);
                }
            });
        handler.handle(isFinishedData1);

        assertEquals(false, isFinishedData1.exists());
        assertLog("ERROR OPERATION.DataStrategyStore - "
                + "Incoming data set '<wd>/data1' claims to belong to experiment "
                + "'/GROUP1/PROJECT1/EXPERIMENT1' and sample identifier 'MY-INSTANCE:/"
                + SAMPLE_CODE
                + "', "
                + "but according to the openBIS server there is no such sample for this experiment "
                + "(it has maybe been invalidated?). We thus consider it invalid."
                + OSUtilities.LINE_SEPARATOR + "INFO  OPERATION.FileRenamer - "
                + "Moving file 'data1' from '<wd>' to '<wd>/invalid/DataSetType_O1'.");

        context.assertIsSatisfied();
    }

    @Test
    public final void testMoveUnidentifiedDataSetFile()
    {
        assertEquals(new File(workingDirectory, DATA1_NAME), data1);
        assertEquals(new File(new File(workingDirectory, FOLDER_NAME), DATA2_NAME), data2);
        assert data1.exists() && data2.exists();
        prepareForStrategy(data1, null);
        final File toDir =
                new File(new File(workingDirectory, NamedDataStrategy
                        .getDirectoryName(DataStoreStrategyKey.UNIDENTIFIED)),
                        IdentifiedDataStrategy.createDataSetTypeDirectory(DATA_SET_TYPE));

        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, "to '" + toDir + "'");
        handler.handle(isFinishedData1);
        assertEquals(false, isFinishedData1.exists());
        appender.verifyLogHasHappened();

        checkNamedStrategyDirectoryPresent(DataStoreStrategyKey.UNIDENTIFIED, data1);

        context.assertIsSatisfied();
    }

    @Test
    public final void testMoveIdentifiedDataSetFolderButStoreDataFailed()
    {
        final SamplePE baseSample = createBaseSample(dataSetInformation);
        final File baseDir = targetFolder;
        prepareForStrategyIDENTIFIED(folder, targetData1, baseSample);
        context.checking(new Expectations()
            {
                {
                    one(processorFactory).createProcessor();
                    will(returnValue(processor));

                    one(typeExtractor).getProcessorType(folder);
                    will(returnValue(EXAMPLE_PROCESSOR_ID));

                    one(storageProcessor).storeData(baseSample, dataSetInformation, typeExtractor,
                            mailClient, folder, baseDir);
                    will(throwException(new Exception("Could store data by storage processor")));

                    one(storageProcessor).unstoreData(with(equal(folder)), with(equal(baseDir)));

                    allowing(typeExtractor).getLocatorType(folder);
                    allowing(typeExtractor).getDataSetType(folder);
                    allowing(typeExtractor).getFileFormatType(folder);
                    allowing(typeExtractor).isMeasuredData(folder);
                }
            });
        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, createLogMsgOfSuccess(
                        dataSetInformation.getExperimentIdentifier(), dataSetInformation
                                .getSampleIdentifier()));
        final LogMonitoringAppender appender2 =
                LogMonitoringAppender.addAppender(LogCategory.NOTIFY, String.format(
                        TransferredDataSetHandler.DATA_SET_STORAGE_FAILURE_TEMPLATE,
                        dataSetInformation));
        handler.handle(isFinishedFolder);

        appender.verifyLogHasNotHappened();
        appender2.verifyLogHasHappened();

        checkNamedStrategyDirectoryPresent(DataStoreStrategyKey.ERROR, folder);

        context.assertIsSatisfied();
    }

    private final void checkNamedStrategyDirectoryPresent(final DataStoreStrategyKey key,
            final File dataSet)
    {
        final File strategyDirectory =
                new File(workingDirectory, NamedDataStrategy.getDirectoryName(key));
        assertEquals(true, strategyDirectory.exists());
        final File dataSetTypeDir =
                new File(strategyDirectory, IdentifiedDataStrategy
                        .createDataSetTypeDirectory(DATA_SET_TYPE));
        assertEquals(true, dataSetTypeDir.exists());
        final File targetFile = new File(dataSetTypeDir, dataSet.getName());
        assertEquals(true, targetFile.exists());
        assertEquals(false, dataSet.exists());
    }

    @Test
    public void testMoveIdentifiedDataSetFolderButWebServiceRegistrationFailed()
    {
        final SamplePE baseSample = createBaseSample(dataSetInformation);
        final File baseDir = targetFolder;
        targetData1.setStorageFormat(null);
        prepareForStrategyIDENTIFIED(folder, targetData1, baseSample);
        prepareForRegistration(folder);
        context.checking(new Expectations()
            {
                {
                    one(processorFactory).createProcessor();
                    will(returnValue(processor));
                    one(storageProcessor).storeData(baseSample, dataSetInformation, typeExtractor,
                            mailClient, folder, baseDir);
                    will(returnValue(new File(baseDir, DATA1_NAME)));

                    one(limsService).registerDataSet(with(equal(SESSION_TOKEN)),
                            with(equal(dataSetInformation.getSampleIdentifier())),
                            with(new ExternalDataMatcher(targetData1)));
                    will(throwException(new EnvironmentFailureException(
                            "Could not register data set folder")));

                    one(storageProcessor).unstoreData(with(equal(folder)), with(equal(baseDir)));
                    one(storageProcessor).getStorageFormat();
                }
            });
        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, createLogMsgOfSuccess(
                        dataSetInformation.getExperimentIdentifier(), dataSetInformation
                                .getSampleIdentifier()));
        final LogMonitoringAppender appender2 =
                LogMonitoringAppender.addAppender(LogCategory.NOTIFY, String.format(
                        TransferredDataSetHandler.DATA_SET_REGISTRATION_FAILURE_TEMPLATE,
                        dataSetInformation));

        handler.handle(isFinishedFolder);

        appender.verifyLogHasNotHappened();
        appender2.verifyLogHasHappened();

        context.assertIsSatisfied();
    }

    private Session createSession()
    {
        return new Session("u", SESSION_TOKEN, new Principal("u", "FirstName", "LastName",
                "email@users.ch"), "remote-host", System.currentTimeMillis() - 1);
    }
}