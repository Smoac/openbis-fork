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

package ch.systemsx.cisd.bds;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;

/**
 * Test cases for corresponding {@link Utilities} class.
 * 
 * @author Christian Ribeaud
 */
public class UtilitiesTest extends AbstractFileSystemTestCase
{
    private final File createFile(final File dir, final String name) throws IOException
    {
        final File file = new File(dir, name);
        FileUtils.touch(file);
        assertTrue(file.exists());
        return file;
    }

    @Test
    public final void testGetNumber()
    {
        final IDirectory directory = (IDirectory) NodeFactory.createNode(workingDirectory);
        final String key = "age";
        final String value = "35";
        final IFile file = directory.addKeyValuePair(key, value);
        final File[] listFiles = workingDirectory.listFiles();
        assertEquals(1, listFiles.length);
        assertEquals(key, listFiles[0].getName());
        assertEquals(value, file.getStringContent().trim());
        boolean fail = true;
        try
        {
            Utilities.getNumber(null, null);
        } catch (AssertionError ex)
        {
            fail = false;
        }
        assertEquals(false, fail);
        try
        {
            Utilities.getNumber(directory, "doesNotExist");
            fail("File 'doesNotExist' missing");
        } catch (DataStructureException ex)
        {
            // Nothing to do here
        }
        assertEquals(35, Utilities.getNumber(directory, key));
    }

    @Test
    public final void testListNodes() throws IOException
    {
        boolean fail = true;
        try
        {
            Utilities.listNodes(null, null);
        } catch (AssertionError e)
        {
            fail = false;
        }
        assertEquals(false, fail);
        createFile(workingDirectory, "file1");
        final File dir1 = new File(workingDirectory, "dir1");
        dir1.mkdir();
        final File dir2 = new File(workingDirectory, "dir2");
        dir2.mkdir();
        createFile(dir1, "file2");
        createFile(dir2, "file3");
        final List<String> nodes =
                Utilities.listNodes(NodeFactory.createDirectoryNode(workingDirectory), null);
        assertEquals(3, nodes.size());
        Collections.sort(nodes);
        assertEquals("dir1/file2", nodes.get(0));
        assertEquals("dir2/file3", nodes.get(1));
        assertEquals("file1", nodes.get(2));
    }

    @Test
    public final void testGetBoolean()
    {
        boolean fail = true;
        try
        {
            Utilities.getBoolean(null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertEquals(false, fail);
        final IDirectory directory = (IDirectory) NodeFactory.createNode(workingDirectory);
        try
        {
            Utilities.getBoolean(directory, "doesNotExist");
            fail("File 'doesNotExist' missing");
        } catch (final DataStructureException e)
        {
            // Nothing to do here.
        }
        final String key = "bool";
        final String value = "TRUE";
        final IFile file = directory.addKeyValuePair(key, value);
        final File[] listFiles = workingDirectory.listFiles();
        assertEquals(1, listFiles.length);
        assertEquals(key, listFiles[0].getName());
        assertEquals(value, file.getStringContent().trim());
        assertTrue(Utilities.getBoolean(directory, key).toBoolean());
        directory.addKeyValuePair(key, "yes");
        try
        {
            Utilities.getBoolean(directory, key);
            fail("Given value is not a boolean.");
        } catch (final DataStructureException ex)
        {
            // Nothing to do here.
        }
        directory.addKeyValuePair(key, " FALSE ");
        assertFalse(Utilities.getBoolean(directory, key).toBoolean());
    }

    @Test
    public void testBooleanToString()
    {
        assertEquals("TRUE", Utilities.Boolean.TRUE.toString());
        assertEquals("FALSE", Utilities.Boolean.FALSE.toString());
    }
    @Test
    public void testBooleanFromString()
    {
        assertEquals(Utilities.Boolean.TRUE, Utilities.Boolean.fromString("TRUE"));
        assertEquals(Utilities.Boolean.TRUE, Utilities.Boolean.fromString("true"));
        assertEquals(Utilities.Boolean.FALSE, Utilities.Boolean.fromString("FALSE"));
        assertEquals(Utilities.Boolean.FALSE, Utilities.Boolean.fromString("false"));
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBooleanFromStringError()
    {
        Utilities.Boolean.fromString("yes");
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBooleanFromStringNull()
    {
        Utilities.Boolean.fromString(null);
    }
    
}