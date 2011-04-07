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
        return getNode("/");
    }

    public IHierarchicalContentNode getNode(String relativePath)
    {
        return new SimpleFileBasedHierarchicalContentNode(this, new File(root, relativePath));
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

    static class SimpleFileBasedHierarchicalContentNode implements IHierarchicalContentNode
    {
        private final SimpleFileBasedHierarchicalContent parent;

        private final File file;

        SimpleFileBasedHierarchicalContentNode(SimpleFileBasedHierarchicalContent parent, File file)
        {
            assert parent != null;
            assert file != null;
            this.parent = parent;
            this.file = file;
        }

        public String getName()
        {
            return file.getName();
        }

        public List<IHierarchicalContentNode> getChildNodes()
        {
            File[] files = file.listFiles();
            List<IHierarchicalContentNode> result = new ArrayList<IHierarchicalContentNode>();
            if (files != null)
            {
                for (File aFile : files)
                {
                    result.add(new SimpleFileBasedHierarchicalContentNode(parent, aFile));
                }
            }
            return result;
        }

        public File getFile()
        {
            return file;
        }

        public IRandomAccessFile getFileContent()
        {
            return new RandomAccessFileImpl(file, "r");
        }

        public InputStream getInputStream()
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
            return "SimpleFileBasedHierarchicalContentNode [parent=" + parent + ", file=" + file
                    + "]";
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + file.hashCode();
            result = prime * result + parent.hashCode();
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
            if (!parent.equals(other.parent))
            {
                return false;
            }
            return true;
        }

    }

}
