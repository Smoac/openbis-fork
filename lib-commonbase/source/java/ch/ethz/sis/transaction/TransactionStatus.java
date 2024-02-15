package ch.ethz.sis.transaction;

public enum TransactionStatus
{
    NEW,
    BEGIN_STARTED(NEW),
    BEGIN_FINISHED(BEGIN_STARTED),
    PREPARE_STARTED(BEGIN_FINISHED),
    PREPARE_FINISHED(PREPARE_STARTED),
    COMMIT_STARTED(PREPARE_FINISHED),
    COMMIT_FINISHED(COMMIT_STARTED),
    ROLLBACK_STARTED(BEGIN_STARTED, BEGIN_FINISHED, PREPARE_STARTED, PREPARE_FINISHED, COMMIT_STARTED),
    ROLLBACK_FINISHED(ROLLBACK_STARTED);

    private final TransactionStatus[] previousStatuses;

    TransactionStatus(TransactionStatus... previousStatuses)
    {
        this.previousStatuses = previousStatuses;
    }

    public boolean isPreviousStatusOf(TransactionStatus status)
    {
        if (status == null)
        {
            throw new IllegalArgumentException("Status cannot be null");
        }

        if (status.previousStatuses != null)
        {
            for (TransactionStatus previousStatus : status.previousStatuses)
            {
                if (this == previousStatus || this.isPreviousStatusOf(previousStatus))
                {
                    return true;
                }
            }
        }

        return false;
    }
}
