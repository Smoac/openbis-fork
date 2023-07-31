package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.util.UUID;

import ch.systemsx.cisd.common.api.IRpcService;

public interface ITransactionCoordinator extends IRpcService
{

    String SERVICE_NAME = "transaction-coordinator";

    String SERVICE_URL = "/rmi-" + SERVICE_NAME;

    String JSON_SERVICE_URL = SERVICE_URL + ".json";

    String PARTICIPANT_ID_APPLICATION_SERVER = "application-server";

    String PARTICIPANT_ID_APPLICATION_SERVER_2 = "application-server-2";

    void beginTransaction(UUID transactionId, String sessionToken, String interactiveSessionKey);

    Object executeOperation(UUID transactionId, String sessionToken, String interactiveSessionKey, String participantId, String operationName,
            Object[] operationArguments);

    void commitTransaction(UUID transactionId, String sessionToken, String interactiveSessionKey);

    void rollbackTransaction(UUID transactionId, String sessionToken, String interactiveSessionKey);

}
