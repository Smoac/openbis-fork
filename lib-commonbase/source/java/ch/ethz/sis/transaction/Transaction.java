package ch.ethz.sis.transaction;

import java.util.Date;
import java.util.UUID;

public class Transaction
{
    private UUID transactionId;

    private TransactionStatus transactionStatus;

    private String sessionToken;

    private Date lastAccessedDate = new Date();

    public Transaction(UUID transactionId, TransactionStatus transactionStatus)
    {
        this(transactionId, transactionStatus, null);
    }

    public Transaction(UUID transactionId, TransactionStatus transactionStatus, String sessionToken)
    {
        this.transactionId = transactionId;
        this.transactionStatus = transactionStatus;
        this.sessionToken = sessionToken;
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

    public String getSessionToken()
    {
        return sessionToken;
    }

    public void setSessionToken(final String sessionToken)
    {
        this.sessionToken = sessionToken;
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
