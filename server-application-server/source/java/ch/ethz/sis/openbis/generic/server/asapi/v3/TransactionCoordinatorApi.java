package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.afsapi.api.OperationsAPI;
import ch.ethz.sis.afsclient.client.AfsClient;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.ITransactionCoordinatorApi;
import ch.ethz.sis.openbis.generic.asapi.v3.ITransactionParticipantApi;
import ch.ethz.sis.transaction.ISessionTokenProvider;
import ch.ethz.sis.transaction.ITransactionParticipant;
import ch.ethz.sis.transaction.TransactionCoordinator;
import ch.ethz.sis.transaction.TransactionLog;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

@Component
public class TransactionCoordinatorApi implements ITransactionCoordinatorApi
{

    private static final String TRANSACTION_LOG_FOLDER_NAME = "coordinator";

    private static final String FINISH_TRANSACTIONS_THREAD_NAME = "coordinator-finish-transactions";

    private final TransactionConfiguration transactionConfiguration;

    private final TransactionCoordinator transactionCoordinator;

    @Autowired
    public TransactionCoordinatorApi(final TransactionConfiguration transactionConfiguration, IApplicationServerInternalApi applicationServerApi)
    {
        List<ITransactionParticipant> participants = Arrays.asList(
                new ApplicationServerParticipant(transactionConfiguration.getApplicationServerUrl(),
                        transactionConfiguration.getApplicationServerTimeoutInSeconds()),
                new DataStoreServerParticipant(applicationServerApi, transactionConfiguration.getDataStoreServerUrl(),
                        transactionConfiguration.getDataStoreServerTimeoutInSeconds()));

        this.transactionConfiguration = transactionConfiguration;
        this.transactionCoordinator = new TransactionCoordinator(
                transactionConfiguration.getCoordinatorKey(),
                transactionConfiguration.getInteractiveSessionKey(),
                new ApplicationServerSessionTokenProvider(applicationServerApi),
                participants,
                new TransactionLog(new File(transactionConfiguration.getTransactionLogFolderPath()), TRANSACTION_LOG_FOLDER_NAME),
                transactionConfiguration.getTransactionTimeoutInSeconds(),
                transactionConfiguration.getTransactionCountLimit());
    }

    @PostConstruct
    public void init()
    {
        this.transactionCoordinator.recoverTransactionsFromTransactionLog();

        new Timer(FINISH_TRANSACTIONS_THREAD_NAME, true).schedule(new TimerTask()
        {
            @Override public void run()
            {
                TransactionCoordinatorApi.this.transactionCoordinator.finishFailedOrAbandonedTransactions();
            }
        }, transactionConfiguration.getFinishTransactionsIntervalInSeconds());
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

    private static class ApplicationServerParticipant implements ITransactionParticipant
    {

        private final ITransactionParticipantApi applicationServer;

        ApplicationServerParticipant(String applicationServerUrl, int timeoutInSeconds)
        {
            this.applicationServer =
                    HttpInvokerUtils.createServiceStub(ITransactionParticipantApi.class,
                            applicationServerUrl + "/openbis/openbis" + ITransactionParticipantApi.SERVICE_URL, timeoutInSeconds * 1000L);
        }

        @Override public String getParticipantId()
        {
            return getApplicationServer().getParticipantId();
        }

        @Override public void beginTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            getApplicationServer().beginTransaction(transactionId, sessionToken, interactiveSessionKey, transactionCoordinatorKey);
        }

        @Override public <T> T executeOperation(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String operationName,
                final Object[] operationArguments)
        {
            return getApplicationServer().executeOperation(transactionId, sessionToken, interactiveSessionKey, operationName, operationArguments);
        }

        @Override public void prepareTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            getApplicationServer().prepareTransaction(transactionId, sessionToken, interactiveSessionKey, transactionCoordinatorKey);
        }

        @Override public void commitTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
        {
            getApplicationServer().commitTransaction(transactionId, sessionToken, interactiveSessionKey);
        }

        @Override public void commitRecoveredTransaction(final UUID transactionId, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            getApplicationServer().commitRecoveredTransaction(transactionId, interactiveSessionKey, transactionCoordinatorKey);
        }

        @Override public void rollbackTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
        {
            getApplicationServer().rollbackTransaction(transactionId, sessionToken, interactiveSessionKey);
        }

        @Override public void rollbackRecoveredTransaction(final UUID transactionId, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            getApplicationServer().rollbackRecoveredTransaction(transactionId, interactiveSessionKey, transactionCoordinatorKey);
        }

        @Override public List<UUID> recoverTransactions(final String interactiveSessionKey, final String transactionCoordinatorKey)
        {
            return getApplicationServer().recoverTransactions(interactiveSessionKey, transactionCoordinatorKey);
        }

        private ITransactionParticipantApi getApplicationServer()
        {
            return applicationServer;
        }
    }

    private static class DataStoreServerParticipant implements ITransactionParticipant
    {

        private final IApplicationServerInternalApi applicationServerApi;

        private final String dataStoreServerUrl;

        private final int timeoutInSeconds;

        public DataStoreServerParticipant(IApplicationServerInternalApi applicationServerApi, String dataStoreServerUrl, int timeoutInSeconds)
        {
            this.applicationServerApi = applicationServerApi;
            this.dataStoreServerUrl = dataStoreServerUrl;
            this.timeoutInSeconds = timeoutInSeconds;
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
                getDataStoreServer(transactionId, sessionToken, interactiveSessionKey, transactionCoordinatorKey).begin(transactionId);
            } catch (RuntimeException e)
            {
                throw e;
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override public <T> T executeOperation(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String operationName, final Object[] operationArguments)
        {
            try
            {
                AfsClient afsClient = getDataStoreServer(transactionId, sessionToken, interactiveSessionKey, null);

                for (Method method : OperationsAPI.class.getDeclaredMethods())
                {
                    if (method.getName().equals(operationName) && method.getParameters().length == operationArguments.length)
                    {
                        return (T) method.invoke(afsClient, operationArguments);
                    }
                }

                throw new UnsupportedOperationException(operationName);
            } catch (InvocationTargetException e)
            {
                Throwable originalException = e.getTargetException();

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
            }
        }

        @Override public void prepareTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            try
            {
                getDataStoreServer(transactionId, sessionToken, interactiveSessionKey, transactionCoordinatorKey).prepare();
            } catch (RuntimeException e)
            {
                throw e;
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override public void commitTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
        {
            try
            {
                getDataStoreServer(transactionId, sessionToken, interactiveSessionKey, null).commit();
            } catch (RuntimeException e)
            {
                throw e;
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override public void commitRecoveredTransaction(final UUID transactionId, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            String sessionToken = null;

            try
            {
                sessionToken = applicationServerApi.loginAsSystem();

                AfsClient dataStoreServer = getDataStoreServer(transactionId, null, interactiveSessionKey, transactionCoordinatorKey);
                dataStoreServer.begin(transactionId);
                dataStoreServer.commit();
            } catch (RuntimeException e)
            {
                throw e;
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            } finally
            {
                if (sessionToken != null)
                {
                    applicationServerApi.logout(sessionToken);
                }
            }
        }

        @Override public void rollbackTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
        {
            try
            {
                getDataStoreServer(transactionId, sessionToken, interactiveSessionKey, null).rollback();
            } catch (RuntimeException e)
            {
                throw e;
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override public void rollbackRecoveredTransaction(final UUID transactionId, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            String sessionToken = null;

            try
            {
                sessionToken = applicationServerApi.loginAsSystem();

                AfsClient dataStoreServer = getDataStoreServer(transactionId, null, interactiveSessionKey, transactionCoordinatorKey);
                dataStoreServer.begin(transactionId);
                dataStoreServer.rollback();
            } catch (RuntimeException e)
            {
                throw e;
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            } finally
            {
                if (sessionToken != null)
                {
                    applicationServerApi.logout(sessionToken);
                }
            }
        }

        @Override public List<UUID> recoverTransactions(final String interactiveSessionKey, final String transactionCoordinatorKey)
        {
            try
            {
                return getDataStoreServer(null, null, interactiveSessionKey, transactionCoordinatorKey).recover();
            } catch (RuntimeException e)
            {
                throw e;
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        private AfsClient getDataStoreServer(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            AfsClient afsClient = new AfsClient(URI.create(dataStoreServerUrl), timeoutInSeconds * 1000);
            afsClient.setSessionToken(sessionToken);
            afsClient.setInteractiveSessionKey(interactiveSessionKey);
            afsClient.setTransactionManagerKey(transactionCoordinatorKey);
            return afsClient;
        }

    }
}
