package ch.ethz.sis.transaction;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.transaction.IDatabaseTransactionProvider;
import ch.ethz.sis.transaction.ISessionTokenProvider;
import ch.ethz.sis.transaction.ITransactionLog;
import ch.ethz.sis.transaction.ITransactionOperationExecutor;
import ch.ethz.sis.transaction.ITransactionParticipant;
import ch.ethz.sis.transaction.TransactionParticipant;
import ch.ethz.sis.transaction.TransactionStatus;

public class TransactionParticipantTest
{

    public static final String TEST_PARTICIPANT_ID = "test-participant-id";

    public static final UUID TEST_TRANSACTION_ID = UUID.randomUUID();

    public static final UUID TEST_TRANSACTION_ID_2 = UUID.randomUUID();

    public static final Object TEST_TRANSACTION = new Object();

    public static final Object TEST_TRANSACTION_2 = new Object();

    public static final String TEST_TRANSACTION_COORDINATOR_KEY = "test-transaction-coordinator-key";

    public static final String TEST_INTERACTIVE_SESSION_KEY = "test-interactive-session-key";

    public static final String TEST_SESSION_TOKEN = "test-session-token";

    public static final String INVALID_TRANSACTION_COORDINATOR_KEY = "invalid-transaction-coordinator-key";

    public static final String INVALID_INTERACTIVE_SESSION_KEY = "invalid-interactive-session-key";

    public static final String INVALID_SESSION_TOKEN = "invalid-session-token";

    public static final String TEST_OPERATION_NAME = "test-operation";

    public static final String TEST_OPERATION_NAME_2 = "test-operation-2";

    public static final Object[] TEST_OPERATION_ARGUMENTS = new Object[] { "test-argument", 1 };

    public static final Object[] TEST_OPERATION_ARGUMENTS_2 = new Object[] { "test-argument-2", 2 };

    public static final String TEST_OPERATION_RESULT = "test-result";

    public static final String TEST_OPERATION_RESULT_2 = "test-result-2";

    public static final RuntimeException TEST_UNCHECKED_EXCEPTION = new RuntimeException("Test unchecked exception");

    public static final Exception TEST_CHECKED_EXCEPTION = new Exception("Test checked exception");

    public static final Error TEST_ERROR = new Error("Test error");

    private Mockery mockery;

    private IDatabaseTransactionProvider databaseTransactionProvider;

    private ISessionTokenProvider sessionTokenProvider;

    private ITransactionOperationExecutor transactionOperationExecutor;

    private ITransactionLog transactionLog;

    @BeforeMethod
    protected void beforeMethod()
    {
        mockery = new Mockery();
        databaseTransactionProvider = mockery.mock(IDatabaseTransactionProvider.class);
        sessionTokenProvider = mockery.mock(ISessionTokenProvider.class);
        transactionOperationExecutor = mockery.mock(ITransactionOperationExecutor.class);
        transactionLog = mockery.mock(ITransactionLog.class);
    }

    @AfterMethod
    protected void afterMethod()
    {
        mockery.assertIsSatisfied();
    }

    @Test
    public void testDifferentTransactionsAreExecutedInSeparateThreads() throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        MutableObject<String> transaction1BeginThreadName = new MutableObject<>();
        MutableObject<String> transaction1PrepareThreadName = new MutableObject<>();
        MutableObject<String> transaction1CommitThreadName = new MutableObject<>();

        MutableObject<String> transaction2BeginThreadName = new MutableObject<>();
        MutableObject<String> transaction2RollbackThreadName = new MutableObject<>();

        mockery.checking(new Expectations()
        {
            {
                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                // begin 1
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(new CustomAction("beginTransaction")
                {
                    @Override public Object invoke(final Invocation invocation)
                    {
                        transaction1BeginThreadName.setValue(Thread.currentThread().getName());
                        return TEST_TRANSACTION;
                    }
                });
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // prepare 1
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_STARTED);
                one(databaseTransactionProvider).prepareTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));
                will(new CustomAction("prepareTransaction")
                {
                    @Override public Object invoke(final Invocation invocation)
                    {
                        transaction1PrepareThreadName.setValue(Thread.currentThread().getName());
                        return null;
                    }
                });
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_FINISHED);

                // execute 1
                one(transactionOperationExecutor).executeOperation(TEST_SESSION_TOKEN, TEST_OPERATION_NAME, TEST_OPERATION_ARGUMENTS);
                will(new CustomAction("executeOperation")
                {
                    @Override public Object invoke(final Invocation invocation)
                    {
                        return Thread.currentThread().getName();
                    }
                });

                // commit 1
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.COMMIT_STARTED);
                one(databaseTransactionProvider).commitTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));
                will(new CustomAction("commitTransaction")
                {
                    @Override public Object invoke(final Invocation invocation)
                    {
                        transaction1CommitThreadName.setValue(Thread.currentThread().getName());
                        return null;
                    }
                });
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.COMMIT_FINISHED);

                // begin 2
                one(transactionLog).logStatus(TEST_TRANSACTION_ID_2, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID_2));
                will(new CustomAction("beginTransaction")
                {
                    @Override public Object invoke(final Invocation invocation)
                    {
                        transaction2BeginThreadName.setValue(Thread.currentThread().getName());
                        return TEST_TRANSACTION_2;
                    }
                });
                one(transactionLog).logStatus(TEST_TRANSACTION_ID_2, TransactionStatus.BEGIN_FINISHED);

                // execute 2
                one(transactionOperationExecutor).executeOperation(TEST_SESSION_TOKEN, TEST_OPERATION_NAME_2, TEST_OPERATION_ARGUMENTS_2);
                will(new CustomAction("executeOperation")
                {
                    @Override public Object invoke(final Invocation invocation)
                    {
                        return Thread.currentThread().getName();
                    }
                });

                // rollback 2
                one(transactionLog).logStatus(TEST_TRANSACTION_ID_2, TransactionStatus.ROLLBACK_STARTED);
                one(databaseTransactionProvider).rollbackTransaction(with(TEST_TRANSACTION_ID_2), with(TEST_TRANSACTION_2));
                will(new CustomAction("rollbackTransaction")
                {
                    @Override public Object invoke(final Invocation invocation)
                    {
                        transaction2RollbackThreadName.setValue(Thread.currentThread().getName());
                        return null;
                    }
                });
                one(transactionLog).logStatus(TEST_TRANSACTION_ID_2, TransactionStatus.ROLLBACK_FINISHED);
            }
        });

        // begin 1
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        // begin 2
        participant.beginTransaction(TEST_TRANSACTION_ID_2, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        // execute 1
        String transaction1OperationThreadName =
                (String) participant.executeOperation(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_OPERATION_NAME,
                        TEST_OPERATION_ARGUMENTS);
        // execute 2
        String transaction2OperationThreadName =
                (String) participant.executeOperation(TEST_TRANSACTION_ID_2, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_OPERATION_NAME_2,
                        TEST_OPERATION_ARGUMENTS_2);
        // prepare 1
        participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_TRANSACTION_COORDINATOR_KEY);
        // commit 1
        participant.commitTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        // rollback 2
        participant.rollbackTransaction(TEST_TRANSACTION_ID_2, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);

        Set<String> transaction1ThreadNames =
                new HashSet<>(List.of(transaction1BeginThreadName.getValue(), transaction1OperationThreadName,
                        transaction1PrepareThreadName.getValue(), transaction1CommitThreadName.getValue()));
        Set<String> transaction2ThreadNames =
                new HashSet<>(
                        List.of(transaction2BeginThreadName.getValue(), transaction2OperationThreadName, transaction2RollbackThreadName.getValue()));

        assertEquals(transaction1ThreadNames.size(), 1);
        assertEquals(transaction2ThreadNames.size(), 1);

        assertFalse(transaction1ThreadNames.contains(Thread.currentThread().getName()));
        assertFalse(transaction2ThreadNames.contains(Thread.currentThread().getName()));
        assertFalse(transaction1ThreadNames.removeAll(transaction2ThreadNames));
    }

    @Test
    public void testBeginTransactionWithoutTransactionId() throws Throwable
    {
        doTestBeginTransactionWithInvalidArguments(participant ->
        {
            participant.beginTransaction(null, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
            return null;
        }, "Transaction id cannot be null");
    }

    @Test
    public void testBeginTransactionWithInvalidSessionToken() throws Throwable
    {
        doTestBeginTransactionWithInvalidArguments(participant ->
        {
            participant.beginTransaction(TEST_TRANSACTION_ID, INVALID_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
            return null;
        }, "Invalid session token");
    }

    @Test
    public void testBeginTransactionWithInvalidInteractiveSessionKey() throws Throwable
    {
        doTestBeginTransactionWithInvalidArguments(participant ->
        {
            participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, INVALID_INTERACTIVE_SESSION_KEY);
            return null;
        }, "Invalid interactive session key");
    }

    private void doTestBeginTransactionWithInvalidArguments(Function<ITransactionParticipant, Void> beginTransaction, String expectedExceptionMessage)
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                allowing(sessionTokenProvider).isValid(INVALID_SESSION_TOKEN);
                will(returnValue(false));
            }
        });

        try
        {
            // begin (fails)
            beginTransaction.apply(participant);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t.getMessage(), expectedExceptionMessage);
            assertFalse(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test(dataProvider = "provideExceptions")
    public void testBeginTransactionFails(Throwable throwable) throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                // begin (fails)
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(throwException(throwable));

                // rollback
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_STARTED);
                one(databaseTransactionProvider).rollbackTransaction(with(TEST_TRANSACTION_ID), with(aNull(Object.class)));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_FINISHED);
            }
        });

        try
        {
            // begin (fails)
            participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t.getCause(), throwable);
            assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));

            // rollback
            participant.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
            assertFalse(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test
    public void testExecuteOperationWithoutTransactionId() throws Throwable
    {
        doTestExecuteOperationWithInvalidArguments(participant ->
        {
            participant.executeOperation(null, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_OPERATION_NAME, TEST_OPERATION_ARGUMENTS);
            return null;
        }, "Transaction id cannot be null");
    }

    @Test
    public void testExecuteOperationWithInvalidSessionToken() throws Throwable
    {
        doTestExecuteOperationWithInvalidArguments(participant ->
        {
            participant.executeOperation(TEST_TRANSACTION_ID, INVALID_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_OPERATION_NAME,
                    TEST_OPERATION_ARGUMENTS);
            return null;
        }, "Invalid session token");
    }

    @Test
    public void testExecuteOperationWithInvalidInteractiveSessionKey() throws Throwable
    {
        doTestExecuteOperationWithInvalidArguments(participant ->
        {
            participant.executeOperation(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, INVALID_INTERACTIVE_SESSION_KEY, TEST_OPERATION_NAME,
                    TEST_OPERATION_ARGUMENTS);
            return null;
        }, "Invalid interactive session key");
    }

    private void doTestExecuteOperationWithInvalidArguments(Function<ITransactionParticipant, Void> executeOperation, String expectedExceptionMessage)
            throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                allowing(sessionTokenProvider).isValid(INVALID_SESSION_TOKEN);
                will(returnValue(false));

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);
            }
        });

        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);

        try
        {
            // execute (fails)
            executeOperation.apply(participant);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t.getMessage(), expectedExceptionMessage);
            assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test(dataProvider = "provideExceptions")
    public void testExecuteOperationFails(Throwable throwable) throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(TEST_TRANSACTION));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // execute
                one(transactionOperationExecutor).executeOperation(TEST_SESSION_TOKEN, TEST_OPERATION_NAME, TEST_OPERATION_ARGUMENTS);
                will(throwException(throwable));

                // rollback
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_STARTED);
                one(databaseTransactionProvider).rollbackTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_FINISHED);
            }
        });

        assertFalse(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));

        try
        {
            // execute (fails)
            participant.executeOperation(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_OPERATION_NAME,
                    TEST_OPERATION_ARGUMENTS);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t.getCause(), throwable);

            assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
            // rollback
            participant.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
            assertFalse(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test(dataProvider = "provideExceptions")
    public void testExecuteOperationFailsButGetsRetriedAndSucceeds(Throwable throwable) throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(TEST_TRANSACTION));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // execute (fails)
                one(transactionOperationExecutor).executeOperation(TEST_SESSION_TOKEN, TEST_OPERATION_NAME, TEST_OPERATION_ARGUMENTS);
                will(throwException(throwable));

                // execute
                one(transactionOperationExecutor).executeOperation(TEST_SESSION_TOKEN, TEST_OPERATION_NAME, TEST_OPERATION_ARGUMENTS);
                will(returnValue("OK"));

                // prepare
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_STARTED);
                one(databaseTransactionProvider).prepareTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_FINISHED);

                // commit
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.COMMIT_STARTED);
                one(databaseTransactionProvider).commitTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.COMMIT_FINISHED);
            }
        });

        assertFalse(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));

        try
        {
            // execute (fails)
            participant.executeOperation(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_OPERATION_NAME,
                    TEST_OPERATION_ARGUMENTS);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t.getCause(), throwable);
            assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));

            // execute
            Object result = participant.executeOperation(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_OPERATION_NAME,
                    TEST_OPERATION_ARGUMENTS);
            assertEquals(result, "OK");

            // prepare
            participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_TRANSACTION_COORDINATOR_KEY);
            assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));

            // commit
            participant.commitTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
            assertFalse(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test
    public void testPrepareTransactionWithoutTransactionId() throws Throwable
    {
        doTestPrepareTransactionWithInvalidArguments(participant ->
        {
            participant.prepareTransaction(null, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_TRANSACTION_COORDINATOR_KEY);
            return null;
        }, "Transaction id cannot be null");
    }

    @Test
    public void testPrepareTransactionWithInvalidSessionToken() throws Throwable
    {
        doTestPrepareTransactionWithInvalidArguments(participant ->
        {
            participant.prepareTransaction(TEST_TRANSACTION_ID, INVALID_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                    TEST_TRANSACTION_COORDINATOR_KEY);
            return null;
        }, "Invalid session token");
    }

    @Test
    public void testPrepareTransactionWithInvalidInteractiveSessionKey() throws Throwable
    {
        doTestPrepareTransactionWithInvalidArguments(participant ->
        {
            participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, INVALID_INTERACTIVE_SESSION_KEY,
                    TEST_TRANSACTION_COORDINATOR_KEY);
            return null;
        }, "Invalid interactive session key");
    }

    @Test
    public void testPrepareTransactionWithInvalidTransactionCoordinatorKey() throws Throwable
    {
        doTestPrepareTransactionWithInvalidArguments(participant ->
        {
            participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                    INVALID_TRANSACTION_COORDINATOR_KEY);
            return null;
        }, "Invalid transaction coordinator key");
    }

    private void doTestPrepareTransactionWithInvalidArguments(Function<ITransactionParticipant, Void> prepareTransaction,
            String expectedExceptionMessage) throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                allowing(sessionTokenProvider).isValid(INVALID_SESSION_TOKEN);
                will(returnValue(false));

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);
            }
        });

        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);

        try
        {
            // prepare (fails)
            prepareTransaction.apply(participant);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t.getMessage(), expectedExceptionMessage);
            assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test(dataProvider = "provideExceptions")
    public void testPrepareTransactionFails(Throwable throwable) throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(TEST_TRANSACTION));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // execute
                one(transactionOperationExecutor).executeOperation(TEST_SESSION_TOKEN, TEST_OPERATION_NAME, TEST_OPERATION_ARGUMENTS);

                // prepare (fails)
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_STARTED);
                one(databaseTransactionProvider).prepareTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));
                will(throwException(throwable));

                // rollback
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_STARTED);
                one(databaseTransactionProvider).rollbackTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_FINISHED);
            }
        });

        assertFalse(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        // execute
        participant.executeOperation(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_OPERATION_NAME,
                TEST_OPERATION_ARGUMENTS);

        try
        {
            // prepare (fails)
            participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_TRANSACTION_COORDINATOR_KEY);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t.getCause(), throwable);

            assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
            // rollback
            participant.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
            assertFalse(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test
    public void testCommitTransactionWithoutTransactionId() throws Throwable
    {
        doTestCommitTransactionWithInvalidArguments(participant ->
        {
            participant.commitTransaction(null, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
            return null;
        }, "Transaction id cannot be null");
    }

    @Test
    public void testCommitTransactionWithInvalidSessionToken() throws Throwable
    {
        doTestCommitTransactionWithInvalidArguments(participant ->
        {
            participant.commitTransaction(TEST_TRANSACTION_ID, INVALID_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
            return null;
        }, "Invalid session token");
    }

    @Test
    public void testCommitTransactionWithInvalidInteractiveSessionKey() throws Throwable
    {
        doTestCommitTransactionWithInvalidArguments(participant ->
        {
            participant.commitTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, INVALID_INTERACTIVE_SESSION_KEY);
            return null;
        }, "Invalid interactive session key");
    }

    @Test
    public void testCommitTransactionRecoveryWithoutTransactionId() throws Throwable
    {
        doTestCommitTransactionWithInvalidArguments(participant ->
        {
            participant.commitTransaction(null, INVALID_TRANSACTION_COORDINATOR_KEY);
            return null;
        }, "Transaction id cannot be null");
    }

    @Test
    public void testCommitTransactionRecoveryWithInvalidTransactionCoordinatorKey() throws Throwable
    {
        doTestCommitTransactionWithInvalidArguments(participant ->
        {
            participant.commitTransaction(TEST_TRANSACTION_ID, INVALID_TRANSACTION_COORDINATOR_KEY);
            return null;
        }, "Invalid transaction coordinator key");
    }

    private void doTestCommitTransactionWithInvalidArguments(Function<ITransactionParticipant, Void> commitTransaction,
            String expectedExceptionMessage)
            throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                allowing(sessionTokenProvider).isValid(INVALID_SESSION_TOKEN);
                will(returnValue(false));

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);
            }
        });

        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);

        try
        {
            commitTransaction.apply(participant);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t.getMessage(), expectedExceptionMessage);
        }
    }

    @Test(dataProvider = "provideExceptions")
    public void testCommitTransactionFails(Throwable throwable) throws Throwable
    {
        doTestCommitTransactionFails(throwable, participant ->
        {
            participant.commitTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
            return null;
        });
    }

    @Test(dataProvider = "provideExceptions")
    public void testCommitTransactionRecoveryFails(Throwable throwable) throws Throwable
    {
        doTestCommitTransactionFails(throwable, participant ->
        {
            participant.commitTransaction(TEST_TRANSACTION_ID, TEST_TRANSACTION_COORDINATOR_KEY);
            return null;
        });
    }

    private void doTestCommitTransactionFails(Throwable throwable, Function<ITransactionParticipant, Void> commitTransaction) throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(TEST_TRANSACTION));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // execute
                one(transactionOperationExecutor).executeOperation(TEST_SESSION_TOKEN, TEST_OPERATION_NAME, TEST_OPERATION_ARGUMENTS);

                // prepare
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_STARTED);
                one(databaseTransactionProvider).prepareTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_FINISHED);

                // commit (fails)
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.COMMIT_STARTED);
                one(databaseTransactionProvider).commitTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));
                will(throwException(throwable));

                // rollback
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_STARTED);
                one(databaseTransactionProvider).rollbackTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_FINISHED);
            }
        });

        assertFalse(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        // execute
        participant.executeOperation(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_OPERATION_NAME,
                TEST_OPERATION_ARGUMENTS);
        assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        // prepare
        participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_TRANSACTION_COORDINATOR_KEY);
        assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));

        try
        {
            // commit (fails)
            commitTransaction.apply(participant);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t.getCause(), throwable);

            assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
            // rollback
            participant.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
            assertFalse(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test
    public void testRollbackTransactionWithoutTransactionId() throws Throwable
    {
        doTestRollbackTransactionWithInvalidArguments(participant ->
        {
            participant.rollbackTransaction(null, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
            return null;
        }, "Transaction id cannot be null");
    }

    @Test
    public void testRollbackTransactionWithInvalidSessionToken() throws Throwable
    {
        doTestRollbackTransactionWithInvalidArguments(
                participant ->
                {
                    participant.rollbackTransaction(TEST_TRANSACTION_ID, INVALID_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
                    return null;
                },
                "Invalid session token");
    }

    @Test
    public void testRollbackTransactionWithInvalidInteractiveSessionKey() throws Throwable
    {
        doTestRollbackTransactionWithInvalidArguments(
                participant ->
                {
                    participant.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, INVALID_INTERACTIVE_SESSION_KEY);
                    return null;
                },
                "Invalid interactive session key");
    }

    @Test
    public void testRollbackTransactionRecoveryWithoutTransactionId() throws Throwable
    {
        doTestRollbackTransactionWithInvalidArguments(participant ->
        {
            participant.rollbackTransaction(null, INVALID_TRANSACTION_COORDINATOR_KEY);
            return null;
        }, "Transaction id cannot be null");
    }

    @Test
    public void testRollbackTransactionRecoveryWithInvalidTransactionCoordinatorKey() throws Throwable
    {
        doTestRollbackTransactionWithInvalidArguments(
                participant ->
                {
                    participant.rollbackTransaction(TEST_TRANSACTION_ID, INVALID_TRANSACTION_COORDINATOR_KEY);
                    return null;
                },
                "Invalid transaction coordinator key");
    }

    private void doTestRollbackTransactionWithInvalidArguments(Function<ITransactionParticipant, Void> rollbackTransaction,
            String expectedExceptionMessage)
            throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                allowing(sessionTokenProvider).isValid(INVALID_SESSION_TOKEN);
                will(returnValue(false));

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);
            }
        });

        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);

        try
        {
            // rollback (fails)
            rollbackTransaction.apply(participant);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t.getMessage(), expectedExceptionMessage);
            assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test(dataProvider = "provideExceptions")
    public void testRollbackTransactionFails(Throwable throwable) throws Throwable
    {
        doTestRollbackTransactionFails(throwable, participant ->
        {
            participant.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
            return null;
        });
    }

    @Test(dataProvider = "provideExceptions")
    public void testRollbackTransactionRecoveryFails(Throwable throwable) throws Throwable
    {
        doTestRollbackTransactionFails(throwable, participant ->
        {
            participant.rollbackTransaction(TEST_TRANSACTION_ID, TEST_TRANSACTION_COORDINATOR_KEY);
            return null;
        });
    }

    private void doTestRollbackTransactionFails(Throwable throwable, Function<ITransactionParticipant, Void> rollbackTransaction) throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(TEST_TRANSACTION));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // execute
                one(transactionOperationExecutor).executeOperation(TEST_SESSION_TOKEN, TEST_OPERATION_NAME, TEST_OPERATION_ARGUMENTS);

                // rollback (fails)
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_STARTED);
                one(databaseTransactionProvider).rollbackTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));
                will(throwException(throwable));
            }
        });

        assertFalse(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        // execute
        participant.executeOperation(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_OPERATION_NAME,
                TEST_OPERATION_ARGUMENTS);
        assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));

        try
        {
            // rollback (fails)
            rollbackTransaction.apply(participant);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t.getCause(), throwable);
            assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test
    public void testNewTransactionCanBeStarted() throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);
            }
        });

        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
    }

    @Test
    public void testNewTransactionCannotExecuteOperations()
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));
            }
        });

        try
        {
            participant.executeOperation(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_OPERATION_NAME,
                    TEST_OPERATION_ARGUMENTS);
            Assert.fail();
        } catch (Exception e)
        {
            assertEquals(e.getCause().getMessage(),
                    "Transaction '" + TEST_TRANSACTION_ID + "' unexpected status 'NEW'. Expected statuses '[BEGIN_FINISHED]'.");
        }
    }

    @Test
    public void testNewTransactionCannotBePrepared()
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));
            }
        });

        try
        {
            participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_TRANSACTION_COORDINATOR_KEY);
            Assert.fail();
        } catch (Exception e)
        {
            assertEquals(e.getCause().getMessage(),
                    "Transaction '" + TEST_TRANSACTION_ID + "' unexpected status 'NEW'. Expected statuses '[BEGIN_FINISHED]'.");
        }
    }

    @Test
    public void testNewTransactionCanBeCommitted()
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));
            }
        });

        // the call is possible and does nothing (used in recovery process)
        participant.commitTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
    }

    @Test
    public void testNewTransactionCanBeRolledBack()
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));
            }
        });

        // the call is possible and does nothing (used in recovery process)
        participant.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
    }

    @Test
    public void testStartedTransactionCannotBeStarted() throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);
            }
        });

        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);

        try
        {
            // repeated begin (fails)
            participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
            Assert.fail();
        } catch (Exception e)
        {
            assertEquals(e.getCause().getMessage(),
                    "Transaction '" + TEST_TRANSACTION_ID + "' unexpected status 'BEGIN_FINISHED'. Expected statuses '[NEW]'.");
        }
    }

    @Test
    public void testStartedTransactionCanExecuteOperations() throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // execute 1
                one(transactionOperationExecutor).executeOperation(TEST_SESSION_TOKEN, TEST_OPERATION_NAME, TEST_OPERATION_ARGUMENTS);
                will(returnValue(TEST_OPERATION_RESULT));

                // execute 2
                one(transactionOperationExecutor).executeOperation(TEST_SESSION_TOKEN, TEST_OPERATION_NAME_2, TEST_OPERATION_ARGUMENTS_2);
                will(returnValue(TEST_OPERATION_RESULT_2));
            }
        });

        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);

        // execute 1
        Object result = participant.executeOperation(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_OPERATION_NAME,
                TEST_OPERATION_ARGUMENTS);
        assertEquals(result, TEST_OPERATION_RESULT);

        // execute 2
        Object result2 = participant.executeOperation(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_OPERATION_NAME_2,
                TEST_OPERATION_ARGUMENTS_2);
        assertEquals(result2, TEST_OPERATION_RESULT_2);
    }

    @Test
    public void testStartedTransactionCanPrepare() throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // prepare
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_STARTED);
                one(databaseTransactionProvider).prepareTransaction(with(TEST_TRANSACTION_ID), with(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_FINISHED);
            }
        });

        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        // prepare
        participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_TRANSACTION_COORDINATOR_KEY);
    }

    @Test
    public void testStartedTransactionCanBeRolledBack() throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // rollback
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_STARTED);
                one(databaseTransactionProvider).rollbackTransaction(with(TEST_TRANSACTION_ID), with(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_FINISHED);
            }
        });

        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        // rollback
        participant.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
    }

    @Test
    public void testStartedTransactionCannotBeCommitted() throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);
            }
        });

        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);

        try
        {
            // commit (fails)
            participant.commitTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
            Assert.fail();
        } catch (Exception e)
        {
            assertEquals(e.getCause().getMessage(),
                    "Transaction '" + TEST_TRANSACTION_ID
                            + "' unexpected status 'BEGIN_FINISHED'. Expected statuses '[NEW, PREPARE_FINISHED]'.");
        }
    }

    @Test
    public void testPreparedTransactionCannotBeStarted() throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // prepare
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_STARTED);
                one(databaseTransactionProvider).prepareTransaction(with(TEST_TRANSACTION_ID), with(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_FINISHED);
            }
        });

        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        // prepare
        participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_TRANSACTION_COORDINATOR_KEY);
        try
        {
            // repeated begin (fails)
            participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
            Assert.fail();
        } catch (Exception e)
        {
            assertEquals(e.getCause().getMessage(),
                    "Transaction '" + TEST_TRANSACTION_ID + "' unexpected status 'PREPARE_FINISHED'. Expected statuses '[NEW]'.");
        }
    }

    @Test
    public void testPreparedTransactionCannotBePrepared() throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // prepare
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_STARTED);
                one(databaseTransactionProvider).prepareTransaction(with(TEST_TRANSACTION_ID), with(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_FINISHED);
            }
        });

        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        // prepare
        participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_TRANSACTION_COORDINATOR_KEY);
        try
        {
            // repeated prepare (fails)
            participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_TRANSACTION_COORDINATOR_KEY);
            Assert.fail();
        } catch (Exception e)
        {
            assertEquals(e.getCause().getMessage(),
                    "Transaction '" + TEST_TRANSACTION_ID
                            + "' unexpected status 'PREPARE_FINISHED'. Expected statuses '[BEGIN_FINISHED]'.");
        }
    }

    @Test
    public void testPreparedTransactionCannotExecuteOperations() throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // prepare
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_STARTED);
                one(databaseTransactionProvider).prepareTransaction(with(TEST_TRANSACTION_ID), with(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_FINISHED);
            }
        });

        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        // prepare
        participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_TRANSACTION_COORDINATOR_KEY);
        try
        {
            // execute (fails)
            participant.executeOperation(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_OPERATION_NAME,
                    TEST_OPERATION_ARGUMENTS);
            Assert.fail();
        } catch (Exception e)
        {
            assertEquals(e.getCause().getMessage(),
                    "Transaction '" + TEST_TRANSACTION_ID
                            + "' unexpected status 'PREPARE_FINISHED'. Expected statuses '[BEGIN_FINISHED]'.");
        }
    }

    @Test
    public void testPreparedTransactionCanBeRolledBack() throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // prepare
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_STARTED);
                one(databaseTransactionProvider).prepareTransaction(with(TEST_TRANSACTION_ID), with(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_FINISHED);

                // rollback
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_STARTED);
                one(databaseTransactionProvider).rollbackTransaction(with(TEST_TRANSACTION_ID), with(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_FINISHED);
            }
        });

        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        // prepare
        participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_TRANSACTION_COORDINATOR_KEY);
        // rollback
        participant.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
    }

    @Test
    public void testPreparedTransactionCanCommit() throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // prepare
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_STARTED);
                one(databaseTransactionProvider).prepareTransaction(with(TEST_TRANSACTION_ID), with(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_FINISHED);

                // commit
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.COMMIT_STARTED);
                one(databaseTransactionProvider).commitTransaction(with(TEST_TRANSACTION_ID), with(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.COMMIT_FINISHED);
            }
        });

        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        // prepare
        participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_TRANSACTION_COORDINATOR_KEY);
        // commit
        participant.commitTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
    }

    @Test
    public void testCommittedTransactionIsForgotten() throws Throwable
    {
        TransactionParticipant participant =
                new TransactionParticipant(TEST_PARTICIPANT_ID, TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        databaseTransactionProvider, transactionOperationExecutor, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // prepare
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_STARTED);
                one(databaseTransactionProvider).prepareTransaction(with(TEST_TRANSACTION_ID), with(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_FINISHED);

                // commit
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.COMMIT_STARTED);
                one(databaseTransactionProvider).commitTransaction(with(TEST_TRANSACTION_ID), with(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.COMMIT_FINISHED);

                // another begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);
            }
        });

        assertFalse(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        // prepare
        participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_TRANSACTION_COORDINATOR_KEY);
        assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        // commit
        participant.commitTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        assertFalse(participant.isRunningTransaction(TEST_TRANSACTION_ID));

        // another begin - this is treated as a new transaction as the previous transaction with the same id has been already committed and therefore forgotten
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
    }

    @DataProvider
    protected Object[][] provideExceptions()
    {
        return new Object[][]
                {
                        { TEST_UNCHECKED_EXCEPTION },
                        { TEST_CHECKED_EXCEPTION },
                        { TEST_ERROR },
                };
    }

}

