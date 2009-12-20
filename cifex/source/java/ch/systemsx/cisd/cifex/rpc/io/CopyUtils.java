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

package ch.systemsx.cisd.cifex.rpc.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utilities for copying files. The code has been taken from from Apache commons-io and modified.
 * 
 * @author Bernd Rinn
 */
public class CopyUtils
{

    /**
     * The default buffer size to use.
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 16;

    /**
     * Copy bytes from a large (over 2GB) <code>InputStream</code> to an <code>OutputStream</code>.
     * The input stream is expected to have a 32-bit checksum appended to the end of the actual
     * input stream like {@link ResumingAndChecksummingInputStream} has with
     * <code>appendChecksumToStream=true</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     * 
     * @param input the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @param size the number of bytes in the stream (after these number of bytes the checksum will
     *            be appended)
     * @param startPos the start position in the <var>input</var> stream (for size calculations)
     * @return the 32-bit checksum provided at the end of the input stream.
     * @throws NullPointerException if the input or output is null
     * @throws IOException if an I/O error occurs
     * @since Commons IO 1.3
     */
    public static int copyAndReturnChecksum(InputStream input, OutputStream output, long size,
            long startPos) throws IOException
    {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int n = 0;
        long count = startPos;
        int bytesToRead = (int) Math.min(DEFAULT_BUFFER_SIZE, size);
        while (-1 != (n = input.read(buffer, 0, bytesToRead)) && count < size)
        {
            output.write(buffer, 0, n);
            count += n;
            bytesToRead = (int) Math.min(DEFAULT_BUFFER_SIZE, size - count);
        }
        n = input.read(buffer, 0, 4);
        if (n != 4)
        {
            throw new IOException("Error reading checksum.");
        }
        return IntConversionUtils.bytesToInt(buffer);
    }

}
