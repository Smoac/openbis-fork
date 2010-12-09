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
import java.util.Properties;

import javax.activation.DataHandler;

import org.hamcrest.core.IsNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleAndDatasetRegistrationHandlerTest extends AbstractFileSystemTestCase
{
    protected Mockery context;

    protected IEncapsulatedOpenBISService openbisService;

    protected IDataSetHandler delegator;

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
        delegator = context.mock(IDataSetHandler.class);
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
        // setupOpenBisExpectations();
        // setupSessionContextExpectations();
        // setupCallerDataSetInfoExpectations();

        File workingCopy = createWorkingCopyOfTestFolder("basic-example");

        initializeDefaultDataSetHandler();
        handler.handleDataSet(workingCopy);

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisteringFolderWithoutControlFiles() throws IOException
    {
        // setupOpenBisExpectations();
        // setupSessionContextExpectations();
        // setupCallerDataSetInfoExpectations();
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
        // setupOpenBisExpectations();
        // setupSessionContextExpectations();
        // setupCallerDataSetInfoExpectations();

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
        // setupOpenBisExpectations();
        // setupSessionContextExpectations();
        // setupCallerDataSetInfoExpectations();

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

    private void checkAppenderContent(String errorText, String folderName)
    {
        boolean matches = logAppender.getLogContent().startsWith(errorText);
        assertTrue("Log does not contain correct text", matches);
        // assertEquals(errorText + "\nDeleting file '" + folderName + "'.",
        // logAppender.getLogContent());
    }

}
