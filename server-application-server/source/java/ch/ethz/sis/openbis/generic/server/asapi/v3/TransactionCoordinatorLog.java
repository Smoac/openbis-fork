package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.util.Collections;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

public class TransactionCoordinatorLog implements ITransactionCoordinatorLog
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, TransactionCoordinatorLog.class);

    private final String folderPath;

    public TransactionCoordinatorLog(String folderPath)
    {
        this.folderPath = folderPath;
    }

    @Override public void logStatus(final String transactionId, final TransactionCoordinatorStatus transactionStatus)
    {
        operationLog.info("Logging transaction: " + transactionId + " status: " + transactionStatus);
    }

    @Override public Map<String, TransactionCoordinatorStatus> getLastStatuses()
    {
        return Collections.emptyMap();
    }

}
