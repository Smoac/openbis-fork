package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TransactionCoordinatorTest
{

    public static final String TEST_TRANSACTION_ID = "test-id";

    private Mockery mockery;

    private ITransactionCoordinatorParticipant participant1;

    private ITransactionCoordinatorParticipant participant2;

    @BeforeMethod
    protected void beforeMethod()
    {
        mockery = new Mockery();
        participant1 = mockery.mock(ITransactionCoordinatorParticipant.class, "participant1");
        participant2 = mockery.mock(ITransactionCoordinatorParticipant.class, "participant2");
    }

    @AfterMethod
    protected void afterMethod()
    {
        mockery.assertIsSatisfied();
    }

    @Test
    public void testBeginTransactionAllParticipantsSucceed()
    {
        TransactionCoordinator coordinator = new TransactionCoordinator(List.of(participant1, participant2));

        mockery.checking(new Expectations()
        {
            {
                one(participant1).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).beginTransaction(with(TEST_TRANSACTION_ID));
            }
        });

        coordinator.beginTransaction(TEST_TRANSACTION_ID);
    }

    @Test
    public void testBeginTransactionSomeParticipantFails()
    {
        TransactionCoordinator coordinator = new TransactionCoordinator(List.of(participant1, participant2));

        mockery.checking(new Expectations()
        {
            {
                one(participant1).beginTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).beginTransaction(with(TEST_TRANSACTION_ID));
                will(throwException(new RuntimeException()));

                one(participant1).rollbackTransaction(with(TEST_TRANSACTION_ID));
                one(participant2).rollbackTransaction(with(TEST_TRANSACTION_ID));
            }
        });

        coordinator.beginTransaction(TEST_TRANSACTION_ID);
    }

}
