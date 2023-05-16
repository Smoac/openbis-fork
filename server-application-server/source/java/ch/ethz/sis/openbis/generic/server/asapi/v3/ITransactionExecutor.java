package ch.ethz.sis.openbis.generic.server.asapi.v3;

public interface ITransactionExecutor
{

    void beginTransaction(String transactionId, String transactionManagerSecret) throws Throwable;

    Object executeOperation(String transactionId, String transactionManagerSecret, ITransactionOperation operation) throws Throwable;

    void prepareTransaction(String transactionId, String transactionManagerSecret) throws Throwable;

    void commitTransaction(String transactionId, String transactionManagerSecret) throws Throwable;

    void rollbackTransaction(String transactionId, String transactionManagerSecret) throws Throwable;

}
