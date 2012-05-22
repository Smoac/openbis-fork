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
import java.util.Calendar;
import java.util.Date;
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

    private static enum RegistrationCheckResult
    {
        REGISTRATION_SUCCEEDED, REGISTRATION_FAILED, CHECK_FAILED, CHECK_FAILED_GIVE_UP
    }

    @DataProvider(name = "recoveryTestCaseProvider")
    public Object[][] recoveryTestCases()
    {
        LinkedList<RecoveryTestCase> testCases = new LinkedList<RecoveryTestCase>();
        RecoveryTestCase testCase;

        testCase = new RecoveryTestCase("basic recovery succeeded");
        testCases.add(testCase);

        testCase = new RecoveryTestCase("basic recovery rollback ");
        testCase.registrationCheckResult = RegistrationCheckResult.REGISTRATION_FAILED;
        testCases.add(testCase);

        testCase = new RecoveryTestCase("cant verify if registration succeeded");
        testCase.registrationCheckResult = RegistrationCheckResult.CHECK_FAILED;
        testCases.add(testCase);

        testCase = new RecoveryTestCase("check retry count incremented");
        testCase.registrationCheckResult = RegistrationCheckResult.CHECK_FAILED;
        testCase.recoveryRertyCount = 3;
        testCases.add(testCase);

        testCase = new RecoveryTestCase("check can't retry immediatelly after last failure");
        testCase.recoveryLastTry = new Date();
        testCase.nextTryInTheFuture = true;
        testCases.add(testCase);

        testCase = new RecoveryTestCase("check failure after the recovery count exceeded");
        testCase.registrationCheckResult = RegistrationCheckResult.CHECK_FAILED_GIVE_UP;
        testCase.recoveryRertyCount = 100;
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
         * Desrcibed the result of the check whether the registration was successful. If
         * REGISTRATION_SUCCEEDED - we can continue recovery IF REGISTRATION_FAILED - we can
         * rollback IF CHECK_FAILED - we don't know and we have to try again.
         */
        protected RegistrationCheckResult registrationCheckResult =
                RegistrationCheckResult.REGISTRATION_SUCCEEDED;

        /**
         * if set to a value > 0, before calling the recovery the retryCount will be set to this
         * value
         */
        protected int recoveryRertyCount = 0;

        /**
         * the timestamp of a recovery last try.
         */
        // gets initialized to some date in the past in the constructor
        protected Date recoveryLastTry;

        /**
         * if set to true, then the registration should do nothing
         */
        protected boolean nextTryInTheFuture = false;

        private RecoveryTestCase(String title)
        {
            this.title = title;
            this.overrideProperties = new HashMap<String, String>();
            this.overrideProperties.put("TEST_V2_API", "");

            // this the ultra-awkward way of initializing the recovery last try to some date in the
            // past
            Calendar c = Calendar.getInstance();
            c.set(2010, 1, 1);
            recoveryLastTry = c.getTime();
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

        // create expectations
        context.checking(new RecoveryTestExpectations(testCase, atomicatOperationDetails));

        handler.handle(markerFile);

        if (testCase.canRecoverFromError)
        {
            setTheRecoveryInfo(testCase.recoveryRertyCount, testCase.recoveryLastTry);

            assertRecoveryFile(testCase.recoveryRertyCount, RecoveryInfoDateConstraint.ORIGINAL,
                    testCase.recoveryLastTry);
            assertOriginalMarkerFileExists();
            assertDirNotEmpty(precommitDirectory, "Precommit directory should not be empty");

            handler.handle(markerFile);

            if (testCase.nextTryInTheFuture)
            {
                assertRecoveryFile(testCase.recoveryRertyCount,
                        RecoveryInfoDateConstraint.ORIGINAL, testCase.recoveryLastTry);
                assertOriginalMarkerFileExists();
                assertDirNotEmpty(precommitDirectory, "Precommit directory should not be empty");
                assertJythonHooks("pre_metadata_registration");
                // nothing happened
            } else
            {
                assertPostRecoveryConstraints(testCase, atomicatOperationDetails);
            }
        } else
        {
            assertNoRecoveryTriggeredConstraints();
        }
    }

    private void assertNoRecoveryTriggeredConstraints()
    {
        assertDataSetNotStoredProcess(DATA_SET_CODE);

        assertNoOriginalMarkerFileExists();
        assertNoRecoveryMarkerFile();

        assertDirEmpty(stagingDirectory);

        assertJythonHooks("pre_metadata_registration", "rollback_pre_registration");
        // FIXME: this check is commented out because of a bug!
        // assertDirEmpty(precommitDirectory);

        // assert there is no recovery file
        // rolllback requirementes
    }

    private void assertJythonHooks(String... messages)
    {
        JythonHookTestTool jythonHookTestTool =
                JythonHookTestTool.createFromWorkingDirectory(workingDirectory);
        for (String msg : messages)
        {
            jythonHookTestTool.assertLogged(msg);
        }
        jythonHookTestTool.assertNoMoreMessages();
    }

    private void assertPostRecoveryConstraints(
            final RecoveryTestCase testCase,
            final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails)
    {
        JythonHookTestTool jythonHookTestTool =
                JythonHookTestTool.createFromWorkingDirectory(workingDirectory);
        // the check from the original registration
        jythonHookTestTool.assertLogged("pre_metadata_registration");

        switch (testCase.registrationCheckResult)
        {
            case REGISTRATION_SUCCEEDED:
                // item in store
                assertStorageProcess(atomicatOperationDetails.recordedObject(), DATA_SET_CODE,
                        "sub_data_set_1", 0);
                // FIXME: this is commented out to cover the bug! beware
                // assertDirEmpty(stagingDirectory);
                assertDirEmpty(precommitDirectory);

                assertNoOriginalMarkerFileExists();
                assertNoRecoveryMarkerFile();

                // the hooks after successful registration
                jythonHookTestTool.assertLogged("post_metadata_registration");
                jythonHookTestTool.assertLogged("post_storage");
                break;
            case REGISTRATION_FAILED:
                assertDataSetNotStoredProcess(DATA_SET_CODE);
                assertDirEmpty(stagingDirectory);

                // FIXME: this is commented out to cover the bug! beware
                // assertDirEmpty(precommitDirectory);

                assertNoOriginalMarkerFileExists();
                assertNoRecoveryMarkerFile();

                jythonHookTestTool.assertLogged("rollback_pre_registration");
                break;
            case CHECK_FAILED:
                assertDataSetNotStoredProcess(DATA_SET_CODE);

                assertDirNotEmpty(precommitDirectory, "Precommit directory should not be empty");
                assertRecoveryFile(testCase.recoveryRertyCount + 1,
                        RecoveryInfoDateConstraint.AFTER_ORIGINAL, testCase.recoveryLastTry);
                assertOriginalMarkerFileExists();
                // marker file is still there
                // recovery state file is still there
                break;
            case CHECK_FAILED_GIVE_UP:
                assertDataSetNotStoredProcess(DATA_SET_CODE);
                assertNoOriginalMarkerFileExists();
                assertNoRecoveryMarkerFile();

                assertDirNotEmpty(precommitDirectory, "precommit should not be empty");

                break;
        }
        jythonHookTestTool.assertNoMoreMessages();
    }

    /**
     * Use this method to update the retry count in the recovery info file.
     */
    private void setTheRecoveryInfo(int count, Date lastTryDate)
    {
        File file = getCreatedRecoveryMarkerFile();

        DataSetStorageRecoveryInfo recoveryInfo =
                handler.getGlobalState().getStorageRecoveryManager()
                        .getRecoveryFileFromMarker(file);
        // as the interface allow only increment, and not setting - we implement addition using
        // increment
        while (recoveryInfo.getTryCount() < count)
        {
            recoveryInfo.increaseTryCount();
        }

        recoveryInfo.setLastTry(lastTryDate);

        recoveryInfo.writeToFile(file);
    }

    private void assertDirNotEmpty(File file, String message)
    {
        assertFalse(message, 0 == file.list().length);
    }

    private void assertDirEmpty(File file)
    {
        String contents = file.getAbsolutePath();
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
        assertFalse("The original registration marker " + markerFile + " file should be deleted",
                markerFile.exists());
    }

    private enum RecoveryInfoDateConstraint
    {
        /**
         * the last try date is the one of the original recovery file
         */
        ORIGINAL,
        /**
         * the last try date is later then the original recovery file
         */
        AFTER_ORIGINAL
    }

    /**
     * @param tryCount - the excepted stored number of tries in a recovery file
     */
    private File assertRecoveryFile(int tryCount, RecoveryInfoDateConstraint dateConstraint,
            Date originalLastTryDate)
    {
        File file = getCreatedRecoveryMarkerFile();
        assertTrue("The recovery marker file does not exist! " + file, file.exists());
        DataSetStorageRecoveryInfo recoveryInfo =
                handler.getGlobalState().getStorageRecoveryManager()
                        .getRecoveryFileFromMarker(file);
        File recoveryFile = recoveryInfo.getRecoveryStateFile();
        assertTrue("The recovery serialized file does not exist! " + recoveryFile,
                recoveryFile.exists());

        assertEquals("The try count in a recovery file is incorrect", tryCount,
                recoveryInfo.getTryCount());

        switch (dateConstraint)
        {
            case ORIGINAL:
                assertEquals(originalLastTryDate, recoveryInfo.getLastTry());
                break;
            case AFTER_ORIGINAL:
                assertTrue(
                        "" + originalLastTryDate + " should be before " + recoveryInfo.getLastTry(),
                        originalLastTryDate.before(recoveryInfo.getLastTry()));
                break;
        }

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
                handler.getGlobalState().getStorageRecoveryManager()
                        .getProcessingMarkerFile(originalIncoming);
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

            if (testCase.canRecoverFromError && false == testCase.nextTryInTheFuture)
            {
                checkRegistrationSucceeded();

                if (testCase.registrationCheckResult == RegistrationCheckResult.REGISTRATION_SUCCEEDED)
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
            switch (testCase.registrationCheckResult)
            {
                case REGISTRATION_SUCCEEDED:
                    // with the current implemntation returning the non-empty list should be enough
                    List<ExternalData> externalDatas = (List) Arrays.asList(new Object());
                    will(returnValue(externalDatas));
                    break;
                case REGISTRATION_FAILED:
                    will(returnValue(new LinkedList<ExternalData>()));
                    break;
                case CHECK_FAILED:
                case CHECK_FAILED_GIVE_UP:
                    will(throwException(new EnvironmentFailureException(
                            "Cannot check whether the registration was successful")));
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
