/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds.storage.filesystem;

import static org.testng.AssertJUnit.assertEquals;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * Test cases for corresponding {@link File} class.
 * 
 * @author Franz-Josef Elmer
 */
public final class FileTest extends AbstractFileSystemTestCase
{
    @Test
    public void testGetValueAndGetName()
    {
        final java.io.File file = new java.io.File(workingDirectory, "test.txt");
        FileUtilities.writeToFile(file, "Hello\nworld!\n");
        final IFile stringFile = NodeFactory.createFileNode(file);

        assertEquals("test.txt", stringFile.getName());
        assertEquals("Hello\nworld!\n", stringFile.getStringContent());
        assertEquals("Hello\nworld!\n", new String(stringFile.getBinaryContent()));
    }

    @Test
    public void testExtractTo()
    {
        final java.io.File file = new java.io.File(workingDirectory, "test.txt");
        FileUtilities.writeToFile(file, "Hello\nworld!\n");
        final IFile stringFile = NodeFactory.createFileNode(file);

        java.io.File subdir = new java.io.File(workingDirectory, "subdir");
        subdir.mkdir();
        stringFile.extractTo(subdir);
        assertEquals("Hello\nworld!\n", FileUtilities.loadToString(new java.io.File(subdir,
                stringFile.getName())));
    }

    @Test
    public void testMoveTo()
    {
        final java.io.File dir = new java.io.File(workingDirectory, "dir");
        dir.mkdirs();
        final IDirectory directory = NodeFactory.createDirectoryNode(dir);
        IFile file = directory.addKeyValuePair("p1", "property 1");

        file.moveTo(workingDirectory);

        assertEquals(false, directory.iterator().hasNext());
        assertEquals("property 1", FileUtilities.loadToString(
                new java.io.File(workingDirectory, "p1")).trim());
    }

    @Test
    public void testGetInputStream() throws Exception
    {
        final java.io.File file = new java.io.File(workingDirectory, "test");
        FileOutputStream fileOutputStream = null;
        try
        {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(new byte[]
                { 1, 2, 3, 4 });
        } catch (IOException ex)
        {
            throw ex;
        } finally
        {
            IOUtils.closeQuietly(fileOutputStream);
        }

        File binaryFile = new File(file);
        InputStream inputStream = binaryFile.getInputStream();
        try
        {
            byte[] bytes = new byte[5];
            inputStream.read(bytes);
            assertEquals(1, bytes[0]);
            assertEquals(2, bytes[1]);
            assertEquals(3, bytes[2]);
            assertEquals(4, bytes[3]);
            assertEquals(0, bytes[4]);
        } catch (IOException ex)
        {
            throw ex;
        } finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }
}
