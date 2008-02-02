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

package ch.systemsx.cisd.cifex.client.application.ui;

import com.gwtext.client.core.Ext;
import com.gwtext.client.data.ArrayReader;
import com.gwtext.client.data.DataProxy;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.MemoryProxy;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.Store;
import com.gwtext.client.widgets.grid.ColumnConfig;
import com.gwtext.client.widgets.grid.ColumnModel;
import com.gwtext.client.widgets.grid.Grid;
import com.gwtext.client.widgets.grid.GridConfig;

import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.model.IDataGridModel;

/**
 * A <code>Grid</code> extension based on a {@link IDataGridModel}.
 * 
 * @author Christian Ribeaud
 */
public final class ModelBasedGrid extends Grid
{
    private final static Object[][] OBJECT_ARRAY_ARRAY = new Object[0][];

    private IDataGridModel model;

    public ModelBasedGrid(final IMessageResources messageResources, final Object[] objects, final IDataGridModel model,
            final String height)
    {
        super(Ext.generateId(), null, height, createStore(objects, model), createColumnModel(messageResources, model),
                createGridConfig());
        this.model = model;
        // To turn off the row selection
        getSelectionModel().lock();
        render();
    }

    private final static DataProxy createDataProxy(final Object[] objects, final IDataGridModel newModel)
    {
        return new MemoryProxy((Object[][]) newModel.getData(objects).toArray(OBJECT_ARRAY_ARRAY));
    }

    private final static Store createStore(final Object[] objects, final IDataGridModel model)
    {
        final DataProxy memoryProxy = createDataProxy(objects, model);
        final RecordDef recordDef = new RecordDef((FieldDef[]) model.getFieldDefs().toArray(new FieldDef[0]));
        final Store store = new Store(memoryProxy, new ArrayReader(recordDef));
        store.load();
        return store;
    }

    private final static ColumnModel createColumnModel(final IMessageResources messageResources,
            final IDataGridModel model)
    {
        return new ColumnModel((ColumnConfig[]) model.getColumnConfigs().toArray(new ColumnConfig[0]));
    }

    private final static GridConfig createGridConfig()
    {
        final GridConfig gridConfig = new GridConfig();
        // True to set the grid's width to the default total width of the grid's columns instead of a fixed width.
        // gridConfig.setAutoWidth(true);
        gridConfig.setAutoHeight(true);
        gridConfig.setMaxRowsToMeasure(5);
        gridConfig.setAutoSizeColumns(true);
        gridConfig.setAutoSizeHeaders(true);
        gridConfig.setTrackMouseOver(false);
        gridConfig.setEnableColumnMove(false);
        return gridConfig;
    }

    /** Reloads the store with given new <var>objects</var> and given <var>newModel</var>. */
    public final void reloadStore(final Object[] objects, final IDataGridModel newModel)
    {
        final DataProxy dataProxy = createDataProxy(objects, newModel);
        final Store store = getStore();
        store.setProxy(dataProxy);
        store.reload();
        this.model = newModel;
        render();
    }

    /** Returns the current <code>IDataGridModel</code>. */
    public final IDataGridModel getModel()
    {
        return model;
    }
}