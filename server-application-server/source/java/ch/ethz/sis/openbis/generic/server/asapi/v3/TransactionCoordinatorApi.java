package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.afsclient.client.AfsClient;
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
    public TransactionCoordinatorApi(final TransactionConfiguration transactionConfiguration, IApplicationServerApi applicationServerApi,
            final ITransactionParticipantApi participantApi)
    {
        List<ITransactionParticipant> participants =
                Arrays.asList(participantApi, new DataStoreServerParticipant(transactionConfiguration.getDataStoreServerUrl()));

        this.transactionCoordinator = new TransactionCoordinator(
                transactionConfiguration.getCoordinatorKey(),
                transactionConfiguration.getInteractiveSessionKey(),
                new ApplicationServerSessionTokenProvider(applicationServerApi),
                participants,
                new TransactionLog(new File(transactionConfiguration.getTransactionLogFolderPath())));
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

    private static class DataStoreServerParticipant implements ITransactionParticipant
    {

        private final String dataStoreServerUrl;

        public DataStoreServerParticipant(String dataStoreServerUrl)
        {
            this.dataStoreServerUrl = dataStoreServerUrl;
        }

        @Override public String getParticipantId()
        {
            return ITransactionCoordinatorApi.DATASTORE_SERVER_PARTICIPANT_ID;
        }

        @Override public void beginTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            try
            {
                getDataStoreClient(transactionId, sessionToken, interactiveSessionKey, transactionCoordinatorKey).begin(transactionId);
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override public <T> T executeOperation(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String operationName,
                final Object[] operationArguments)
        {
            try
            {
                AfsClient afsClient = getDataStoreClient(transactionId, sessionToken, interactiveSessionKey, null);

                Object result = null;

                if (operationName.equals("list"))
                {
                    result = afsClient.list((String) operationArguments[0], (String) operationArguments[1], (Boolean) operationArguments[2]);
                } else if (operationName.equals("write"))
                {
                    result = afsClient.write((String) operationArguments[0], (String) operationArguments[1], (Long) operationArguments[2],
                            (byte[]) operationArguments[3], (byte[]) operationArguments[4]);
                } else
                {
                    throw new UnsupportedOperationException(operationName);
                }

                return (T) result;
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override public void prepareTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            try
            {
                getDataStoreClient(transactionId, sessionToken, interactiveSessionKey, transactionCoordinatorKey).prepare();
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override public void commitTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
        {
            try
            {
                getDataStoreClient(transactionId, sessionToken, interactiveSessionKey, null).commit();
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override public void commitTransaction(final UUID transactionId, final String transactionCoordinatorKey)
        {

        }

        @Override public void rollbackTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
        {
            try
            {
                getDataStoreClient(transactionId, sessionToken, interactiveSessionKey, null).rollback();
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override public void rollbackTransaction(final UUID transactionId, final String transactionCoordinatorKey)
        {

        }

        @Override public List<UUID> getTransactions(final String transactionCoordinatorKey)
        {
            return null;
        }

        private AfsClient getDataStoreClient(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            AfsClient afsClient = new AfsClient(URI.create(dataStoreServerUrl));
            afsClient.setSessionToken(sessionToken);
            afsClient.setInteractiveSessionKey(interactiveSessionKey);
            afsClient.setTransactionManagerKey(transactionCoordinatorKey);
            return afsClient;
        }

    }
}
