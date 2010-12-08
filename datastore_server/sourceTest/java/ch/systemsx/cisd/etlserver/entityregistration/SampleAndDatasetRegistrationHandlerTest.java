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
import java.util.Properties;

import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.etlserver.IDataSetHandlerRpc;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleAndDatasetRegistrationHandlerTest extends AbstractFileSystemTestCase
{
    protected Mockery context;

    protected IEncapsulatedOpenBISService openbisService;

    protected IDataSetHandlerRpc delegator;

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
        delegator = context.mock(IDataSetHandlerRpc.class);
    }

    @AfterMethod
    public void tearDown()
    {
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
    public void testRegisteringFolderWithoutControlFiles()
    {
        // setupOpenBisExpectations();
        // setupSessionContextExpectations();
        // setupCallerDataSetInfoExpectations();

        File workingCopy = createWorkingCopyOfTestFolder("no-control");

        initializeDefaultDataSetHandler();
        handler.handleDataSet(workingCopy);

        // Check that
        assertEquals(
                "Folder (no-control) for sample/dataset registration contains no control files with the required extension: .tsv.\n"
                        + "Folder contents:\n"
                        + "\tnot-a-tsv.txt\n\n"
                        + "Deleting file 'no-control'.", logAppender.getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisteringEmptyFolder()
    {
        // setupOpenBisExpectations();
        // setupSessionContextExpectations();
        // setupCallerDataSetInfoExpectations();

        File workingCopy = createWorkingCopyOfTestFolder("empty-folder");

        initializeDefaultDataSetHandler();
        handler.handleDataSet(workingCopy);

        // Check that
        assertEquals(
                "Folder (empty-folder) for sample/dataset registration contains no control files with the required extension: .tsv.\n"
                        + "Folder contents:\n" + "\n" + "Deleting file 'empty-folder'.",
                logAppender.getLogContent());

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

}
