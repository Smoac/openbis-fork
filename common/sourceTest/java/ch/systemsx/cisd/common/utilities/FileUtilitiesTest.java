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

package ch.systemsx.cisd.common.utilities;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * Test cases for the {@link FileUtilities}.
 * 
 * @author Bernd Rinn
 */
public class FileUtilitiesTest
{
    private static final File workingDirectory = new File("targets" + File.separator + "unit-test-wd");

    @BeforeSuite
    public void init()
    {
        LogInitializer.init();
        workingDirectory.mkdirs();
        assert workingDirectory.isDirectory();
    }

    @Test
    public void moveFile()
    {
        final File root = new File(workingDirectory, "move-test");
        FileUtilities.deleteRecursively(root);
        root.mkdir();
        assert root.isDirectory();
        final File file = new File(root, "a");
        try
        {
            file.createNewFile();
        } catch (IOException e)
        {
            throw new CheckedExceptionTunnel(e);
        }
        final File destinationDir = new File(root, "d");
        destinationDir.mkdir();
        assert destinationDir.exists();
        FileUtilities.movePath(file, destinationDir);
        assert file.exists() == false;
        final File newFile = new File(destinationDir, "a");
        assert newFile.exists();
        FileUtilities.deleteRecursively(root);
    }

    @Test
    public void moveDirectory()
    {
        final File root = new File(workingDirectory, "move-test");
        FileUtilities.deleteRecursively(root);
        root.mkdir();
        assert root.isDirectory();
        final File file = new File(root, "a");
        file.mkdir();
        assert file.isDirectory();
        final File destinationDir = new File(root, "d");
        destinationDir.mkdir();
        assert destinationDir.exists();
        FileUtilities.movePath(file, destinationDir);
        assert file.exists() == false;
        final File newFile = new File(destinationDir, "a");
        assert newFile.exists();
        FileUtilities.deleteRecursively(root);
    }

    @Test
    public void testLoadToStringFile() throws Exception
    {
        File file = new File(workingDirectory, "test.txt");
        FileWriter writer = new FileWriter(file);
        try
        {
            writer.write("Hello\nWorld!");
        } finally
        {
            writer.close();
        }

        String text = FileUtilities.loadToString(file);
        assert file.delete();
        assertEquals("Hello\nWorld!\n", text);
    }

    @Test
    public void testLoadToStringResource() throws Exception
    {
        final String thisFile =
                FileUtilities.loadToString(getClass(), "/ch/systemsx/cisd/common/utilities/FileUtilitiesTest.class");
        assert thisFile != null;
        assert thisFile.indexOf("FileUtilitiesTest") >= 0;
    }

    @Test
    public void testLoadToStringMissingResource() throws Exception
    {
        final String thisFile = FileUtilities.loadToString(getClass(), "/some/missing/resource");
        assert thisFile == null;
    }

    @Test
    public void testLoadToStringListFile() throws Exception
    {
        File file = new File(workingDirectory, "test.txt");
        FileWriter writer = new FileWriter(file);
        try
        {
            writer.write("Hello\nWorld!");
        } finally
        {
            writer.close();
        }

        final List<String> text = FileUtilities.loadToStringList(file);
        assert file.delete();
        assertEquals(Arrays.asList("Hello", "World!"), text);
    }

    @Test
    public final void testRemovePrefixFromFileName()
    {
        File file = new File("/tmp/dir/x.txt");
        try
        {
            FileUtilities.removePrefixFromFileName(null, Constants.IS_FINISHED_PREFIX);
            fail("Given file can not be null.");
        } catch (AssertionError e)
        {
            // Nothing to do here
        }
        assertEquals(file, FileUtilities.removePrefixFromFileName(file, null));
        assertEquals(file, FileUtilities.removePrefixFromFileName(file, Constants.IS_FINISHED_PREFIX));
        file = new File("/tmp/dir/" + Constants.IS_FINISHED_PREFIX + "x.txt");
        assertEquals("/tmp/dir/x.txt", FileUtilities.removePrefixFromFileName(file, Constants.IS_FINISHED_PREFIX)
                .getPath());
    }

    @Test
    public final void testCreateNextNumberedFile()
    {
        File file = new File(workingDirectory, "abc_[12]");
        assert file.exists() == false;
        Pattern pattern = Pattern.compile("_\\[(\\d+)\\]");
        File newFile = FileUtilities.createNextNumberedFile(file, pattern, null);
        assertEquals(FilenameUtils.getName(new File(workingDirectory, "abc_[13]").getPath()), FilenameUtils
                .getName(newFile.getPath()));
        file = new File(workingDirectory, "abc");
        newFile = FileUtilities.createNextNumberedFile(file, pattern, null);
        assertEquals(FilenameUtils.getName(new File(workingDirectory, "abc").getPath()), FilenameUtils.getName(newFile
                .getPath()));
        try
        {
            FileUtilities.createNextNumberedFile(null, pattern, null);
            fail("Null value for file not allowed.");
        } catch (AssertionError e)
        {
            // Nothing to do here
        }
        newFile = FileUtilities.createNextNumberedFile(file, pattern, "abc_[1]");
        assertEquals(FilenameUtils.getName(new File(workingDirectory, "abc_[1]").getPath()), FilenameUtils
                .getName(newFile.getPath()));
        file = new File(workingDirectory, "a0bc1");
        newFile = FileUtilities.createNextNumberedFile(file, null);
        assertEquals(FilenameUtils.getName(new File(workingDirectory, "a1bc2").getPath()), FilenameUtils
                .getName(newFile.getPath()));
        file = new File(workingDirectory, "12abc_[12]");
        newFile = FileUtilities.createNextNumberedFile(file, pattern, "12abc_[1]");
        assertEquals(FilenameUtils.getName(new File(workingDirectory, "12abc_[13]").getPath()), FilenameUtils
                .getName(newFile.getPath()));
    }

    @Test
    public final void testGetRelativeFile()
    {
        try
        {
            FileUtilities.getRelativeFile(null, null);
            fail("Given file can not be null.");
        } catch (AssertionError e)
        {
            // Nothing to do here
        }
        File file = new File(workingDirectory, "hello");
        assertEquals(workingDirectory.getAbsolutePath() + File.separator + "hello", file.getAbsolutePath());
        assertEquals(file, FileUtilities.getRelativeFile(null, file));
        assertEquals(file, FileUtilities.getRelativeFile(new File(""), file));
        File root = new File("/temp");
        assertEquals("/temp", root.getAbsolutePath());
        File relativeFile = FileUtilities.getRelativeFile(root, file);
        assertNull(relativeFile);
        root = workingDirectory;
        relativeFile = FileUtilities.getRelativeFile(root, file);
        assertFalse(relativeFile.isAbsolute());
        assertEquals("hello", relativeFile.getPath());
    }
}
