package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

public class TransactionLog implements ITransactionLog
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, TransactionLog.class);

    private final Map<String, TransactionStatus> statuses = new HashMap<>();

    @Override public void logStatus(final String transactionId, final TransactionStatus transactionStatus)
    {
        operationLog.info("Logging transaction: " + transactionId + " status: " + transactionStatus);
        statuses.put(transactionId, transactionStatus);
    }

    @Override public Map<String, TransactionStatus> getLastStatuses()
    {
        return Collections.unmodifiableMap(statuses);
    }

}
