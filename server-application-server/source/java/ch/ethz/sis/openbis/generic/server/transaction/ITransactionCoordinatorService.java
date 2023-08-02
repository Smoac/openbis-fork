package ch.ethz.sis.openbis.generic.server.transaction;

import ch.systemsx.cisd.common.api.IRpcService;

public interface ITransactionCoordinatorService extends ITransactionCoordinator, IRpcService
{

    String SERVICE_NAME = "transaction-coordinator";

    String SERVICE_URL = "/rmi-" + SERVICE_NAME;

}
