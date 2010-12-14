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

package ch.systemsx.cisd.etlserver.entityregistration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;

import org.hamcrest.core.IsNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.IExtensibleDataSetHandler;
import ch.systemsx.cisd.etlserver.IExtensibleDataSetHandler.IDataSetRegistrator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleAndDatasetRegistrationHandlerTest extends AbstractFileSystemTestCase
{
    protected Mockery context;

    protected IEncapsulatedOpenBISService openbisService;

    protected IExtensibleDataSetHandler delegator;

    protected IMailClient mailClient;

    protected SampleAndDataSetRegistrationHandler handler;

    protected BufferedAppender logAppender;

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();

        logAppender = new BufferedAppender();

        context = new Mockery();
        openbisService = context.mock(IEncapsulatedOpenBISService.class);
        delegator = context.mock(IExtensibleDataSetHandler.class);
        mailClient = context.mock(IMailClient.class);
    }

    @AfterMethod
    public void tearDown()
    {
        logAppender.resetLogContent();
        context.assertIsSatisfied();
    }

    @Test
    public void testRegisteringBasicControlFile()
    {
        final RecordingMatcher<DataSetInformation> dataSetInfoMatcher =
                new RecordingMatcher<DataSetInformation>();
        final RecordingMatcher<NewSample> newSampleMatcher = new RecordingMatcher<NewSample>();

        setupOpenBisExpectations();
        setupDataSetHandlerExpectations(dataSetInfoMatcher, newSampleMatcher);
        // setupSessionContextExpectations();
        // setupCallerDataSetInfoExpectations();

        File workingCopy = createWorkingCopyOfTestFolder("basic-example");

        initializeDefaultDataSetHandler();
        handler.handleDataSet(workingCopy);

        String logText =
                "Global properties extracted from file 'control.tsv': SAMPLE_TYPE(MY_SAMPLE_TYPE) DATA_SET_TYPE(MY_DATA_SET_TYPE) DEFAULT_SPACE(MYSPACE) USER(test@test.test)\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=<null>,sampleProperties={prop1: VAL10,prop2: VAL20,prop3: VAL30},dataSetInformation=DataSetInformation{sampleCode=<null>,properties={},dataSetType=MY_DATA_SET_TYPE,instanceUUID=<null>,instanceCode=<null>,spaceCode=<null>,experimentIdentifier=<null>,isCompleteFlag=U,extractableData=ExtractableData{productionDate=<null>,dataProducerCode=<null>,parentDataSetCodes=[],dataSetProperties=[NewProperty{property=prop1,value=VAL40}, NewProperty{property=prop2,value=VAL50}],code=<null>},uploadingUserEmailOrNull=<null>,uploadingUserIdOrNull=test}]\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=<null>,sampleProperties={prop1: VAL11,prop2: VAL21,prop3: VAL31},dataSetInformation=DataSetInformation{sampleCode=<null>,properties={},dataSetType=MY_DATA_SET_TYPE,instanceUUID=<null>,instanceCode=<null>,spaceCode=<null>,experimentIdentifier=<null>,isCompleteFlag=U,extractableData=ExtractableData{productionDate=<null>,dataProducerCode=<null>,parentDataSetCodes=[],dataSetProperties=[NewProperty{property=prop1,value=VAL41}, NewProperty{property=prop2,value=VAL51}],code=<null>},uploadingUserEmailOrNull=<null>,uploadingUserIdOrNull=test}]\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=<null>,sampleProperties={prop1: VAL12,prop2: VAL22,prop3: VAL32},dataSetInformation=DataSetInformation{sampleCode=<null>,properties={},dataSetType=MY_DATA_SET_TYPE,instanceUUID=<null>,instanceCode=<null>,spaceCode=<null>,experimentIdentifier=<null>,isCompleteFlag=U,extractableData=ExtractableData{productionDate=<null>,dataProducerCode=<null>,parentDataSetCodes=[],dataSetProperties=[NewProperty{property=prop1,value=VAL42}, NewProperty{property=prop2,value=VAL52}],code=<null>},uploadingUserEmailOrNull=<null>,uploadingUserIdOrNull=test}]";
        checkAppenderContent(logText, "basic-example");

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisteringFolderWithoutControlFiles() throws IOException
    {
        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> addressesMatcher =
                new RecordingMatcher<EMailAddress[]>();

        setupErrorEmailExpectations(attachmentMatcher, addressesMatcher, "no-control");

        File workingCopy = createWorkingCopyOfTestFolder("no-control");

        initializeDefaultDataSetHandler();
        handler.handleDataSet(workingCopy);

        String errorText =
                "Folder (no-control) for sample/dataset registration contains no control files with the required extension: .tsv.\n"
                        + "Folder contents:\n" + "\t.svn\n" + "\tnot-a-tsv.txt\n";

        // Check the log
        checkAppenderContent(errorText, "no-control");

        checkEmailContent(attachmentMatcher, addressesMatcher, errorText);

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisteringEmptyFolder() throws IOException
    {
        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> addressesMatcher =
                new RecordingMatcher<EMailAddress[]>();

        setupErrorEmailExpectations(attachmentMatcher, addressesMatcher, "empty-folder");

        File workingCopy = createWorkingCopyOfTestFolder("empty-folder");

        initializeDefaultDataSetHandler();
        handler.handleDataSet(workingCopy);

        String errorText =
                "Folder (empty-folder) for sample/dataset registration contains no control files with the required extension: .tsv.\n"
                        + "Folder contents:\n" + "\t.svn\n";
        // Check the log
        checkAppenderContent(errorText, "empty-folder");

        // Check the email
        checkEmailContent(attachmentMatcher, addressesMatcher, errorText);

        context.assertIsSatisfied();
    }

    @Test
    public void testUndefinedSampleRegistrationMode()
    {
        final Properties props = new Properties();
        props.setProperty("dataset-handler.sample-registration-mode", "UNKNOWN");
        initializeDataSetHandler(props);

        // Check that the configuration failure was logged
        assertEquals("UNKNOWN is an unknown registration mode, defaulting to ACCEPT_ALL",
                logAppender.getLogContent());

        context.assertIsSatisfied();
    }

    private void initializeDefaultDataSetHandler()
    {
        final Properties props = new Properties();
        props.setProperty("dataset-handler.data-space", "store");
        initializeDataSetHandler(props);
    }

    private void initializeDataSetHandler(Properties props)
    {
        props.setProperty("processor", "ch.systemsx.cisd.etlserver.DefaultStorageProcessor");
        handler = new SampleAndDataSetRegistrationHandler(props, delegator, openbisService);
        handler.initializeMailClient(mailClient);
    }

    private File createWorkingCopyOfTestFolder(String folderName)
    {
        File dataSetFile =
                new File("sourceTest/java/ch/systemsx/cisd/etlserver/entityregistration/test-data/"
                        + folderName);

        File workingCopy = new File(workingDirectory, folderName);
        FileOperations.getInstance().copy(dataSetFile, workingCopy);
        return workingCopy;
    }

    private void setupErrorEmailExpectations(final RecordingMatcher<DataHandler> attachmentMatcher,
            final RecordingMatcher<EMailAddress[]> addressesMatcher, final String folderName)
    {
        context.checking(new Expectations()
            {
                {
                    Person admin = new Person();
                    admin.setEmail("test@test.test");
                    admin.setUserId("test");
                    one(openbisService).listAdministrators();
                    will(returnValue(Collections.singletonList(admin)));
                    one(mailClient)
                            .sendEmailMessageWithAttachment(
                                    with("Sample / Data Set Registration Error -- targets/unit-test-wd/ch.systemsx.cisd.etlserver.entityregistration.SampleAndDatasetRegistrationHandlerTest/"
                                            + folderName),
                                    with("When trying to process the files in the folder, targets/unit-test-wd/ch.systemsx.cisd.etlserver.entityregistration.SampleAndDatasetRegistrationHandlerTest/"
                                            + folderName
                                            + ", errors were encountered. These errors are detailed in the attachment."),
                                    with("errors.txt"), with(attachmentMatcher),
                                    with(new IsNull<EMailAddress>()),
                                    with(new IsNull<EMailAddress>()), with(addressesMatcher));
                }
            });
    }

    private void setupOpenBisExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    Person admin = new Person();
                    admin.setEmail("test@test.test");
                    admin.setUserId("test");
                    oneOf(openbisService).tryPersonWithUserIdOrEmail(admin.getEmail());
                    will(returnValue(admin));
                }
            });
    }

    private void setupDataSetHandlerExpectations(
            final RecordingMatcher<DataSetInformation> dataSetInfoMatcher,
            final RecordingMatcher<NewSample> newSampleMatcher)
    {
        context.checking(new Expectations()
            {
                {
                    final RecordingMatcher<File> fileMatcher = new RecordingMatcher<File>();
                    final RecordingMatcher<IExtensibleDataSetHandler.IDataSetRegistrator> registratorMatcher =
                            new RecordingMatcher<IExtensibleDataSetHandler.IDataSetRegistrator>();

                    final NewExternalData externalData = new NewExternalData();

                    exactly(3).of(delegator).handleDataSet(with(fileMatcher),
                            with(dataSetInfoMatcher), with(registratorMatcher));
                    will(new CustomAction("Call registrator")
                        {
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                List<IDataSetRegistrator> recordedObjects =
                                        registratorMatcher.getRecordedObjects();
                                IDataSetRegistrator registrator =
                                        recordedObjects.get(recordedObjects.size() - 1);
                                fillExternalDataFromInformation(externalData, dataSetInfoMatcher
                                        .getRecordedObjects().get(recordedObjects.size() - 1));
                                registrator.registerDataSetInApplicationServer(externalData);

                                return null;
                            }
                        });
                    exactly(3).of(openbisService).registerSampleAndDataSet(with(newSampleMatcher),
                            with(externalData), with("test"));
                    will(returnValue(new Sample()));
                }
            });
    }

    private void fillExternalDataFromInformation(NewExternalData externalData,
            DataSetInformation dataSetInformation)
    {
        externalData.setUserId(dataSetInformation.getUploadingUserIdOrNull());
        externalData.setUserEMail(dataSetInformation.tryGetUploadingUserEmail());
        externalData.setExtractableData(dataSetInformation.getExtractableData());
    }

    private void checkEmailContent(final RecordingMatcher<DataHandler> attachmentMatcher,
            final RecordingMatcher<EMailAddress[]> addressesMatcher, String errorText)
            throws IOException
    {
        for (EMailAddress address : addressesMatcher.getRecordedObjects().get(0))
        {
            assertEquals("test@test.test", address.tryGetEmailAddress());
        }
        assertEquals(errorText, attachmentMatcher.getRecordedObjects().get(0).getContent());
    }

    private void checkAppenderContent(String logText, String folderName)
    {
        // Check the text
        assertEquals(logText + "\nDeleting file '" + folderName + "'.", logAppender.getLogContent());
    }

}
