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

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.highwatermark.FileWithHighwaterMark;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkSelfTestable;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher;
import ch.systemsx.cisd.common.utilities.StoreItem;

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

    protected final IFileSysOperationsFactory factory;

    protected FileStore(final FileWithHighwaterMark fileWithHighwaterMark, final String hostOrNull,
            final String kind, final IFileSysOperationsFactory factory)
    {
        assert fileWithHighwaterMark != null;
        assert kind != null;
        this.fileWithHighwaterMark = fileWithHighwaterMark;
        this.kind = kind;
        this.hostOrNull = hostOrNull;
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
    protected final IStoreCopier constructStoreCopier(final IFileStore destinationDirectory,
            final boolean requiresDeletionBeforeCreation)
    {
        final IPathCopier copier = factory.getCopier(requiresDeletionBeforeCreation);
        final String srcHostOrNull = hostOrNull;
        final String destHostOrNull = ((FileStore) destinationDirectory).hostOrNull;
        final File destPath = ((FileStore) destinationDirectory).getPath();
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

    public final StoreItemLocation getStoreItemLocation(final StoreItem item)
    {
        return new StoreItemLocation(hostOrNull, StoreItem.asFile(getPath(), item)
                .getAbsolutePath());
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
                tryCheckDirectoryFullyAccessible(Constants.MILLIS_TO_WAIT_BEFORE_TIMEOUT);
        if (errorMessage != null)
        {
            throw new ConfigurationFailureException(errorMessage);
        }
        final HighwaterMarkWatcher highwaterMarkWatcher = getHighwaterMarkWatcher();
        new HighwaterMarkSelfTestable(fileWithHighwaterMark.getFile(), highwaterMarkWatcher)
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

}