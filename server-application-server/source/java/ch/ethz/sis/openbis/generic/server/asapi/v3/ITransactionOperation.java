package ch.ethz.sis.openbis.generic.server.asapi.v3;

public interface ITransactionOperation
{

    String getOperationName();

    Object executeOperation() throws Throwable;

}
