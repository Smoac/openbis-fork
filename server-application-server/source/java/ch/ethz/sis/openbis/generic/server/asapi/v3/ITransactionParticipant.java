package ch.ethz.sis.openbis.generic.server.asapi.v3;

public interface ITransactionParticipant
{

    void beginTransaction(String transactionId, String transactionCoordinatorSecret) throws Throwable;

    Object executeOperation(String transactionId, String transactionCoordinatorSecret, ITransactionParticipantOperation operation) throws Throwable;

    void prepareTransaction(String transactionId, String transactionCoordinatorSecret) throws Throwable;

    void commitTransaction(String transactionId, String transactionCoordinatorSecret) throws Throwable;

    void rollbackTransaction(String transactionId, String transactionCoordinatorSecret) throws Throwable;

}
