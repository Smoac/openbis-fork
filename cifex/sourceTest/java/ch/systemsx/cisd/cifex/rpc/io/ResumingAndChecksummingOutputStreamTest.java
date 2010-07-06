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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.zip.CRC32;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for the {@link ResumingAndChecksummingOutputStream}.
 * 
 * @author Bernd Rinn
 */
public class ResumingAndChecksummingOutputStreamTest
{

    private static final File unitTestRootDirectory =
            new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory =
            new File(unitTestRootDirectory, "ResumingAndChecksummingOutputStreamTest");

    @BeforeClass
    public void init()
    {
        workingDirectory.mkdirs();
        assertTrue(unitTestRootDirectory.isDirectory());
    }

    @Test
    public void testWrite1() throws IOException
    {
        final int chunkSize = 10;
        final byte content = (byte) 213;
        final CRC32 crc32 = new CRC32();
        crc32.update(content);
        final File f = new File(workingDirectory, "file1");
        f.delete();
        f.deleteOnExit();
        final ResumingAndChecksummingOutputStream s =
                new ResumingAndChecksummingOutputStream(f, chunkSize,
                        new ISimpleChecksummingProgressListener()
                            {
                                public void update(long size, int crc32Value)
                                {
                                    assertEquals(1, size);
                                    assertEquals((int) crc32.getValue(), crc32Value);
                                }
                                public void exceptionThrown(IOException e)
                                {
                                }
                            });
        s.write(content);
        s.close();
        assertEquals(1, f.length());
    }

    @Test
    public void testWrite2() throws IOException
    {
        final int chunkSize = 10;
        final byte[] content = new byte[]
            { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };
        final CRC32 crc32 = new CRC32();
        crc32.update(content);
        final File f = new File(workingDirectory, "file2");
        f.delete();
        f.deleteOnExit();
        final ResumingAndChecksummingOutputStream s =
                new ResumingAndChecksummingOutputStream(f, chunkSize,
                        new ISimpleChecksummingProgressListener()
                            {
                                public void update(long size, int crc32Value)
                                {
                                    assertEquals(content.length, size);
                                    assertEquals((int) crc32.getValue(), crc32Value);
                                }
                                public void exceptionThrown(IOException e)
                                {
                                }
                            });
        s.write(content);
        s.close();
        assertEquals(content.length, f.length());
    }

    @Test
    public void testWrite3() throws IOException
    {
        final int chunkSize = 10;
        final byte[] content = new byte[]
            { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };
        final CRC32[] crc32 = new CRC32[]
            { new CRC32(), new CRC32() };
        crc32[0].update(content, 0, 10);
        crc32[1].update(content);
        final File f = new File(workingDirectory, "file3");
        f.delete();
        f.deleteOnExit();
        final int[] count = new int[]
            { 0 };
        final ResumingAndChecksummingOutputStream s =
                new ResumingAndChecksummingOutputStream(f, chunkSize,
                        new ISimpleChecksummingProgressListener()
                            {
                                public void update(long bytesWritten, int crc32Value)
                                {
                                    assertEquals(chunkSize * (count[0] + 1), bytesWritten);
                                    assertEquals(crc32[count[0]].getValue(), crc32Value);
                                    ++count[0];
                                }
                                public void exceptionThrown(IOException e)
                                {
                                }
                            });
        s.write(content, 0, chunkSize);
        s.write(content, chunkSize, chunkSize);
        s.close();
        assertEquals(2, count[0]);
        assertEquals(content.length, f.length());
    }

    @Test
    public void testWrite4() throws IOException
    {
        final int chunkSize = 10;
        final byte[] content = new byte[]
            { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
        final CRC32[] crc32 = new CRC32[]
            { new CRC32(), new CRC32(), new CRC32() };
        crc32[0].update(content, 0, chunkSize);
        crc32[1].update(content, 0, 2 * chunkSize);
        crc32[2].update(content);
        final File f = new File(workingDirectory, "file4");
        f.delete();
        f.deleteOnExit();
        final int[] count = new int[]
            { 0 };
        final ResumingAndChecksummingOutputStream s =
                new ResumingAndChecksummingOutputStream(f, chunkSize,
                        new ISimpleChecksummingProgressListener()
                            {
                                public void update(long bytesWritten, int crc32Value)
                                {
                                    if (count[0] < 2)
                                    {
                                        assertEquals(chunkSize * (count[0] + 1), bytesWritten);
                                    } else
                                    {
                                        assertEquals(content.length, bytesWritten);
                                    }
                                    assertEquals(crc32[count[0]].getValue(), crc32Value);
                                    ++count[0];
                                }
                                public void exceptionThrown(IOException e)
                                {
                                }
                            });
        s.write(content, 0, chunkSize);
        s.write(content, chunkSize, chunkSize);
        s.write(content[content.length - 1]);
        s.close();
        assertEquals(3, count[0]);
        assertEquals(content.length, f.length());
    }

    @Test
    public void testWriteResume() throws IOException
    {
        final int chunkSize = 10;
        final byte[] content = new byte[]
            { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
        final CRC32[] crc32 = new CRC32[]
            { new CRC32(), new CRC32(), new CRC32() };
        crc32[0].update(content, 0, chunkSize);
        crc32[1].update(content, 0, 2 * chunkSize);
        crc32[2].update(content);
        final File f = new File(workingDirectory, "fileResume");
        f.delete();
        f.deleteOnExit();
        final int[] count = new int[]
            { 0 };
        ResumingAndChecksummingOutputStream s =
                new ResumingAndChecksummingOutputStream(f, chunkSize,
                        new ISimpleChecksummingProgressListener()
                            {
                                public void update(long bytesWritten, int crc32Value)
                                {
                                    assertEquals(0, count[0]);
                                    assertEquals(chunkSize, bytesWritten);
                                    assertEquals(crc32[0].getValue(), crc32Value);
                                    ++count[0];
                                }
                                public void exceptionThrown(IOException e)
                                {
                                }
                            });
        s.write(content, 0, chunkSize);
        s.close();
        s =
                new ResumingAndChecksummingOutputStream(f, chunkSize,
                        new ISimpleChecksummingProgressListener()
                            {
                                public void update(long bytesWritten, int crc32Value)
                                {
                                    if (count[0] < 2)
                                    {
                                        assertEquals(chunkSize * (count[0] + 1), bytesWritten);
                                    } else
                                    {
                                        assertEquals(content.length, bytesWritten);
                                    }
                                    assertEquals(crc32[count[0]].getValue(), crc32Value);
                                    ++count[0];
                                }
                                public void exceptionThrown(IOException e)
                                {
                                }
                            }, chunkSize, (int) crc32[0].getValue());
        s.write(content, chunkSize, chunkSize);
        s.write(content[content.length - 1]);
        s.close();
        assertEquals(3, count[0]);
        assertEquals(content.length, f.length());
    }

    @Test
    public void testWriteResume2() throws IOException
    {
        final int chunkSize = 10;
        final byte[] content = new byte[]
            { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
        final CRC32[] crc32 = new CRC32[]
            { new CRC32(), new CRC32(), new CRC32() };
        crc32[0].update(content, 0, chunkSize);
        crc32[1].update(content, 0, 2 * chunkSize);
        crc32[2].update(content);
        final File f = new File(workingDirectory, "fileResume2");
        f.delete();
        f.deleteOnExit();
        final int[] count = new int[]
            { 0 };
        ResumingAndChecksummingOutputStream s =
                new ResumingAndChecksummingOutputStream(f, chunkSize,
                        new ISimpleChecksummingProgressListener()
                            {
                                public void update(long bytesWritten, int crc32Value)
                                {
                                    assertEquals(chunkSize * (count[0] + 1), bytesWritten);
                                    assertEquals(crc32[count[0]].getValue(), crc32Value);
                                    ++count[0];
                                }
                                public void exceptionThrown(IOException e)
                                {
                                }
                            });
        s.write(content, 0, chunkSize);
        s.write(content, chunkSize, chunkSize);
        s.close();
        assertEquals(count[0], 2);
        --count[0];
        s =
                new ResumingAndChecksummingOutputStream(f, chunkSize,
                        new ISimpleChecksummingProgressListener()
                            {
                                public void update(long bytesWritten, int crc32Value)
                                {
                                    if (count[0] < 2)
                                    {
                                        assertEquals(chunkSize * (count[0] + 1), bytesWritten);
                                    } else
                                    {
                                        assertEquals(content.length, bytesWritten);
                                    }
                                    assertEquals(crc32[count[0]].getValue(), crc32Value);
                                    ++count[0];
                                }
                                public void exceptionThrown(IOException e)
                                {
                                }
                            }, chunkSize, (int) crc32[0].getValue());
        s.write(content, chunkSize, chunkSize);
        s.write(content[content.length - 1]);
        s.close();
        assertEquals(3, count[0]);
        assertEquals(content.length, f.length());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFailResumeOnStartPosExceedsLength() throws IOException
    {
        final int chunkSize = 10;
        final byte[] content = new byte[]
            { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
        final CRC32[] crc32 = new CRC32[]
            { new CRC32(), new CRC32(), new CRC32() };
        crc32[0].update(content, 0, chunkSize);
        crc32[1].update(content, 0, 2 * chunkSize);
        crc32[2].update(content);
        final File f = new File(workingDirectory, "fileExceeds");
        f.delete();
        f.deleteOnExit();
        final int[] count = new int[]
            { 0 };
        new ResumingAndChecksummingOutputStream(f, chunkSize,
                new ISimpleChecksummingProgressListener()
                    {
                        public void update(long bytesWritten, int crc32Value)
                        {
                            if (count[0] < 2)
                            {
                                assertEquals(chunkSize * (count[0] + 1), bytesWritten);
                            } else
                            {
                                assertEquals(content.length, bytesWritten);
                            }
                            assertEquals(crc32[count[0]].getValue(), crc32Value);
                            ++count[0];
                        }
                        public void exceptionThrown(IOException e)
                        {
                        }
                    }, 10, 0);
    }
}
