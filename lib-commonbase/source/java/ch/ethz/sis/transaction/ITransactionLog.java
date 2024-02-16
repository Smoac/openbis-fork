package ch.ethz.sis.transaction;

import java.util.Map;
import java.util.UUID;

public interface ITransactionLog
{

    void logTransaction(final TransactionLogEntry transaction);

    Map<UUID, TransactionLogEntry> getTransactions();

}
