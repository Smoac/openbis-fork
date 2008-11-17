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

/**
 * A data object that provides information about a link in a file system.
 * 
 * @author Bernd Rinn
 */
public final class FileLinkInfo
{
    private final long inode;

    private final int hardLinkCount;

    private final long size;

    private final short permissions;

    private final int uid;

    private final int gid;

    private final long lastModified;

    private final FileLinkType linkType;

    private final String symbolicLinkOrNull;

    FileLinkInfo(long[] info, String symbolicLinkOrNull)
    {
        this.inode = info[0];
        this.hardLinkCount = (int) info[1];
        this.linkType = FileLinkType.values()[(int) info[2]];
        this.permissions = (short) info[3];
        this.size = info[4];
        this.uid = (int) info[5];
        this.gid = (int) info[6];
        this.lastModified = info[7];
        this.symbolicLinkOrNull = symbolicLinkOrNull;
    }

    /**
     * Returns the {@link FileLinkType} of a link.
     */
    public final FileLinkType getLinkType()
    {
        return linkType;
    }

    /**
     * Returns <code>true</code>, if this link is a symbolic link.
     */
    public final boolean isSymbolicLink()
    {
        return FileLinkType.SYMLINK == linkType;
    }

    /**
     * Returns the value of the symbolic link, or <code>null</code>, if this link is not a symbolic
     * link.
     */
    public final String tryGetSymbolicLink()
    {
        return symbolicLinkOrNull;
    }

    /**
     * Returns the inode number of this link.
     */
    public final long getInode()
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

    /**
     * Returns the size of the link in bytes.
     */
    public long getSize()
    {
        return size;
    }

    /**
     * Returns the time when the file was last modified measured in seconds since 00:00:00 UTC, Jan.
     * 1, 1970. Note that the unit deviates from {@link java.io.File#lastModified()} by a factor of
     * 1000.
     */
    public long getLastModified()
    {
        return lastModified;
    }

    /**
     * Returns the access permissions of the link.
     */
    public short getPermissions()
    {
        return permissions;
    }

    /**
     * Returns the UID of the owner of the link.
     */
    public int getUid()
    {
        return uid;
    }

    /**
     * Returns the UID of the owner of the link.
     */
    public int getGid()
    {
        return gid;
    }
}