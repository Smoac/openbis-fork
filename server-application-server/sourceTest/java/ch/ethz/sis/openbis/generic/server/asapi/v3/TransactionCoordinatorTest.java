package ch.ethz.sis.openbis.generic.server.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TransactionCoordinatorTest
{

    public static final String TEST_TRANSACTION_ID = "test-id";

    public static final String TEST_TRANSACTION_ID_2 = "test-id-2";

    private Mockery mockery;

    private ITransactionCoordinatorParticipant participant1;

    private ITransactionCoordinatorParticipant participant2;

    private ITransactionCoordinatorParticipant participant3;

    private ITransactionLog transactionLog;

    @BeforeMethod
    protected void beforeMethod()
    {
        mockery = new Mockery();
        participant1 = mockery.mock(ITransactionCoordinatorParticipant.class, "participant1");
        participant2 = mockery.mock(ITransactionCoordinatorParticipant.class, "participant2");
        participant3 = mockery.mock(ITransactionCoordinatorParticipant.class, "participant3");
        transactionLog = mockery.mock(ITransactionLog.class);
    }

    @AfterMethod
    protected void afterMethod()
    {
        mockery.assertIsSatisfied();
    }

    @Test
    public void testBeginTransactionSucceeds()
    {
        TransactionCoordinator coordinator = new TransactionCoordinator(List.of(participant1, participant2), transactionLog);

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                allowing(participant2).getParticipantId();

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.BEGIN_STARTED));

                one(participant1).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).beginTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.BEGIN_FINISHED));
            }
        });

        coordinator.beginTransaction(TEST_TRANSACTION_ID);
    }

    @Test
    public void testBeginTransactionFails()
    {
        TransactionCoordinator coordinator = new TransactionCoordinator(List.of(participant1, participant2), transactionLog);

        Exception beginException = new RuntimeException();
        Exception rollbackException = new RuntimeException();

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                allowing(participant2).getParticipantId();

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.BEGIN_STARTED));

                one(participant1).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).beginTransaction(with(TEST_TRANSACTION_ID));
                will(throwException(beginException));

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.ROLLBACK_STARTED));

                one(participant1).rollbackTransaction(with(TEST_TRANSACTION_ID));
                // test that a failing rollback won't prevent other rollbacks from being called
                will(throwException(rollbackException));
                one(participant2).rollbackTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.ROLLBACK_FINISHED));
            }
        });

        try
        {
            coordinator.beginTransaction(TEST_TRANSACTION_ID);
        } catch (Exception e)
        {
            assertEquals(e, beginException);
        }
    }

    @Test
    public void testCommitTransactionSucceeds()
    {
        TransactionCoordinator coordinator = new TransactionCoordinator(List.of(participant1, participant2), transactionLog);

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                allowing(participant2).getParticipantId();

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.BEGIN_STARTED));

                one(participant1).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).beginTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.BEGIN_FINISHED));
                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.PREPARE_STARTED));

                one(participant1).prepareTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).prepareTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.PREPARE_FINISHED));
                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.COMMIT_STARTED));

                one(participant1).commitTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).commitTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.COMMIT_FINISHED));
            }
        });

        coordinator.beginTransaction(TEST_TRANSACTION_ID);
        coordinator.commitTransaction(TEST_TRANSACTION_ID);
    }

    @Test
    public void testCommitTransactionFailsDuringPrepare()
    {
        TransactionCoordinator coordinator = new TransactionCoordinator(List.of(participant1, participant2, participant3), transactionLog);

        Exception prepareException = new RuntimeException();
        Exception rollbackException = new RuntimeException();

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                allowing(participant2).getParticipantId();
                allowing(participant3).getParticipantId();

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.BEGIN_STARTED));

                one(participant1).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant3).beginTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.BEGIN_FINISHED));
                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.PREPARE_STARTED));

                one(participant1).prepareTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).prepareTransaction(with(TEST_TRANSACTION_ID));
                will(throwException(prepareException));

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.ROLLBACK_STARTED));

                one(participant1).rollbackTransaction(with(TEST_TRANSACTION_ID));
                // test that a failing rollback won't prevent other rollbacks from being called
                will(throwException(rollbackException));
                one(participant2).rollbackTransaction(with(TEST_TRANSACTION_ID));
                one(participant3).rollbackTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.ROLLBACK_FINISHED));
            }
        });

        coordinator.beginTransaction(TEST_TRANSACTION_ID);

        try
        {
            coordinator.commitTransaction(TEST_TRANSACTION_ID);
            Assert.fail();
        } catch (Exception e)
        {
            assertEquals(e, prepareException);
        }
    }

    @Test
    public void testCommitTransactionFailsDuringCommit()
    {
        TransactionCoordinator coordinator = new TransactionCoordinator(List.of(participant1, participant2, participant3), transactionLog);

        Exception commitException = new RuntimeException();
        Exception rollbackException = new RuntimeException();

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                allowing(participant2).getParticipantId();
                allowing(participant3).getParticipantId();

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.BEGIN_STARTED));

                one(participant1).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant3).beginTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.BEGIN_FINISHED));
                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.PREPARE_STARTED));

                one(participant1).prepareTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).prepareTransaction(with(TEST_TRANSACTION_ID));
                one(participant3).prepareTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.PREPARE_FINISHED));
                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.COMMIT_STARTED));

                one(participant1).commitTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).commitTransaction(with(TEST_TRANSACTION_ID));
                will(throwException(commitException));

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.ROLLBACK_STARTED));

                one(participant1).rollbackTransaction(with(TEST_TRANSACTION_ID));
                // test that a failing rollback won't prevent other rollbacks from being called
                will(throwException(rollbackException));
                one(participant2).rollbackTransaction(with(TEST_TRANSACTION_ID));
                one(participant3).rollbackTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.ROLLBACK_FINISHED));
            }
        });

        coordinator.beginTransaction(TEST_TRANSACTION_ID);

        try
        {
            coordinator.commitTransaction(TEST_TRANSACTION_ID);
            Assert.fail();
        } catch (Exception e)
        {
            assertEquals(e, commitException);
        }
    }

    @Test
    public void testRollbackTransactionSucceeds()
    {
        TransactionCoordinator coordinator = new TransactionCoordinator(List.of(participant1, participant2), transactionLog);

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                allowing(participant2).getParticipantId();

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.BEGIN_STARTED));

                one(participant1).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).beginTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.BEGIN_FINISHED));
                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.ROLLBACK_STARTED));

                one(participant1).rollbackTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).rollbackTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.ROLLBACK_FINISHED));
            }
        });

        coordinator.beginTransaction(TEST_TRANSACTION_ID);
        coordinator.rollbackTransaction(TEST_TRANSACTION_ID);
    }

    @Test
    public void testRollbackTransactionFails()
    {
        TransactionCoordinator coordinator = new TransactionCoordinator(List.of(participant1, participant2, participant3), transactionLog);

        Exception rollbackException = new RuntimeException();

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                allowing(participant2).getParticipantId();
                allowing(participant3).getParticipantId();

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.BEGIN_STARTED));

                one(participant1).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant3).beginTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.BEGIN_FINISHED));
                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.ROLLBACK_STARTED));

                one(participant1).rollbackTransaction(with(TEST_TRANSACTION_ID));
                // test that a failing rollback won't prevent other rollbacks from being called
                will(throwException(rollbackException));
                one(participant2).rollbackTransaction(with(TEST_TRANSACTION_ID));
                one(participant3).rollbackTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.ROLLBACK_FINISHED));
            }
        });

        coordinator.beginTransaction(TEST_TRANSACTION_ID);
        coordinator.rollbackTransaction(TEST_TRANSACTION_ID);
    }

    @Test
    public void testRestoreTransactionWithStatusBeginStarted()
    {
        TransactionCoordinator coordinator = new TransactionCoordinator(List.of(participant1, participant2), transactionLog);

        Map<String, TransactionStatus> lastStatuses = new HashMap<>();
        lastStatuses.put(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED);

        Exception rollbackException = new RuntimeException();

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                allowing(participant2).getParticipantId();

                one(transactionLog).getLastStatuses();
                will(returnValue(lastStatuses));

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.ROLLBACK_STARTED));

                one(participant1).rollbackTransaction(with(TEST_TRANSACTION_ID));
                // test that a failing rollback won't prevent other rollbacks from being called
                will(throwException(rollbackException));
                one(participant2).rollbackTransaction(TEST_TRANSACTION_ID);

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.ROLLBACK_FINISHED));
            }
        });

        coordinator.restoreTransactions();
    }

    @Test(dataProvider = "provideTestRestoreTransactionWithStatus")
    public void testRestoreTransactionWithStatus(TransactionStatus transactionStatus, boolean throwException)
    {
        TransactionCoordinator coordinator = new TransactionCoordinator(List.of(participant1, participant2), transactionLog);

        Map<String, TransactionStatus> lastStatuses = new HashMap<>();
        lastStatuses.put(TEST_TRANSACTION_ID, transactionStatus);

        Exception exception = new RuntimeException();

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                allowing(participant2).getParticipantId();

                one(transactionLog).getLastStatuses();
                will(returnValue(lastStatuses));

                switch (transactionStatus)
                {
                    case BEGIN_STARTED:
                    case BEGIN_FINISHED:
                    case PREPARE_STARTED:
                    case ROLLBACK_STARTED:
                        one(participant1).rollbackTransaction(with(TEST_TRANSACTION_ID));

                        if (throwException)
                        {
                            // test that a failing rollback won't prevent other rollbacks from being called
                            will(throwException(exception));
                        }

                        one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.ROLLBACK_STARTED));
                        one(participant2).rollbackTransaction(TEST_TRANSACTION_ID);
                        one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.ROLLBACK_FINISHED));
                        break;
                    case PREPARE_FINISHED:
                    case COMMIT_STARTED:
                        one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.COMMIT_STARTED));
                        one(participant1).commitTransaction(with(TEST_TRANSACTION_ID));

                        if (throwException)
                        {
                            will(throwException(exception));
                            one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.ROLLBACK_STARTED));
                            one(participant1).rollbackTransaction(with(TEST_TRANSACTION_ID));
                            // test that a failing rollback won't prevent other rollbacks from being called
                            will(throwException(exception));
                            one(participant2).rollbackTransaction(TEST_TRANSACTION_ID);
                            one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.ROLLBACK_FINISHED));
                        } else
                        {
                            one(participant2).commitTransaction(TEST_TRANSACTION_ID);
                            one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.COMMIT_FINISHED));
                        }
                        break;
                    case COMMIT_FINISHED:
                    case ROLLBACK_FINISHED:
                }
            }
        });

        coordinator.restoreTransactions();
    }

    @Test
    public void testRestoreMultipleTransactions()
    {
        TransactionCoordinator coordinator = new TransactionCoordinator(List.of(participant1, participant2), transactionLog);

        Map<String, TransactionStatus> lastStatuses = new HashMap<>();
        lastStatuses.put(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_FINISHED);
        lastStatuses.put(TEST_TRANSACTION_ID_2, TransactionStatus.PREPARE_FINISHED);

        Exception exception = new RuntimeException();

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                allowing(participant2).getParticipantId();

                one(transactionLog).getLastStatuses();
                will(returnValue(lastStatuses));

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.COMMIT_STARTED));
                one(participant1).commitTransaction(with(TEST_TRANSACTION_ID));
                will(throwException(exception));

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.ROLLBACK_STARTED));
                one(participant1).rollbackTransaction(with(TEST_TRANSACTION_ID));
                // test that a failing rollback won't prevent other rollbacks from being called
                will(throwException(exception));
                one(participant2).rollbackTransaction(TEST_TRANSACTION_ID);
                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID), with(TransactionStatus.ROLLBACK_FINISHED));

                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID_2), with(TransactionStatus.COMMIT_STARTED));
                one(participant1).commitTransaction(with(TEST_TRANSACTION_ID_2));
                one(participant2).commitTransaction(with(TEST_TRANSACTION_ID_2));
                one(transactionLog).logStatus(with(TEST_TRANSACTION_ID_2), with(TransactionStatus.COMMIT_FINISHED));
            }
        });

        coordinator.restoreTransactions();
    }

    @DataProvider(name = "provideTestRestoreTransactionWithStatus")
    public Object[][] provideTestRestoreTransactionWithStatus()
    {
        List<Object[]> statuses = new ArrayList<>();
        Arrays.stream(TransactionStatus.values()).forEach(s ->
        {
            statuses.add(new Object[] { s, false });
            statuses.add(new Object[] { s, true });
        });
        return statuses.toArray(new Object[0][0]);
    }

}
