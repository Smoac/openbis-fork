/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * Useful utility methods.
 *
 * @author Franz-Josef Elmer
 */
public class IOUtilities
{
    /**
     * Calculates the CRC32 checksum of specified input stream.
     * Note, that the input stream is closed after invocation of this method.
     */
    public static long getChecksumCRC32(InputStream inputStream)
    {
        Checksum checksummer = new CRC32();
        InputStream in = null;
        try {
            in = new CheckedInputStream(inputStream, checksummer);
            IOUtils.copy(in, new NullOutputStream());
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return checksummer.getValue();
    }
}
