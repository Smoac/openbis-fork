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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.ExecutionResult;
import ch.systemsx.cisd.common.concurrent.NamingThreadPoolExecutor;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher;
import ch.systemsx.cisd.common.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.datamover.filesystem.intf.AbstractFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.BooleanStatus;
import ch.systemsx.cisd.datamover.filesystem.intf.IExtendedFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IStoreCopier;
import ch.systemsx.cisd.datamover.filesystem.intf.DateStatus;

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
    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, FileStoreRemoteMounted.class);

    private final IFileStore localImpl;

    private final LastChangeWrapper lastChangeInvoker;

    /**
     * @param lastChangedTimeoutMillis number of milliseconds after which checking last modification
     *            time of the item will be terminated and will return with an error.
     */
    public FileStoreRemoteMounted(final HostAwareFileWithHighwaterMark file,
            final String desription, final IFileSysOperationsFactory factory,
            long lastChangedTimeoutMillis)
    {
        super(file, desription, factory);
        this.localImpl = new FileStoreLocal(file, desription, factory);
        this.lastChangeInvoker = new LastChangeWrapper(localImpl, lastChangedTimeoutMillis);
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
        return localImpl.delete(item);
    }

    public final BooleanStatus exists(final StoreItem item)
    {
        return localImpl.exists(item);
    }

    public final DateStatus lastChanged(final StoreItem item, final long stopWhenFindYounger)
    {
        return lastChangeInvoker.lastChangedInternal(item, stopWhenFindYounger, false);
    }

    public final DateStatus lastChangedRelative(final StoreItem item,
            final long stopWhenFindYoungerRelative)
    {
        return lastChangeInvoker.lastChangedInternal(item, stopWhenFindYoungerRelative, true);
    }

    public final BooleanStatus tryCheckDirectoryFullyAccessible(final long timeOutMillis)
    {
        return localImpl.tryCheckDirectoryFullyAccessible(timeOutMillis);
    }

    public final StoreItem[] tryListSortByLastModified(final ISimpleLogger loggerOrNull)
    {
        return localImpl.tryListSortByLastModified(loggerOrNull);
    }

    public final HighwaterMarkWatcher getHighwaterMarkWatcher()
    {
        return localImpl.getHighwaterMarkWatcher();
    }

    //
    // FileStore
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

    // -----

    @Private
    static final class LastChangeWrapper
    {
        private final ExecutorService lastChangedExecutor =
                new NamingThreadPoolExecutor("Last Changed Explorer").daemonize();

        private final long lastChangedTimeoutMillis;

        private final IFileStore localImpl;

        public LastChangeWrapper(IFileStore localImpl, long lastChangedTimeoutMillis)
        {
            this.lastChangedTimeoutMillis = lastChangedTimeoutMillis;
            this.localImpl = localImpl;
        }

        // call checking last change in a separate thread with timeout
        public DateStatus lastChangedInternal(StoreItem item, long stopWhenFindYoungerAge,
                boolean isAgeRelative)
        {
            Callable<DateStatus> callable =
                    createLastChangedCallable(localImpl, item, stopWhenFindYoungerAge,
                            isAgeRelative);
            final ISimpleLogger simpleMachineLog = new Log4jSimpleLogger(machineLog);
            final Future<DateStatus> future = lastChangedExecutor.submit(callable);
            ExecutionResult<DateStatus> executionResult =
                    ConcurrencyUtilities.getResult(future, lastChangedTimeoutMillis,
                            simpleMachineLog, "Check for recent paths");
            DateStatus result = executionResult.tryGetResult();
            if (result == null)
            {
                return DateStatus.createError(String.format(
                        "Could not determine \"last changed time\" of %s: time out.", item));
            } else
            {
                return result;
            }
        }

        private Callable<DateStatus> createLastChangedCallable(final IFileStore store,
                final StoreItem item, final long stopWhenFindYoungerAge, final boolean isAgeRelative)
        {
            return new Callable<DateStatus>()
                {
                    public DateStatus call() throws Exception
                    {
                        if (machineLog.isTraceEnabled())
                        {
                            machineLog.trace("Starting quick check for recent paths on '" + item
                                    + "'.");
                        }
                        final DateStatus lastChanged;
                        if (isAgeRelative)
                        {
                            lastChanged = store.lastChangedRelative(item, stopWhenFindYoungerAge);
                        } else
                        {
                            lastChanged = store.lastChanged(item, stopWhenFindYoungerAge);
                        }
                        if (machineLog.isTraceEnabled())
                        {
                            machineLog.trace(String.format(
                                    "Finishing quick check for recent paths on '%s': %s.", item,
                                    lastChanged));
                        }
                        return lastChanged;
                    }
                };
        }
    }
}
