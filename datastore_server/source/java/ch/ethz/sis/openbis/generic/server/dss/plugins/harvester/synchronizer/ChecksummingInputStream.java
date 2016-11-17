/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.harvester.synchronizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.CRC32;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
public class ChecksummingInputStream extends FilterInputStream
{

    private final CRC32 crc32;

    private long totalLength;

    ChecksummingInputStream(InputStream in)
    {
        super(in);
        crc32 = new CRC32();
    }

    public int read() throws IOException
    {
        int value = in.read();
        if (value > 0)
        {
            crc32.update(value);
            totalLength++;
        }
        return value;
    }

    public int read(byte[] data, int offset, int length) throws IOException
    {
        int result = in.read(data, offset, length);
        if (result > 0)
        {
            crc32.update(data, offset, result);
            this.totalLength += result;
        }
        return result;
    }

    public long checksum()
    {
        return crc32.getValue();
    }

    public long getLength()
    {
        return totalLength;
    }

    public static void main(String[] args)
    {
        try
        {
            FileInputStream fis =
                    new FileInputStream(
                            new File(
                                    "/Users/gakin/Documents/workspace_openbis_trunk/integration-tests/targets/playground/test_openbis_sync/openbis2/data/store/harvester-tmp/20161117115152163-19/original/DSC_0358.JPG"));
            ChecksummingInputStream cis = new ChecksummingInputStream(fis);
            Files.copy(cis, Paths.get("/Users/gakin/Documents/writable"), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Crc: " + (cis.checksum() & (~0L)));
            System.out.println("Length: " + cis.getLength());
        } catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
