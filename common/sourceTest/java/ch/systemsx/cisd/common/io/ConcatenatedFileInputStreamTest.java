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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;

/**
 * Tests for {@link ConcatenatedFileInputStream}
 * 
 * @author Tomasz Pylak
 */
public class ConcatenatedFileInputStreamTest extends AbstractFileSystemTestCase
{
    @Test
    public void testNoFiles() throws IOException
    {
        ConcatenatedFileInputStream stream = new ConcatenatedFileInputStream(false, new File[0]);
        AssertJUnit.assertEquals(-1, stream.read());
    }

    @Test
    public void testOneFile() throws IOException
    {
        String content = createLongString("1");
        File file = createFile(content, "f1.txt");
        ConcatenatedFileInputStream stream = new ConcatenatedFileInputStream(false, file);
        List<String> streamContent = readStrings(stream);
        assertEquals(1, streamContent.size());
        assertEquals(content, streamContent.get(0));
    }

    @Test
    public void testManyFiles() throws IOException
    {
        String content1 = createLongString("1");
        File file1 = createFile(content1, "f1.txt");

        String content2 = ""; // empty content
        File file2 = createFile(content2, "f2.txt");

        String content3 = createLongString("3");
        File file3 = createFile(content3, "f3.txt");

        ConcatenatedFileInputStream stream =
                new ConcatenatedFileInputStream(false, file1, file2, file3);
        List<String> streamContent = readStrings(stream);
        assertEquals(3, streamContent.size());
        assertEquals(content1, streamContent.get(0));
        assertEquals(content2, streamContent.get(1));
        assertEquals(content3, streamContent.get(2));
    }

    @Test
    public void testExistingAndNonExistingFiles() throws IOException
    {
        String content1 = createLongString("1");
        File file1 = createFile(content1, "f1.txt");

        File unexistingFile = new File(workingDirectory, "unexisting.txt");

        String content3 = createLongString("3");
        File file3 = createFile(content3, "f3.txt");

        File fileNull = null;

        ConcatenatedFileInputStream stream =
                new ConcatenatedFileInputStream(true, fileNull, file1, fileNull, unexistingFile,
                        file3, fileNull);
        List<String> streamContent = readStrings(stream);
        assertEquals(6, streamContent.size());
        assertEquals("", streamContent.get(0));
        assertEquals(content1, streamContent.get(1));
        assertEquals("", streamContent.get(2));
        assertEquals("", streamContent.get(3));
        assertEquals(content3, streamContent.get(4));
        assertEquals("", streamContent.get(5));

    }

    @Test
    public void testNonExistingFile()
    {
        File file = new File(workingDirectory, "f.txt");

        ConcatenatedFileInputStream stream = new ConcatenatedFileInputStream(false, file);
        try
        {
            readStrings(stream);
            fail("IOException expected");
        } catch (IOException ex)
        {
            assertEquals(file + " (No such file or directory)", ex.getMessage());
        }
    }

    @Test
    public void testNullFile() throws IOException
    {
        ConcatenatedFileInputStream stream = new ConcatenatedFileInputStream(false, new File[]
            { null });
        try
        {
            readStrings(stream);
            fail("NullPointerException expected");
        } catch (NullPointerException ex)
        {
        }
    }

    // --------- helpers

    private static List<String> readStrings(ConcatenatedFileInputStream stream) throws IOException
    {
        ConcatenatedFileOutputStreamWriter reader = new ConcatenatedFileOutputStreamWriter(stream);
        List<String> result = new ArrayList<String>();
        while (true)
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            long blockSize = reader.writeNextBlock(out);
            if (blockSize == -1)
            {
                break;
            }
            result.add(out.toString());
        }
        return result;
    }

    private File createFile(String content, String fileName) throws FileNotFoundException,
            IOException
    {
        File file = new File(workingDirectory, fileName);

        IOUtils.writeLines(Arrays.asList(content), "", new FileOutputStream(file));
        return file;
    }

    private static String createLongString(String text)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 9000; i++)
        {
            sb.append(text);
        }
        sb.append("\n");
        return sb.toString();
    }
}
