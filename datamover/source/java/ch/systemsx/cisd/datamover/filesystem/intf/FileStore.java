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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.highwatermark.FileWithHighwaterMark;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkSelfTestable;
import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.datamover.filesystem.remote.RemotePathMover;

/**
 * The abstract super-class of classes that represent a file store.
 * 
 * @author Bernd Rinn
 * @author Tomasz Pylak
 */
public abstract class FileStore implements IFileStore
{
    private final FileWithHighwaterMark fileWithHighwaterMark;

    private final String hostOrNull;

    private final String kind;

    private final boolean remote;

    protected final IFileSysOperationsFactory factory;

    protected FileStore(final FileWithHighwaterMark path, final String hostOrNull,
            final boolean remote, final String kind, final IFileSysOperationsFactory factory)
    {
        assert path != null;
        assert kind != null;
        this.fileWithHighwaterMark = path;
        this.kind = kind;
        this.hostOrNull = hostOrNull;
        this.remote = remote;
        this.factory = factory;
    }

    private final String getCanonicalPath(final File file)
    {
        if (hostOrNull != null)
        {
            return file.getPath();
        }
        try
        {
            return file.getCanonicalPath() + File.separator;
        } catch (final IOException e)
        {
            throw EnvironmentFailureException.fromTemplate(e,
                    "Cannot determine canonical form of path '%s'", file.getPath());
        }
    }

    protected final File getPath()
    {
        return fileWithHighwaterMark.getFile();
    }

    protected final String tryGetHost()
    {
        return hostOrNull;
    }

    protected final String getDescription()
    {
        return kind;
    }

    protected final File getChildFile(final StoreItem item)
    {
        return new File(getPath(), item.getName());
    }

    // does not take into account the fact, that the destination cannot be overwritten and must be
    // deleted beforehand
    protected final IStoreCopier constructStoreCopier(final FileStore destinationDirectory,
            final boolean requiresDeletionBeforeCreation)
    {
        final IPathCopier copier = factory.getCopier(requiresDeletionBeforeCreation);
        final String srcHostOrNull = hostOrNull;
        final String destHostOrNull = destinationDirectory.hostOrNull;
        final File destPath = destinationDirectory.getPath();
        return new IStoreCopier()
            {
                public Status copy(final StoreItem item)
                {
                    final File srcItem = getChildFile(item);
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
     * Returns <code>true</code>, if the file store resides on a remote computer and
     * <code>false</code> otherwise.
     * <p>
     * Note that even if this method returns <code>true</code> paths on this file system might be
     * reached via local file system operation, if the remote file system is provided as a remote
     * share and mounted via NFS or CIFS.
     */
    public final boolean isRemote()
    {
        return remote;
    }

    public final boolean isParentDirectory(final IFileStore child)
    {
        if (child instanceof FileStore == false)
        {
            return false;
        }
        final FileStore potentialChild = (FileStore) child;
        return StringUtils.equals(hostOrNull, potentialChild.hostOrNull)
                && getCanonicalPath(potentialChild.getPath()).startsWith(
                        getCanonicalPath(getPath()));
    }

    public void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        final String errorMessage =
                tryCheckDirectoryFullyAccessible(RemotePathMover.DIRECTORY_ACCESSIBLE_TIMEOUT_MILLIS);
        if (errorMessage != null)
        {
            throw new ConfigurationFailureException(errorMessage);
        }
        new HighwaterMarkSelfTestable(fileWithHighwaterMark.getFile(), getHighwaterMarkWatcher())
                .check();
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof FileStore == false)
        {
            return false;
        }
        final FileStore that = (FileStore) obj;
        final EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(hostOrNull, that.hostOrNull);
        equalsBuilder.append(kind, that.kind);
        equalsBuilder.append(fileWithHighwaterMark, that.fileWithHighwaterMark);
        return equalsBuilder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(hostOrNull);
        builder.append(kind);
        builder.append(fileWithHighwaterMark);
        return builder.toHashCode();
    }


    //
    // Helper classes
    //

    public static abstract class ExtendedFileStore extends FileStore implements IExtendedFileStore
    {
        protected ExtendedFileStore(final FileWithHighwaterMark path, final String hostOrNull,
                final boolean remote, final String kind, final IFileSysOperationsFactory factory)
        {
            super(path, hostOrNull, remote, kind, factory);
        }

        //
        // IExtendedFileStore
        //

        public abstract boolean createNewFile(StoreItem item);

        public abstract File tryMoveLocal(StoreItem sourceItem, File destinationDir,
                String newFilePrefix);
    }
}