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
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.utilities.AbstractFileSystemTestCase;

/**
 * Test cases for corresponding {@link HardLinkMaker} class.
 * 
 * @author Christian Ribeaud
 */
@Test(groups =
    { "requires_unix" })
public final class HardLinkMakerTest extends AbstractFileSystemTestCase
{

    private static final String FILE_CONTENT = "This is my content";

    private static final String SOURCE_FILE_NAME = "source.txt";

    private File createFile() throws IOException
    {
        final File srcFile = new File(new File(workingDirectory, "dir"), SOURCE_FILE_NAME);
        assertFalse(srcFile.exists());
        FileUtils.writeStringToFile(srcFile, FILE_CONTENT);
        assertTrue(srcFile.exists());
        return srcFile;
    }

    @Test
    public final void testCreateLinkParameters() throws IOException
    {
        try
        {
            HardLinkMaker.getInstance().tryCreateLink(null, workingDirectory, null);
            fail("Null value not accepted.");
        } catch (AssertionError ex)
        {
            // Nothing to do here.
        }
        try
        {
            HardLinkMaker.getInstance().tryCreateLink(workingDirectory, workingDirectory, null);
            fail("First parameter must be an existing file.");
        } catch (AssertionError ex)
        {
            // Nothing to do here.
        }
        final File srcFile = createFile();
        final File destinationDir = new File(workingDirectory, "dir");
        try
        {
            HardLinkMaker.getInstance().tryCreateLink(srcFile, destinationDir, null);
            fail("File already exits.");
        } catch (final IllegalArgumentException ex)
        {
            assertEquals(String.format(HardLinkMaker.ALREADY_EXISTS_FORMAT, new File(destinationDir, srcFile.getName())
                    .getAbsolutePath()), ex.getMessage());
        }

    }

    @Test
    public final void testCreateLink() throws IOException
    {
        final File srcFile = createFile();
        HardLinkMaker.getInstance().tryCreateLink(srcFile, workingDirectory, null);
        final File destFile = new File(workingDirectory, SOURCE_FILE_NAME);
        assertTrue(destFile.exists());
        assertEquals(FILE_CONTENT, FileUtils.readFileToString(destFile));
        assertTrue(srcFile.delete());
        assertFalse(srcFile.exists());
        // We removed the source file but the destination file (the hard link) still persists.
        assertTrue(destFile.exists());
        assertEquals(FILE_CONTENT, FileUtils.readFileToString(destFile));
    }
}