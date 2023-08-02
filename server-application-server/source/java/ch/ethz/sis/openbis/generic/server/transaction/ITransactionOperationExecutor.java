package ch.ethz.sis.openbis.generic.server.transaction;

public interface ITransactionOperationExecutor
{

    Object executeOperation(String sessionToken, String operationName, Object[] operationArguments) throws Throwable;

}
