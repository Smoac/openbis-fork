package ch.ethz.sis.transaction;

import java.util.Map;
import java.util.UUID;

public interface ITransactionLog
{

    void logStatus(final UUID transactionId, TransactionStatus transactionStatus);

    Map<UUID, TransactionStatus> getLastStatuses();

}
