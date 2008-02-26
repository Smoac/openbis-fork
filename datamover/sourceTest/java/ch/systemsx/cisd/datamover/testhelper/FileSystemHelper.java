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

package ch.systemsx.cisd.datamover.testhelper;

import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.common.collections.CollectionIO;

/**
 * Helper for files and directories manipulations.
 * 
 * @author Tomasz Pylak on Aug 29, 2007
 */
public class FileSystemHelper
{
    public static File createFile(File dir, String name, List<String> lines)
    {
        File file = new File(dir, name);
        createFile(file, lines);
        return file;
    }

    private static void createFile(File file, List<String> lines)
    {
        CollectionIO.writeIterable(file, lines);
    }

    public static void createEmptyFile(File file)
    {
        CollectionIO.writeIterable(file, new ArrayList<String>());
    }

    public static File createDir(File directory, String name) throws IOException
    {
        final File file = new File(directory, name);
        file.mkdir();
        assert file.isDirectory();
        file.deleteOnExit();
        return file;
    }

    public static File assertDirExists(File parentDir, String dirName)
    {
        File dir = new File(parentDir, dirName);
        assert dir.isDirectory();
        return dir;
    }

    public static void assertEmptyDir(File dir)
    {
        assertTrue(dir.getAbsolutePath() + " is no directory", dir.isDirectory());
        final List<String> dirList = Arrays.asList(dir.list());
        if (dirList.size() > 0)
        {
            final StringBuilder builder = new StringBuilder();
            for (String f : dirList)
            {
                builder.append(f);
                builder.append(' ');
            }
            assertTrue("Should be empty, but found: " + builder.toString(), false);
        }
    }
}
