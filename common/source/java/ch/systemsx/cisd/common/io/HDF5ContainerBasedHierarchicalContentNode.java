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
import java.util.List;

import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.hdf5.Hdf5Container;
import ch.systemsx.cisd.hdf5.IHDF5SimpleReader;

/**
 * {@link IHierarchicalContent} implementation for HDF5 container.
 * 
 * @author Piotr Buczek
 */
class HDF5ContainerBasedHierarchicalContentNode extends DefaultFileBasedHierarchicalContentNode
{
    private final Hdf5Container hdf5Container;

    HDF5ContainerBasedHierarchicalContentNode(
            IHierarchicalContentFactory hierarchicalContentFactory, IHierarchicalContent root,
            File hdf5ContainerFile)
    {
        super(hierarchicalContentFactory, root, hdf5ContainerFile);
        this.hdf5Container = new Hdf5Container(hdf5ContainerFile);
    }

    private IHDF5SimpleReader createReader()
    {
        return hdf5Container.createSimpleReader();
    }

    //

    @Override
    public boolean isDirectory()
    {
        return true; // always a directory
    }

    @Override
    public List<IHierarchicalContentNode> doGetChildNodes()
    {
        // NOTE this is a slow implementation - improve it when information can be retrieved from DB
        IHDF5SimpleReader reader = createReader();
        try
        {
            List<String> childPaths = reader.getGroupMembers("/");
            List<IHierarchicalContentNode> result = new ArrayList<IHierarchicalContentNode>();
            for (String childPath : childPaths)
            {
                result.add(getChildNode(reader, childPath));
            }
            return result;
        } finally
        {
            reader.close();
        }
    }

    /** @return child node with given path relative to this container */
    public IHierarchicalContentNode getChildNode(String childPath)
    {
        IHDF5SimpleReader reader = createReader();
        try
        {
            return getChildNode(reader, childPath);
        } finally
        {
            reader.close();
        }
    }

    private IHierarchicalContentNode getChildNode(IHDF5SimpleReader reader, String childPath)
    {
        String fileName = FileUtilities.getFileNameFromRelativePath(childPath);
        if (reader.isGroup(childPath))
        {
            return new HDF5GroupNode(this, childPath, fileName);
        } else
        {
            return new HDF5FileNode(this, childPath, fileName);
        }
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return "HDF5ContainerBasedHierarchicalContentNode [root=" + root + ", container="
                + hdf5Container.getHdf5File() + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + hdf5Container.getHdf5File().hashCode();
        result = prime * result + root.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof HDF5ContainerBasedHierarchicalContentNode))
        {
            return false;
        }
        HDF5ContainerBasedHierarchicalContentNode other =
                (HDF5ContainerBasedHierarchicalContentNode) obj;
        if (!hdf5Container.getHdf5File().equals(other.hdf5Container.getHdf5File()))
        {
            return false;
        }
        if (!root.equals(other.root))
        {
            return false;
        }
        return true;
    }

    class HDF5GroupNode extends AbstractHierarchicalDirectoryContentNode
    {

        private final String relativePath; // internal path in HDF5 container

        private final String groupName;

        private final HDF5ContainerBasedHierarchicalContentNode containerNode;

        public HDF5GroupNode(HDF5ContainerBasedHierarchicalContentNode containerNode,
                String relativePath, String groupName)
        {
            this.containerNode = containerNode;
            this.relativePath = relativePath;
            this.groupName = groupName;
        }

        public String getName()
        {
            return groupName;
        }

        public String getRelativePath()
        {
            return containerNode.getRelativePath() + File.separator + relativePath;
        }

        public boolean exists()
        {
            return true;
        }

        public boolean isDirectory()
        {
            return true;
        }

        public File getFile() throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException("This is not a normal directory node.");
        }

        @Override
        protected List<IHierarchicalContentNode> doGetChildNodes()
        {
            IHDF5SimpleReader reader = createReader();
            try
            {
                List<IHierarchicalContentNode> result = new ArrayList<IHierarchicalContentNode>();
                List<String> children = reader.getGroupMembers(relativePath);
                for (String childName : children)
                {
                    String newRelativePath = relativePath + File.separator + childName;
                    if (reader.isGroup(newRelativePath))
                    {
                        result.add(new HDF5GroupNode(containerNode, newRelativePath, childName));
                    } else
                    {
                        result.add(new HDF5FileNode(containerNode, newRelativePath, childName));
                    }
                }
                return result;
            } finally
            {
                reader.close();
            }
        }

        @Override
        public String toString()
        {
            return "HDF5GroupNode [containerPath=" + containerNode.getRelativePath()
                    + ", relativePath=" + relativePath + ", groupName=" + groupName + "]";
        }

    }

    class HDF5FileNode extends AbstractHierarchicalFileContentNode
    {

        private final String relativePath; // internal path in HDF5 container

        private final String fileName;

        private IContent contentOrNull;

        private final HDF5ContainerBasedHierarchicalContentNode containerNode;

        public HDF5FileNode(HDF5ContainerBasedHierarchicalContentNode containerNode,
                String relativePath, String fileName)
        {
            this.containerNode = containerNode;
            this.relativePath = relativePath;
            this.fileName = fileName;
        }

        public String getName()
        {
            return fileName;
        }

        public String getRelativePath()
        {
            return containerNode.getRelativePath() + File.separator + relativePath;
        }

        public boolean exists()
        {
            return true; // getContent().exists();
        }

        public boolean isDirectory()
        {
            return false;
        }

        public File getFile() throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException("This is not a normal file node.");
        }

        @Override
        protected long doGetFileLength()
        {
            return getContent().getSize();
        }

        @Override
        protected IRandomAccessFile doGetFileContent()
        {
            return getContent().getReadOnlyRandomAccessFile();
        }

        @Override
        protected InputStream doGetInputStream()
        {
            return getContent().getInputStream();
        }

        private IContent getContent()
        {
            if (contentOrNull == null)
            {
                contentOrNull = extractFileContent(file, relativePath);
            }
            return contentOrNull;
        }

        @Override
        public String toString()
        {
            return "HDF5FileNode [containerPath=" + containerNode.getRelativePath()
                    + ", relativePath=" + relativePath + ", fileName=" + fileName + "]";
        }

    }

    private static IContent extractFileContent(File hdf5File, String dataSetPath)
    {
        return new HDF5DataSetBasedContent(hdf5File, dataSetPath);
    }
}