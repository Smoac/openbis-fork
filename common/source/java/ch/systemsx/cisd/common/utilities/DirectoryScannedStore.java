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
package ch.systemsx.cisd.common.utilities;

import java.io.File;
import java.io.FileFilter;

import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask.IScannedStore;

/**
 * A <code>IScannedStore</code> implementation for directories.
 * 
 * @author Christian Ribeaud
 */
public final class DirectoryScannedStore implements IScannedStore
{
    private final FileFilter filter;

    private final File directory;

    DirectoryScannedStore(final FileFilter filter, final File directory)
    {
        this.filter = filter;
        this.directory = directory;
    }

    //
    // IScannedStore
    //

    public final String getLocationDescription(final StoreItem item)
    {
        return StoreItem.asFile(directory, item).getPath();
    }

    public final boolean existsOrError(final StoreItem item)
    {
        return StoreItem.asFile(directory, item).exists();
    }

    public final StoreItem[] tryListSortedReadyToProcess(final ISimpleLogger loggerOrNull)
    {
        final File[] files = FileUtilities.tryListFiles(directory, filter, loggerOrNull);
        if (files != null)
        {
            FileUtilities.sortByLastModified(files);
            return StoreItem.asItems(files);
        } else
        {
            return null;
        }
    }
}