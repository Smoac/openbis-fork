/*
 * Copyright 2016 ETH Zuerich, SIS
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
package ch.ethz.sis.openbis.generic.shared.entitygraph;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntityGraph<N extends Node<?>>
{
    private final Map<String, N> nodes;

    private final Map<N, List<EdgeNodePair>> adjacencyMap;

    // the following flags make sure then we don't end up in a cycle when a sample is the component of a its child sample
    // or when a data set is the component of its child data set
    private final Set<String> visitedParents = new HashSet<String>();

    private final Set<String> visitedContainers = new HashSet<String>();

    public EntityGraph()
    {
        this.nodes = new HashMap<String, N>();
        this.adjacencyMap = new HashMap<N, List<EdgeNodePair>>();
    }

    public boolean isVisitedAsParent(String identifier)
    {
        return visitedParents.contains(identifier);
    }

    public boolean isVisitedAsContainer(String identifier)
    {
        return visitedContainers.contains(identifier);
    }

    public void markAsVisitedAsParent(String identifier)
    {
        visitedParents.add(identifier);
    }

    public void markAsVisitedAsContainer(String identifier)
    {
        visitedContainers.add(identifier);
    }

    public void addEdge(N startNode, N endNode, Edge edge)
    {
        List<EdgeNodePair> adjacencyList = adjacencyMap.get(startNode);
        if (adjacencyList == null)
        {
            adjacencyList = new ArrayList<EdgeNodePair>();
            adjacencyMap.put(startNode, adjacencyList);
        }
        else
        {
            // do not add the edge-node pair if it has already been added
            // happens when for a sub sample of a sample with multiple parent
            for (EdgeNodePair pair : adjacencyList)
            {
                if (pair.getNode().equals(endNode))
                {
                    return;
                }
            }
        }
        EdgeNodePair enPair = new EdgeNodePair(edge, endNode);
        startNode.addConnection(enPair);
        adjacencyList.add(enPair);
        addNode(endNode);
    }

    public void addNode(N node)
    {
        String identifier = node.getIdentifier();
        if (nodes.containsKey(identifier) == false)
        {
            adjacencyMap.put(node, new ArrayList<EdgeNodePair>());
            nodes.put(identifier, node);
        }
        // this.printGraph();
    }


    public List<N> getNodes()
    {
        return new ArrayList<N>(nodes.values());
    }

    public String getEdgesForDOTRepresentation()
    {
        StringBuffer sb = new StringBuffer();
        for (Node<?> node : getNodes())
        {
            List<EdgeNodePair> list = adjacencyMap.get(node);
            if (list.isEmpty() && node.getEntityKind().equals("DATA_SET") == false)
            {
                sb.append(getRightHandNodeRep(node));
                sb.append(System.getProperty("line.separator"));
                continue;
            }
            for (EdgeNodePair edgeNodePair : list)
            {
                Node<?> neighbourNode = edgeNodePair.getNode();
                sb.append("\"" + node.getCode() + "(" + node.getEntityKind().charAt(0) + getDifferentiatorStr(node) + ")\" -> "
                        + getRightHandNodeRep(neighbourNode));
                sb.append(System.getProperty("line.separator"));
            }
        }
        return sb.toString();
    }

    private String getRightHandNodeRep(Node<?> node)
    {
        return "\"" + node.getCode() + "(" + node.getEntityKind().charAt(0) + getDifferentiatorStr(node) + ")\";";
    }

    private String getDifferentiatorStr(Node<?> node)
    {
        String differentiatorStr = "";
        if (node.getEntityKind().equals("EXPERIMENT")) // in order to differentiate between experiments in the same space but under different projects
        {
            differentiatorStr =
                    node.getPermId().substring(node.getPermId().indexOf('-') + 1);
        }
        return differentiatorStr;
    }
    
    private void printGraphInDOT(String spaceId)
    {
        PrintWriter writer;
        try
        {
            writer = new PrintWriter("/Users/gakin/Documents/Entity_DAG_" + spaceId + ".dot");
            writer.println("digraph DAG");
            writer.println("{");
            writer.println(getEdgesForDOTRepresentation());
            writer.println("}");
            writer.close();
        } catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void printGraph(String space)
    {
        for (Node<?> node : nodes.values())
        {
            // printNeighboursForNode(node);
        }

        printGraphInDOT(space);
    }

    public void printNeighboursForNode(Node<?> node)
    {
        List<EdgeNodePair> list = getNeighboursForEntity(node, null);
        if (list.isEmpty())
        {
            System.out.println(node.getCode()); // " has no connections"
        }
        for (EdgeNodePair pair : list)
        {
            System.out.println(node.getCode() + " : " + pair.getNode().getCode() + " : " + pair.getEdge().getType());
        }
    }

    public boolean containsEntity(String identifier)
    {
        return nodes.containsKey(identifier);
    }
    public boolean edgeExists(String nodeIdentifierFrom, String nodeIdentifierTo, String connType)
    {
        List<EdgeNodePair> neighboursForEntityWithIdentifier = getNeighboursForEntityWithIdentifier(nodeIdentifierFrom, null);
        for (EdgeNodePair edgeNodePair : neighboursForEntityWithIdentifier)
        {
            Node<?> connectedNode = edgeNodePair.getNode();
            String connectionType = edgeNodePair.getEdge().getType();
            if (connectedNode.getIdentifier().equals(nodeIdentifierTo)
                    && connType.equals(connectionType))
            {
                return true;
            }
        }
        return false;
    }

    public List<EdgeNodePair> getChildrenForEntityWithIdentifier(String identifier)
    {
        return getNeighboursForEntityWithIdentifier(identifier, "Child");
    }

    public List<EdgeNodePair> getComponentsForEntityWithIdentifier(String identifier)
    {
        return getNeighboursForEntityWithIdentifier(identifier, "Component");
    }

    public List<EdgeNodePair> getNeighboursForEntityWithIdentifier(String identifier, String connType)
    {
        Node<?> node = getNodeForIdentifier(identifier);
        return getNeighboursForEntity(node, connType);
    }

    public List<EdgeNodePair> getNeighboursForEntity(String permId, String connType)
    {
        Node<?> node = getNodeWithPermId(permId);
        // TODO throw an exception if node not found
        if (node == null)
        {
            return Collections.<EdgeNodePair> emptyList();
        }
        return getNeighboursForEntity(node, connType);
    }

    private List<EdgeNodePair> getNeighboursForEntity(Node<?> node, String connType)
    {
        if (adjacencyMap.containsKey(node) == false)
        {
            // TODO throw an exception if node not found
            System.out.println("Node " + node.getCode() + " not in the entity graph");
            return Collections.<EdgeNodePair> emptyList();
        }
        List<EdgeNodePair> list = new ArrayList<EdgeNodePair>();
        for (EdgeNodePair enp : adjacencyMap.get(node))
        {
            if (connType == null || enp.getEdge().getType().equals(connType))
            {
                list.add(enp);
            }
        }
        return list;
    }

    public Node<?> getNodeWithPermId(String permId)
    {
        // TODO make this more efficient by mapping nodes by permId as well
        for (Node<?> node : nodes.values())
        {
            if (node.getPermId().equals(permId))
            {
                return node;
            }
        }
        return null;
    }

    public Node<?> getNodeForIdentifier(String identifier)
    {
        return nodes.get(identifier);
        // for (Node<?> node : nodes.values())
        // {
        // // TODO for data sets we can just use getNodeWithPermId(identifier) since permId and identifier are the same
        // if (node.getIdentifier().equals(identifier))
        // {
        // return node;
        // }
        // }
        // return null;
    }

    public void clear()
    {
        nodes.clear();
        adjacencyMap.clear();
        visitedContainers.clear();
        visitedParents.clear();
    }
}
