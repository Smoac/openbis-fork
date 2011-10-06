/*
 * Copyright 2010 ETH Zuerich, CISD
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
import java.util.List;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Content based on a {@link IContentProvider}.
 * 
 * @author Franz-Josef Elmer
 */
public class HierarchicalContentNodeBasedHierarchicalContentNode implements IHierarchicalContentNode
{
    private IHierarchicalContentNode content;

    private static final IHierarchicalContentNode DUMMY = new IHierarchicalContentNode()
        {
            public boolean isDirectory()
            {
                return false;
            }

            public String getRelativePath()
            {
                return null;
            }

            public String getParentRelativePath()
            {
                return null;
            }

            public String getName()
            {
                return null;
            }

            public long getLastModified()
            {
                return 0;
            }

            public InputStream getInputStream() throws UnsupportedOperationException,
                    IOExceptionUnchecked
            {
                return null;
            }

            public long getFileLength() throws UnsupportedOperationException
            {
                return 0;
            }

            public IRandomAccessFile getFileContent() throws UnsupportedOperationException,
                    IOExceptionUnchecked
            {
                return null;
            }

            public File getFile() throws UnsupportedOperationException
            {
                throw new UnsupportedOperationException();
            }

            public List<IHierarchicalContentNode> getChildNodes()
                    throws UnsupportedOperationException
            {
                throw new UnsupportedOperationException();
            }

            public boolean exists()
            {
                return false;
            }
        };

    /**
     * Creates an instance for specified content provider.
     */
    public HierarchicalContentNodeBasedHierarchicalContentNode(IHierarchicalContentNode content)
    {
        this.content = content;
    }

    public boolean exists()
    {
        return getContent().exists();
    }

    public InputStream getInputStream()
    {
        return getContent().getInputStream();
    }

    private IHierarchicalContentNode getContent()
    {
        if (content == null)
        {
            content = DUMMY;
        }
        return content;
    }

    public String getName()
    {
        return getContent().getName();
    }

    public String getRelativePath()
    {
        return getContent().getRelativePath();
    }

    public String getParentRelativePath()
    {
        return getContent().getParentRelativePath();
    }

    public boolean isDirectory()
    {
        return getContent().isDirectory();
    }

    public long getLastModified()
    {
        return getContent().getLastModified();
    }

    public List<IHierarchicalContentNode> getChildNodes() throws UnsupportedOperationException
    {
        return getContent().getChildNodes();
    }

    public File getFile() throws UnsupportedOperationException
    {
        return getContent().getFile();
    }

    public long getFileLength() throws UnsupportedOperationException
    {
        return getContent().getFileLength();
    }

    public IRandomAccessFile getFileContent() throws UnsupportedOperationException,
            IOExceptionUnchecked
    {
        return getContent().getFileContent();
    }

}
