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
import java.util.HashMap;
import java.util.List;

import ch.systemsx.cisd.common.collections.DAG;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;

/**
 * Represents a DAG of registration dependencies between new data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class NewExternalDataDAG
{
    private final List<? extends NewExternalData> dataSetRegistrations;

    // For simplicity, we use the data set codes to construct the graph, so we need a map from
    // codes to data sets
    HashMap<String, NewExternalData> codeToDataMap = new HashMap<String, NewExternalData>();

    HashMap<String, ArrayList<String>> dependencyGraph = new HashMap<String, ArrayList<String>>();

    /**
     * Create a DAG from the registrations.
     * 
     * @param dataSetRegistrations
     */
    public NewExternalDataDAG(List<? extends NewExternalData> dataSetRegistrations)
    {
        super();
        this.dataSetRegistrations = dataSetRegistrations;
        constructGraph();
    }

    /**
     * @return The registrations ordered topologically such that each registrations comes after the
     *         ones it depends on.
     */
    public List<? extends NewExternalData> getOrderedRegistrations()
    {
        DAG<String, ArrayList<String>> dag = new DAG<String, ArrayList<String>>(dependencyGraph);
        List<String> sortedCodes = dag.sortTopologically();
        ArrayList<NewExternalData> sortedData = new ArrayList<NewExternalData>();
        for (String code : sortedCodes)
        {
            NewExternalData data = codeToDataMap.get(code);
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
        for (NewExternalData dataSet : dataSetRegistrations)
        {
            String dataSetCode = dataSet.getCode();
            codeToDataMap.put(dataSetCode, dataSet);

            // There may already be dependents for this data set -- get them or initialize the
            // dependents
            ArrayList<String> dependents = getDependentsList(dataSetCode);

            // All the parents are dependents
            dependents.addAll(dataSet.getParentDataSetCodes());

            if (dataSet instanceof NewContainerDataSet)
            {
                // All contained data sets are dependents
                List<String> containedDataSetCodes =
                        ((NewContainerDataSet) dataSet).getContainedDataSetCodes();
                dependents.addAll(containedDataSetCodes);
            }

            dependencyGraph.put(dataSet.getCode(), dependents);
        }
    }

    private ArrayList<String> getDependentsList(String dataSetCode)
    {
        ArrayList<String> dependents = dependencyGraph.get(dataSetCode);
        if (null == dependents)
        {
            dependents = new ArrayList<String>();
            dependencyGraph.put(dataSetCode, dependents);
        }
        return dependents;
    }

}
