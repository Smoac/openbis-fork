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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author pkupczyk
 */
public class BatchesResults<T> implements IBatchesResults<T>
{

    private List<List<T>> batchesResults = new ArrayList<List<T>>();

    public void addBatchResults(List<T> results)
    {
        batchesResults.add(results);
    }

    @Override
    public List<T> withDuplicates()
    {
        List<T> results = new ArrayList<T>();

        for (List<T> batchResults : batchesResults)
        {
            if (batchResults != null)
            {
                results.addAll(batchResults);
            }
        }

        return results;
    }

    @Override
    public List<T> withoutDuplicates()
    {
        Set<T> results = new LinkedHashSet<T>();

        for (List<T> batchResults : batchesResults)
        {
            if (batchResults != null)
            {
                results.addAll(batchResults);
            }
        }

        return new ArrayList<T>(results);
    }

    @Override
    public List<T> withoutDuplicatesPreservingOrder()
    {
        List<T> results = new ArrayList<T>();

        for (List<T> batchResults : batchesResults)
        {
            if (batchResults != null)
            {
                for (T batchResult : batchResults)
                {
                    if (results.contains(batchResult) == false)
                    {
                        results.add(batchResult);
                    }
                }
            }
        }
        return results;
    }

}
