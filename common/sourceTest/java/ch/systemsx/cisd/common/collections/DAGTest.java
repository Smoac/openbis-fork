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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author Kaloyan Enimanev
 */
public class DAGTest extends AssertJUnit
{

    @Test
    public void testTopologicalSort()
    {
        HashMap<String, List<String>> adjacencyMap = new HashMap<String, List<String>>();

        adjacencyMap.put("A", new ArrayList<String>());
        adjacencyMap.put("B", Arrays.asList("A"));
        adjacencyMap.put("C", Arrays.asList("A", "B"));
        adjacencyMap.put("D", Arrays.asList("C", "B", "A"));
        adjacencyMap.put("E", Arrays.asList("C", "D"));

        Collection<String> sorted = sortTopologically(adjacencyMap);

        assertEquals(Arrays.asList("A", "B", "C", "D", "E"), sorted);

    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testCyclicGraph()
    {
        HashMap<String, List<String>> adjacencyMap = new HashMap<String, List<String>>();

        adjacencyMap.put("A", Arrays.asList("B"));
        adjacencyMap.put("B", Arrays.asList("C"));
        adjacencyMap.put("C", Arrays.asList("A"));

        sortTopologically(adjacencyMap);
    }

    private Collection<String> sortTopologically(final Map<String, List<String>> adjacencyMap)
    {
        DAG<String, List<String>> dag = new DAG<String, List<String>>(adjacencyMap);
        return dag.sortTopologically();
    }
}
