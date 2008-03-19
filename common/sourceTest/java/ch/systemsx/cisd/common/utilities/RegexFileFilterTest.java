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
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.RegexFileFilter;
import ch.systemsx.cisd.common.utilities.RegexFileFilter.PathType;

/**
 * Test cases for the {@link RegexFileFilter}.
 * 
 * @author Bernd Rinn
 */
public class RegexFileFilterTest
{

    private static final File unitTestRootDirectory =
            new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory =
            new File(unitTestRootDirectory, "RegexFileFilterTestTest");

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

    @Test
    public void testEmpty() throws IOException
    {
        createFile("aaa");
        createFile("aba");
        createFile("baa");
        createFile("bba");
        createDirectory("bab");
        Set<File> actual =
                new HashSet<File>(Arrays.asList(workingDirectory.listFiles(new RegexFileFilter())));
        assert actual.isEmpty();
    }

    @Test
    public void testFileMatching() throws IOException
    {
        final File f1 = createFile("aaa");
        final File f2 = createFile("aba");
        final File f3 = createFile("baa");
        final File f4 = createFile("bba");
        createDirectory("bab");
        Set<File> actual =
                new HashSet<File>(Arrays.asList(workingDirectory.listFiles(new RegexFileFilter(
                        PathType.FILE, "a.+"))));
        Set<File> expected = new HashSet<File>(Arrays.asList(f1, f2));
        assertEquals(expected, actual);

        actual =
                new HashSet<File>(Arrays.asList(workingDirectory.listFiles(new RegexFileFilter(
                        PathType.FILE, ".a."))));
        expected = new HashSet<File>(Arrays.asList(f1, f3));
        assertEquals(expected, actual);

        actual =
                new HashSet<File>(Arrays.asList(workingDirectory.listFiles(new RegexFileFilter(
                        PathType.FILE, ".+a"))));
        expected = new HashSet<File>(Arrays.asList(f1, f2, f3, f4));
        assertEquals(expected, actual);
    }

    @Test
    public void testDirectoryMatching() throws IOException
    {
        final File d1 = createDirectory("aaa");
        final File d2 = createDirectory("aba");
        final File d3 = createDirectory("baa");
        final File d4 = createDirectory("bba");
        createFile("bab");
        Set<File> actual =
                new HashSet<File>(Arrays.asList(workingDirectory.listFiles(new RegexFileFilter(
                        PathType.DIRECTORY, "a.+"))));
        Set<File> expected = new HashSet<File>(Arrays.asList(d1, d2));
        assertEquals(expected, actual);

        actual =
                new HashSet<File>(Arrays.asList(workingDirectory.listFiles(new RegexFileFilter(
                        PathType.DIRECTORY, ".a."))));
        expected = new HashSet<File>(Arrays.asList(d1, d3));
        assertEquals(expected, actual);

        actual =
                new HashSet<File>(Arrays.asList(workingDirectory.listFiles(new RegexFileFilter(
                        PathType.DIRECTORY, ".+a"))));
        expected = new HashSet<File>(Arrays.asList(d1, d2, d3, d4));
        assertEquals(expected, actual);
    }

    @Test
    public void testAllMatching() throws IOException
    {
        final File f1 = createFile("aaa");
        final File d2 = createDirectory("aba");
        final File f3 = createFile("baa");
        final File d4 = createDirectory("bba");
        final File f5 = createFile("bab");
        Set<File> actual =
                new HashSet<File>(Arrays.asList(workingDirectory.listFiles(new RegexFileFilter(
                        PathType.ALL, "a.+"))));
        Set<File> expected = new HashSet<File>(Arrays.asList(f1, d2));
        assertEquals(expected, actual);

        actual =
                new HashSet<File>(Arrays.asList(workingDirectory.listFiles(new RegexFileFilter(
                        PathType.ALL, ".a."))));
        expected = new HashSet<File>(Arrays.asList(f1, f3, f5));
        assertEquals(expected, actual);

        actual =
                new HashSet<File>(Arrays.asList(workingDirectory.listFiles(new RegexFileFilter(
                        PathType.ALL, ".+a"))));
        expected = new HashSet<File>(Arrays.asList(f1, d2, f3, d4));
        assertEquals(expected, actual);
    }

    @Test
    public void testTwoPatterns() throws IOException
    {
        final File f1 = createFile("aaa");
        final File f2 = createFile("aba");
        final File f3 = createFile("111");
        final File f4 = createFile("222");
        createFile("2a2");
        createFile("c0d");
        final RegexFileFilter filter = new RegexFileFilter();
        filter.add(PathType.ALL, "[a-z]+");
        filter.add(PathType.ALL, "[0-9]+");
        Set<File> actual = new HashSet<File>(Arrays.asList(workingDirectory.listFiles(filter)));
        Set<File> expected = new HashSet<File>(Arrays.asList(f1, f2, f3, f4));
        assertEquals(expected, actual);
    }

    private File createFile(String name) throws IOException
    {
        final File file = new File(workingDirectory, name);
        file.createNewFile();
        assert file.isFile();
        file.deleteOnExit();
        return file;
    }

    private File createDirectory(String name) throws IOException
    {
        final File file = new File(workingDirectory, name);
        file.mkdir();
        assert file.isDirectory();
        file.deleteOnExit();
        return file;
    }

}
