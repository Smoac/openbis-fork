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
import java.io.RandomAccessFile;

/**
 * Utility methods for CRC32 calculation.
 * 
 * @author Bernd Rinn
 */
class CRC32Utils
{

    private static final int CHUNK_SIZE = 16 * 1024;

    /**
     * Computes the CRC32 checksum from the first <var>count</var> bytes of <var>raFile</var>. The
     * file pointer is expected to be at position 0 at the beginning. When returning, the file
     * pointer will be <var>count</var>.
     */
    static CloneableCRC32 computeCRC32(final RandomAccessFile raFile, final long count)
            throws IOException
    {
        final CloneableCRC32 partialCrc32 = new CloneableCRC32();
        final byte[] chunk = new byte[CHUNK_SIZE];
        long bytesToRead = count - raFile.getFilePointer();
        while (bytesToRead > 0)
        {
            final int actualCount =
                    raFile.read(chunk, 0, (int) Math.min(bytesToRead, chunk.length));
            partialCrc32.update(chunk, 0, actualCount);
            bytesToRead -= actualCount;
        }
        return partialCrc32;
    }

}
