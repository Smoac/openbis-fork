/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.client.application.utils;

/**
 * General file manipulation utilities.
 * 
 * @author Christian Ribeaud
 */
public final class FileUtils
{

    private FileUtils()
    {
        // Can not be instantiated.
    }

    /**
     * The number of bytes in a kilobyte.
     */
    public static final long ONE_KB = 1024;

    /**
     * The number of bytes in a megabyte.
     */
    public static final long ONE_MB = ONE_KB * ONE_KB;

    /**
     * The number of bytes in a gigabyte.
     */
    public static final long ONE_GB = ONE_KB * ONE_MB;

    /**
     * Returns a human-readable version of the file size, where the input represents a specific number of bytes.
     * 
     * @param size the number of bytes
     * @return a human-readable display value (includes units)
     */
    public final static String byteCountToDisplaySize(final long size)
    {
        final String displaySize;

        if (size / ONE_GB > 0)
        {
            displaySize = String.valueOf(size / ONE_GB) + " GB";
        } else if (size / ONE_MB > 0)
        {
            displaySize = String.valueOf(size / ONE_MB) + " MB";
        } else if (size / ONE_KB > 0)
        {
            displaySize = String.valueOf(size / ONE_KB) + " KB";
        } else
        {
            displaySize = String.valueOf(size) + " bytes";
        }
        return displaySize;
    }

}
