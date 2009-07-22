/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

import org.apache.commons.io.input.CountingInputStream;

/**
 * An {@link InputStream} that computes a CRC32 checksum of the bytes it has seen.
 * 
 * @author Bernd Rinn
 */
public class ChecksummingInputStream extends CountingInputStream
{

    private final CRC32 crc32 = new CRC32();

    /**
     * Constructs a new ChecksummingInputStream.
     * 
     * @param in the InputStream to delegate to
     */
    public ChecksummingInputStream(InputStream in)
    {
        super(in);
    }

    @Override
    public int read() throws IOException
    {
        final int read = super.read();
        if (read >= 0)
        {
            crc32.update(read);
        }
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        final int read = super.read(b, off, len);
        if (read > 0)
        {
            crc32.update(b, off, read);
        }
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        final int read = super.read(b);
        if (read > 0)
        {
            crc32.update(b, 0, read);
        }
        return read;
    }
    
    /**
     * Returns the CRC32 checksum value of all bytes seen by this input stream.
     */
    public int getCRC32Value()
    {
        return (int) crc32.getValue();
    }

}
