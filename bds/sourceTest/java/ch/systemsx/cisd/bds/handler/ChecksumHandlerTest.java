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

package ch.systemsx.cisd.bds.handler;

import static ch.systemsx.cisd.base.utilities.OSUtilities.LINE_SEPARATOR;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * Test cases for corresponding {@link ChecksumHandler} class.
 * 
 * @author Christian Ribeaud
 */
public final class ChecksumHandlerTest extends AbstractFileSystemTestCase
{
    private static final String ORIGINAL = "original";

    private final static String CHECKSUMS =
            "31b98e3c4af8d09595dcef2ce8232fbd  abracadabra" + LINE_SEPARATOR
                    + "90d87b30f70e265e9653356917320679  bigBinaryFile" + LINE_SEPARATOR
                    + "23b541115335bbe719c447bb9c942b1a  dir/barbapapa" + LINE_SEPARATOR
                    + "b63f5226ef2d2fc6d38342d90869412c  dir/escargot.png" + LINE_SEPARATOR
                    + "0f782d8d89620f2a5600eba6565f6bfd  dir/file2" + LINE_SEPARATOR
                    + "23b447be20a6ddfe875a8b59ceae83ff  file1" + LINE_SEPARATOR;

    private ChecksumHandler handler;

    private File checksumDirectory;

    private final void prepareChecksumHandler()
    {
        checksumDirectory = new File(workingDirectory, ChecksumHandler.CHECKSUM_DIRECTORY);
        checksumDirectory.mkdir();
        final File originalDirectory = new File(workingDirectory, ORIGINAL);
        originalDirectory.mkdir();
        handler =
                new ChecksumHandler(NodeFactory.createDirectoryNode(checksumDirectory), NodeFactory
                        .createDirectoryNode(originalDirectory));
    }

    private final void prepareWorkingDirectory() throws IOException
    {
        final File originalDir = new File(workingDirectory, ORIGINAL);
        FileUtilities.writeToFile(new File(originalDir, "file1"), "This is my first file.");
        FileUtilities.writeToFile(new File(originalDir, "abracadabra"), "This is my second file.");
        fillWithRandomBytes(new File(originalDir, "bigBinaryFile"));

        File dir = new File(originalDir, "dir");
        dir.mkdir();
        FileUtilities.writeToFile(new File(dir, "file2"), "This is my fourth file.");
        FileUtilities.writeToFile(new File(dir, "escargot.png"), "This is my fifth file.");
        FileUtilities.writeToFile(new File(dir, "barbapapa"), "This is my sixth file.");
    }

    private final void fillWithRandomBytes(final File bigFile) throws IOException
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
        } finally
        {
            IOUtils.closeQuietly(fileOutputStream);
        }
    }

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        prepareChecksumHandler();
        prepareWorkingDirectory();
    }

    @Test
    public final void testPerformClosing() throws IOException
    {
        handler.performClosing();
        final String checksums = FileUtils.readFileToString(new File(checksumDirectory, ORIGINAL));
        assertEquals(CHECKSUMS, checksums);
    }

    @Test
    public final void testAssertValid() throws IOException
    {
        FileUtils.writeStringToFile(new File(checksumDirectory, ORIGINAL), CHECKSUMS);
        handler.assertValid();
    }
}