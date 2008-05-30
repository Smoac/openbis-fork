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

package ch.systemsx.cisd.datamover.filesystem.intf;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.utilities.ISelfTestable;
import ch.systemsx.cisd.common.utilities.StoreItem;

/**
 * An abstraction for a file store.
 * 
 * @author Bernd Rinn
 */
public interface IFileStore extends ISelfTestable
{
    /**
     * Returns the location of the specified store item.
     */
    public StoreItemLocation getStoreItemLocation(StoreItem item);

    /**
     * Returns <code>true</code> if this file store is the parent directory of the specified file
     * store.
     */
    public boolean isParentDirectory(IFileStore child);

    /**
     * Checks whether this store is a directory and is fully accessible to the program.
     * 
     * @param timeOutMillis The time (in milli-seconds) to wait for the target to become available
     *            if it is not initially.
     * @return <code>null</code> if the <var>directory</var> is fully accessible and an error
     *         message describing the problem with the <var>directory</var> otherwise.
     */
    public String tryCheckDirectoryFullyAccessible(final long timeOutMillis);

    /**
     * Returns <code>true</code> if the specified store item exists in this file store.
     */
    public boolean exists(StoreItem item);

    /**
     * Returns the last time when there was a write access to <var>item</var>.
     * 
     * @param item The {@link StoreItem} to check.
     * @param stopWhenFindYounger If &gt; 0, the recursive search for younger file will be stopped
     *            when a file or directory is found that is younger than the time specified in this
     *            parameter. Supposed to be used when one does not care about the absolutely
     *            youngest entry, but only, if there are entries that are "young enough".
     * @return The time (in milliseconds since the start of the epoch) when <var>resource</var> was
     *         last changed.
     */
    public long lastChanged(StoreItem item, long stopWhenFindYounger);

    /**
     * Returns the last time when there was a write access to <var>item</var>.
     * 
     * @param item The {@link StoreItem} to check.
     * @param stopWhenFindYoungerRelative If &gt; 0, the recursive search for younger file will be
     *            stopped when a file or directory is found that is younger than
     *            <code>System.currentTimeMillis() - stopWhenYoungerRelative</code>.
     * @return The time (in milliseconds since the start of the epoch) when <var>resource</var> was
     *         last changed.
     */
    public long lastChangedRelative(StoreItem item, long stopWhenFindYoungerRelative);

    /**
     * List files in the scanned store. Sort in order of "oldest first".
     */
    public StoreItem[] tryListSortByLastModified(ISimpleLogger loggerOrNull);

    /**
     * Deletes the specified item from this store.
     */
    public Status delete(StoreItem item);

    /**
     * @param destinationDirectory The directory to use as a destination in the copy operation. It
     *            must be readable and writable. Copier will override the destination item if it
     *            already exists.
     */
    public IStoreCopier getCopier(IFileStore destinationDirectory);

    // returned description should give the user the idea about file location. You should not use
    // the result for
    // something else than printing it for user. It should not be especially assumed that the result
    // is the path
    // which could be used in java.io.File constructor.
    public String getLocationDescription(StoreItem item);

    /**
     * Returns this file store as an extended file store if possible.
     * 
     * @return <code>null</code> if this file store can not be returned as an extended file store.
     */
    public IExtendedFileStore tryAsExtended();

    /**
     * Returns the <code>HighwaterMarkWatcher</code> for this implementation.
     * <p>
     * Note that we expect the path to be set in the returned <code>HighwaterMarkWatcher</code>.
     * </p>
     */
    public HighwaterMarkWatcher getHighwaterMarkWatcher();
}