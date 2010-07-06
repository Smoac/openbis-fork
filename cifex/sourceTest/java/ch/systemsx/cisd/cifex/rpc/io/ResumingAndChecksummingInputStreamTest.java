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

import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.cifex.rpc.io.ResumingAndChecksummingInputStream.ChecksumHandling;

import static org.testng.AssertJUnit.*;

/**
 * Test cases for the {@link ResumingAndChecksummingInputStream}.
 * 
 * @author Bernd Rinn
 */
public class ResumingAndChecksummingInputStreamTest
{

    private static final File unitTestRootDirectory =
            new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory =
            new File(unitTestRootDirectory, "ResumingAndChecksummingInputStreamTest");

    private static final File file = new File(workingDirectory, "file");

    private static final byte[] content = new byte[]
        { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23 };

    private static final int[] contentCrc32Values = new int[3];

    static
    {
        final CRC32 crc32 = new CRC32();
        crc32.update(content, 0, 10);
        contentCrc32Values[0] = (int) crc32.getValue();
        crc32.update(content, 10, 10);
        contentCrc32Values[1] = (int) crc32.getValue();
        crc32.update(content, 20, 4);
        contentCrc32Values[2] = (int) crc32.getValue();
    }

    @BeforeClass
    public void init() throws IOException
    {
        workingDirectory.mkdirs();
        assertTrue(unitTestRootDirectory.isDirectory());
        file.deleteOnExit();
        final OutputStream s = new FileOutputStream(file);
        s.write(content);
        s.close();
    }

    @Test
    public void testRead1() throws IOException
    {
        final int chunkSize = 10;
        final int[] count = new int[] { 0 };
        final ResumingAndChecksummingInputStream s =
                new ResumingAndChecksummingInputStream(file, chunkSize, new ISimpleChecksummingProgressListener()
                    {
                        public void update(long bytesRead, int crc32Value)
                        {
                            if (count[0] < 2)
                            {
                                assertEquals(chunkSize * (count[0] + 1), bytesRead);
                            } else
                            {
                                assertEquals(content.length, bytesRead);
                            }
                            assertEquals(contentCrc32Values[count[0]], crc32Value);
                            ++count[0];
                        }
                        public void exceptionThrown(IOException e)
                        {
                        }
                    }, ChecksumHandling.COMPUTE);
        int bcount = 0;
        int b;
        while ((b = s.read()) >= 0)
        {
            assertEquals(content[bcount++], b);
        }
        assertEquals(content.length, bcount);
        assertEquals(contentCrc32Values[2], s.getCrc32Value());
        assertEquals(3, count[0]);
        s.close();
    }

    @Test
    public void testRead2() throws IOException
    {
        final int chunkSize = 10;
        final int[] count = new int[] { 0 };
        final ResumingAndChecksummingInputStream s =
                new ResumingAndChecksummingInputStream(file, chunkSize, new ISimpleChecksummingProgressListener()
                    {
                        public void update(long bytesRead, int crc32Value)
                        {
                            assertEquals(content.length, bytesRead);
                            assertEquals(contentCrc32Values[2], crc32Value);
                            ++count[0];
                        }
                        public void exceptionThrown(IOException e)
                        {
                        }
                    }, ChecksumHandling.COMPUTE);
        final byte[] b = new byte[3 * chunkSize];
        assertEquals(content.length, s.read(b));
        assertEquals(contentCrc32Values[2], s.getCrc32Value());
        assertEquals(1, count[0]);
        s.close();
    }

    @Test
    public void testReadWithoutChecksum() throws IOException
    {
        final int chunkSize = 10;
        final int[] count = new int[] { 0 };
        final ResumingAndChecksummingInputStream s =
                new ResumingAndChecksummingInputStream(file, chunkSize, new ISimpleChecksummingProgressListener()
                    {
                        public void update(long bytesRead, int crc32Value)
                        {
                            assertEquals(content.length, bytesRead);
                            assertEquals(0, crc32Value);
                            ++count[0];
                        }
                        public void exceptionThrown(IOException e)
                        {
                        }
                    }, ChecksumHandling.DONT_COMPUTE);
        final byte[] b = new byte[3 * chunkSize];
        assertEquals(content.length, s.read(b));
        assertEquals(0, s.getCrc32Value());
        assertEquals(1, count[0]);
        s.close();
    }

    @Test
    public void testReadAppendChecksum() throws IOException
    {
        final int chunkSize = 10;
        final int[] count = new int[] { 0 };
        final ResumingAndChecksummingInputStream s =
                new ResumingAndChecksummingInputStream(file, chunkSize, new ISimpleChecksummingProgressListener()
                    {
                        public void update(long bytesRead, int crc32Value)
                        {
                            assertEquals(content.length, bytesRead);
                            assertEquals(contentCrc32Values[2], crc32Value);
                            ++count[0];
                        }
                        public void exceptionThrown(IOException e)
                        {
                        }
                    }, ChecksumHandling.COMPUTE_AND_APPEND);
        final byte[] b = new byte[3 * chunkSize];
        assertEquals(content.length, s.read(b, 0, content.length));
        final byte[] checksumBytes = new byte[4];
        assertEquals(checksumBytes.length, s.read(checksumBytes, 0, 4));
        assertEquals(-1, s.read());
        final int checksumFromStream = IntConversionUtils.bytesToInt(checksumBytes);
        assertEquals(contentCrc32Values[2], s.getCrc32Value());
        assertEquals(contentCrc32Values[2], checksumFromStream);
        assertEquals(1, count[0]);
        s.close();
    }

    @Test
    public void testReadResume() throws IOException
    {
        final int chunkSize = 10;
        final int[] count = new int[] { 0 };
        final ResumingAndChecksummingInputStream s =
                new ResumingAndChecksummingInputStream(file, 10, new ISimpleChecksummingProgressListener()
                    {
                        public void update(long bytesRead, int crc32Value)
                        {
                            assertEquals(content.length, bytesRead);
                            assertEquals(contentCrc32Values[2], crc32Value);
                            ++count[0];
                        }
                        public void exceptionThrown(IOException e)
                        {
                        }
                    }, chunkSize, ChecksumHandling.COMPUTE);
        final byte[] b = new byte[3 * chunkSize];
        assertEquals(content.length - chunkSize, s.read(b));
        assertEquals(contentCrc32Values[2], s.getCrc32Value());
        assertEquals(1, count[0]);
        s.close();
    }

    @Test
    public void testReadResumePrecomputedCRC() throws IOException
    {
        final int chunkSize = 10;
        final int[] count = new int[] { 0 };
        final ResumingAndChecksummingInputStream s =
                new ResumingAndChecksummingInputStream(file, 10, new ISimpleChecksummingProgressListener()
                    {
                        public void update(long bytesRead, int crc32Value)
                        {
                            assertEquals(content.length, bytesRead);
                            assertEquals(contentCrc32Values[2], crc32Value);
                            ++count[0];
                        }
                        public void exceptionThrown(IOException e)
                        {
                        }
                    }, chunkSize, contentCrc32Values[0], ChecksumHandling.COMPUTE);
        final byte[] b = new byte[3 * chunkSize];
        assertEquals(content.length - chunkSize, s.read(b));
        assertEquals(contentCrc32Values[2], s.getCrc32Value());
        assertEquals(1, count[0]);
        s.close();
    }

}
