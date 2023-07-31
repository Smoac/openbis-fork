package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

@Component
public class TransactionCoordinator implements ITransactionCoordinator
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, TransactionCoordinator.class);

    private static final String APPLICATION_SERVER_URL = "http://127.0.0.1:8888";

    private static final String APPLICATION_SERVER_URL_2 = "http://127.0.0.1:9999";

    private static final String TRANSACTION_LOG_PATH = "transaction-logs";

    private static final String TRANSACTION_COORDINATOR_KEY = "test-transaction-coordinator-key";

    private static final long TIMEOUT = 10000000;

    private List<ITransactionParticipant> participants = new ArrayList<>();

    private ITransactionLog transactionLog;

    public TransactionCoordinator()
    {
    }

    public TransactionCoordinator(final List<ITransactionParticipant> participants, final ITransactionLog transactionLog)
    {
        this.participants = Collections.unmodifiableList(participants);
        this.transactionLog = transactionLog;
    }

    @PostConstruct
    public void init()
    {
        this.participants = List.of(
                new ApplicationServerApiParticipant(APPLICATION_SERVER_URL, TIMEOUT),
                new ApplicationServerApiParticipant(APPLICATION_SERVER_URL_2, TIMEOUT));
        this.transactionLog = new TransactionLog(new File(TRANSACTION_LOG_PATH));
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
        operationLog.info("Execute operation '" + transactionId + "' started.");

        for (ITransactionParticipant participant : participants)
        {
            if (Objects.equals(participant.getParticipantId(), participantId))
            {
                Object result = participant.executeOperation(transactionId, sessionToken, interactiveSessionKey, operationName, operationArguments);

                operationLog.info("Execute operation '" + transactionId + "' finished.");

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
                participant.prepareTransaction(transactionId, sessionToken, interactiveSessionKey, TRANSACTION_COORDINATOR_KEY);
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
                    List<UUID> transactions = participant.getTransactions(TRANSACTION_COORDINATOR_KEY);

                    if (transactions != null && transactions.contains(transactionId))
                    {
                        operationLog.info(
                                "Commit prepared transaction '" + transactionId + "' for participant '" + participant.getParticipantId() + "'.");
                        participant.commitTransaction(transactionId, TRANSACTION_COORDINATOR_KEY);
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
                    List<UUID> transactions = participant.getTransactions(TRANSACTION_COORDINATOR_KEY);

                    if (transactions != null && transactions.contains(transactionId))
                    {
                        operationLog.info("Rollback transaction '" + transactionId + "' for participant '" + participant.getParticipantId() + "'.");
                        participant.rollbackTransaction(transactionId, TRANSACTION_COORDINATOR_KEY);
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

    @Override public int getMajorVersion()
    {
        return 1;
    }

    @Override public int getMinorVersion()
    {
        return 0;
    }

    private static class ApplicationServerApiParticipant implements ITransactionParticipant
    {

        private final String applicationServerUrl;

        private final ITransactionParticipant applicationServerApi;

        public ApplicationServerApiParticipant(String applicationServerUrl, long timeout)
        {
            this.applicationServerUrl = applicationServerUrl;
            this.applicationServerApi = HttpInvokerUtils.createServiceStub(ITransactionParticipant.class,
                    applicationServerUrl + "/openbis/openbis" + IApplicationServerApi.SERVICE_URL, timeout, null);
        }

        @Override public String getParticipantId()
        {
            return "ApplicationServer[" + applicationServerUrl + "]";
        }

        @Override public void beginTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
        {
            applicationServerApi.beginTransaction(transactionId, sessionToken, interactiveSessionKey);
        }

        @Override public Object executeOperation(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String operationName, final Object[] operationArguments)
        {
            return applicationServerApi.executeOperation(transactionId, sessionToken, interactiveSessionKey, operationName, operationArguments);
        }

        @Override public void prepareTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            applicationServerApi.prepareTransaction(transactionId, sessionToken, interactiveSessionKey, transactionCoordinatorKey);
        }

        @Override public List<UUID> getTransactions(final String transactionCoordinatorKey)
        {
            return applicationServerApi.getTransactions(transactionCoordinatorKey);
        }

        @Override public void commitTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
        {
            applicationServerApi.commitTransaction(transactionId, sessionToken, interactiveSessionKey);
        }

        @Override public void commitTransaction(final UUID transactionId, final String transactionCoordinatorKey)
        {
            applicationServerApi.commitTransaction(transactionId, transactionCoordinatorKey);
        }

        @Override public void rollbackTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
        {
            applicationServerApi.rollbackTransaction(transactionId, sessionToken, interactiveSessionKey);
        }

        @Override public void rollbackTransaction(final UUID transactionId, final String transactionCoordinatorKey)
        {
            applicationServerApi.rollbackTransaction(transactionId, transactionCoordinatorKey);
        }
    }

}
