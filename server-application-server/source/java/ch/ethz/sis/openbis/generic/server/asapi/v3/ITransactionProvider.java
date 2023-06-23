package ch.ethz.sis.openbis.generic.server.asapi.v3;

public interface ITransactionProvider
{

    Object beginTransaction(String transactionId) throws Exception;

    void prepareTransaction(String transactionId, Object transaction) throws Exception;

    void rollbackTransaction(String transactionId, Object transaction, TransactionStatus transactionStatus) throws Exception;

    void commitTransaction(String transactionId, Object transaction) throws Exception;
}
