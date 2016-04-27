/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.datamover.common;

import ch.systemsx.cisd.common.filesystem.IStoreHandler;
import ch.systemsx.cisd.common.filesystem.StoreItem;
import ch.systemsx.cisd.datamover.common.IStoreMover.MoveStatus;

/**
 * An adapter from a {@link IStoreMover} to an {@link IStoreHandler}.
 *
 * @author Bernd Rinn
 */
public class StoreHandlerToMoverAdapter implements IStoreHandler
{

    private final IStoreMover mover;

    public StoreHandlerToMoverAdapter(IStoreMover mover)
    {
        this.mover = mover;
    }

    @Override
    public boolean isStopped()
    {
        return mover.isStopped();
    }

    @Override
    public boolean handle(StoreItem item)
    {
        return mover.move(item) == MoveStatus.MOVE_OK;
    }

}
