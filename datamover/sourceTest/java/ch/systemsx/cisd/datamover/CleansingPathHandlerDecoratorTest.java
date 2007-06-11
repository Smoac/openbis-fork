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

package ch.systemsx.cisd.datamover;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask.IPathHandler;


/**
 * Test cases for the {@link CleansingPathHandlerDecorator}.
 * 
 * @author Bernd Rinn
 */
public class CleansingPathHandlerDecoratorTest
{

    private static final File unitTestRootDirectory = new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory = new File(unitTestRootDirectory, "CleansingPathHandlerDecoratorTest");

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
        workingDirectory.mkdirs();
        workingDirectory.deleteOnExit();
    }

    private void createStructure() throws IOException
    {
        final File dir1 = createDirectory("dir1");
        final File dir1a = createDirectory(dir1, "dir1a");
        final File dir1b = createDirectory(dir1, "dir1b");
        final File dir2 = createDirectory("dir2");
        createFile(dir1a, "file1a");
        createFile(dir1b, "file1b");
        createFile("file2");
        createFile("file3");
        createFile(dir2, "file4");
    }

    private File createFile(String name) throws IOException
    {
        return createFile(workingDirectory, name);
    }

    private File createFile(File directory, String name) throws IOException
    {
        final File file = new File(directory, name);
        file.createNewFile();
        assert file.isFile();
        file.deleteOnExit();
        return file;
    }

    private File createDirectory(String name) throws IOException
    {
        return createDirectory(workingDirectory, name);
    }

    private File createDirectory(File directory, String name) throws IOException
    {
        final File file = new File(directory, name);
        file.mkdir();
        assert file.isDirectory();
        file.deleteOnExit();
        return file;
    }

    @Test
    public void testDecoratedPathHandlerDoesntSeeDeletedFile() throws IOException
    {
        createStructure();
        CleansingPathHandlerDecorator cph = new CleansingPathHandlerDecorator(new FileFilter()
            {
                public boolean accept(File pathname)
                {
                    return pathname.getName().equals("file2");
                }
            }, new IPathHandler()
            {
                public boolean handle(File path)
                {
                    assert path.getName().equals("file2") == false;
                    return false;
                }

            });
        cph.handle(workingDirectory);
    }

    @Test
    public void testDecoratedPathHandlerDoesntSeeDeletedDirectory() throws IOException
    {
        createStructure();
        CleansingPathHandlerDecorator cph = new CleansingPathHandlerDecorator(new FileFilter()
            {
                public boolean accept(File pathname)
                {
                    return pathname.getName().equals("dir2");
                }
            }, new IPathHandler()
            {
                public boolean handle(File path)
                {
                    assert path.getName().equals("dir2") == false && path.getName().equals("file4") == false;
                    return false;
                }

            });
        cph.handle(workingDirectory);
    }

}
