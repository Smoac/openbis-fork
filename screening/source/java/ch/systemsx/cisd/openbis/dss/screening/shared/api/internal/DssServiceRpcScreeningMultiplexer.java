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

package ch.systemsx.cisd.openbis.dss.screening.shared.api.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.systemsx.cisd.base.namedthread.NamingThreadPoolExecutor;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.ITerminableFuture;
import ch.systemsx.cisd.common.concurrent.TerminableCallable.INamedCallable;
import ch.systemsx.cisd.common.concurrent.TerminableCallable.IStoppableExecutor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;

/**
 * @author pkupczyk
 */
public class DssServiceRpcScreeningMultiplexer implements IDssServiceRpcScreeningMultiplexer
{
    private final IDssServiceRpcScreeningFactory dssServiceFactory;

    private final NamingThreadPoolExecutor executor;

    public DssServiceRpcScreeningMultiplexer(IDssServiceRpcScreeningFactory dssServiceFactory)
    {
        if (dssServiceFactory == null)
        {
            throw new IllegalArgumentException("Dss service factory cannot be null");
        }
        this.dssServiceFactory = dssServiceFactory;
        this.executor = new NamingThreadPoolExecutor("Dss service screening multiplexer");
    }

    @Override
    public <R extends IDatasetIdentifier, V> DssServiceRpcScreeningBatchResults<V> process(
            final List<? extends R> references,
            final IDssServiceRpcScreeningBatchHandler<R, V> batchHandler)
    {
        Map<String, List<R>> referencesPerDataStore = getReferencesPerDataStore(cast(references));

        Map<String, ITerminableFuture<List<V>>> futuresPerDataStore =
                submitReferencesToDataStores(referencesPerDataStore, batchHandler);

        return gatherResultsFromDataStores(futuresPerDataStore);
    }

    public static <R extends IDatasetIdentifier> Map<String, List<R>> getReferencesPerDataStore(
            final List<R> references)
    {
        HashMap<String, List<R>> referencesPerDataStore = new HashMap<String, List<R>>();

        if (references != null)
        {
            for (R reference : references)
            {
                if (reference != null)
                {
                    String dataStoreUrl = reference.getDatastoreServerUrl();
                    if (dataStoreUrl != null)
                    {
                        List<R> dataStoreReferences = referencesPerDataStore.get(dataStoreUrl);
                        if (dataStoreReferences == null)
                        {
                            dataStoreReferences = new ArrayList<R>();
                            referencesPerDataStore.put(dataStoreUrl, dataStoreReferences);
                        }
                        dataStoreReferences.add(reference);
                    }
                }
            }
        }

        return referencesPerDataStore;
    }

    private <R extends IDatasetIdentifier, V> Map<String, ITerminableFuture<List<V>>> submitReferencesToDataStores(
            final Map<String, List<R>> referencesPerDataStore,
            final IDssServiceRpcScreeningBatchHandler<R, V> batchHandler)
    {
        Map<String, ITerminableFuture<List<V>>> futuresPerDataStore =
                new LinkedHashMap<String, ITerminableFuture<List<V>>>();
        final long submitTime = System.currentTimeMillis();

        for (Entry<String, List<R>> referencePerDataStore : referencesPerDataStore.entrySet())
        {
            final String dataStoreUrl = referencePerDataStore.getKey();
            final List<R> dataStoreReferences = referencePerDataStore.getValue();

            ITerminableFuture<List<V>> dataStoreFuture =
                    ConcurrencyUtilities.submit(executor, new INamedCallable<List<V>>()
                        {
                            @Override
                            public List<V> call(IStoppableExecutor<List<V>> stoppableExecutor)
                                    throws Exception
                            {
                                final DssServiceRpcScreeningHolder dataStoreServiceHolder =
                                        dssServiceFactory.createDssService(dataStoreUrl);
                                return batchHandler.handle(dataStoreServiceHolder,
                                        dataStoreReferences);
                            }

                            @Override
                            public String getCallableName()
                            {
                                return dataStoreUrl + "(" + submitTime + ")";
                            }
                        });
            futuresPerDataStore.put(dataStoreUrl, dataStoreFuture);
        }

        return futuresPerDataStore;
    }

    private <V> DssServiceRpcScreeningBatchResults<V> gatherResultsFromDataStores(
            final Map<String, ITerminableFuture<List<V>>> futuresPerDataStore)
    {
        DssServiceRpcScreeningBatchResults<V> results = new DssServiceRpcScreeningBatchResults<V>();

        try
        {
            for (Map.Entry<String, ITerminableFuture<List<V>>> futurePerDataStore : futuresPerDataStore
                    .entrySet())
            {
                String dataStoreUrl = futurePerDataStore.getKey();
                ITerminableFuture<List<V>> dataStoreFuture = futurePerDataStore.getValue();

                List<V> dataStoreResults = ConcurrencyUtilities.tryGetResult(dataStoreFuture, -1);
                if (dataStoreResults != null)
                {
                    results.addDataStoreResults(dataStoreUrl, dataStoreResults);
                }
            }
        } catch (RuntimeException e)
        {
            for (ITerminableFuture<List<V>> dataStoreFuture : futuresPerDataStore.values())
            {
                dataStoreFuture.cancel(true);
            }
            throw e;
        }

        return results;
    }

    @SuppressWarnings("unchecked")
    private <R extends IDatasetIdentifier> List<R> cast(List<? extends R> references)
    {
        return (List<R>) references;
    }

}
