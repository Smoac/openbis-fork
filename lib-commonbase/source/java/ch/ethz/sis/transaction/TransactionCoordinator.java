package ch.ethz.sis.transaction;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

public class TransactionCoordinator implements ITransactionCoordinator
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, TransactionCoordinator.class);

    private final String transactionCoordinatorKey;

    private final String interactiveSessionKey;

    private final ISessionTokenProvider sessionTokenProvider;

    private final List<ITransactionParticipant> participants;

    private final Map<UUID, Transaction> transactionMap = new ConcurrentHashMap<>();

    private final ITransactionLog transactionLog;

    private final int transactionTimeoutInSeconds;

    private final int transactionCountLimit;

    public TransactionCoordinator(final String transactionCoordinatorKey, final String interactiveSessionKey,
            final ISessionTokenProvider sessionTokenProvider, final List<ITransactionParticipant> participants, final ITransactionLog transactionLog,
            int transactionTimeoutInSeconds, int transactionCountLimit)
    {
        if (transactionCoordinatorKey == null)
        {
            throw new IllegalArgumentException("Transaction coordinator key cannot be null");
        }

        if (interactiveSessionKey == null)
        {
            throw new IllegalArgumentException("Interactive session key cannot be null");
        }

        if (sessionTokenProvider == null)
        {
            throw new IllegalArgumentException("Session token provider cannot be null");
        }

        if (participants == null || participants.isEmpty())
        {
            throw new IllegalArgumentException("Participants cannot be null or empty");
        }

        if (transactionLog == null)
        {
            throw new IllegalArgumentException("Transaction log cannot be null");
        }

        if (transactionTimeoutInSeconds <= 0)
        {
            throw new IllegalArgumentException("Transaction timeout cannot be <= 0");
        }

        if (transactionCountLimit <= 0)
        {
            throw new IllegalArgumentException("Transaction count cannot be <= 0");
        }

        this.transactionCoordinatorKey = transactionCoordinatorKey;
        this.interactiveSessionKey = interactiveSessionKey;
        this.sessionTokenProvider = sessionTokenProvider;
        this.participants = participants;
        this.transactionLog = transactionLog;
        this.transactionTimeoutInSeconds = transactionTimeoutInSeconds;
        this.transactionCountLimit = transactionCountLimit;
    }

    public void restoreTransactions()
    {
        operationLog.info("Starting restore of unfinished transactions");

        Map<UUID, TransactionStatus> lastStatuses = transactionLog.getLastStatuses();

        if (lastStatuses != null && !lastStatuses.isEmpty())
        {
            for (UUID transactionId : lastStatuses.keySet())
            {
                TransactionStatus lastStatus = lastStatuses.get(transactionId);

                if (TransactionStatus.COMMIT_FINISHED.equals(lastStatus) || TransactionStatus.ROLLBACK_FINISHED.equals(lastStatus))
                {
                    continue;
                }

                Transaction existingTransaction = getTransaction(transactionId);

                try
                {
                    if (existingTransaction == null)
                    {
                        operationLog.info(
                                "Restoring transaction '" + transactionId + "' with last status '" + lastStatus + "' from the transaction log.");

                        Transaction transaction = createTransaction(transactionId, lastStatus);

                        transaction.lockOrSkip(() ->
                        {
                            switch (lastStatus)
                            {
                                case BEGIN_STARTED:
                                case BEGIN_FINISHED:
                                case PREPARE_STARTED:
                                case ROLLBACK_STARTED:
                                case ROLLBACK_FAILED:
                                    rollbackTransaction(transaction, null, null, true);
                                    break;
                                case PREPARE_FINISHED:
                                case COMMIT_STARTED:
                                case COMMIT_FAILED:
                                    commitPreparedTransaction(transaction, null, null, true);
                                    break;
                                default:
                                    throw new IllegalStateException(
                                            "Transaction '" + transactionId + "' has an unsupported last status '" + lastStatus + "'");
                            }
                        });
                    } else
                    {
                        operationLog.info("Another attempt to finish transaction '" + transactionId + "' with last status '" + lastStatus + "'");

                        existingTransaction.lockOrSkip(() ->
                        {
                            if (TransactionStatus.COMMIT_FAILED.equals(lastStatus))
                            {

                                commitPreparedTransaction(existingTransaction, null, null, true);
                            } else if (TransactionStatus.ROLLBACK_FAILED.equals(lastStatus))
                            {
                                rollbackTransaction(existingTransaction, null, null, true);
                            }
                        });
                    }
                } catch (Exception e)
                {
                    if (existingTransaction == null)
                    {
                        operationLog.warn("Restore of transaction '" + transactionId + "' with last status '" + lastStatus + "' failed.", e);
                    } else
                    {
                        operationLog.warn(
                                "Another attempt to finish a transaction '" + transactionId + "' with last status '" + lastStatus + "' failed.");
                    }
                }
            }
        } else
        {
            operationLog.info("No unfinished transactions found in the transaction log");
        }

        operationLog.info("Finished restore of unfinished transactions");
    }

    public void cleanupTransactions()
    {
        operationLog.info("Starting cleanup of abandoned transactions");

        for (Transaction transaction : transactionMap.values())
        {
            transaction.lockOrSkip(() ->
            {
                boolean timedOut = System.currentTimeMillis() - transaction.getLastAccessedDate().getTime() > transactionTimeoutInSeconds * 1000L;

                if (timedOut && TransactionStatus.BEGIN_FINISHED.equals(transaction.getTransactionStatus()))
                {
                    try
                    {
                        operationLog.info("Cleaning up abandoned transaction '" + transaction.getTransactionId() + "' with last status '"
                                + transaction.getTransactionStatus() + "' and last accessed date '" + transaction.getLastAccessedDate() + "'.");
                        rollbackTransaction(transaction, null, null, false);
                    } catch (Exception e)
                    {
                        operationLog.warn("Cleanup of abandoned transaction '" + transaction.getTransactionId() + "' failed.", e);
                    }
                }
            });
        }

        operationLog.info("Finished cleanup of abandoned transactions");
    }

    @Override public void beginTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        checkTransactionId(transactionId);
        checkSessionToken(sessionToken);
        checkInteractiveSessionKey(interactiveSessionKey);

        Transaction transaction = createTransaction(transactionId, TransactionStatus.NEW);

        transaction.lockOrFail(() ->
        {
            transaction.setTransactionStatus(TransactionStatus.BEGIN_STARTED);

            operationLog.info("Begin transaction '" + transactionId + "' started.");

            for (ITransactionParticipant participant : participants)
            {
                try
                {
                    operationLog.info("Begin transaction '" + transactionId + "' for participant '" + participant.getParticipantId() + "'.");

                    participant.beginTransaction(transactionId, sessionToken, interactiveSessionKey, transactionCoordinatorKey);
                } catch (Exception e)
                {
                    operationLog.info(
                            "Begin transaction '" + transactionId + "' failed for participant '" + participant.getParticipantId() + "'.", e);

                    try
                    {
                        rollbackTransaction(transactionId, sessionToken, interactiveSessionKey);
                    } catch (Exception ignore)
                    {
                    }

                    throw e;
                }
            }

            transaction.setTransactionStatus(TransactionStatus.BEGIN_FINISHED);

            operationLog.info("Begin transaction '" + transactionId + "' finished successfully.");

            return null;
        });
    }

    @Override public <T> T executeOperation(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
            final String participantId, final String operationName, final Object[] operationArguments)
    {
        checkTransactionId(transactionId);
        checkSessionToken(sessionToken);
        checkInteractiveSessionKey(interactiveSessionKey);

        Transaction transaction = getTransaction(transactionId);

        if (transaction == null)
        {
            throw new IllegalStateException("Transaction '" + transactionId + "' does not exist.");
        }

        return transaction.lockOrFail(() ->
        {
            checkTransactionStatus(transaction, TransactionStatus.BEGIN_FINISHED);

            operationLog.info("Transaction '" + transactionId + "' execute operation '" + operationName + "' started.");

            for (ITransactionParticipant participant : participants)
            {
                if (Objects.equals(participant.getParticipantId(), participantId))
                {
                    T result =
                            participant.executeOperation(transactionId, sessionToken, interactiveSessionKey, operationName, operationArguments);

                    operationLog.info("Transaction '" + transactionId + "' execute operation '" + operationName + "' finished successfully.");

                    return result;
                }
            }

            throw new IllegalArgumentException("Unknown participant id: " + participantId);
        });
    }

    @Override public void commitTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        checkTransactionId(transactionId);
        checkSessionToken(sessionToken);
        checkInteractiveSessionKey(interactiveSessionKey);

        Transaction transaction = getTransaction(transactionId);

        if (transaction == null)
        {
            throw new IllegalStateException("Transaction '" + transactionId + "' does not exist.");
        }

        transaction.lockOrFail(() ->
        {
            checkTransactionStatus(transaction, TransactionStatus.BEGIN_FINISHED);

            operationLog.info("Commit transaction '" + transactionId + "' started.");

            prepareTransaction(transaction, sessionToken, interactiveSessionKey);

            try
            {
                commitPreparedTransaction(transaction, sessionToken, interactiveSessionKey, false);
            } catch (Exception ignore)
            {
                // do not throw the exception to the client as there is nothing it can do,
                // the commit will be retried automatically by the coordinator
            }

            operationLog.info("Commit transaction '" + transactionId + "' finished successfully.");

            return null;
        });
    }

    private void prepareTransaction(final Transaction transaction, final String sessionToken, final String interactiveSessionKey)
    {
        operationLog.info("Prepare transaction '" + transaction.getTransactionId() + "' started.");

        transaction.setTransactionStatus(TransactionStatus.PREPARE_STARTED);

        for (ITransactionParticipant participant : participants)
        {
            try
            {
                operationLog.info(
                        "Prepare transaction '" + transaction.getTransactionId() + "' for participant '" + participant.getParticipantId() + "'.");
                participant.prepareTransaction(transaction.getTransactionId(), sessionToken, interactiveSessionKey, transactionCoordinatorKey);
            } catch (Exception e)
            {
                operationLog.info(
                        "Prepare transaction '" + transaction.getTransactionId() + "' failed for participant '" + participant.getParticipantId()
                                + "'.", e);

                try
                {
                    rollbackTransaction(transaction.getTransactionId(), sessionToken, interactiveSessionKey);
                } catch (Exception ignore)
                {
                }

                operationLog.info("Prepare transaction '" + transaction.getTransactionId() + "' failed.");

                throw e;
            }
        }

        transaction.setTransactionStatus(TransactionStatus.PREPARE_FINISHED);

        operationLog.info("Prepare transaction '" + transaction.getTransactionId() + "' finished successfully.");
    }

    private void commitPreparedTransaction(Transaction transaction, final String sessionToken, final String interactiveSessionKey,
            final boolean restore)
    {
        operationLog.info("Commit prepared transaction '" + transaction.getTransactionId() + "' started.");

        transaction.setTransactionStatus(TransactionStatus.COMMIT_STARTED);

        RuntimeException exception = null;

        for (ITransactionParticipant participant : participants)
        {
            try
            {
                if (restore)
                {
                    List<UUID> transactions = participant.getTransactions(transactionCoordinatorKey);

                    if (transactions != null && transactions.contains(transaction.getTransactionId()))
                    {
                        operationLog.info(
                                "Commit prepared transaction '" + transaction.getTransactionId() + "' for participant '"
                                        + participant.getParticipantId() + "'.");
                        participant.commitTransaction(transaction.getTransactionId(), transactionCoordinatorKey);
                    } else
                    {
                        operationLog.info(
                                "Skipping commit of prepared transaction '" + transaction.getTransactionId() + "' for participant '"
                                        + participant.getParticipantId()
                                        + "'. The transaction has been already committed at that participant before.");
                    }
                } else
                {
                    operationLog.info(
                            "Commit prepared transaction '" + transaction.getTransactionId() + "' for participant '"
                                    + participant.getParticipantId()
                                    + "'.");
                    participant.commitTransaction(transaction.getTransactionId(), sessionToken, interactiveSessionKey);
                }
            } catch (RuntimeException e)
            {
                operationLog.error(
                        "Commit prepared transaction '" + transaction.getTransactionId() + "' failed for participant '"
                                + participant.getParticipantId() + "'.", e);
                if (exception == null)
                {
                    exception = e;
                }
            }
        }

        if (exception == null)
        {
            transaction.setTransactionStatus(TransactionStatus.COMMIT_FINISHED);
            transactionMap.remove(transaction.getTransactionId());
            operationLog.info("Commit prepared transaction '" + transaction.getTransactionId() + "' finished successfully.");
        } else
        {
            transaction.setTransactionStatus(TransactionStatus.COMMIT_FAILED);
            operationLog.info("Commit prepared transaction '" + transaction.getTransactionId() + "' failed.");
            throw exception;
        }
    }

    @Override public void rollbackTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        checkTransactionId(transactionId);
        checkSessionToken(sessionToken);
        checkInteractiveSessionKey(interactiveSessionKey);

        Transaction transaction = getTransaction(transactionId);

        if (transaction == null)
        {
            return;
        }

        transaction.lockOrFail(() ->
        {
            checkTransactionStatus(transaction, TransactionStatus.BEGIN_STARTED, TransactionStatus.BEGIN_FINISHED,
                    TransactionStatus.PREPARE_STARTED, TransactionStatus.PREPARE_FINISHED, TransactionStatus.ROLLBACK_STARTED,
                    TransactionStatus.ROLLBACK_FINISHED);

            try
            {
                rollbackTransaction(transaction, sessionToken, interactiveSessionKey, false);
            } catch (Exception ignore)
            {
                // do not throw the exception to the client as there is nothing it can do,
                // the rollback will be retried automatically by the coordinator
            }

            return null;
        });
    }

    private void rollbackTransaction(final Transaction transaction, final String sessionToken, final String interactiveSessionKey,
            final boolean restore)
    {
        operationLog.info("Rollback transaction '" + transaction.getTransactionId() + "' started.");

        transaction.setTransactionStatus(TransactionStatus.ROLLBACK_STARTED);

        RuntimeException exception = null;

        for (ITransactionParticipant participant : participants)
        {
            try
            {
                if (restore)
                {
                    List<UUID> transactions = participant.getTransactions(transactionCoordinatorKey);

                    if (transactions != null && transactions.contains(transaction.getTransactionId()))
                    {
                        operationLog.info(
                                "Rollback transaction '" + transaction.getTransactionId() + "' for participant '" + participant.getParticipantId()
                                        + "'.");
                        participant.rollbackTransaction(transaction.getTransactionId(), transactionCoordinatorKey);
                    } else
                    {
                        operationLog.info(
                                "Skipping rollback of transaction '" + transaction.getTransactionId() + "' for participant '"
                                        + participant.getParticipantId()
                                        + "'. The transaction has been already rolled back at that participant before.");
                    }
                } else
                {
                    operationLog.info(
                            "Rollback transaction '" + transaction.getTransactionId() + "' for participant '" + participant.getParticipantId()
                                    + "'.");
                    participant.rollbackTransaction(transaction.getTransactionId(), sessionToken, interactiveSessionKey);
                }
            } catch (RuntimeException e)
            {
                operationLog.info(
                        "Rollback transaction '" + transaction.getTransactionId() + "' failed for participant '" + participant.getParticipantId()
                                + "'.", e);
                if (exception == null)
                {
                    exception = e;
                }
            }
        }

        if (exception == null)
        {
            transaction.setTransactionStatus(TransactionStatus.ROLLBACK_FINISHED);
            transactionMap.remove(transaction.getTransactionId());
            operationLog.info("Rollback transaction '" + transaction.getTransactionId() + "' finished successfully.");
        } else
        {
            transaction.setTransactionStatus(TransactionStatus.ROLLBACK_FAILED);
            operationLog.info("Rollback transaction '" + transaction.getTransactionId() + "' failed.");
            throw exception;
        }
    }

    private void checkTransactionId(final UUID transactionId)
    {
        if (transactionId == null)
        {
            throw new IllegalArgumentException("Transaction id cannot be null");
        }
    }

    private void checkSessionToken(final String sessionToken)
    {
        if (sessionToken == null)
        {
            throw new IllegalArgumentException("Session token cannot be null");
        }

        if (!sessionTokenProvider.isValid(sessionToken))
        {
            throw new IllegalArgumentException("Invalid session token");
        }
    }

    private void checkInteractiveSessionKey(final String interactiveSessionKey)
    {
        if (!this.interactiveSessionKey.equals(interactiveSessionKey))
        {
            throw new IllegalArgumentException("Invalid interactive session key");
        }
    }

    private void checkTransactionStatus(final Transaction transaction, final TransactionStatus... expectedStatuses)
    {
        for (final TransactionStatus expectedStatus : expectedStatuses)
        {
            if (transaction.getTransactionStatus() == expectedStatus)
            {
                return;
            }
        }

        throw new IllegalStateException(
                "Transaction '" + transaction.getTransactionId() + "' unexpected status '" + transaction.getTransactionStatus()
                        + "'. Expected statuses '"
                        + Arrays.toString(expectedStatuses) + "'.");
    }

    private Transaction createTransaction(UUID transactionId, TransactionStatus initialTransactionStatus)
    {
        synchronized (transactionMap)
        {
            Transaction transaction = transactionMap.get(transactionId);

            if (transaction == null)
            {
                if (transactionMap.size() < transactionCountLimit)
                {
                    transaction = new Transaction(transactionId, initialTransactionStatus);
                    transactionMap.put(transactionId, transaction);
                    return transaction;
                } else
                {
                    throw new IllegalStateException(
                            "Cannot create transaction '" + transactionId + "' because transaction count limit (" + transactionCountLimit
                                    + ") has been reached.");
                }
            } else
            {
                throw new IllegalStateException("Transaction '" + transactionId + "' already exists.");
            }
        }
    }

    private Transaction getTransaction(UUID transactionId)
    {
        synchronized (transactionMap)
        {
            Transaction transaction = transactionMap.get(transactionId);

            if (transaction == null)
            {
                return null;
            } else
            {
                transaction.setLastAccessedDate(new Date());
                return transaction;
            }

        }
    }

    private class Transaction
    {

        private final UUID transactionId;

        private TransactionStatus transactionStatus;

        private Date lastAccessedDate = new Date();

        private final ReentrantLock lock = new ReentrantLock();

        public Transaction(UUID transactionId, TransactionStatus initialTransactionStatus)
        {
            this.transactionId = transactionId;
            this.transactionStatus = initialTransactionStatus;
        }

        public UUID getTransactionId()
        {
            return transactionId;
        }

        public TransactionStatus getTransactionStatus()
        {
            return transactionStatus;
        }

        public void setTransactionStatus(final TransactionStatus transactionStatus)
        {
            transactionLog.logStatus(transactionId, transactionStatus);
            this.transactionStatus = transactionStatus;
        }

        public <T> T lockOrFail(Callable<T> action)
        {
            if (lock.tryLock())
            {
                try
                {
                    return action.call();
                } catch (RuntimeException e)
                {
                    throw e;
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                } finally
                {
                    lock.unlock();
                }
            } else
            {
                throw new RuntimeException(
                        "Cannot execute a new action on transaction '" + transactionId + "' as it is still busy executing a previous action.");
            }
        }

        public void lockOrSkip(Runnable action)
        {
            if (lock.tryLock())
            {
                try
                {
                    action.run();
                } finally
                {
                    lock.unlock();
                }
            } else
            {
                operationLog.info(
                        "Cannot execute a new action on transaction '" + transactionId + "' as it is still busy executing a previous action.");
            }
        }

        public Date getLastAccessedDate()
        {
            return lastAccessedDate;
        }

        public void setLastAccessedDate(final Date lastAccessedDate)
        {
            this.lastAccessedDate = lastAccessedDate;
        }

    }

}
