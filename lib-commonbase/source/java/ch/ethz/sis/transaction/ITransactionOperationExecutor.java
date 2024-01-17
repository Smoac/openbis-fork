package ch.ethz.sis.transaction;

public interface ITransactionOperationExecutor
{

    Object executeOperation(String sessionToken, String operationName, Object[] operationArguments) throws Throwable;

}
