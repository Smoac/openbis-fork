/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipException;

import de.schlichtherle.util.zip.ZipEntry;
import de.schlichtherle.util.zip.ZipFile;

/**
 * @author anttil
 */
public class ZipFileIntegrityVerifier implements IArchiveFileVerifier
{

    @Override
    public List<String> verify(File file)
    {
        List<String> errors = new ArrayList<String>();
        ZipFile zip;
        try
        {
            zip = new ZipFile(file);
        } catch (ZipException ex)
        {
            errors.add("Reading zip file failed: " + ex.getMessage());
            return errors;
        } catch (IOException ex)
        {
            errors.add("Reading zip file failed: " + ex.getMessage());
            return errors;
        }

        Enumeration<?> entries = zip.entries();
        while (entries.hasMoreElements())
        {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            errors.addAll(checkZipEntry(zip, entry));
        }
        return errors;
    }

    private Collection<String> checkZipEntry(ZipFile zip, ZipEntry entry)
    {
        InputStream input = null;
        try
        {
            input = zip.getInputStream(entry);

            long crc = calcCRC32(input);

            if (crc != entry.getCrc())
            {
                return Arrays.asList(entry.getName() + ": CRC failure (got " + Long.toHexString(crc) + ", should be "
                        + Long.toHexString(entry.getCrc()) + ")");
            }

        } catch (ZipException ex)
        {
            return Arrays.asList(ex.getMessage());
        } catch (IOException ex)
        {
            return Arrays.asList(entry.getName() + ": " + ex.getMessage());
        } finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                } catch (IOException ex)
                {
                }
            }
        }
        return new ArrayList<String>();
    }

    private static long calcCRC32(InputStream input) throws IOException
    {
        BufferedInputStream inStream = new BufferedInputStream(input);
        int BLOCK_SIZE = 128 * 1024;
        int len;
        byte[] buffer = new byte[BLOCK_SIZE];

        CRC32 crc32 = new CRC32();
        crc32.reset();

        while ((len = inStream.read(buffer, 0, BLOCK_SIZE)) > 0)
        {
            crc32.update(buffer, 0, len);
            buffer = new byte[BLOCK_SIZE];
        }

        return crc32.getValue();
    }
}
