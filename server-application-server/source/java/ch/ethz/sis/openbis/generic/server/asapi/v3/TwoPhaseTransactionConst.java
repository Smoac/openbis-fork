package ch.ethz.sis.openbis.generic.server.asapi.v3;

public class TwoPhaseTransactionConst
{

    public static final String TRANSACTION_ID_ATTRIBUTE = "transactionId";

    public static final String BEGIN_TRANSACTION_METHOD = "beginTransaction";

    public static final String COMMIT_TRANSACTION_METHOD = "commitTransaction";

    public static final String ROLLBACK_TRANSACTION_METHOD = "rollbackTransaction";

    public static final int THREAD_COUNT_LIMIT = 10;
}
