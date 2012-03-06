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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.python.core.PyException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.eodsql.MockDataSet;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.utilities.IDelegatedAction;
import ch.systemsx.cisd.common.utilities.IPredicate;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PropertyBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class JythonTopLevelDataSetRegistratorTest extends AbstractJythonDataSetHandlerTest
{
    private static final String SCRIPTS_FOLDER =
            "sourceTest/java/ch/systemsx/cisd/etlserver/registrator/";

    private static final String DATA_SET_CODE = "data-set-code";

    private static final String DATA_SET_CODE_1 = "data-set-code-1";

    private static final String CONTAINER_DATA_SET_CODE = "container-data-set-code";

    private static final DataSetType DATA_SET_TYPE = new DataSetType("O1");

    private static final String EXPERIMENT_PERM_ID = "experiment-perm-id";

    private static final String EXPERIMENT_IDENTIFIER = "/SPACE/PROJECT/EXP";

    private static final String SAMPLE_PERM_ID = "sample-perm-id";

    private BufferedAppender logAppender;

    @BeforeMethod
    @Override
    public void setUp() throws IOException
    {
        super.setUp();

        logAppender = new BufferedAppender();
    }

    public ArrayList<TestCaseParameters> multipleVersionsOfTestCase(TestCaseParameters params)
    {
        ArrayList<TestCaseParameters> list = new ArrayList<TestCaseParameters>(2);
        list.add(params);
        list.add(versionV2(params));
        return list;
    }

    public TestCaseParameters versionV2(TestCaseParameters params)
    {
        params = params.clone();
        params.overrideProperties = (HashMap<String, String>) params.overrideProperties.clone();
        params.overrideProperties.put("TEST_V2_API", "");
        params.title += " - V2";
        return params;
    }

    @DataProvider(name = "simpleTransactionTestCaseProvider")
    public Object[][] simpleTransactionCases()
    {
        /* --- create some useful data for the scenarios --- */

        // creates the property with the setting
        HashMap<String, String> dontUsePrestaging = new HashMap<String, String>();
        dontUsePrestaging.put(ThreadParameters.DATASET_REGISTRATION_PRE_STAGING_BEHAVIOR,
                DataSetRegistrationPreStagingBehavior.USE_ORIGINAL.toString().toLowerCase());

        // creates data with more than only one dataset
        IDelegatedAction createTwoDataSetsDelegate = new IDelegatedAction()
            {
                public void execute()
                {
                    createData();
                }
            };

        LinkedList<TestCaseParameters> testCases =
                new LinkedList<JythonTopLevelDataSetRegistratorTest.TestCaseParameters>();

        // basic testCase
        testCases.addAll(multipleVersionsOfTestCase(new TestCaseParameters(
                "Basic successful registration")));

        // testCase without prestaging
        TestCaseParameters testCase =
                new TestCaseParameters(
                        "registration without prestaging. Should clean the incoming directory.");
        testCase.overrideProperties = dontUsePrestaging;
        testCase.incomingDataSetAfterRegistration = "empty";
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        // without pre-staging with some data left in incoming directory
        // this test case is for a particular users, who use incoming directory in the way they
        // aren't supposed to
        testCase =
                new TestCaseParameters(
                        "registration without prestaging. Should leave some data in the incoming directory.");
        testCase.overrideProperties = dontUsePrestaging;
        testCase.incomingDataSetAfterRegistration = "content";
        testCase.createDataSetDelegate = createTwoDataSetsDelegate;
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        // simple test failing registration testCase
        testCase = new TestCaseParameters("The simple transaction rollback.");
        testCase.incomingDataSetAfterRegistration = "untouched_two_datasets";
        testCase.createDataSetDelegate = createTwoDataSetsDelegate;
        testCase.shouldRegistrationFail = true;
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        String[] allErrors =
                    { ConfiguredOnErrorActionDecision.INVALID_DATA_SET_KEY,
                            ConfiguredOnErrorActionDecision.OPENBIS_REGISTRATION_FAILURE_KEY,
                            ConfiguredOnErrorActionDecision.POST_REGISTRATION_ERROR_KEY,
                            ConfiguredOnErrorActionDecision.REGISTRATION_SCRIPT_ERROR_KEY,
                            ConfiguredOnErrorActionDecision.STORAGE_PROCESSOR_ERROR_KEY,
                            ConfiguredOnErrorActionDecision.VALIDATION_SCRIPT_ERROR_KEY, };

        // simple test failing registration testCase
        testCase = new TestCaseParameters("The simple transaction rollback with DELETE on error.");
        for (String error : allErrors)
        {
            testCase.overrideProperties.put(ThreadParameters.ON_ERROR_DECISION_KEY + "." + error,
                    UnstoreDataAction.DELETE.toString());
        }
        testCase.incomingDataSetAfterRegistration = "deleted";
        testCase.createDataSetDelegate = createTwoDataSetsDelegate;
        testCase.shouldRegistrationFail = true;
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        // simple test failing registration testCase
        testCase =
                new TestCaseParameters(
                        "The simple transaction rollback with DELETE on error without prestaging.");
        testCase.overrideProperties.put(ThreadParameters.DATASET_REGISTRATION_PRE_STAGING_BEHAVIOR,
                DataSetRegistrationPreStagingBehavior.USE_ORIGINAL.toString().toLowerCase());

        for (String error : allErrors)
        {
            testCase.overrideProperties.put(ThreadParameters.ON_ERROR_DECISION_KEY + "." + error,
                    UnstoreDataAction.DELETE.toString());
        }

        testCase.incomingDataSetAfterRegistration = "deleted";
        testCase.createDataSetDelegate = createTwoDataSetsDelegate;
        testCase.shouldRegistrationFail = true;
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        // TODO: In this case should it be "invalid dataset error" or what?
        testCase = new TestCaseParameters("The validation error with DELETE on error.");
        for (String error : allErrors)
        {
            testCase.overrideProperties.put(ThreadParameters.ON_ERROR_DECISION_KEY + "." + error,
                    UnstoreDataAction.DELETE.toString());
        }
        testCase.incomingDataSetAfterRegistration = "deleted";
        testCase.shouldValidationFail = true;
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        testCase =
                new TestCaseParameters(
                        "The simple validation without post_storage function defined.");
        testCase.dropboxScriptPath = "testcase-without-post-storage.py";
        testCase.postStorageFunctionNotDefinedInADropbox = true;
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        testCase = new TestCaseParameters("Dataset file not found.");
        testCase.dropboxScriptPath = "file-not-found.py";
        testCase.shouldThrowExceptionDuringRegistration = true;
        testCase.failurePoint = TestCaseParameters.FailurePoint.AFTER_CREATE_DATA_SET_CODE;
        testCase.exceptionAcceptor = new IPredicate<Exception>()
            {
                public boolean execute(Exception arg)
                {
                    PyException pyException = (PyException) arg;
                    IOExceptionUnchecked tunnel = (IOExceptionUnchecked) pyException.getCause();
                    FileNotFoundException ex = (FileNotFoundException) tunnel.getCause();
                    return ex.getMessage().startsWith("Neither '/non/existent/path' nor '");
                }
            };
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        testCase = new TestCaseParameters("Test for registration context in hook methods.");
        testCase.dropboxScriptPath = "testcase-registration-context.py";
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        testCase =
                new TestCaseParameters(
                        "Test for preregistration hook preventing registration in application server.");
        testCase.dropboxScriptPath = "testcase-preregistration-hook-failed.py";
        testCase.shouldThrowExceptionDuringRegistration = true;
        testCase.failurePoint = TestCaseParameters.FailurePoint.BEFORE_OPENBIS_REGISTRATION;
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        testCase =
                new TestCaseParameters(
                        "Postregistration hook error should not prevent succesfull registration.");
        testCase.dropboxScriptPath = "testcase-postregistration-hook-failed.py";
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        testCase = new TestCaseParameters("Postregistration hook has wrong signature.");
        testCase.dropboxScriptPath = "testcase-postregistration-hook-wrong-signature.py";
        testCase.shouldThrowExceptionDuringRegistration = true;
        testCase.exceptionAcceptor = new IPredicate<Exception>()
            {
                public boolean execute(Exception arg)
                {
                    System.out.println(arg);
                    System.out.println(arg.getMessage());
                    return arg.getMessage().contains("wrong number of arguments");
                }
            };
        testCase.failurePoint = TestCaseParameters.FailurePoint.AFTER_GET_EXPERIMENT;
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        testCase = new TestCaseParameters("Two transactions.");
        testCase.dropboxScriptPath = "testcase-double-transaction.py";
        testCase.shouldRegisterTwoDataSets = true;
        testCase.createDataSetDelegate = createTwoDataSetsDelegate;
        testCases.add(testCase);

        testCase = new TestCaseParameters("Two transactions.");
        testCase.dropboxScriptPath = "testcase-double-transaction.py";
        testCase = versionV2(testCase);
        testCase.shouldThrowExceptionDuringRegistration = true;
        testCase.failurePoint = TestCaseParameters.FailurePoint.AT_THE_BEGINNING;
        testCases.add(testCase);
        
        // here is crappy code for
        // return parameters.map( (x) => new Object[]{x} )
        Object[][] resultsList = new Object[testCases.size()][];

        int index = 0;
        for (TestCaseParameters t : testCases)
        {
            resultsList[index++] = new Object[]
                { t };
        }

        return resultsList;
    }

    /**
     * Parameters for the single run of the testSimpleTransaction
     * 
     * @author jakubs
     */
    private static class TestCaseParameters implements Cloneable
    {
        /**
         * short description of the test. Will be presented in the test results view
         */
        protected String title;

        /**
         * The dropbox script file that should be used for this test case
         */
        protected String dropboxScriptPath = "simple-testcase.py";

        /**
         * Specifies what properties should be overriden for this test case.
         */
        protected HashMap<String, String> overrideProperties;

        /**
         * Describe what should happen with incoming data after execution of this test case.
         */
        protected String incomingDataSetAfterRegistration = "deleted";

        /**
         * Specifies the custom creator of datasets instead of createDataWithOneSubDataSet.
         */
        protected IDelegatedAction createDataSetDelegate = null;

        /**
         * True if the registration of metadata should fail
         */
        protected boolean shouldRegistrationFail = false;

        /**
         * True if assertValidDataSet method should return validation error on dataset
         */
        protected boolean shouldValidationFail = false;

        /**
         * Specifies how far we expect the registration process to go.
         */
        protected FailurePoint failurePoint = null;

        /**
         * True if the registration should throw exception to the top level.
         */
        protected boolean shouldThrowExceptionDuringRegistration = false;

        /**
         * Must return true for the exception from the registration process, when one is caught.
         */
        protected IPredicate<Exception> exceptionAcceptor = null;

        /**
         * True if commit_transaction function is defined in a jython dropbox script file, and
         * post_storage function is not.
         */
        protected boolean postStorageFunctionNotDefinedInADropbox = false;

        /**
         * If true, than we expect that two datasets have been registered.
         */
        protected boolean shouldRegisterTwoDataSets = false;

        private TestCaseParameters(String title)
        {
            this.title = title;
            this.overrideProperties = new HashMap<String, String>();
        };

        @Override
        public TestCaseParameters clone()
        {
            try
            {
                return (TestCaseParameters) super.clone();
            } catch (CloneNotSupportedException e)
            {
                return null;
            }
        }

        @Override
        public String toString()
        {
            return title;
        }

        // add more when necessary
        public enum FailurePoint
        {
            AT_THE_BEGINNING, AFTER_CREATE_DATA_SET_CODE, BEFORE_OPENBIS_REGISTRATION,
            AFTER_GET_EXPERIMENT
        }
    }

    @Test(dataProvider = "simpleTransactionTestCaseProvider")
    public void testSimpleTransaction(final TestCaseParameters testCase)
    {
        setUpHomeDataBaseExpectations();

        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder(testCase.dropboxScriptPath,
                        testCase.overrideProperties);

        if (testCase.shouldRegistrationFail || testCase.shouldValidationFail)
        {
            createHandler(properties, false, false);
        } else
        {
            createHandler(properties, false, true);
        }

        if (testCase.createDataSetDelegate != null)
        {
            testCase.createDataSetDelegate.execute();
        } else
        {
            createDataWithOneSubDataSet();
        }

        ExperimentBuilder builder = new ExperimentBuilder().identifier(EXPERIMENT_IDENTIFIER);
        final Experiment experiment = builder.getExperiment();
        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();

        context.checking(new Expectations()
            {
                {
                    setupExpectations();
                }

                protected void setupExpectations()
                {
                    if (testCase.failurePoint == TestCaseParameters.FailurePoint.AT_THE_BEGINNING)
                    {
                        return;
                    }

                    createDataSet();

                    if (testCase.failurePoint == TestCaseParameters.FailurePoint.AFTER_CREATE_DATA_SET_CODE)
                    {
                        return;
                    }

                    tryGetExperiment();

                    if (testCase.failurePoint == TestCaseParameters.FailurePoint.AFTER_GET_EXPERIMENT)
                    {
                        return;
                    }

                    validateDataSet();

                    if (testCase.shouldValidationFail)
                    {
                        return;
                    }

                    if (testCase.failurePoint == TestCaseParameters.FailurePoint.BEFORE_OPENBIS_REGISTRATION)
                    {
                        return;
                    }

                    registerDataSets();

                    if (testCase.shouldRegistrationFail)
                    {
                        return;
                    }

                    setStorageConfirmed();

                    will(checkPrecommitDirIsEmpty());
                }

                protected void setStorageConfirmed()
                {
                    one(openBisService).setStorageConfirmed(DATA_SET_CODE);

                    if (testCase.shouldRegisterTwoDataSets)
                    {
                        one(openBisService).setStorageConfirmed(DATA_SET_CODE_1);
                    }
                }

                protected void registerDataSets()
                {
                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));

                    if (testCase.shouldRegistrationFail)
                    {
                        will(throwException(new AssertionError("Fail")));
                    } else
                    {
                        // return value from performEntityOperations
                        // perform additional check if the precommit dir is empty immediatelly after
                        // performEntityOperations returns
                        will(doAll(returnValue(new AtomicEntityOperationResult()),
                                checkPrecommitDirIsNotEmpty()));

                        if (testCase.shouldRegisterTwoDataSets)
                        {
                            one(openBisService).performEntityOperations(
                                    with(atomicatOperationDetails));
                            will(doAll(returnValue(new AtomicEntityOperationResult()),
                                    checkPrecommitDirIsNotEmpty()));
                        }
                    }
                }

                protected void validateDataSet()
                {
                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"));

                    if (testCase.shouldValidationFail)
                    {
                        Exception innerException = new Exception();
                        will(throwException(new UserFailureException("Data set of type '"
                                + DATA_SET_CODE + "' is invalid ", innerException)));
                    } else if (testCase.shouldRegisterTwoDataSets)
                    {
                        one(dataSetValidator).assertValidDataSet(
                                DATA_SET_TYPE,
                                new File(new File(stagingDirectory, DATA_SET_CODE_1),
                                        "sub_data_set_2"));
                    }
                }

                protected void tryGetExperiment()
                {
                    atLeast(1).of(openBisService).tryToGetExperiment(
                            new ExperimentIdentifierFactory(experiment.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment));
                }

                protected void createDataSet()
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE));
                    if (testCase.shouldRegisterTwoDataSets)
                    {
                        one(openBisService).createDataSetCode();
                        will(returnValue(DATA_SET_CODE_1));
                    }
                }
            });

        if (testCase.shouldThrowExceptionDuringRegistration)
        {
            try
            {
                handler.handle(markerFile);
                fail("Expected an exception.");
            } catch (Exception exception)
            {
                if (testCase.exceptionAcceptor != null)
                {
                    assertTrue("Exception " + exception + "was not accepted by validator",
                            testCase.exceptionAcceptor.execute(exception));
                }
            }
            context.assertIsSatisfied();
            return;
        } else
        {
            handler.handle(markerFile);
        }

        checkInitialDirAfterRegistration(testCase.incomingDataSetAfterRegistration);

        if (false == testCase.shouldValidationFail)
        {
            // the incoming dir in storage processor is created at the beginning of transaction
            // so after the successful validation

            int dataSetsStoredInIncomingDir = testCase.shouldRegisterTwoDataSets ? 2 : 1;
            assertEquals(dataSetsStoredInIncomingDir,
                    MockStorageProcessor.instance.incomingDirs.size());
        }

        assertCommitCount(testCase);

        assertJythonHooksExecuted(testCase);

        if (testCase.shouldValidationFail)
        {
        } else if (testCase.shouldRegistrationFail)
        {
            assertEquals("[]", Arrays.asList(stagingDirectory.list()).toString());
        } else
        {
            if (testCase.shouldRegisterTwoDataSets)
            {
                List<AtomicEntityOperationDetails> recordedObjects =
                        atomicatOperationDetails.getRecordedObjects();

                assertEquals("There should be two items in recordedObjects", 2,
                        recordedObjects.size());
                assertStorageProcess(recordedObjects.get(0), DATA_SET_CODE, "sub_data_set_1", 0);
                assertStorageProcess(recordedObjects.get(1), DATA_SET_CODE_1, "sub_data_set_2", 1);
            } else
            {
                assertStorageProcess(atomicatOperationDetails.recordedObject(), DATA_SET_CODE,
                        "sub_data_set_1", 0);
            }
        }
        context.assertIsSatisfied();
    }

    protected void assertStorageProcess(AtomicEntityOperationDetails recordedObject,
            String dataSetCode, String dataSetDirectory, int testId)
    {
        assertEquals(1, recordedObject.getDataSetRegistrations().size());

        NewExternalData dataSet = recordedObject.getDataSetRegistrations().get(0);

        assertEquals(dataSetCode, dataSet.getCode());
        assertEquals(DATA_SET_TYPE, dataSet.getDataSetType());

        File datasetLocation =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, dataSetCode,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        DATABASE_INSTANCE_UUID);

        assertEquals(FileUtilities.getRelativeFilePath(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                datasetLocation), dataSet.getLocation());

        assertEquals(new File(stagingDirectory, dataSetCode + "-storage"),
                MockStorageProcessor.instance.rootDirs.get(testId));

        File incomingDir = MockStorageProcessor.instance.incomingDirs.get(testId);

        assertEquals(new File(new File(stagingDirectory, dataSetCode), dataSetDirectory),
                incomingDir);

        assertEquals("hello world" + (testId + 1),
                FileUtilities
                        .loadToString(new File(datasetLocation, "read" + (testId + 1) + ".me"))
                        .trim());
    }

    protected void assertCommitCount(final TestCaseParameters testCase)
    {
        int expectedCommitCount;
        if (testCase.shouldRegistrationFail || testCase.shouldValidationFail)
        {
            expectedCommitCount = 0;
        } else if (testCase.shouldRegisterTwoDataSets)
        {
            expectedCommitCount = 2;
        } else
        {
            expectedCommitCount = 1;
        }

        assertEquals(expectedCommitCount, MockStorageProcessor.instance.calledCommitCount);
    }

    private void assertJythonHooksExecuted(final TestCaseParameters testCase)
    {
        assertNull("Registration context error occured: "
                + handler.getExpectations().registrationContextError,
                handler.getExpectations().registrationContextError);

        if (testCase.shouldValidationFail)
        {
        } else if (testCase.shouldRegistrationFail)
        {
            assertFalse(handler.getExpectations().didRollbackServiceFunctionRun);
            assertTrue(handler.getExpectations().didTransactionRollbackHappen);
            assertTrue(handler.getExpectations().didRollbackTransactionFunctionRunHappen);

            assertTrue(handler.getExpectations().didPreRegistrationFunctionRunHappen);
            assertFalse(handler.getExpectations().didPostRegistrationFunctionRunHappen);

            assertFalse(handler.getExpectations().didCommitTransactionFunctionRunHappen);
            assertFalse(handler.getExpectations().didPostStorageFunctionRunHappen);

        } else
        {
            assertFalse(handler.getExpectations().didRollbackTransactionFunctionRunHappen);

            assertTrue(handler.getExpectations().didPreRegistrationFunctionRunHappen);
            assertTrue(handler.getExpectations().didPostRegistrationFunctionRunHappen);

            if (testCase.postStorageFunctionNotDefinedInADropbox)
            {
                assertTrue(handler.getExpectations().didCommitTransactionFunctionRunHappen);
                assertFalse(handler.getExpectations().didPostStorageFunctionRunHappen);
            } else
            {
                assertFalse(handler.getExpectations().didCommitTransactionFunctionRunHappen);
                assertTrue(handler.getExpectations().didPostStorageFunctionRunHappen);
            }
        }
    }

    @Test
    public void testSimpleTransactionExplicitRollback()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("testcase-rollback.py");
        createHandler(properties, true, false);
        createData();
        ExperimentBuilder builder = new ExperimentBuilder().identifier(EXPERIMENT_IDENTIFIER);
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
        checkStagingDirIsEmpty();
        assertEquals(
                "hello world1",
                FileUtilities.loadToString(
                        new File(workingDirectory, "data_set/sub_data_set_1/read1.me")).trim());
        assertEquals(
                "hello world2",
                FileUtilities.loadToString(
                        new File(workingDirectory, "data_set/sub_data_set_2/read2.me")).trim());

        assertFalse(handler.getExpectations().didRollbackServiceFunctionRun);

        // These do not get called when the caller herself invokes a rollback
        assertFalse(handler.getExpectations().didTransactionRollbackHappen);
        assertFalse(handler.getExpectations().didRollbackTransactionFunctionRunHappen);

        context.assertIsSatisfied();
    }

    @Test
    public void testTwoSimpleDataSets()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("two-simple-datasets.py");
        createHandler(properties, false, true);
        createData();
        ExperimentBuilder builder1 = new ExperimentBuilder().identifier("/SPACE/PROJECT/EXP1");
        final Experiment experiment1 = builder1.getExperiment();
        ExperimentBuilder builder2 = new ExperimentBuilder().identifier("/SPACE/PROJECT/EXP2");
        final Experiment experiment2 = builder2.getExperiment();
        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> operations =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE + 1));

                    atLeast(1).of(openBisService).tryToGetExperiment(
                            new ExperimentIdentifierFactory(experiment1.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment1));

                    one(dataSetValidator).assertValidDataSet(
                            DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE + 1),
                                    "sub_data_set_1"));

                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE + 2));

                    atLeast(1).of(openBisService).tryToGetExperiment(
                            new ExperimentIdentifierFactory(experiment2.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment2));

                    one(dataSetValidator).assertValidDataSet(
                            DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE + 2),
                                    "sub_data_set_2"));

                    one(openBisService).performEntityOperations(with(operations));
                    will(returnValue(new AtomicEntityOperationResult()));

                    one(openBisService).setStorageConfirmed(DATA_SET_CODE + 1);
                    one(openBisService).setStorageConfirmed(DATA_SET_CODE + 2);
                }
            });

        handler.handle(markerFile);
        checkInitialDirAfterRegistration("deleted");

        assertEquals(2, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(2, MockStorageProcessor.instance.calledCommitCount);
        assertEquals(2, operations.recordedObject().getDataSetRegistrations().size());

        NewExternalData dataSet1 = operations.recordedObject().getDataSetRegistrations().get(0);
        NewExternalData dataSet2 = operations.recordedObject().getDataSetRegistrations().get(1);

        assertEquals(experiment1.getIdentifier(), dataSet1.getExperimentIdentifierOrNull()
                .toString());
        assertEquals(DATA_SET_CODE + 1, dataSet1.getCode());
        assertEquals(DATA_SET_TYPE, dataSet1.getDataSetType());
        File datasetLocation1 =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, DATA_SET_CODE + 1,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        DATABASE_INSTANCE_UUID);
        assertEquals(FileUtilities.getRelativeFilePath(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                datasetLocation1), dataSet1.getLocation());
        assertEquals(new File(stagingDirectory, DATA_SET_CODE + 1 + "-storage"),
                MockStorageProcessor.instance.rootDirs.get(0));
        File incomingDir1 = MockStorageProcessor.instance.incomingDirs.get(0);
        assertEquals(new File(new File(stagingDirectory, DATA_SET_CODE + 1), "sub_data_set_1"),
                incomingDir1);
        assertEquals("hello world1",
                FileUtilities.loadToString(new File(datasetLocation1, "read1.me")).trim());
        assertEquals(experiment2.getIdentifier(), dataSet2.getExperimentIdentifierOrNull()
                .toString());
        assertEquals(DATA_SET_CODE + 2, dataSet2.getCode());
        assertEquals(DATA_SET_TYPE, dataSet2.getDataSetType());
        File datasetLocation2 =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, DATA_SET_CODE + 2,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        DATABASE_INSTANCE_UUID);
        assertEquals(FileUtilities.getRelativeFilePath(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                datasetLocation2), dataSet2.getLocation());
        assertEquals(new File(stagingDirectory, DATA_SET_CODE + 2 + "-storage"),
                MockStorageProcessor.instance.rootDirs.get(1));
        File incomingDir2 = MockStorageProcessor.instance.incomingDirs.get(1);
        assertEquals(new File(new File(stagingDirectory, DATA_SET_CODE + 2), "sub_data_set_2"),
                incomingDir2);
        assertEquals("hello world2",
                FileUtilities.loadToString(new File(datasetLocation2, "read2.me")).trim());
        context.assertIsSatisfied();
    }

    @Test
    public void testTransactionWithNewExperiment()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("transaction-with-new-experiment.py");
        createHandler(properties, false, true);
        createDataWithOneSubDataSet();
        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE));

                    one(openBisService).createPermId();
                    will(returnValue(EXPERIMENT_PERM_ID));

                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"));
                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));

                    one(openBisService).setStorageConfirmed(DATA_SET_CODE);
                }
            });

        handler.handle(markerFile);

        checkInitialDirAfterRegistration("deleted");
        assertEquals(1, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(1, atomicatOperationDetails.recordedObject().getDataSetRegistrations().size());

        NewExternalData dataSet =
                atomicatOperationDetails.recordedObject().getDataSetRegistrations().get(0);

        assertEquals(DATA_SET_CODE, dataSet.getCode());
        assertEquals(DATA_SET_TYPE, dataSet.getDataSetType());

        NewProperty newProp = new NewProperty("dataSetProp", "dataSetPropValue");
        assertTrue(dataSet.getExtractableData().getDataSetProperties().contains(newProp));

        File datasetLocation =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, DATA_SET_CODE,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        DATABASE_INSTANCE_UUID);
        assertEquals(FileUtilities.getRelativeFilePath(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                datasetLocation), dataSet.getLocation());
        assertEquals(1, MockStorageProcessor.instance.calledCommitCount);
        File incomingDir = MockStorageProcessor.instance.incomingDirs.get(0);
        assertEquals(new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"),
                incomingDir);
        context.assertIsSatisfied();
    }

    @Test
    public void testTransactionWithNewSample()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("transaction-with-new-sample.py");
        createHandler(properties, false, true);
        createData();
        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE));

                    one(openBisService).createPermId();
                    will(returnValue(EXPERIMENT_PERM_ID));

                    one(openBisService).createPermId();
                    will(returnValue(SAMPLE_PERM_ID));

                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"));
                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));

                    one(openBisService).setStorageConfirmed(DATA_SET_CODE);
                }
            });

        handler.handle(markerFile);

        assertEquals(1, MockStorageProcessor.instance.incomingDirs.size());
        ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails operations =
                atomicatOperationDetails.recordedObject();

        assertEquals(1, operations.getDataSetRegistrations().size());
        assertEquals(0, operations.getExperimentUpdates().size());
        assertEquals(0, operations.getSampleUpdates().size());
        assertEquals(1, operations.getSampleRegistrations().size());
        assertEquals(1, operations.getExperimentRegistrations().size());

        NewSample newSample = operations.getSampleRegistrations().get(0);
        assertEquals(SAMPLE_PERM_ID, newSample.getPermID());
        assertEquals(EXPERIMENT_IDENTIFIER, newSample.getExperimentIdentifier());
        assertEquals("sample_type", newSample.getSampleType().getCode());

        NewExperiment newExperiment = operations.getExperimentRegistrations().get(0);
        assertEquals(EXPERIMENT_PERM_ID, newExperiment.getPermID());
        assertEquals(EXPERIMENT_IDENTIFIER, newExperiment.getIdentifier());
        assertEquals("experiment_type", newExperiment.getExperimentTypeCode());

        NewExternalData dataSet = operations.getDataSetRegistrations().get(0);
        assertEquals(DATA_SET_CODE, dataSet.getCode());
        assertEquals(DATA_SET_TYPE, dataSet.getDataSetType());

        File datasetLocation =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, DATA_SET_CODE,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        DATABASE_INSTANCE_UUID);
        assertEquals(FileUtilities.getRelativeFilePath(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                datasetLocation), dataSet.getLocation());
        assertEquals(1, MockStorageProcessor.instance.calledCommitCount);
        File incomingDir = MockStorageProcessor.instance.incomingDirs.get(0);
        assertEquals(new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"),
                incomingDir);
        context.assertIsSatisfied();
    }

    @Test
    public void testTransactionWithNewMaterial()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("transaction-with-new-material.py");
        createHandler(properties, false, true);
        createData();

        ExperimentBuilder builder = new ExperimentBuilder().identifier(EXPERIMENT_IDENTIFIER);
        final Experiment experiment = builder.getExperiment();
        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
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
                            new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"));
                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));

                    one(openBisService).setStorageConfirmed(DATA_SET_CODE);
                }
            });

        handler.handle(markerFile);

        assertEquals(1, MockStorageProcessor.instance.incomingDirs.size());
        ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails operations =
                atomicatOperationDetails.recordedObject();

        assertEquals(1, operations.getDataSetRegistrations().size());
        assertEquals(0, operations.getExperimentUpdates().size());
        assertEquals(0, operations.getSampleUpdates().size());
        assertEquals(0, operations.getSampleRegistrations().size());
        assertEquals(0, operations.getExperimentRegistrations().size());
        assertEquals(1, operations.getMaterialRegistrations().size());

        NewMaterial newMaterial =
                operations.getMaterialRegistrations().get("new-material-type").get(0);
        assertEquals("new-material", newMaterial.getCode());
        assertEquals("[material-prop: material-prop-value]",
                Arrays.asList(newMaterial.getProperties()).toString());

        NewExternalData dataSet = operations.getDataSetRegistrations().get(0);
        assertEquals(DATA_SET_CODE, dataSet.getCode());
        assertEquals(DATA_SET_TYPE, dataSet.getDataSetType());

        context.assertIsSatisfied();
    }

    @Test
    public void testTransactionWithDataSetUpdate()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("transaction-with-dataset-update.py");
        createHandler(properties, false, true);
        createData();

        ExperimentBuilder builder = new ExperimentBuilder().identifier(EXPERIMENT_IDENTIFIER);
        final Experiment experiment = builder.getExperiment();

        final ContainerDataSet containerDataSet = new ContainerDataSet();
        containerDataSet.setId(1L);

        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE));

                    exactly(2).of(openBisService).tryToGetExperiment(
                            new ExperimentIdentifierFactory(experiment.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment));

                    one(openBisService).tryGetDataSet(CONTAINER_DATA_SET_CODE);
                    will(returnValue(containerDataSet));

                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE), "data_set"));

                    one(openBisService).performEntityOperations(with(atomicOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));

                    one(openBisService).setStorageConfirmed(DATA_SET_CODE);
                }
            });

        handler.handle(markerFile);

        assertEquals(1, MockStorageProcessor.instance.incomingDirs.size());
        ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails operations =
                atomicOperationDetails.recordedObject();

        assertEquals(1, operations.getDataSetRegistrations().size());
        assertEquals(0, operations.getExperimentUpdates().size());
        assertEquals(0, operations.getSampleUpdates().size());
        assertEquals(0, operations.getSampleRegistrations().size());
        assertEquals(0, operations.getExperimentRegistrations().size());
        assertEquals(1, operations.getDataSetUpdates().size());

        NewExternalData dataSet = operations.getDataSetRegistrations().get(0);
        assertEquals(DATA_SET_CODE, dataSet.getCode());
        assertEquals(DATA_SET_TYPE, dataSet.getDataSetType());

        DataSetUpdatesDTO dataSetUpdate = operations.getDataSetUpdates().get(0);
        assertEquals(Arrays.asList(DATA_SET_CODE),
                Arrays.asList(dataSetUpdate.getModifiedContainedDatasetCodesOrNull()));

        EntityProperty propertyChanged =
                new PropertyBuilder("newProp").value("newValue").getProperty();
        assertEquals(Arrays.asList(propertyChanged).toString(), dataSetUpdate.getProperties()
                .toString());
        assertEquals(EXPERIMENT_IDENTIFIER, dataSetUpdate.getExperimentIdentifierOrNull()
                .toString());

        File datasetLocation =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, DATA_SET_CODE,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        DATABASE_INSTANCE_UUID);
        assertEquals(FileUtilities.getRelativeFilePath(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                datasetLocation), dataSet.getLocation());
        assertEquals(1, MockStorageProcessor.instance.calledCommitCount);
        context.assertIsSatisfied();
    }

    @Test
    public void testScriptDies()
    {
        setUpHomeDataBaseExpectations();

        Properties threadProperties =
                createThreadPropertiesRelativeToScriptsFolder("dying-script.py");

        createHandler(threadProperties, false);
        createData();

        handler.handle(markerFile);

        checkStagingDirIsEmpty();

        assertTrue(logAppender.getLogContent(), logAppender.getLogContent().length() > 0);

        assertEquals(0, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(0, MockStorageProcessor.instance.calledCommitCount);
        context.assertIsSatisfied();
    }

    private void createData()
    {
        incomingDataSetFile = createDirectory(workingDirectory, "data_set");

        assertTrue(incomingDataSetFile.isDirectory());

        subDataSet1 = createDirectory(incomingDataSetFile, "sub_data_set_1");
        subDataSet2 = createDirectory(incomingDataSetFile, "sub_data_set_2");

        FileUtilities.writeToFile(new File(subDataSet1, "read1.me"), "hello world1");
        FileUtilities.writeToFile(new File(subDataSet2, "read2.me"), "hello world2");

        markerFile = new File(workingDirectory, IS_FINISHED_PREFIX + "data_set");
        FileUtilities.writeToFile(markerFile, "");
    }

    private void createDataWithOneSubDataSet()
    {
        incomingDataSetFile = createDirectory(workingDirectory, "data_set");

        assertTrue(incomingDataSetFile.isDirectory());

        subDataSet1 = createDirectory(incomingDataSetFile, "sub_data_set_1");

        FileUtilities.writeToFile(new File(subDataSet1, "read1.me"), "hello world1");

        markerFile = new File(workingDirectory, IS_FINISHED_PREFIX + "data_set");
        FileUtilities.writeToFile(markerFile, "");
    }

    @Test
    public void testRollbackService()
    {
        setUpHomeDataBaseExpectations();

        // Create a handler that throws an exception during registration
        Properties threadProperties =
                createThreadPropertiesRelativeToScriptsFolder("rollback-dying-script.py");
        createHandler(threadProperties, false);

        createData();

        handler.handle(markerFile);

        checkStagingDirIsEmpty();

        assertEquals(0, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(0, MockStorageProcessor.instance.calledCommitCount);

        assertTrue(logAppender.getLogContent(), logAppender.getLogContent().length() > 0);

        assertTrue(handler.getExpectations().didRollbackServiceFunctionRun);
        context.assertIsSatisfied();
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

        assertTrue(handler.getExpectations().didServiceRollbackHappen);
        context.assertIsSatisfied();
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
        context.assertIsSatisfied();
    }

    @Test
    public void testSearching()
    {
        setUpHomeDataBaseExpectations();
        Properties threadProperties = createThreadPropertiesRelativeToScriptsFolder("search.py");
        createHandler(threadProperties, false, true);

        createData();

        setUpSearchExpectations();

        handler.handle(markerFile);

        assertEquals(0, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(0, MockStorageProcessor.instance.calledCommitCount);

        assertFalse(handler.getExpectations().didServiceRollbackHappen);
        assertFalse(handler.getExpectations().didTransactionRollbackHappen);
        assertFalse(handler.getExpectations().didRollbackServiceFunctionRun);

        context.assertIsSatisfied();
    }

    @Test
    public void testQuerying()
    {
        setUpHomeDataBaseExpectations();
        Properties threadProperties =
                createThreadPropertiesRelativeToScriptsFolder("query-interface-test.py");
        createHandler(threadProperties, false, true);

        createData();

        setUpQueryExpectations();

        handler.handle(markerFile);

        assertEquals(0, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(0, MockStorageProcessor.instance.calledCommitCount);

        assertFalse(handler.getExpectations().didServiceRollbackHappen);
        assertFalse(handler.getExpectations().didTransactionRollbackHappen);
        context.assertIsSatisfied();
    }

    @Test
    public void testDynamicQueryCommitFail()
    {
        setUpHomeDataBaseExpectations();
        Properties threadProperties =
                createThreadPropertiesRelativeToScriptsFolder("dynamic-query-failure-test.py");
        createHandler(threadProperties, false, true);

        createData();

        setUpDynamicQueryExpectations();

        handler.handle(markerFile);

        assertEquals(0, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(0, MockStorageProcessor.instance.calledCommitCount);

        assertFalse(handler.getExpectations().didServiceRollbackHappen);
        assertFalse(handler.getExpectations().didTransactionRollbackHappen);
        assertTrue(handler.getExpectations().didSecondaryTransactionErrorNotificationHappen);
        context.assertIsSatisfied();
    }

    private Properties createThreadProperties(String scriptPath)
    {
        Properties threadProperties = new Properties();
        threadProperties.put(ThreadParameters.INCOMING_DIR, "incoming");
        threadProperties.put(ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION,
                ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE);
        threadProperties.put(ThreadParameters.DELETE_UNIDENTIFIED_KEY, "false");
        threadProperties.put(IStorageProcessorTransactional.STORAGE_PROCESSOR_KEY,
                MockStorageProcessor.class.getName());
        threadProperties.put(JythonTopLevelDataSetHandler.SCRIPT_PATH_KEY, scriptPath);
        return threadProperties;
    }

    @Override
    protected String getRegistrationScriptsFolderPath()
    {
        return SCRIPTS_FOLDER;
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
        threadProperties.put(IStorageProcessorTransactional.STORAGE_PROCESSOR_KEY,
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
        context.assertIsSatisfied();
    }

    private void createHandler(Properties threadProperties, final boolean registrationShouldFail)
    {
        createHandler(threadProperties, registrationShouldFail, false);
    }

    private void setUpSearchExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    ProjectIdentifier projectIdentifier =
                            new ProjectIdentifierFactory("/SPACE/PROJECT").createIdentifier();
                    oneOf(openBisService).listExperiments(projectIdentifier);

                    Experiment experiment = new Experiment();
                    experiment.setIdentifier("/SPACE/PROJECT/EXP-CODE");
                    experiment.setCode("EXP-CODE");
                    Person registrator = new Person();
                    registrator.setEmail("email@email.com");
                    experiment.setRegistrator(registrator);
                    will(returnValue(Arrays.asList(experiment)));

                    SearchCriteria searchCriteria = createTestSearchCriteria("DATA_SET_TYPE");
                    oneOf(openBisService).searchForDataSets(searchCriteria);
                    will(returnValue(Collections.EMPTY_LIST));

                    searchCriteria = createTestSearchCriteria("SAMPLE_TYPE");
                    oneOf(openBisService).searchForSamples(searchCriteria);
                    will(returnValue(Collections.EMPTY_LIST));

                    oneOf(openBisService)
                            .performEntityOperations(
                                    with(any(ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails.class)));
                }
            });
    }

    private void setUpQueryExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    oneOf(dataSourceQueryService).select("path-info-db",
                            "SELECT * from data_set_files WHERE parent_id is NULL");
                    Object[] args =
                        { 155555 };
                    will(returnValue(new MockDataSet<Map<String, Object>>()));
                    oneOf(dataSourceQueryService).select("path-info-db",
                            "SELECT * from data_set_files WHERE parent_id = ?1", args);
                    will(returnValue(new MockDataSet<Map<String, Object>>()));
                }
            });
    }

    private void setUpDynamicQueryExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    oneOf(dynamicTransactionQuery)
                            .select("SELECT * from data_set_files WHERE parent_id is NULL",
                                    (Object[]) null);
                    will(returnValue(new MockDataSet<Map<String, Object>>()));

                    oneOf(openBisService)
                            .performEntityOperations(
                                    with(any(ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails.class)));

                }
            });
    }

    protected SearchCriteria createTestSearchCriteria(String typeString)
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, typeString));
        sc.addMatchClause(MatchClause.createPropertyMatch("PROP", "VALUE"));
        return sc;
    }

    private void checkInitialDirAfterRegistration(String expectedBehavior)
    {
        if (expectedBehavior.equals("deleted"))
        {
            assertFalse("Incoming directory should have been deleted", incomingDataSetFile.exists());
        } else if (expectedBehavior.equals("empty"))
        {
            assertTrue("Incoming directory should not be deleted.", incomingDataSetFile.exists());
            assertEquals("Incomind directory should be empty", 0,
                    incomingDataSetFile.listFiles().length);
        } else if (expectedBehavior.equals("content"))
        {
            assertTrue("Incoming directory should not be deleted.", incomingDataSetFile.exists());
            assertNotSame("The incoming directory is not expected to be empty", 0,
                    incomingDataSetFile.listFiles().length);
        } else if (expectedBehavior.equals("untouched_two_datasets"))
        {
            assertEquals("Staging directory is supposed to be empty", "[]",
                    Arrays.asList(stagingDirectory.list()).toString());
            assertEquals(
                    "The content of the incoming dataset 1 has changed",
                    "hello world1",
                    FileUtilities.loadToString(
                            new File(workingDirectory, "data_set/sub_data_set_1/read1.me")).trim());
            assertEquals(
                    "The content of the incoming dataset 2 has changed",
                    "hello world2",
                    FileUtilities.loadToString(
                            new File(workingDirectory, "data_set/sub_data_set_2/read2.me")).trim());
        } else
        {
            fail("Unknown behavior '" + expectedBehavior + "'");
        }
    }

    private CustomAction checkPrecommitDirIsEmpty()
    {
        return new CustomAction("foo")
            {
                public Object invoke(Invocation invocation) throws Throwable
                {
                    assertEquals("[]",
                            Arrays.asList(handler.getGlobalState().getPreCommitDir().list())
                                    .toString());
                    return null;
                }
            };
    }

    private CustomAction checkPrecommitDirIsNotEmpty()
    {
        return new CustomAction("foo")
            {
                public Object invoke(Invocation invocation) throws Throwable
                {
                    assertNotSame(0, handler.getGlobalState().getPreCommitDir().list().length);
                    return null;
                }
            };
    }

    private void checkStagingDirIsEmpty()
    {
        assertEquals("[]", Arrays.asList(handler.getGlobalState().getStagingDir().list())
                .toString());
    }

}
