package ch.ethz.sis.transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

public class TransactionParticipant implements ITransactionParticipant
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, TransactionParticipant.class);

    private final Map<UUID, Transaction> transactionMap = new ConcurrentHashMap<>();

    private final String participantId;

    private final String transactionCoordinatorKey;

    private final String interactiveSessionKey;

    private final ISessionTokenProvider sessionTokenProvider;

    private IDatabaseTransactionProvider databaseTransactionProvider;

    private final ITransactionOperationExecutor operationExecutor;

    private final ITransactionLog transactionLog;

    private final int transactionTimeoutInSeconds;

    private final int transactionCountLimit;

    public TransactionParticipant(String participantId, String transactionCoordinatorKey, String interactiveSessionKey,
            ISessionTokenProvider sessionTokenProvider, IDatabaseTransactionProvider databaseTransactionProvider,
            ITransactionOperationExecutor operationExecutor, ITransactionLog transactionLog, int transactionTimeoutInSeconds,
            int transactionCountLimit)
    {
        if (participantId == null)
        {
            throw new IllegalArgumentException("Participant id cannot be null");
        }

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

        if (databaseTransactionProvider == null)
        {
            throw new IllegalArgumentException("Database transaction provider cannot be null");
        }

        if (operationExecutor == null)
        {
            throw new IllegalArgumentException("Operation executor cannot be null");
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

        this.participantId = participantId;
        this.transactionCoordinatorKey = transactionCoordinatorKey;
        this.interactiveSessionKey = interactiveSessionKey;
        this.sessionTokenProvider = sessionTokenProvider;
        this.databaseTransactionProvider = databaseTransactionProvider;
        this.operationExecutor = operationExecutor;
        this.transactionLog = transactionLog;
        this.transactionTimeoutInSeconds = transactionTimeoutInSeconds;
        this.transactionCountLimit = transactionCountLimit;
    }

    @Override public String getParticipantId()
    {
        return participantId;
    }

    public void recoverTransactions()
    {
        operationLog.info("Started recovering transactions");

        for (TransactionLogEntry logEntry : transactionLog.getTransactions().values())
        {
            if (TransactionStatus.COMMIT_FINISHED.equals(logEntry.getTransactionStatus()) || TransactionStatus.ROLLBACK_FINISHED.equals(
                    logEntry.getTransactionStatus()))
            {
                continue;
            }

            Transaction existingTransaction = getTransaction(logEntry.getTransactionId());

            if (existingTransaction == null)
            {
                recoverTransactionFromTransactionLog(logEntry);
            } else
            {
                recoverFailedOrAbandonedTransaction(existingTransaction);
            }
        }

        operationLog.info("Finished recovering transactions");
    }

    private void recoverTransactionFromTransactionLog(TransactionLogEntry logEntry)
    {
        try
        {
            Transaction transaction = createTransaction(logEntry.getTransactionId(), logEntry.getTransactionStatus());
            transaction.setTwoPhaseTransaction(logEntry.isTwoPhaseTransaction());

            transaction.lockOrSkip(() ->
            {
                operationLog.info(
                        "Recovering transaction '" + transaction.getTransactionId() + "' found in the transaction log with last status '"
                                + transaction.getTransactionStatus() + "' .");

                switch (transaction.getTransactionStatus())
                {
                    case NEW:
                    case BEGIN_STARTED:
                    case PREPARE_STARTED:
                    case ROLLBACK_STARTED:
                        rollbackTransaction(transaction);
                        break;
                    case BEGIN_FINISHED:
                        if (!transaction.isTwoPhaseTransaction())
                        {
                            rollbackTransaction(transaction);
                        }
                        break;
                    case PREPARE_FINISHED:
                        // wait for the coordinator to decide whether to commit or rollback
                        break;
                    case COMMIT_STARTED:
                        commitTransaction(transaction);
                        break;
                    default:
                        throw new IllegalStateException(
                                "Transaction '" + transaction.getTransactionId() + "' has an unsupported last status '"
                                        + transaction.getTransactionStatus() + "'");
                }
            });
        } catch (Exception e)
        {
            operationLog.warn(
                    "Recovering transaction '" + logEntry.getTransactionId() + "' found in the transaction log with last status '"
                            + logEntry.getTransactionStatus() + "' has failed.",
                    e);
        }
    }

    private void recoverFailedOrAbandonedTransaction(Transaction transaction)
    {
        try
        {
            transaction.lockOrSkip(() ->
            {
                operationLog.info(
                        "Recovering failed or abandoned transaction '" + transaction.getTransactionId() + "' with last status '"
                                + transaction.getTransactionStatus()
                                + "'");

                switch (transaction.getTransactionStatus())
                {
                    case BEGIN_STARTED:
                    case PREPARE_STARTED:
                    case ROLLBACK_STARTED:
                        /*
                          If we are able to lock the transaction with the last state XXX_STARTED,
                          then XXX operation either failed in the middle or was unable to log XXX_FINISHED
                          state at the end. We can roll back the transaction without waiting for timeout.
                        */
                        rollbackTransaction(transaction);
                        break;
                    case NEW:
                        /*
                          If we are able to lock the transaction with the last state NEW then
                          either we have just created a new transaction and didn't lock it yet
                          or the transaction was unable to log BEGIN_STARTED status and failed.
                          To handle both cases correctly we should roll back after a timeout.
                         */
                    case BEGIN_FINISHED:
                        /*
                          The transaction in BEGIN_FINISHED state should be receiving operation executions.
                          If the operations are not coming then after a timeout we need to roll back.
                         */
                        if (transaction.hasTimedOut())
                        {
                            rollbackTransaction(transaction);
                        }
                        break;
                    case PREPARE_FINISHED:
                        // wait for the coordinator to decide whether to commit or rollback
                        break;
                    case COMMIT_STARTED:
                        commitTransaction(transaction);
                        break;
                }
            });
        } catch (Exception e)
        {
            operationLog.warn(
                    "Recovering failed or abandoned transaction '" + transaction.getTransactionId() + "' with last status '"
                            + transaction.getTransactionStatus() + "' has failed.", e);
        }
    }

    @Override public void beginTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
            final String transactionCoordinatorKey)
    {
        checkTransactionId(transactionId);
        checkSessionToken(sessionToken);
        checkInteractiveSessionKey(interactiveSessionKey);

        if (transactionCoordinatorKey != null)
        {
            checkTransactionCoordinatorKey(transactionCoordinatorKey);
        }

        Transaction transaction = createTransaction(transactionId, TransactionStatus.NEW);
        transaction.setTwoPhaseTransaction(transactionCoordinatorKey != null);

        transaction.lockOrFail(() ->
        {
            transaction.setTransactionStatus(TransactionStatus.BEGIN_STARTED);

            operationLog.info("Begin transaction '" + transactionId + "' started.");

            Object databaseTransaction = databaseTransactionProvider.beginTransaction(transactionId);
            transaction.setDatabaseTransaction(databaseTransaction);

            transaction.setTransactionStatus(TransactionStatus.BEGIN_FINISHED);

            operationLog.info("Begin transaction '" + transactionId + "' finished successfully.");

            return null;
        });
    }

    @Override public <T> T executeOperation(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
            final String operationName, final Object[] operationArguments)
    {
        checkTransactionId(transactionId);
        checkSessionToken(sessionToken);
        checkInteractiveSessionKey(interactiveSessionKey);
        checkOperationName(operationName);
        checkOperationArguments(operationArguments);

        Transaction transaction = getTransaction(transactionId);

        if (transaction == null)
        {
            throw new IllegalStateException("Transaction '" + transactionId + "' does not exist.");
        }

        return transaction.lockOrFail(() ->
        {
            checkTransactionStatus(transaction, TransactionStatus.BEGIN_FINISHED);

            operationLog.info("Transaction '" + transactionId + "' execute operation '" + operationName + "' started.");
            T result = operationExecutor.executeOperation(sessionToken, operationName, operationArguments);
            operationLog.info("Transaction '" + transactionId + "' execute operation '" + operationName + "' finished successfully.");

            return result;
        });
    }

    @Override public void prepareTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
            final String transactionCoordinatorKey)
    {
        checkTransactionId(transactionId);
        checkSessionToken(sessionToken);
        checkInteractiveSessionKey(interactiveSessionKey);
        checkTransactionCoordinatorKey(transactionCoordinatorKey);

        Transaction transaction = getTransaction(transactionId);

        if (transaction == null)
        {
            throw new IllegalStateException("Transaction '" + transactionId + "' does not exist.");
        }

        transaction.lockOrFail(() ->
        {
            checkTransactionStatus(transaction, TransactionStatus.BEGIN_FINISHED);

            if (!transaction.isTwoPhaseTransaction())
            {
                throw new IllegalStateException("Transaction '" + transactionId
                        + "' was started without transaction coordinator key, therefore calling prepare is not allowed.");
            }

            operationLog.info("Prepare transaction '" + transactionId + "' started.");

            transaction.setTransactionStatus(TransactionStatus.PREPARE_STARTED);
            databaseTransactionProvider.prepareTransaction(transactionId, transaction.getDatabaseTransaction());
            transaction.setTransactionStatus(TransactionStatus.PREPARE_FINISHED);

            operationLog.info("Prepare transaction '" + transactionId + "' finished successfully.");

            return null;
        });
    }

    @Override public List<UUID> recoverTransactions(final String transactionCoordinatorKey)
    {
        checkTransactionCoordinatorKey(transactionCoordinatorKey);

        operationLog.info("Started recovering transactions (triggered by the coordinator)");

        List<UUID> preparedTransactions = new ArrayList<>();

        for (TransactionLogEntry logEntry : transactionLog.getTransactions().values())
        {
            if (TransactionStatus.PREPARE_FINISHED.equals(logEntry.getTransactionStatus()))
            {
                Transaction transaction = getTransaction(logEntry.getTransactionId());

                if (transaction == null)
                {
                    transaction = createTransaction(logEntry.getTransactionId(), logEntry.getTransactionStatus());
                }

                preparedTransactions.add(transaction.getTransactionId());
            }
        }

        operationLog.info("Finished recovering transactions (triggered by the coordinator)");

        return preparedTransactions;
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

        commitTransaction(transaction);
    }

    @Override public void commitTransaction(final UUID transactionId, final String transactionCoordinatorKey)
    {
        checkTransactionId(transactionId);
        checkTransactionCoordinatorKey(transactionCoordinatorKey);

        Transaction transaction = getTransaction(transactionId);

        if (transaction == null)
        {
            throw new IllegalStateException("Transaction '" + transactionId + "' does not exist.");
        }

        commitTransaction(transaction);
    }

    private void commitTransaction(Transaction transaction)
    {
        transaction.lockOrFail(() ->
        {
            checkTransactionStatus(transaction, TransactionStatus.NEW, TransactionStatus.BEGIN_FINISHED, TransactionStatus.PREPARE_FINISHED);

            operationLog.info("Commit transaction '" + transaction.getTransactionId() + "' started.");

            if (transaction.getTransactionStatus() != TransactionStatus.NEW)
            {
                transaction.setTransactionStatus(TransactionStatus.COMMIT_STARTED);
                databaseTransactionProvider.commitTransaction(transaction.getTransactionId(), transaction.getDatabaseTransaction());
                transaction.setTransactionStatus(TransactionStatus.COMMIT_FINISHED);
            }

            closeTransaction(transaction);

            operationLog.info("Commit transaction '" + transaction.getTransactionId() + "' finished successfully.");

            return null;
        });
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

        rollbackTransaction(transaction);
    }

    @Override public void rollbackTransaction(final UUID transactionId, final String transactionCoordinatorKey)
    {
        checkTransactionId(transactionId);
        checkTransactionCoordinatorKey(transactionCoordinatorKey);

        Transaction transaction = getTransaction(transactionId);

        if (transaction == null)
        {
            return;
        }

        rollbackTransaction(transaction);
    }

    private void rollbackTransaction(Transaction transaction)
    {
        transaction.lockOrFail(() ->
        {
            checkTransactionStatus(transaction, TransactionStatus.NEW, TransactionStatus.BEGIN_STARTED,
                    TransactionStatus.BEGIN_FINISHED, TransactionStatus.PREPARE_STARTED,
                    TransactionStatus.PREPARE_FINISHED, TransactionStatus.COMMIT_STARTED);

            operationLog.info("Rollback transaction '" + transaction.getTransactionId() + "' started.");

            if (transaction.getTransactionStatus() != TransactionStatus.NEW)
            {
                transaction.setTransactionStatus(TransactionStatus.ROLLBACK_STARTED);
                databaseTransactionProvider.rollbackTransaction(transaction.getTransactionId(), transaction.getDatabaseTransaction());
                transaction.setTransactionStatus(TransactionStatus.ROLLBACK_FINISHED);
            }

            closeTransaction(transaction);

            operationLog.info("Rollback transaction '" + transaction.getTransactionId() + "' finished successfully.");

            return null;
        });
    }

    public boolean isRunningTransaction(UUID transactionId)
    {
        synchronized (transactionMap)
        {
            Transaction transaction = transactionMap.get(transactionId);
            return transaction != null;
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
        if (interactiveSessionKey == null)
        {
            throw new IllegalArgumentException("Interactive session key cannot be null");
        }

        if (!this.interactiveSessionKey.equals(interactiveSessionKey))
        {
            throw new IllegalArgumentException("Invalid interactive session key");
        }
    }

    private void checkTransactionCoordinatorKey(final String transactionCoordinatorKey)
    {
        if (transactionCoordinatorKey == null)
        {
            throw new IllegalArgumentException("Transaction coordinator key cannot be null");
        }

        if (!this.transactionCoordinatorKey.equals(transactionCoordinatorKey))
        {
            throw new IllegalArgumentException("Invalid transaction coordinator key");
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

    private void checkOperationName(final String operationName)
    {
        if (operationName == null)
        {
            throw new IllegalArgumentException("Operation name cannot be null");
        }
    }

    private void checkOperationArguments(final Object[] operationArguments)
    {
        if (operationArguments == null)
        {
            throw new IllegalArgumentException("Operation arguments cannot be null");
        }
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

    private void closeTransaction(Transaction transaction)
    {
        transaction.close();
        transactionMap.remove(transaction.getTransactionId());
    }

    public Map<UUID, Transaction> getTransactionMap()
    {
        return transactionMap;
    }

    private class Transaction extends ch.ethz.sis.transaction.Transaction
    {

        private Object databaseTransaction;

        private boolean isTwoPhaseTransaction;

        private final ExecutorService executor = Executors.newSingleThreadExecutor();

        private final ReentrantLock lock = new ReentrantLock();

        public Transaction(UUID transactionId, TransactionStatus initialTransactionStatus)
        {
            super(transactionId, initialTransactionStatus);
        }

        public void setTransactionStatus(final TransactionStatus transactionStatus)
        {
            TransactionLogEntry entry = new TransactionLogEntry();
            entry.setTransactionId(getTransactionId());
            entry.setTransactionStatus(transactionStatus);
            entry.setTwoPhaseTransaction(isTwoPhaseTransaction);
            entry.setLastAccessedDate(getLastAccessedDate());
            transactionLog.logTransaction(entry);

            super.setTransactionStatus(transactionStatus);
        }

        public Object getDatabaseTransaction()
        {
            return databaseTransaction;
        }

        public void setDatabaseTransaction(final Object databaseTransaction)
        {
            this.databaseTransaction = databaseTransaction;
        }

        public boolean isTwoPhaseTransaction()
        {
            return isTwoPhaseTransaction;
        }

        public void setTwoPhaseTransaction(final boolean twoPhaseTransaction)
        {
            isTwoPhaseTransaction = twoPhaseTransaction;
        }

        public boolean hasTimedOut()
        {
            return System.currentTimeMillis() - getLastAccessedDate().getTime() > transactionTimeoutInSeconds * 1000L;
        }

        public void close()
        {
            this.executor.shutdown();
        }

        public <T> T lockOrFail(Callable<T> action)
        {
            if (lock.tryLock())
            {
                try
                {
                    Future<T> future = executor.submit(action);
                    return future.get();
                } catch (ExecutionException e)
                {
                    Throwable originalException = e.getCause();
                    if (originalException instanceof RuntimeException)
                    {
                        throw (RuntimeException) originalException;
                    } else
                    {
                        throw new RuntimeException(originalException);
                    }
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                } finally
                {
                    setLastAccessedDate(new Date());
                    lock.unlock();
                }
            } else
            {
                throw new RuntimeException(
                        "Cannot execute a new action on transaction '" + getTransactionId() + "' as it is still busy executing a previous action.");
            }
        }

        public void lockOrSkip(Runnable action)
        {
            if (lock.tryLock())
            {
                try
                {
                    Future<?> future = executor.submit(action);
                    future.get();
                } catch (ExecutionException e)
                {
                    Throwable originalException = e.getCause();
                    if (originalException instanceof RuntimeException)
                    {
                        throw (RuntimeException) originalException;
                    } else
                    {
                        throw new RuntimeException(originalException);
                    }
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                } finally
                {
                    setLastAccessedDate(new Date());
                    lock.unlock();
                }
            } else
            {
                operationLog.info(
                        "Cannot execute a new action on transaction '" + getTransactionId() + "' as it is still busy executing a previous action.");
            }
        }
    }

    public IDatabaseTransactionProvider getDatabaseTransactionProvider()
    {
        return databaseTransactionProvider;
    }

    public void setDatabaseTransactionProvider(final IDatabaseTransactionProvider databaseTransactionProvider)
    {
        this.databaseTransactionProvider = databaseTransactionProvider;
    }

}
