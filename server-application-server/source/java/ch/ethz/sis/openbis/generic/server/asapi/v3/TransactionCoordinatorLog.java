package ch.ethz.sis.openbis.generic.server.asapi.v3;

public class TransactionCoordinatorLog implements ITransactionCoordinatorLog
{

    private final String folderPath;

    public TransactionCoordinatorLog(String folderPath)
    {
        this.folderPath = folderPath;
    }

    @Override public void beginTransactionStarted(final String transactionId)
    {

    }

    @Override public void beginTransactionFinished(final String transactionId)
    {

    }

    @Override public void commitTransactionStarted(final String transactionId)
    {

    }

    @Override public void commitTransactionPrepared(final String transactionId)
    {

    }

    @Override public void commitTransactionFinished(final String transactionId)
    {

    }

    @Override public void rollbackTransactionStarted(final String transactionId)
    {

    }

    @Override public void rollbackTransactionFinished(final String transactionId)
    {
        
    }
}
