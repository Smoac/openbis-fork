package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.util.Map;

public interface ITransactionCoordinatorLog
{

    void logStatus(final String transactionId, TransactionCoordinatorStatus transactionStatus);

    Map<String, TransactionCoordinatorStatus> getLastStatuses();

}
