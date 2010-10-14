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

package ch.systemsx.cisd.etlserver.hdf5;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.hdf5.IHDF5SimpleReader;
import ch.systemsx.cisd.hdf5.IHDF5SimpleWriter;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class Hdf5ContainerTest extends AbstractFileSystemTestCase
{
    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
    }

    @Test
    public void testReadWriteUncompressed()
    {
        File hdf5File = new File(workingDirectory, "test.h5");
        Hdf5Container hdf5Content = new Hdf5Container(hdf5File);
        IHDF5SimpleWriter writer = hdf5Content.createSimpleWriter(false);
        byte[] byteArray = createByteArray();
        writer.writeByteArray("/test-bytes", byteArray);
        writer.close();

        IHDF5SimpleReader reader = hdf5Content.createSimpleReader();
        byte[] readData = reader.readAsByteArray("/test-bytes");
        assertEquals(byteArray, readData);
    }

    @Test
    public void testReadWriteCompressed()
    {
        File hdf5File = new File(workingDirectory, "test.h5");
        Hdf5Container hdf5Content = new Hdf5Container(hdf5File);
        IHDF5SimpleWriter writer = hdf5Content.createSimpleWriter(true);
        byte[] byteArray = createByteArray();
        writer.writeByteArray("/test-bytes", byteArray);
        writer.close();

        IHDF5SimpleReader reader = hdf5Content.createSimpleReader();
        byte[] readData = reader.readAsByteArray("/test-bytes");
        assertEquals(byteArray, readData);
    }

    @Test
    public void testSizeComparison()
    {
        byte[] byteArray = createByteArray();

        File hdf5FileUncompressed = new File(workingDirectory, "test-uncompressed.h5");
        Hdf5Container hdf5ContentUncompressed = new Hdf5Container(hdf5FileUncompressed);
        IHDF5SimpleWriter writerUncompressed = hdf5ContentUncompressed.createSimpleWriter(false);
        writerUncompressed.writeByteArray("/test-bytes", byteArray);
        writerUncompressed.close();

        File hdf5FileCompressed = new File(workingDirectory, "test-compressed.h5");
        Hdf5Container hdf5ContentCompressed = new Hdf5Container(hdf5FileCompressed);
        IHDF5SimpleWriter writerCompressed = hdf5ContentCompressed.createSimpleWriter(true);
        writerCompressed.writeByteArray("/test-bytes", byteArray);
        writerCompressed.close();

        long uncompressedLength = hdf5FileUncompressed.length();
        long compressedLength = hdf5FileCompressed.length();

        assertTrue("" + uncompressedLength + " <= " + compressedLength,
                uncompressedLength > compressedLength);
    }

    private byte[] createByteArray()
    {
        int numberOfBytes = 1024 * 1024;
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        for (int i = 0; i < numberOfBytes; ++i)
        {
            bos.write(1);
        }
        return bos.toByteArray();
    }

}
