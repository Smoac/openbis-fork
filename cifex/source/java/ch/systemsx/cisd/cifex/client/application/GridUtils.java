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

package ch.systemsx.cisd.cifex.client.application;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.data.SortInfo;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.cifex.client.application.model.AbstractUserGridModel;
import ch.systemsx.cisd.cifex.client.application.model.UserGridModel;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * Helper to create a paged grid with filtering option.
 * 
 * @author Tomasz Pylak
 */
public class GridUtils
{
    private static final int PAGE_SIZE = 50;

    public static <M extends ModelData> void reloadStore(GridWidget<M> gridWidget, List<M> models)
    {
        Grid<M> grid = gridWidget.getGrid();
        SortInfo sortState = grid.getStore().getSortState();
        PagingLoader<PagingLoadResult<M>> loader = createLoader(models);
        ListStore<M> store = createListStore(loader);
        setSortState(store, sortState);

        bindFilters(gridWidget.getFilterFields(), store);
        grid.reconfigure(store, grid.getColumnModel());
        bindAndLoad(gridWidget.getToolbar(), loader);
    }

    private static void setSortState(ListStore<?> store, SortInfo sortState)
    {
        store.sort(sortState.getSortField(), sortState.getSortDir());
    }

    /** creates a grid with specified users */
    public static GridWidget<UserGridModel> createUserGrid(final List<UserInfoDTO> users,
            ViewContext viewContext)
    {
        List<UserGridModel> data = UserGridModel.convert(viewContext, users);
        IMessageResources messageResources = viewContext.getMessageResources();
        List<ColumnConfig> columnConfigs = UserGridModel.getColumnConfigs(messageResources);
        List<StoreFilterField<UserGridModel>> filterItems =
                AbstractUserGridModel.createFilterItems(messageResources);
        GridWidget<UserGridModel> gridWidget =
                GridUtils.createGrid(columnConfigs, data, filterItems, messageResources);
        return gridWidget;
    }

    /** creates a paged grid with specified columns and filters */
    public static <M extends ModelData> GridWidget<M> createGrid(List<ColumnConfig> columnConfigs,
            List<M> data, List<StoreFilterField<M>> filterFields, IMessageResources messageResources)
    {
        PagingLoader<PagingLoadResult<M>> loader = createLoader(data);
        ListStore<M> store = createListStore(loader);
        Grid<M> grid = createGrid(columnConfigs, store);
        return addToolbar(grid, loader, filterFields, messageResources);
    }

    private static <M extends ModelData> Grid<M> createGrid(List<ColumnConfig> columnConfigs,
            ListStore<M> store)
    {
        Grid<M> grid = new Grid<M>(store, new ColumnModel(columnConfigs));
        grid.setHeight(Constants.GRID_HEIGHT);
        return grid;
    }

    private static <M extends ModelData> ListStore<M> createListStore(
            PagingLoader<PagingLoadResult<M>> loader)
    {
        ListStore<M> store = new ListStore<M>(loader)
            {
                @Override
                /*
                 * Overriding this methods allows us to redefine the filter from "begins with" to
                 * "contains". See {@link ContainFilterField}.
                 */
                public void filter(String property, String beginsWith)
                {
                    super.filter(property);
                }
            };
        return store;
    }

    private static <M extends ModelData> PagingLoader<PagingLoadResult<M>> createLoader(List<M> data)
    {
        PagingModelMemoryProxy proxy = new PagingModelMemoryProxy(data);
        PagingLoader<PagingLoadResult<M>> loader = new BasePagingLoader<PagingLoadResult<M>>(proxy);
        loader.setRemoteSort(false);
        return loader;
    }

    private static Widget createGridWithToolbar(Grid<?> grid, PagingToolBar toolbar)
    {
        final ContentPanel container = new ContentPanel();
        container.setLayout(new RowLayout());
        container.add(grid, new RowData(1, 1));
        container.add(toolbar);
        container.setBottomComponent(toolbar);
        return container;
    }

    private static <M extends ModelData> PagingToolBar createGridPagingToolbar(
            PagingLoader<PagingLoadResult<M>> loader, List<Component> filterItems)
    {
        final PagingToolBar toolBar = new PagingToolBarWithItems(PAGE_SIZE, filterItems);
        bindAndLoad(toolBar, loader);
        return toolBar;
    }

    private static <M extends ModelData> void bindAndLoad(final PagingToolBar toolBar,
            PagingLoader<PagingLoadResult<M>> loader)
    {
        toolBar.bind(loader);
        loader.load(0, PAGE_SIZE);
    }

    private static class PagingToolBarWithItems extends PagingToolBar
    {
        // Allows to add additional items directly before the refresh button.
        // The refresh button is removed.
        public PagingToolBarWithItems(int pageSize, List<Component> additionalItems)
        {
            super(pageSize);
            List<Component> items = getItems();
            int startIx = items.indexOf(refresh);
            items.remove(startIx);
            for (Component item : additionalItems)
            {
                items.add(startIx++, item);
            }
        }
    }

    private static <M extends ModelData> GridWidget<M> addToolbar(Grid<M> grid,
            PagingLoader<PagingLoadResult<M>> loader, List<StoreFilterField<M>> filterFields,
            IMessageResources messageResources)
    {
        bindFilters(filterFields, grid.getStore());
        List<Component> filterItems = createFilterItems(filterFields, messageResources);
        final PagingToolBar toolBar = createGridPagingToolbar(loader, filterItems);
        Widget widget = createGridWithToolbar(grid, toolBar);
        return new GridWidget<M>(widget, grid, toolBar, filterFields);
    }

    private static <M extends ModelData> List<Component> createFilterItems(
            List<StoreFilterField<M>> filterFields, IMessageResources messageResources)
    {
        List<Component> filterItems = new ArrayList<Component>();
        filterItems.add(new LabelField(messageResources.getGridFiltersLabel()));
        for (StoreFilterField<M> filterField : filterFields)
        {
            filterItems.add(filterField);
        }
        return filterItems;
    }

    private static <M extends ModelData> void bindFilters(List<StoreFilterField<M>> filterFields,
            ListStore<M> store)
    {
        for (StoreFilterField<M> filterField : filterFields)
        {
            filterField.bind(store);
        }
    }

    public static class GridWidget<M extends ModelData>
    {
        private final Widget widget;

        private final Grid<M> grid;

        private final PagingToolBar toolBar;

        private final List<StoreFilterField<M>> filterFields;

        public GridWidget(Widget gridWithToolbar, Grid<M> grid, PagingToolBar toolBar,
                List<StoreFilterField<M>> filterFields)
        {
            this.widget = gridWithToolbar;
            this.grid = grid;
            this.toolBar = toolBar;
            this.filterFields = filterFields;
        }

        /** widget containing the grid with the paging and filtering toolbar at the bottom */
        public Widget getWidget()
        {
            return widget;
        }

        /** The internal grid control */
        public Grid<M> getGrid()
        {
            return grid;
        }

        public PagingToolBar getToolbar()
        {
            return toolBar;
        }

        public List<StoreFilterField<M>> getFilterFields()
        {
            return filterFields;
        }
    }
}
