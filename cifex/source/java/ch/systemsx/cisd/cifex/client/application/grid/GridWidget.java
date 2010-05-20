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

package ch.systemsx.cisd.cifex.client.application.grid;

import java.util.ArrayList;
import java.util.Iterator;
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
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;

import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.model.IModelDataWithID;
import ch.systemsx.cisd.cifex.client.application.utils.IDelegatedAction;
import ch.systemsx.cisd.cifex.shared.basic.Constants;

/**
 * A grid with paging, filtering option. All rows are stored at the client side.
 * 
 * @author Tomasz Pylak
 */
public class GridWidget<M extends IModelDataWithID>
{
    private static final int PAGE_SIZE = 50;

    /** creates a paged grid with specified columns and filters */
    public static <M extends IModelDataWithID> GridWidget<M> create(
            List<ColumnConfig> columnConfigs, List<M> models,
            List<AbstractFilterField<M>> filterFields, IMessageResources messageResources)
    {
        return new GridWidget<M>(columnConfigs, models, filterFields, messageResources);
    }

    // --------------------------------------------

    private final Grid<M> grid;

    private final PagingToolBar toolBar;

    // filters which should be applied to the models before they will be displayed
    private final List<AbstractFilterField<M>> filterFields;

    // --- grid state

    // all displayed rows on all pages before filtering
    private List<M> models;

    // The screen widget -- lazily created in the getter.
    private ContentPanel widget = null;

    private GridWidget(List<ColumnConfig> columnConfigs, List<M> models,
            List<AbstractFilterField<M>> filterFields, IMessageResources messageResources)
    {
        this.models = models;
        this.filterFields = filterFields;
        this.grid = createGrid(columnConfigs);
        this.toolBar = createFilterAndPagingToolbar(messageResources);

        refreshStore();
    }

    private PagingToolBar createFilterAndPagingToolbar(IMessageResources messageResources)
    {
        IDelegatedAction onFilterAction = new IDelegatedAction()
            {
                public void execute()
                {
                    refreshStore();
                }
            };
        return createFilterAndPagingToolbar(filterFields, onFilterAction, new FilterMenu(
                filterFields, messageResources.getGridFiltersLabel(), grid.getColumnModel()),
                new ColumnMenu(messageResources.getGridColumnsLabel(), grid.getColumnModel()));
    }

    public void setDataAndRefresh(List<M> models)
    {
        this.models = models;
        refreshStore();
    }

    public boolean removeItem(long id)
    {
        boolean removed = false;
        final Iterator<M> it = models.iterator();
        while (it.hasNext())
        {
            final M item = it.next();
            if (item.getID() == id)
            {
                it.remove();
                removed = true;
                break;
            }
        }
        if (removed)
        {
            refreshStore();
        }
        return removed;
    }

    public M tryGetModel(long id)
    {
        final Iterator<M> it = models.iterator();
        while (it.hasNext())
        {
            final M item = it.next();
            if (item.getID() == id)
            {
                return item;
            }
        }
        return null; // id not found
    }

    // applies filters and refreshes the grid items
    private void refreshStore()
    {
        SortInfo sortState = grid.getStore().getSortState();

        List<M> filteredModel = applyFilters(models, filterFields);
        PagingLoader<PagingLoadResult<M>> loader = createLoader(filteredModel);
        ListStore<M> store = createListStore(loader);

        store.sort(sortState.getSortField(), sortState.getSortDir());

        grid.reconfigure(store, grid.getColumnModel());
        bindAndLoad(toolBar, loader);
    }

    /** widget containing the grid with the paging and filtering toolbar at the bottom */
    public ContentPanel getWidget()
    {
        if (widget == null)
        {
            widget = new ContentPanel();
            widget.setHeaderVisible(false);
            widget.setLayout(new RowLayout());
            widget.add(grid);
            widget.add(toolBar);
        }
        return widget;
    }

    /** The internal grid control */
    public Grid<M> getGrid()
    {
        return grid;
    }

    // ---- static helpers ---------------------

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
        // we sort locally, but the whole store is sorted, not one page
        loader.setRemoteSort(true);
        return loader;
    }

    private static <M extends ModelData> PagingToolBar createFilterAndPagingToolbar(
            List<AbstractFilterField<M>> filterFields, IDelegatedAction onFilterAction,
            FilterMenu filterMenu, ColumnMenu columnMenu)
    {
        List<Component> filterItems =
                createFilterItems(filterFields, onFilterAction, filterMenu, columnMenu);
        return new PagingToolBarWithItems(PAGE_SIZE, filterItems);
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

    private static <M extends ModelData> List<Component> createFilterItems(
            List<AbstractFilterField<M>> filterFields, IDelegatedAction onFilterAction,
            FilterMenu filterMenu, ColumnMenu columnMenu)
    {
        List<Component> filterItems = new ArrayList<Component>();
        filterItems.add(columnMenu);
        filterItems.add(filterMenu);
        for (AbstractFilterField<M> filterField : filterFields)
        {
            filterItems.add(filterField);
            filterField.bind(onFilterAction);
        }
        return filterItems;
    }

    private static <M extends ModelData> Grid<M> createGrid(List<ColumnConfig> columnConfigs)
    {
        Grid<M> grid = new Grid<M>(new ListStore<M>(), new ColumnModel(columnConfigs));
        grid.setHeight(Constants.GRID_HEIGHT);
        grid.setAutoWidth(true);
        grid.setView(new ExtendedGridView());
        return grid;
    }

    private static <M extends ModelData> List<M> applyFilters(List<M> models,
            List<AbstractFilterField<M>> filterFields)
    {
        List<M> filteredModels = new ArrayList<M>();
        for (M model : models)
        {
            if (isMatching(model, filterFields))
            {
                filteredModels.add(model);
            }
        }
        return filteredModels;
    }

    // applies all filters joined by 'and' operator to the model
    private static <M extends ModelData> boolean isMatching(M model,
            List<AbstractFilterField<M>> filterFields)
    {
        for (AbstractFilterField<M> filter : filterFields)
        {
            if (filter.isMatching(model) == false)
            {
                return false;
            }
        }
        return true;
    }

}