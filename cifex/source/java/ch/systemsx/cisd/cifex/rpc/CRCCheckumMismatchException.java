/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.rpc;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * An exception that indicates a mismatch of CRC32 checksums for a file.
 * 
 * @author Bernd Rinn
 */
public class CRCCheckumMismatchException extends EnvironmentFailureException
{

    private static final long serialVersionUID = 1L;

    public CRCCheckumMismatchException(final String fileName, final int crc32Value,
            final int crc32ValueExpected, final String msg)
    {
        super(String.format(
                "CRC32 checksum mismatch: File '%s' has CRC32 checksum %x (expected: %x)\n[%s].",
                fileName, crc32Value, crc32ValueExpected, msg));
    }

    public CRCCheckumMismatchException(final String fileName, final int crc32Value,
            final int crc32ValueExpected)
    {
        super(String.format(
                "CRC32 checksum mismatch: File '%s' has CRC32 checksum %x (expected: %x).",
                fileName, crc32Value, crc32ValueExpected));
    }

    public CRCCheckumMismatchException(final String fileName, final long filePosition,
            final int crc32Value, final int crc32ValueExpected)
    {
        super(String.format(
                "CRC32 checksum mismatch: Block %d of file '%s' has CRC32 checksum %x (expected: %x).",
                filePosition, fileName, crc32Value, crc32ValueExpected));
    }
}
