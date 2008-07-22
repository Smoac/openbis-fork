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

package ch.systemsx.cisd.datamover.filesystem.remote;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.concurrent.InactivityMonitor.IActivitySensor;
import ch.systemsx.cisd.common.exceptions.StatusWithResult;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.AbstractCopyActivitySensor;
import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;

/**
 * A {@link IActivitySensor} that senses changes in copy operations to a {@link StoreItem} in a
 * remote store.
 * 
 * @author Bernd Rinn
 */
public class RemoteStoreCopyActivitySensor extends AbstractCopyActivitySensor
{
    private final static Logger machineLog =
        LogFactory.getLogger(LogCategory.MACHINE, RemoteStoreCopyActivitySensor.class);

    private final IFileStore destinationStore;

    private final StoreItem copyItem;

    public RemoteStoreCopyActivitySensor(IFileStore destinationStore, StoreItem copyItem)
    {
        super();
        this.destinationStore = destinationStore;
        this.copyItem = copyItem;
    }

    public RemoteStoreCopyActivitySensor(IFileStore destinationStore, StoreItem copyItem,
            int maxErrorsToIgnore)
    {
        super(maxErrorsToIgnore);
        this.destinationStore = destinationStore;
        this.copyItem = copyItem;
    }

    @Override
    protected StatusWithResult<Long> getTargetTimeOfLastActivityMoreRecentThan(long thresholdMillis)
    {
        return destinationStore.lastChangedRelative(copyItem, thresholdMillis);
    }

    @Override
    protected String getTargetDescription()
    {
        return String.format("item '%s' in store '%s'", copyItem, destinationStore);
    }

    @Override
    protected Logger getMachineLog()
    {
        return machineLog;
    }

}
