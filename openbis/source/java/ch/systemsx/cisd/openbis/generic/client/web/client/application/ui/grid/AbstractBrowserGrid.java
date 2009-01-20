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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.PagingToolBar;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.VoidAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.PagingToolBarWithoutRefresh;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GxtTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SortInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;

/**
 * @author Tomasz Pylak
 */
public abstract class AbstractBrowserGrid<T/* Entity */, M extends ModelData> extends
        LayoutContainer
{
    abstract protected void showEntityViewer(M modelData);

    abstract protected void listEntities(DefaultResultSetConfig<String> resultSetConfig,
            AbstractAsyncCallback<ResultSet<T>> callback);

    abstract protected List<M> createModels(List<T> entities);

    abstract protected void prepareExportEntities(TableExportCriteria<T> exportCriteria,
            AbstractAsyncCallback<String> callback);

    abstract protected ColumnDefsAndConfigs<T> createColumnsDefinition();

    private static final int PAGE_SIZE = 50;

    protected final IViewContext<ICommonClientServiceAsync> viewContext;

    // NOTE: private fields should remain unaccessible to subclasses!

    private final PagingLoader<PagingLoadConfig> pagingLoader;

    private final ContentPanel contentPanel;

    private final Grid<M> grid;

    // available columns configs and definitions
    private ColumnDefsAndConfigs<T> columns;

    // result set key of the last refreshed data
    private String resultSetKey;

    // information about sorting options of the last refreshed data
    private SortInfo sortInfo;

    private IDataRefreshCallback refreshCallback;

    public interface IDataRefreshCallback
    {
        /**
         * called after the grid is refreshed with new data
         * 
         * @param wasSuccessful false if the call ended with a failure
         */
        void postRefresh(boolean wasSuccessful);
    }

    protected AbstractBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            String gridId)
    {
        this.viewContext = viewContext;
        this.pagingLoader = createPagingLoader();

        this.grid = createGrid(pagingLoader, createEntityViewerHandler(), gridId);
        this.contentPanel = createContentPanel();

        setLayout(new FitLayout());
        add(contentPanel);
    }

    private final PagingLoader<PagingLoadConfig> createPagingLoader()
    {
        final RpcProxy<PagingLoadConfig, PagingLoadResult<M>> proxy = createDataLoaderProxy();
        final BasePagingLoader<PagingLoadConfig, PagingLoadResult<M>> newPagingLoader =
                new BasePagingLoader<PagingLoadConfig, PagingLoadResult<M>>(proxy);
        newPagingLoader.setRemoteSort(true);
        return newPagingLoader;
    }

    private final RpcProxy<PagingLoadConfig, PagingLoadResult<M>> createDataLoaderProxy()
    {
        return new RpcProxy<PagingLoadConfig, PagingLoadResult<M>>()
            {
                @Override
                public final void load(final PagingLoadConfig loadConfig,
                        final AsyncCallback<PagingLoadResult<M>> callback)
                {
                    DefaultResultSetConfig<String> resultSetConfig =
                            createPagingConfig(loadConfig, resultSetKey);
                    ListEntitiesCallback listCallback =
                            new ListEntitiesCallback(viewContext, callback, resultSetConfig);
                    listEntities(resultSetConfig, listCallback);
                }
            };
    }

    private static DefaultResultSetConfig<String> createPagingConfig(PagingLoadConfig loadConfig,
            String resultSetKey)
    {
        DefaultResultSetConfig<String> resultSetConfig = new DefaultResultSetConfig<String>();
        resultSetConfig.setLimit(loadConfig.getLimit());
        resultSetConfig.setOffset(loadConfig.getOffset());
        resultSetConfig.setSortInfo(GxtTranslator.translate(loadConfig.getSortInfo()));
        resultSetConfig.setResultSetKey(resultSetKey);
        return resultSetConfig;
    }

    /** @return number of rows in the grid */
    public int getRowNumber()
    {
        return grid.getStore().getCount();
    }

    // @Private
    public final class ListEntitiesCallback extends AbstractAsyncCallback<ResultSet<T>>
    {
        private final AsyncCallback<PagingLoadResult<M>> delegate;

        // configuration with which the listing was called
        private final DefaultResultSetConfig<String> resultSetConfig;

        public ListEntitiesCallback(final IViewContext<?> viewContext,
                final AsyncCallback<PagingLoadResult<M>> delegate,
                final DefaultResultSetConfig<String> resultSetConfig)
        {
            super(viewContext);
            this.delegate = delegate;
            this.resultSetConfig = resultSetConfig;
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        protected final void finishOnFailure(final Throwable caught)
        {
            refreshCallback.postRefresh(false);
            delegate.onFailure(caught);
        }

        @Override
        protected final void process(final ResultSet<T> result)
        {
            // save the key of the result, later we can refer to the result in the cache using this
            // key
            saveCacheKey(result.getResultSetKey());
            saveSortInfo(resultSetConfig.getSortInfo());
            // convert the result to the model data for the grid control
            final List<M> models = createModels(result.getList());
            final PagingLoadResult<M> loadResult =
                    new BasePagingLoadResult<M>(models, resultSetConfig.getOffset(), result
                            .getTotalLength());
            delegate.onSuccess(loadResult);
            // notify that the refresh is done
            refreshCallback.postRefresh(true);
        }
    }

    private Listener<GridEvent> createEntityViewerHandler()
    {
        return new Listener<GridEvent>()
            {
                @SuppressWarnings("unchecked")
                public final void handleEvent(final GridEvent be)
                {
                    ModelData modelData = be.grid.getStore().getAt(be.rowIndex);
                    showEntityViewer((M) modelData);
                }
            };
    }

    // creates a panel with grid and a toolbar for paging
    private ContentPanel createContentPanel()
    {
        ContentPanel panel = createEmptyContentPanel();
        panel.add(grid);
        final PagingToolBar toolBar = new PagingToolBarWithoutRefresh(PAGE_SIZE);
        toolBar.bind(pagingLoader);
        panel.setBottomComponent(toolBar);
        return panel;
    }

    /**
     * Refreshes the grid.
     * <p>
     * Note that, doing so, the result set associated on the server side will be removed.
     * </p>
     */
    protected void refresh(final IDataRefreshCallback newRefreshCallback, String newHeaderOrNull,
            boolean refreshColumnsDefinition)
    {
        disposeCache();
        this.refreshCallback = newRefreshCallback;
        setHeader(newHeaderOrNull);
        if (refreshColumnsDefinition)
        {
            this.columns = createColumnsDefinition();
        }

        ColumnModel columnModel = new ColumnModel(columns.getColumnConfigs());
        grid.reconfigure(grid.getStore(), columnModel);
        GWTUtils.setAutoExpandOnLastVisibleColumn(grid);
        pagingLoader.load(0, PAGE_SIZE);
    }

    private void setHeader(String newHeaderOrNull)
    {
        if (newHeaderOrNull != null)
        {
            this.contentPanel.setHeading(newHeaderOrNull);
        } else
        {
            this.contentPanel.setHeaderVisible(false);
        }
    }

    private List<IColumnDefinition<T>> getSelectedColumns(
            List<IColumnDefinition<T>> availableColumns)
    {
        Map<String, IColumnDefinition<T>> availableColumnsMap = asColumnIdMap(availableColumns);
        final ColumnModel columnModel = grid.getColumnModel();
        return getSelectedColumns(availableColumnsMap, columnModel);
    }

    private void saveSortInfo(SortInfo newSortInfo)
    {
        this.sortInfo = newSortInfo;
    }

    private void saveCacheKey(final String newResultSetKey)
    {
        if (resultSetKey == null)
        {
            resultSetKey = newResultSetKey;
        } else
        {
            assert resultSetKey.equals(newResultSetKey) : "Result set keys not the same.";
        }
    }

    public void disposeCache()
    {
        if (resultSetKey != null)
        {
            viewContext.getService().removeResultSet(resultSetKey,
                    new VoidAsyncCallback<Void>(viewContext));
            resultSetKey = null;
        }
    }

    /** Export always deals with data from the previous refresh operation */
    public final void export()
    {
        export(new ExportEntitiesCallback(viewContext));
    }

    // @Private - for tests
    public final void export(final AbstractAsyncCallback<String> callback)
    {
        assert columns != null : "refresh before exporting!";
        assert resultSetKey != null : "refresh before exporting, resultSetKey is null!";

        final List<IColumnDefinition<T>> columnDefs = getSelectedColumns(columns.getColumnDefs());
        final TableExportCriteria<T> exportCriteria =
                new TableExportCriteria<T>(resultSetKey, sortInfo, columnDefs);

        prepareExportEntities(exportCriteria, callback);
    }

    // ------- generic static helpers

    private final static ContentPanel createEmptyContentPanel()
    {
        final ContentPanel contentPanel = new ContentPanel();
        contentPanel.setBorders(false);
        contentPanel.setBodyBorder(false);
        contentPanel.setLayout(new FitLayout());
        return contentPanel;
    }

    private static final <T extends ModelData> Grid<T> createGrid(
            PagingLoader<PagingLoadConfig> dataLoader, Listener<GridEvent> detailsViewer,
            String gridId)
    {
        ListStore<T> listStore = new ListStore<T>(dataLoader);
        ColumnModel columnModel = new ColumnModel(new ArrayList<ColumnConfig>());
        Grid<T> grid = new Grid<T>(listStore, columnModel);
        grid.setId(gridId);
        grid.setLoadMask(true);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid.addListener(Events.CellDoubleClick, detailsViewer);
        GWTUtils.setAutoExpandOnLastVisibleColumn(grid);
        return grid;
    }

    private static final class ExportEntitiesCallback extends AbstractAsyncCallback<String>
    {
        public ExportEntitiesCallback(final IViewContext<ICommonClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final String exportDataKey)
        {
            final URLMethodWithParameters methodWithParameters =
                    new URLMethodWithParameters(
                            GenericConstants.FILE_EXPORTER_DOWNLOAD_SERVLET_NAME);
            methodWithParameters.addParameter(GenericConstants.EXPORT_CRITERIA_KEY_PARAMETER,
                    exportDataKey);
            WindowUtils.openWindow(methodWithParameters.toString());
        }
    }

    // creates a map to quickly find a definition by its identifier
    private static <T> Map<String, IColumnDefinition<T>> asColumnIdMap(
            final List<IColumnDefinition<T>> defs)
    {
        final Map<String, IColumnDefinition<T>> map = new HashMap<String, IColumnDefinition<T>>();
        for (final IColumnDefinition<T> def : defs)
        {
            map.put(def.getIdentifier(), def);
        }
        return map;
    }

    /**
     * @param availableColumns map of all available columns definitions.
     * @param columnModel describes the visual properties of the columns. Connected with
     *            availableColumns by column id.
     * @return list of columns definitions for those columns which are currently shown
     */
    private static <T/* column definition */> List<T> getSelectedColumns(
            final Map<String, T> availableColumns, final ColumnModel columnModel)
    {
        final List<T> selectedColumnDefs = new ArrayList<T>();
        final int columnCount = columnModel.getColumnCount();
        for (int i = 0; i < columnCount; i++)
        {
            if (columnModel.isHidden(i) == false)
            {
                final String columnId = columnModel.getColumnId(i);
                selectedColumnDefs.add(availableColumns.get(columnId));
            }
        }
        return selectedColumnDefs;
    }

}
