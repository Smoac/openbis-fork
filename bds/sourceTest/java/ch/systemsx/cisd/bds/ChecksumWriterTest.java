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

package ch.systemsx.cisd.bds;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.StringWriter;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * Test cases for corresponding {@link ChecksumWriter} class.
 * 
 * @author Christian Ribeaud
 */
public class ChecksumWriterTest
{
    private static final File UNIT_TEST_ROOT_DIRECTORY = new File("targets" + File.separator + "unit-test-wd");

    private static final File WORKING_DIRECTORY =
            new File(UNIT_TEST_ROOT_DIRECTORY, ChecksumWriterTest.class.getSimpleName());

    private IChecksumWriter checksumWriter = new ChecksumWriter();

    @BeforeMethod
    public final void setUp()
    {
        LogInitializer.init();
        WORKING_DIRECTORY.mkdirs();
        assert WORKING_DIRECTORY.isDirectory();
        WORKING_DIRECTORY.deleteOnExit();
    }

    @Test
    public final void testWriteChecksum()
    {
        FileUtilities.writeToFile(new File(WORKING_DIRECTORY, "file1"), "This is my first file.");
        FileUtilities.writeToFile(new File(WORKING_DIRECTORY, "abracadabra"), "This is my second file.");
        FileUtilities.writeToFile(new File(WORKING_DIRECTORY, "zoro.txt"), "This is my third file.");
        File dir = new File(WORKING_DIRECTORY, "dir");
        dir.mkdir();
        FileUtilities.writeToFile(new File(dir, "file2"), "This is my fourth file.");
        FileUtilities.writeToFile(new File(dir, "escargot.png"), "This is my fifth file.");
        FileUtilities.writeToFile(new File(dir, "barbapapa"), "This is my sixth file.");
        try
        {
            checksumWriter.writeChecksum(null, null);
            fail("Null value not allowed here.");
        } catch (AssertionError e)
        {
            // Nothing to do here
        }
        final StringWriter writer = new StringWriter();
        try
        {
            checksumWriter.writeChecksum(new File("choubidou"), writer);
            fail("Given file must exist.");
        } catch (AssertionError e)
        {
            // Nothing to do here
        }
        checksumWriter.writeChecksum(WORKING_DIRECTORY, writer);
        assertEquals("31b98e3c4af8d09595dcef2ce8232fbd  abracadabra\n"
                + "23b447be20a6ddfe875a8b59ceae83ff  file1\n"
                + "0ba14d9315ba1c59449eb2fe1dbcbb61  zoro.txt\n"
                + "23b541115335bbe719c447bb9c942b1a  dir/barbapapa\n"
                + "b63f5226ef2d2fc6d38342d90869412c  dir/escargot.png\n"
                + "0f782d8d89620f2a5600eba6565f6bfd  dir/file2\n", writer.toString());
    }
}