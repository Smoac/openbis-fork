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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.base.io.RandomAccessFileImpl;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * {@link IHierarchicalContent} implementation for normal {@link java.io.File}.
 * 
 * @author Chandrasekhar Ramakrishnan
 * @author Piotr Buczek
 */
class SimpleFileBasedHierarchicalContent implements IHierarchicalContent
{
    private final File root;

    SimpleFileBasedHierarchicalContent(File file)
    {
        if (file.exists() == false)
        {
            throw new IllegalArgumentException(file.getAbsolutePath() + " doesn't exist");
        }
        if (file.isDirectory() == false)
        {
            throw new IllegalArgumentException(file.getAbsolutePath() + " is not a directory");
        }
        this.root = file;
    }

    public IHierarchicalContentNode getRootNode()
    {
        return asNode(root);
    }

    public IHierarchicalContentNode getNode(String relativePath)
    {
        return asNode(new File(root, relativePath));
    }

    private IHierarchicalContentNode asNode(File file)
    {
        return new SimpleFileBasedHierarchicalContentNode(this, file);
    }

    public List<IHierarchicalContentNode> listMatchingNodes(final String pattern)
    {
        File[] files = root.listFiles(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name.matches(pattern);
                }
            });

        List<IHierarchicalContentNode> nodes = new ArrayList<IHierarchicalContentNode>();
        for (File file : files)
        {
            nodes.add(new SimpleFileBasedHierarchicalContentNode(this, file));
        }
        return nodes;
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return "SimpleFileBasedHierarchicalContent [root=" + root + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((root == null) ? 0 : root.hashCode());
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
        if (!(obj instanceof SimpleFileBasedHierarchicalContent))
        {
            return false;
        }
        SimpleFileBasedHierarchicalContent other = (SimpleFileBasedHierarchicalContent) obj;
        if (root == null)
        {
            if (other.root != null)
            {
                return false;
            }
        } else if (!root.equals(other.root))
        {
            return false;
        }
        return true;
    }

    static class SimpleFileBasedHierarchicalContentNode extends AbstractHierarchicalContentNode
    {
        private final SimpleFileBasedHierarchicalContent root;

        private final File file;

        SimpleFileBasedHierarchicalContentNode(SimpleFileBasedHierarchicalContent root, File file)
        {
            assert root != null;
            assert file != null;
            this.root = root;
            this.file = file;
        }

        public String getName()
        {
            return file.getName();
        }

        public String getRelativePath()
        {
            return FileUtilities.getRelativeFile(root.getRootNode().getFile(), file);
        }

        public boolean exists()
        {
            return file.exists();
        }

        public boolean isDirectory()
        {
            return file.isDirectory();
        }

        public File getFile()
        {
            return file;
        }

        @Override
        public long doGetSize()
        {
            return file.length();
        }

        @Override
        public List<IHierarchicalContentNode> doGetChildNodes()
        {
            File[] files = file.listFiles();
            List<IHierarchicalContentNode> result = new ArrayList<IHierarchicalContentNode>();
            if (files != null)
            {
                for (File aFile : files)
                {
                    result.add(new SimpleFileBasedHierarchicalContentNode(root, aFile));
                }
            }
            return result;
        }

        @Override
        public IRandomAccessFile doGetFileContent()
        {
            return new RandomAccessFileImpl(file, "r");
        }

        @Override
        public InputStream doGetInputStream()
        {
            try
            {
                return new FileInputStream(file);
            } catch (FileNotFoundException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }

        //
        // Object
        //

        @Override
        public String toString()
        {
            return "SimpleFileBasedHierarchicalContentNode [root=" + root + ", file=" + file + "]";
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + file.hashCode();
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
            if (!(obj instanceof SimpleFileBasedHierarchicalContentNode))
            {
                return false;
            }
            SimpleFileBasedHierarchicalContentNode other =
                    (SimpleFileBasedHierarchicalContentNode) obj;
            if (!file.equals(other.file))
            {
                return false;
            }
            if (!root.equals(other.root))
            {
                return false;
            }
            return true;
        }

    }

}
