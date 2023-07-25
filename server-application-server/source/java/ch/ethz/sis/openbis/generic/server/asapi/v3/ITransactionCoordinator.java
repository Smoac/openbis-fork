package ch.ethz.sis.openbis.generic.server.asapi.v3;

import ch.systemsx.cisd.common.api.IRpcService;

public interface ITransactionCoordinator extends IRpcService
{

    String SERVICE_NAME = "transaction-coordinator";

    String SERVICE_URL = "/rmi-" + SERVICE_NAME;

    String JSON_SERVICE_URL = SERVICE_URL + ".json";

    String PARTICIPANT_ID_APPLICATION_SERVER = "application-server";

    String PARTICIPANT_ID_APPLICATION_SERVER_2 = "application-server-2";

    void beginTransaction(String transactionId);

    Object executeOperation(String transactionId, String participantId, String methodName, Object[] methodArguments);

    void commitTransaction(String transactionId);

    void rollbackTransaction(String transactionId);

}
