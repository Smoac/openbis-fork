/*
 * Copyright 2013 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.common.multiplexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.base.namedthread.NamingThreadPoolExecutor;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.ITerminableFuture;
import ch.systemsx.cisd.common.concurrent.TerminableCallable.INamedCallable;
import ch.systemsx.cisd.common.concurrent.TerminableCallable.IStoppableExecutor;

/**
 * @author pkupczyk
 */
public class ThreadPoolMultiplexer implements IMultiplexer
{
    private final NamingThreadPoolExecutor executor;

    public ThreadPoolMultiplexer(String threadPoolName)
    {
        this.executor = new NamingThreadPoolExecutor(threadPoolName).daemonize();
    }

    @Override
    public <O, I, R> BatchesResults<R> process(final List<? extends O> objects,
            final IBatchIdProvider<O, I> batchIdProvider, final IBatchHandler<O, I, R> batchHandler)
    {
        List<IBatch<O, I>> batches = createBatches(objects, batchIdProvider);
        validateBatches(batches, batchHandler);
        List<ITerminableFuture<List<R>>> futures = submitBatches(batches, batchHandler);
        return gatherResults(futures);
    }

    public static <O, I, R> List<IBatch<O, I>> createBatches(final List<? extends O> objects,
            final IBatchIdProvider<O, I> batchIdProvider)
    {
        Map<I, List<O>> batchIdToObjectsMap = createBatchIdToObjectsMap(objects, batchIdProvider);
        List<IBatch<O, I>> batches = new ArrayList<IBatch<O, I>>();

        for (Map.Entry<I, List<O>> batchIdToObjectsMapEntry : batchIdToObjectsMap.entrySet())
        {
            IBatch<O, I> batch =
                    new Batch<O, I>(batchIdToObjectsMapEntry.getKey(),
                            batchIdToObjectsMapEntry.getValue());
            batches.add(batch);
        }

        return batches;
    }

    public static <O, I, R> Map<I, List<O>> createBatchIdToObjectsMap(
            final List<? extends O> objects, final IBatchIdProvider<O, I> batchIdProvider)
    {
        Map<I, List<O>> batchIdToObjectsMap = new HashMap<I, List<O>>();

        if (objects != null)
        {
            for (O object : objects)
            {
                if (object != null)
                {
                    I batchId = batchIdProvider.getBatchId(object);
                    if (batchId != null)
                    {
                        List<O> objectsForBatchId = batchIdToObjectsMap.get(batchId);
                        if (objectsForBatchId == null)
                        {
                            objectsForBatchId = new ArrayList<O>();
                            batchIdToObjectsMap.put(batchId, objectsForBatchId);
                        }
                        objectsForBatchId.add(object);
                    }
                }
            }
        }

        return batchIdToObjectsMap;
    }

    private <O, I, R> void validateBatches(
            final List<IBatch<O, I>> batches, final IBatchHandler<O, I, R> batchHandler)
    {
        for (final IBatch<O, I> batch : batches)
        {
            batchHandler.validateBatch(batch);
        }
    }

    private <O, I, R> List<ITerminableFuture<List<R>>> submitBatches(
            final List<IBatch<O, I>> batches, final IBatchHandler<O, I, R> batchHandler)
    {
        List<ITerminableFuture<List<R>>> futures = new ArrayList<ITerminableFuture<List<R>>>();

        final long startTime = System.currentTimeMillis();

        for (final IBatch<O, I> batch : batches)
        {
            ITerminableFuture<List<R>> future =
                    ConcurrencyUtilities.submit(executor, new INamedCallable<List<R>>()
                        {
                            @Override
                            public List<R> call(IStoppableExecutor<List<R>> stoppableExecutor)
                                    throws Exception
                            {
                                return batchHandler.processBatch(batch);
                            }

                            @Override
                            public String getCallableName()
                            {
                                return batch.getId() + "(" + startTime + ")";
                            }
                        });
            futures.add(future);
        }

        return futures;
    }

    private <R> BatchesResults<R> gatherResults(final List<ITerminableFuture<List<R>>> futures)
    {
        BatchesResults<R> batchesResults = new BatchesResults<R>();

        try
        {
            for (ITerminableFuture<List<R>> future : futures)
            {
                List<R> results = ConcurrencyUtilities.tryGetResult(future, -1);
                if (results != null)
                {
                    batchesResults.addBatchResults(results);
                }
            }
        } catch (RuntimeException e)
        {
            for (ITerminableFuture<List<R>> future : futures)
            {
                future.cancel(true);
            }
            throw e;
        }

        return batchesResults;
    }

}
