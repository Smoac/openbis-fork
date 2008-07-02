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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.StoreItem;

/**
 * The abstract super-class of classes that represent a file store.
 * 
 * @author Bernd Rinn
 * @author Tomasz Pylak
 */
public abstract class AbstractFileStore implements IFileStore
{
    private final HostAwareFileWithHighwaterMark hostAwareFileWithHighwaterMark;

    private final String kind;

    protected final IFileSysOperationsFactory factory;

    protected AbstractFileStore(
            final HostAwareFileWithHighwaterMark hostAwareFileWithHighwaterMark, final String kind,
            final IFileSysOperationsFactory factory)
    {
        assert hostAwareFileWithHighwaterMark != null;
        assert kind != null;
        this.hostAwareFileWithHighwaterMark = hostAwareFileWithHighwaterMark;
        this.kind = kind;
        this.factory = factory;
    }

    private final String getCanonicalPath(final File file)
    {
        if (tryGetHost() != null)
        {
            return file.getPath();
        }
        return FileUtilities.getCanonicalPath(file);
    }

    protected final File getPath()
    {
        return hostAwareFileWithHighwaterMark.getFile();
    }

    protected final String tryGetHost()
    {
        return hostAwareFileWithHighwaterMark.tryGetHost();
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
        final String srcHostOrNull = tryGetHost();
        final String destHostOrNull = ((AbstractFileStore) destinationDirectory).tryGetHost();
        final File destPath = ((AbstractFileStore) destinationDirectory).getPath();
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
        return new StoreItemLocation(tryGetHost(), StoreItem.asFile(getPath(), item)
                .getAbsolutePath());
    }

    public final boolean isParentDirectory(final IFileStore child)
    {
        if (child instanceof AbstractFileStore == false)
        {
            return false;
        }
        final AbstractFileStore potentialChild = (AbstractFileStore) child;
        return StringUtils.equals(tryGetHost(), potentialChild.tryGetHost())
                && getCanonicalPath(potentialChild.getPath()).startsWith(
                        getCanonicalPath(getPath()));
    }

    public void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        final BooleanStatus result =
                tryCheckDirectoryFullyAccessible(Constants.MILLIS_TO_WAIT_BEFORE_TIMEOUT);
        if (result.isSuccess() == false)
        {
            throw new ConfigurationFailureException(result.tryGetMessage());
        }
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
        if (obj instanceof AbstractFileStore == false)
        {
            return false;
        }
        final AbstractFileStore that = (AbstractFileStore) obj;
        final EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(kind, that.kind);
        equalsBuilder.append(hostAwareFileWithHighwaterMark, that.hostAwareFileWithHighwaterMark);
        return equalsBuilder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(kind);
        builder.append(hostAwareFileWithHighwaterMark);
        return builder.toHashCode();
    }

}