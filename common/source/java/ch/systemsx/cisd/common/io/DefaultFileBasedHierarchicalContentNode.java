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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.base.io.RandomAccessFileImpl;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * {@link IHierarchicalContent} implementation for normal {@link java.io.File}.
 * 
 * @author Piotr Buczek
 */
class DefaultFileBasedHierarchicalContentNode extends AbstractHierarchicalContentNode
{
    protected final IHierarchicalContent root;

    protected final File file;

    protected final IHierarchicalContentFactory hierarchicalContentFactory;

    protected DefaultFileBasedHierarchicalContentNode(
            IHierarchicalContentFactory hierarchicalContentFactory, IHierarchicalContent root,
            File file)
    {
        assert hierarchicalContentFactory != null;
        assert root != null;
        assert file != null;
        this.hierarchicalContentFactory = hierarchicalContentFactory;
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

    public File getFile()
    {
        return file;
    }

    public boolean exists()
    {
        return file.exists();
    }

    public boolean isDirectory()
    {
        return file.isDirectory();
    }

    @Override
    public List<IHierarchicalContentNode> doGetChildNodes()
    {
        File[] files = file.listFiles();
        Arrays.sort(files);
        List<IHierarchicalContentNode> result = new ArrayList<IHierarchicalContentNode>();
        if (files != null)
        {
            for (File aFile : files)
            {
                result.add(hierarchicalContentFactory.asHierarchicalContentNode(root, aFile));
            }
        }
        return result;
    }

    @Override
    public long doGetFileLength()
    {
        return file.length();
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
        return "DefaultFileBasedHierarchicalContentNode [root=" + root + ", file=" + file + "]";
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
        if (!(obj instanceof DefaultFileBasedHierarchicalContentNode))
        {
            return false;
        }
        DefaultFileBasedHierarchicalContentNode other =
                (DefaultFileBasedHierarchicalContentNode) obj;
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