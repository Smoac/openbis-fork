/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.harvester.synchronizer;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
class ChecksummmingInputStream extends FilterInputStream
{

    private final CRC32 crc32;

    private long totalLength;

    ChecksummmingInputStream(InputStream in)
    {
        super(in);
        crc32 = new CRC32();
    }

    public int read() throws IOException
    {
        int value = in.read();
        if (value > 0)
        {
            crc32.update(value);
            totalLength++;
        }
        return value;
    }

    public int read(byte[] data, int offset, int length) throws IOException
    {
        int result = in.read(data, offset, length);
        if (result > 0)
        {
            crc32.update(data, offset, result);
            this.totalLength += result;
        }
        return result;
    }

    public long checksum()
    {
        return crc32.getValue();
    }

    public long getLength()
    {
        return totalLength;
    }
}

// class Main {
// public static void main(String[] args) throws IOException {
// FileInputStream fis = new FileInputStream(new File("akin_dinner.mp4"));
// CheckSummingInputStream checkSummingInputStream = new CheckSummingInputStream(fis);
// BufferedInputStream bis = new BufferedInputStream(checkSummingInputStream, 1 << 18);
// long start = System.currentTimeMillis();
// Files.copy(bis, Paths.get("akin_dinner_copy.mp4"), StandardCopyOption.REPLACE_EXISTING);
// long end = System.currentTimeMillis();
// System.out.println("Total: " + (end - start) + " ms.");
// System.out.println("Crc: " + checkSummingInputStream.checksum());
// System.out.println("Length: " + checkSummingInputStream.getLength());
// }
// }
