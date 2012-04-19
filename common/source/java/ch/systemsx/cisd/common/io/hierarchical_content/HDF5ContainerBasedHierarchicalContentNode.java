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

package ch.systemsx.cisd.common.io.hierarchical_content;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.base.io.AdapterIInputStreamToInputStream;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.hdf5.HDF5Container;
import ch.systemsx.cisd.common.hdf5.IHDF5ContainerReader;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.hdf5.HDF5FactoryProvider;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import ch.systemsx.cisd.hdf5.h5ar.ArchiveEntry;
import ch.systemsx.cisd.hdf5.io.HDF5DataSetRandomAccessFile;
import ch.systemsx.cisd.hdf5.io.HDF5IOAdapterFactory;

/**
 * {@link IHierarchicalContent} implementation for HDF5 container.
 * 
 * @author Piotr Buczek
 */
public class HDF5ContainerBasedHierarchicalContentNode extends
        DefaultFileBasedHierarchicalContentNode
{
    private final HDF5Container hdf5Container;

    public HDF5ContainerBasedHierarchicalContentNode(IHierarchicalContent root,
            File hdf5ContainerFile)
    {
        super(root, hdf5ContainerFile);
        this.hdf5Container = new HDF5Container(hdf5ContainerFile);
    }

    private IHDF5ContainerReader createReader()
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
        IHDF5ContainerReader reader = createReader();
        try
        {
            final List<ArchiveEntry> entries = reader.getGroupMembers("/");
            final List<IHierarchicalContentNode> result = new ArrayList<IHierarchicalContentNode>();
            for (ArchiveEntry entry : entries)
            {
                result.add(getChildNode(entry));
            }
            return result;
        } finally
        {
            reader.close();
        }
    }

    /** @return child node with given path relative to this container */
    private IHierarchicalContentNode getChildNode(ArchiveEntry entry)
    {
        if (entry.isDirectory())
        {
            return new HDF5GroupNode(this, entry.getPath(), entry.getName());
        } else
        {
            return new HDF5FileNode(this, entry.getPath(), entry.getName());
        }
    }

    /** @return child node with given path relative to this container */
    public IHierarchicalContentNode getChildNode(String childPath)
    {
        final IHDF5ContainerReader reader = createReader();
        try
        {
            final String fileName = FileUtilities.getFileNameFromRelativePath(childPath);
            if (reader.isGroup(childPath))
            {
                return new HDF5GroupNode(this, childPath, fileName);
            } else
            {
                return new HDF5FileNode(this, childPath, fileName);
            }
        } finally
        {
            reader.close();
        }
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return "HDF5ContainerBasedHierarchicalContentNode [root=" + root + ", container="
                + hdf5Container.getHDF5File() + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + hdf5Container.getHDF5File().hashCode();
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
        if (!hdf5Container.getHDF5File().equals(other.hdf5Container.getHDF5File()))
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

        @Override
        public String doGetRelativePath()
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

        public long getLastModified()
        {
            return file.lastModified();
        }

        public File getFile() throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException("This is not a normal directory node.");
        }

        @Override
        protected List<IHierarchicalContentNode> doGetChildNodes()
        {
            IHDF5ContainerReader reader = createReader();
            try
            {
                final List<IHierarchicalContentNode> result =
                        new ArrayList<IHierarchicalContentNode>();
                final List<ArchiveEntry> children = reader.getGroupMembers(relativePath);
                for (ArchiveEntry childEntry : children)
                {
                    if (childEntry.isDirectory())
                    {
                        result.add(new HDF5GroupNode(containerNode, childEntry.getPath(),
                                childEntry.getName()));
                    } else
                    {
                        result.add(new HDF5FileNode(containerNode, childEntry.getPath(), childEntry
                                .getName()));
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

        private HDF5DataSetBasedContent contentOrNull;

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

        @Override
        public String doGetRelativePath()
        {
            return containerNode.getRelativePath() + File.separator + relativePath;
        }

        public boolean exists()
        {
            return getContent().exists();
        }

        public boolean isDirectory()
        {
            return false;
        }

        public long getLastModified()
        {
            return file.lastModified();
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

        private HDF5DataSetBasedContent getContent()
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

    private static HDF5DataSetBasedContent extractFileContent(File hdf5File, String dataSetPath)
    {
        return new HDF5DataSetBasedContent(hdf5File, dataSetPath);
    }

    public static class HDF5DataSetBasedContent implements Closeable
    {
        private final File hdf5File;

        private final String dataSetPath;

        private final String name;

        private final boolean exists;

        private final long size;

        private final List<HDF5DataSetRandomAccessFile> randomAccessFiles;

        public HDF5DataSetBasedContent(File hdf5File, String dataSetPath)
        {
            this.hdf5File = hdf5File;
            this.dataSetPath = dataSetPath;
            this.name = FileUtilities.getFileNameFromRelativePath(dataSetPath);
            final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(hdf5File);
            try
            {
                if (reader.isDataSet(dataSetPath))
                {
                    this.exists = true;
                    this.size = reader.getSize(dataSetPath);
                } else
                {
                    this.exists = false;
                    this.size = 0L;
                }
            } finally
            {
                reader.close();
            }
            this.randomAccessFiles = new ArrayList<HDF5DataSetRandomAccessFile>();
        }

        public String tryGetName()
        {
            return name;
        }

        public long getSize()
        {
            return size;
        }

        public boolean exists()
        {
            return exists;
        }

        public IRandomAccessFile getReadOnlyRandomAccessFile()
        {
            final HDF5DataSetRandomAccessFile randomAccessFile =
                    HDF5IOAdapterFactory.asRandomAccessFileReadOnly(hdf5File, dataSetPath);
            randomAccessFiles.add(randomAccessFile);
            return randomAccessFile;
        }

        public InputStream getInputStream()
        {
            return new AdapterIInputStreamToInputStream(getReadOnlyRandomAccessFile());
        }

        public void close()
        {
            for (HDF5DataSetRandomAccessFile raFile : randomAccessFiles)
            {
                raFile.close();
            }
        }
    }
}