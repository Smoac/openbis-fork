/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import ch.systemsx.cisd.common.collections.DAG;

/**
 * Represents a DAG of registration dependencies between entities. The subclasses of this class
 * should implement getCode(T), and getDependentEntitiesCodes(T).
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class EntityDAG<T>
{
    private final List<? extends T> dataSetRegistrations;

    // For simplicity, we use the data set codes to construct the graph, so we need a map
    // from
    // codes to data sets
    HashMap<String, T> codeToDataMap = new HashMap<String, T>();

    HashMap<String, Collection<String>> dependencyGraph = new HashMap<String, Collection<String>>();

    /**
     * Create a DAG from the registrations.
     * 
     * @param dataSetRegistrations
     */
    public EntityDAG(List<? extends T> dataSetRegistrations)
    {
        this.dataSetRegistrations = dataSetRegistrations;
        constructGraph();
    }

    protected abstract String getCode(T entity);

    protected abstract Collection<String> getDependentEntitiesCodes(T entity);

    /**
     * @return The registrations ordered topologically such that each registrations comes after the
     *         ones it depends on.
     */
    public List<T> getOrderedRegistrations()
    {
        DAG<String, Collection<String>> dag = new DAG<String, Collection<String>>(dependencyGraph);
        List<String> sortedCodes = dag.sortTopologically();
        ArrayList<T> sortedData = new ArrayList<T>();
        for (String code : sortedCodes)
        {
            T data = codeToDataMap.get(code);
            // Some of the dependencies may be to *existing* data -- we don't care about those here.
            if (null != data)
            {
                sortedData.add(data);
            }
        }

        return sortedData;
    }

    /**
     * Create a dependency graph that can be used for topological sorting.
     */
    private void constructGraph()
    {
        for (T dataSet : dataSetRegistrations)
        {
            String dataSetCode = getCode(dataSet);
            codeToDataMap.put(dataSetCode, dataSet);

            assertNoDependantsForThisDataset(dataSetCode);

            dependencyGraph.put(dataSetCode, getDependentEntitiesCodes(dataSet));
        }
    }

    private Collection<String> assertNoDependantsForThisDataset(String dataSetCode)
    {

        Collection<String> dependents = dependencyGraph.get(dataSetCode);
        if (dependents != null)
        {
            throw new IllegalStateException("Forbidden to add dataset twice!");
        }

        return dependents;
    }

}
