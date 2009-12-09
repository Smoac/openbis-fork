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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
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
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.JavaMailProperties;
import ch.systemsx.cisd.common.test.LogMonitoringAppender;
import ch.systemsx.cisd.etlserver.IStorageProcessor.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.server.EncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.server.SessionTokenManager;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.PluginTaskProviders;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PluginUtilTest;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
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
    { TransferredDataSetHandler.class, IdentifiedDataStrategy.class, PluginTaskProviders.class })
public final class TransferredDataSetHandlerTest extends AbstractFileSystemTestCase
{

    private static final String SAMPLE_CODE = "sample1";

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

    private static final class ExternalDataMatcher extends BaseMatcher<NewExternalData>
    {
        private final NewExternalData expectedData;

        public ExternalDataMatcher(final NewExternalData externalData)
        {
            this.expectedData = externalData;
        }

        public void describeTo(final Description description)
        {
            description.appendValue(expectedData);
        }

        public boolean matches(final Object item)
        {
            if (item instanceof NewExternalData == false)
            {
                return false;
            }
            final NewExternalData data = (NewExternalData) item;
            assertEquals(expectedData.getCode(), data.getCode());
            assertEquals(expectedData.getDataProducerCode(), data.getDataProducerCode());
            assertEquals(expectedData.getLocation(), data.getLocation());
            assertEquals(expectedData.getLocatorType(), data.getLocatorType());
            assertEquals(expectedData.getFileFormatType(), data.getFileFormatType());
            assertEquals(expectedData.getDataSetType(), data.getDataSetType());
            assertEquals(expectedData.getParentDataSetCodes(), data.getParentDataSetCodes());
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

    private NewExternalData targetData1;

    private File folder;

    private File isFinishedFolder;

    private File data2;

    private IMailClient mailClient;

    private BufferedAppender logRecorder;

    private DatabaseInstance homeDatabaseInstance;

    private IDataSetValidator dataSetValidator;

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
        final IETLServerPlugin plugin =
                new ETLServerPlugin(new MockDataSetInfoExtractor(dataSetInfoExtractor),
                        typeExtractor, storageProcessor);
        final EncapsulatedOpenBISService authorizedLimsService =
                new EncapsulatedOpenBISService(new SessionTokenManager(), limsService,
                        PluginUtilTest.createPluginTaskProviders());
        authorizedLimsService.setUsername("u");
        authorizedLimsService.setPassword("p");
        dataSetValidator = context.mock(IDataSetValidator.class);
        handler =
                new TransferredDataSetHandler("dss", storageProcessor, plugin,
                        authorizedLimsService, mailClient, dataSetValidator, true, true, false);

        dataSetInformation = new DataSetInformation();
        dataSetInformation.setDataSetType(DATA_SET_TYPE);
        final ExperimentIdentifier experimentIdentifier = new ExperimentIdentifier();
        experimentIdentifier.setExperimentCode("experiment1".toUpperCase());
        experimentIdentifier.setProjectCode("project1".toUpperCase());
        experimentIdentifier.setGroupCode("group1".toUpperCase());
        homeDatabaseInstance = new DatabaseInstance();
        homeDatabaseInstance.setCode("my-instance");
        homeDatabaseInstance.setUuid("1111-2222");
        dataSetInformation.setInstanceCode(homeDatabaseInstance.getCode());
        dataSetInformation.setInstanceUUID(homeDatabaseInstance.getUuid());
        dataSetInformation.setExperimentIdentifier(experimentIdentifier);
        dataSetInformation.setSampleCode(SAMPLE_CODE);
        dataSetInformation.setProducerCode(DATA_PRODUCER_CODE);
        dataSetInformation.setProductionDate(DATA_PRODUCTION_DATE);
        dataSetInformation.setDataSetCode(DATA_SET_CODE);
        dataSetInformation.setParentDataSetCodes(Collections.singletonList(PARENT_DATA_SET_CODE));
        targetFolder =
                IdentifiedDataStrategy.createBaseDirectory(workingDirectory, dataSetInformation);
        targetData1 = createTargetData(data1);
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.INFO);
    }

    private final String createLogMsgOfSuccess(final ExperimentIdentifier identifier,
            final SampleIdentifier sampleIdentifier)
    {
        return String.format(TransferredDataSetHandler.SUCCESSFULLY_REGISTERED_FOR_SAMPLE_TEMPLATE,
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

    private final NewExternalData createTargetData(final File dataSet)
    {
        final NewExternalData data = new NewExternalData();
        data.setLocation(getRelativeTargetFolder() + File.separator + dataSet.getName());
        data.setLocatorType(LOCATOR_TYPE);
        data.setDataSetType(DATA_SET_TYPE);
        data.setFileFormatType(FILE_FORMAT_TYPE);
        data.setStorageFormat(StorageFormat.BDS_DIRECTORY);
        data.setDataProducerCode(DATA_PRODUCER_CODE);
        data.setProductionDate(DATA_PRODUCTION_DATE);
        data.setCode(DATA_SET_CODE);
        data.setParentDataSetCodes(Collections.singletonList(PARENT_DATA_SET_CODE));
        return data;
    }

    private String getRelativeTargetFolder()
    {
        String absoluteTarget = targetFolder.getAbsolutePath();
        return absoluteTarget.substring(workingDirectory.getAbsolutePath().length() + 1);
    }

    // crates sample connected to an experiment
    private final static Sample createBaseSample(final DataSetInformation dataSetInformation)
    {
        final Experiment baseExperiment = createExperiment(dataSetInformation);
        Sample sample = new Sample();
        sample.setCode("code");
        sample.setExperiment(baseExperiment);
        return sample;
    }

    private final static Experiment createExperiment(final DataSetInformation dataSetInformation)
    {
        final Experiment baseExperiment = new Experiment();
        final ExperimentIdentifier experimentIdentifier =
                dataSetInformation.getExperimentIdentifier();
        baseExperiment.setCode(experimentIdentifier.getExperimentCode());
        final Group group = new Group();
        group.setCode(experimentIdentifier.getGroupCode());
        final Project project = new Project();
        project.setCode(experimentIdentifier.getProjectCode());
        project.setGroup(group);
        baseExperiment.setProject(project);
        final Person person = new Person();
        person.setEmail("john.doe@somewhere.com");
        baseExperiment.setRegistrator(person);
        return baseExperiment;
    }

    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    private final void prepareForStrategy(final File dataSet, final Sample sample)
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
                    will(returnValue(sample));

                    allowing(typeExtractor).getDataSetType(dataSet);
                    will(returnValue(DATA_SET_TYPE));
                }
            });
    }

    private final void prepareForStrategyIDENTIFIED(final File dataSet,
            final NewExternalData targetData, final Sample sample)
    {
        prepareForStrategy(dataSet, sample);
        context.checking(new Expectations()
            {
                {
                    one(limsService).tryToGetPropertiesOfTopSampleRegisteredFor(SESSION_TOKEN,
                            dataSetInformation.getSampleIdentifier());
                    will(returnValue(new IEntityProperty[0]));
                }
            });
    }

    private final void prepareForRegistration(final File dataSet)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE, dataSet);
                    
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
        if (dataset.getParentDataSetCodes().isEmpty() == false)
        {
            stringBuffer.append("Parent Data Sets:\t"
                    + StringUtils.join(dataset.getParentDataSetCodes(), ' ')
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
                getNotificationEmailContent(dataSet, dataSetCode), null, null, recipient);
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
        final Sample baseSample = createBaseSample(dataSetInformation);
        final Experiment baseExperiment = baseSample.getExperiment();
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
                    one(storageProcessor).storeData(dataSetInformation, typeExtractor, mailClient,
                            data1, baseDir);
                    final File finalDataSetPath = new File(baseDir, DATA1_NAME);
                    will(returnValue(finalDataSetPath));
                    one(storageProcessor).commit();
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
        final Sample baseSample = createBaseSample(dataSetInformation);
        final Experiment baseExperiment = baseSample.getExperiment();
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
                    one(storageProcessor).storeData(dataSetInformation, typeExtractor, mailClient,
                            data1, baseDir);
                    final File finalDataSetPath = new File(baseDir, DATA1_NAME);
                    will(returnValue(finalDataSetPath));
                    one(storageProcessor).commit();
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
    public final void testMoveInvalidDataSetFile()
    {
        assertEquals(new File(workingDirectory, DATA1_NAME), data1);
        assertEquals(new File(new File(workingDirectory, FOLDER_NAME), DATA2_NAME), data2);
        assert data1.exists() && data2.exists();
        final Sample baseSample = createBaseSample(dataSetInformation);
        final Experiment baseExperiment = baseSample.getExperiment();
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
                    one(mailClient).sendMessage(subject, body, null, null, email);
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
        final Sample baseSample = createBaseSample(dataSetInformation);
        final File baseDir = targetFolder;
        prepareForStrategyIDENTIFIED(folder, targetData1, baseSample);
        context.checking(new Expectations()
            {
                {
                    one(typeExtractor).getProcessorType(folder);
                    will(returnValue(EXAMPLE_PROCESSOR_ID));
                    
                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE, folder);

                    one(storageProcessor).storeData(dataSetInformation, typeExtractor, mailClient,
                            folder, baseDir);
                    UserFailureException exception =
                            new UserFailureException("Could store data by storage processor");
                    will(throwException(exception));

                    one(storageProcessor).rollback(folder, baseDir, exception);
                    will(returnValue(UnstoreDataAction.MOVE_TO_ERROR));

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
        final Sample baseSample = createBaseSample(dataSetInformation);
        final File baseDir = targetFolder;
        targetData1.setStorageFormat(null);
        prepareForStrategyIDENTIFIED(folder, targetData1, baseSample);
        prepareForRegistration(folder);
        context.checking(new Expectations()
            {
                {
                    one(storageProcessor).storeData(dataSetInformation, typeExtractor, mailClient,
                            folder, baseDir);
                    will(returnValue(new File(baseDir, DATA1_NAME)));

                    one(limsService).registerDataSet(with(equal(SESSION_TOKEN)),
                            with(equal(dataSetInformation.getSampleIdentifier())),
                            with(new ExternalDataMatcher(targetData1)));
                    EnvironmentFailureException exception =
                            new EnvironmentFailureException("Could not register data set folder");
                    will(throwException(exception));

                    one(storageProcessor).rollback(folder, baseDir, exception);
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

    private static SessionContextDTO createSession()
    {
        SessionContextDTO session = new SessionContextDTO();
        session.setSessionToken(SESSION_TOKEN);
        return session;
    }
}