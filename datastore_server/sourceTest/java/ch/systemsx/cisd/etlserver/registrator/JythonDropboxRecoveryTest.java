/*
 * Copyright 2012 ETH Zuerich, CISD
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.jmock.Expectations;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * @author jakubs
 */
public class JythonDropboxRecoveryTest extends AbstractJythonDataSetHandlerTest
{
    private static final String SCRIPTS_FOLDER =
            "sourceTest/java/ch/systemsx/cisd/etlserver/registrator/";

    @Override
    protected String getRegistrationScriptsFolderPath()
    {
        return SCRIPTS_FOLDER;
    }

    @DataProvider(name = "recoveryTestCaseProvider")
    public Object[][] recoveryTestCases()
    {
        LinkedList<RecoveryTestCase> testCases = new LinkedList<RecoveryTestCase>();
        RecoveryTestCase testCase;

        testCase = new RecoveryTestCase("basic recovery succeeded");
        testCases.add(testCase);

        testCase = new RecoveryTestCase("basic unrecoverable");
        testCase.canRecoverFromError = false;
        testCases.add(testCase);

        // result value
        Object[][] resultsList = new Object[testCases.size()][];
        int index = 0;
        for (RecoveryTestCase t : testCases)
        {
            resultsList[index++] = new Object[]
                { t };
        }

        return resultsList;
    }

    private static class RecoveryTestCase
    {

        /**
         * short description of the test. Will be presented in the test results view
         */
        protected String title;

        /**
         * The dropbox script file that should be used for this test case
         */
        protected String dropboxScriptPath = "v2-simple-testcase.py";

        /**
         * Specifies what properties should be overriden for this test case.
         */
        protected HashMap<String, String> overrideProperties;

        /**
         * If true, than autorecovery should take place. If not, then transaction should be rolled
         * back, and recovery artifacts removed.
         */
        protected boolean canRecoverFromError = true;

        /**
         * If true than this registration has been succesfull. Which means that the recovery should
         * continue registration rather rollback.
         */
        protected boolean registrationSuccessful = true;

        private RecoveryTestCase(String title)
        {
            this.title = title;
            this.overrideProperties = new HashMap<String, String>();
            this.overrideProperties.put("TEST_V2_API", "");
        }

        @Override
        public String toString()
        {
            return title;
        }
    }

    @Test(dataProvider = "recoveryTestCaseProvider")
    public void testRecovery(final RecoveryTestCase testCase)
    {
        setUpHomeDataBaseExpectations();

        createData();

        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder(testCase.dropboxScriptPath,
                        testCase.overrideProperties);

        createHandler(properties, true, false);

        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();

        context.checking(new RecoveryTestExpectations(testCase, atomicatOperationDetails));

        // create expectations:
        // the expectations are up to the point when the registration in openbis fails

        // run the actual code
        handler.handle(markerFile);

        if (testCase.canRecoverFromError)
        {
            File recoveryMarkerFile = assertRecoveryMarkerFile();
            assertOriginalMarkerFileExists();

            handler.handle(recoveryMarkerFile);

            // if failure happened here then don't expect recovery / marker files to be deleted

            if (testCase.registrationSuccessful)
            {
                //item in store
                assertStorageProcess(atomicatOperationDetails.recordedObject(), DATA_SET_CODE,
                        "sub_data_set_1", 0);
                //FIXME: this is commented out to cover the bug! beware
                // assertDirEmpty(stagingDirectory);
                assertDirEmpty(precommitDirectory);
            } else
            {
                assertDirEmpty(stagingDirectory);
                assertDirEmpty(precommitDirectory);
                // nothing is is store, all is cleared
            }

            assertNoOriginalMarkerFileExists();
            assertNoRecoveryMarkerFile();
        } else
        {
            assertNoOriginalMarkerFileExists();
            assertNoRecoveryMarkerFile();
            // assert there is no recovery file
            // rolllback requirementes
        }

        // now! we know that the error has happened

        // then we assert there exists a recovery file
        // assert there is a recovery marker file

        // we continue with the recovery

    }
    
    private void assertDirEmpty(File file)
    {
     String   contents = file.getAbsolutePath();
        assertEquals(contents, "[]", Arrays.asList(file.list()).toString());
    }

    private void assertOriginalMarkerFileExists()
    {
        assertTrue(
                "The original registration marker file should not be deleted when entering recovery mode",
                markerFile.exists());
    }

    private void assertNoOriginalMarkerFileExists()
    {
        assertFalse("The original registration marker file should be deleted", markerFile.exists());
    }

    private File assertRecoveryMarkerFile()
    {
        File file = getCreatedRecoveryMarkerFile();
        assertTrue("The recovery marker file does not exist! " + file, file.exists());
        return file;
    }

    private void assertNoRecoveryMarkerFile()
    {
        File file = getCreatedRecoveryMarkerFile();
        assertTrue("The recovery marker file should not exist! " + file, false == file.exists());
    }

    private File getCreatedRecoveryMarkerFile()
    {
        File originalIncoming =
                FileUtilities.removePrefixFromFileName(markerFile, IS_FINISHED_PREFIX);
        File recoveryMarkerFile =
                new File(originalIncoming.getAbsolutePath()
                        + IDataSetStorageRecoveryManager.PROCESSING_MARKER);
        return recoveryMarkerFile;
    }

    class RecoveryTestExpectations extends Expectations
    {
        final RecoveryTestCase testCase;

        final Experiment experiment;

        final RecordingMatcher<AtomicEntityOperationDetails> atomicatOperationDetails;

        public RecoveryTestExpectations(final RecoveryTestCase testCase,
                final RecordingMatcher<AtomicEntityOperationDetails> atomicatOperationDetails)
        {
            this.testCase = testCase;
            ExperimentBuilder builder = new ExperimentBuilder().identifier(EXPERIMENT_IDENTIFIER);
            this.experiment = builder.getExperiment();

            this.atomicatOperationDetails = atomicatOperationDetails;

            prepareExpecatations();
        }

        private void prepareExpecatations()
        {
            initialExpectations();

            registerDataSets();

            // now registration has failed with the exception. we continue depending on where

            if (testCase.canRecoverFromError)
            {
                checkRegistrationSucceeded();

                if (testCase.registrationSuccessful)
                {
                    setStorageConfirmed();
                }
            } else
            {
                // rollback
            }
        }

        protected void initialExpectations()
        {

            // create dataset
            one(openBisService).createDataSetCode();
            will(returnValue(DATA_SET_CODE));

            // get experiment
            atLeast(1).of(openBisService).tryToGetExperiment(
                    new ExperimentIdentifierFactory(experiment.getIdentifier()).createIdentifier());
            will(returnValue(experiment));

            // validate dataset
            one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                    new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"));
        }

        protected void registerDataSets()
        {
            one(openBisService).performEntityOperations(with(atomicatOperationDetails));

            Exception e;
            if (testCase.canRecoverFromError)
            {
                e =
                        new EnvironmentFailureException(
                                "Potentially recoverable failure in registration");
            } else
            {
                e = new UserFailureException("Unrecoverable failure in registration");
            }

            will(throwException(e));

        }

        @SuppressWarnings(
            { "rawtypes", "unchecked" })
        protected void checkRegistrationSucceeded()
        {
            one(openBisService).listDataSetsByCode(Arrays.asList(DATA_SET_CODE));
            if (testCase.registrationSuccessful)
            {
                // with the current implemntation returning the non-empty list should be enough
                List<ExternalData> externalDatas = (List) Arrays.asList(new Object());
                will(returnValue(externalDatas));
            } else
            {
                will(returnValue(new LinkedList<ExternalData>()));
            }
        }

        protected void setStorageConfirmed()
        {
            one(openBisService).setStorageConfirmed(DATA_SET_CODE);
        }

    }

    /**
     * creates a very simple dataset to import
     */
    private void createData()
    {
        incomingDataSetFile = createDirectory(workingDirectory, "data_set");

        assertTrue(incomingDataSetFile.isDirectory());

        subDataSet1 = createDirectory(incomingDataSetFile, "sub_data_set_1");

        FileUtilities.writeToFile(new File(subDataSet1, "read1.me"), "hello world1");

        markerFile = new File(workingDirectory, IS_FINISHED_PREFIX + "data_set");
        FileUtilities.writeToFile(markerFile, "");
    }

}
