package ch.systemsx.cisd.openbis.plugin.generic.server.batch;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Executes provided operation in batches of chosen size.
 * 
 * @author Izabela Adamczyk
 */
public class BatchOperationExecutor<S>
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, BatchOperationExecutor.class);

    public void executeInBatches(IBatchOperation<S> strategy, int batchSize)
    {
        assert strategy != null : "Unspecified operation.";

        List<S> batch = new ArrayList<S>();
        int counter = 0;
        for (S entity : strategy.getAllEntities())
        {
            batch.add(entity);
            if (batch.size() >= batchSize)
            {
                strategy.execute(batch);
                counter += batch.size();
                operationLog.info(String.format("%s %s progress: %d/%d", strategy.getEntityName(),
                        strategy.getOperationName(), counter, strategy.getAllEntities().size()));
                batch.clear();
            }
        }
        if (batch.size() > 0)
        {
            strategy.execute(batch);
        }
    }
}