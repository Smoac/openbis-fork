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

package ch.systemsx.cisd.datamover.utils;

import java.io.FileFilter;

import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.datamover.filesystem.intf.FileStore;
import ch.systemsx.cisd.datamover.intf.ITimingParameters;

/**
 * A {@link FileFilter} that picks all entries that haven't been changed for longer than some given quiet period.
 * 
 * @author Bernd Rinn
 */
public class QuietPeriodFileFilter
{
    private final long quietPeriodMillis;

    private final FileStore store;

    /**
     * Creates a <var>QuietPeriodFileFilter</var>.
     * 
     * @param store The store in which items reside
     * @param timingParameters The timing paramter object to get the quiet period from.
     */
    public QuietPeriodFileFilter(FileStore store, ITimingParameters timingParameters)
    {
        this.store = store;
        this.quietPeriodMillis = timingParameters.getQuietPeriodMillis();
        assert quietPeriodMillis > 0;
    }

    public boolean accept(StoreItem item)
    {
        return (System.currentTimeMillis() - store.lastChanged(item)) > quietPeriodMillis;
    }

}
