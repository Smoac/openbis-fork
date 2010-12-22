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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleAndDatasetRegistrationHandlerTest extends AbstractFileSystemTestCase
{
    private static final String SPACE_CODE = "MYSPACE";

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
    public void testRegisteringMissingSampleIdentifier()
    {
        final RecordingMatcher<DataSetInformation> dataSetInfoMatcher =
                new RecordingMatcher<DataSetInformation>();
        final RecordingMatcher<NewSample> newSampleMatcher = new RecordingMatcher<NewSample>();

        setupOpenBisExpectations();
        NewExternalData externalData = setupDataSetHandlerExpectations(dataSetInfoMatcher, 2);
        setupUpdateSampleExistsExpectations("S1", false);
        setupUpdateSampleExistsExpectations("S3", false);
        setupRegisterSampleAndDataSetExpectations(externalData, newSampleMatcher, 2);

        // Error email on the line with the missing sample identifier
        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> addressesMatcher =
                new RecordingMatcher<EMailAddress[]>();
        final String folderName = "missing-sample-identifier";
        setupPartialSuccessErrorEmailExpectations(attachmentMatcher, addressesMatcher, folderName);

        File workingCopy = createWorkingCopyOfTestFolder(folderName);

        initializeDefaultDataSetHandler();
        handler.handleDataSet(workingCopy);

        String logText =
                "Global properties extracted from file 'control.tsv': SAMPLE_TYPE(MY_SAMPLE_TYPE) DATA_SET_TYPE(MY_DATA_SET_TYPE) USER(test@test.test)\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S1,sampleProperties={prop1: VAL10,prop2: VAL20,prop3: VAL30},dataSetInformation=DataSetInformation{sampleCode=<null>,properties={},dataSetType=MY_DATA_SET_TYPE,instanceUUID=<null>,instanceCode=<null>,spaceCode=<null>,experimentIdentifier=/MYSPACE/MYPROJ/EXP1,isCompleteFlag=U,extractableData=ExtractableData{productionDate=<null>,dataProducerCode=<null>,parentDataSetCodes=[],dataSetProperties=[NewProperty{property=prop1,value=VAL40}, NewProperty{property=prop2,value=VAL50}],code=<null>},uploadingUserEmailOrNull=<null>,uploadingUserIdOrNull=test}]\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S3,sampleProperties={prop1: VAL12,prop2: VAL22,prop3: VAL32},dataSetInformation=DataSetInformation{sampleCode=<null>,properties={},dataSetType=MY_DATA_SET_TYPE,instanceUUID=<null>,instanceCode=<null>,spaceCode=<null>,experimentIdentifier=/MYSPACE/MYPROJ/EXP3,isCompleteFlag=U,extractableData=ExtractableData{productionDate=<null>,dataProducerCode=<null>,parentDataSetCodes=[],dataSetProperties=[NewProperty{property=prop1,value=VAL42}, NewProperty{property=prop2,value=VAL52}],code=<null>},uploadingUserEmailOrNull=<null>,uploadingUserIdOrNull=test}]\n"
                        + "Encountered errors in the following lines:\n"
                        + "# Illegal empty identifier\n"
                        + "\t/MYSPACE/MYPROJ/EXP2\tVAL11\tVAL21\tVAL31\tFILE_TYPE\tVAL41\tVAL51\tds2/\n\n"
                        + "The following lines were successfully registered:\n"
                        + "# /MYSPACE/S3\t/MYSPACE/MYPROJ/EXP3\tVAL12\tVAL22\tVAL32\tFILE_TYPE\tVAL42\tVAL52\tds3/\n"
                        + "# /MYSPACE/S1\t/MYSPACE/MYPROJ/EXP1\tVAL10\tVAL20\tVAL30\tFILE_TYPE\tVAL40\tVAL50\tds1/\n";
        checkAppenderContent(logText, folderName);

        context.assertIsSatisfied();
    }

    @Test
    public void testNotAllFoldersMentioned()
    {
        final RecordingMatcher<DataSetInformation> dataSetInfoMatcher =
                new RecordingMatcher<DataSetInformation>();
        final RecordingMatcher<NewSample> newSampleMatcher = new RecordingMatcher<NewSample>();

        setupOpenBisExpectations();
        NewExternalData externalData = setupDataSetHandlerExpectations(dataSetInfoMatcher, 1);
        setupUpdateSampleExistsExpectations("S1", false);
        setupRegisterSampleAndDataSetExpectations(externalData, newSampleMatcher, 1);

        final String folderName = "not-all-subfolders-mentioned";
        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> addressesMatcher =
                new RecordingMatcher<EMailAddress[]>();
        setupPartialSuccessErrorEmailExpectations(attachmentMatcher, addressesMatcher, folderName);

        File workingCopy = createWorkingCopyOfTestFolder(folderName);

        initializeDefaultDataSetHandler();
        handler.handleDataSet(workingCopy);

        String logText =
                "Global properties extracted from file 'control.tsv': SAMPLE_TYPE(MY_SAMPLE_TYPE) DATA_SET_TYPE(MY_DATA_SET_TYPE) USER(test@test.test)\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S1,sampleProperties={prop1: VAL10,prop2: VAL20,prop3: VAL30},dataSetInformation=DataSetInformation{sampleCode=<null>,properties={},dataSetType=MY_DATA_SET_TYPE,instanceUUID=<null>,instanceCode=<null>,spaceCode=<null>,experimentIdentifier=/MYSPACE/MYPROJ/EXP1,isCompleteFlag=U,extractableData=ExtractableData{productionDate=<null>,dataProducerCode=<null>,parentDataSetCodes=[],dataSetProperties=[NewProperty{property=prop1,value=VAL40}, NewProperty{property=prop2,value=VAL50}],code=<null>},uploadingUserEmailOrNull=<null>,uploadingUserIdOrNull=test}]\n"
                        + "The following subfolders were in the uploaded folder, but were not mentioned in the control file:\n"
                        + "ds2,ds3\n";
        checkAppenderContent(logText, folderName);

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisteringBasicControlFile()
    {
        final RecordingMatcher<DataSetInformation> dataSetInfoMatcher =
                new RecordingMatcher<DataSetInformation>();
        final RecordingMatcher<NewSample> newSampleMatcher = new RecordingMatcher<NewSample>();

        setupOpenBisExpectations();
        NewExternalData externalData = setupDataSetHandlerExpectations(dataSetInfoMatcher, 3);
        setupUpdateSampleExistsExpectations("S1", false);
        setupUpdateSampleExistsExpectations("S2", false);
        setupUpdateSampleExistsExpectations("S3", false);
        setupRegisterSampleAndDataSetExpectations(externalData, newSampleMatcher, 3);

        final String folderName = "basic-example";
        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> addressesMatcher =
                new RecordingMatcher<EMailAddress[]>();
        setupSuccessEmailExpectations(attachmentMatcher, addressesMatcher, folderName);

        File workingCopy = createWorkingCopyOfTestFolder(folderName);

        initializeDefaultDataSetHandler();
        handler.handleDataSet(workingCopy);

        String logText =
                "Global properties extracted from file 'control.tsv': SAMPLE_TYPE(MY_SAMPLE_TYPE) DATA_SET_TYPE(MY_DATA_SET_TYPE) USER(test@test.test)\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S1,sampleProperties={prop1: VAL10,prop2: VAL20,prop3: VAL30},dataSetInformation=DataSetInformation{sampleCode=<null>,properties={},dataSetType=MY_DATA_SET_TYPE,instanceUUID=<null>,instanceCode=<null>,spaceCode=<null>,experimentIdentifier=/MYSPACE/MYPROJ/EXP1,isCompleteFlag=U,extractableData=ExtractableData{productionDate=<null>,dataProducerCode=<null>,parentDataSetCodes=[],dataSetProperties=[NewProperty{property=prop1,value=VAL40}, NewProperty{property=prop2,value=VAL50}],code=<null>},uploadingUserEmailOrNull=<null>,uploadingUserIdOrNull=test}]\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S2,sampleProperties={prop1: VAL11,prop2: VAL21,prop3: VAL31},dataSetInformation=DataSetInformation{sampleCode=<null>,properties={},dataSetType=MY_DATA_SET_TYPE,instanceUUID=<null>,instanceCode=<null>,spaceCode=<null>,experimentIdentifier=/MYSPACE/MYPROJ/EXP2,isCompleteFlag=U,extractableData=ExtractableData{productionDate=<null>,dataProducerCode=<null>,parentDataSetCodes=[],dataSetProperties=[NewProperty{property=prop1,value=VAL41}, NewProperty{property=prop2,value=VAL51}],code=<null>},uploadingUserEmailOrNull=<null>,uploadingUserIdOrNull=test}]\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S3,sampleProperties={prop1: VAL12,prop2: VAL22,prop3: VAL32},dataSetInformation=DataSetInformation{sampleCode=<null>,properties={},dataSetType=MY_DATA_SET_TYPE,instanceUUID=<null>,instanceCode=<null>,spaceCode=<null>,experimentIdentifier=/MYSPACE/MYPROJ/EXP3,isCompleteFlag=U,extractableData=ExtractableData{productionDate=<null>,dataProducerCode=<null>,parentDataSetCodes=[],dataSetProperties=[NewProperty{property=prop1,value=VAL42}, NewProperty{property=prop2,value=VAL52}],code=<null>},uploadingUserEmailOrNull=<null>,uploadingUserIdOrNull=test}]";
        checkAppenderContent(logText, folderName);

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisteringWithEmptyLinesInControlFile()
    {
        final RecordingMatcher<DataSetInformation> dataSetInfoMatcher =
                new RecordingMatcher<DataSetInformation>();
        final RecordingMatcher<NewSample> newSampleMatcher = new RecordingMatcher<NewSample>();

        setupOpenBisExpectations();
        NewExternalData externalData = setupDataSetHandlerExpectations(dataSetInfoMatcher, 3);
        setupUpdateSampleExistsExpectations("S1", false);
        setupUpdateSampleExistsExpectations("S2", false);
        setupUpdateSampleExistsExpectations("S3", false);
        setupRegisterSampleAndDataSetExpectations(externalData, newSampleMatcher, 3);

        final String folderName = "empty-lines";
        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> addressesMatcher =
                new RecordingMatcher<EMailAddress[]>();
        setupSuccessEmailExpectations(attachmentMatcher, addressesMatcher, folderName);

        File workingCopy = createWorkingCopyOfTestFolder(folderName);

        initializeDefaultDataSetHandler();
        handler.handleDataSet(workingCopy);

        String logText =
                "Global properties extracted from file 'control.tsv': SAMPLE_TYPE(MY_SAMPLE_TYPE) DATA_SET_TYPE(MY_DATA_SET_TYPE) USER(test@test.test)\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S1,sampleProperties={prop1: VAL10,prop2: VAL20,prop3: VAL30},dataSetInformation=DataSetInformation{sampleCode=<null>,properties={},dataSetType=MY_DATA_SET_TYPE,instanceUUID=<null>,instanceCode=<null>,spaceCode=<null>,experimentIdentifier=/MYSPACE/MYPROJ/EXP1,isCompleteFlag=U,extractableData=ExtractableData{productionDate=<null>,dataProducerCode=<null>,parentDataSetCodes=[],dataSetProperties=[NewProperty{property=prop1,value=VAL40}, NewProperty{property=prop2,value=VAL50}],code=<null>},uploadingUserEmailOrNull=<null>,uploadingUserIdOrNull=test}]\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S2,sampleProperties={prop1: VAL11,prop2: VAL21,prop3: VAL31},dataSetInformation=DataSetInformation{sampleCode=<null>,properties={},dataSetType=MY_DATA_SET_TYPE,instanceUUID=<null>,instanceCode=<null>,spaceCode=<null>,experimentIdentifier=/MYSPACE/MYPROJ/EXP2,isCompleteFlag=U,extractableData=ExtractableData{productionDate=<null>,dataProducerCode=<null>,parentDataSetCodes=[],dataSetProperties=[NewProperty{property=prop1,value=VAL41}, NewProperty{property=prop2,value=VAL51}],code=<null>},uploadingUserEmailOrNull=<null>,uploadingUserIdOrNull=test}]\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S3,sampleProperties={prop1: VAL12,prop2: VAL22,prop3: VAL32},dataSetInformation=DataSetInformation{sampleCode=<null>,properties={},dataSetType=MY_DATA_SET_TYPE,instanceUUID=<null>,instanceCode=<null>,spaceCode=<null>,experimentIdentifier=/MYSPACE/MYPROJ/EXP3,isCompleteFlag=U,extractableData=ExtractableData{productionDate=<null>,dataProducerCode=<null>,parentDataSetCodes=[],dataSetProperties=[NewProperty{property=prop1,value=VAL42}, NewProperty{property=prop2,value=VAL52}],code=<null>},uploadingUserEmailOrNull=<null>,uploadingUserIdOrNull=test}]";
        checkAppenderContent(logText, folderName);

        context.assertIsSatisfied();
    }

    @Test
    public void testOnlyUpdates()
    {
        final RecordingMatcher<DataSetInformation> dataSetInfoMatcher =
                new RecordingMatcher<DataSetInformation>();
        final RecordingMatcher<SampleUpdatesDTO> sampleUpdatesMatcher =
                new RecordingMatcher<SampleUpdatesDTO>();

        setupOpenBisExpectations();
        NewExternalData externalData = setupDataSetHandlerExpectations(dataSetInfoMatcher, 3);
        setupUpdateSampleExistsExpectations("S1", true);
        setupUpdateSampleExistsExpectations("S2", true);
        setupUpdateSampleExistsExpectations("S3", true);
        setupUpdateSampleAndRegisterDataSetExpectations(externalData, sampleUpdatesMatcher, 3);

        final String folderName = "basic-example";
        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> addressesMatcher =
                new RecordingMatcher<EMailAddress[]>();
        setupSuccessEmailExpectations(attachmentMatcher, addressesMatcher, folderName);

        File workingCopy = createWorkingCopyOfTestFolder(folderName);

        initializeDefaultDataSetHandler();
        handler.handleDataSet(workingCopy);

        String logText =
                "Global properties extracted from file 'control.tsv': SAMPLE_TYPE(MY_SAMPLE_TYPE) DATA_SET_TYPE(MY_DATA_SET_TYPE) USER(test@test.test)\n"
                        + "Updated sample, registered data set SampleDataSetPair[sampleIdentifier=/MYSPACE/S1,sampleProperties={prop1: VAL10,prop2: VAL20,prop3: VAL30},dataSetInformation=DataSetInformation{sampleCode=<null>,properties={},dataSetType=MY_DATA_SET_TYPE,instanceUUID=<null>,instanceCode=<null>,spaceCode=<null>,experimentIdentifier=/MYSPACE/MYPROJ/EXP1,isCompleteFlag=U,extractableData=ExtractableData{productionDate=<null>,dataProducerCode=<null>,parentDataSetCodes=[],dataSetProperties=[NewProperty{property=prop1,value=VAL40}, NewProperty{property=prop2,value=VAL50}],code=<null>},uploadingUserEmailOrNull=<null>,uploadingUserIdOrNull=test}]\n"
                        + "Updated sample, registered data set SampleDataSetPair[sampleIdentifier=/MYSPACE/S2,sampleProperties={prop1: VAL11,prop2: VAL21,prop3: VAL31},dataSetInformation=DataSetInformation{sampleCode=<null>,properties={},dataSetType=MY_DATA_SET_TYPE,instanceUUID=<null>,instanceCode=<null>,spaceCode=<null>,experimentIdentifier=/MYSPACE/MYPROJ/EXP2,isCompleteFlag=U,extractableData=ExtractableData{productionDate=<null>,dataProducerCode=<null>,parentDataSetCodes=[],dataSetProperties=[NewProperty{property=prop1,value=VAL41}, NewProperty{property=prop2,value=VAL51}],code=<null>},uploadingUserEmailOrNull=<null>,uploadingUserIdOrNull=test}]\n"
                        + "Updated sample, registered data set SampleDataSetPair[sampleIdentifier=/MYSPACE/S3,sampleProperties={prop1: VAL12,prop2: VAL22,prop3: VAL32},dataSetInformation=DataSetInformation{sampleCode=<null>,properties={},dataSetType=MY_DATA_SET_TYPE,instanceUUID=<null>,instanceCode=<null>,spaceCode=<null>,experimentIdentifier=/MYSPACE/MYPROJ/EXP3,isCompleteFlag=U,extractableData=ExtractableData{productionDate=<null>,dataProducerCode=<null>,parentDataSetCodes=[],dataSetProperties=[NewProperty{property=prop1,value=VAL42}, NewProperty{property=prop2,value=VAL52}],code=<null>},uploadingUserEmailOrNull=<null>,uploadingUserIdOrNull=test}]";
        checkAppenderContent(logText, folderName);

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisteringAndUpdates()
    {
        final RecordingMatcher<DataSetInformation> dataSetInfoMatcher =
                new RecordingMatcher<DataSetInformation>();
        final RecordingMatcher<NewSample> newSampleMatcher = new RecordingMatcher<NewSample>();
        final RecordingMatcher<SampleUpdatesDTO> sampleUpdatesMatcher =
                new RecordingMatcher<SampleUpdatesDTO>();

        setupOpenBisExpectations();
        NewExternalData externalData = setupDataSetHandlerExpectations(dataSetInfoMatcher, 3);
        setupUpdateSampleExistsExpectations("S1", false);
        setupUpdateSampleExistsExpectations("S2", true);
        setupUpdateSampleExistsExpectations("S3", false);
        setupRegisterSampleAndDataSetExpectations(externalData, newSampleMatcher, 2);
        setupUpdateSampleAndRegisterDataSetExpectations(externalData, sampleUpdatesMatcher, 1);

        final String folderName = "basic-example";

        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> addressesMatcher =
                new RecordingMatcher<EMailAddress[]>();
        setupSuccessEmailExpectations(attachmentMatcher, addressesMatcher, folderName);

        File workingCopy = createWorkingCopyOfTestFolder(folderName);

        initializeDefaultDataSetHandler();
        handler.handleDataSet(workingCopy);

        String logText =
                "Global properties extracted from file 'control.tsv': SAMPLE_TYPE(MY_SAMPLE_TYPE) DATA_SET_TYPE(MY_DATA_SET_TYPE) USER(test@test.test)\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S1,sampleProperties={prop1: VAL10,prop2: VAL20,prop3: VAL30},dataSetInformation=DataSetInformation{sampleCode=<null>,properties={},dataSetType=MY_DATA_SET_TYPE,instanceUUID=<null>,instanceCode=<null>,spaceCode=<null>,experimentIdentifier=/MYSPACE/MYPROJ/EXP1,isCompleteFlag=U,extractableData=ExtractableData{productionDate=<null>,dataProducerCode=<null>,parentDataSetCodes=[],dataSetProperties=[NewProperty{property=prop1,value=VAL40}, NewProperty{property=prop2,value=VAL50}],code=<null>},uploadingUserEmailOrNull=<null>,uploadingUserIdOrNull=test}]\n"
                        + "Updated sample, registered data set SampleDataSetPair[sampleIdentifier=/MYSPACE/S2,sampleProperties={prop1: VAL11,prop2: VAL21,prop3: VAL31},dataSetInformation=DataSetInformation{sampleCode=<null>,properties={},dataSetType=MY_DATA_SET_TYPE,instanceUUID=<null>,instanceCode=<null>,spaceCode=<null>,experimentIdentifier=/MYSPACE/MYPROJ/EXP2,isCompleteFlag=U,extractableData=ExtractableData{productionDate=<null>,dataProducerCode=<null>,parentDataSetCodes=[],dataSetProperties=[NewProperty{property=prop1,value=VAL41}, NewProperty{property=prop2,value=VAL51}],code=<null>},uploadingUserEmailOrNull=<null>,uploadingUserIdOrNull=test}]\n"
                        + "Registered sample/data set pair SampleDataSetPair[sampleIdentifier=/MYSPACE/S3,sampleProperties={prop1: VAL12,prop2: VAL22,prop3: VAL32},dataSetInformation=DataSetInformation{sampleCode=<null>,properties={},dataSetType=MY_DATA_SET_TYPE,instanceUUID=<null>,instanceCode=<null>,spaceCode=<null>,experimentIdentifier=/MYSPACE/MYPROJ/EXP3,isCompleteFlag=U,extractableData=ExtractableData{productionDate=<null>,dataProducerCode=<null>,parentDataSetCodes=[],dataSetProperties=[NewProperty{property=prop1,value=VAL42}, NewProperty{property=prop2,value=VAL52}],code=<null>},uploadingUserEmailOrNull=<null>,uploadingUserIdOrNull=test}]";
        checkAppenderContent(logText, folderName);

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisteringFolderWithoutControlFiles() throws IOException
    {
        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> addressesMatcher =
                new RecordingMatcher<EMailAddress[]>();

        setupFailureErrorEmailExpectations(attachmentMatcher, addressesMatcher, "no-control");

        File workingCopy = createWorkingCopyOfTestFolder("no-control");

        initializeDefaultDataSetHandler();
        handler.handleDataSet(workingCopy);

        String errorText =
                "Folder (no-control) for sample/dataset registration contains no control files matching the configured pattern: .*\\.[Tt][Ss][Vv].\n"
                        + "Folder contents:\n" + "\tnot-a-tsv.txt\n";

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

        setupFailureErrorEmailExpectations(attachmentMatcher, addressesMatcher, "empty-folder");

        File workingCopy = createWorkingCopyOfTestFolder("empty-folder");

        initializeDefaultDataSetHandler();
        handler.handleDataSet(workingCopy);

        String errorText =
                "Folder (empty-folder) for sample/dataset registration contains no control files matching the configured pattern: .*\\.[Tt][Ss][Vv].\n"
                        + "Folder contents:\n";
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
        FileOperations.getInstance().deleteRecursively(new File(workingCopy, ".svn"));
        return workingCopy;
    }

    private void setupFailureErrorEmailExpectations(
            final RecordingMatcher<DataHandler> attachmentMatcher,
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
                }
            });
        setupEmailExpectations(
                attachmentMatcher,
                addressesMatcher,
                "Sample / Data Set Registration Error -- targets/unit-test-wd/ch.systemsx.cisd.etlserver.entityregistration.SampleAndDatasetRegistrationHandlerTest/"
                        + folderName,
                "When trying to process the files in the folder, targets/unit-test-wd/ch.systemsx.cisd.etlserver.entityregistration.SampleAndDatasetRegistrationHandlerTest/"
                        + folderName
                        + ", errors were encountered. These errors are detailed in the attachment.",
                "errors.txt", folderName);
    }

    private void setupPartialSuccessErrorEmailExpectations(
            final RecordingMatcher<DataHandler> attachmentMatcher,
            final RecordingMatcher<EMailAddress[]> addressesMatcher, final String folderName)
    {
        setupEmailExpectations(
                attachmentMatcher,
                addressesMatcher,
                "Sample / Data Set Registration Error -- targets/unit-test-wd/ch.systemsx.cisd.etlserver.entityregistration.SampleAndDatasetRegistrationHandlerTest/"
                        + folderName + "/control.tsv",
                "Not all samples and data sets specified in the control file, targets/unit-test-wd/ch.systemsx.cisd.etlserver.entityregistration.SampleAndDatasetRegistrationHandlerTest/"
                        + folderName
                        + "/control.tsv, could be registered / updated. The errors are detailed in the attachment. Each faulty line is reproduced, preceded by a comment explaining the cause of the error.",
                "errors.txt", folderName);
    }

    private void setupSuccessEmailExpectations(
            final RecordingMatcher<DataHandler> attachmentMatcher,
            final RecordingMatcher<EMailAddress[]> addressesMatcher, final String folderName)
    {
        setupEmailExpectations(
                attachmentMatcher,
                addressesMatcher,
                "Sample / Data Set Registration Succeeded -- targets/unit-test-wd/ch.systemsx.cisd.etlserver.entityregistration.SampleAndDatasetRegistrationHandlerTest/"
                        + folderName + "/control.tsv",
                "The registration/update of samples and the registration of data sets was successful specified in the control file, targets/unit-test-wd/ch.systemsx.cisd.etlserver.entityregistration.SampleAndDatasetRegistrationHandlerTest/"
                        + folderName + "/control.tsv, was successful.", "registered.txt",
                folderName);
    }

    private void setupEmailExpectations(final RecordingMatcher<DataHandler> attachmentMatcher,
            final RecordingMatcher<EMailAddress[]> addressesMatcher, final String emailSubject,
            final String emailBody, final String attachmentName, final String folderName)
    {
        context.checking(new Expectations()
            {
                {
                    one(mailClient).sendEmailMessageWithAttachment(with(emailSubject),
                            with(emailBody), with(attachmentName), with(attachmentMatcher),
                            with(new IsNull<EMailAddress>()), with(new IsNull<EMailAddress>()),
                            with(addressesMatcher));
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

    private NewExternalData setupDataSetHandlerExpectations(
            final RecordingMatcher<DataSetInformation> dataSetInfoMatcher, final int count)
    {
        final NewExternalData externalData = new NewExternalData();
        context.checking(new Expectations()
            {
                {
                    final RecordingMatcher<File> fileMatcher = new RecordingMatcher<File>();
                    final RecordingMatcher<IExtensibleDataSetHandler.IDataSetRegistrator> registratorMatcher =
                            new RecordingMatcher<IExtensibleDataSetHandler.IDataSetRegistrator>();

                    for (int i = 1; i < 4; ++i)
                    {
                        ExperimentIdentifier experimentId =
                                new ExperimentIdentifier(DatabaseInstanceIdentifier.HOME,
                                        SPACE_CODE, "MYPROJ", "EXP" + i);
                        allowing(openbisService).tryToGetExperiment(experimentId);
                        Experiment exp = new Experiment();
                        exp.setIdentifier(experimentId.toString());
                        will(returnValue(exp));
                    }

                    exactly(count).of(delegator).handleDataSet(with(fileMatcher),
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
                }
            });
        return externalData;
    }

    private void setupRegisterSampleAndDataSetExpectations(final NewExternalData externalData,
            final RecordingMatcher<NewSample> newSampleMatcher, final int count)
    {
        context.checking(new Expectations()
            {
                {
                    exactly(count).of(openbisService).registerSampleAndDataSet(
                            with(newSampleMatcher), with(externalData), with("test"));
                    will(returnValue(new Sample()));
                }
            });
    }

    private void setupUpdateSampleExistsExpectations(final String sampleCode, final boolean exists)
    {
        final SpaceIdentifier spaceId =
                new SpaceIdentifier(DatabaseInstanceIdentifier.HOME, SPACE_CODE);
        // Decide if the samples exist
        context.checking(new Expectations()
            {
                {

                    oneOf(openbisService).tryGetSampleWithExperiment(
                            new SampleIdentifier(spaceId, sampleCode));
                    if (exists)
                    {
                        will(returnValue(new Sample()));
                    } else
                    {
                        will(returnValue(null));
                    }
                }
            });
    }

    private void setupUpdateSampleAndRegisterDataSetExpectations(
            final NewExternalData externalData,
            final RecordingMatcher<SampleUpdatesDTO> sampleUpdatesMatcher, final int count)
    {
        context.checking(new Expectations()
            {
                {
                    exactly(count).of(openbisService).updateSampleAndRegisterDataSet(
                            with(sampleUpdatesMatcher), with(externalData));
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
        for (EMailAddress address : addressesMatcher.recordedObject())
        {
            assertEquals("test@test.test", address.tryGetEmailAddress());
        }
        assertEquals(errorText, attachmentMatcher.recordedObject().getContent());
    }

    private void checkAppenderContent(String logText, String folderName)
    {
        // Check the text
        assertEquals(logText + "\nDeleting file '" + folderName + "'.", logAppender.getLogContent());
    }

}
