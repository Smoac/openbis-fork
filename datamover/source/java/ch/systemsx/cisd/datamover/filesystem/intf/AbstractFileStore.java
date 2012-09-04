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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.StoreItem;
import ch.systemsx.cisd.common.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.datamover.DatamoverConstants;

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

    protected final boolean skipAccessibilityTest;

    protected AbstractFileStore(
            final HostAwareFileWithHighwaterMark hostAwareFileWithHighwaterMark, final String kind,
            final IFileSysOperationsFactory factory, final boolean skipAccessibilityTest)
    {
        assert hostAwareFileWithHighwaterMark != null;
        assert kind != null;
        this.hostAwareFileWithHighwaterMark = hostAwareFileWithHighwaterMark;
        this.kind = kind;
        this.factory = factory;
        this.skipAccessibilityTest = skipAccessibilityTest;
    }

    protected final String getPath()
    {
        return hostAwareFileWithHighwaterMark.getPath();
    }

    protected final String getCanonicalPathIfLocal()
    {
        if (hostAwareFileWithHighwaterMark.tryGetHost() == null)
        {
            return hostAwareFileWithHighwaterMark.getCanonicalPath();
        } else
        {
            return hostAwareFileWithHighwaterMark.getPath();
        }
    }

    protected final File getLocalFile()
    {
        return hostAwareFileWithHighwaterMark.getLocalFile();
    }

    protected final String tryGetHost()
    {
        return hostAwareFileWithHighwaterMark.tryGetHost();
    }

    protected final String tryGetRsyncModuleName()
    {
        return hostAwareFileWithHighwaterMark.tryGetRsyncModule();
    }

    protected final String getDescription()
    {
        return kind;
    }

    protected final File getChildFile(final StoreItem item)
    {
        return new File(getPath(), item.getName());
    }

    protected final String getChildPath(final StoreItem item)
    {
        return FilenameUtils.concat(getPath(), item.getName());
    }

    // does not take into account the fact, that the destination cannot be overwritten and must be
    // deleted beforehand
    protected final IStoreCopier constructStoreCopier(final IFileStore destinationDirectory,
            final boolean requiresDeletionBeforeCreation)
    {
        final IPathCopier copier = factory.getCopier(requiresDeletionBeforeCreation);
        final String srcHostOrNull = tryGetHost();
        final AbstractFileStore destinationStore = (AbstractFileStore) destinationDirectory;
        final String destHostOrNull = destinationStore.tryGetHost();
        return new IStoreCopier()
            {
                @Override
                public Status copy(final StoreItem item)
                {
                    if (srcHostOrNull == null)
                    {
                        final File srcItem = getChildFile(item);
                        if (destHostOrNull == null)
                        {
                            return copier.copy(srcItem, destinationStore.getLocalFile());
                        } else
                        {
                            return copier.copyToRemote(srcItem, destinationStore.getPath(),
                                    destHostOrNull, destinationStore.tryGetRsyncModuleName(),
                                    DatamoverConstants.RSYNC_PASSWORD_FILE_OUTGOING);
                        }
                    } else
                    {
                        final String srcItem = getChildPath(item);
                        assert destHostOrNull == null;
                        return copier.copyFromRemote(srcItem, srcHostOrNull,
                                destinationStore.getLocalFile(), tryGetRsyncModuleName(),
                                DatamoverConstants.RSYNC_PASSWORD_FILE_INCOMING);
                    }
                }

                @Override
                public boolean terminate()
                {
                    return copier.terminate();
                }

                @Override
                public void check() throws ConfigurationFailureException
                {
                    if (skipAccessibilityTest)
                    {
                        return;
                    }
                    if (srcHostOrNull != null)
                    {
                        final String executableOrNull = factory.tryGetIncomingRsyncExecutable();
                        FileUtilities.checkPathCopier(copier, srcHostOrNull, executableOrNull,
                                tryGetRsyncModuleName(),
                                DatamoverConstants.RSYNC_PASSWORD_FILE_INCOMING,
                                DatamoverConstants.TIMEOUT_REMOTE_CONNECTION_MILLIS);
                    }
                    if (destHostOrNull != null)
                    {
                        final String executableOrNull = factory.tryGetOutgoingRsyncExecutable();
                        FileUtilities.checkPathCopier(copier, destHostOrNull, executableOrNull,
                                destinationStore.tryGetRsyncModuleName(),
                                DatamoverConstants.RSYNC_PASSWORD_FILE_OUTGOING,
                                DatamoverConstants.TIMEOUT_REMOTE_CONNECTION_MILLIS);
                    }
                }

                @Override
                public boolean isRemote()
                {
                    return srcHostOrNull != null || destHostOrNull != null;
                }

                @Override
                public String toString()
                {
                    String src = describe(AbstractFileStore.this);
                    String dest = describe(destinationStore);
                    return "store copier " + src + " -> " + dest;
                }

                private String describe(AbstractFileStore store)
                {
                    String description;
                    final String hostOrNull = store.tryGetHost();
                    final String rsyncModuleOrNull = store.tryGetRsyncModuleName();
                    if (hostOrNull != null)
                    {
                        description = hostOrNull;
                        if (rsyncModuleOrNull != null)
                        {
                            description += "::" + rsyncModuleOrNull;
                        }
                    } else
                    {
                        description = "local";
                    }
                    return description;
                }
            };
    }

    @Override
    public final StoreItemLocation getStoreItemLocation(final StoreItem item)
    {
        return new StoreItemLocation(tryGetHost(), StoreItem.asFile(getPath(), item)
                .getAbsolutePath());
    }

    @Override
    public final boolean isParentDirectory(final IFileStore child)
    {
        if (child instanceof AbstractFileStore == false)
        {
            return false;
        }
        final AbstractFileStore potentialChild = (AbstractFileStore) child;
        return StringUtils.equals(tryGetHost(), potentialChild.tryGetHost())
                && potentialChild.getCanonicalPathIfLocal().startsWith(getCanonicalPathIfLocal());
    }

    @Override
    public void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        final BooleanStatus result =
                checkDirectoryFullyAccessible(Constants.MILLIS_TO_WAIT_BEFORE_TIMEOUT);
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