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
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;

/**
 * Test cases for the {@link FileUtilities}.
 * 
 * @author Bernd Rinn
 */
public final class FileUtilitiesTest extends AbstractFileSystemTestCase
{

    @Test
    public void testFailedConstructionNonExistent()
    {
        final File nonExistentFile = new File(workingDirectory, "non-existent");
        nonExistentFile.delete();
        String errorMsg = FileUtilities.checkDirectoryFullyAccessible(nonExistentFile, "test");
        assertNotNull(errorMsg);
    }

    @Test
    public void testFailedConstructionFileInsteadOfDirectory() throws IOException
    {
        final File file = new File(workingDirectory, "existent_file");
        file.delete();
        file.deleteOnExit();
        file.createNewFile();
        String errorMsg = FileUtilities.checkDirectoryFullyAccessible(file, "test");
        assertNotNull(errorMsg);
    }

    @Test(groups =
        { "requires_unix" })
    public void testFailedConstructionReadOnly() throws IOException, InterruptedException
    {
        final File readOnlyDirectory = new File(workingDirectory, "read_only_directory");
        readOnlyDirectory.delete();
        readOnlyDirectory.mkdir();
        readOnlyDirectory.deleteOnExit();
        assert readOnlyDirectory.setReadOnly();

        String errorMsg = FileUtilities.checkDirectoryFullyAccessible(readOnlyDirectory, "test");

        // --- clean before checking results
        // Unfortunately, with JDK 5 there is no portable way to set a file or directory read/write, once
        // it has been set read-only, thus this test 'requires_unix' for the time being.
        Runtime.getRuntime().exec(String.format("/bin/chmod u+w %s", readOnlyDirectory.getPath())).waitFor();
        if (readOnlyDirectory.canWrite() == false)
        {
            // Can't use assert here since we expect an AssertationError
            throw new IllegalStateException();
        }
        assertNotNull(errorMsg);
    }

    @Test
    public void testCopyFile() throws Exception
    {
        File sourceFile = new File(workingDirectory, "source.txt");
        FileUtilities.writeToFile(sourceFile, "hello world!");
        sourceFile.setLastModified(47110000);
        assertEquals(47110000, sourceFile.lastModified());
        File destinationFile = new File(workingDirectory, "destination.txt");
        FileUtilities.copyFileTo(sourceFile, destinationFile, true);
        assertEquals(FileUtilities.loadToString(sourceFile), FileUtilities.loadToString(destinationFile));
        assertEquals(47110000, destinationFile.lastModified());
    }

    @Test
    public void testWriteToFile() throws Exception
    {
        File file = new File(workingDirectory, "testWriteToFile.txt");
        FileUtilities.writeToFile(file, "Hello world\nhow are you?");
        assertEquals("Hello world\nhow are you?\n", FileUtilities.loadToString(file));
    }

    @Test
    public void testWriteToExistingFile() throws Exception
    {
        File file = new File(workingDirectory, "testWriteToFile.txt");
        FileUtilities.writeToFile(file, "Hello");
        FileUtilities.writeToFile(file, "Hello world");
        assertEquals("Hello world\n", FileUtilities.loadToString(file));
    }

    @Test
    public void testWriteToExistingDirectory() throws Exception
    {
        File dir = new File(workingDirectory, "dir");
        assert dir.mkdir();
        try
        {
            FileUtilities.writeToFile(dir, "Hello world");
            fail("CheckedExceptionTunnel expected.");
        } catch (CheckedExceptionTunnel e)
        {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof IOException);
            String message = cause.getMessage();
            assertTrue("Exception message not as expected: " + message, cause.getMessage().startsWith(dir.toString()));
        }
    }

    @Test
    public void testWriteToExistingReadOnlyFile() throws Exception
    {
        File file = new File(workingDirectory, "testWriteToFile.txt");
        FileUtilities.writeToFile(file, "Hello");
        assert file.setReadOnly();
        try
        {
            FileUtilities.writeToFile(file, "Hello world");
            fail("CheckedExceptionTunnel expected.");
        } catch (CheckedExceptionTunnel e)
        {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof IOException);
            String message = cause.getMessage();
            assertTrue("Exception message not as expected: " + message, cause.getMessage().startsWith(file.toString()));
        } finally
        {
            assert file.delete();
        }
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
    public final void testCreateNextNumberedFile() throws IOException
    {
        File file = new File(workingDirectory, "abc_[12]");
        // File does not exist
        assert file.exists() == false;
        Pattern pattern = Pattern.compile("_\\[(\\d+)\\]");
        File newFile = FileUtilities.createNextNumberedFile(file, pattern, null);
        assertEquals(new File(workingDirectory, "abc_[12]"), newFile);
        FileUtils.touch(file);
        assert file.exists();
        // File exists
        newFile = FileUtilities.createNextNumberedFile(file, pattern, null);
        assertEquals(new File(workingDirectory, "abc_[13]"), newFile);
        // File not containing a number in it
        file = new File(workingDirectory, "abc");
        assert file.exists() == false;
        newFile = FileUtilities.createNextNumberedFile(file, pattern, null);
        assertEquals(new File(workingDirectory, "abc"), newFile);
        FileUtils.touch(file);
        assert file.exists();
        newFile = FileUtilities.createNextNumberedFile(file, pattern, null);
        assertEquals(new File(workingDirectory, "abc1"), newFile);
        try
        {
            FileUtilities.createNextNumberedFile(null, pattern, null);
            fail("Null value for file not allowed.");
        } catch (AssertionError e)
        {
            // Nothing to do here
        }
        String defaultFileName = "abc_[1]";
        try
        {
            FileUtilities.createNextNumberedFile(file, Pattern.compile("dummyPattern"), defaultFileName);
            fail("Must contain either '(\\d+)' or ([0-9]+).");
        } catch (AssertionError e)
        {
            // Nothing to do here
        }
        // File already exists but default one does not
        assertEquals(file.getName(), "abc");
        assert file.exists();
        newFile = FileUtilities.createNextNumberedFile(file, pattern, defaultFileName);
        File defaultFile = new File(workingDirectory, defaultFileName);
        assertEquals(defaultFile, newFile);
        // File already exists and default one also
        FileUtils.touch(defaultFile);
        assert defaultFile.exists();
        newFile = FileUtilities.createNextNumberedFile(file, pattern, defaultFileName);
        assertEquals(new File(workingDirectory, "abc_[2]"), newFile);
        // With no pattern (using the default one)
        file = new File(workingDirectory, "a0bc1");
        FileUtils.touch(file);
        assert file.exists();
        newFile = FileUtilities.createNextNumberedFile(file, null);
        assertEquals(new File(workingDirectory, "a1bc2"), newFile);
        // More examples
        file = new File(workingDirectory, "12abc_[12]");
        newFile = FileUtilities.createNextNumberedFile(file, pattern, "12abc_[1]");
        assertEquals(new File(workingDirectory, "12abc_[12]"), newFile);
        FileUtils.touch(file);
        newFile = FileUtilities.createNextNumberedFile(file, pattern, "12abc_[1]");
        assertEquals(new File(workingDirectory, "12abc_[13]"), newFile);
        newFile = FileUtilities.createNextNumberedFile(file, Pattern.compile("xxx(\\d+)xxx"), "12abc_[1]");
        assertEquals(new File(workingDirectory, "12abc_[1]"), newFile);
    }

    @Test
    public final void testGetRelativeFile()
    {
        try
        {
            FileUtilities.getRelativeFile(null, null);
            fail("Given root and file can not be null.");
        } catch (AssertionError e)
        {
            // Nothing to do here
        }
        File file = new File(workingDirectory, "hello");
        assertEquals(workingDirectory.getAbsolutePath() + File.separator + "hello", file.getAbsolutePath());
        // If the given string is the empty string, then the result is the empty abstract pathname.
        final File rootFile = new File("");
        assertEquals(TARGETS_DIRECTORY + File.separator + UNIT_TEST_WORKING_DIRECTORY + File.separator
                + workingDirectory.getName() + File.separator + "hello", FileUtilities.getRelativeFile(rootFile, file));
        String root = "/temp";
        String relativeFile = FileUtilities.getRelativeFile(new File(root), file);
        assertNull(relativeFile);
        root = workingDirectory.getAbsolutePath();
        relativeFile = FileUtilities.getRelativeFile(new File(root), file);
        assertEquals("hello", relativeFile);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public final void testCopyResourceToTempFileIllegalResource()
    {
        FileUtilities.copyResourceToTempFile("nonexistent", "pre", "post");
    }

    @Test
    public final void testCopyResourceToTempFile()
    {
        final String resourceName = "/ch/systemsx/cisd/common/utilities/FileUtilities.class";
        final String absoluteTempFileName = FileUtilities.copyResourceToTempFile(resourceName, "pre", "post");
        assertNotNull(absoluteTempFileName);
        final File tempFile = new File(absoluteTempFileName);
        final String tempFileName = tempFile.getName();
        assertTrue(tempFile.exists());
        assertTrue(tempFile.length() > 0);
        assertTrue(tempFileName.startsWith("pre"));
        assertTrue(tempFileName.endsWith("post"));
        assertTrue(Arrays.equals(resourceToByteArray(resourceName), fileToByteArray(absoluteTempFileName)));

    }

    @Test
    public final void testIsAvailableFileHappyCase() throws IOException
    {
        final File f = new File(workingDirectory, "this_file_is_available");
        f.createNewFile();
        f.deleteOnExit();
        assertTrue(FileUtilities.isAvailable(f, 500L));
    }

    @Test
    public final void testIsAvailableDirHappyCase() throws IOException
    {
        final File d = new File(workingDirectory, "this_dir_is_available");
        d.mkdir();
        d.deleteOnExit();
        assertTrue(FileUtilities.isAvailable(d, 500L));
    }

    @Test(groups = "slow")
    public final void testIsAvailableDoesntExist() throws IOException
    {
        final File f = new File(workingDirectory, "this_file_is_unavailable");
        assertFalse(FileUtilities.isAvailable(f, 500L));
    }

    @Test(groups = "slow")
    public final void testIsAvailableBecomesAvailableInTime() throws IOException
    {
        final File f = new File(workingDirectory, "this_file_will_become_available");
        f.deleteOnExit();
        Timer t = new Timer();
        t.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    try
                    {
                        f.createNewFile();
                    } catch (IOException ex)
                    {
                        throw new CheckedExceptionTunnel(ex);
                    }
                }
            }, 250L);
        assertTrue(FileUtilities.isAvailable(f, 500L));
    }

    private byte[] resourceToByteArray(String resourcename)
    {
        final InputStream is = FileUtilitiesTest.class.getResourceAsStream(resourcename);
        if (is == null)
        {
            return null;
        }
        try
        {
            return IOUtils.toByteArray(is);
        } catch (IOException ex)
        {
            return null;
        } finally
        {
            IOUtils.closeQuietly(is);
        }
    }

    private byte[] fileToByteArray(String filename)
    {
        InputStream is = null;
        try
        {
            is = new FileInputStream(filename);
            return IOUtils.toByteArray(is);
        } catch (IOException ex)
        {
            return null;
        } finally
        {
            IOUtils.closeQuietly(is);
        }

    }

    @Test
    public final void testNormalizeFile() throws IOException
    {
        try
        {
            FileUtilities.normalizeFile(null);
            fail("Given file can not be null.");
        } catch (AssertionError ex)
        {
            // Nothing to do here.
        }
        final File file = new File(workingDirectory, "../dir");
        final String canonicalPath = file.getCanonicalPath();
        assertFalse(canonicalPath.equals(file.getAbsolutePath()));
        assertEquals(canonicalPath, FileUtilities.normalizeFile(file).getAbsolutePath());
    }

}
