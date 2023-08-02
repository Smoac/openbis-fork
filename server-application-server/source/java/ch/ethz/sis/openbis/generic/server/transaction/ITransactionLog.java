package ch.ethz.sis.openbis.generic.server.transaction;

import java.util.Map;
import java.util.UUID;

public interface ITransactionLog
{

    void logStatus(final UUID transactionId, TransactionStatus transactionStatus);

    Map<UUID, TransactionStatus> getLastStatuses();

}
