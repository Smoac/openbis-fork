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

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * @author Franz-Josef Elmer
 */
public class FileTest extends StorageTestCase
{
    @Test
    public void testGetValueAndGetName()
    {
        java.io.File file = new java.io.File(TEST_DIR, "test.txt");
        FileUtilities.writeToFile(file, "Hello\nworld!\n");
        File stringFile = new File(file);

        assertEquals("test.txt", stringFile.getName());
        assertEquals("Hello\nworld!\n", stringFile.getStringContent());
        assertEquals("Hello\nworld!\n", new String(stringFile.getBinaryContent()));
    }

    @Test
    public void testExtractTo()
    {
        java.io.File file = new java.io.File(TEST_DIR, "test.txt");
        FileUtilities.writeToFile(file, "Hello\nworld!\n");
        File stringFile = new File(file);

        java.io.File subdir = new java.io.File(TEST_DIR, "subdir");
        subdir.mkdir();
        stringFile.extractTo(subdir);
        assertEquals("Hello\nworld!\n", FileUtilities.loadToString(new java.io.File(subdir, stringFile.getName())));
    }

}
