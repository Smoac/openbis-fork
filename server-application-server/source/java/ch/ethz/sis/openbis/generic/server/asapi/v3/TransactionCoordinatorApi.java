package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.ITransactionCoordinatorApi;
import ch.ethz.sis.openbis.generic.asapi.v3.ITransactionParticipantApi;
import ch.ethz.sis.transaction.ISessionTokenProvider;
import ch.ethz.sis.transaction.ITransactionCoordinator;
import ch.ethz.sis.transaction.ITransactionParticipant;
import ch.ethz.sis.transaction.TransactionCoordinator;
import ch.ethz.sis.transaction.TransactionLog;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

@Component
public class TransactionCoordinatorApi implements ITransactionCoordinatorApi
{

    private static final String TRANSACTION_COORDINATOR_KEY = "test-transaction-coordinator-key";

    private static final String INTERACTIVE_SESSION_KEY = "test-interactive-session-key";

    private static final String TRANSACTION_LOG_PATH = "transaction-logs";

    private final ITransactionCoordinator transactionCoordinator;

    @Autowired
    public TransactionCoordinatorApi(final IApplicationServerApi applicationServerApi, final ITransactionParticipantApi participantApi)
    {
        List<ITransactionParticipant> participants =
                Arrays.asList(new ApplicationServerApiParticipant(ITransactionCoordinatorApi.APPLICATION_SERVER_PARTICIPANT_ID, participantApi));

        this.transactionCoordinator = new TransactionCoordinator(
                TRANSACTION_COORDINATOR_KEY,
                INTERACTIVE_SESSION_KEY,
                new ApplicationServerSessionTokenProvider(applicationServerApi),
                participants,
                new TransactionLog(new File(TRANSACTION_LOG_PATH)));
    }

    @Override public void beginTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        transactionCoordinator.beginTransaction(transactionId, sessionToken, interactiveSessionKey);
    }

    @Override public <T> T executeOperation(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
            final String participantId, final String operationName, final Object[] operationArguments)
    {
        return transactionCoordinator.executeOperation(transactionId, sessionToken, interactiveSessionKey, participantId, operationName,
                operationArguments);
    }

    @Override public void commitTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        transactionCoordinator.commitTransaction(transactionId, sessionToken, interactiveSessionKey);
    }

    @Override public void rollbackTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        transactionCoordinator.rollbackTransaction(transactionId, sessionToken, interactiveSessionKey);
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

        private final String participantId;

        private final ITransactionParticipantApi participantApi;

        public ApplicationServerApiParticipant(String participantId, ITransactionParticipantApi participantApi)
        {
            this.participantId = participantId;
            this.participantApi = participantApi;
        }

        @Override public String getParticipantId()
        {
            return participantId;
        }

        @Override public void beginTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
        {
            participantApi.beginTransaction(transactionId, sessionToken, interactiveSessionKey);
        }

        @Override public Object executeOperation(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String operationName, final Object[] operationArguments)
        {
            return participantApi.executeOperation(transactionId, sessionToken, interactiveSessionKey, operationName, operationArguments);
        }

        @Override public void prepareTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            participantApi.prepareTransaction(transactionId, sessionToken, interactiveSessionKey, transactionCoordinatorKey);
        }

        @Override public List<UUID> getTransactions(final String transactionCoordinatorKey)
        {
            return participantApi.getTransactions(transactionCoordinatorKey);
        }

        @Override public void commitTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
        {
            participantApi.commitTransaction(transactionId, sessionToken, interactiveSessionKey);
        }

        @Override public void commitTransaction(final UUID transactionId, final String transactionCoordinatorKey)
        {
            participantApi.commitTransaction(transactionId, transactionCoordinatorKey);
        }

        @Override public void rollbackTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
        {
            participantApi.rollbackTransaction(transactionId, sessionToken, interactiveSessionKey);
        }

        @Override public void rollbackTransaction(final UUID transactionId, final String transactionCoordinatorKey)
        {
            participantApi.rollbackTransaction(transactionId, transactionCoordinatorKey);
        }
    }

    private static class ApplicationServerSessionTokenProvider implements ISessionTokenProvider
    {

        private final IApplicationServerApi applicationServerApi;

        public ApplicationServerSessionTokenProvider(final IApplicationServerApi applicationServerApi)
        {
            this.applicationServerApi = applicationServerApi;
        }

        @Override public boolean isValid(final String sessionToken)
        {
            return applicationServerApi.isSessionActive(sessionToken);
        }
    }

}
