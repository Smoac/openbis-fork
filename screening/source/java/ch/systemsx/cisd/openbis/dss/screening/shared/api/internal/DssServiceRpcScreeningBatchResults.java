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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author pkupczyk
 */
public class DssServiceRpcScreeningBatchResults<T> implements
        IDssServiceRpcScreeningBatchResults<T>
{

    private Map<String, List<T>> dssResultsMap = new LinkedHashMap<String, List<T>>();

    public void addDataStoreResults(String dssUrl, List<T> dssResults)
    {
        dssResultsMap.put(dssUrl, dssResults);
    }

    @Override
    public List<T> withDuplicates()
    {
        List<T> results = new ArrayList<T>();

        for (List<T> dssResults : dssResultsMap.values())
        {
            if (dssResults != null)
            {
                results.addAll(dssResults);
            }
        }

        return results;
    }

    @Override
    public List<T> withoutDuplicates()
    {
        Set<T> results = new LinkedHashSet<T>();

        for (List<T> dssResults : dssResultsMap.values())
        {
            if (dssResults != null)
            {
                results.addAll(dssResults);
            }
        }

        return new ArrayList<T>(results);
    }

    @Override
    public List<T> withoutDuplicatesPreservingOrder()
    {
        List<T> results = new ArrayList<T>();

        for (List<T> dssResults : dssResultsMap.values())
        {
            if (dssResults != null)
            {
                for (T result : dssResults)
                {
                    if (results.contains(result) == false)
                    {
                        results.add(result);
                    }
                }
            }
        }
        return results;
    }

}
