package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.util.Map;

public interface ITransactionLog
{

    void logStatus(final String transactionId, TransactionStatus transactionStatus);

    Map<String, TransactionStatus> getLastStatuses();

}
