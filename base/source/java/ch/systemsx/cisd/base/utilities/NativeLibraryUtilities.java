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

package ch.systemsx.cisd.base.utilities;

import java.io.File;

/**
 * A library for loading native libraries from jar files.
 * 
 * @author Bernd Rinn
 */
public final class NativeLibraryUtilities
{

    /**
     * Loads the native library <var>libraryName</var> from a Java resource that follows a naming
     * convention described in {@link #tryCopyNativeLibraryToTempFile(String)}.
     * 
     * @return <code>true</code> if the library has been loaded successfully and <code>false</code>
     *         otherwise.
     */
    public static boolean loadNativeLibraryFromResource(final String libraryName)
    {
        final String filename = tryCopyNativeLibraryToTempFile(libraryName);

        if (filename != null)
        {
            final File linkLib = new File(filename);
            if (linkLib.exists() && linkLib.canRead() && linkLib.isFile())
            {
                try
                {
                    System.load(filename);
                    return true;
                } catch (final Throwable err)
                {
                    System.err.printf("Native library '%s' failed to load:\n", filename);
                    err.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * Tries to copy a native library which is available as a resource to a temporary file. It will
     * use the following naming schema to locate the resource containing the native library:
     * <p>
     * <code>/native/&lt;libname&gt;/&lt;platform_id&gt;/&lt;libname&gt;.so</code>.
     * 
     * @param libraryName The name of the library.
     * @return The name of the temporary file, or <code>null</code>, if the resource could not be
     *         copied.
     */
    public static String tryCopyNativeLibraryToTempFile(final String libraryName)
    {
        return ResourceUtilities.tryCopyResourceToTempFile(String.format("/native/%s/%s/%s.so",
                libraryName, OSUtilities.getCompatibleComputerPlatform(), libraryName),
                libraryName, ".so");
    }

}
