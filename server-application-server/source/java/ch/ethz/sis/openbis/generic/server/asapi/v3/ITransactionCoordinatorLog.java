package ch.ethz.sis.openbis.generic.server.asapi.v3;

public interface ITransactionCoordinatorLog
{

    void beginTransactionStarted(final String transactionId);

    void beginTransactionFinished(final String transactionId);

    void commitTransactionStarted(final String transactionId);

    void commitTransactionPrepared(final String transactionId);

    void commitTransactionFinished(final String transactionId);

    void rollbackTransactionStarted(final String transactionId);

    void rollbackTransactionFinished(final String transactionId);

}
