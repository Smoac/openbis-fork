package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.afsapi.api.OperationsAPI;
import ch.ethz.sis.afsclient.client.AfsClient;
import ch.ethz.sis.openbis.generic.asapi.v3.ITransactionCoordinatorApi;
import ch.ethz.sis.openbis.generic.asapi.v3.ITransactionParticipantApi;
import ch.ethz.sis.transaction.ISessionTokenProvider;
import ch.ethz.sis.transaction.ITransactionLog;
import ch.ethz.sis.transaction.ITransactionParticipant;
import ch.ethz.sis.transaction.TransactionCoordinator;
import ch.ethz.sis.transaction.TransactionLog;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;

@Component
public class TransactionCoordinatorApi extends AbstractTransactionNodeApi implements ITransactionCoordinatorApi
{

    private static final String TRANSACTION_LOG_FOLDER_NAME = "coordinator";

    private final IApplicationServerInternalApi applicationServerApi;

    private final IOpenBisSessionManager sessionManager;

    private final String logFolderName;

    private TransactionCoordinator transactionCoordinator;

    @Autowired
    public TransactionCoordinatorApi(final TransactionConfiguration transactionConfiguration, IApplicationServerInternalApi applicationServerApi,
            IOpenBisSessionManager sessionManager)
    {
        this(transactionConfiguration, applicationServerApi, sessionManager, TRANSACTION_LOG_FOLDER_NAME);
    }

    public TransactionCoordinatorApi(final TransactionConfiguration transactionConfiguration, IApplicationServerInternalApi applicationServerApi,
            IOpenBisSessionManager sessionManager, String logFolderName)
    {
        super(transactionConfiguration);
        this.applicationServerApi = applicationServerApi;
        this.sessionManager = sessionManager;
        this.logFolderName = logFolderName;
    }

    @PostConstruct
    public void init()
    {
        if (transactionConfiguration.isEnabled())
        {
            List<ITransactionParticipant> participants = Arrays.asList(
                    new ApplicationServerParticipant(transactionConfiguration.getApplicationServerUrl(),
                            transactionConfiguration.getApplicationServerTimeoutInSeconds()),
                    new AfsServerParticipant(applicationServerApi, transactionConfiguration.getAfsServerUrl(),
                            transactionConfiguration.getAfsServerTimeoutInSeconds()));

            this.transactionCoordinator =
                    createCoordinator(transactionConfiguration.getCoordinatorKey(), transactionConfiguration.getInteractiveSessionKey(),
                            new ApplicationServerSessionTokenProvider(sessionManager), participants,
                            new TransactionLog(new File(transactionConfiguration.getTransactionLogFolderPath()), logFolderName),
                            transactionConfiguration.getTransactionTimeoutInSeconds(),
                            transactionConfiguration.getTransactionCountLimit());
        } else
        {
            this.transactionCoordinator = null;
        }
    }

    protected TransactionCoordinator createCoordinator(final String transactionCoordinatorKey, final String interactiveSessionKey,
            final ISessionTokenProvider sessionTokenProvider, final List<ITransactionParticipant> participants, final ITransactionLog transactionLog,
            int transactionTimeoutInSeconds, int transactionCountLimit)
    {
        return new TransactionCoordinator(
                transactionCoordinatorKey,
                interactiveSessionKey,
                sessionTokenProvider,
                participants,
                transactionLog, transactionTimeoutInSeconds, transactionCountLimit);
    }

    @Override public void recoverTransactionsFromTransactionLog()
    {
        checkTransactionsEnabled();
        transactionCoordinator.recoverTransactionsFromTransactionLog();
    }

    @Override public void finishFailedOrAbandonedTransactions()
    {
        checkTransactionsEnabled();
        transactionCoordinator.finishFailedOrAbandonedTransactions();
    }

    @Override public void beginTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        checkTransactionsEnabled();
        transactionCoordinator.beginTransaction(transactionId, sessionToken, interactiveSessionKey);
    }

    @Override public <T> T executeOperation(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
            final String participantId, final String operationName, final Object[] operationArguments)
    {
        checkTransactionsEnabled();
        return transactionCoordinator.executeOperation(transactionId, sessionToken, interactiveSessionKey, participantId, operationName,
                operationArguments);
    }

    @Override public void commitTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        checkTransactionsEnabled();
        transactionCoordinator.commitTransaction(transactionId, sessionToken, interactiveSessionKey);
    }

    @Override public void rollbackTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
    {
        checkTransactionsEnabled();
        transactionCoordinator.rollbackTransaction(transactionId, sessionToken, interactiveSessionKey);
    }

    public Map<UUID, TransactionCoordinator.Transaction> getTransactionMap()
    {
        checkTransactionsEnabled();
        return transactionCoordinator.getTransactionMap();
    }

    @Override public int getMajorVersion()
    {
        return 1;
    }

    @Override public int getMinorVersion()
    {
        return 0;
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

    private static class AfsServerParticipant implements ITransactionParticipant
    {

        private final IApplicationServerInternalApi applicationServerApi;

        private final String afsServerUrl;

        private final int timeoutInSeconds;

        public AfsServerParticipant(IApplicationServerInternalApi applicationServerApi, String afsServerUrl, int timeoutInSeconds)
        {
            this.applicationServerApi = applicationServerApi;
            this.afsServerUrl = afsServerUrl;
            this.timeoutInSeconds = timeoutInSeconds;
        }

        @Override public String getParticipantId()
        {
            return ITransactionCoordinatorApi.AFS_SERVER_PARTICIPANT_ID;
        }

        @Override public void beginTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            try
            {
                getAfsClient(transactionId, sessionToken, interactiveSessionKey, transactionCoordinatorKey).begin(transactionId);
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
                AfsClient afsClient = getAfsClient(transactionId, sessionToken, interactiveSessionKey, null);

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
                getAfsClient(transactionId, sessionToken, interactiveSessionKey, transactionCoordinatorKey).prepare();
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
                getAfsClient(transactionId, sessionToken, interactiveSessionKey, null).commit();
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

                AfsClient dataStoreServer = getAfsClient(transactionId, sessionToken, interactiveSessionKey, transactionCoordinatorKey);
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
                getAfsClient(transactionId, sessionToken, interactiveSessionKey, null).rollback();
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

                AfsClient dataStoreServer = getAfsClient(transactionId, sessionToken, interactiveSessionKey, transactionCoordinatorKey);
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
            String sessionToken = null;

            try
            {
                sessionToken = applicationServerApi.loginAsSystem();

                AfsClient dataStoreServer = getAfsClient(null, sessionToken, interactiveSessionKey, transactionCoordinatorKey);
                return dataStoreServer.recover();
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

        private AfsClient getAfsClient(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            AfsClient afsClient = new AfsClient(URI.create(afsServerUrl), timeoutInSeconds * 1000);
            afsClient.setSessionToken(sessionToken);
            afsClient.setInteractiveSessionKey(interactiveSessionKey);
            afsClient.setTransactionManagerKey(transactionCoordinatorKey);
            return afsClient;
        }

    }
}
