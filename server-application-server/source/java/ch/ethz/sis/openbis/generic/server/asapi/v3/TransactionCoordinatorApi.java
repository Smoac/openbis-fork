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

@Component
public class TransactionCoordinatorApi implements ITransactionCoordinatorApi
{

    private final ITransactionCoordinator transactionCoordinator;

    @Autowired
    public TransactionCoordinatorApi(final TransactionConfiguration transactionConfiguration, IApplicationServerApi applicationServerApi, final ITransactionParticipantApi participantApi)
    {
        List<ITransactionParticipant> participants = Arrays.asList(participantApi);

        this.transactionCoordinator = new TransactionCoordinator(
                transactionConfiguration.getCoordinatorKey(),
                transactionConfiguration.getInteractiveSessionKey(),
                new ApplicationServerSessionTokenProvider(applicationServerApi),
                participants,
                new TransactionLog(new File(transactionConfiguration.getLogFolderPath())));
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
