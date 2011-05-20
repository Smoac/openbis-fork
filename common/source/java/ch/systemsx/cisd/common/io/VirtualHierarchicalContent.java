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

package ch.systemsx.cisd.common.io;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;

/**
 * Simple {@link IHierarchicalContent} implementation for virtual data sets with dynamic behavior
 * (almost no caching).
 * 
 * @author Piotr Buczek
 */
class VirtualHierarchicalContent implements IHierarchicalContent
{

    private final List<IHierarchicalContent> components;

    private IHierarchicalContentNode rootNode; // cached

    public VirtualHierarchicalContent(List<IHierarchicalContent> components)
    {
        if (components == null || components.isEmpty())
        {
            throw new IllegalArgumentException("Undefined contents");
        }
        this.components = components;
    }

    public IHierarchicalContentNode getRootNode()
    {
        if (rootNode == null)
        {
            rootNode = mergeNodes(new INodeProvider()
                {
                    public IHierarchicalContentNode tryGetNode(IHierarchicalContent content)
                    {
                        return content.getRootNode();
                    }
                });
        }
        return rootNode;
    }

    public IHierarchicalContentNode getNode(final String relativePath)
            throws IllegalArgumentException
    {
        return mergeNodes(new INodeProvider()
            {
                public IHierarchicalContentNode tryGetNode(IHierarchicalContent content)
                {
                    try
                    {
                        return content.getNode(relativePath);
                    } catch (IllegalArgumentException ex)
                    {
                        return null; // ignore (not all components need to contain the node)
                    }
                }
            });
    }

    public List<IHierarchicalContentNode> listMatchingNodes(final String relativePathPattern)
    {
        return mergeNodeLists(new INodeListProvider()
            {
                public List<IHierarchicalContentNode> getNodeList(IHierarchicalContent content)
                {
                    return content.listMatchingNodes(relativePathPattern);
                }
            });
    }

    public List<IHierarchicalContentNode> listMatchingNodes(final String startingPath,
            final String fileNamePattern)
    {
        return mergeNodeLists(new INodeListProvider()
            {
                public List<IHierarchicalContentNode> getNodeList(IHierarchicalContent content)
                {
                    return content.listMatchingNodes(startingPath, fileNamePattern);
                }
            });
    }

    public void close()
    {
        for (IHierarchicalContent component : components)
        {
            component.close();
        }
    }

    private IHierarchicalContentNode mergeNodes(INodeProvider provider)
    {
        IVirtualNodeMerger merger = createNodeMerger();
        for (IHierarchicalContent component : components)
        {
            IHierarchicalContentNode componentNode = provider.tryGetNode(component);
            if (componentNode != null)
            {
                merger.addNode(componentNode);
            }
        }
        return merger.createMergedNode();
    }

    private List<IHierarchicalContentNode> mergeNodeLists(INodeListProvider listProvider)
    {
        IVirtualNodeListMerger listMerger = createNodeListMerger();
        for (IHierarchicalContent component : components)
        {
            List<IHierarchicalContentNode> componentNodes = listProvider.getNodeList(component);
            listMerger.addNodes(componentNodes);
        }
        return listMerger.createMergedNodeList();
    }

    private static IVirtualNodeMerger createNodeMerger()
    {
        return new VirtualNodeMerger();
    }

    private static IVirtualNodeListMerger createNodeListMerger()
    {
        return new VirtualNodeListMerger();
    }

    interface INodeProvider
    {
        IHierarchicalContentNode tryGetNode(IHierarchicalContent content);
    }

    interface INodeListProvider
    {
        List<IHierarchicalContentNode> getNodeList(IHierarchicalContent content);
    }

    interface IVirtualNodeMerger
    {
        void addNode(IHierarchicalContentNode node);

        IHierarchicalContentNode createMergedNode();
    }

    interface IVirtualNodeListMerger
    {
        void addNodes(List<IHierarchicalContentNode> nodes);

        List<IHierarchicalContentNode> createMergedNodeList();
    }

    /**
     * Merges nodes with the same relative paths:
     * <ul>
     * <li>For directories merges the internal nodes.
     * <li>For normal files uses the 'last' node's file.
     * </ul>
     */
    static class VirtualNodeMerger implements IVirtualNodeMerger
    {
        // For convenience in iteration the order of these nodes is reversed.
        // It is the first node, not the last one, which is overriding all files of other nodes.
        private LinkedList<IHierarchicalContentNode> nodes =
                new LinkedList<IHierarchicalContentNode>();

        public void addNode(IHierarchicalContentNode node)
        {
            nodes.addFirst(node);
        }

        public IHierarchicalContentNode createMergedNode()
        {
            return new VirtualNode(nodes);
        }
    }

    /**
     * Merges lists of nodes into a list with nodes merging nodes with the same relative paths.
     */
    static class VirtualNodeListMerger implements IVirtualNodeListMerger
    {
        // relative path -> merger (with preserved order)
        Map<String, IVirtualNodeMerger> mergers = new LinkedHashMap<String, IVirtualNodeMerger>();

        public void addNodes(List<IHierarchicalContentNode> nodes)
        {
            for (IHierarchicalContentNode node : nodes)
            {
                String relativePath = node.getRelativePath();
                IVirtualNodeMerger merger = mergers.get(relativePath);
                if (merger == null)
                {
                    merger = createNodeMerger();
                    mergers.put(relativePath, merger);
                }
                merger.addNode(node);
            }
        }

        public List<IHierarchicalContentNode> createMergedNodeList()
        {
            List<IHierarchicalContentNode> result = new ArrayList<IHierarchicalContentNode>();
            for (IVirtualNodeMerger merger : mergers.values())
            {
                result.add(merger.createMergedNode());
            }
            return result;
        }
    }

    /**
     * {@link IHierarchicalContentNode} implementation merging nodes with the same relative paths:
     * <ul>
     * <li>For directories merges the internal nodes.
     * <li>For normal files uses the 'last' available node's file.
     * </ul>
     */
    static class VirtualNode implements IHierarchicalContentNode
    {

        private final List<IHierarchicalContentNode> nodes;

        public VirtualNode(List<IHierarchicalContentNode> nodes)
        {
            if (nodes.isEmpty())
            {
                throw new IllegalArgumentException("Resource doesn't exist.");
            }
            this.nodes = nodes;
        }

        private IHierarchicalContentNode lastNode()
        {
            return nodes.get(0);
        }

        private IHierarchicalContentNode lastExistingNode()
        {
            for (IHierarchicalContentNode node : nodes)
            {
                if (node.exists()) // archived node doesn't exist and will be omitted
                {
                    return node;
                }
            }
            throw new IllegalStateException("Resource is unavailable.");
        }

        public String getName()
        {
            return lastNode().getName();
        }

        public String getRelativePath()
        {
            return lastNode().getRelativePath();
        }

        public String getParentRelativePath()
        {
            return lastNode().getParentRelativePath();
        }

        public boolean exists()
        {
            // the node exists if at least one node exists
            for (IHierarchicalContentNode node : nodes)
            {
                if (node.exists())
                {
                    return true;
                }
            }
            return false;
        }

        public boolean isDirectory()
        {
            // NOTE: we don't support files and directories with the same name
            return lastNode().isDirectory();
        }

        public List<IHierarchicalContentNode> getChildNodes() throws UnsupportedOperationException
        {
            IVirtualNodeListMerger listMerger = createNodeListMerger();
            for (IHierarchicalContentNode node : nodes)
            {
                listMerger.addNodes(node.getChildNodes());
            }
            return listMerger.createMergedNodeList();
        }

        public File getFile() throws UnsupportedOperationException
        {
            return lastExistingNode().getFile();
        }

        public long getFileLength() throws UnsupportedOperationException
        {
            if (isDirectory())
            {
                // For directory we return an estimated length - sum of lengths of all nodes.
                // NOTE: This code is here for future. Currently all implementations for real data
                // set directories throw an exception when asked for file length.
                long estimatedLength = 0;
                for (IHierarchicalContentNode node : nodes)
                {
                    estimatedLength += node.getFileLength();
                }
                return estimatedLength;
            } else
            {
                return lastExistingNode().getFileLength();
            }
        }

        public IRandomAccessFile getFileContent() throws UnsupportedOperationException,
                IOExceptionUnchecked
        {
            return lastExistingNode().getFileContent();
        }

        public InputStream getInputStream() throws UnsupportedOperationException,
                IOExceptionUnchecked
        {
            return lastExistingNode().getInputStream();
        }

    }

}
