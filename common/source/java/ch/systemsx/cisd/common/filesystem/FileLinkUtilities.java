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

/**
 * A utility class that provides access to hard link and symbolic link creation on Unix platforms.
 * 
 * @author Bernd Rinn
 */
public class FileLinkUtilities
{

    private final static boolean operational = FileUtilities.loadNativeLibraryFromResource("jlink");

    private static void throwLinkCreationException(String type, String source, String target,
            String errorMessage)
    {
        throw new WrappedIOException(new IOException(String.format(
                "Creating %s link '%s' -> '%s': %s", type, source, target, errorMessage)));
    }

    private static void throwStatException(String filename, String errorMessage)
    {
        throw new WrappedIOException(new IOException(String.format(
                "Cannot obtain inode info for file '%s': %s", filename, errorMessage)));
    }

    private static void throwFileException(String operation, String filename, String errorMessage)
    {
        throw new WrappedIOException(new IOException(String.format("Cannot %s of file '%s': %s",
                operation, filename, errorMessage)));
    }

    private static void throwIdException(String operation, int id, String errorMessage)
    {
        throw new WrappedIOException(new IOException(String.format("Cannot %s of id %s: %s",
                operation, id, errorMessage)));
    }

    private static void throwNameException(String operation, String name, String errorMessage)
    {
        throw new WrappedIOException(new IOException(String.format("Cannot %s of name '%s': %s",
                operation, name, errorMessage)));
    }

    private static native int hardlink(String filename, String linktarget);

    private static native int symlink(String filename, String linktarget);

    private static native int linkinfo(String filename, long[] info);

    private static native String readlink(String filename, int linkvallen);

    private static native int chmod(String filename, short mode);

    private static native int chown(String filename, int uid, int gid);

    private static native String getpwuid(int uid);

    private static native String getgrgid(int gid);

    private static native int getpwnam(String user);

    private static native int getgrnam(String group);

    private static native String strerror(int errnum);

    private static native String strerrorErrno();

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
            throwLinkCreationException("hard", filename, linkname, "null is not allowed");
        }
        final int result = hardlink(filename, linkname);
        if (result < 0)
        {
            throwLinkCreationException("hard", filename, linkname, strerror(result));
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
            throwLinkCreationException("symbolic", filename, linkname, "null is not allowed");
        }
        final int result = symlink(filename, linkname);
        if (result < 0)
        {
            throwLinkCreationException("symbolic", filename, linkname, strerror(result));
        }
    }

    private static long[] getLinkInfoArray(String linkname) throws WrappedIOException
    {
        if (linkname == null)
        {
            throwStatException(linkname, "null is not allowed");
        }
        final long[] inodeInfo = new long[8];
        final int result = linkinfo(linkname, inodeInfo);
        if (result < 0)
        {
            throwStatException(linkname, strerror(result));
        }
        return inodeInfo;
    }

    /**
     * Returns the inode for the <var>filename</var>.
     * 
     * @throws WrappedIOException If the information could not be obtained, e.g. because the link
     *             does not exist.
     */
    public static final long getInode(String filename) throws WrappedIOException
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
        return (int) getLinkInfoArray(filename)[1];
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
        final long[] info = getLinkInfoArray(linkname);
        return FileLinkType.isSymLink(info[2]) ? readlink(linkname, (int) info[4]) : null;
    }

    /**
     * Returns the information about <var>linkname</var>.
     * 
     * @throws WrappedIOException If the information could not be obtained, e.g. because the link
     *             does not exist.
     */
    public static final FileLinkInfo getLinkInfo(String linkname) throws WrappedIOException
    {
        return getLinkInfo(linkname, true);
    }

    /**
     * Returns the information about <var>linkname</var>. If
     * <code>readSymbolicLinkTarget == true</code>, then the symbolic link target is read when
     * <var>linkname</var> is a symbolic link.
     * 
     * @throws WrappedIOException If the information could not be obtained, e.g. because the link
     *             does not exist.
     */
    public static final FileLinkInfo getLinkInfo(String linkname, boolean readSymbolicLinkTarget)
            throws WrappedIOException
    {
        final long[] info = getLinkInfoArray(linkname);
        final String symbolicLinkOrNull =
                (readSymbolicLinkTarget && FileLinkType.isSymLink(info[2])) ? readlink(
                        linkname, (int) info[4]) : null;
        return new FileLinkInfo(info, symbolicLinkOrNull);
    }

    /**
     * Sets the access mode of <var>filename</var> to the specified <var>mode</var> value.
     */
    public static final void setAccessMode(String filename, short mode) throws WrappedIOException
    {
        final int result = chmod(filename, mode);
        if (result < 0)
        {
            throwFileException("set mode", filename, strerror(result));
        }
    }

    /**
     * Sets the owner of <var>filename</var> to the specified <var>uid</var> and <var>gid</var>
     * values.
     */
    public static final void setOwner(String filename, int uid, int gid) throws WrappedIOException
    {
        final int result = chown(filename, uid, gid);
        if (result < 0)
        {
            throwFileException("set owner", filename, strerror(result));
        }
    }

    /**
     * Returns the name of the user identified by <var>uid</var>.
     */
    public static final String getUserNameForUid(int uid) throws WrappedIOException
    {
        final String name = getpwuid(uid);
        if (name == null)
        {
            throwIdException("get user name", uid, strerrorErrno());
        }
        return name;
    }

    /**
     * Returns the name of the group identified by <var>gid</var>.
     */
    public static final String getGroupNameForGid(int gid) throws WrappedIOException
    {
        final String name = getgrgid(gid);
        if (name == null)
        {
            throwIdException("get group name", gid, strerrorErrno());
        }
        return name;
    }

    /**
     * Returns the uid of the <var>user</var>.
     */
    public static final int getUidForUserName(String user) throws WrappedIOException
    {
        final int uid = getpwnam(user);
        if (uid < 0)
        {
            throwNameException("get uid", user, strerrorErrno());
        }
        return uid;
    }

    /**
     * Returns the gid of the <var>group</var>.
     */
    public static final int getGidForGroupName(String group) throws WrappedIOException
    {
        final int gid = getgrnam(group);
        if (gid < 0)
        {
            throwNameException("get gid", group, strerrorErrno());
        }
        return gid;
    }

}
