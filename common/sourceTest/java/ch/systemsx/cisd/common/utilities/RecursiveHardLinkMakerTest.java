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

import static org.testng.AssertJUnit.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.collections.CollectionIO;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.IImmutableCopier;
import ch.systemsx.cisd.common.utilities.RecursiveHardLinkMaker;

/**
 * Test cases for the {@link RecursiveHardLinkMaker}.
 * 
 * @author Tomasz Pylak
 */
public class RecursiveHardLinkMakerTest
{
    private static final File unitTestRootDirectory =
            new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory =
            new File(unitTestRootDirectory, RecursiveHardLinkMakerTest.class.getSimpleName());

    private static final File outputDir = new File(workingDirectory, "output");

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
        unitTestRootDirectory.mkdirs();
        assert unitTestRootDirectory.isDirectory();
    }

    @BeforeMethod
    public void setUp()
    {
        FileUtilities.deleteRecursively(workingDirectory);
        outputDir.mkdirs();
    }

    @AfterClass
    public void clean()
    {
        FileUtilities.deleteRecursively(outputDir);
    }

    private static File createFile(File directory, String name) throws IOException
    {
        final File file = new File(directory, name);
        file.createNewFile();
        assert file.isFile();
        CollectionIO
                .writeIterable(file, Arrays.asList("test line 1", "test line 2", "test line 3"));
        file.deleteOnExit();
        return file;
    }

    private static File createDirectory(File directory, String name) throws IOException
    {
        final File file = new File(directory, name);
        file.mkdir();
        assert file.isDirectory();
        file.deleteOnExit();
        return file;
    }

    private static File mkPath(File parent, String... subdirs)
    {
        File file = parent;
        for (String subdir : subdirs)
        {
            file = new File(file, subdir);
        }
        return file;
    }

    private static void assertFileExists(File file)
    {
        assert file.isFile();
    }

    private static IImmutableCopier createHardLinkCopier()
    {
        IImmutableCopier copier = RecursiveHardLinkMaker.tryCreate(HardLinkMaker.tryCreate());
        assert copier != null;
        return copier;
    }

    // ------------------------------------- test 1

    // creates following structure
    // dir1
    // ---dir1a
    // ------file1a
    // ---dir1b
    // ------file1b
    // dir2
    // ---file4
    // file2
    // file3
    private void createStructure(File inputDir) throws IOException
    {
        final File dir1 = createDirectory(inputDir, "dir1");
        final File dir1a = createDirectory(dir1, "dir1a");
        final File dir1b = createDirectory(dir1, "dir1b");
        final File dir2 = createDirectory(inputDir, "dir2");
        createFile(dir1a, "file1a");
        createFile(dir1b, "file1b");
        createFile(inputDir, "file2");
        createFile(inputDir, "file3");
        createFile(dir2, "file4");
    }

    @Test(groups =
        { "requires_unix" })
    public void testCopyWithHardLinks() throws IOException
    {
        File inputDir = createDirectory(workingDirectory, "resource-to-copy");
        createStructure(inputDir);
        assertTrue(createHardLinkCopier().copyImmutably(inputDir, outputDir, null));
        File newInput = new File(outputDir, inputDir.getName());

        assertStructureExists(newInput);
        boolean deleted = FileUtilities.deleteRecursively(inputDir);
        assert deleted;
        assertStructureExists(newInput);
    }

    private static void assertStructureExists(File destinationDir)
    {
        assertFileExists(mkPath(destinationDir, "dir1", "dir1a", "file1a"));
        assertFileExists(mkPath(destinationDir, "dir1", "dir1b", "file1b"));
        assertFileExists(mkPath(destinationDir, "dir2", "file4"));
        assertFileExists(mkPath(destinationDir, "file2"));
        assertFileExists(mkPath(destinationDir, "file3"));
    }

    // ------------------------------------- test 2

    @Test(groups =
        { "requires_unix" })
    public void testCopyFile() throws IOException
    {
        File src = createFile(workingDirectory, "fileXXX");
        assertFileExists(src);

        assertTrue(createHardLinkCopier().copyImmutably(src, outputDir, null));
        File dest = new File(outputDir, src.getName());
        assertFileExists(dest);

        modifyDest(dest);
        assertFilesIdentical(src, dest);
    }

    private static void assertFilesIdentical(File file1, File file2)
    {
        List<String> list1 = CollectionIO.readList(file1);
        List<String> list2 = CollectionIO.readList(file2);
        assertEquals(list1, list2);
    }

    private static void modifyDest(File file)
    {
        List<String> list = Arrays.asList("new line 1", "new line 2");
        CollectionIO.writeIterable(file, list);
    }
}
