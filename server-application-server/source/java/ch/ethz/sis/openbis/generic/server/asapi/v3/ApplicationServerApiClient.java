package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.util.Collections;
import java.util.List;
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

    private final IApplicationServerApiWithTransactions applicationServerApi;

    private String sessionToken;

    private String transactionId;

    public ApplicationServerApiClient(String applicationServerUrl, long timeout)
    {
        applicationServerApi =
                HttpInvokerUtils.createServiceStub(IApplicationServerApiWithTransactions.class, applicationServerUrl + "/openbis/openbis"
                        + IApplicationServerApi.SERVICE_URL, timeout, new DefaultRemoteInvocationFactory()
                {
                    @Override public RemoteInvocation createRemoteInvocation(final MethodInvocation methodInvocation)
                    {
                        RemoteInvocation remoteInvocation = super.createRemoteInvocation(methodInvocation);
                        remoteInvocation.setAttributes(Collections.singletonMap(TwoPhaseTransactionConst.TRANSACTION_ID_ATTRIBUTE, transactionId));
                        return remoteInvocation;
                    }
                });
    }

    public void login(String userId, String password)
    {
        sessionToken = applicationServerApi.login(userId, password);
    }

    public void beginTransaction()
    {
        if (transactionId != null)
        {
            throw new IllegalStateException("Transaction has been already started");
        }
        transactionId = UUID.randomUUID().toString();
        applicationServerApi.beginTransaction();
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

    public void commitTransaction()
    {
        if (transactionId == null)
        {
            throw new IllegalStateException("Transaction hasn't started yet");
        }
        applicationServerApi.commitTransaction();
    }

    public void rollbackTransaction()
    {
        if (transactionId == null)
        {
            throw new IllegalStateException("Transaction hasn't started yet");
        }
        applicationServerApi.rollbackTransaction();
    }

    private interface IApplicationServerApiWithTransactions extends IApplicationServerApi
    {
        void beginTransaction();

        void commitTransaction();

        void rollbackTransaction();
    }

    public static void main(String[] args)
    {
        ApplicationServerApiClient client = new ApplicationServerApiClient("http://localhost:8888", 10000000);
        client.beginTransaction();

        client.login("admin", "admin");

        SpaceCreation creation = new SpaceCreation();
        creation.setCode("2PT_TEST");
        client.createSpaces(List.of(creation));

        SearchResult<Space> result = client.searchSpaces(new SpaceSearchCriteria(), new SpaceFetchOptions());
        System.out.println(result.getObjects());

        client.commitTransaction();
    }

}
