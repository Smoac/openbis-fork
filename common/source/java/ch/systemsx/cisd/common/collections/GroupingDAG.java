/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * The implementation of a DAG, that returns the group of items, where the first group includes the
 * items without dependencies, the second group contain only dependencies to the first group etc.
 * 
 * @author Jakub Straszewski
 */
public class GroupingDAG<T>
{

    /**
     * The algorithm works on three internal structures.
     * <p>
     * <code>graph</code> as the adjacency list, where vertice from A to B means that a must be
     * executed before B. <code> dependenciesCount </code> is the priority table, which keeps
     * information for each item, how many items should be still taken before this one.
     * <code>queue </code> is the priority queue, that uses <code> dependenciesCount </code> as the
     * priority key.
     * <p>
     * At each level algorithm takes the items that have zero dependencies, then updates the
     * dependency counts, and repeats the procedure until there are no more vertices. If it cannot
     * proceed at some stage - it means that there is a circular dependency and the exception is
     * being thrown.
     */
    private final Map<T, Integer> dependenciesCount;

    private final Map<T, Collection<T>> graph;

    private final PriorityQueue<T> queue;

    private final List<List<T>> sortedGroups;

    private class DependenciesComparator implements Comparator<T>
    {
        @Override
        public int compare(T o1, T o2)
        {
            return dependenciesCount.get(o1).compareTo(dependenciesCount.get(o2));
        }
    }

    /**
     * @param graph must be non-empty graph
     */
    private GroupingDAG(Map<T, Collection<T>> graph)
    {
        this.graph = graph;
        this.dependenciesCount = new HashMap<T, Integer>();
        this.queue = new PriorityQueue<T>(graph.size(), new DependenciesComparator());
        this.sortedGroups = new LinkedList<List<T>>();
        initialize();
        sort();
    }

    /**
     * Return the items in the list of groups, where the earlier groups are independent on the
     * latter ones.
     * 
     * @param graph the connection graph(A).contains(B) means that A must be scheduled BEFORE B
     */
    public static <T> List<List<T>> groupByDepencies(Map<T, Collection<T>> graph)
    {
        if (graph.size() == 0)
        {
            return Collections.emptyList();
        }

        GroupingDAG<T> dag = new GroupingDAG<T>(graph);
        return dag.sortedGroups;
    }

    private void addNoDependency(T item)
    {
        if (!dependenciesCount.containsKey(item))
        {
            dependenciesCount.put(item, 0);
        }
    }

    private void addDependency(T dependant)
    {
        int count = 0;
        if (dependenciesCount.containsKey(dependant))
        {
            count = dependenciesCount.get(dependant);
        }
        dependenciesCount.put(dependant, count + 1);
    }

    private void initialize()
    {
        for (Map.Entry<T, Collection<T>> entry : graph.entrySet())
        {
            addNoDependency(entry.getKey());
            for (T dependant : entry.getValue())
            {
                addDependency(dependant);
            }
        }

        for (T item : graph.keySet())
        {
            queue.add(item);
        }
    }

    private void sort()
    {
        while (!queue.isEmpty())
        {
            List<T> levelItems = new LinkedList<T>();

            if (dependenciesCount.get(queue.peek()) > 0)
            {
                throw new UserFailureException("Circular dependency found!");
            }

            while (!queue.isEmpty() && dependenciesCount.get(queue.peek()) == 0)
            {
                T item = queue.poll();
                levelItems.add(item);
            }
            sortedGroups.add(levelItems);

            if (!queue.isEmpty())
            {
                // we don't need to clean if we know, that this is the last loop
                updateQueueAfterTheLevelCompleted(levelItems);
            }
        }
    }

    /**
     * after all items that have no dependencies have been taken, we remove further dependencies to
     * those items
     */
    private void updateQueueAfterTheLevelCompleted(List<T> levelItems)
    {
        HashSet<T> allSonsInTheLevel = new HashSet<T>();

        for (T item : levelItems)
        {
            for (T son : graph.get(item))
            {
                allSonsInTheLevel.add(son);
                dependenciesCount.put(son, dependenciesCount.get(son) - 1);
            }
        }

        for (T son : allSonsInTheLevel)
        {
            queue.remove(son);
            queue.add(son);
        }
    }
}
