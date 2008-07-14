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

import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask.IScannedStore;

/**
 * A helper class for {@link DirectoryScanningTimerTask} which performs operations before and after
 * treating the matching paths.
 * 
 * @author Christian Ribeaud
 * @see DirectoryScanningTimerTask
 */
public interface IDirectoryScanningHandler
{

    /**
     * The instruction of whether to process an item or not.
     */
    public enum HandleInstruction
    {
        PROCESS, IGNORE, ERROR
    }

    /**
     * Is performed just before handling all the items contained in the store.
     */
    public void beforeHandle();

    /**
     * Whether given <code>storeItem</code> found in given <var>scannedStore</var> should be
     * processed or not, and whether not processing it constitues an error or not.
     */
    public HandleInstruction mayHandle(IScannedStore scannedStore, StoreItem storeItem);

    /**
     * Finishes and closes the handling of given <var>storeItem</var>.
     * 
     * @returns <code>true</code>, if the item has been handled correctly and <code>false</code>
     *          if an error occurred.
     */
    public boolean finishItemHandle(IScannedStore scannedStore, StoreItem storeItem);
}
