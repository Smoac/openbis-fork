/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.datamover.filesystem.intf;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.utilities.StoreItem;

/**
 * A class to holds the information about a file store.
 * 
 * @author Bernd Rinn
 * @author Tomasz Pylak
 */
public abstract class FileStore
{
    protected final File path;

    protected final String hostOrNull;

    protected final String kind;

    protected final boolean remote;

    protected final IFileSysOperationsFactory factory;

    protected FileStore(File path, String hostOrNull, boolean remote, String kind, IFileSysOperationsFactory factory)
    {
        assert path != null;
        assert kind != null;
        this.path = path;
        this.kind = kind;
        this.hostOrNull = hostOrNull;
        this.remote = remote;
        this.factory = factory;
    }

    protected final File getPath()
    {
        return path;
    }

    protected final String tryGetHost()
    {
        return hostOrNull;
    }

    protected final String getDescription()
    {
        return kind;
    }

    protected final File getChildFile(StoreItem item)
    {
        return new File(path, item.getName());
    }

    // does not take into account the fact, that the destination cannot be overwritten and must be deleted beforehand
    protected final IStoreCopier constructStoreCopier(FileStore destinationDirectory,
            boolean requiresDeletionBeforeCreation)
    {
        final IPathCopier copier = factory.getCopier(requiresDeletionBeforeCreation);
        final String srcHostOrNull = hostOrNull;
        final String destHostOrNull = destinationDirectory.hostOrNull;
        final File destPath = destinationDirectory.path;
        return new IStoreCopier()
            {
                public Status copy(StoreItem item)
                {
                    File srcItem = getChildFile(item);
                    if (srcHostOrNull == null)
                    {
                        if (destHostOrNull == null)
                        {
                            return copier.copy(srcItem, destPath);
                        } else
                        {
                            return copier.copyToRemote(srcItem, destPath, destHostOrNull);
                        }
                    } else
                    {
                        assert destHostOrNull == null;
                        return copier.copyFromRemote(srcItem, srcHostOrNull, destPath);
                    }
                }

                public boolean terminate()
                {
                    return copier.terminate();
                }
            };
    }

    /**
     * Returns <code>true</code>, if the file store resides on a remote computer and <code>false</code> otherwise.
     * <p>
     * Note that even if this method returns <code>true</code> paths on this file system might be reached via local
     * file system operation, if the remote file system is provided as a remote share and mounted via NFS or CIFS.
     */
    public final boolean isRemote()
    {
        return remote;
    }

    public boolean isParentDirectory(FileStore child)
    {
        return StringUtils.equals(hostOrNull, child.hostOrNull)
                && getCanonicalPath(child.path).startsWith(getCanonicalPath(path));
    }

    private String getCanonicalPath(File file)
    {
        if (hostOrNull != null)
        {
            return file.getPath();
        }
        try
        {
            return file.getCanonicalPath() + File.separator;
        } catch (IOException e)
        {
            throw EnvironmentFailureException.fromTemplate(e, "Cannot determine canonical form of path '%s'", file
                    .getPath());
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof FileStore)
        {
            FileStore fileStore = (FileStore) obj;
            boolean sameHost =
                    (hostOrNull == null ? fileStore.hostOrNull == null : fileStore.hostOrNull != null
                            && hostOrNull.equals(fileStore.hostOrNull));
            return sameHost && kind.equals(fileStore.kind) && path.equals(fileStore.path);
        } else
        {
            return false;
        }
    }

    // -------------------

    /**
     * Checks whether this store is a directory and is fully accessible to the program.
     * 
     * @return <code>null</code> if the <var>directory</var> is fully accessible and an error message describing the
     *         problem with the <var>directory</var> otherwise.
     */
    public abstract String tryCheckDirectoryFullyAccessible();

    public abstract boolean exists(StoreItem item);

    /**
     * Returns the last time when there was a write access to <var>resource</var>.
     * 
     * @return The time (in milliseconds since the start of the epoch) when <var>resource</var> was last changed.
     */
    public abstract long lastChanged(StoreItem item);

    /**
     * List files in the scanned store. Sort in order of "oldest first".  
     */
    public abstract StoreItem[] tryListSortByLastModified(ISimpleLogger loggerOrNull);

    public abstract Status delete(StoreItem item);

    /**
     * @param destinationDirectory The directory to use as a destination in the copy operation. It must be readable and
     *            writable. Copier will override the destination item if it already exists.
     */
    public abstract IStoreCopier getCopier(FileStore destinationDirectory);

    // returned description should give the user the idea about file location. You should not use the result for
    // something else than printing it for user. It should not be especially assumed that the result is the path
    // which could be used in java.io.File constructor.
    public abstract String getLocationDescription(StoreItem item);

    public abstract ExtendedFileStore tryAsExtended();

    public static abstract class ExtendedFileStore extends FileStore
    {
        protected ExtendedFileStore(File path, String hostOrNull, boolean remote, String kind,
                IFileSysOperationsFactory factory)
        {
            super(path, hostOrNull, remote, kind, factory);
        }

        public abstract boolean createNewFile(StoreItem item);

        public abstract File tryMoveLocal(StoreItem sourceItem, File destinationDir, String newFilePrefix);
    }
}