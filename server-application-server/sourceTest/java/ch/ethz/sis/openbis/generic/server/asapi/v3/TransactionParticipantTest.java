package ch.ethz.sis.openbis.generic.server.asapi.v3;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class TransactionParticipantTest
{

    public static final String TEST_TRANSACTION_ID = "test-id";

    public static final String TEST_TRANSACTION_ID_2 = "test-id-2";

    public static final Object TEST_TRANSACTION = new Object();

    public static final Object TEST_TRANSACTION_2 = new Object();

    public static final String TEST_SECRET = "i_am_secret";

    public static final String TEST_OPERATION_NAME = "test-operation";

    public static final String TEST_OPERATION_NAME_2 = "test-operation-2";

    public static final String TEST_RESULT = "test-result";

    public static final String TEST_RESULT_2 = "test-result-2";

    public static final RuntimeException TEST_UNCHECKED_EXCEPTION = new RuntimeException("Test unchecked exception");

    public static final Exception TEST_CHECKED_EXCEPTION = new Exception("Test checked exception");

    public static final Error TEST_ERROR = new Error("Test error");

    private Mockery mockery;

    private IDatabaseTransactionProvider databaseTransactionProvider;

    private ITransactionLog transactionLog;

    private ITransactionParticipantOperation transactionOperation;

    private ITransactionParticipantOperation transactionOperation2;

    @BeforeMethod
    protected void beforeMethod()
    {
        mockery = new Mockery();
        databaseTransactionProvider = mockery.mock(IDatabaseTransactionProvider.class);
        transactionLog = mockery.mock(ITransactionLog.class);
        transactionOperation = mockery.mock(ITransactionParticipantOperation.class, "transactionOperation");
        transactionOperation2 = mockery.mock(ITransactionParticipantOperation.class, "transactionOperation2");
    }

    @AfterMethod
    protected void afterMethod()
    {
        mockery.assertIsSatisfied();
    }

    @Test
    public void testDifferentTransactionsAreExecutedInSeparateThreads() throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        MutableObject<String> transaction1BeginThreadName = new MutableObject<>();
        MutableObject<String> transaction1PrepareThreadName = new MutableObject<>();
        MutableObject<String> transaction1CommitThreadName = new MutableObject<>();

        MutableObject<String> transaction2BeginThreadName = new MutableObject<>();
        MutableObject<String> transaction2RollbackThreadName = new MutableObject<>();

        mockery.checking(new Expectations()
        {
            {
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
                allowing(transactionOperation).getOperationName();
                will(returnValue(TEST_OPERATION_NAME));
                one(transactionOperation).executeOperation();
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
                allowing(transactionOperation2).getOperationName();
                will(returnValue(TEST_OPERATION_NAME_2));
                one(transactionOperation2).executeOperation();
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
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // begin 2
        participant.beginTransaction(TEST_TRANSACTION_ID_2, TEST_SECRET);
        // execute 1
        String transaction1OperationThreadName = (String) participant.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, transactionOperation);
        // execute 2
        String transaction2OperationThreadName = (String) participant.executeOperation(TEST_TRANSACTION_ID_2, TEST_SECRET, transactionOperation2);
        // prepare 1
        participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // commit 1
        participant.commitTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // rollback 2
        participant.rollbackTransaction(TEST_TRANSACTION_ID_2, TEST_SECRET);

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

    @Test(dataProvider = "provideExceptions")
    public void testBeginTransactionFails(Throwable throwable) throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
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
            participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t, throwable);
            assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));

            // rollback
            participant.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            assertFalse(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test(dataProvider = "provideExceptions")
    public void testExecuteOperationFails(Throwable throwable) throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(TEST_TRANSACTION));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // rollback
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_STARTED);
                one(databaseTransactionProvider).rollbackTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_FINISHED);
            }
        });

        assertFalse(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));

        try
        {
            participant.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, new ITransactionParticipantOperation()
            {
                @Override public String getOperationName()
                {
                    return TEST_OPERATION_NAME;
                }

                @Override public Object executeOperation() throws Throwable
                {
                    throw throwable;
                }
            });
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t, throwable);

            assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
            // rollback
            participant.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            assertFalse(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test(dataProvider = "provideExceptions")
    public void testExecuteOperationFailsButGetsRetriedAndSucceeds(Throwable throwable) throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(TEST_TRANSACTION));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

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
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));

        try
        {
            participant.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, new ITransactionParticipantOperation()
            {
                @Override public String getOperationName()
                {
                    return TEST_OPERATION_NAME;
                }

                @Override public Object executeOperation() throws Throwable
                {
                    throw throwable;
                }
            });
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t, throwable);

            assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
            participant.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, new ITransactionParticipantOperation()
            {
                @Override public String getOperationName()
                {
                    return TEST_OPERATION_NAME;
                }

                @Override public Object executeOperation()
                {
                    return "OK";
                }
            });

            // prepare
            participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));

            // commit
            participant.commitTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            assertFalse(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test(dataProvider = "provideExceptions")
    public void testRollbackFails(Throwable throwable) throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(TEST_TRANSACTION));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // execute
                allowing(transactionOperation).getOperationName();
                will(returnValue(TEST_OPERATION_NAME));
                one(transactionOperation).executeOperation();
                will(returnValue(TEST_RESULT));

                // rollback (fails)
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_STARTED);
                one(databaseTransactionProvider).rollbackTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));
                will(throwException(throwable));
            }
        });

        assertFalse(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        // execute
        participant.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, transactionOperation);
        assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));

        try
        {
            // rollback (fails)
            participant.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t, throwable);
            assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test(dataProvider = "provideExceptions")
    public void testPrepareFails(Throwable throwable) throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(TEST_TRANSACTION));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // execute
                allowing(transactionOperation).getOperationName();
                will(returnValue(TEST_OPERATION_NAME));
                one(transactionOperation).executeOperation();
                will(returnValue(TEST_RESULT));

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
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        // execute
        participant.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, transactionOperation);

        try
        {
            // prepare (fails)
            participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t, throwable);

            assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
            // rollback
            participant.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            assertFalse(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test(dataProvider = "provideExceptions")
    public void testCommitFails(Throwable throwable) throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(TEST_TRANSACTION));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // execute
                allowing(transactionOperation).getOperationName();
                will(returnValue(TEST_OPERATION_NAME));
                one(transactionOperation).executeOperation();
                will(returnValue(TEST_RESULT));

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
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        // execute
        participant.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, transactionOperation);
        assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        // prepare
        participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));

        try
        {
            // commit (fails)
            participant.commitTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t, throwable);

            assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
            // rollback
            participant.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            assertFalse(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test
    public void testNewTransactionCanBeStarted() throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);
            }
        });

        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
    }

    @Test
    public void testNewTransactionCannotExecuteOperations() throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                // execute (fails)
                allowing(transactionOperation).getOperationName();
                will(returnValue(TEST_OPERATION_NAME));
            }
        });

        try
        {
            // execute (fails)
            participant.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, transactionOperation);
            Assert.fail();
        } catch (IllegalStateException e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status NEW. Expected statuses [BEGIN_FINISHED].");
        }
    }

    @Test
    public void testNewTransactionCannotBePrepared() throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        try
        {
            participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (IllegalStateException e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status NEW. Expected statuses [BEGIN_FINISHED].");
        }
    }

    @Test
    public void testNewTransactionCanBeCommitted() throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);
        // the call is possible and does nothing (used in recovery process)
        participant.commitTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
    }

    @Test
    public void testNewTransactionCanBeRolledBack() throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);
        // the call is possible and does nothing (used in recovery process)
        participant.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
    }

    @Test
    public void testStartedTransactionCannotBeStarted() throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);
            }
        });

        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);

        try
        {
            // repeated begin (fails)
            participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status BEGIN_FINISHED. Expected statuses [NEW].");
        }
    }

    @Test
    public void testStartedTransactionCanExecuteOperations() throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // execute 1
                allowing(transactionOperation).getOperationName();
                will(returnValue(TEST_OPERATION_NAME));
                one(transactionOperation).executeOperation();
                will(returnValue(TEST_RESULT));

                // execute 2
                allowing(transactionOperation2).getOperationName();
                will(returnValue(TEST_OPERATION_NAME_2));
                one(transactionOperation2).executeOperation();
                will(returnValue(TEST_RESULT_2));
            }
        });

        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);

        // execute 1
        Object result = participant.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, transactionOperation);
        assertEquals(result, TEST_RESULT);

        // execute 2
        Object result2 = participant.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, transactionOperation2);
        assertEquals(result2, TEST_RESULT_2);
    }

    @Test
    public void testStartedTransactionCanPrepare() throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

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
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // prepare
        participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
    }

    @Test
    public void testStartedTransactionCanBeRolledBack() throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

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
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // rollback
        participant.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
    }

    @Test
    public void testStartedTransactionCannotBeCommitted() throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);
            }
        });

        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);

        try
        {
            // commit (fails)
            participant.commitTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (IllegalStateException e)
        {
            assertEquals(e.getMessage(),
                    "Two phase transaction test-id unexpected status BEGIN_FINISHED. Expected statuses [NEW, PREPARE_FINISHED].");
        }
    }

    @Test
    public void testPreparedTransactionCannotBeStarted() throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

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
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // prepare
        participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        try
        {
            // repeated begin (fails)
            participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status PREPARE_FINISHED. Expected statuses [NEW].");
        }
    }

    @Test
    public void testPreparedTransactionCannotBePrepared() throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

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
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // prepare
        participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        try
        {
            // repeated prepare (fails)
            participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status PREPARE_FINISHED. Expected statuses [BEGIN_FINISHED].");
        }
    }

    @Test
    public void testPreparedTransactionCannotExecuteOperations() throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // prepare
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_STARTED);
                one(databaseTransactionProvider).prepareTransaction(with(TEST_TRANSACTION_ID), with(transaction));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_FINISHED);

                // execute
                allowing(transactionOperation).getOperationName();
                will(returnValue(TEST_OPERATION_NAME));
            }
        });

        // begin
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // prepare
        participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        try
        {
            // execute (fails)
            participant.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, transactionOperation);
            Assert.fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status PREPARE_FINISHED. Expected statuses [BEGIN_FINISHED].");
        }
    }

    @Test
    public void testPreparedTransactionCanBeRolledBack() throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

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
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // prepare
        participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // rollback
        participant.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
    }

    @Test
    public void testPreparedTransactionCanCommit() throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

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
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // prepare
        participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // commit
        participant.commitTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
    }

    @Test
    public void testCommittedTransactionIsForgotten() throws Throwable
    {
        TransactionParticipant participant = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

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
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        // prepare
        participant.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(participant.isRunningTransaction(TEST_TRANSACTION_ID));
        // commit
        participant.commitTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertFalse(participant.isRunningTransaction(TEST_TRANSACTION_ID));

        // another begin - this is treated as a new transaction as the previous transaction with the same id has been already committed and therefore forgotten
        participant.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
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

