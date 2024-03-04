package ch.ethz.sis.transaction;

public class TransactionOperationException extends RuntimeException
{

    public TransactionOperationException(Exception cause)
    {
        super(cause);
    }

}
