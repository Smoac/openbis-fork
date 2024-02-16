package ch.ethz.sis.transaction;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class TransactionLogEntry
{

    private UUID transactionId;

    private TransactionStatus transactionStatus;

    private boolean isTwoPhaseTransaction;

    private Date lastAccessedDate;

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

    public boolean isTwoPhaseTransaction()
    {
        return isTwoPhaseTransaction;
    }

    public void setTwoPhaseTransaction(final boolean twoPhaseTransaction)
    {
        isTwoPhaseTransaction = twoPhaseTransaction;
    }

    public Date getLastAccessedDate()
    {
        return lastAccessedDate;
    }

    public void setLastAccessedDate(final Date lastAccessedDate)
    {
        this.lastAccessedDate = lastAccessedDate;
    }

    @Override public boolean equals(final Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final TransactionLogEntry that = (TransactionLogEntry) o;
        return isTwoPhaseTransaction() == that.isTwoPhaseTransaction() && Objects.equals(getTransactionId(), that.getTransactionId())
                && getTransactionStatus() == that.getTransactionStatus() && Objects.equals(getLastAccessedDate(), that.getLastAccessedDate());
    }

    @Override public int hashCode()
    {
        return Objects.hash(getTransactionId());
    }

    @Override public String toString()
    {
        return "TransactionLogEntry{" +
                "transactionId=" + transactionId +
                ", transactionStatus=" + transactionStatus +
                ", isTwoPhaseTransaction=" + isTwoPhaseTransaction +
                ", lastAccessedDate=" + lastAccessedDate +
                '}';
    }
}
