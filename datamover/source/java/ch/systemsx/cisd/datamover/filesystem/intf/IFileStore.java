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
import ch.systemsx.cisd.common.exceptions.StatusWithResult;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.ILastModificationChecker;
import ch.systemsx.cisd.common.filesystem.StoreItem;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.utilities.ISelfTestable;

/**
 * An abstraction for a file store.
 * 
 * @author Bernd Rinn
 */
public interface IFileStore extends ISelfTestable, ILastModificationChecker
{
    public static final IFileStore[] EMPTY_ARRAY = new IFileStore[0];

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
     * @return status describing if the <var>directory</var> is fully accessible. If this operation
     *         fails, there is an error message available describing the problem with the
     *         <var>directory</var>. In this case nothing can be stated about directory
     *         accessability.
     */
    public BooleanStatus checkDirectoryFullyAccessible(final long timeOutMillis);

    /**
     * Checks if the specified store item exists in this file store. This operation can fail, error
     * status is then available and the result is unknown.
     */
    public BooleanStatus exists(StoreItem item);

    /** @see ILastModificationChecker#lastChanged(StoreItem, long) */
    public StatusWithResult<Long> lastChanged(StoreItem item, long stopWhenFindYounger);

    /** @see ILastModificationChecker#lastChangedRelative(StoreItem, long) */
    public StatusWithResult<Long> lastChangedRelative(StoreItem item,
            long stopWhenFindYoungerRelative);

    /**
     * List files in the scanned store. Sort in order of "oldest first".
     * 
     * @return <code>null</code> if it was no able to access the items of this store.
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

    /**
     * @return description which should give the user the idea about item location in sthe store. It
     *         should not be used for something else than printing it for user. In particular it
     *         should not be assumed that the result is the path which could be used in java.io.File
     *         constructor.
     */
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