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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * Test cases for {@link AutoResolveUtils}.
 * 
 * @author Izabela Adamczyk
 */
@Friend(toClasses = AutoResolveUtils.class)
public class AutoResolveUtilsTest extends AssertJUnit
{

    // f1
    // - f2
    // - - f3
    // - - - e.txt
    private static final File TEST_FOLDER = new File("targets/unit-test/store");

    private static final File EXAMPLE_FOLDER_1 = new File(TEST_FOLDER, "f1");

    private static final File EXAMPLE_FOLDER_2 = new File(EXAMPLE_FOLDER_1, "f2");

    private static final File EXAMPLE_FOLDER_3 = new File(EXAMPLE_FOLDER_2, "f3");

    private static final File EXAMPLE_FILE_TXT = new File(EXAMPLE_FOLDER_3, "e.txt");

    // f4
    // - f5
    // - - f6
    // - - e.abc
    private static final File EXAMPLE_FOLDER_4 = new File(TEST_FOLDER, "f4");

    private static final File EXAMPLE_FOLDER_5 = new File(EXAMPLE_FOLDER_4, "f5");

    private static final File EXAMPLE_FOLDER_6 = new File(EXAMPLE_FOLDER_5, "f6");

    private static final File EXAMPLE_FILE_ABC = new File(EXAMPLE_FOLDER_5, "e.abc");

    private static final File EXAMPLE_FILE_6A = new File(EXAMPLE_FOLDER_6, "a.txt");

    private static final File EXAMPLE_FILE_6B = new File(EXAMPLE_FOLDER_6, "b.jpg");

    private static final String EXAMPLE_FILE_CONTENT = "Hello world!";

    @BeforeMethod
    public void setUp()
    {
        TEST_FOLDER.mkdirs();
        EXAMPLE_FOLDER_3.mkdirs();
        FileUtilities.writeToFile(EXAMPLE_FILE_TXT, EXAMPLE_FILE_CONTENT);
        EXAMPLE_FOLDER_5.mkdirs();
        EXAMPLE_FOLDER_6.mkdirs();
        FileUtilities.writeToFile(EXAMPLE_FILE_ABC, EXAMPLE_FILE_CONTENT);
        FileUtilities.writeToFile(EXAMPLE_FILE_6A, EXAMPLE_FILE_CONTENT);
        FileUtilities.writeToFile(EXAMPLE_FILE_6B, EXAMPLE_FILE_CONTENT);
    }

    @AfterMethod
    public void tearDown()
    {
        FileUtilities.deleteRecursively(TEST_FOLDER);
    }

    @Test
    public void testContinueAutoResolving() throws Exception
    {
        assertTrue(AutoResolveUtils.continueAutoResolving(null, EXAMPLE_FOLDER_1));
        assertTrue(AutoResolveUtils.continueAutoResolving(null, EXAMPLE_FOLDER_2));
        assertFalse(AutoResolveUtils.continueAutoResolving(null, EXAMPLE_FOLDER_3));

        assertTrue(AutoResolveUtils.continueAutoResolving(".*", EXAMPLE_FOLDER_3));
        assertFalse(AutoResolveUtils.continueAutoResolving(".*abc", EXAMPLE_FOLDER_3));
        assertFalse(AutoResolveUtils.continueAutoResolving(".*\\.jpg", EXAMPLE_FOLDER_3));
        assertTrue(AutoResolveUtils.continueAutoResolving(".*\\.txt", EXAMPLE_FOLDER_3));

        assertTrue(AutoResolveUtils.continueAutoResolving(null, EXAMPLE_FOLDER_4));
        assertTrue(AutoResolveUtils.continueAutoResolving(".*", EXAMPLE_FOLDER_4));

        assertFalse(AutoResolveUtils.continueAutoResolving(null, EXAMPLE_FOLDER_5));
        assertFalse(AutoResolveUtils.continueAutoResolving(".*", EXAMPLE_FOLDER_5));

        assertFalse(AutoResolveUtils.continueAutoResolving(null, EXAMPLE_FOLDER_6));
        assertFalse(AutoResolveUtils.continueAutoResolving(".*", EXAMPLE_FOLDER_6));
    }

    @Test
    public void testAcceptFile() throws Exception
    {
        assertTrue(AutoResolveUtils.acceptFile(".*", EXAMPLE_FILE_TXT));
        assertTrue(AutoResolveUtils.acceptFile(".*txt", EXAMPLE_FILE_TXT));
        assertTrue(AutoResolveUtils.acceptFile(".*\\.txt", EXAMPLE_FILE_TXT));
        assertTrue(AutoResolveUtils.acceptFile("txt", EXAMPLE_FILE_TXT));
        // match path
        assertTrue(AutoResolveUtils.acceptFile("f3", EXAMPLE_FILE_TXT));
        // empty pattern
        assertFalse(AutoResolveUtils.acceptFile("", EXAMPLE_FILE_TXT));
        assertFalse(AutoResolveUtils.acceptFile(null, EXAMPLE_FILE_TXT));
        // folder
        assertFalse(AutoResolveUtils.acceptFile(".*", EXAMPLE_FOLDER_3));
        assertFalse(AutoResolveUtils.acceptFile(null, EXAMPLE_FOLDER_3));
        assertFalse(AutoResolveUtils.acceptFile("f3", EXAMPLE_FOLDER_3));
    }

    @Test
    public void testCreateStartingPoint() throws Exception
    {
        // empty path
        assertEquals(TEST_FOLDER, AutoResolveUtils.createStartingPoint(TEST_FOLDER, ""));
        assertEquals(TEST_FOLDER, AutoResolveUtils.createStartingPoint(TEST_FOLDER, null));
        // nonexistent path
        assertEquals(TEST_FOLDER, AutoResolveUtils.createStartingPoint(TEST_FOLDER,
                "nonexistent/no/no"));
        // correct path
        assertEquals(EXAMPLE_FOLDER_2, AutoResolveUtils.createStartingPoint(TEST_FOLDER, "f1/f2"));
        assertEquals(EXAMPLE_FOLDER_2, AutoResolveUtils.createStartingPoint(TEST_FOLDER, "/f1/f2"));
        assertEquals(EXAMPLE_FOLDER_2, AutoResolveUtils.createStartingPoint(TEST_FOLDER, "/f1/f2/"));
        // path is a file
        assertEquals(TEST_FOLDER, AutoResolveUtils.createStartingPoint(TEST_FOLDER,
                "f1/f2/f3/e.txt"));
    }

    @Test
    public void testFindSomeMatchingFiles() throws Exception
    {
        // empty pattern
        assertTrue(AutoResolveUtils.findSomeMatchingFiles(TEST_FOLDER, null, null).isEmpty());
        assertTrue(AutoResolveUtils.findSomeMatchingFiles(TEST_FOLDER, null, "").isEmpty());

        // one matching file
        assertEquals(1, AutoResolveUtils.findSomeMatchingFiles(EXAMPLE_FOLDER_1, null, ".*").size());
        assertEquals(1, AutoResolveUtils.findSomeMatchingFiles(EXAMPLE_FOLDER_1, null, ".*\\.txt")
                .size());
        // nonexistent path
        assertEquals(1, AutoResolveUtils.findSomeMatchingFiles(EXAMPLE_FOLDER_1,
                "abc/cde/efg/nonexistent", ".*").size());
        // no path specified, many files, only one matches pattern
        assertEquals(1, AutoResolveUtils.findSomeMatchingFiles(EXAMPLE_FOLDER_4, null, ".*\\.abc")
                .size());
        // more matching files
        assertTrue(AutoResolveUtils.findSomeMatchingFiles(EXAMPLE_FOLDER_4, null, ".*").size() > 1);
        // path specified, many files, only one matches pattern
        assertEquals(1, AutoResolveUtils.findSomeMatchingFiles(EXAMPLE_FOLDER_4, "f5/f6", ".*txt")
                .size());

    }
}
