package ch.ethz.sis.transaction;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TransactionCoordinatorTest
{

    public static final UUID TEST_TRANSACTION_ID = UUID.randomUUID();

    public static final UUID TEST_TRANSACTION_ID_2 = UUID.randomUUID();

    public static final UUID TEST_TRANSACTION_ID_3 = UUID.randomUUID();

    public static final UUID TEST_TRANSACTION_ID_4 = UUID.randomUUID();

    public static final String TEST_PARTICIPANT_ID = "participant-id";

    public static final String TEST_PARTICIPANT_ID_2 = "participant-id-2";

    public static final String TEST_PARTICIPANT_ID_3 = "participant-id-3";

    public static final String TEST_SESSION_TOKEN = "test-session-token";

    public static final String TEST_INTERACTIVE_SESSION_KEY = "test-interactive-session-key";

    public static final String TEST_TRANSACTION_COORDINATOR_KEY = "test-transaction-coordinator-key";

    public static final String TEST_OPERATION_NAME = "test-operation";

    public static final Object[] TEST_OPERATION_ARGUMENTS = new Object[] { 1, "abc" };

    public static final int TEST_TIMEOUT = 60;

    public static final int TEST_COUNT_LIMIT = 10;

    private Mockery mockery;

    private ITransactionParticipant participant1;

    private ITransactionParticipant participant2;

    private ITransactionParticipant participant3;

    private ISessionTokenProvider sessionTokenProvider;

    private ITransactionLog transactionLog;

    @BeforeMethod
    protected void beforeMethod()
    {
        mockery = new Mockery();
        participant1 = mockery.mock(ITransactionParticipant.class, "participant1");
        participant2 = mockery.mock(ITransactionParticipant.class, "participant2");
        participant3 = mockery.mock(ITransactionParticipant.class, "participant3");
        sessionTokenProvider = mockery.mock(ISessionTokenProvider.class);
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
        TransactionCoordinator coordinator =
                new TransactionCoordinator(TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        List.of(participant1, participant2), transactionLog, TEST_TIMEOUT, TEST_COUNT_LIMIT);

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                allowing(participant2).getParticipantId();

                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED)));

                one(participant1).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);
                one(participant2).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED)));
            }
        });

        coordinator.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
    }

    @Test
    public void testBeginTransactionFails()
    {
        TransactionCoordinator coordinator =
                new TransactionCoordinator(TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        List.of(participant1, participant2), transactionLog, TEST_TIMEOUT, TEST_COUNT_LIMIT);

        Exception beginException = new RuntimeException();
        Exception rollbackException = new RuntimeException();

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                will(returnValue(TEST_PARTICIPANT_ID));
                allowing(participant2).getParticipantId();
                will(returnValue(TEST_PARTICIPANT_ID_2));

                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED)));

                one(participant1).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);
                one(participant2).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);
                will(throwException(beginException));

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_STARTED)));

                one(participant1).rollbackTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
                // test that a failing rollback won't prevent other rollbacks from being called
                will(throwException(rollbackException));
                one(participant2).rollbackTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
            }
        });

        try
        {
            coordinator.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Begin transaction '" + TEST_TRANSACTION_ID + "' failed for participant '" + TEST_PARTICIPANT_ID_2 + "'.");
            assertEquals(e.getCause(), beginException);
        }
    }

    @Test
    public void testExecuteOperationSucceeds()
    {
        TransactionCoordinator coordinator =
                new TransactionCoordinator(TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        List.of(participant1, participant2), transactionLog, TEST_TIMEOUT, TEST_COUNT_LIMIT);

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                will(returnValue(TEST_PARTICIPANT_ID));
                allowing(participant2).getParticipantId();
                will(returnValue(TEST_PARTICIPANT_ID_2));

                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED)));

                one(participant1).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);
                one(participant2).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED)));

                one(participant1).executeOperation(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_OPERATION_NAME,
                        TEST_OPERATION_ARGUMENTS);
            }
        });

        coordinator.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        coordinator.executeOperation(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_PARTICIPANT_ID, TEST_OPERATION_NAME,
                TEST_OPERATION_ARGUMENTS);
    }

    @Test
    public void testExecuteOperationFails()
    {
        TransactionCoordinator coordinator =
                new TransactionCoordinator(TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        List.of(participant1, participant2), transactionLog, TEST_TIMEOUT, TEST_COUNT_LIMIT);

        Exception executeOperationException = new RuntimeException();

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                will(returnValue(TEST_PARTICIPANT_ID));
                allowing(participant2).getParticipantId();
                will(returnValue(TEST_PARTICIPANT_ID_2));

                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED)));

                one(participant1).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);
                one(participant2).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED)));

                one(participant1).executeOperation(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_OPERATION_NAME,
                        TEST_OPERATION_ARGUMENTS);
                will(throwException(executeOperationException));
            }
        });

        coordinator.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);

        try
        {
            coordinator.executeOperation(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, TEST_PARTICIPANT_ID,
                    TEST_OPERATION_NAME,
                    TEST_OPERATION_ARGUMENTS);
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(),
                    "Transaction '" + TEST_TRANSACTION_ID + "' execute operation '" + TEST_OPERATION_NAME + "' failed for participant '"
                            + TEST_PARTICIPANT_ID + "'.");
            assertEquals(e.getCause(), executeOperationException);
        }
    }

    @Test
    public void testExecuteOperationWithUnknownParticipantId()
    {
        TransactionCoordinator coordinator =
                new TransactionCoordinator(TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        List.of(participant1, participant2), transactionLog, TEST_TIMEOUT, TEST_COUNT_LIMIT);

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                will(returnValue(TEST_PARTICIPANT_ID));
                allowing(participant2).getParticipantId();
                will(returnValue(TEST_PARTICIPANT_ID_2));

                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED)));

                one(participant1).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);
                one(participant2).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED)));
            }
        });

        coordinator.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);

        try
        {
            coordinator.executeOperation(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY, "unknown-participant-id",
                    TEST_OPERATION_NAME, TEST_OPERATION_ARGUMENTS);
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Unknown participant id: unknown-participant-id");
        }
    }

    @Test
    public void testCommitTransactionSucceeds()
    {
        TransactionCoordinator coordinator =
                new TransactionCoordinator(TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        List.of(participant1, participant2), transactionLog, TEST_TIMEOUT, TEST_COUNT_LIMIT);

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                allowing(participant2).getParticipantId();

                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED)));

                one(participant1).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);
                one(participant2).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED)));
                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_STARTED)));

                one(participant1).prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);
                one(participant2).prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_FINISHED)));
                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.COMMIT_STARTED)));

                one(participant1).commitTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
                one(participant2).commitTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.COMMIT_FINISHED)));
            }
        });

        coordinator.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        coordinator.commitTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
    }

    @Test
    public void testCommitTransactionFailsDuringPrepare()
    {
        TransactionCoordinator coordinator =
                new TransactionCoordinator(TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        List.of(participant1, participant2, participant3), transactionLog, TEST_TIMEOUT, TEST_COUNT_LIMIT);

        Exception prepareException = new RuntimeException();
        Exception rollbackException = new RuntimeException();

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                will(returnValue(TEST_PARTICIPANT_ID));
                allowing(participant2).getParticipantId();
                will(returnValue(TEST_PARTICIPANT_ID_2));
                allowing(participant3).getParticipantId();
                will(returnValue(TEST_PARTICIPANT_ID_3));

                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED)));

                one(participant1).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);
                one(participant2).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);
                one(participant3).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED)));
                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_STARTED)));

                one(participant1).prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);
                one(participant2).prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);
                will(throwException(prepareException));

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_STARTED)));

                one(participant1).rollbackTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
                // test that a failing rollback won't prevent other rollbacks from being called
                will(throwException(rollbackException));
                one(participant2).rollbackTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
                one(participant3).rollbackTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
            }
        });

        coordinator.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);

        try
        {
            coordinator.commitTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
            fail();
        } catch (Exception e)
        {
            assertEquals(e.getMessage(), "Prepare transaction '" + TEST_TRANSACTION_ID + "' failed for participant '" + TEST_PARTICIPANT_ID_2 + "'.");
            assertEquals(e.getCause(), prepareException);
        }
    }

    @Test
    public void testCommitTransactionFailsDuringCommit()
    {
        TransactionCoordinator coordinator =
                new TransactionCoordinator(TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        List.of(participant1, participant2, participant3), transactionLog, TEST_TIMEOUT, TEST_COUNT_LIMIT);

        Exception commitException1 = new RuntimeException();
        Exception commitException2 = new RuntimeException();

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                allowing(participant2).getParticipantId();
                allowing(participant3).getParticipantId();

                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED)));

                one(participant1).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);
                one(participant2).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);
                one(participant3).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED)));
                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_STARTED)));

                one(participant1).prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);
                one(participant2).prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);
                one(participant3).prepareTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.PREPARE_FINISHED)));
                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.COMMIT_STARTED)));

                // test that a failing commit won't prevent other commits from being called
                one(participant1).commitTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
                will(throwException(commitException1));
                one(participant2).commitTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
                will(throwException(commitException2));
                one(participant3).commitTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
            }
        });

        coordinator.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        coordinator.commitTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
    }

    @Test
    public void testRollbackTransactionSucceeds()
    {
        TransactionCoordinator coordinator =
                new TransactionCoordinator(TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        List.of(participant1, participant2), transactionLog, TEST_TIMEOUT, TEST_COUNT_LIMIT);

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                allowing(participant2).getParticipantId();

                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED)));

                one(participant1).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);
                one(participant2).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED)));
                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_STARTED)));

                one(participant1).rollbackTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
                one(participant2).rollbackTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_FINISHED)));
            }
        });

        coordinator.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        coordinator.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
    }

    @Test
    public void testRollbackTransactionFails()
    {
        TransactionCoordinator coordinator =
                new TransactionCoordinator(TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        List.of(participant1, participant2, participant3), transactionLog, TEST_TIMEOUT, TEST_COUNT_LIMIT);

        Exception rollbackException1 = new RuntimeException();
        Exception rollbackException2 = new RuntimeException();

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                allowing(participant2).getParticipantId();
                allowing(participant3).getParticipantId();

                allowing(sessionTokenProvider).isValid(TEST_SESSION_TOKEN);
                will(returnValue(true));

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_STARTED)));

                one(participant1).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);
                one(participant2).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);
                one(participant3).beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY,
                        TEST_TRANSACTION_COORDINATOR_KEY);

                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.BEGIN_FINISHED)));
                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_STARTED)));

                // test that a failing rollback won't prevent other rollbacks from being called
                one(participant1).rollbackTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
                will(throwException(rollbackException1));
                one(participant2).rollbackTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
                will(throwException(rollbackException2));
                one(participant3).rollbackTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
            }
        });

        coordinator.beginTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
        coordinator.rollbackTransaction(TEST_TRANSACTION_ID, TEST_SESSION_TOKEN, TEST_INTERACTIVE_SESSION_KEY);
    }

    @Test(dataProvider = "provideTestRecoverTransactionWithStatus")
    public void testRecoverTransactionWithStatus(TransactionStatus transactionStatus, boolean throwException)
    {
        TransactionCoordinator coordinator =
                new TransactionCoordinator(TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        List.of(participant1, participant2, participant3), transactionLog, TEST_TIMEOUT, TEST_COUNT_LIMIT);

        TransactionLogEntry logEntry = new TransactionLogEntry();
        logEntry.setTransactionId(TEST_TRANSACTION_ID);
        logEntry.setTransactionStatus(transactionStatus);

        Map<UUID, TransactionLogEntry> logEntries = Map.of(logEntry.getTransactionId(), logEntry);

        Exception exception = new RuntimeException();

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                allowing(participant2).getParticipantId();
                allowing(participant3).getParticipantId();

                one(transactionLog).getTransactions();
                will(returnValue(logEntries));

                switch (transactionStatus)
                {
                    case BEGIN_STARTED:
                    case BEGIN_FINISHED:
                    case PREPARE_STARTED:
                    case ROLLBACK_STARTED:
                        one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_STARTED)));
                        one(participant1).rollbackTransaction(TEST_TRANSACTION_ID, TEST_TRANSACTION_COORDINATOR_KEY);

                        if (throwException)
                        {
                            // test that a failing rollback won't prevent other rollbacks from being called
                            will(throwException(exception));
                        }

                        one(participant2).rollbackTransaction(TEST_TRANSACTION_ID, TEST_TRANSACTION_COORDINATOR_KEY);

                        if (!throwException)
                        {
                            one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.ROLLBACK_FINISHED)));
                        }

                        one(participant3).rollbackTransaction(TEST_TRANSACTION_ID, TEST_TRANSACTION_COORDINATOR_KEY);
                        break;
                    case PREPARE_FINISHED:
                    case COMMIT_STARTED:
                        // only participant 1 and 2 know the transaction
                        one(participant1).recoverTransactions(TEST_TRANSACTION_COORDINATOR_KEY);
                        will(returnValue(Collections.singletonList(TEST_TRANSACTION_ID)));
                        one(participant2).recoverTransactions(TEST_TRANSACTION_COORDINATOR_KEY);
                        will(returnValue(Collections.singletonList(TEST_TRANSACTION_ID)));
                        one(participant3).recoverTransactions(TEST_TRANSACTION_COORDINATOR_KEY);
                        will(returnValue(Collections.emptyList()));

                        one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.COMMIT_STARTED)));
                        one(participant1).commitTransaction(TEST_TRANSACTION_ID, TEST_TRANSACTION_COORDINATOR_KEY);

                        if (throwException)
                        {
                            // test that a failing commit won't prevent other commits from being called
                            will(throwException(exception));
                        }

                        one(participant2).commitTransaction(TEST_TRANSACTION_ID, TEST_TRANSACTION_COORDINATOR_KEY);

                        if (!throwException)
                        {
                            one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.COMMIT_FINISHED)));
                        }
                        break;
                    case COMMIT_FINISHED:
                    case ROLLBACK_FINISHED:
                }
            }
        });

        coordinator.recoverTransactionsFromTransactionLog();
    }

    @Test
    public void testRecoverMultipleTransactions()
    {
        TransactionCoordinator coordinator =
                new TransactionCoordinator(TEST_TRANSACTION_COORDINATOR_KEY, TEST_INTERACTIVE_SESSION_KEY, sessionTokenProvider,
                        List.of(participant1, participant2), transactionLog, TEST_TIMEOUT, TEST_COUNT_LIMIT);

        TransactionLogEntry logEntry = new TransactionLogEntry();
        logEntry.setTransactionId(TEST_TRANSACTION_ID);
        logEntry.setTransactionStatus(TransactionStatus.PREPARE_FINISHED);

        TransactionLogEntry logEntry2 = new TransactionLogEntry();
        logEntry2.setTransactionId(TEST_TRANSACTION_ID_2);
        logEntry2.setTransactionStatus(TransactionStatus.COMMIT_STARTED);

        TransactionLogEntry logEntry3 = new TransactionLogEntry();
        logEntry3.setTransactionId(TEST_TRANSACTION_ID_3);
        logEntry3.setTransactionStatus(TransactionStatus.ROLLBACK_STARTED);

        TransactionLogEntry logEntry4 = new TransactionLogEntry();
        logEntry4.setTransactionId(TEST_TRANSACTION_ID_4);
        logEntry4.setTransactionStatus(TransactionStatus.ROLLBACK_STARTED);

        Map<UUID, TransactionLogEntry> logEntries =
                Map.of(logEntry.getTransactionId(), logEntry, logEntry2.getTransactionId(), logEntry2, logEntry3.getTransactionId(), logEntry3,
                        logEntry4.getTransactionId(), logEntry4);

        Exception exception = new RuntimeException();

        mockery.checking(new Expectations()
        {
            {
                allowing(participant1).getParticipantId();
                allowing(participant2).getParticipantId();

                one(transactionLog).getTransactions();
                will(returnValue(logEntries));

                // participant 1 (transactions 1, 2); participant 2 (transactions 1, 3)
                allowing(participant1).recoverTransactions(TEST_TRANSACTION_COORDINATOR_KEY);
                will(returnValue(Arrays.asList(TEST_TRANSACTION_ID, TEST_TRANSACTION_ID_2)));
                allowing(participant2).recoverTransactions(TEST_TRANSACTION_COORDINATOR_KEY);
                will(returnValue(Arrays.asList(TEST_TRANSACTION_ID, TEST_TRANSACTION_ID_3)));

                // recover transaction 1 (participant 1 and 2)
                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID, TransactionStatus.COMMIT_STARTED)));
                one(participant1).commitTransaction(TEST_TRANSACTION_ID, TEST_TRANSACTION_COORDINATOR_KEY);
                will(throwException(exception));
                one(participant2).commitTransaction(TEST_TRANSACTION_ID, TEST_TRANSACTION_COORDINATOR_KEY);

                // recover transaction 2 (only participant 1)
                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID_2, TransactionStatus.COMMIT_STARTED)));
                one(participant1).commitTransaction(TEST_TRANSACTION_ID_2, TEST_TRANSACTION_COORDINATOR_KEY);
                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID_2, TransactionStatus.COMMIT_FINISHED)));

                // recover transaction 3
                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID_3, TransactionStatus.ROLLBACK_STARTED)));
                one(participant1).rollbackTransaction(TEST_TRANSACTION_ID_3, TEST_TRANSACTION_COORDINATOR_KEY);
                one(participant2).rollbackTransaction(TEST_TRANSACTION_ID_3, TEST_TRANSACTION_COORDINATOR_KEY);
                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID_3, TransactionStatus.ROLLBACK_FINISHED)));

                // recover transaction 4
                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID_4, TransactionStatus.ROLLBACK_STARTED)));
                one(participant1).rollbackTransaction(TEST_TRANSACTION_ID_4, TEST_TRANSACTION_COORDINATOR_KEY);
                one(participant2).rollbackTransaction(TEST_TRANSACTION_ID_4, TEST_TRANSACTION_COORDINATOR_KEY);
                one(transactionLog).logTransaction(with(logEntry(TEST_TRANSACTION_ID_4, TransactionStatus.ROLLBACK_FINISHED)));
            }
        });

        coordinator.recoverTransactionsFromTransactionLog();
    }

    @DataProvider(name = "testRecoverTransactionWithStatus")
    public Object[][] provideTestRecoverTransactionWithStatus()
    {
        List<Object[]> statuses = new ArrayList<>();
        Arrays.stream(TransactionStatus.values()).forEach(s ->
        {
            statuses.add(new Object[] { s, false });
            statuses.add(new Object[] { s, true });
        });
        return statuses.toArray(new Object[0][0]);
    }

    private Matcher<TransactionLogEntry> logEntry(UUID transactionId, TransactionStatus transactionStatus)
    {
        return new TransactionLogEntryMatcher(transactionId, transactionStatus);
    }

}
