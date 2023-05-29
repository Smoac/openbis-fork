package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.io.Serializable;
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

public class ApplicationServerApiClient
{

    private final ITransactionManager transactionManager;

    private final IApplicationServerApi applicationServerApi;

    private final IApplicationServerApi applicationServerApi2;

    private String sessionToken;

    private String sessionToken2;

    private String transactionId;

    public ApplicationServerApiClient(String applicationServerUrl, String applicationServerUrl2, long timeout)
    {
        transactionManager = HttpInvokerUtils.createServiceStub(ITransactionManager.class, applicationServerUrl + "/openbis/openbis"
                + ITransactionManager.SERVICE_URL, timeout);

        applicationServerApi = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, applicationServerUrl + "/openbis/openbis"
                + IApplicationServerApi.SERVICE_URL, timeout, new InvocationFactoryWithTransactionAttributes());

        applicationServerApi2 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, applicationServerUrl2 + "/openbis/openbis"
                + IApplicationServerApi.SERVICE_URL, timeout, new InvocationFactoryWithTransactionAttributes());
    }

    public void beginTransaction()
    {
        if (transactionId != null)
        {
            throw new IllegalStateException("Transaction has been already started");
        }
        transactionId = UUID.randomUUID().toString();
        transactionManager.beginTransaction(transactionId);
    }

    public void commitTransaction()
    {
        if (transactionId == null)
        {
            throw new IllegalStateException("Transaction hasn't started yet");
        }
        transactionManager.commitTransaction(transactionId);
    }

    public void rollbackTransaction()
    {
        if (transactionId == null)
        {
            throw new IllegalStateException("Transaction hasn't started yet");
        }
        transactionManager.rollbackTransaction(transactionId);
    }

    public void login(String userId, String password)
    {
        sessionToken = applicationServerApi.login(userId, password);
    }

    public void login2(String userId, String password)
    {
        sessionToken2 = applicationServerApi2.login(userId, password);
    }

    public List<SpacePermId> createSpaces(List<SpaceCreation> creations)
    {
        if (sessionToken == null)
        {
            throw new IllegalStateException("Session token is null. Please login.");
        }
        return applicationServerApi.createSpaces(sessionToken, creations);
    }

    public List<SpacePermId> createSpaces2(List<SpaceCreation> creations)
    {
        if (sessionToken2 == null)
        {
            throw new IllegalStateException("Session token is null. Please login.");
        }
        return applicationServerApi2.createSpaces(sessionToken2, creations);
    }

    public SearchResult<Space> searchSpaces(SpaceSearchCriteria criteria, SpaceFetchOptions fetchOptions)
    {
        if (sessionToken == null)
        {
            throw new IllegalStateException("Session token is null. Please login.");
        }
        return applicationServerApi.searchSpaces(sessionToken, criteria, fetchOptions);
    }

    public SearchResult<Space> searchSpaces2(SpaceSearchCriteria criteria, SpaceFetchOptions fetchOptions)
    {
        if (sessionToken2 == null)
        {
            throw new IllegalStateException("Session token is null. Please login.");
        }
        return applicationServerApi2.searchSpaces(sessionToken2, criteria, fetchOptions);
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
        ApplicationServerApiClient client = new ApplicationServerApiClient("http://localhost:8888", "http://localhost:9999", 10000000);
        client.beginTransaction();

        client.login("admin", "admin");
        client.login2("admin", "admin");

        SpaceCreation creation = new SpaceCreation();
        creation.setCode("2PT_TEST_A");
        client.createSpaces(List.of(creation));

        SpaceCreation creation2 = new SpaceCreation();
        creation2.setCode("2PT_TEST_A2");
        client.createSpaces2(List.of(creation2));

        SearchResult<Space> result = client.searchSpaces(new SpaceSearchCriteria(), new SpaceFetchOptions());
        System.out.println("SPACES: " + result.getObjects());

        result = client.searchSpaces2(new SpaceSearchCriteria(), new SpaceFetchOptions());
        System.out.println("SPACES 2: " + result.getObjects());

        client.rollbackTransaction();
    }

}
