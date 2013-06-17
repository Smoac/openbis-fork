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

package ch.systemsx.cisd.openbis.common.io.hierarchical_content;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

import de.schlichtherle.io.rof.SimpleReadOnlyFile;
import de.schlichtherle.util.zip.BasicZipFile;
import de.schlichtherle.util.zip.ZipEntry;

/**
 * {@link IHierarchicalContent} implementation for ZIP files.
 *
 * @author Franz-Josef Elmer
 */
public class ZipBasedHierarchicalContent extends AbstractHierarchicalContent
{
    private static final String extractName(String relativePath)
    {
        int indexOfLastDelimiter = relativePath.lastIndexOf('/');
        if (indexOfLastDelimiter < 0)
        {
            return relativePath;
        } 
        return relativePath.substring(indexOfLastDelimiter + 1);
    }

    private static final class ZipContainerNode extends AbstractHierarchicalDirectoryContentNode
    {
        private final String relativePath;
        private final String name;
        private final List<IHierarchicalContentNode> children = new ArrayList<IHierarchicalContentNode>();
        
        ZipContainerNode(String relativePath)
        {
            this.relativePath = relativePath;
            name = extractName(relativePath);
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public boolean exists()
        {
            return true;
        }

        @Override
        public boolean isDirectory()
        {
            return true;
        }

        @Override
        public long getLastModified()
        {
            return 0;
        }

        @Override
        public File getFile() throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public File tryGetFile()
        {
            return null;
        }

        @Override
        public boolean isChecksumCRC32Precalculated()
        {
            return true;
        }

        @Override
        protected String doGetRelativePath()
        {
            return relativePath;
        }

        @Override
        protected List<IHierarchicalContentNode> doGetChildNodes()
        {
            return new ArrayList<IHierarchicalContentNode>(children);
        }
    }
    
    private static final class ZipContentNode extends AbstractHierarchicalFileContentNode
    {
        private final BasicZipFile zipFile;
        private final ZipEntry zipEntry;
        private final String relativePath;
        private final String name;

        ZipContentNode(BasicZipFile zipFile, ZipEntry zipEntry)
        {
            this.zipFile = zipFile;
            this.zipEntry = zipEntry;
            relativePath = zipEntry.getName();
            name = extractName(relativePath);
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public boolean exists()
        {
            return true;
        }

        @Override
        public boolean isDirectory()
        {
            return false;
        }

        @Override
        public long getLastModified()
        {
            return zipEntry.getTime();
        }

        @Override
        public File getFile() throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public File tryGetFile()
        {
            return null;
        }

        @Override
        protected String doGetRelativePath()
        {
            return relativePath;
        }

        @Override
        protected long doGetFileLength()
        {
            return zipEntry.getSize();
        }

        @Override
        public boolean isChecksumCRC32Precalculated()
        {
            return true;
        }
        
        @Override
        protected int doGetChecksumCRC32()
        {
            return (int) zipEntry.getCrc();
        }

        @Override
        protected IRandomAccessFile doGetFileContent()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        protected InputStream doGetInputStream()
        {
            try
            {
                return zipFile.getInputStream(zipEntry);
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }
    
    private static final class NodeManager
    {
        private final Map<String, ZipContainerNode> containerNodes = new HashMap<String, ZipContainerNode>();
        private final Map<String, IHierarchicalContentNode> allNodes = new HashMap<String, IHierarchicalContentNode>();
        
        private ZipContainerNode rootNode;
        
        void handle(BasicZipFile zipFile, ZipEntry zipEntry)
        {
            if (zipEntry.isDirectory())
            {
                String name = zipEntry.getName();
                if (name.endsWith("/"))
                {
                    name = name.substring(0, name.length() - 1);
                }
                linkNode(new ZipContainerNode(name));
            } else
            {
                linkNode(new ZipContentNode(zipFile, zipEntry));
            }
        }

        private void linkNode(IHierarchicalContentNode contentNode)
        {
            allNodes.put(contentNode.getRelativePath(), contentNode);
            String parentRelativePath = contentNode.getParentRelativePath();
            ZipContainerNode containerNode = containerNodes.get(parentRelativePath);
            if (containerNode == null)
            {
                containerNode = new ZipContainerNode(parentRelativePath);
                containerNodes.put(parentRelativePath, containerNode);
                if (parentRelativePath.length() == 0)
                {
                    rootNode = containerNode;
                } else
                {
                    linkNode(containerNode);
                }
            }
            containerNode.children.add(contentNode);
        }
    }

    private final BasicZipFile zipFile;
    private final IHierarchicalContentNode rootNode;
    private final Map<String, IHierarchicalContentNode> allNodes;

    public ZipBasedHierarchicalContent(File file)
    {
        try
        {
            NodeManager nodeManager = new NodeManager();
            zipFile = new BasicZipFile(new SimpleReadOnlyFile(file), "UTF-8", true, false);
            @SuppressWarnings("unchecked")
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                nodeManager.handle(zipFile, entry);
            }
            rootNode = nodeManager.rootNode;
            allNodes = nodeManager.allNodes;
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public IHierarchicalContentNode getRootNode()
    {
        return rootNode;
    }

    @Override
    public IHierarchicalContentNode getNode(String relativePath) throws IllegalArgumentException
    {
        IHierarchicalContentNode node = tryGetNode(relativePath);
        if (node == null)
        {
            throw new IllegalArgumentException("Resource '" + relativePath + "' does not exist.");
        }
        return node;
    }

    @Override
    public IHierarchicalContentNode tryGetNode(String relativePath)
    {
        return allNodes.get(relativePath);
    }

    @Override
    public void close()
    {
        try
        {
            zipFile.close();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    
}
