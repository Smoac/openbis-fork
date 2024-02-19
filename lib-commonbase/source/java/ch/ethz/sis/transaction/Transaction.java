package ch.ethz.sis.transaction;

import java.util.Date;
import java.util.UUID;

public class Transaction
{
    private UUID transactionId;

    private TransactionStatus transactionStatus;

    private Date lastAccessedDate = new Date();

    public Transaction(UUID transactionId, TransactionStatus initialTransactionStatus)
    {
        this.transactionId = transactionId;
        this.transactionStatus = initialTransactionStatus;
    }

    public UUID getTransactionId()
    {
        return transactionId;
    }

    public void setTransactionId(final UUID transactionId)
    {
        this.transactionId = transactionId;
    }

    public TransactionStatus getTransactionStatus()
    {
        return transactionStatus;
    }

    public void setTransactionStatus(final TransactionStatus transactionStatus)
    {
        this.transactionStatus = transactionStatus;
    }

    public Date getLastAccessedDate()
    {
        return lastAccessedDate;
    }

    public void setLastAccessedDate(final Date lastAccessedDate)
    {
        this.lastAccessedDate = lastAccessedDate;
    }

}
