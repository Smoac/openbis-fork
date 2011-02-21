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

package ch.systemsx.cisd.ant.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Kaloyan Enimanev
 */
public class TopologicalSort
{
    /**
     * sorts the nodes in a graph topologically.
     * 
     * @param graphInput the outgoing edges from each vertex are represented as key-values in a map.
     * @return the names of the nodes in sorted order
     */
    public static List<String> sort(final Map<String, List<String>> graphInput)
    {
        Map<String, List<String>> graph = copy(graphInput);
        List<String> sorted = new ArrayList<String>();
        while (graph.isEmpty() == false)
        {
            String nextNode = getNextNode(graph);
            if (nextNode == null)
            {
                throw new RuntimeException("Graph cycle detected. Cannot execute topological sort.");
            }
            sorted.add(nextNode);
            graph.remove(nextNode);
        }
        return sorted;
    }

    /**
     * creates a copy of the graph.
     */
    private static Map<String, List<String>> copy(Map<String, List<String>> graphInput)
    {
        HashMap<String, List<String>> copy = new HashMap<String, List<String>>();
        for (Entry<String, List<String>> entry : graphInput.entrySet())
        {
            List<String> neighboursCopy = new ArrayList<String>(entry.getValue());
            copy.put(entry.getKey(), neighboursCopy);
        }
        return copy;
    }

    private static String getNextNode(Map<String, List<String>> graph)
    {
        for (Entry<String, List<String>> entry : graph.entrySet())
        {
            boolean hasDependencies = false;

            for (String dependency : entry.getValue())
            {
                if (graph.containsKey(dependency))
                {
                    hasDependencies = true;
                }
            }
            if (hasDependencies == false)
            {
                return entry.getKey();
            }
        }
        return null;
    }

}
