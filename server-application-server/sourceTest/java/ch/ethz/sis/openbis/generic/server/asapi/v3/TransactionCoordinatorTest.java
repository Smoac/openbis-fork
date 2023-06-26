package ch.ethz.sis.openbis.generic.server.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TransactionCoordinatorTest
{

    public static final String TEST_TRANSACTION_ID = "test-id";

    private Mockery mockery;

    private ITransactionCoordinatorParticipant participant1;

    private ITransactionCoordinatorParticipant participant2;

    private ITransactionCoordinatorParticipant participant3;

    private ITransactionCoordinatorLog transactionLog;

    @BeforeMethod
    protected void beforeMethod()
    {
        mockery = new Mockery();
        participant1 = mockery.mock(ITransactionCoordinatorParticipant.class, "participant1");
        participant2 = mockery.mock(ITransactionCoordinatorParticipant.class, "participant2");
        participant3 = mockery.mock(ITransactionCoordinatorParticipant.class, "participant3");
        transactionLog = mockery.mock(ITransactionCoordinatorLog.class);
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
                one(transactionLog).beginTransactionStarted(with(TEST_TRANSACTION_ID));

                one(participant1).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).beginTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).beginTransactionFinished(with(TEST_TRANSACTION_ID));
            }
        });

        coordinator.beginTransaction(TEST_TRANSACTION_ID);
    }

    @Test
    public void testBeginTransactionFails()
    {
        TransactionCoordinator coordinator = new TransactionCoordinator(List.of(participant1, participant2, participant3), transactionLog);

        Exception beginException = new RuntimeException();
        Exception rollbackException = new RuntimeException();

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                allowing(participant2).getParticipantId();
                allowing(participant3).getParticipantId();

                one(transactionLog).beginTransactionStarted(with(TEST_TRANSACTION_ID));

                one(participant1).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).beginTransaction(with(TEST_TRANSACTION_ID));
                will(throwException(beginException));

                one(transactionLog).rollbackTransactionStarted(with(TEST_TRANSACTION_ID));

                one(participant1).rollbackTransaction(with(TEST_TRANSACTION_ID));
                // test that a failing rollback won't prevent other rollbacks from being called
                will(throwException(rollbackException));
                one(participant2).rollbackTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).rollbackTransactionFinished(with(TEST_TRANSACTION_ID));
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
                one(transactionLog).beginTransactionStarted(with(TEST_TRANSACTION_ID));

                one(participant1).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).beginTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).beginTransactionFinished(with(TEST_TRANSACTION_ID));
                one(transactionLog).commitTransactionStarted(with(TEST_TRANSACTION_ID));

                one(participant1).prepareTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).prepareTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).commitTransactionPrepared(with(TEST_TRANSACTION_ID));

                one(participant1).commitTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).commitTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).commitTransactionFinished(with(TEST_TRANSACTION_ID));
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

                one(transactionLog).beginTransactionStarted(with(TEST_TRANSACTION_ID));

                one(participant1).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant3).beginTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).beginTransactionFinished(with(TEST_TRANSACTION_ID));
                one(transactionLog).commitTransactionStarted(with(TEST_TRANSACTION_ID));

                one(participant1).prepareTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).prepareTransaction(with(TEST_TRANSACTION_ID));
                will(throwException(prepareException));

                one(transactionLog).rollbackTransactionStarted(with(TEST_TRANSACTION_ID));

                one(participant1).rollbackTransaction(with(TEST_TRANSACTION_ID));
                // test that a failing rollback won't prevent other rollbacks from being called
                will(throwException(rollbackException));
                one(participant2).rollbackTransaction(with(TEST_TRANSACTION_ID));
                one(participant3).rollbackTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).rollbackTransactionFinished(with(TEST_TRANSACTION_ID));
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

                one(transactionLog).beginTransactionStarted(with(TEST_TRANSACTION_ID));

                one(participant1).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant3).beginTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).beginTransactionFinished(with(TEST_TRANSACTION_ID));
                one(transactionLog).commitTransactionStarted(with(TEST_TRANSACTION_ID));

                one(participant1).prepareTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).prepareTransaction(with(TEST_TRANSACTION_ID));
                one(participant3).prepareTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).commitTransactionPrepared(with(TEST_TRANSACTION_ID));

                one(participant1).commitTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).commitTransaction(with(TEST_TRANSACTION_ID));
                will(throwException(commitException));

                one(transactionLog).rollbackTransactionStarted(with(TEST_TRANSACTION_ID));

                one(participant1).rollbackTransaction(with(TEST_TRANSACTION_ID));
                // test that a failing rollback won't prevent other rollbacks from being called
                will(throwException(rollbackException));
                one(participant2).rollbackTransaction(with(TEST_TRANSACTION_ID));
                one(participant3).rollbackTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).rollbackTransactionFinished(with(TEST_TRANSACTION_ID));
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
                one(transactionLog).beginTransactionStarted(with(TEST_TRANSACTION_ID));

                one(participant1).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).beginTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).beginTransactionFinished(with(TEST_TRANSACTION_ID));
                one(transactionLog).rollbackTransactionStarted(with(TEST_TRANSACTION_ID));

                one(participant1).rollbackTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).rollbackTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).rollbackTransactionFinished(with(TEST_TRANSACTION_ID));
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

                one(transactionLog).beginTransactionStarted(with(TEST_TRANSACTION_ID));

                one(participant1).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant3).beginTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).beginTransactionFinished(with(TEST_TRANSACTION_ID));
                one(transactionLog).rollbackTransactionStarted(with(TEST_TRANSACTION_ID));

                one(participant1).rollbackTransaction(with(TEST_TRANSACTION_ID));
                // test that a failing rollback won't prevent other rollbacks from being called
                will(throwException(rollbackException));
                one(participant2).rollbackTransaction(with(TEST_TRANSACTION_ID));
                one(participant3).rollbackTransaction(with(TEST_TRANSACTION_ID));

                one(transactionLog).rollbackTransactionFinished(with(TEST_TRANSACTION_ID));
            }
        });

        coordinator.beginTransaction(TEST_TRANSACTION_ID);
        coordinator.rollbackTransaction(TEST_TRANSACTION_ID);
    }

}
