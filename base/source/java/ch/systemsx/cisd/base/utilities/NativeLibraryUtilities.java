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
 * A library for loading native libraries.
 * 
 * @author Bernd Rinn
 */
public final class NativeLibraryUtilities
{
    private static final String JNI_LIB_PREFIX = getJNILibPrefixForSystem();

    private static final String JNI_LIB_EXTENSION = getJNILibExtensionForSystem();

    private static String getJNILibPrefixForSystem()
    {
        return OSUtilities.isWindows() ? "" : "lib";
    }
    
    private static String getJNILibExtensionForSystem()
    {
        if (OSUtilities.isMacOS())
        {
            return "jnilib";
        } else if (OSUtilities.isWindows())
        {
            return "dll";
        } else
        {
            return "so";
        }
    }

    /**
     * Loads the native library <var>libraryName</var>. The native library will be searched for in
     * this way:
     * <ol>
     * <li>Try to use {@link System#loadLibrary(String)}. If this fails, use the next method.</li>
     * <li>The library path can either be provided as a Java property {@code
     * native.libpath.<libraryName>}.</li>
     * <li>Or a prefix on the filesystem can be provided by specifying the Java property {@code
     * native.libpath} and then the library is expected to be in {@code
     * <native.libpath>/<libraryName>/<platform_id>/<libraryName>.so}.</li>
     * <li>Finally, the routine will try to find the library as a resource in the class path with
     * resource name {@code /native/<libraryName>/<platform_id>/<libraryName>.so}.</li>
     * </ol>
     * 
     * @return <code>true</code> if the library has been loaded successfully and <code>false</code>
     *         otherwise.
     */
    public static boolean loadNativeLibrary(final String libraryName)
    {
        // Try specific path
        String linkLibNameOrNull = System.getProperty("native.libpath." + libraryName);
        if (linkLibNameOrNull != null)
        {
            return loadLib(linkLibNameOrNull);
        }

        // Try generic path
        final String linkLibPathOrNull = System.getProperty("native.libpath");
        if (linkLibPathOrNull != null)
        {
            linkLibNameOrNull = getLibPath(linkLibPathOrNull, libraryName);
            return loadLib(linkLibNameOrNull);
        }

        // Try resource
        linkLibNameOrNull = tryCopyNativeLibraryToTempFile(libraryName);
        if (linkLibNameOrNull != null)
        {
            return loadLib(linkLibNameOrNull);
        }
        // Finally, try system dependent loading
        return loadSystemLibrary(libraryName);
    }

    private static boolean loadLib(String libPath)
    {
        final File linkLib = new File(libPath);
        if (linkLib.exists() && linkLib.canRead() && linkLib.isFile())
        {
            final String linkLibNameAbsolute = linkLib.getAbsolutePath();
            try
            {
                System.load(linkLibNameAbsolute);
                return true;
            } catch (final Throwable err)
            {
                System.err.printf("Native library '%s' failed to load:\n", linkLibNameAbsolute);
                err.printStackTrace();
                return false;
            }
        } else
        {
            System.err.printf("Native library '%s' does not exist or is not readable.\n", linkLib
                    .getAbsolutePath());
            return false;
        }
    }

    private static boolean loadSystemLibrary(String libName)
    {
        try
        {
            System.loadLibrary(libName);
            return true;
        } catch (Throwable th)
        {
            // Silence this - we return failure back as boolean value.
            return false;
        }
    }

    /**
     * Tries to copy a native library which is available as a resource to a temporary file. It will
     * use the following naming schema to locate the resource containing the native library:
     * <p>
     * {@code /native/<libraryName>/<platform_id>/<libraryName>.so}.
     * 
     * @param libraryName The name of the library.
     * @return The name of the temporary file, or <code>null</code>, if the resource could not be
     *         copied.
     */
    public static String tryCopyNativeLibraryToTempFile(final String libraryName)
    {
        return ResourceUtilities.tryCopyResourceToTempFile(getLibPath("/native", libraryName),
                libraryName, ".so");
    }

    private static String getLibPath(final String prefix, final String libraryName)
    {
        return String.format("%s/%s/%s/%s%s.%s", prefix, libraryName, OSUtilities
                .getCompatibleComputerPlatform(), JNI_LIB_PREFIX, libraryName, JNI_LIB_EXTENSION);
    }
    
}
