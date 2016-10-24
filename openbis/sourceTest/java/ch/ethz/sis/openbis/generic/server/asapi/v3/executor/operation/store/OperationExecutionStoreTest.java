package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store.OperationExecutionFSStore.ERROR_FILE_NAME;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store.OperationExecutionFSStore.OPERATIONS_FILE_NAME;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store.OperationExecutionFSStore.PROGRESS_FILE_NAME;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store.OperationExecutionFSStore.RESULTS_FILE_NAME;
import static ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionAvailability.AVAILABLE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationExecutionError;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionError;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.OperationExecutionPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.UpdateSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.CreateSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.ProgressFormatter;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.OperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.config.IOperationExecutionConfig;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionAvailability;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionState;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

public class OperationExecutionStoreTest
{

    private String STORE_PATH = "targets/unit-test-wd/operation-execution-store";

    private String PROGRESS_THREAD_NAME = "operation-execution-progress";

    private Mockery mockery;

    private IOperationExecutionConfig executionConfig;

    private IOperationExecutionDBStoreDAO executionDAO;

    private OperationExecutionPermId executionId;

    private IOperationContext context;

    private SynchronousOperationExecutionOptions options;

    private IOperation operation1;

    private IOperation operation2;

    private List<IOperation> operations;

    private BufferedAppender logRecorder;

    private long timestamp;

    @BeforeMethod
    private void beforeMethod()
    {
        LogInitializer.init();
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.DEBUG);

        mockery = new Mockery();

        executionConfig = mockery.mock(IOperationExecutionConfig.class);
        executionDAO = mockery.mock(IOperationExecutionDBStoreDAO.class);
        executionId = new OperationExecutionPermId();

        PersonPE person = new PersonPE();
        person.setId(1L);

        Session session = new Session("user", "sessionToken", new Principal(), "remoteHost", 1);
        session.setPerson(person);

        context = new OperationContext(session);
        options = new SynchronousOperationExecutionOptions();
        options.setDescription("testDescription");
        options.setAvailabilityTime(100);
        options.setSummaryAvailabilityTime(200);
        options.setDetailsAvailabilityTime(300);

        SpaceCreation spaceCreation = new SpaceCreation();
        SampleUpdate sampleUpdate = new SampleUpdate();

        operation1 = new CreateSpacesOperation(spaceCreation);
        operation2 = new UpdateSamplesOperation(sampleUpdate);

        operations = Arrays.asList(operation1, operation2);

        timestamp = System.currentTimeMillis();
    }

    @AfterMethod
    public void afterMethod(Method method)
    {
        logRecorder.reset();
    }

    @Test(enabled=false)
    public void testExecutionNew()
    {
        assertExecutionDirectoryExistsWithFiles(false);

        mockery.checking(new TestExpectations()
            {
                {
                    addDefaultProgressConfigExpectations();
                    addDefaultAvailabilityConfigExpectations();

                    oneOf(executionConfig).getStorePath();
                    will(returnValue(STORE_PATH));

                    oneOf(executionDAO).findPersonById(context.getSession().tryGetPerson().getId());
                    will(returnValue(context.getSession().tryGetPerson()));

                    oneOf(executionDAO).createExecution(with(any(OperationExecutionPE.class)));
                    will(new CustomAction("assert")
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                OperationExecutionPE executionPE = (OperationExecutionPE) invocation.getParameter(0);
                                assertEquals(executionPE.getCode(), executionId.getPermId());
                                assertEquals(executionPE.getOwner(), context.getSession().tryGetPerson());
                                assertEquals(executionPE.getDescription(), options.getDescription());
                                assertEquals(executionPE.getState(), OperationExecutionState.NEW);

                                assertAvailability(executionPE.getAvailability(), AVAILABLE, executionPE.getAvailabilityTime(),
                                        options.getAvailabilityTime());

                                assertAvailability(executionPE.getSummaryAvailability(), AVAILABLE, executionPE.getSummaryAvailabilityTime(),
                                        options.getSummaryAvailabilityTime());

                                assertAvailability(executionPE.getDetailsAvailability(), AVAILABLE, executionPE.getDetailsAvailabilityTime(),
                                        options.getDetailsAvailabilityTime());

                                assertEquals(executionPE.getSummaryOperations(), operation1.getMessage() + "\n" + operation2.getMessage());
                                assertEquals(executionPE.getSummaryOperationsList(), Arrays.asList(operation1.getMessage(), operation2.getMessage()));

                                assertNull(executionPE.getSummaryProgress());
                                assertNull(executionPE.getSummaryError());

                                assertNull(executionPE.getSummaryResults());
                                assertEquals(executionPE.getSummaryResultsList(), Collections.emptyList());

                                assertTrue(timestamp <= executionPE.getCreationDate().getTime());
                                assertNull(executionPE.getStartDate());
                                assertNull(executionPE.getFinishDate());

                                return null;
                            }
                        });
                }
            });

        createStore().executionNew(context, executionId, Arrays.asList(operation1, operation2), options);

        assertExecutionDirectoryExistsWithFiles(true, OPERATIONS_FILE_NAME);

        mockery.assertIsSatisfied();
    }

    @Test(enabled=false)
    public void testExecutionScheduled()
    {
        final OperationExecutionPE executionPE = new OperationExecutionPE();
        executionPE.setState(OperationExecutionState.NEW);
        executionPE.setOwner(context.getSession().tryGetPerson());

        assertExecutionDirectoryExistsWithFiles(false);

        mockery.checking(new TestExpectations()
            {
                {
                    addDefaultProgressConfigExpectations();

                    allowing(executionDAO).findExecutionByCode(executionId.getPermId());
                    will(returnValue(executionPE));
                }
            });

        createStore().executionScheduled(context, executionId);

        assertEquals(executionPE.getState(), OperationExecutionState.SCHEDULED);
        assertExecutionDirectoryExistsWithFiles(false);

        mockery.assertIsSatisfied();
    }

    @Test(enabled=false)
    public void testExecutionRunning()
    {
        final OperationExecutionPE executionPE = new OperationExecutionPE();
        executionPE.setState(OperationExecutionState.SCHEDULED);
        executionPE.setOwner(context.getSession().tryGetPerson());

        assertExecutionDirectoryExistsWithFiles(false);

        mockery.checking(new TestExpectations()
            {
                {
                    addDefaultProgressConfigExpectations();

                    allowing(executionDAO).findExecutionByCode(executionId.getPermId());
                    will(returnValue(executionPE));
                }
            });

        createStore().executionRunning(context, executionId);

        assertEquals(executionPE.getState(), OperationExecutionState.RUNNING);
        assertTrue(timestamp <= executionPE.getStartDate().getTime());
        assertExecutionDirectoryExistsWithFiles(false);

        mockery.assertIsSatisfied();
    }

    @Test(enabled=false)
    public void testExecutionProgress() throws Exception
    {
        final IProgress progress1 = new TestProgress(1);
        final IProgress progress2 = new TestProgress(2);
        final IProgress progress3 = new TestProgress(3);

        assertExecutionDirectoryExistsWithFiles(false);

        mockery.checking(new TestExpectations()
            {
                {
                    addProgressConfigExpectations(0);
                    addDefaultAvailabilityConfigExpectations();

                    allowing(executionConfig).getStorePath();
                    will(returnValue(STORE_PATH));

                    oneOf(executionDAO).findPersonById(context.getSession().tryGetPerson().getId());
                    will(returnValue(context.getSession().tryGetPerson()));

                    oneOf(executionDAO).createExecution(with(any(OperationExecutionPE.class)));
                    will(new CustomAction("assert")
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                OperationExecutionPE executionPE = (OperationExecutionPE) invocation.getParameter(0);
                                assertEquals(executionPE.getCode(), executionId.getPermId());
                                return null;
                            }
                        });

                    oneOf(executionDAO).updateExecutionProgress(executionId.getPermId(), ProgressFormatter.format(progress1));
                    will(throwException(new RuntimeException()));

                    oneOf(executionDAO).updateExecutionProgress(executionId.getPermId(), ProgressFormatter.format(progress2));
                    oneOf(executionDAO).updateExecutionProgress(executionId.getPermId(), ProgressFormatter.format(progress3));
                }
            });

        createStore().executionNew(context, executionId, operations, options);

        context.pushProgress(progress1);
        waitForLogMessage("ERROR OPERATION.OperationExecutionStore - Couldn't synchronize progress for execution with id " + executionId.getPermId());
        assertExecutionDirectoryExistsWithFiles(true, OPERATIONS_FILE_NAME);

        context.pushProgress(progress2);
        waitForLogMessage(
                "INFO  OPERATION.OperationExecutionStore - Execution " + executionId.getPermId() + " progressed (testProgressLabel2 (2/100))");
        assertExecutionDirectoryExistsWithFiles(true, OPERATIONS_FILE_NAME, PROGRESS_FILE_NAME);

        FileUtils.deleteDirectory(getExecutionDirectory());
        assertFalse(getExecutionDirectory().exists());

        context.pushProgress(progress3);
        waitForLogMessage(
                "INFO  OPERATION.OperationExecutionStore - Execution " + executionId.getPermId() + " progressed (testProgressLabel3 (3/100))");
        assertExecutionDirectoryExistsWithFiles(false);

        mockery.assertIsSatisfied();
    }

    @Test(enabled=false)
    public void testExecutionFinishedWhenExecutionDirectoryExists()
    {
        final OperationExecutionPE executionPE = new OperationExecutionPE();
        executionPE.setState(OperationExecutionState.RUNNING);
        executionPE.setOwner(context.getSession().tryGetPerson());

        final TestResult executionResult1 = new TestResult(1);
        final TestResult executionResult2 = new TestResult(2);

        final List<IOperationResult> executionResults = new ArrayList<IOperationResult>();
        executionResults.add(executionResult1);
        executionResults.add(executionResult2);

        createExecutionDirectory();

        mockery.checking(new TestExpectations()
            {
                {
                    addDefaultProgressConfigExpectations();

                    allowing(executionDAO).findExecutionByCode(executionId.getPermId());
                    will(returnValue(executionPE));

                    oneOf(executionConfig).getStorePath();
                    will(returnValue(STORE_PATH));
                }
            });

        createStore().executionFinished(context, executionId, executionResults);

        assertEquals(executionPE.getState(), OperationExecutionState.FINISHED);
        assertEquals(executionPE.getSummaryResults(), executionResult1.getMessage() + "\n" + executionResult2.getMessage());
        assertTrue(timestamp <= executionPE.getFinishDate().getTime());

        assertExecutionDirectoryExistsWithFiles(true, RESULTS_FILE_NAME);

        mockery.assertIsSatisfied();
    }

    @Test(enabled=false)
    public void testExecutionFinishedWhenExecutionDirectoryDoesNotExist()
    {
        final OperationExecutionPE executionPE = new OperationExecutionPE();
        executionPE.setState(OperationExecutionState.RUNNING);
        executionPE.setOwner(context.getSession().tryGetPerson());

        final List<IOperationResult> executionResults = new ArrayList<IOperationResult>();
        executionResults.add(new TestResult(1));

        mockery.checking(new TestExpectations()
            {
                {
                    addDefaultProgressConfigExpectations();

                    allowing(executionDAO).findExecutionByCode(executionId.getPermId());
                    will(returnValue(executionPE));

                    oneOf(executionConfig).getStorePath();
                    will(returnValue(STORE_PATH));
                }
            });

        try
        {
            createStore().executionFinished(context, executionId, executionResults);
            fail();
        } catch (Exception e)
        {
            AssertionUtil.assertStarts("Couldn't write operation execution details to file ", e.getMessage());
        }
    }

    @Test(enabled=false)
    public void testExecutionFailedWhenExecutionDirectoryExists()
    {
        final OperationExecutionPE executionPE = new OperationExecutionPE();
        executionPE.setState(OperationExecutionState.RUNNING);
        executionPE.setOwner(context.getSession().tryGetPerson());

        final IOperationExecutionError executionError = new OperationExecutionError("testError");

        createExecutionDirectory();

        mockery.checking(new TestExpectations()
            {
                {
                    addDefaultProgressConfigExpectations();

                    allowing(executionDAO).findExecutionByCode(executionId.getPermId());
                    will(returnValue(executionPE));

                    oneOf(executionConfig).getStorePath();
                    will(returnValue(STORE_PATH));
                }
            });

        createStore().executionFailed(context, executionId, executionError);

        assertEquals(executionPE.getState(), OperationExecutionState.FAILED);
        assertEquals(executionPE.getSummaryError(), executionError.getMessage());
        assertTrue(timestamp <= executionPE.getFinishDate().getTime());
        assertExecutionDirectoryExistsWithFiles(true, ERROR_FILE_NAME);

        mockery.assertIsSatisfied();
    }

    @Test(enabled=false)
    public void testExecutionFailedWhenExecutionDirectoryDoesNotExist()
    {
        final OperationExecutionPE executionPE = new OperationExecutionPE();
        executionPE.setState(OperationExecutionState.RUNNING);
        executionPE.setOwner(context.getSession().tryGetPerson());

        final IOperationExecutionError executionError = new OperationExecutionError("testError");

        mockery.checking(new TestExpectations()
            {
                {
                    addDefaultProgressConfigExpectations();

                    allowing(executionDAO).findExecutionByCode(executionId.getPermId());
                    will(returnValue(executionPE));

                    oneOf(executionConfig).getStorePath();
                    will(returnValue(STORE_PATH));
                }
            });

        try
        {
            createStore().executionFailed(context, executionId, executionError);
            fail();
        } catch (Exception e)
        {
            AssertionUtil.assertStarts("Couldn't write operation execution details to file ", e.getMessage());
        }
    }

    private OperationExecutionStore createStore()
    {
        OperationExecutionFSStore fsStore = new OperationExecutionFSStore(executionConfig);
        OperationExecutionDBStore dbStore = new OperationExecutionDBStore(executionDAO);
        return new OperationExecutionStore(executionConfig, dbStore, fsStore);
    }

    private void createExecutionDirectory()
    {
        getExecutionDirectory().mkdirs();
    }

    private File getExecutionDirectory()
    {
        return new File(STORE_PATH + "/" + executionId.getPermId());
    }

    private void assertExecutionDirectoryExistsWithFiles(boolean exists, String... expectedFileNames)
    {
        File directory = new File(STORE_PATH + "/" + executionId.getPermId());
        assertEquals(directory.exists(), exists);

        String[] actualFileNames = directory.list() != null ? directory.list() : new String[] {};

        Arrays.sort(expectedFileNames);
        Arrays.sort(actualFileNames);

        assertEquals(Arrays.toString(actualFileNames), Arrays.toString(expectedFileNames));
    }

    private void assertAvailability(OperationExecutionAvailability actualAvailability, OperationExecutionAvailability expectedAvailability,
            Long actualAvailabilityTime, Integer expectedAvailabilityTime)
    {
        assertEquals(actualAvailability, expectedAvailability);
        assertEquals((Integer) actualAvailabilityTime.intValue(), expectedAvailabilityTime);
    }

    private void waitForLogMessage(String message)
    {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() < startTime + 5000)
        {
            if (logRecorder.getLogContent().contains(message))
            {
                return;
            }
        }

        fail("Waited for a log mesage:\n" + message + "\nBut the log contained only:\n" + logRecorder.getLogContent());
    }

    private class TestExpectations extends Expectations
    {

        public void addDefaultProgressConfigExpectations()
        {
            addProgressConfigExpectations(1);
        }

        public void addProgressConfigExpectations(int progressInterval)
        {
            allowing(executionConfig).getProgressThreadName();
            will(returnValue(PROGRESS_THREAD_NAME));

            allowing(executionConfig).getProgressInterval();
            will(returnValue(progressInterval));
        }

        public void addDefaultAvailabilityConfigExpectations()
        {
            oneOf(executionConfig).getAvailabilityTimeOrDefault(options.getAvailabilityTime());
            will(returnValue(options.getAvailabilityTime()));

            oneOf(executionConfig).getSummaryAvailabilityTimeOrDefault(options.getSummaryAvailabilityTime());
            will(returnValue(options.getSummaryAvailabilityTime()));

            oneOf(executionConfig).getDetailsAvailabilityTimeOrDefault(options.getDetailsAvailabilityTime());
            will(returnValue(options.getDetailsAvailabilityTime()));
        }

    }

    private static class TestResult implements IOperationResult
    {

        private static final long serialVersionUID = 1L;

        private int index;

        public TestResult(int index)
        {
            this.index = index;
        }

        @Override
        public String getMessage()
        {
            return "testResult" + index;
        }

    }

    private static class TestProgress implements IProgress
    {

        private static final long serialVersionUID = 1L;

        private int index;

        public TestProgress(int index)
        {
            this.index = index;
        }

        @Override
        public String getLabel()
        {
            return "testProgressLabel" + index;
        }

        @Override
        public String getDetails()
        {
            return "testProgressDetails" + index;
        }

        @Override
        public Integer getNumItemsProcessed()
        {
            return index;
        }

        @Override
        public Integer getTotalItemsToProcess()
        {
            return 100;
        }

    }

}
