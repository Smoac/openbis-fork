package ch.ethz.sis.openbis.generic.server.asapi.v3;

public interface ITransactionOperationExecutor
{

    Object executeOperation(String sessionToken, String operationName, Object[] operationArguments) throws Throwable;

}
