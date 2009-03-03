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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.HashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.Grid;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;

/**
 * Listener for a Grid which delegates click on a cell to a registered {@link ICellListener}.
 *
 * @author Franz-Josef Elmer
 */
public final class ColumnListener<T, M extends BaseEntityModel<T>> implements
        Listener<GridEvent>
{
    private final Map<String, ICellListener<T>> listeners =
            new HashMap<String, ICellListener<T>>();

    private final Grid<M> grid;

    /**
     * Creates an instance for the specified grid.
     */
    public ColumnListener(Grid<M> grid)
    {
        this.grid = grid;
        grid.addListener(Events.CellClick, this);
    }

    /**
     * Registers the specified cell listener for the specified column.
     */
    public void registerCellClickListener(String columnID, ICellListener<T> listener)
    {
        listeners.put(columnID.toLowerCase(), listener);
    }

    /**
     * Invokes {@link ICellListener#handle(Object)} with the base object of the row specified
     * by the row index of the event. The cell listener is determined by the column index. This
     * method does nothing if no cell listener has been registered for column determined by the
     * event.
     */
    @SuppressWarnings("unchecked")
    public void handleEvent(GridEvent be)
    {
        String columnID = grid.getColumnModel().getColumn(be.colIndex).getId().toLowerCase();
        ICellListener<T> listener = listeners.get(columnID);
        if (listener != null)
        {
            ListStore store = be.grid.getStore();
            listener.handle(((BaseEntityModel<T>) store.getAt(be.rowIndex)).getBaseObject());
        }
    }
}