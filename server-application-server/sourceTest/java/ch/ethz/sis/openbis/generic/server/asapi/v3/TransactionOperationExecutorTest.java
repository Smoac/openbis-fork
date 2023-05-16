package ch.ethz.sis.openbis.generic.server.asapi.v3;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TransactionOperationExecutorTest
{

    public static final String TEST_TRANSACTION_ID = "test-id";

    public static final String TEST_TRANSACTION_ID_2 = "test-id-2";

    public static final String TEST_SECRET = "test-secret";

    public static final String TEST_OPERATION_NAME = "test-operation";

    public static final String TEST_OPERATION_NAME_2 = "test-operation-2";

    public static final String TEST_RESULT = "test-result";

    public static final String TEST_RESULT_2 = "test-result-2";

    private Mockery mockery;

    private ITransactionOperationContext context;

    @BeforeMethod
    protected void beforeMethod()
    {
        mockery = new Mockery();
        context = mockery.mock(ITransactionOperationContext.class);
    }

    @AfterMethod
    protected void afterMethod()
    {
        mockery.assertIsSatisfied();
    }

    @Test
    public void testNewTransactionCanBeStarted() throws Throwable
    {
        TransactionOperationExecutor executor = new TransactionOperationExecutor(context);

        mockery.checking(new Expectations()
        {
            {
                one(context).getTransaction(with(TEST_TRANSACTION_ID));
            }
        });

        Object result = executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.BEGIN_TRANSACTION_METHOD));
        Assert.assertNull(result);
    }

    @Test
    public void testNewTransactionCannotExecuteOperations() throws Throwable
    {
        TransactionOperationExecutor executor = new TransactionOperationExecutor(context);

        try
        {
            executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TEST_OPERATION_NAME, TEST_RESULT));
            Assert.fail();
        } catch (IllegalStateException e)
        {
            Assert.assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status NEW. Expected statuses [STARTED].");
        }
    }

    @Test
    public void testNewTransactionCannotBePrepared() throws Throwable
    {
        TransactionOperationExecutor executor = new TransactionOperationExecutor(context);

        try
        {
            executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.PREPARE_TRANSACTION_METHOD));
            Assert.fail();
        } catch (IllegalStateException e)
        {
            Assert.assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status NEW. Expected statuses [STARTED].");
        }
    }

    @Test
    public void testNewTransactionCannotBeCommitted() throws Throwable
    {
        TransactionOperationExecutor executor = new TransactionOperationExecutor(context);

        try
        {
            executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.COMMIT_TRANSACTION_METHOD));
            Assert.fail();
        } catch (IllegalStateException e)
        {
            Assert.assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status NEW. Expected statuses [PREPARED].");
        }
    }

    @Test
    public void testNewTransactionCannotBeRolledBack() throws Throwable
    {
        TransactionOperationExecutor executor = new TransactionOperationExecutor(context);

        try
        {
            executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.ROLLBACK_TRANSACTION_METHOD));
            Assert.fail();
        } catch (IllegalStateException e)
        {
            Assert.assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status NEW. Expected statuses [STARTED, PREPARED].");
        }
    }

    @Test
    public void testStartedTransactionCannotBeStarted() throws Throwable
    {
        TransactionOperationExecutor executor = new TransactionOperationExecutor(context);

        mockery.checking(new Expectations()
        {
            {
                one(context).getTransaction(with(TEST_TRANSACTION_ID));
            }
        });

        executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.BEGIN_TRANSACTION_METHOD));
        try
        {
            executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.BEGIN_TRANSACTION_METHOD));
            Assert.fail();
        } catch (Exception e)
        {
            Assert.assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status STARTED. Expected statuses [NEW].");
        }
    }

    @Test
    public void testStartedTransactionCanExecuteOperations() throws Throwable
    {
        TransactionOperationExecutor executor = new TransactionOperationExecutor(context);

        mockery.checking(new Expectations()
        {
            {
                one(context).getTransaction(with(TEST_TRANSACTION_ID));
            }
        });

        executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.BEGIN_TRANSACTION_METHOD));

        Object result = executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TEST_OPERATION_NAME, TEST_RESULT));
        Assert.assertEquals(result, TEST_RESULT);

        Object result2 = executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TEST_OPERATION_NAME_2, TEST_RESULT_2));
        Assert.assertEquals(result2, TEST_RESULT_2);
    }

    @Test
    public void testStartedTransactionCanPrepare() throws Throwable
    {
        TransactionOperationExecutor executor = new TransactionOperationExecutor(context);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                one(context).getTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));

                one(context).prepareTransaction(with(TEST_TRANSACTION_ID), with(transaction));
            }
        });

        executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.BEGIN_TRANSACTION_METHOD));
        executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.PREPARE_TRANSACTION_METHOD));
    }

    @Test
    public void testStartedTransactionCanRollback() throws Throwable
    {
        TransactionOperationExecutor executor = new TransactionOperationExecutor(context);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                one(context).getTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));

                one(context).rollbackTransaction(with(TEST_TRANSACTION_ID), with(transaction));
            }
        });

        executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.BEGIN_TRANSACTION_METHOD));
        executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.ROLLBACK_TRANSACTION_METHOD));
    }

    @Test
    public void testStartedTransactionCannotBeCommitted() throws Throwable
    {
        TransactionOperationExecutor executor = new TransactionOperationExecutor(context);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                one(context).getTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));

                one(context).rollbackTransaction(with(TEST_TRANSACTION_ID), with(transaction));
            }
        });

        executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.BEGIN_TRANSACTION_METHOD));
        executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.ROLLBACK_TRANSACTION_METHOD));
    }

    @Test
    public void testPreparedTransactionCannotBeStarted() throws Throwable
    {
        TransactionOperationExecutor executor = new TransactionOperationExecutor(context);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                one(context).getTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));

                one(context).prepareTransaction(with(TEST_TRANSACTION_ID), with(transaction));
            }
        });

        executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.BEGIN_TRANSACTION_METHOD));
        executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.PREPARE_TRANSACTION_METHOD));
        try
        {
            executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.BEGIN_TRANSACTION_METHOD));
            Assert.fail();
        } catch (Exception e)
        {
            Assert.assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status PREPARED. Expected statuses [NEW].");
        }
    }

    @Test
    public void testPreparedTransactionCannotBePrepared() throws Throwable
    {
        TransactionOperationExecutor executor = new TransactionOperationExecutor(context);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                one(context).getTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));

                one(context).prepareTransaction(with(TEST_TRANSACTION_ID), with(transaction));
            }
        });

        executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.BEGIN_TRANSACTION_METHOD));
        executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.PREPARE_TRANSACTION_METHOD));
        try
        {
            executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.PREPARE_TRANSACTION_METHOD));
            Assert.fail();
        } catch (Exception e)
        {
            Assert.assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status PREPARED. Expected statuses [STARTED].");
        }
    }

    @Test
    public void testPreparedTransactionCannotExecuteOperations() throws Throwable
    {
        TransactionOperationExecutor executor = new TransactionOperationExecutor(context);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                one(context).getTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));

                one(context).prepareTransaction(with(TEST_TRANSACTION_ID), with(transaction));
            }
        });

        executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.BEGIN_TRANSACTION_METHOD));
        executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.PREPARE_TRANSACTION_METHOD));
        try
        {
            executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TEST_OPERATION_NAME));
            Assert.fail();
        } catch (Exception e)
        {
            Assert.assertEquals(e.getMessage(), "Two phase transaction test-id unexpected status PREPARED. Expected statuses [STARTED].");
        }
    }

    @Test
    public void testPreparedTransactionCanRollBack() throws Throwable
    {
        TransactionOperationExecutor executor = new TransactionOperationExecutor(context);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                one(context).getTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));

                one(context).prepareTransaction(with(TEST_TRANSACTION_ID), with(transaction));

                one(context).rollbackTransaction(with(TEST_TRANSACTION_ID), with(transaction));
            }
        });

        executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.BEGIN_TRANSACTION_METHOD));
        executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.PREPARE_TRANSACTION_METHOD));
        executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.ROLLBACK_TRANSACTION_METHOD));
    }

    @Test
    public void testPreparedTransactionCanCommit() throws Throwable
    {
        TransactionOperationExecutor executor = new TransactionOperationExecutor(context);

        mockery.checking(new Expectations()
        {
            {
                Object transaction = new Object();

                one(context).getTransaction(with(TEST_TRANSACTION_ID));
                will(returnValue(transaction));

                one(context).prepareTransaction(with(TEST_TRANSACTION_ID), with(transaction));

                one(context).commitTransaction(with(TEST_TRANSACTION_ID), with(transaction));
            }
        });

        executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.BEGIN_TRANSACTION_METHOD));
        executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.PREPARE_TRANSACTION_METHOD));
        executor.execute(TEST_TRANSACTION_ID, TEST_SECRET, new TestOperation(TransactionConst.COMMIT_TRANSACTION_METHOD));
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

}

