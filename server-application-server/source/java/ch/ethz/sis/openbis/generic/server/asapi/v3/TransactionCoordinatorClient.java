package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.util.List;
import java.util.UUID;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.transaction.ITransactionCoordinator;
import ch.ethz.sis.openbis.generic.server.transaction.ITransactionCoordinatorService;
import ch.ethz.sis.openbis.generic.server.transaction.ITransactionParticipantService;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

public class TransactionCoordinatorClient
{

    private final ITransactionCoordinator transactionCoordinator;

    private final IApplicationServerApi applicationServerApi;

    private UUID transactionId;

    private String sessionToken;

    private final String interactiveSessionKey;

    public TransactionCoordinatorClient(String transactionCoordinatorUrl, String applicationServerUrl, String interactiveSessionKey, long timeout)
    {
        if (interactiveSessionKey == null)
        {
            throw new IllegalArgumentException("Interactive session key cannot be null");
        }
        this.transactionCoordinator =
                HttpInvokerUtils.createServiceStub(ITransactionCoordinator.class, transactionCoordinatorUrl + "/openbis/openbis"
                        + ITransactionCoordinatorService.SERVICE_URL, timeout);
        this.applicationServerApi =
                HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, applicationServerUrl + "/openbis/openbis"
                        + IApplicationServerApi.SERVICE_URL, timeout);
        this.interactiveSessionKey = interactiveSessionKey;
    }

    public void login(String userId, String password)
    {
        String sessionToken = applicationServerApi.login(userId, password);

        if (sessionToken == null)
        {
            throw new RuntimeException("Incorrect user or password");
        } else
        {
            this.sessionToken = sessionToken;
        }
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
        transactionId = null;
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
        transactionId = null;
    }

    public ApplicationServerApiClient getApplicationServerApi()
    {
        if (transactionId == null)
        {
            throw new IllegalStateException("Transaction hasn't started yet");
        }

        if (sessionToken == null)
        {
            throw new IllegalStateException("Session token hasn't been set yet");
        }

        return new ApplicationServerApiClient(ITransactionParticipantService.PARTICIPANT_ID);
    }

    public ApplicationServerApiClient getApplicationServerApi2()
    {
        return new ApplicationServerApiClient(ITransactionParticipantService.PARTICIPANT_ID_2);
    }

    public class ApplicationServerApiClient
    {

        private final String participantId;

        public ApplicationServerApiClient(String participantId)
        {
            this.participantId = participantId;
        }

        @SuppressWarnings("unchecked")
        public List<SpacePermId> createSpaces(List<SpaceCreation> newSpaces)
        {
            if (transactionId == null)
            {
                throw new IllegalStateException("Transaction hasn't started yet");
            }

            if (sessionToken == null)
            {
                throw new IllegalStateException("Session token hasn't been set yet");
            }

            return (List<SpacePermId>) transactionCoordinator.executeOperation(transactionId, sessionToken, interactiveSessionKey,
                    participantId, "createSpaces", new Object[] { sessionToken, newSpaces });
        }

        @SuppressWarnings("unchecked")
        public SearchResult<Space> searchSpaces(SpaceSearchCriteria searchCriteria, SpaceFetchOptions fetchOptions)
        {
            if (transactionId == null)
            {
                throw new IllegalStateException("Transaction hasn't started yet");
            }

            if (sessionToken == null)
            {
                throw new IllegalStateException("Session token hasn't been set yet");
            }

            return (SearchResult<Space>) transactionCoordinator.executeOperation(transactionId, sessionToken, interactiveSessionKey,
                    participantId, "searchSpaces", new Object[] { sessionToken, searchCriteria, fetchOptions });
        }

    }

    public static void main(String[] args)
    {
        TransactionCoordinatorClient client =
                new TransactionCoordinatorClient("http://localhost:8888", "http://localhost:8888", "i_am_secret", 10000000);

        try
        {
            client.login("admin", "admin");

            client.beginTransaction();

            SpaceCreation creation = new SpaceCreation();
            creation.setCode("2PT_TEST_E");
            client.getApplicationServerApi().createSpaces(List.of(creation));

            SearchResult<Space> result = client.getApplicationServerApi().searchSpaces(new SpaceSearchCriteria(), new SpaceFetchOptions());
            System.out.println("SPACES: " + result.getObjects());

            client.commitTransaction();
        } catch (Exception e)
        {
            e.printStackTrace(System.out);
            client.rollbackTransaction();
        }
    }

}
