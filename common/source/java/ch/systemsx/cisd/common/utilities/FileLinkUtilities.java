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

package ch.systemsx.cisd.common.utilities;

import java.io.File;

/**
 * A utility class that provides access to hard link and symbolic link creation on Unix platforms.
 * 
 * @author Bernd Rinn
 */
public class FileLinkUtilities
{

    private static boolean operational = false;

    static
    {
        final String filename = FileUtilities.tryCopyNativeLibraryToTempFile("jlink");

        if (filename != null)
        {
            final File linkLib = new File(filename);
            if (linkLib.exists() && linkLib.canRead() && linkLib.isFile())
            {
                try
                {
                    System.load(filename);
                    operational = true;
                } catch (final Throwable err)
                {
                    System.err.printf("Native Link library '%s' failed to load:\n", filename);
                    err.printStackTrace();
                }
            }
        }
    }

    /** An exception that indicates that creating a link failed. */
    public static final class FileLinkException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        private FileLinkException(String type, String source, String target, String errorMessage)
        {
            super(String.format("Creating %s link '%s' -> '%s': %s", type, source, target,
                    errorMessage));
        }
    }

    //
    // The wrappers for the native function calls.
    //

    private static native int hardlink(String filename, String linktarget);

    private static native int symlink(String filename, String linktarget);

    private static native String strerror(int errnum);

    /**
     * Returns <code>true</code>, if the native library has been loaded successfully and the link
     * utilities are operational, <code>false</code> otherwise.
     */
    public static final boolean isOperational()
    {
        return operational;
    }

    /**
     * Creates a hard link from <var>filename</var> to <var>linktarget</var>.
     * 
     * @throws FileLinkException If the underlying system call fails, e.g. because <var>filename</var>
     *             does not exist or because <var>linktarget</var> already exists.
     */
    public static final void createHardLink(String filename, String linktarget)
            throws FileLinkException
    {
        final int result = hardlink(filename, linktarget);
        if (result != 0)
        {
            throw new FileLinkException("hard", linktarget, filename, strerror(result));
        }
    }

    /**
     * Creates a symbolic link from <var>filename</var> to <var>linktarget</var>.
     * 
     * @throws FileLinkException If the underlying system call fails, e.g. because <var>filename</var>
     *             does not exist or because <var>linktarget</var> already exists.
     */
    public static final void createSymbolicLink(String filename, String linktarget)
            throws FileLinkException
    {
        final int result = symlink(filename, linktarget);
        if (result != 0)
        {
            throw new FileLinkException("symbolic", linktarget, filename, strerror(result));
        }
    }

}
