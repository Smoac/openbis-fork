package ch.ethz.sis.openbis.generic.server.asapi.v3;

public interface ITransactionProvider
{

    Object beginTransaction(String transactionId) throws Exception;

    void prepareTransaction(String transactionId, Object transaction) throws Exception;

    void rollbackTransaction(String transactionId, Object transaction) throws Exception;

    void rollbackPreparedTransaction(String transactionId, Object transaction) throws Exception;

    void commitPreparedTransaction(String transactionId, Object transaction) throws Exception;
}
