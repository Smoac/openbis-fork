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

    public static final String TEST_SECRET = "test-secret";

    public static final String TEST_OPERATION_NAME = "test-operation";

    public static final String TEST_OPERATION_NAME_2 = "test-operation-2";

    public static final String TEST_RESULT = "test-result";

    public static final String TEST_RESULT_2 = "test-result-2";

    public static final RuntimeException TEST_UNCHECKED_EXCEPTION = new RuntimeException("Test unchecked exception");

    public static final Exception TEST_CHECKED_EXCEPTION = new Exception("Test checked exception");

    public static final Error TEST_ERROR = new Error("Test error");

    private Mockery mockery;

    private ITransactionProvider provider;

    @BeforeMethod
    protected void beforeMethod()
    {
        mockery = new Mockery();
        provider = mockery.mock(ITransactionProvider.class);
    }

    @AfterMethod
    protected void afterMethod()
    {
        mockery.assertIsSatisfied();
    }

    @Test
    public void testDifferentTransactionsAreExecutedInSeparateThreads() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(provider);

        MutableObject<String> transaction1BeginThreadName = new MutableObject<>();
        MutableObject<String> transaction1PrepareThreadName = new MutableObject<>();
        MutableObject<String> transaction1CommitThreadName = new MutableObject<>();

        MutableObject<String> transaction2BeginThreadName = new MutableObject<>();
        MutableObject<String> transaction2RollbackThreadName = new MutableObject<>();

        mockery.checking(new Expectations()
        {
            {
                one(provider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(new CustomAction("beginTransaction")
                {
                    @Override public Object invoke(final Invocation invocation) throws Throwable
                    {
                        transaction1BeginThreadName.setValue(Thread.currentThread().getName());
                        return TEST_TRANSACTION;
                    }
                });

                one(provider).prepareTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));
                will(new CustomAction("prepareTransaction")
                {
                    @Override public Object invoke(final Invocation invocation) throws Throwable
                    {
                        transaction1PrepareThreadName.setValue(Thread.currentThread().getName());
                        return null;
                    }
                });

                one(provider).commitTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));
                will(new CustomAction("commitTransaction")
                {
                    @Override public Object invoke(final Invocation invocation) throws Throwable
                    {
                        transaction1CommitThreadName.setValue(Thread.currentThread().getName());
                        return null;
                    }
                });

                one(provider).beginTransaction(with(TEST_TRANSACTION_ID_2));
                will(new CustomAction("beginTransaction")
                {
                    @Override public Object invoke(final Invocation invocation) throws Throwable
                    {
                        transaction2BeginThreadName.setValue(Thread.currentThread().getName());
                        return TEST_TRANSACTION_2;
                    }
                });

                one(provider).rollbackTransaction(with(TEST_TRANSACTION_ID_2), with(TEST_TRANSACTION_2), with(TransactionStatus.STARTED));
                will(new CustomAction("rollbackTransaction")
                {
                    @Override public Object invoke(final Invocation invocation) throws Throwable
                    {
                        transaction2RollbackThreadName.setValue(Thread.currentThread().getName());
                        return null;
                    }
                });
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

        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        executor.beginTransaction(TEST_TRANSACTION_ID_2, TEST_SECRET);

        String transaction1OperationThreadName = (String) executor.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, testOperation);
        String transaction2OperationThreadName = (String) executor.executeOperation(TEST_TRANSACTION_ID_2, TEST_SECRET, testOperation);

        executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        executor.commitTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
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
        TransactionParticipant executor = new TransactionParticipant(provider);

        mockery.checking(new Expectations()
        {
            {
                one(provider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(throwException(throwable));

                one(provider).rollbackTransaction(with(TEST_TRANSACTION_ID), with(aNull(Object.class)), with(TransactionStatus.NEW));
            }
        });

        try
        {
            executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t, throwable);
            assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));

            executor.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test(dataProvider = "provideExceptions")
    public void testExecuteOperationFails(Throwable throwable) throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(provider);

        mockery.checking(new Expectations()
        {
            {
                one(provider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(TEST_TRANSACTION));

                one(provider).rollbackTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION), with(TransactionStatus.STARTED));
            }
        });

        assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
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
            executor.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test(dataProvider = "provideExceptions")
    public void testExecuteOperationFailsButGetsRetriedAndSucceeds(Throwable throwable) throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(provider);

        mockery.checking(new Expectations()
        {
            {
                one(provider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(TEST_TRANSACTION));

                one(provider).prepareTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));

                one(provider).commitTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));
            }
        });

        assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
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

            executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));
            executor.commitTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test(dataProvider = "provideExceptions")
    public void testRollbackFails(Throwable throwable) throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(provider);

        mockery.checking(new Expectations()
        {
            {
                one(provider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(TEST_TRANSACTION));

                one(provider).rollbackTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION), with(TransactionStatus.STARTED));
                will(throwException(throwable));
            }
        });

        assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        executor.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TEST_OPERATION_NAME, TEST_RESULT));
        assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));

        try
        {
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
        TransactionParticipant executor = new TransactionParticipant(provider);

        mockery.checking(new Expectations()
        {
            {
                one(provider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(TEST_TRANSACTION));

                one(provider).prepareTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));
                will(throwException(throwable));

                one(provider).rollbackTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION), with(TransactionStatus.STARTED));
            }
        });

        assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        executor.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TEST_OPERATION_NAME, TEST_RESULT));

        try
        {
            executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t, throwable);

            assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));
            executor.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test(dataProvider = "provideExceptions")
    public void testCommitFails(Throwable throwable) throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(provider);

        mockery.checking(new Expectations()
        {
            {
                one(provider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(TEST_TRANSACTION));

                one(provider).prepareTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));

                one(provider).commitTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION));
                will(throwException(throwable));

                one(provider).rollbackTransaction(with(TEST_TRANSACTION_ID), with(TEST_TRANSACTION), with(TransactionStatus.PREPARED));
            }
        });

        assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        executor.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TEST_OPERATION_NAME, TEST_RESULT));
        assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));

        try
        {
            executor.commitTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (Throwable t)
        {
            assertEquals(t, throwable);

            assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));
            executor.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        }
    }

    @Test
    public void testNewTransactionCanBeStarted() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(provider);

        mockery.checking(new Expectations()
        {
            {
                one(provider).beginTransaction(with(TEST_TRANSACTION_ID));
            }
        });

        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
    }

    @Test
    public void testNewTransactionCannotExecuteOperations() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(provider);

        try
        {
            executor.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TEST_OPERATION_NAME, TEST_RESULT));
            Assert.fail();
        } catch (IllegalStateException e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status NEW. Expected statuses [STARTED].");
        }
    }

    @Test
    public void testNewTransactionCannotBePrepared() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(provider);

        try
        {
            executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (IllegalStateException e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status NEW. Expected statuses [STARTED].");
        }
    }

    @Test
    public void testNewTransactionCannotBeCommitted() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(provider);

        try
        {
            executor.commitTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (IllegalStateException e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status NEW. Expected statuses [PREPARED].");
        }
    }

    @Test
    public void testNewTransactionCanBeRolledBack() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(provider);

        mockery.checking(new Expectations()
        {
            {
                one(provider).rollbackTransaction(with(TEST_TRANSACTION_ID), with(aNull(Object.class)), with(TransactionStatus.NEW));
            }
        });

        executor.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
    }

    @Test
    public void testStartedTransactionCannotBeStarted() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(provider);

        mockery.checking(new Expectations()
        {
            {
                one(provider).beginTransaction(with(TEST_TRANSACTION_ID));
            }
        });

        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);

        try
        {
            executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status STARTED. Expected statuses [NEW].");
        }
    }

    @Test
    public void testStartedTransactionCanExecuteOperations() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(provider);

        mockery.checking(new Expectations()
        {
            {
                one(provider).beginTransaction(with(TEST_TRANSACTION_ID));
            }
        });

        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);

        Object result = executor.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TEST_OPERATION_NAME, TEST_RESULT));
        assertEquals(result, TEST_RESULT);

        Object result2 = executor.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TEST_OPERATION_NAME_2, TEST_RESULT_2));
        assertEquals(result2, TEST_RESULT_2);
    }

    @Test
    public void testStartedTransactionCanPrepare() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(provider);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                one(provider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));

                one(provider).prepareTransaction(with(TEST_TRANSACTION_ID), with(transaction));
            }
        });

        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
    }

    @Test
    public void testStartedTransactionCanBeRolledBack() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(provider);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                one(provider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));

                one(provider).rollbackTransaction(with(TEST_TRANSACTION_ID), with(transaction), with(TransactionStatus.STARTED));
            }
        });

        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        executor.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
    }

    @Test
    public void testStartedTransactionCannotBeCommitted() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(provider);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                one(provider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));
            }
        });

        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        try
        {
            executor.commitTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (IllegalStateException e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status STARTED. Expected statuses [PREPARED].");
        }
    }

    @Test
    public void testPreparedTransactionCannotBeStarted() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(provider);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                one(provider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));

                one(provider).prepareTransaction(with(TEST_TRANSACTION_ID), with(transaction));
            }
        });

        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        try
        {
            executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status PREPARED. Expected statuses [NEW].");
        }
    }

    @Test
    public void testPreparedTransactionCannotBePrepared() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(provider);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                one(provider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));

                one(provider).prepareTransaction(with(TEST_TRANSACTION_ID), with(transaction));
            }
        });

        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        try
        {
            executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
            Assert.fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status PREPARED. Expected statuses [STARTED].");
        }
    }

    @Test
    public void testPreparedTransactionCannotExecuteOperations() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(provider);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                one(provider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));

                one(provider).prepareTransaction(with(TEST_TRANSACTION_ID), with(transaction));
            }
        });

        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        try
        {
            executor.executeOperation(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TEST_OPERATION_NAME));
            Assert.fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status PREPARED. Expected statuses [STARTED].");
        }
    }

    @Test
    public void testPreparedTransactionCanBeRolledBack() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(provider);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                one(provider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));

                one(provider).prepareTransaction(with(TEST_TRANSACTION_ID), with(transaction));

                one(provider).rollbackTransaction(with(TEST_TRANSACTION_ID), with(transaction), with(TransactionStatus.PREPARED));
            }
        });

        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        executor.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
    }

    @Test
    public void testPreparedTransactionCanCommit() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(provider);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                one(provider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));

                one(provider).prepareTransaction(with(TEST_TRANSACTION_ID), with(transaction));

                one(provider).commitTransaction(with(TEST_TRANSACTION_ID), with(transaction));
            }
        });

        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        executor.commitTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
    }

    @Test
    public void testCommittedTransactionIsForgotten() throws Throwable
    {
        TransactionParticipant executor = new TransactionParticipant(provider);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                allowing(provider).beginTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));

                one(provider).prepareTransaction(with(TEST_TRANSACTION_ID), with(transaction));

                one(provider).commitTransaction(with(TEST_TRANSACTION_ID), with(transaction));
            }
        });

        assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        executor.beginTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        executor.prepareTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertTrue(executor.isRunningTransaction(TEST_TRANSACTION_ID));
        executor.commitTransaction(TEST_TRANSACTION_ID, TEST_SECRET);
        assertFalse(executor.isRunningTransaction(TEST_TRANSACTION_ID));

        // this is treated as a new transaction as the previous transaction with the same id has been already committed and therefore forgotten
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

