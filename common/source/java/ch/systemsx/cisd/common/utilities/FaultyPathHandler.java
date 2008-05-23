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
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collections.CollectionIO;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask.IScannedStore;

/**
 * An <code>IDirectoryScanningHandler</code> which manages faulty paths.
 * <p>
 * A faulty path is a {@link StoreItem} which was <i>not handled</i> (still exists in the
 * {@link IScannedStore}).
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class FaultyPathHandler implements IDirectoryScanningHandler
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, FaultyPathHandler.class);

    private final Set<String> faultyPaths;

    private final File faultyPathsFile;

    private long faultyPathsLastChanged;

    static final String FAULTY_PATH_FILENAME = ".faulty_paths";

    public FaultyPathHandler(final File faultyPathDirectory)
    {
        this.faultyPaths = new HashSet<String>();
        this.faultyPathsFile = new File(faultyPathDirectory, FAULTY_PATH_FILENAME);
        faultyPathsFile.delete();
    }

    private final void checkForFaultyPathsFileChanged()
    {
        if (faultyPathsFile.exists())
        {
            // Handles manual manipulation.
            if (faultyPathsFile.lastModified() > faultyPathsLastChanged)
            {
                faultyPaths.clear();
                CollectionIO.readCollection(faultyPathsFile, faultyPaths);
                faultyPathsLastChanged = faultyPathsFile.lastModified();
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info(String.format(
                            "Reread faulty paths file (%s), new set contains %d entries",
                            getLocationDescription(faultyPathsFile), faultyPaths.size()));
                }
            }
        } else
        {
            // Handles manual removal.
            faultyPaths.clear();
        }
    }

    private final static String getLocationDescription(final File file)
    {
        return file.getPath();
    }

    private final boolean isFaultyPath(final IScannedStore scannedStore, final StoreItem storeItem)
    {
        final String path = scannedStore.getLocationDescription(storeItem);
        return faultyPaths.contains(path);
    }

    private final boolean isFaultyPathsFile(final IScannedStore scannedStore,
            final StoreItem storeItem)
    {
        final String itemLocation = scannedStore.getLocationDescription(storeItem);
        final String faultyPathsLocation = getLocationDescription(faultyPathsFile);
        return itemLocation.equals(faultyPathsLocation);
    }

    private final void addToFaultyPaths(final IScannedStore scannedStore, final StoreItem item)
    {
        final String path = scannedStore.getLocationDescription(item);
        faultyPaths.add(path);
        refreshFaultyPathsFile();
    }

    private final void refreshFaultyPathsFile()
    {
        CollectionIO.writeIterable(faultyPathsFile, faultyPaths);
        faultyPathsLastChanged = faultyPathsFile.lastModified();
    }

    //
    // IDirectoryScanningHandler
    //

    public final void beforeHandle()
    {
        checkForFaultyPathsFileChanged();
    }

    public final boolean mayHandle(final IScannedStore scannedStore, final StoreItem storeItem)
    {
        return isFaultyPath(scannedStore, storeItem) == false
                && isFaultyPathsFile(scannedStore, storeItem) == false;
    }

    public final void finishItemHandle(final IScannedStore scannedStore, final StoreItem storeItem)
    {
        // If the item still exists, we assume that it has not been handled. So it
        // should be added to the faulty paths.
        if (scannedStore.exists(storeItem))
        {
            addToFaultyPaths(scannedStore, storeItem);
        }
    }

}
