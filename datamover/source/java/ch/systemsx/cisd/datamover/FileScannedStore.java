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
package ch.systemsx.cisd.datamover;

import java.util.Vector;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask.IScannedStore;
import ch.systemsx.cisd.datamover.filesystem.intf.BooleanStatus;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.utils.IStoreItemFilter;

/**
 * An <code>IScannedStore</code> implementation which is based on {@link IFileStore}.
 * 
 * @author Christian Ribeaud
 */
final class FileScannedStore implements IScannedStore
{
    private final IFileStore fileStore;

    private final IStoreItemFilter storeItemFilter;

    FileScannedStore(final IFileStore fileStore, final IStoreItemFilter storeItemFilter)
    {
        assert fileStore != null : "File store not specified";
        assert storeItemFilter != null : "Store item filter not specified";
        this.fileStore = fileStore;
        this.storeItemFilter = storeItemFilter;
    }

    private final StoreItem[] filterReadyToProcess(final StoreItem[] items)
    {
        final Vector<StoreItem> result = new Vector<StoreItem>();
        for (final StoreItem item : items)
        {
            if (isReadyToProcess(item))
            {
                result.add(item);
            }
        }
        return result.toArray(StoreItem.EMPTY_ARRAY);
    }

    private final boolean isReadyToProcess(final StoreItem item)
    {
        if (item.getName().startsWith(Constants.DELETION_IN_PROGRESS_PREFIX))
        {
            return false;
        }
        return storeItemFilter.accept(item);
    }

    //
    // IScannedStore
    //

    public final boolean existsOrError(final StoreItem item)
    {
        final BooleanStatus status = fileStore.exists(item);
        return status.isError() || status.getResult() == true;
    }

    public final String getLocationDescription(final StoreItem item)
    {
        return fileStore.getLocationDescription(item);
    }

    public final StoreItem[] tryListSortedReadyToProcess(final ISimpleLogger loggerOrNull)
    {
        // Older items will be handled before newer items. This becomes important when doing online
        // quality control of measurements.
        final StoreItem[] items = fileStore.tryListSortByLastModified(loggerOrNull);
        if (items == null)
        {
            return null;
        }
        return filterReadyToProcess(items);
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return fileStore.toString();
    }
}