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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * Tests for {@link SimpleFileBasedHierarchicalContent}
 * 
 * @author Piotr Buczek
 */
public class SimpleFileBasedHierarchicalContentTest extends AbstractFileSystemTestCase
{

    private File rootDir;

    private File file1;

    private File file2;

    private File subDir;

    private File subFile1;

    private File subFile2;

    private File subFile3;

    @BeforeMethod
    public void beforeMethod() throws Exception
    {
        rootDir = new File(workingDirectory, "rootDir");
        file1 = new File(rootDir, "file1");
        file2 = new File(rootDir, "file2");
        subDir = new File(rootDir, "subDir");
        subDir.mkdirs();
        subFile1 = new File(subDir, "subFile1");
        subFile2 = new File(subDir, "subFile2");
        subFile3 = new File(subDir, "subFile3");
        for (File f : Arrays.asList(file1, file2, subFile1, subFile2, subFile3))
        {
            FileUtilities.writeToFile(f, f.getName() + " data");
        }
    }

    @Test
    public void testFailWithNonExistentRoot()
    {
        final File fakeFile = new File(workingDirectory, "fakeFile");
        try
        {
            new SimpleFileBasedHierarchicalContent(fakeFile);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex)
        {
            assertEquals(fakeFile.getAbsolutePath() + " doesn't exist", ex.getMessage());
        }
    }

    @Test
    public void testFailWithNonDirectoryRoot()
    {
        final File rootFile = new File(workingDirectory, "rootFile");
        try
        {
            rootFile.createNewFile();
            try
            {
                new SimpleFileBasedHierarchicalContent(rootFile);
                fail("Expected IllegalArgumentException");
            } catch (IllegalArgumentException ex)
            {
                assertEquals(rootFile.getAbsolutePath() + " is not a directory", ex.getMessage());
            }
        } catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }

}
