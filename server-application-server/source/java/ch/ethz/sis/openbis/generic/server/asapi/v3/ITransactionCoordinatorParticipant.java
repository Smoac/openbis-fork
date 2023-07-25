package ch.ethz.sis.openbis.generic.server.asapi.v3;

public interface ITransactionCoordinatorParticipant
{

    String getParticipantId();

    void beginTransaction(final String transactionId);

    Object executeOperation(final String transactionId, final String methodName, Object[] methodArguments);

    void prepareTransaction(final String transactionId);

    void commitTransaction(final String transactionId);

    void rollbackTransaction(final String transactionId);

}
