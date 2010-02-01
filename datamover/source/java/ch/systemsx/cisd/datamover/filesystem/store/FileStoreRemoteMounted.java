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

package ch.systemsx.cisd.datamover.filesystem.store;

import ch.systemsx.cisd.base.exceptions.TimeoutExceptionUnchecked;
import ch.systemsx.cisd.common.TimingParameters;
import ch.systemsx.cisd.common.concurrent.MonitoringProxy;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusWithResult;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.StoreItem;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher;
import ch.systemsx.cisd.common.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.datamover.filesystem.intf.AbstractFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IExtendedFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IStoreCopier;

/**
 * A <code>FileStore</code> extension for remote paths mounted.
 * <p>
 * The works is mainly delegated to an internal {@link FileStoreLocal}.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public final class FileStoreRemoteMounted extends AbstractFileStore
{
    private final IFileStore localImpl;

    private final IFileStore localImplMonitored;

    /**
     * @param lastChangedTimeoutMillis number of milliseconds after which checking last modification
     *            time of the item will be terminated and will return with an error.
     */
    public FileStoreRemoteMounted(final HostAwareFileWithHighwaterMark file,
            final String description, final IFileSysOperationsFactory factory,
            final boolean skipAccessibilityTest, final long lastChangedTimeoutMillis)
    {
        super(file, description, factory);
        this.localImpl = new FileStoreLocal(file, description, factory, skipAccessibilityTest);
        this.localImplMonitored =
                MonitoringProxy.create(IFileStore.class, localImpl).timing(
                        TimingParameters.create(lastChangedTimeoutMillis)).get();
    }

    //
    // FileStore
    //

    public final IExtendedFileStore tryAsExtended()
    {
        return null;
    }

    public final IStoreCopier getCopier(final IFileStore destinationDirectory)
    {
        final boolean requiresDeletion = false;
        return constructStoreCopier(destinationDirectory, requiresDeletion);
    }

    public final String getLocationDescription(final StoreItem item)
    {
        return localImpl.getLocationDescription(item);
    }

    public final Status delete(final StoreItem item)
    {
        try
        {
            // we do not run delete with a timeout
            return localImpl.delete(item);
        } catch (TimeoutExceptionUnchecked ex)
        {
            return Status.createRetriableError(ex.getMessage());
        }
    }

    public final BooleanStatus exists(final StoreItem item)
    {
        try
        {
            final BooleanStatus statusOrNull = localImplMonitored.exists(item);
            if (statusOrNull == null)
            {
                return BooleanStatus.createError("Could not determine whether '" + item
                        + "' exists: time out.");
            }
            return statusOrNull;
        } catch (TimeoutExceptionUnchecked ex)
        {
            return BooleanStatus.createError(ex.getMessage());
        }
    }

    public final StatusWithResult<Long> lastChanged(final StoreItem item,
            final long stopWhenFindYounger)
    {
        try
        {
            return localImplMonitored.lastChanged(item, stopWhenFindYounger);
        } catch (TimeoutExceptionUnchecked ex)
        {
            return StatusWithResult.<Long> createRetriableError(ex.getMessage());
        }
    }

    public final StatusWithResult<Long> lastChangedRelative(final StoreItem item,
            final long stopWhenFindYoungerRelative)
    {
        try
        {
            final StatusWithResult<Long> statusOrNull =
                    localImplMonitored.lastChangedRelative(item, stopWhenFindYoungerRelative);
            if (statusOrNull == null)
            {
                return StatusWithResult.<Long> createError(String.format(
                        "Could not determine \"last changed time\" of %s: time out.", item));
            }
            return statusOrNull;
        } catch (TimeoutExceptionUnchecked ex)
        {
            return StatusWithResult.<Long> createRetriableError(ex.getMessage());
        }
    }

    public final BooleanStatus checkDirectoryFullyAccessible(final long timeOutMillis)
    {
        try
        {
            final BooleanStatus statusOrNull =
                    localImplMonitored.checkDirectoryFullyAccessible(timeOutMillis);
            if (statusOrNull == null)
            {
                return BooleanStatus.createError("Could not determine whether store '" + toString()
                        + "' is fully accessible: time out.");
            }
            return statusOrNull;
        } catch (TimeoutExceptionUnchecked ex)
        {
            return BooleanStatus.createError(ex.getMessage());
        }
    }

    private static class StoringLogger implements ISimpleLogger
    {

        LogLevel storedLevel = null;

        String storedMessage = null;

        public void log(LogLevel level, String message)
        {
            this.storedLevel = level;
            this.storedMessage = message;
        }

    }

    public final StoreItem[] tryListSortByLastModified(final ISimpleLogger loggerOrNull)
    {
        final StoringLogger storingLoggerOrNull =
                (loggerOrNull == null) ? null : new StoringLogger();
        final StoreItem[] itemsOrNull =
                localImplMonitored.tryListSortByLastModified(storingLoggerOrNull);
        if (loggerOrNull != null)
        {
            if (storingLoggerOrNull.storedMessage != null)
            {
                loggerOrNull
                        .log(storingLoggerOrNull.storedLevel, storingLoggerOrNull.storedMessage);
            } else if (itemsOrNull == null)
            {
                loggerOrNull.log(LogLevel.ERROR, "Could not get listing of store '" + toString()
                        + "': time out.");
            }
        }
        return itemsOrNull;
    }

    public final HighwaterMarkWatcher getHighwaterMarkWatcher()
    {
        return localImpl.getHighwaterMarkWatcher();
    }

    public boolean isRemote()
    {
        return true;
    }

    //
    // AbstractFileStore
    //

    @Override
    public final void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        localImpl.check();
    }

    @Override
    public final String toString()
    {
        final String pathStr = getPath().getPath();
        return "[mounted remote fs] " + pathStr;
    }

}
