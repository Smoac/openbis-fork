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

    @Override public void beginTransaction(final String transactionId)
    {
        transactionLog.beginTransactionStarted(transactionId);

        for (int index = 0; index < participants.size(); index++)
        {
            try
            {
                participants.get(index).beginTransaction(transactionId);
            } catch (Exception beginException)
            {
                operationLog.warn(
                        "Transaction '" + transactionId + "' beginTransaction failed for participant '" + participants.get(index)
                                .getParticipantId() + "'", beginException);

                rollbackTransaction(transactionId, index);

                throw beginException;
            }
        }

        transactionLog.beginTransactionFinished(transactionId);
    }

    @Override public void commitTransaction(final String transactionId)
    {
        transactionLog.commitTransactionStarted(transactionId);

        for (ITransactionCoordinatorParticipant participant : participants)
        {
            try
            {
                participant.prepareTransaction(transactionId);
            } catch (Exception prepareException)
            {
                operationLog.warn(
                        "Transaction '" + transactionId + "' prepareTransaction failed for participant '" + participant.getParticipantId() + "'",
                        prepareException);

                rollbackTransaction(transactionId, null);

                throw prepareException;
            }
        }

        transactionLog.commitTransactionPrepared(transactionId);

        for (ITransactionCoordinatorParticipant participant : participants)
        {
            try
            {
                participant.commitTransaction(transactionId);
            } catch (Exception commitException)
            {
                operationLog.warn(
                        "Transaction '" + transactionId + "' commitTransaction failed for participant '" + participant.getParticipantId() + "'",
                        commitException);

                rollbackTransaction(transactionId, null);

                throw commitException;
            }
        }

        transactionLog.commitTransactionFinished(transactionId);
    }

    @Override public void rollbackTransaction(final String transactionId)
    {
        rollbackTransaction(transactionId, null);
    }

    private void rollbackTransaction(final String transactionId, final Integer toIndexOrNull)
    {
        transactionLog.rollbackTransactionStarted(transactionId);

        int toIndex = toIndexOrNull != null ? toIndexOrNull : participants.size() - 1;

        for (int index = 0; index <= toIndex; index++)
        {
            try
            {
                participants.get(index).rollbackTransaction(transactionId);
            } catch (Exception rollbackException)
            {
                operationLog.info(
                        "Transaction '" + transactionId + "' rollbackTransaction failed for participant '" + participants.get(index)
                                .getParticipantId() + "'", rollbackException);
            }
        }

        transactionLog.rollbackTransactionFinished(transactionId);
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
