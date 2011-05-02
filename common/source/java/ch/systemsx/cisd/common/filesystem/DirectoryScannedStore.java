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

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.filesystem.DirectoryScanningTimerTask.IScannedStore;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;

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
        assert directory != null : "Directory not specified";
        assert filter != null : "File filter not specified";
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

    public StoreItem asStoreItem(String locationDescription)
    {
        return new StoreItem(FilenameUtils.getName(locationDescription));
    }

    public final boolean existsOrError(final StoreItem item)
    {
        return StoreItem.asFile(directory, item).exists();
    }

    public StoreItem[] tryListSorted(ISimpleLogger loggerOrNull)
    {
        final File[] files = FileUtilities.tryListFiles(directory, null, loggerOrNull);
        if (files != null)
        {
            FileUtilities.sortByLastModified(files);
            return StoreItem.asItems(files);
        } else
        {
            return null;
        }
    }

    public StoreItem[] tryFilterReadyToProcess(StoreItem[] items, ISimpleLogger loggerOrNull)
    {
        StoreItem currentItem = null;
        try
        {
            final List<StoreItem> result = new ArrayList<StoreItem>(items.length);
            for (StoreItem item : items)
            {
                currentItem = item;
                if (filter.accept(new File(directory, item.getName())))
                {
                    result.add(item);
                }
            }
            return result.toArray(new StoreItem[result.size()]);
        } catch (final RuntimeException ex)
        {
            if (loggerOrNull != null)
            {
                loggerOrNull.log(LogLevel.ERROR, String.format(
                        "Failed to filter store items for processing: "
                                + "filter '%s' threw exception %s (message: \"%s\") on item '%s'",
                        StringUtils.defaultIfEmpty(filter.getClass().getSimpleName(), "UNKNOWN"),
                        ex.getClass().getSimpleName(), StringUtils.defaultIfEmpty(ex.getMessage(),
                                "-"), currentItem));
            }
            return null;
        }
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return directory.toString();
    }

}