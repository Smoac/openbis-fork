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

    private String sessionToken;

    private String transactionId;

    public ApplicationServerApiClient(String applicationServerUrl, long timeout)
    {
        transactionManager = HttpInvokerUtils.createServiceStub(ITransactionManager.class, applicationServerUrl + "/openbis/openbis"
                + ITransactionManager.SERVICE_URL, timeout);

        applicationServerApi = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, applicationServerUrl + "/openbis/openbis"
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

    public List<SpacePermId> createSpaces(List<SpaceCreation> creations)
    {
        if (sessionToken == null)
        {
            throw new IllegalStateException("Session token is null. Please login.");
        }
        return applicationServerApi.createSpaces(sessionToken, creations);
    }

    public SearchResult<Space> searchSpaces(SpaceSearchCriteria criteria, SpaceFetchOptions fetchOptions)
    {
        if (sessionToken == null)
        {
            throw new IllegalStateException("Session token is null. Please login.");
        }
        return applicationServerApi.searchSpaces(sessionToken, criteria, fetchOptions);
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
        ApplicationServerApiClient client = new ApplicationServerApiClient("http://localhost:8888", 10000000);
        client.beginTransaction();

        client.login("admin", "admin");

        SpaceCreation creation = new SpaceCreation();
        creation.setCode("2PT_TEST_5");
        client.createSpaces(List.of(creation));

        SearchResult<Space> result = client.searchSpaces(new SpaceSearchCriteria(), new SpaceFetchOptions());
        System.out.println(result.getObjects());

        client.commitTransaction();
    }

}
