package ch.ethz.sis.openbis.generic.server.asapi.v3;

import ch.systemsx.cisd.common.api.IRpcService;

public interface ITransactionCoordinator extends IRpcService
{

    /**
     * Name of this service for which it is registered at the RPC name server.
     */
    String SERVICE_NAME = "transaction-coordinator";

    /**
     * Application part of the URL to access this service remotely.
     */
    String SERVICE_URL = "/rmi-" + SERVICE_NAME;

    String JSON_SERVICE_URL = SERVICE_URL + ".json";

    void beginTransaction(String transactionId);

    void commitTransaction(String transactionId);

    void rollbackTransaction(String transactionId);
}
