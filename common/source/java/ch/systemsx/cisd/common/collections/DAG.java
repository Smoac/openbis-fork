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

package ch.systemsx.cisd.common.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Directed acyclic graph.
 * 
 * @author Kaloyan Enimanev
 */
public abstract class DAG<T>
{
    protected Collection<T> nodes;

    /**
     * constructs a {@link DAG} for a given set of nodes.
     * 
     * @param nodes
     */
    public DAG(Collection<T> nodes)
    {
        this.nodes = Collections.unmodifiableCollection(nodes);
    }

    /**
     * Return the graph's nodes.
     */
    public Collection<T> getNodes()
    {
        return nodes;
    }

    /**
     * For a given node returns the list of its successors.
     */
    public abstract Collection<T> getSuccessors(T node);

    /**
     * @return a topological sort of the graph's nodes.
     */
    public List<T> sortTopologically()
    {
        HashSet<T> nodesCopy = new HashSet<T>(nodes);
        ArrayList<T> sorted = new ArrayList<T>();
        while (nodesCopy.isEmpty() == false)
        {
            T nextNode = getNextNodeForTopologicalSort(nodesCopy);
            if (nextNode == null)
            {
                throw new UserFailureException(
                        "Graph cycle detected. Cannot execute topological sort.");
            }
            sorted.add(nextNode);
            nodesCopy.remove(nextNode);
        }

        return sorted;
    }

    /**
     * helper method for topological sorting.
     */
    private T getNextNodeForTopologicalSort(Collection<T> nodesCollection)
    {
        for (T node : nodesCollection)
        {
            Collection<T> successors = getSuccessors(node);
            boolean hasDependencies = false;
            if (successors != null)
            {
                for (T successor : successors)
                {
                    if (nodesCollection.contains(successor))
                    {
                        hasDependencies = true;
                    }
                }

            }
            if (hasDependencies == false)
            {
                return node;
            }
        }
        return null;
    }

}
