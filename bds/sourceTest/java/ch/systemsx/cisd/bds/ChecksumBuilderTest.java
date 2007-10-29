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

import static ch.systemsx.cisd.common.utilities.OSUtilities.LINE_SEPARATOR;
import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.storage.filesystem.FileStorage;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * Test cases for corresponding {@link ChecksumBuilder} class.
 * 
 * @author Christian Ribeaud
 */
public class ChecksumBuilderTest
{
    private static final File UNIT_TEST_ROOT_DIRECTORY = new File("targets" + File.separator + "unit-test-wd");

    private static final File WORKING_DIRECTORY =
            new File(UNIT_TEST_ROOT_DIRECTORY, ChecksumBuilderTest.class.getSimpleName());

    private ChecksumBuilder checksumBuilder;

    @BeforeMethod
    public final void setUp()
    {
        checksumBuilder = new ChecksumBuilder(new MD5ChecksumCalculator());
        LogInitializer.init();
        WORKING_DIRECTORY.mkdirs();
        assert WORKING_DIRECTORY.isDirectory();
        WORKING_DIRECTORY.deleteOnExit();
    }

    @Test
    public final void testBuildChecksumsForAllFilesIn() throws Exception
    {
        FileUtilities.writeToFile(new File(WORKING_DIRECTORY, "file1"), "This is my first file.");
        FileUtilities.writeToFile(new File(WORKING_DIRECTORY, "abracadabra"), "This is my second file.");
        fillWithRandomBytes(new File(WORKING_DIRECTORY, "bigBinaryFile"));
        
        File dir = new File(WORKING_DIRECTORY, "dir");
        dir.mkdir();
        FileUtilities.writeToFile(new File(dir, "file2"), "This is my fourth file.");
        FileUtilities.writeToFile(new File(dir, "escargot.png"), "This is my fifth file.");
        FileUtilities.writeToFile(new File(dir, "barbapapa"), "This is my sixth file.");
        
        FileStorage fileStorage = new FileStorage(WORKING_DIRECTORY);
        fileStorage.mount();
        
        String checksums = checksumBuilder.buildChecksumsForAllFilesIn(fileStorage.getRoot());
        
        assertEquals("31b98e3c4af8d09595dcef2ce8232fbd  abracadabra" + LINE_SEPARATOR
                + "90d87b30f70e265e9653356917320679  bigBinaryFile" + LINE_SEPARATOR
                + "23b541115335bbe719c447bb9c942b1a  dir/barbapapa" + LINE_SEPARATOR
                + "b63f5226ef2d2fc6d38342d90869412c  dir/escargot.png" + LINE_SEPARATOR
                + "0f782d8d89620f2a5600eba6565f6bfd  dir/file2" + LINE_SEPARATOR
                + "23b447be20a6ddfe875a8b59ceae83ff  file1" + LINE_SEPARATOR, checksums);
    }

    private void fillWithRandomBytes(File bigFile) throws IOException
    {
        FileOutputStream fileOutputStream = null;
        try
        {
            fileOutputStream = new FileOutputStream(bigFile);
            Random random = new Random(4711);
            byte[] bytes = new byte[1024];
            for (int n = 0; n < 9; n++)
            {
                for (int i = 0; i < bytes.length; i++)
                {
                    bytes[i] = (byte) random.nextInt();
                }
                fileOutputStream.write(bytes);
            }
        } catch (IOException ex)
        {
            throw ex;
        } finally
        {
            IOUtils.closeQuietly(fileOutputStream);
        }
    }
}