package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.springframework.remoting.support.DefaultRemoteInvocationFactory;
import org.springframework.remoting.support.RemoteInvocation;
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

    private static final String TRANSACTION_LOGS_FOLDER_PATH = "transaction-logs";

    private static final String SECRET = "i_am_secret";

    private static final long TIMEOUT = 10000000;

    private List<ITransactionCoordinatorParticipant> participants = new ArrayList<>();

    private ITransactionCoordinatorLog transactionLog;

    public TransactionCoordinator()
    {
    }

    public TransactionCoordinator(final List<ITransactionCoordinatorParticipant> participants, final ITransactionCoordinatorLog transactionLog)
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
        this.transactionLog = new TransactionCoordinatorLog(TRANSACTION_LOGS_FOLDER_PATH);
    }

    public void restoreTransactions()
    {
        Map<String, TransactionCoordinatorStatus> lastStatuses = transactionLog.getLastStatuses();

        if (lastStatuses != null && !lastStatuses.isEmpty())
        {
            for (String transactionId : lastStatuses.keySet())
            {
                TransactionCoordinatorStatus lastStatus = lastStatuses.get(transactionId);

                operationLog.info("Restoring transaction '" + transactionId + "' with last status '" + lastStatus + "'");

                switch (lastStatus)
                {
                    case BEGIN_STARTED:
                    case BEGIN_FINISHED:
                    case COMMIT_STARTED:
                    case ROLLBACK_STARTED:
                        try
                        {
                            rollbackTransactionOnParticipants(transactionId, null);
                            transactionLog.logStatus(transactionId, TransactionCoordinatorStatus.ROLLBACK_FINISHED);
                        } catch (Exception e)
                        {
                            operationLog.info("Transaction '" + transactionId + "' restore of started rollback failed.", e);
                        }
                        break;
                    case COMMIT_PREPARED:
                        try
                        {
                            commitTransactionOnParticipants(transactionId);
                            transactionLog.logStatus(transactionId, TransactionCoordinatorStatus.COMMIT_FINISHED);
                        } catch (Exception e)
                        {
                            operationLog.info("Transaction '" + transactionId + "' restore of prepared commit failed.", e);
                        }
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
            }
        }
    }

    @Override public void beginTransaction(final String transactionId)
    {
        operationLog.info("Begin transaction '" + transactionId + "' started.");

        transactionLog.logStatus(transactionId, TransactionCoordinatorStatus.BEGIN_STARTED);
        beginTransactionOnParticipants(transactionId);
        transactionLog.logStatus(transactionId, TransactionCoordinatorStatus.BEGIN_FINISHED);

        operationLog.info("Begin transaction '" + transactionId + "' finished.");
    }

    private void beginTransactionOnParticipants(String transactionId)
    {
        for (int index = 0; index < participants.size(); index++)
        {
            ITransactionCoordinatorParticipant participant = participants.get(index);

            try
            {
                operationLog.info("Begin transaction '" + transactionId + "' for participant '" + participant.getParticipantId() + "'.");
                participant.beginTransaction(transactionId);
            } catch (Exception e)
            {
                operationLog.info(
                        "Begin transaction '" + transactionId + "' failed for participant '" + participant.getParticipantId() + "'.", e);

                transactionLog.logStatus(transactionId, TransactionCoordinatorStatus.ROLLBACK_STARTED);
                rollbackTransactionOnParticipants(transactionId, index);
                transactionLog.logStatus(transactionId, TransactionCoordinatorStatus.ROLLBACK_FINISHED);

                throw e;
            }
        }
    }

    @Override public void commitTransaction(final String transactionId)
    {
        operationLog.info("Commit transaction '" + transactionId + "' started.");

        transactionLog.logStatus(transactionId, TransactionCoordinatorStatus.COMMIT_STARTED);
        prepareTransactionOnParticipants(transactionId);
        transactionLog.logStatus(transactionId, TransactionCoordinatorStatus.COMMIT_PREPARED);
        commitTransactionOnParticipants(transactionId);
        transactionLog.logStatus(transactionId, TransactionCoordinatorStatus.COMMIT_FINISHED);

        operationLog.info("Commit transaction '" + transactionId + "' finished.");
    }

    private void prepareTransactionOnParticipants(String transactionId)
    {
        for (ITransactionCoordinatorParticipant participant : participants)
        {
            try
            {
                operationLog.info("Prepare transaction '" + transactionId + "' for participant '" + participant.getParticipantId() + "'.");
                participant.prepareTransaction(transactionId);
            } catch (Exception e)
            {
                operationLog.info(
                        "Prepare transaction '" + transactionId + "' failed for participant '" + participant.getParticipantId() + "'.", e);

                transactionLog.logStatus(transactionId, TransactionCoordinatorStatus.ROLLBACK_STARTED);
                rollbackTransactionOnParticipants(transactionId, null);
                transactionLog.logStatus(transactionId, TransactionCoordinatorStatus.ROLLBACK_FINISHED);

                throw e;
            }
        }
    }

    private void commitTransactionOnParticipants(String transactionId)
    {
        for (ITransactionCoordinatorParticipant participant : participants)
        {
            try
            {
                operationLog.info("Commit transaction '" + transactionId + "' for participant '" + participant.getParticipantId() + "'.");
                participant.commitTransaction(transactionId);
            } catch (Exception e)
            {
                operationLog.info(
                        "Commit transaction '" + transactionId + "' failed for participant '" + participant.getParticipantId() + "'.", e);

                transactionLog.logStatus(transactionId, TransactionCoordinatorStatus.ROLLBACK_STARTED);
                rollbackTransactionOnParticipants(transactionId, null);
                transactionLog.logStatus(transactionId, TransactionCoordinatorStatus.ROLLBACK_FINISHED);

                throw e;
            }
        }
    }

    @Override public void rollbackTransaction(final String transactionId)
    {
        operationLog.info("Rollback transaction '" + transactionId + "' started.");

        transactionLog.logStatus(transactionId, TransactionCoordinatorStatus.ROLLBACK_STARTED);
        rollbackTransactionOnParticipants(transactionId, null);
        transactionLog.logStatus(transactionId, TransactionCoordinatorStatus.ROLLBACK_FINISHED);

        operationLog.info("Rollback transaction '" + transactionId + "' finished.");
    }

    private void rollbackTransactionOnParticipants(final String transactionId, final Integer toIndexOrNull)
    {
        int toIndex = toIndexOrNull != null ? toIndexOrNull : participants.size() - 1;

        for (int index = 0; index <= toIndex; index++)
        {
            ITransactionCoordinatorParticipant participant = participants.get(index);

            try
            {
                operationLog.info("Rollback transaction '" + transactionId + "' for participant '" + participant.getParticipantId() + "'.");
                participant.rollbackTransaction(transactionId);
            } catch (Exception rollbackException)
            {
                operationLog.info(
                        "Transaction '" + transactionId + "' rollbackTransaction failed for participant '" + participants.get(index)
                                .getParticipantId() + "'", rollbackException);
            }
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

    private interface IApplicationServerApiWithTransactions extends IApplicationServerApi
    {
        void beginTransaction(String transactionId);

        void prepareTransaction(String transactionId);

        void commitTransaction(String transactionId);

        void rollbackTransaction(String transactionId);
    }

    private static class ApplicationServerApiParticipant implements ITransactionCoordinatorParticipant
    {

        private final String applicationServerUrl;

        private final IApplicationServerApiWithTransactions applicationServerApi;

        public ApplicationServerApiParticipant(String applicationServerUrl, long timeout)
        {
            this.applicationServerUrl = applicationServerUrl;
            this.applicationServerApi = HttpInvokerUtils.createServiceStub(IApplicationServerApiWithTransactions.class,
                    applicationServerUrl + "/openbis/openbis" + IApplicationServerApi.SERVICE_URL, timeout,
                    new InvocationFactoryWithTransactionAttributes());
        }

        @Override public String getParticipantId()
        {
            return "ApplicationServer[" + applicationServerUrl + "]";
        }

        @Override public void beginTransaction(final String transactionId)
        {
            applicationServerApi.beginTransaction(transactionId);
        }

        @Override public void prepareTransaction(final String transactionId)
        {
            applicationServerApi.prepareTransaction(transactionId);
        }

        @Override public void commitTransaction(final String transactionId)
        {
            applicationServerApi.commitTransaction(transactionId);
        }

        @Override public void rollbackTransaction(final String transactionId)
        {
            applicationServerApi.rollbackTransaction(transactionId);
        }
    }

    private static class InvocationFactoryWithTransactionAttributes extends DefaultRemoteInvocationFactory
    {
        @Override public RemoteInvocation createRemoteInvocation(final MethodInvocation methodInvocation)
        {
            String methodName = methodInvocation.getMethod().getName();

            if (TransactionConst.BEGIN_TRANSACTION_METHOD.equals(methodName)
                    || TransactionConst.PREPARE_TRANSACTION_METHOD.equals(methodName)
                    || TransactionConst.COMMIT_TRANSACTION_METHOD.equals(methodName)
                    || TransactionConst.ROLLBACK_TRANSACTION_METHOD.equals(methodName))
            {
                Map<String, Serializable> attributes = new HashMap<>();
                attributes.put(TransactionConst.TRANSACTION_ID_ATTRIBUTE, (String) methodInvocation.getArguments()[0]);
                attributes.put(TransactionConst.TRANSACTION_MANAGER_SECRET_ATTRIBUTE, SECRET);

                RemoteInvocation remoteInvocation = super.createRemoteInvocation(methodInvocation);
                remoteInvocation.setAttributes(attributes);
                return remoteInvocation;
            } else
            {
                throw new IllegalArgumentException(
                        "Only transaction management calls are allowed. Tried to call " + methodName + " method.");
            }
        }
    }

}
