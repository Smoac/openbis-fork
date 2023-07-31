package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

public class TransactionCoordinatorClient
{

    private final ITransactionCoordinator transactionCoordinator;

    private UUID transactionId;

    private String sessionToken;

    private final String interactiveSessionKey;

    public TransactionCoordinatorClient(String transactionCoordinatorUrl, String interactiveSessionKey, long timeout)
    {
        if (interactiveSessionKey == null)
        {
            throw new IllegalArgumentException("Interactive session key cannot be null");
        }
        this.transactionCoordinator =
                HttpInvokerUtils.createServiceStub(ITransactionCoordinator.class, transactionCoordinatorUrl + "/openbis/openbis"
                        + ITransactionCoordinator.SERVICE_URL, timeout);
        this.interactiveSessionKey = interactiveSessionKey;
    }

    public void login(String userId, String password)
    {
        sessionToken = getApplicationServerApi().login(userId, password);
    }

    public void beginTransaction()
    {
        if (sessionToken == null)
        {
            throw new IllegalStateException("Session token hasn't been set yet");
        }
        if (transactionId != null)
        {
            throw new IllegalStateException("Transaction has been already started");
        }
        transactionId = UUID.randomUUID();
        transactionCoordinator.beginTransaction(transactionId, sessionToken, interactiveSessionKey);
    }

    public void commitTransaction()
    {
        if (sessionToken == null)
        {
            throw new IllegalStateException("Session token hasn't been set yet");
        }
        if (transactionId == null)
        {
            throw new IllegalStateException("Transaction hasn't started yet");
        }
        transactionCoordinator.commitTransaction(transactionId, sessionToken, interactiveSessionKey);
    }

    public void rollbackTransaction()
    {
        if (sessionToken == null)
        {
            throw new IllegalStateException("Session token hasn't been set yet");
        }
        if (transactionId == null)
        {
            throw new IllegalStateException("Transaction hasn't started yet");
        }
        transactionCoordinator.rollbackTransaction(transactionId, sessionToken, interactiveSessionKey);
    }

    public IApplicationServerApi getApplicationServerApi()
    {
        return (IApplicationServerApi) Proxy.newProxyInstance(IApplicationServerApi.class.getClassLoader(),
                new Class[] { IApplicationServerApi.class },
                (proxy, method, args) -> transactionCoordinator.executeOperation(transactionId, sessionToken, interactiveSessionKey,
                        ITransactionCoordinator.PARTICIPANT_ID_APPLICATION_SERVER,
                        method.getName(),
                        args));
    }

    public IApplicationServerApi getApplicationServerApi2()
    {
        return (IApplicationServerApi) Proxy.newProxyInstance(IApplicationServerApi.class.getClassLoader(),
                new Class[] { IApplicationServerApi.class },
                (proxy, method, args) -> transactionCoordinator.executeOperation(transactionId, sessionToken, interactiveSessionKey,
                        ITransactionCoordinator.PARTICIPANT_ID_APPLICATION_SERVER_2,
                        method.getName(),
                        args));
    }

    public static void main(String[] args)
    {
        TransactionCoordinatorClient client = new TransactionCoordinatorClient("http://localhost:8888", "i_am_secret", 10000000);

        try
        {
            client.beginTransaction();

            String sessionToken = client.getApplicationServerApi().login("admin", "admin");
            String sessionToken2 = client.getApplicationServerApi2().login("admin", "admin");

            SpaceCreation creation = new SpaceCreation();
            creation.setCode("2PT_TEST_A");
            client.getApplicationServerApi().createSpaces(sessionToken, List.of(creation));

            SpaceCreation creation2 = new SpaceCreation();
            creation2.setCode("2PT_TEST_A2");
            client.getApplicationServerApi2().createSpaces(sessionToken2, List.of(creation2));

            SearchResult<Space> result =
                    client.getApplicationServerApi().searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions());
            System.out.println("SPACES: " + result.getObjects());

            result = client.getApplicationServerApi2().searchSpaces(sessionToken2, new SpaceSearchCriteria(), new SpaceFetchOptions());
            System.out.println("SPACES 2: " + result.getObjects());

            client.commitTransaction();
        } catch (Exception e)
        {
            client.rollbackTransaction();
        }
    }

}
