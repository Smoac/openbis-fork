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

package ch.systemsx.cisd.common.filesystem;

import java.io.IOException;

import ch.systemsx.cisd.common.exceptions.WrappedIOException;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * A utility class that provides access to hard link and symbolic link creation on Unix platforms.
 * 
 * @author Bernd Rinn
 */
public class FileLinkUtilities
{

    private final static boolean operational = FileUtilities.loadNativeLibraryFromResource("jlink");

    private static void throwException(String type, String source, String target,
            String errorMessage)
    {
        throw new WrappedIOException(new IOException(String.format(
                "Creating %s link '%s' -> '%s': %s", type, source, target, errorMessage)));
    }

    private static void throwException(String filename, String errorMessage)
    {
        throw new WrappedIOException(new IOException(String.format(
                "Cannot obtain inode info for file '%s': %s", filename, errorMessage)));
    }

    /** A class that provides information about a link. */
    public static final class LinkInfo
    {
        private final int inode;

        private final int hardLinkCount;

        private final boolean isSsymbolicLink;

        private final String symbolicLinkOrNull;

        private LinkInfo(int[] info, String symbolicLinkOrNull)
        {
            this.inode = info[0];
            this.hardLinkCount = info[1];
            this.isSsymbolicLink = (info[2] != 0);
            this.symbolicLinkOrNull = symbolicLinkOrNull;
        }

        /**
         * Returns <code>true</code>, if this link is a symbolic link.
         */
        public final boolean isSymbolicLink()
        {
            return isSsymbolicLink;
        }

        /**
         * Returns the value of the symbolic link, or <code>null</code>, if this link is not a
         * symbolic link.
         */
        public final String tryGetSymbolicLink()
        {
            return symbolicLinkOrNull;
        }

        /**
         * Returns the inode number of this link.
         */
        public final int getInode()
        {
            return inode;
        }

        /**
         * Returns the hard link count of this link.
         */
        public final int getHardLinkCount()
        {
            return hardLinkCount;
        }
    }

    //
    // The wrappers for the native function calls.
    //

    private static native int hardlink(String filename, String linktarget);

    private static native int symlink(String filename, String linktarget);

    private static native int linkinfo(String filename, int[] info);

    private static native String readlink(String filename, int linkvallen);

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
     * Creates a hard link from <var>filename</var> to <var>linkname</var>.
     * 
     * @throws WrappedIOException If the underlying system call fails, e.g. because
     *             <var>filename</var> does not exist or because <var>linkname</var> already exists.
     */
    public static final void createHardLink(String filename, String linkname)
            throws WrappedIOException
    {
        if (filename == null || linkname == null)
        {
            throwException("hard", filename, linkname, "null is not allowed");
        }
        final int result = hardlink(filename, linkname);
        if (result < 0)
        {
            throwException("hard", filename, linkname, strerror(result));
        }
    }

    /**
     * Creates a symbolic link from <var>filename</var> to <var>linkname</var>.
     * 
     * @throws WrappedIOException If the underlying system call fails, e.g. because
     *             <var>filename</var> does not exist or because <var>linktarget</var> already
     *             exists.
     */
    public static final void createSymbolicLink(String filename, String linkname)
            throws WrappedIOException
    {
        if (filename == null || linkname == null)
        {
            throwException("symbolic", filename, linkname, "null is not allowed");
        }
        final int result = symlink(filename, linkname);
        if (result < 0)
        {
            throwException("symbolic", filename, linkname, strerror(result));
        }
    }

    private static int[] getLinkInfoArray(String linkname) throws WrappedIOException
    {
        if (linkname == null)
        {
            throwException(linkname, "null is not allowed");
        }
        final int[] inodeInfo = new int[4];
        final int result = linkinfo(linkname, inodeInfo);
        if (result < 0)
        {
            throwException(linkname, strerror(result));
        }
        return inodeInfo;
    }

    /**
     * Returns the inode for the <var>filename</var>.
     * 
     * @throws WrappedIOException If the information could not be obtained, e.g. because the link
     *             does not exist.
     */
    public static final int getInode(String filename) throws WrappedIOException
    {
        return getLinkInfoArray(filename)[0];
    }

    /**
     * Returns the inode for the <var>filename</var>.
     * 
     * @throws WrappedIOException If the information could not be obtained, e.g. because the link
     *             does not exist.
     */
    public static final int getHardLinkCount(String filename) throws WrappedIOException
    {
        return getLinkInfoArray(filename)[1];
    }

    /**
     * Returns <code>true</code> if <var>filename</var> is a symbolic link and <code>false</code>
     * otherwise.
     * 
     * @throws WrappedIOException If the information could not be obtained, e.g. because the link
     *             does not exist.
     */
    public static final boolean isSymbolicLink(String filename) throws WrappedIOException
    {
        return getLinkInfoArray(filename)[2] != 0;
    }

    /**
     * Returns the value of the symbolik link <var>filename</var>, or <code>null</code>, if
     * <var>filename</var> is not a symbolic link.
     * 
     * @throws WrappedIOException If the information could not be obtained, e.g. because the link
     *             does not exist.
     */
    public static final String tryReadSymbolicLink(String linkname) throws WrappedIOException
    {
        final int[] info = getLinkInfoArray(linkname);
        return (info[2] != 0) ? readlink(linkname, info[3]) : null;
    }

    /**
     * Returns the information about <var>linkname</var>.
     * 
     * @throws WrappedIOException If the information could not be obtained, e.g. because the link
     *             does not exist.
     */
    public static final LinkInfo getLinkInfo(String linkname) throws WrappedIOException
    {
        final int[] info = getLinkInfoArray(linkname);
        final String symbolicLinkOrNull = (info[2] != 0) ? readlink(linkname, info[3]) : null;
        return new LinkInfo(info, symbolicLinkOrNull);
    }

}
