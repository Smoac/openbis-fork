package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.util.Map;

public interface ITransactionLog
{

    void logStatus(final String transactionId, TransactionStatus transactionStatus);

    TransactionStatus getLastStatus(String transactionId);

    Map<String, TransactionStatus> getLastStatuses();

}
