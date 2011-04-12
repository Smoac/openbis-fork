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

package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.common.io.AbstractHierarchicalDirectoryContentNode;
import ch.systemsx.cisd.common.io.AbstractHierarchicalFileContentNode;
import ch.systemsx.cisd.common.io.ByteArrayBasedContent;
import ch.systemsx.cisd.common.io.DefaultFileBasedHierarchicalContentNode;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.common.io.IHierarchicalContentFactory;
import ch.systemsx.cisd.common.io.IHierarchicalContentNode;
import ch.systemsx.cisd.hdf5.IHDF5SimpleReader;

/**
 * {@link IHierarchicalContent} implementation for an HDF5 container.
 * 
 * @author Piotr Buczek
 */
class HDF5ContainerBasedHierarchicalContentNode extends DefaultFileBasedHierarchicalContentNode
{
    private final Hdf5Container hdf5Container;

    private IHDF5SimpleReader reader;

    HDF5ContainerBasedHierarchicalContentNode(
            IHierarchicalContentFactory hierarchicalContentFactory, IHierarchicalContent root,
            File hdf5ContainerFile)
    {
        super(hierarchicalContentFactory, root, hdf5ContainerFile);
        this.hdf5Container = new Hdf5Container(hdf5ContainerFile);
        this.reader = hdf5Container.createSimpleReader(); // TODO close
    }

    public void close()
    {
        reader.close();
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
        List<String> childPaths = reader.getGroupMembers("/");
        List<IHierarchicalContentNode> result = new ArrayList<IHierarchicalContentNode>();
        for (String childPath : childPaths)
        {
            System.err.println("child: " + childPath);
            if (reader.isGroup(childPath))
            {
                result.add(new HDF5GroupNode(childPath, childPath));
            } else
            {
                result.add(new HDF5FileNode(childPath, childPath));
            }
        }
        return result;
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

        private final String relativePath;

        private final String groupName;

        public HDF5GroupNode(String relativePath, String groupName)
        {
            System.err.println("[HDF5GroupNode] path:" + relativePath + ", group: " + groupName);
            this.relativePath = relativePath;
            this.groupName = groupName;
        }

        public String getName()
        {
            return groupName;
        }

        public String getRelativePath()
        {
            return relativePath;
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
            System.err.println("traverse: " + getRelativePath());
            List<IHierarchicalContentNode> result = new ArrayList<IHierarchicalContentNode>();
            List<String> children = reader.getGroupMembers(relativePath);
            for (String childName : children)
            {
                String newRelativePath = getRelativePath() + File.separator + childName;
                if (reader.isGroup(newRelativePath))
                {
                    result.add(new HDF5GroupNode(newRelativePath, childName));
                } else
                {
                    result.add(new HDF5FileNode(newRelativePath, childName));
                }
            }
            return result;
        }

    }

    class HDF5FileNode extends AbstractHierarchicalFileContentNode
    {

        private final String relativePath;

        private final String fileName;

        private IContent contentOrNull;

        public HDF5FileNode(String relativePath, String fileName)
        {
            System.err.println("[HDF5FileNode] path:" + relativePath + ", fileName: " + fileName);
            this.relativePath = relativePath;
            this.fileName = fileName;
        }

        public String getName()
        {
            return fileName;
        }

        public String getRelativePath()
        {
            return relativePath;
        }

        public boolean exists()
        {
            return true;
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
            return reader.getDataSetInformation(relativePath).getSize();
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
                contentOrNull = extractFileContent(reader, relativePath);
            }
            return contentOrNull;
        }
    }

    private static IContent extractFileContent(IHDF5SimpleReader reader, String path)
    {
        byte[] content = reader.readAsByteArray(path);
        int index = path.lastIndexOf('/');
        return new ByteArrayBasedContent(content, index < 0 ? path : path.substring(index + 1));
    }

}