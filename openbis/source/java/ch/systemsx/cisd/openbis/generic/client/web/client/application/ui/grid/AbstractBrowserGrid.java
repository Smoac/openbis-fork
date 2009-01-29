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
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.VoidAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.BrowserGridPagingToolBar.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SortInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SortInfo.SortDir;

/**
 * @author Tomasz Pylak
 */
public abstract class AbstractBrowserGrid<T/* Entity */, M extends ModelData> extends
        LayoutContainer
{
    /** Shows the detail view for the specified entity */
    abstract protected void showEntityViewer(M modelData);

    abstract protected void listEntities(DefaultResultSetConfig<String, T> resultSetConfig,
            AbstractAsyncCallback<ResultSet<T>> callback);

    /** Converts specified list of entities into a list of grid rows models */
    abstract protected List<M> createModels(List<T> entities);

    /**
     * Called when user wants to export the data. It can happen only after a previous refresh of the
     * data has taken place. The export criteria has only the cache key
     */
    abstract protected void prepareExportEntities(TableExportCriteria<T> exportCriteria,
            AbstractAsyncCallback<String> callback);

    /**
     * @return definition of all the columns in the grid: visible properties and the mechanism of
     *         creating each column's value from the row model
     */
    abstract protected ColumnDefsAndConfigs<T> createColumnsDefinition();

    /**
     * Called when the user wants to refresh the data. Should be implemented by calling
     * {@link #refresh(IDataRefreshCallback, String, boolean) at some point. }
     */
    abstract protected void refresh();

    /** @return should the refresh button be enabled? */
    abstract protected boolean isRefreshEnabled();

    protected final IViewContext<ICommonClientServiceAsync> viewContext;

    // ------ private section. NOTE: it should remain unaccessible to subclasses! ---------------

    private static final int PAGE_SIZE = 50;

    private final PagingLoader<PagingLoadConfig> pagingLoader;

    private final ContentPanel contentPanel;

    private final Grid<M> grid;

    // the toolbar has the refresh and export buttons besides the paging controls
    private final BrowserGridPagingToolBar pagingToolbar;

    // should the data be automatically loaded when the grid is rendered for the first time?
    private final boolean refreshAutomatically;

    // available columns configs and definitions
    private ColumnDefsAndConfigs<T> columns;

    // result set key of the last refreshed data
    private String resultSetKey;

    // information about sorting options of the last refreshed data
    private SortInfo<T> sortInfo;

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
        this(viewContext, gridId, true, false);
    }

    protected AbstractBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            String gridId, boolean showHeader, boolean refreshAutomatically)
    {
        this.viewContext = viewContext;
        this.refreshAutomatically = refreshAutomatically;
        this.pagingLoader = createPagingLoader();

        this.grid = createGrid(pagingLoader, createEntityViewerHandler(), gridId);
        this.pagingToolbar =
                new BrowserGridPagingToolBar(asActionInvoker(), viewContext, PAGE_SIZE);
        pagingToolbar.bind(pagingLoader);

        this.contentPanel = createEmptyContentPanel();
        contentPanel.add(grid);
        contentPanel.setBottomComponent(pagingToolbar);
        contentPanel.setHeaderVisible(showHeader);

        setLayout(new FitLayout());
        add(contentPanel);
    }

    /** @return this grid as a disposable component with a specified toolbar at the top. */
    protected final DisposableComponent asDisposableWithToolbar(final Component toolbar)
    {
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());
        container.add(toolbar);
        container.add(this, new RowData(1, 1));

        return asDisposableComponent(container);
    }

    /** @return this grid as a disposable component */
    protected final DisposableComponent asDisposableWithoutToolbar()
    {
        return asDisposableComponent(this);
    }

    private DisposableComponent asDisposableComponent(final Component component)
    {
        return new DisposableComponent()
            {
                public void dispose()
                {
                    disposeCache();
                }

                public Component getComponent()
                {
                    return component;
                }
            };
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        if (refreshAutomatically)
        {
            layout();
            refresh();
        }
    }

    private PagingLoader<PagingLoadConfig> createPagingLoader()
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
                    DefaultResultSetConfig<String, T> resultSetConfig =
                            createPagingConfig(loadConfig, columns.getColumnDefs(), resultSetKey);
                    ListEntitiesCallback listCallback =
                            new ListEntitiesCallback(viewContext, callback, resultSetConfig);
                    listEntities(resultSetConfig, listCallback);
                }
            };
    }

    private static <T> DefaultResultSetConfig<String, T> createPagingConfig(
            PagingLoadConfig loadConfig, List<IColumnDefinition<T>> availableColumns,
            String resultSetKey)
    {
        DefaultResultSetConfig<String, T> resultSetConfig = new DefaultResultSetConfig<String, T>();
        resultSetConfig.setLimit(loadConfig.getLimit());
        resultSetConfig.setOffset(loadConfig.getOffset());
        SortInfo<T> sortInfo = translateSortInfo(loadConfig, availableColumns);
        resultSetConfig.setSortInfo(sortInfo);
        resultSetConfig.setResultSetKey(resultSetKey);
        return resultSetConfig;
    }

    private static <T> SortInfo<T> translateSortInfo(PagingLoadConfig loadConfig,
            List<IColumnDefinition<T>> availableColumns)
    {
        com.extjs.gxt.ui.client.data.SortInfo origSortInfo = loadConfig.getSortInfo();
        String origSortField = origSortInfo.getSortField();
        IColumnDefinition<T> sortColumnDefinition = null;
        if (origSortField != null)
        {
            Map<String, IColumnDefinition<T>> availableColumnsMap = asColumnIdMap(availableColumns);
            sortColumnDefinition = availableColumnsMap.get(origSortField);
            assert sortColumnDefinition != null : "sortColumnDefinition is null";
        }
        SortInfo<T> sortInfo = new SortInfo<T>();
        sortInfo.setSortField(sortColumnDefinition);
        sortInfo.setSortDir(translate(origSortInfo.getSortDir()));
        return sortInfo;
    }

    private static SortDir translate(com.extjs.gxt.ui.client.Style.SortDir sortDir)
    {
        if (sortDir.equals(com.extjs.gxt.ui.client.Style.SortDir.ASC))
        {
            return SortDir.ASC;
        } else if (sortDir.equals(com.extjs.gxt.ui.client.Style.SortDir.DESC))
        {
            return SortDir.DESC;
        } else if (sortDir.equals(com.extjs.gxt.ui.client.Style.SortDir.NONE))
        {
            return SortDir.NONE;
        } else
        {
            throw new IllegalStateException("unknown sort dir: " + sortDir);
        }
    }

    /** @return number of rows in the grid */
    public final int getRowNumber()
    {
        return grid.getStore().getCount();
    }

    // @Private
    public final class ListEntitiesCallback extends AbstractAsyncCallback<ResultSet<T>>
    {
        private final AsyncCallback<PagingLoadResult<M>> delegate;

        // configuration with which the listing was called
        private final DefaultResultSetConfig<String, T> resultSetConfig;

        public ListEntitiesCallback(final IViewContext<?> viewContext,
                final AsyncCallback<PagingLoadResult<M>> delegate,
                final DefaultResultSetConfig<String, T> resultSetConfig)
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
            onComplete(false);
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
            onComplete(true);
        }

        // notify that the refresh is done
        private void onComplete(boolean wasSuccessful)
        {
            refreshCallback.postRefresh(wasSuccessful);
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

    // wraps this browser into the interface appropriate for the toolbar. If this class would just
    // implement the interface it could be very confusing for the code reader.
    private final IBrowserGridActionInvoker asActionInvoker()
    {
        final AbstractBrowserGrid<T, M> delegate = this;
        return new IBrowserGridActionInvoker()
            {
                public void export()
                {
                    delegate.export();
                }

                public void refresh()
                {
                    delegate.refresh();
                }
            };
    }

    protected final SelectionChangedListener<?> addRefreshButton(ToolBar container)
    {
        String title = viewContext.getMessage(Dict.BUTTON_SHOW);
        Button showButton = BrowserGridPagingToolBar.createRefreshButton(title, asActionInvoker());
        container.add(new AdapterToolItem(showButton));
        return createRefreshButtonsListener(showButton);
    }

    private <D extends ModelData> SelectionChangedListener<D> createRefreshButtonsListener(
            final Button showButton)
    {
        return new SelectionChangedListener<D>()
            {
                @Override
                public void selectionChanged(SelectionChangedEvent<D> se)
                {
                    updateDefaultRefreshButton();

                    boolean isEnabled = isRefreshEnabled();
                    // Refreshes the state of the specified refresh button.
                    BrowserGridPagingToolBar
                            .updateRefreshButton(showButton, isEnabled, viewContext);
                }
            };
    }

    protected final void updateDefaultRefreshButton()
    {
        boolean isEnabled = isRefreshEnabled();
        pagingToolbar.updateDefaultRefreshButton(isEnabled);
    }

    /**
     * Refreshes the grid.
     * <p>
     * Note that, doing so, the result set associated on the server side will be removed.
     * </p>
     */
    protected final void refresh(String headerOrNull, boolean refreshColumnsDefinition)
    {
        refresh(null, headerOrNull, refreshColumnsDefinition);
    }

    /**
     * @externalRefreshCallbackOrNull external class can define it's own refresh callback method. It
     *                                will be merged with the internal one.
     */
    protected final void refresh(final IDataRefreshCallback externalRefreshCallbackOrNull,
            String headerOrNull, boolean refreshColumnsDefinition)
    {
        pagingToolbar.updateDefaultRefreshButton(false);
        disposeCache();
        this.refreshCallback = createRefreshCallback(externalRefreshCallbackOrNull);
        setHeader(headerOrNull);
        if (columns == null || refreshColumnsDefinition)
        {
            this.columns = createColumnsDefinition();
        }

        ColumnModel columnModel = new ColumnModel(columns.getColumnConfigs());
        grid.reconfigure(grid.getStore(), columnModel);
        GWTUtils.setAutoExpandOnLastVisibleColumn(grid);
        pagingLoader.load(0, PAGE_SIZE);
    }

    private IDataRefreshCallback createRefreshCallback(
            IDataRefreshCallback externalRefreshCallbackOrNull)
    {
        IDataRefreshCallback internalCallback = createInternalPostRefreshCallback();
        if (externalRefreshCallbackOrNull == null)
        {
            return internalCallback;
        } else
        {
            return mergeCallbacks(internalCallback, externalRefreshCallbackOrNull);
        }
    }

    private IDataRefreshCallback createInternalPostRefreshCallback()
    {
        return new IDataRefreshCallback()
            {
                public void postRefresh(boolean wasSuccessful)
                {
                    pagingToolbar.updateDefaultRefreshButton(true);
                    if (wasSuccessful)
                    {
                        pagingToolbar.enableExportButton();
                    }
                }
            };
    }

    private static IDataRefreshCallback mergeCallbacks(final IDataRefreshCallback c1,
            final IDataRefreshCallback c2)
    {
        return new IDataRefreshCallback()
            {
                public void postRefresh(boolean wasSuccessful)
                {
                    c1.postRefresh(wasSuccessful);
                    c2.postRefresh(wasSuccessful);
                }
            };
    }

    private void setHeader(String headerOrNull)
    {
        if (headerOrNull != null)
        {
            assert this.contentPanel.isHeaderVisible() : "header was switched off";
            this.contentPanel.setHeading(headerOrNull);
        }
    }

    private List<IColumnDefinition<T>> getSelectedColumns(
            List<IColumnDefinition<T>> availableColumns)
    {
        Map<String, IColumnDefinition<T>> availableColumnsMap = asColumnIdMap(availableColumns);
        final ColumnModel columnModel = grid.getColumnModel();
        return getSelectedColumns(availableColumnsMap, columnModel);
    }

    private void saveSortInfo(SortInfo<T> newSortInfo)
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

    private void disposeCache()
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
