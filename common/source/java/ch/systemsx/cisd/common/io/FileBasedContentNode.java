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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.base.io.RandomAccessFileImpl;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * File content. Wraps an instance of {@link File}.
 * 
 * @author Franz-Josef Elmer
 */
public class FileBasedContentNode implements IHierarchicalContentNode
{
    private final File file;

    /**
     * Creates an instance based on the specified file.
     */
    public FileBasedContentNode(File file)
    {
        this.file = file;
    }

    /**
     * Returns the name of the wrapped file.
     */
    public String tryGetName()
    {
        return file.getName();
    }

    /**
     * Returns <code>true</code> if the wrapped file exists.
     */
    public boolean exists()
    {
        return file.exists();
    }

    /**
     * Returns a new instance of {@link FileInputStream} for the wrapped file.
     */
    public InputStream getInputStream()
    {
        try
        {
            return new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public String getName()
    {
        return file.getName();
    }

    public String getRelativePath()
    {
        return file.getPath();
    }

    public String getParentRelativePath()
    {
        return file.getParent();
    }

    public boolean isDirectory()
    {
        return file.isDirectory();
    }

    public long getLastModified()
    {
        return file.lastModified();
    }

    public List<IHierarchicalContentNode> getChildNodes() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    public File getFile() throws UnsupportedOperationException
    {
        return file;
    }

    public File tryGetFile()
    {
        return file;
    }

    public long getFileLength() throws UnsupportedOperationException
    {
        return file.length();
    }

    public IRandomAccessFile getFileContent() throws UnsupportedOperationException,
            IOExceptionUnchecked
    {
        return new RandomAccessFileImpl(file, "r");
    }
}
