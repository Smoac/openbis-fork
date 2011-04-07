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
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
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

    // TODO Implement hash/equals

    class SimpleFileBasedHierarchicalContentNode implements IHierarchicalContentNode
    {
        @SuppressWarnings("unused")
        private final SimpleFileBasedHierarchicalContent parent;

        private final File file;

        private SimpleFileBasedHierarchicalContentNode(SimpleFileBasedHierarchicalContent parent,
                File file)
        {
            this.parent = parent;
            this.file = file;
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

        public OutputStream getOutputStream()
        {
            try
            {
                return new FileOutputStream(file);
            } catch (FileNotFoundException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }

        // TODO Implement hash/equals
    }
}
