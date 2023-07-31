package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.util.Map;
import java.util.UUID;

public interface ITransactionLog
{

    void logStatus(final UUID transactionId, TransactionStatus transactionStatus);

    Map<UUID, TransactionStatus> getLastStatuses();

}
