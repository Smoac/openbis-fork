/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractBrowserGrid;

/**
 * Abstract {@link IDataRefreshCallback} implementation for refreshing {@link AbstractBrowserGrid}.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractGridDataRefreshCallback<T/* Entity */> implements
        IDataRefreshCallback
{
    private AbstractBrowserGrid<T, BaseEntityModel<T>> grid;

    public AbstractBrowserGrid<T, BaseEntityModel<T>> getGrid()
    {
        assert grid != null : "grid not set!";
        return grid;
    }

    public void setGrid(AbstractBrowserGrid<T, BaseEntityModel<T>> grid)
    {
        this.grid = grid;
    }
}
