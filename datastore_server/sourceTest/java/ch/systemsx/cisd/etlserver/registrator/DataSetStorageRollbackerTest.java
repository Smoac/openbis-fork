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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.etlserver.registrator.IDataSetOnErrorActionDecision.ErrorType;
import ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetRegistratorTest.MockStorageProcessor;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetStorageRollbackerTest extends AbstractFileSystemTestCase
{

    private static final String DATABASE_INSTANCE_UUID = "db-uuid";

    private static final Logger logger = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetStorageRollbackerTest.class);

    private Mockery context;

    private IEncapsulatedOpenBISService openBisService;

    private IMailClient mailClient;

    private IDataSetValidator dataSetValidator;

    private File incomingDataSetFile;

    private BufferedAppender logAppender;

    private TestDataSetRegistrator testRegistrator;

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();

        context = new Mockery();
        openBisService = context.mock(IEncapsulatedOpenBISService.class);
        dataSetValidator = context.mock(IDataSetValidator.class);
        mailClient = context.mock(IMailClient.class);
        logAppender = new BufferedAppender();

        setUpHomeDataBaseExpectations();
        TopLevelDataSetRegistratorGlobalState topLevelState =
                createGlobalState(createThreadProperties());
        testRegistrator = new TestDataSetRegistrator(topLevelState);
        logAppender.resetLogContent();
    }

    @Test
    public void testLogging()
    {
        incomingDataSetFile = createDirectory(workingDirectory, "data_set");
        OmniscientTopLevelDataSetRegistratorState globalState =
                testRegistrator.getRegistratorState();
        DataSetStorageRollbacker rollbacker =
                new DataSetStorageRollbacker(globalState, logger, UnstoreDataAction.DELETE,
                        incomingDataSetFile, null, null);
        rollbacker.appendErrorMessage();
        assertEquals(
                "Performing action DELETE on targets/unit-test-wd/ch.systemsx.cisd.etlserver.registrator.DataSetStorageRollbackerTest/data_set",
                logAppender.getLogContent());

        logAppender.resetLogContent();

        rollbacker =
                new DataSetStorageRollbacker(globalState, logger, UnstoreDataAction.DELETE,
                        incomingDataSetFile, null, null, ErrorType.REGISTRATION_SCRIPT_ERROR);
        rollbacker.appendErrorMessage();
        assertEquals(
                "Responding to error [REGISTRATION_SCRIPT_ERROR] by performing action DELETE on targets/unit-test-wd/ch.systemsx.cisd.etlserver.registrator.DataSetStorageRollbackerTest/data_set",
                logAppender.getLogContent());
    }

    private TopLevelDataSetRegistratorGlobalState createGlobalState(Properties threadProperties)
    {
        ThreadParameters threadParameters =
                new ThreadParameters(threadProperties, "rollbacker-test");

        TopLevelDataSetRegistratorGlobalState globalState =
                new TopLevelDataSetRegistratorGlobalState("dss",
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        workingDirectory, openBisService, mailClient, dataSetValidator, true,
                        threadParameters);
        return globalState;
    }

    private Properties createThreadProperties()
    {
        Properties threadProperties = new Properties();
        threadProperties.put(ThreadParameters.INCOMING_DIR, "incoming");
        threadProperties.put(ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION,
                ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE);
        threadProperties.put(ThreadParameters.DELETE_UNIDENTIFIED_KEY, "false");
        threadProperties.put(IStorageProcessorTransactional.STORAGE_PROCESSOR_KEY,
                MockStorageProcessor.class.getName());
        return threadProperties;
    }

    private class TestDataSetRegistrator extends
            AbstractOmniscientTopLevelDataSetRegistrator<DataSetInformation>
    {

        /**
         * @param globalState
         */
        protected TestDataSetRegistrator(TopLevelDataSetRegistratorGlobalState globalState)
        {
            super(globalState);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected void handleDataSet(File dataSetFile,
                DataSetRegistrationService<DataSetInformation> service) throws Throwable
        {
            // TODO Auto-generated method stub

        }
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

    protected void assertLogContent(BufferedAppender logRecorder, String... expectedLinesOfContent)
    {
        BufferedReader reader = new BufferedReader(new StringReader(logRecorder.getLogContent()));
        StringBuilder builder = new StringBuilder();
        for (String line : expectedLinesOfContent)
        {
            builder.append(line).append('\n');
        }
        String expectedContent = builder.toString();
        builder.setLength(0);
        String line;
        try
        {
            while ((line = reader.readLine()) != null)
            {
                if (line.startsWith("\tat") == false && line.startsWith("\t... ") == false)
                {
                    builder.append(line).append('\n');
                }
            }
        } catch (IOException ex)
        {
            // ignored, because we are reading from a string
        }
        assertEquals(expectedContent, builder.toString());
    }

    private File createDirectory(File parentDir, String directoryName)
    {
        final File file = new File(parentDir, directoryName);
        file.mkdir();
        return file;
    }
}
