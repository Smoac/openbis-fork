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

    @BeforeMethod
    protected void beforeMethod()
    {
        mockery = new Mockery();
        databaseTransactionProvider = mockery.mock(IDatabaseTransactionProvider.class);
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
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

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
                    @Override public Object invoke(final Invocation invocation) throws Throwable
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
                    @Override public Object invoke(final Invocation invocation) throws Throwable
                    {
                        transaction1PrepareThreadName.setValue(Thread.currentThread().getName());
                        return null;
                    }
                });
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_FINISHED);

                // commit 1
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.COMMIT_STARTED);
                one(databaseTransactionProvider).commitTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));
                will(new CustomAction("commitTransaction")
                {
                    @Override public Object invoke(final Invocation invocation) throws Throwable
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
                    @Override public Object invoke(final Invocation invocation) throws Throwable
                    {
                        transaction2BeginThreadName.setValue(Thread.currentThread().getName());
                        return TEST_TRANSACTION_2;
                    }
                });
                one(transactionLog).logStatus(TEST_TRANSACTION_ID_2, TransactionStatus.BEGIN_FINISHED);

                // rollback 2
                one(transactionLog).logStatus(TEST_TRANSACTION_ID_2, TransactionStatus.ROLLBACK_STARTED);
                one(databaseTransactionProvider).rollbackTransaction(with(TEST_TRANSACTION_ID_2), with(TEST_TRANSACTION_2));
                will(new CustomAction("rollbackTransaction")
                {
                    @Override public Object invoke(final Invocation invocation) throws Throwable
                    {
                        transaction2RollbackThreadName.setValue(Thread.currentThread().getName());
                        return null;
                    }
                });
                one(transactionLog).logStatus(TEST_TRANSACTION_ID_2, TransactionStatus.ROLLBACK_FINISHED);
            }
        });

        ITransactionOperation testOperation = new ITransactionOperation()
        {
            @Override public String getOperationName()
            {
                return TEST_OPERATION_NAME;
            }

            @Override public Object executeOperation() throws Throwable
            {
                return Thread.currentThread().getName();
            }
        };

        // begin 1
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // begin 2
        executor.beginTransaction(TEST_TRANSACTION_ID_2, TEST_SECRET);

        String transaction1OperationThreadName = (String) executor.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, testOperation);
        String transaction2OperationThreadName = (String) executor.executeOperation(TEST_TRANSACTION_ID_2, TEST_SECRET, testOperation);

        // prepare 1
        executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // commit 1
        executor.commitTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // rollback 2
        executor.rollbackTransaction(TEST_TRANSACTION_ID_2, TEST_SECRET);

        Set<String> transaction1ThreadNames =
                new HashSet<String>(List.of(transaction1BeginThreadName.getValue(), transaction1OperationThreadName,
                        transaction1PrepareThreadName.getValue(), transaction1CommitThreadName.getValue()));
        Set<String> transaction2ThreadNames =
                new HashSet<String>(
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
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

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
            executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t, throwable);
            assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));

            // rollback
            executor.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test(dataProvider = "provideExceptions")
    public void testExecuteOperationFails(Throwable throwable) throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

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

        assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        // begin
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));

        try
        {
            executor.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, new ITransactionOperation()
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

            assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));
            // rollback
            executor.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test(dataProvider = "provideExceptions")
    public void testExecuteOperationFailsButGetsRetriedAndSucceeds(Throwable throwable) throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

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

        assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        // begin
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));

        try
        {
            executor.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, new ITransactionOperation()
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

            assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));
            executor.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, new ITransactionOperation()
            {
                @Override public String getOperationName()
                {
                    return TEST_OPERATION_NAME;
                }

                @Override public Object executeOperation() throws Throwable
                {
                    return "OK";
                }
            });

            // prepare
            executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));

            // commit
            executor.commitTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test(dataProvider = "provideExceptions")
    public void testRollbackFails(Throwable throwable) throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(TEST_TRANSACTION));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

                // rollback (fails)
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_STARTED);
                one(databaseTransactionProvider).rollbackTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));
                will(throwException(throwable));
            }
        });

        assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        // begin
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        executor.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TEST_OPERATION_NAME, TEST_RESULT));
        assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));

        try
        {
            // rollback (fails)
            executor.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t, throwable);
            assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test(dataProvider = "provideExceptions")
    public void testPrepareFails(Throwable throwable) throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                // begin
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);
                one(databaseTransactionProvider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(TEST_TRANSACTION));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED);

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

        assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        // begin
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        executor.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TEST_OPERATION_NAME, TEST_RESULT));

        try
        {
            // prepare (fails)
            executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t, throwable);

            assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));
            // rollback
            executor.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test(dataProvider = "provideExceptions")
    public void testCommitFails(Throwable throwable) throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

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

        assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        // begin
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        executor.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TEST_OPERATION_NAME, TEST_RESULT));
        assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        // prepare
        executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));

        try
        {
            // commit (fails)
            executor.commitTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t, throwable);

            assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));
            // rollback
            executor.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test
    public void testNewTransactionCanBeStarted() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

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
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
    }

    @Test
    public void testNewTransactionCannotExecuteOperations() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        try
        {
            executor.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TEST_OPERATION_NAME, TEST_RESULT));
            Assert.fail();
        } catch (IllegalStateException e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status null. Expected statuses [BEGIN_FINISHED].");
        }
    }

    @Test
    public void testNewTransactionCannotBePrepared() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        try
        {
            executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (IllegalStateException e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status null. Expected statuses [BEGIN_FINISHED].");
        }
    }

    @Test
    public void testNewTransactionCannotBeCommitted() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        try
        {
            executor.commitTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (IllegalStateException e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status null. Expected statuses [PREPARE_FINISHED].");
        }
    }

    @Test
    public void testNewTransactionCanBeRolledBack() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

        mockery.checking(new Expectations()
        {
            {
                // rollback
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_STARTED);
                one(databaseTransactionProvider).rollbackTransaction(with(TEST_TRANSACTION_ID), with(aNull(Object.class)));
                one(transactionLog).logStatus(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_FINISHED);
            }
        });

        // rollback
        executor.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
    }

    @Test
    public void testStartedTransactionCannotBeStarted() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

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
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);

        try
        {
            // repeated begin (fails)
            executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status BEGIN_FINISHED. Expected statuses [null].");
        }
    }

    @Test
    public void testStartedTransactionCanExecuteOperations() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

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
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);

        Object result = executor.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TEST_OPERATION_NAME, TEST_RESULT));
        assertEquals(result, TEST_RESULT);

        Object result2 = executor.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TEST_OPERATION_NAME_2, TEST_RESULT_2));
        assertEquals(result2, TEST_RESULT_2);
    }

    @Test
    public void testStartedTransactionCanPrepare() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

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
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // prepare
        executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
    }

    @Test
    public void testStartedTransactionCanBeRolledBack() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

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
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // rollback
        executor.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
    }

    @Test
    public void testStartedTransactionCannotBeCommitted() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

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
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);

        try
        {
            // commit (fails)
            executor.commitTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (IllegalStateException e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status BEGIN_FINISHED. Expected statuses [PREPARE_FINISHED].");
        }
    }

    @Test
    public void testPreparedTransactionCannotBeStarted() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

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
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // prepare
        executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        try
        {
            // repeated begin (fails)
            executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status PREPARE_FINISHED. Expected statuses [null].");
        }
    }

    @Test
    public void testPreparedTransactionCannotBePrepared() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

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
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // prepare
        executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        try
        {
            // repeated prepare (fails)
            executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status PREPARE_FINISHED. Expected statuses [BEGIN_FINISHED].");
        }
    }

    @Test
    public void testPreparedTransactionCannotExecuteOperations() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

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
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // prepare
        executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        try
        {
            // execute (fails)
            executor.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TEST_OPERATION_NAME));
            Assert.fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status PREPARE_FINISHED. Expected statuses [BEGIN_FINISHED].");
        }
    }

    @Test
    public void testPreparedTransactionCanBeRolledBack() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

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
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // prepare
        executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // rollback
        executor.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
    }

    @Test
    public void testPreparedTransactionCanCommit() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

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
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // prepare
        executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        // commit
        executor.commitTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
    }

    @Test
    public void testCommittedTransactionIsForgotten() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(databaseTransactionProvider, transactionLog);

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

        assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        // begin
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        // prepare
        executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        // commit
        executor.commitTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));

        // another begin - this is treated as a new transaction as the previous transaction with the same id has been already committed and therefore forgotten
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));
    }

    private static class TestOperation implements ITransactionOperation
    {
        private final String name;

        private Object result;

        private Throwable exception;

        public TestOperation(String name)
        {
            this.name = name;
        }

        public TestOperation(String name, Object result)
        {
            this.name = name;
            this.result = result;
        }

        public TestOperation(String name, Throwable exception)
        {
            this.name = name;
            this.exception = exception;
        }

        @Override public String getOperationName()
        {
            return name;
        }

        @Override public Object executeOperation() throws Throwable
        {
            if (exception != null)
            {
                throw exception;
            } else
            {
                return result;
            }
        }
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

