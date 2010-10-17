package ch.systemsx.cisd.openbis.generic.server.batch;

import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Executes provided operation in batches of chosen size (by default 1000).
 * 
 * @author Izabela Adamczyk
 */
public class BatchOperationExecutor
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            BatchOperationExecutor.class);

    private static final int DEFAULT_BATCH_SIZE = 1000;

    public static <S> void executeInBatches(IBatchOperation<S> strategy)
    {
        executeInBatches(strategy, DEFAULT_BATCH_SIZE);
    }

    public static <S> void executeInBatches(IBatchOperation<S> strategy, int batchSize)
    {
        assert strategy != null : "Unspecified operation.";

        final List<S> allEntities = strategy.getAllEntities();
        int maxIndex = allEntities.size();

        operationLog.info(getMemoryUsageMessage());
        // Loop over the list, one block at a time
        for (int startIndex = 0, endIndex = Math.min(startIndex + batchSize, maxIndex); startIndex < maxIndex; startIndex =
                endIndex, endIndex = Math.min(startIndex + batchSize, maxIndex))
        {
            final List<S> batch = allEntities.subList(startIndex, endIndex);
            strategy.execute(batch);
            operationLog.info(String.format("%s %s progress: %d/%d", strategy.getEntityName(),
                    strategy.getOperationName(), endIndex, maxIndex));
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(getMemoryUsageMessage());
            }
        }
    }

    private static String getMemoryUsageMessage()
    {
        Runtime runtime = Runtime.getRuntime();
        long mb = 1024l * 1024l;
        long totalMemory = runtime.totalMemory() / mb;
        long freeMemory = runtime.freeMemory() / mb;
        long maxMemory = runtime.maxMemory() / mb;
        return "MEMORY (in MB): free:" + freeMemory + " total:" + totalMemory + " max:" + maxMemory;
    }

}