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
    public <R extends IDatasetIdentifier, V> List<V> process(final List<? extends R> references,
            final IDssServiceRpcScreeningBatchHandler<R, V> handler)
    {
        Map<String, List<R>> referencesPerDss = getReferencesPerDss(cast(references));
        List<ITerminableFuture<List<V>>> dssFutures = new ArrayList<ITerminableFuture<List<V>>>();
        final long callMillis = System.currentTimeMillis();

        for (Entry<String, List<R>> entry : referencesPerDss.entrySet())
        {
            final String dssUrl = entry.getKey();
            final List<R> referencesForDss = entry.getValue();

            ITerminableFuture<List<V>> dssFuture =
                    ConcurrencyUtilities.submit(executor, new INamedCallable<List<V>>()
                        {
                            @Override
                            public List<V> call(IStoppableExecutor<List<V>> stoppableExecutor)
                                    throws Exception
                            {
                                final DssServiceRpcScreeningHolder dssServiceHolder =
                                        dssServiceFactory.createDssService(dssUrl);
                                return handler.handle(dssServiceHolder, referencesForDss);
                            }

                            @Override
                            public String getCallableName()
                            {
                                return dssUrl + "(" + callMillis + ")";
                            }
                        });
            dssFutures.add(dssFuture);
        }

        List<V> allResults = new ArrayList<V>();

        try
        {
            for (ITerminableFuture<List<V>> dssFuture : dssFutures)
            {
                List<V> dssResults = ConcurrencyUtilities.tryGetResult(dssFuture, -1);
                if (dssResults != null)
                {
                    allResults.addAll(dssResults);
                }
            }
        } catch (RuntimeException e)
        {
            for (ITerminableFuture<List<V>> dssFuture : dssFutures)
            {
                dssFuture.cancel(true);
            }
            throw e;
        }

        return allResults;
    }

    @SuppressWarnings("unchecked")
    private <R extends IDatasetIdentifier> List<R> cast(List<? extends R> references)
    {
        return (List<R>) references;
    }

    public static <R extends IDatasetIdentifier> Map<String, List<R>> getReferencesPerDss(
            List<R> references)
    {
        HashMap<String, List<R>> referencesPerDss = new HashMap<String, List<R>>();

        if (references != null)
        {
            for (R reference : references)
            {
                if (reference != null)
                {
                    String url = reference.getDatastoreServerUrl();
                    if (url != null)
                    {
                        List<R> list = referencesPerDss.get(url);
                        if (list == null)
                        {
                            list = new ArrayList<R>();
                            referencesPerDss.put(url, list);
                        }
                        list.add(reference);
                    }
                }
            }
        }

        return referencesPerDss;
    }

}
