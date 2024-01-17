package ch.ethz.sis.transaction;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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

    private final ITransactionLog transactionLog;

    public TransactionCoordinator(final String transactionCoordinatorKey, final String interactiveSessionKey,
            final ISessionTokenProvider sessionTokenProvider, final List<ITransactionParticipant> participants, final ITransactionLog transactionLog)
    {
        this.transactionCoordinatorKey = transactionCoordinatorKey;
        this.interactiveSessionKey = interactiveSessionKey;
        this.sessionTokenProvider = sessionTokenProvider;
        this.participants = participants;
        this.transactionLog = transactionLog;
    }

    public void restoreTransactions()
    {
        Map<UUID, TransactionStatus> lastStatuses = transactionLog.getLastStatuses();

        if (lastStatuses != null && !lastStatuses.isEmpty())
        {
            for (UUID transactionId : lastStatuses.keySet())
            {
                TransactionStatus lastStatus = lastStatuses.get(transactionId);

                operationLog.info("Restoring transaction '" + transactionId + "' with last status '" + lastStatus + "'");

                try
                {
                    switch (lastStatus)
                    {
                        case BEGIN_STARTED:
                        case BEGIN_FINISHED:
                        case PREPARE_STARTED:
                        case ROLLBACK_STARTED:
                            rollbackTransaction(transactionId, null, null, true);
                            break;
                        case PREPARE_FINISHED:
                        case COMMIT_STARTED:
                            commitPreparedTransaction(transactionId, null, null, true);
                            break;
                        case COMMIT_FINISHED:
                        case ROLLBACK_FINISHED:
                            // nothing to do
                            break;
                        default:
                            operationLog.error(
                                    "Transaction '" + transactionId + "' restore failed because of an unknown transaction last status '" + lastStatus
                                            + "'");
                    }
                } catch (Exception e)
                {
                    operationLog.info("Restore of transaction '" + transactionId + "' with last status '" + lastStatus + "' failed.", e);
                }
            }
        }
    }

    @Override public void beginTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        operationLog.info("Begin transaction '" + transactionId + "' started.");

        transactionLog.logStatus(transactionId, TransactionStatus.BEGIN_STARTED);

        for (ITransactionParticipant participant : participants)
        {
            try
            {
                operationLog.info("Begin transaction '" + transactionId + "' for participant '" + participant.getParticipantId() + "'.");

                participant.beginTransaction(transactionId, sessionToken, interactiveSessionKey);
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

        transactionLog.logStatus(transactionId, TransactionStatus.BEGIN_FINISHED);

        operationLog.info("Begin transaction '" + transactionId + "' finished successfully.");
    }

    @Override public Object executeOperation(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
            final String participantId, final String operationName, final Object[] operationArguments)
    {
        operationLog.info("Transaction '" + transactionId + "' execute operation '" + operationName + "' started.");

        for (ITransactionParticipant participant : participants)
        {
            if (Objects.equals(participant.getParticipantId(), participantId))
            {
                Object result = participant.executeOperation(transactionId, sessionToken, interactiveSessionKey, operationName, operationArguments);

                operationLog.info("Transaction '" + transactionId + "' execute operation '" + operationName + "' finished successfully.");

                return result;
            }
        }

        throw new IllegalArgumentException("Unknown participant id: " + participantId);
    }

    @Override public void commitTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        operationLog.info("Commit transaction '" + transactionId + "' started.");

        prepareTransaction(transactionId, sessionToken, interactiveSessionKey);
        commitPreparedTransaction(transactionId, sessionToken, interactiveSessionKey, false);

        operationLog.info("Commit transaction '" + transactionId + "' finished successfully.");
    }

    private void prepareTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        operationLog.info("Prepare transaction '" + transactionId + "' started.");

        transactionLog.logStatus(transactionId, TransactionStatus.PREPARE_STARTED);

        for (ITransactionParticipant participant : participants)
        {
            try
            {
                operationLog.info("Prepare transaction '" + transactionId + "' for participant '" + participant.getParticipantId() + "'.");
                participant.prepareTransaction(transactionId, sessionToken, interactiveSessionKey, transactionCoordinatorKey);
            } catch (Exception e)
            {
                operationLog.info(
                        "Prepare transaction '" + transactionId + "' failed for participant '" + participant.getParticipantId() + "'.", e);

                try
                {
                    rollbackTransaction(transactionId, sessionToken, interactiveSessionKey);
                } catch (Exception ignore)
                {
                }

                operationLog.info("Prepare transaction '" + transactionId + "' failed.");

                throw e;
            }
        }

        transactionLog.logStatus(transactionId, TransactionStatus.PREPARE_FINISHED);

        operationLog.info("Prepare transaction '" + transactionId + "' finished successfully.");
    }

    private void commitPreparedTransaction(UUID transactionId, final String sessionToken, final String interactiveSessionKey, boolean restore)
    {
        operationLog.info("Commit prepared transaction '" + transactionId + "' started.");

        transactionLog.logStatus(transactionId, TransactionStatus.COMMIT_STARTED);

        RuntimeException exception = null;

        for (ITransactionParticipant participant : participants)
        {
            try
            {
                if (restore)
                {
                    List<UUID> transactions = participant.getTransactions(transactionCoordinatorKey);

                    if (transactions != null && transactions.contains(transactionId))
                    {
                        operationLog.info(
                                "Commit prepared transaction '" + transactionId + "' for participant '" + participant.getParticipantId() + "'.");
                        participant.commitTransaction(transactionId, transactionCoordinatorKey);
                    } else
                    {
                        operationLog.info(
                                "Skipping commit of prepared transaction '" + transactionId + "' for participant '" + participant.getParticipantId()
                                        + "'. The transaction has been already committed at that participant before.");
                    }
                } else
                {
                    operationLog.info(
                            "Commit prepared transaction '" + transactionId + "' for participant '" + participant.getParticipantId() + "'.");
                    participant.commitTransaction(transactionId, sessionToken, interactiveSessionKey);
                }
            } catch (RuntimeException e)
            {
                operationLog.error(
                        "Commit prepared transaction '" + transactionId + "' failed for participant '" + participant.getParticipantId() + "'.", e);
                if (exception == null)
                {
                    exception = e;
                }
            }
        }

        if (exception == null)
        {
            transactionLog.logStatus(transactionId, TransactionStatus.COMMIT_FINISHED);
            operationLog.info("Commit prepared transaction '" + transactionId + "' finished successfully.");
        } else
        {
            operationLog.info("Commit prepared transaction '" + transactionId + "' failed.");
            throw exception;
        }
    }

    @Override public void rollbackTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        rollbackTransaction(transactionId, sessionToken, interactiveSessionKey, false);
    }

    private void rollbackTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey, final boolean restore)
    {
        operationLog.info("Rollback transaction '" + transactionId + "' started.");

        transactionLog.logStatus(transactionId, TransactionStatus.ROLLBACK_STARTED);

        RuntimeException exception = null;

        for (ITransactionParticipant participant : participants)
        {
            try
            {
                if (restore)
                {
                    List<UUID> transactions = participant.getTransactions(transactionCoordinatorKey);

                    if (transactions != null && transactions.contains(transactionId))
                    {
                        operationLog.info("Rollback transaction '" + transactionId + "' for participant '" + participant.getParticipantId() + "'.");
                        participant.rollbackTransaction(transactionId, transactionCoordinatorKey);
                    } else
                    {
                        operationLog.info(
                                "Skipping rollback of transaction '" + transactionId + "' for participant '" + participant.getParticipantId()
                                        + "'. The transaction has been already rolled back at that participant before.");
                    }
                } else
                {
                    operationLog.info("Rollback transaction '" + transactionId + "' for participant '" + participant.getParticipantId() + "'.");
                    participant.rollbackTransaction(transactionId, sessionToken, interactiveSessionKey);
                }
            } catch (RuntimeException e)
            {
                operationLog.info("Rollback transaction '" + transactionId + "' failed for participant '" + participant.getParticipantId() + "'.", e);

                if (exception == null)
                {
                    exception = e;
                }
            }
        }

        if (exception == null)
        {
            transactionLog.logStatus(transactionId, TransactionStatus.ROLLBACK_FINISHED);
            operationLog.info("Rollback transaction '" + transactionId + "' finished successfully.");
        } else
        {
            operationLog.info("Rollback transaction '" + transactionId + "' failed.");
            throw exception;
        }
    }

}
