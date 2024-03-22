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

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.afsapi.api.OperationsAPI;
import ch.ethz.sis.afsapi.exception.ThrowableReason;
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
            return ITransactionCoordinatorApi.APPLICATION_SERVER_PARTICIPANT_ID;
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
            executeAfsFunction(sessionToken, interactiveSessionKey, transactionCoordinatorKey, afsClient ->
            {
                afsClient.begin(transactionId);
                return null;
            });
        }

        @Override public <T> T executeOperation(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String operationName, final Object[] operationArguments)
        {
            return executeAfsFunction(sessionToken, interactiveSessionKey, null, afsClient ->
            {
                try
                {
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

                    if (originalException instanceof Exception)
                    {
                        throw (Exception) originalException;
                    } else
                    {
                        throw new RuntimeException(originalException);
                    }
                }
            });
        }

        @Override public void prepareTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            executeAfsFunction(sessionToken, interactiveSessionKey, transactionCoordinatorKey, afsClient ->
            {
                afsClient.prepare();
                return null;
            });
        }

        @Override public void commitTransaction(final UUID transactionId, final String sessionToken, final String interactiveSessionKey)
        {
            executeAfsFunction(sessionToken, interactiveSessionKey, null, afsClient ->
            {
                afsClient.commit();
                return null;
            });
        }

        @Override public void commitRecoveredTransaction(final UUID transactionId, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            String sessionToken = null;

            try
            {
                sessionToken = applicationServerApi.loginAsSystem();
                executeAfsFunction(sessionToken, interactiveSessionKey, transactionCoordinatorKey, afsClient ->
                {
                    afsClient.begin(transactionId);
                    afsClient.commit();
                    return null;
                });
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
            executeAfsFunction(sessionToken, interactiveSessionKey, null, afsClient ->
            {
                afsClient.rollback();
                return null;
            });
        }

        @Override public void rollbackRecoveredTransaction(final UUID transactionId, final String interactiveSessionKey,
                final String transactionCoordinatorKey)
        {
            String sessionToken = null;

            try
            {
                sessionToken = applicationServerApi.loginAsSystem();
                executeAfsFunction(sessionToken, interactiveSessionKey, transactionCoordinatorKey, afsClient ->
                {
                    afsClient.begin(transactionId);
                    afsClient.rollback();
                    return null;
                });
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
                return executeAfsFunction(sessionToken, interactiveSessionKey, transactionCoordinatorKey, AfsClient::recover);
            } finally
            {
                if (sessionToken != null)
                {
                    applicationServerApi.logout(sessionToken);
                }
            }
        }

        private <T> T executeAfsFunction(String sessionToken, String interactiveSessionKey, String transactionCoordinatorKey, AfsCall<T> call)
        {
            AfsClient afsClient = new AfsClient(URI.create(afsServerUrl), timeoutInSeconds * 1000);
            afsClient.setSessionToken(sessionToken);
            afsClient.setInteractiveSessionKey(interactiveSessionKey);
            afsClient.setTransactionManagerKey(transactionCoordinatorKey);

            try
            {
                return call.execute(afsClient);
            } catch (RuntimeException e)
            {
                if (e.getCause() instanceof ThrowableReason)
                {
                    ThrowableReason throwableReason = (ThrowableReason) e.getCause();
                    throw new RuntimeException(ReflectionToStringBuilder.toString(throwableReason.getReason()));
                }
                throw e;
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        private interface AfsCall<T>
        {
            T execute(AfsClient client) throws Exception;
        }

    }
}
