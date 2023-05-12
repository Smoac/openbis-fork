package ch.ethz.sis.openbis.generic.server.asapi.v3;

public interface ITransactionOperationExecutor
{

    Object execute(String transactionId, String transactionManagerSecret, ITransactionOperation operation) throws Throwable;

}
