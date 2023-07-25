package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.remoting.support.DefaultRemoteInvocationFactory;
import org.springframework.remoting.support.RemoteInvocation;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

public class TransactionCoordinatorClient
{

    private final ITransactionCoordinator transactionCoordinator;

    private String transactionId;

    public TransactionCoordinatorClient(String transactionCoordinatorServerUrl, long timeout)
    {
        transactionCoordinator =
                HttpInvokerUtils.createServiceStub(ITransactionCoordinator.class, transactionCoordinatorServerUrl + "/openbis/openbis"
                        + ITransactionCoordinator.SERVICE_URL, timeout);
    }

    public void beginTransaction()
    {
        if (transactionId != null)
        {
            throw new IllegalStateException("Transaction has been already started");
        }
        transactionId = UUID.randomUUID().toString();
        transactionCoordinator.beginTransaction(transactionId);
    }

    public void commitTransaction()
    {
        if (transactionId == null)
        {
            throw new IllegalStateException("Transaction hasn't started yet");
        }
        transactionCoordinator.commitTransaction(transactionId);
    }

    public void rollbackTransaction()
    {
        if (transactionId == null)
        {
            throw new IllegalStateException("Transaction hasn't started yet");
        }
        transactionCoordinator.rollbackTransaction(transactionId);
    }

    public IApplicationServerApi getApplicationServerApi()
    {
        return (IApplicationServerApi) Proxy.newProxyInstance(IApplicationServerApi.class.getClassLoader(),
                new Class[] { IApplicationServerApi.class },
                (proxy, method, args) -> transactionCoordinator.executeOperation(transactionId,
                        ITransactionCoordinator.PARTICIPANT_ID_APPLICATION_SERVER,
                        method.getName(),
                        args));
    }

    public IApplicationServerApi getApplicationServerApi2()
    {
        return (IApplicationServerApi) Proxy.newProxyInstance(IApplicationServerApi.class.getClassLoader(),
                new Class[] { IApplicationServerApi.class },
                (proxy, method, args) -> transactionCoordinator.executeOperation(transactionId,
                        ITransactionCoordinator.PARTICIPANT_ID_APPLICATION_SERVER_2,
                        method.getName(),
                        args));
    }

    private class InvocationFactoryWithTransactionAttributes extends DefaultRemoteInvocationFactory
    {
        @Override public RemoteInvocation createRemoteInvocation(final MethodInvocation methodInvocation)
        {
            Map<String, Serializable> attributes =
                    Collections.singletonMap(TransactionConst.TRANSACTION_ID_ATTRIBUTE, transactionId);

            RemoteInvocation remoteInvocation = super.createRemoteInvocation(methodInvocation);
            remoteInvocation.setAttributes(attributes);
            return remoteInvocation;
        }
    }

    public static void main(String[] args)
    {
        TransactionCoordinatorClient client = new TransactionCoordinatorClient("http://localhost:8888", 10000000);

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
