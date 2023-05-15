package ch.ethz.sis.openbis.generic.server.asapi.v3;

public interface ITransactionOperationContext
{

    Object getTransaction(String transactionId) throws Exception;

    void prepareTransaction(String transactionId, Object transaction) throws Exception;

    void rollbackTransaction(String transactionId, Object transaction) throws Exception;

    void commitTransaction(String transactionId, Object transaction) throws Exception;
}
