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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

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
    public void testLoadText() throws Exception
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
        
        String text = FileUtilities.loadText(file);
        assert file.delete();
        assertEquals("Hello\nWorld!\n", text);
    }

}
