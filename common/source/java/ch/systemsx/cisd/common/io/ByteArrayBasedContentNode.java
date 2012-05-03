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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.ByteBufferRandomAccessFile;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Content based on an array of bytes.
 * 
 * @author Franz-Josef Elmer
 */
public class ByteArrayBasedContentNode implements IHierarchicalContentNode
{
    private final byte[] byteArray;

    private final String nameOrNull;

    private long lastModified;
    
    private Long checksum;

    /**
     * Creates an instance for the specified byte array.
     * 
     * @param nameOrNull Name of the content or null
     */
    public ByteArrayBasedContentNode(byte[] byteArray, String nameOrNull)
    {
        this.byteArray = byteArray;
        this.nameOrNull = nameOrNull;
        this.lastModified = new Date().getTime();
    }

    /**
     * Returns always <code>true</code>.
     */
    public boolean exists()
    {
        return true;
    }

    /**
     * Returns an instance of {@link ByteArrayInputStream}.
     */
    public InputStream getInputStream()
    {
        return new ByteArrayInputStream(byteArray);
    }

    public String getName()
    {
        return nameOrNull;
    }

    public String getRelativePath()
    {
        throw new UnsupportedOperationException();
    }

    public String getParentRelativePath()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isDirectory()
    {
        return false;
    }

    public long getLastModified()
    {
        return lastModified;
    }

    public List<IHierarchicalContentNode> getChildNodes() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    public File getFile() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }
    
    public File tryGetFile()
    {
        return null;
    }

    public long getFileLength() throws UnsupportedOperationException
    {
        return byteArray.length;
    }

    public long getChecksumCRC32() throws UnsupportedOperationException
    {
        if (checksum == null)
        {
            checksum = IOUtilities.getChecksumCRC32(new ByteArrayInputStream(byteArray));
        }
        return checksum;
    }

    public IRandomAccessFile getFileContent() throws UnsupportedOperationException,
            IOExceptionUnchecked
    {
        return new ByteBufferRandomAccessFile(ByteBuffer.wrap(byteArray));
    }
}
