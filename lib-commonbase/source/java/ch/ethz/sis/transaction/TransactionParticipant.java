package ch.ethz.sis.transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

public class TransactionParticipant implements ITransactionParticipant
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, TransactionParticipant.class);

    private static final String BEGIN_TRANSACTION_METHOD = "beginTransaction";

    private static final String PREPARE_TRANSACTION_METHOD = "prepareTransaction";

    private static final String COMMIT_TRANSACTION_METHOD = "commitTransaction";

    private static final String ROLLBACK_TRANSACTION_METHOD = "rollbackTransaction";

    private final Map<UUID, TransactionThread> threadMap = new HashMap<>();

    private final String participantId;

    private final String transactionCoordinatorKey;

    private final String interactiveSessionKey;

    private final ISessionTokenProvider sessionTokenProvider;

    private final IDatabaseTransactionProvider databaseTransactionProvider;

    private final ITransactionOperationExecutor operationExecutor;

    private final ITransactionLog transactionLog;

    private final int transactionCountLimit;

    public TransactionParticipant(String participantId, String transactionCoordinatorKey, String interactiveSessionKey,
            ISessionTokenProvider sessionTokenProvider, IDatabaseTransactionProvider databaseTransactionProvider,
            ITransactionOperationExecutor operationExecutor, ITransactionLog transactionLog, int transactionCountLimit)
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
        this.transactionCountLimit = transactionCountLimit;
    }

    @Override public String getParticipantId()
    {
        return participantId;
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

        executeInTransactionThread(transactionId, sessionToken, interactiveSessionKey, transactionCoordinatorKey, BEGIN_TRANSACTION_METHOD, null);
    }

    @Override public <T> T executeOperation(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
            final String operationName, final Object[] operationArguments)
    {
        checkTransactionId(transactionId);
        checkSessionToken(sessionToken);
        checkInteractiveSessionKey(interactiveSessionKey);
        checkOperationName(operationName);
        checkOperationArguments(operationArguments);

        return (T) executeInTransactionThread(transactionId, sessionToken, interactiveSessionKey, null, operationName, operationArguments);
    }

    @Override public void prepareTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
            final String transactionCoordinatorKey)
    {
        checkTransactionId(transactionId);
        checkSessionToken(sessionToken);
        checkInteractiveSessionKey(interactiveSessionKey);
        checkTransactionCoordinatorKey(transactionCoordinatorKey);

        executeInTransactionThread(transactionId, sessionToken, interactiveSessionKey, transactionCoordinatorKey, PREPARE_TRANSACTION_METHOD, null);
    }

    @Override public List<UUID> getTransactions(final String transactionCoordinatorKey)
    {
        checkTransactionCoordinatorKey(transactionCoordinatorKey);

        Map<UUID, TransactionStatus> lastStatuses = transactionLog.getLastStatuses();

        List<UUID> preparedTransactions = new ArrayList<>();
        for (Map.Entry<UUID, TransactionStatus> lastStatusEntry : lastStatuses.entrySet())
        {
            if (TransactionStatus.PREPARE_FINISHED.equals(lastStatusEntry.getValue()))
            {
                preparedTransactions.add(lastStatusEntry.getKey());
            }
        }

        return preparedTransactions;
    }

    @Override public void commitTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        checkTransactionId(transactionId);
        checkSessionToken(sessionToken);
        checkInteractiveSessionKey(interactiveSessionKey);

        executeInTransactionThread(transactionId, sessionToken, interactiveSessionKey, null, COMMIT_TRANSACTION_METHOD, null);
    }

    @Override public void commitTransaction(final UUID transactionId, final String transactionCoordinatorKey)
    {
        checkTransactionId(transactionId);
        checkTransactionCoordinatorKey(transactionCoordinatorKey);

        executeInTransactionThread(transactionId, null, null, transactionCoordinatorKey, COMMIT_TRANSACTION_METHOD, null);
    }

    @Override public void rollbackTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        checkTransactionId(transactionId);
        checkSessionToken(sessionToken);
        checkInteractiveSessionKey(interactiveSessionKey);

        executeInTransactionThread(transactionId, sessionToken, interactiveSessionKey, null, ROLLBACK_TRANSACTION_METHOD, null);
    }

    @Override public void rollbackTransaction(final UUID transactionId, final String transactionCoordinatorKey)
    {
        checkTransactionId(transactionId);
        checkTransactionCoordinatorKey(transactionCoordinatorKey);

        executeInTransactionThread(transactionId, null, null, transactionCoordinatorKey, ROLLBACK_TRANSACTION_METHOD, null);
    }

    private Object executeInTransactionThread(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
            final String transactionCoordinatorKey, final String operationName, final Object[] operationArguments)
    {
        if (transactionId == null)
        {
            throw new IllegalArgumentException("Transaction id cannot be null");
        }

        TransactionThread thread;

        operationLog.info("Transaction '" + transactionId + "' execute operation '" + operationName + "' started.");

        synchronized (this)
        {
            thread = threadMap.get(transactionId);

            if (thread == null)
            {
                if (threadMap.size() >= transactionCountLimit)
                {
                    throw new RuntimeException(
                            "Cannot handle transaction '" + transactionId + "' as there are too many other transactions running already.");
                }

                operationLog.info("Creating a new thread for transaction '" + transactionId + "'.");

                thread = new TransactionThread(transactionId, interactiveSessionKey, transactionCoordinatorKey);
                threadMap.put(transactionId, thread);
                thread.startThread();
            } else
            {
                operationLog.info("Found existing thread for transaction '" + transactionId + "'.");
            }
        }

        try
        {
            Object result = thread.executeOperation(sessionToken, operationName, operationArguments);
            operationLog.info("Transaction '" + transactionId + "' execute operation '" + operationName + "' finished successfully.");
            return result;
        } catch (Exception e)
        {
            operationLog.info("Transaction '" + transactionId + "' execute operation '" + operationName + "' failed.", e);
            throw e;
        } finally
        {
            synchronized (this)
            {
                if (thread.isThreadFinished())
                {
                    operationLog.info("Transaction '" + transactionId + "' finished. Removing its thread.");
                    threadMap.remove(transactionId);
                }
            }
        }
    }

    public boolean isRunningTransaction(UUID transactionId)
    {
        synchronized (this)
        {
            TransactionThread thread = threadMap.get(transactionId);
            return thread != null && !thread.isThreadFinished();
        }
    }

    class TransactionThread
    {

        private final Object lock = new Object();

        private final Thread thread;

        private boolean threadFinished;

        private Object transactionObject;

        private TransactionStatus transactionStatus = TransactionStatus.NEW;

        private final UUID transactionId;

        private final String interactiveSessionKey;

        private final String transactionCoordinatorKey;

        private String sessionToken;

        private String operationName;

        private Object[] operationArguments;

        private Object operationResult;

        private Throwable operationException;

        public TransactionThread(UUID transactionId, String interactiveSessionKey, String transactionCoordinatorKey)
        {
            this.transactionId = transactionId;
            this.interactiveSessionKey = interactiveSessionKey;
            this.transactionCoordinatorKey = transactionCoordinatorKey;
            this.thread = new Thread(new Runnable()
            {
                @Override public void run()
                {
                    synchronized (lock)
                    {
                        while (true)
                        {
                            try
                            {
                                if (operationName != null)
                                {
                                    operationLog.info("Transaction '" + transactionId + "' thread executing operation '" + operationName + "'.");

                                    if (BEGIN_TRANSACTION_METHOD.equals(operationName))
                                    {
                                        checkTransactionStatus(transactionStatus, TransactionStatus.NEW);

                                        changeTransactionStatus(TransactionStatus.BEGIN_STARTED);
                                        operationResult = transactionObject = databaseTransactionProvider.beginTransaction(transactionId);
                                        changeTransactionStatus(TransactionStatus.BEGIN_FINISHED);
                                    } else if (PREPARE_TRANSACTION_METHOD.equals(operationName))
                                    {
                                        checkTransactionStatus(transactionStatus, TransactionStatus.BEGIN_FINISHED);

                                        if (transactionCoordinatorKey == null)
                                        {
                                            throw new IllegalStateException("Transaction " + transactionId
                                                    + " was started without transaction coordinator key, therefore calling prepare is not allowed.");
                                        }

                                        changeTransactionStatus(TransactionStatus.PREPARE_STARTED);
                                        databaseTransactionProvider.prepareTransaction(transactionId, transactionObject);
                                        changeTransactionStatus(TransactionStatus.PREPARE_FINISHED);
                                    } else if (COMMIT_TRANSACTION_METHOD.equals(operationName))
                                    {
                                        checkTransactionStatus(transactionStatus, TransactionStatus.NEW, TransactionStatus.BEGIN_FINISHED,
                                                TransactionStatus.PREPARE_FINISHED);

                                        if (transactionStatus != TransactionStatus.NEW)
                                        {
                                            changeTransactionStatus(TransactionStatus.COMMIT_STARTED);
                                            databaseTransactionProvider.commitTransaction(transactionId, transactionObject);
                                            changeTransactionStatus(TransactionStatus.COMMIT_FINISHED);
                                        }
                                    } else if (ROLLBACK_TRANSACTION_METHOD.equals(operationName))
                                    {
                                        checkTransactionStatus(transactionStatus, TransactionStatus.NEW, TransactionStatus.BEGIN_STARTED,
                                                TransactionStatus.BEGIN_FINISHED, TransactionStatus.PREPARE_STARTED,
                                                TransactionStatus.PREPARE_FINISHED, TransactionStatus.COMMIT_STARTED);

                                        if (transactionStatus != TransactionStatus.NEW)
                                        {
                                            changeTransactionStatus(TransactionStatus.ROLLBACK_STARTED);
                                            databaseTransactionProvider.rollbackTransaction(transactionId, transactionObject);
                                            changeTransactionStatus(TransactionStatus.ROLLBACK_FINISHED);
                                        }
                                    } else
                                    {
                                        checkTransactionStatus(transactionStatus, TransactionStatus.BEGIN_FINISHED);
                                        try
                                        {
                                            operationResult = operationExecutor.executeOperation(sessionToken, operationName, operationArguments);
                                        } catch (Throwable e)
                                        {
                                            throw new TransactionOperationException(e);
                                        }
                                    }

                                    operationName = null;
                                    operationArguments = null;
                                    operationException = null;

                                    if (transactionStatus == TransactionStatus.NEW || transactionStatus == TransactionStatus.COMMIT_FINISHED
                                            || transactionStatus == TransactionStatus.ROLLBACK_FINISHED)
                                    {
                                        threadFinished = true;
                                        return;
                                    }
                                } else
                                {
                                    operationLog.info("Transaction '" + transactionId + "' thread waiting for the next call.");
                                    lock.wait();
                                }

                            } catch (Throwable e)
                            {
                                operationName = null;
                                operationArguments = null;
                                operationResult = null;
                                operationException = e;

                                if (transactionStatus == TransactionStatus.NEW)
                                {
                                    threadFinished = true;
                                    return;
                                }
                            } finally
                            {
                                lock.notifyAll();
                            }
                        }
                    }
                }

                private void checkTransactionStatus(TransactionStatus actualStatus, TransactionStatus... expectedStatuses)
                {
                    for (final TransactionStatus expectedStatus : expectedStatuses)
                    {
                        if (actualStatus == expectedStatus)
                        {
                            return;
                        }
                    }

                    throw new IllegalStateException(
                            "Transaction '" + transactionId + "' unexpected status '" + actualStatus + "'. Expected statuses '"
                                    + Arrays.toString(expectedStatuses) + "'.");
                }

                private void changeTransactionStatus(TransactionStatus status)
                {
                    transactionLog.logStatus(transactionId, status);
                    TransactionThread.this.transactionStatus = status;
                }

            });
            this.thread.setName("Transaction-" + transactionId + "-participant-" + getParticipantId());
        }

        public void startThread()
        {
            operationLog.info("Transaction '" + transactionId + "' thread '" + thread.getName() + "' started.");
            thread.start();
        }

        public Object executeOperation(String newSessionToken, String newOperationName, Object[] newOperationArguments)
        {
            synchronized (lock)
            {
                try
                {
                    if (operationName != null)
                    {
                        throw new IllegalStateException(
                                "Cannot schedule transaction '" + transactionId + "' operation '" + newOperationName
                                        + "' execution as the previous operation '"
                                        + operationName + "' hasn't finished yet.");
                    }

                    sessionToken = newSessionToken;
                    operationName = newOperationName;
                    operationArguments = newOperationArguments;
                    operationResult = null;
                    operationException = null;
                    lock.notifyAll();

                    operationLog.info("Transaction '" + transactionId + "' thread scheduled operation '" + newOperationName + "' execution.");

                    while (operationName != null)
                    {
                        lock.wait();
                    }

                } catch (Exception e)
                {
                    operationLog.error(
                            "Scheduling of transaction '" + operationName + "' operation '" + operationName
                                    + "' execution in thread failed or got interrupted.", e);
                    throw new RuntimeException(e);
                }

                if (operationException != null)
                {
                    if (operationException instanceof TransactionOperationException)
                    {
                        throw (TransactionOperationException) operationException;
                    } else
                    {
                        throw new RuntimeException(operationException);
                    }
                } else
                {
                    return operationResult;
                }
            }
        }

        private boolean isThreadFinished()
        {
            return threadFinished;
        }

        public String getInteractiveSessionKey()
        {
            return interactiveSessionKey;
        }

        public String getTransactionCoordinatorKey()
        {
            return transactionCoordinatorKey;
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
}
