package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.util.UUID;

public interface IDatabaseTransactionProvider
{

    Object beginTransaction(UUID transactionId) throws Exception;

    void prepareTransaction(UUID transactionId, Object transaction) throws Exception;

    void rollbackTransaction(UUID transactionId, Object transaction) throws Exception;

    void commitTransaction(UUID transactionId, Object transaction) throws Exception;
}
