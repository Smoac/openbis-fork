package ch.systemsx.cisd.openbis.plugin.generic.server.batch;

import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Executes provided operation in batches of chosen size (by default 1000).
 * 
 * @author Izabela Adamczyk
 */
public class BatchOperationExecutor<S>
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, BatchOperationExecutor.class);

    private static final int DEFAULT_BATCH_SIZE = 1000;

    public void executeInBatches(IBatchOperation<S> strategy)
    {
        executeInBatches(strategy, DEFAULT_BATCH_SIZE);
    }

    public void executeInBatches(IBatchOperation<S> strategy, int batchSize)
    {
        assert strategy != null : "Unspecified operation.";

        final List<S> allEntities = strategy.getAllEntities();
        int startIndex = 0;
        int maxIndex = allEntities.size();
        while (startIndex < maxIndex)
        {
            final int endIndex = Math.min(startIndex + batchSize, maxIndex);
            final List<S> batch = allEntities.subList(startIndex, endIndex);
            strategy.execute(batch);
            operationLog.info(String.format("%s %s progress: %d/%d", strategy.getEntityName(),
                    strategy.getOperationName(), endIndex, maxIndex));
            startIndex += batchSize;
        }
    }
}