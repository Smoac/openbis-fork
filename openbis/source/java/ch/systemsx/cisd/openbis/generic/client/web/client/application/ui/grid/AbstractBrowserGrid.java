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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ColumnModelEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.PagingToolBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ShowRelatedDatasetsDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.VoidAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppEvents;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplaySettingsGetter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager.GridDisplaySettings;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager.Modification;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.InternalLinkCellRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.MultilineStringCellRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.BorderLayoutDataFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.GridCustomColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.filter.FilterToolbar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridCustomColumnInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RelatedDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridFilterInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo.SortDir;

/**
 * @author Tomasz Pylak
 */
public abstract class AbstractBrowserGrid<T/* Entity */, M extends BaseEntityModel<T>> extends
        LayoutContainer implements IDatabaseModificationObserver, IDisplayTypeIDProvider,
        IColumnDefinitionProvider<T>
{

    /**
     * Shows the detail view for the specified entity
     */
    abstract protected void showEntityViewer(T entity, boolean editMode);

    abstract protected void listEntities(DefaultResultSetConfig<String, T> resultSetConfig,
            AbstractAsyncCallback<ResultSet<T>> callback);

    /** Converts specified entity into a grid row model */
    abstract protected M createModel(GridRowModel<T> entity);

    /**
     * Called when user wants to export the data. It can happen only after a previous refresh of the
     * data has taken place. The export criteria has only the cache key
     */
    abstract protected void prepareExportEntities(TableExportCriteria<T> exportCriteria,
            AbstractAsyncCallback<String> callback);

    /**
     * Creates a column model with all available columns from scratch without taking user settings
     * into account.
     * 
     * @return definition of all the columns in the grid
     */
    abstract protected ColumnDefsAndConfigs<T> createColumnsDefinition();

    /**
     * Called when the user wants to refresh the data. Should be implemented by calling
     * {@link #refresh(IDataRefreshCallback, String, boolean) at some point. }
     */
    abstract protected void refresh();

    /** @return should the refresh button be enabled? */
    abstract protected boolean isRefreshEnabled();

    /** @return on which fields filters should be switched on by default? */
    abstract protected List<IColumnDefinition<T>> getInitialFilters();

    /**
     * To be subclassed if columns in the grid depend on the internal grid configuration.
     * 
     * @return id at which grid display settings are saved.
     */
    public String getGridDisplayTypeID()
    {
        return createGridDisplayTypeID(null);
    }

    // --------

    protected final IViewContext<ICommonClientServiceAsync> viewContext;

    // the toolbar has the refresh and export buttons besides the paging controls
    protected final BrowserGridPagingToolBar pagingToolbar;

    // ------ private section. NOTE: it should remain unaccessible to subclasses! ---------------

    private static final int PAGE_SIZE = 50;

    // set to true to see some useful debugging messages
    private static final boolean DEBUG = false;

    private final PagingLoader<PagingLoadConfig> pagingLoader;

    private final ContentPanel contentPanel;

    private final Grid<M> grid;

    private final ColumnListener<T, M> columnListener;

    private final boolean refreshAutomatically;

    // used to change displayed filter widgets
    private final FilterToolbar<T> filterToolbar;

    private final IDisplayTypeIDGenerator displayTypeIDGenerator;

    // --------- private non-final fields

    private List<PagingColumnFilter<T>> filterWidgets = new ArrayList<PagingColumnFilter<T>>();

    // available columns definitions
    private Set<IColumnDefinition<T>> columnDefinitions;

    private CustomColumnsMetadataProvider customColumnsMetadataProvider;

    // result set key of the last refreshed data
    private String resultSetKey;

    /** the number of all objects cached in the browser */
    private int totalCount;

    private IDataRefreshCallback refreshCallback;

    protected AbstractBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            String gridId, IDisplayTypeIDGenerator displayTypeIDGenerator)
    {
        this(viewContext, gridId, true, false, displayTypeIDGenerator);
    }

    /**
     * @param showHeader decides if the header bar on top of the grid should be displayed. If false,
     *            then an attempt to set a non-null header (e.g. in refresh method) is treated as an
     *            error.
     * @param refreshAutomatically should the data be automatically loaded when the grid is rendered
     *            for the first time?
     * @param gridId unique id of the grid which will can used by testframework to identify the grid
     *            and identify the callback which fills it in.
     */
    protected AbstractBrowserGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            String gridId, boolean showHeader, boolean refreshAutomatically,
            IDisplayTypeIDGenerator displayTypeIDGenerator)
    {
        this.displayTypeIDGenerator = displayTypeIDGenerator;
        this.viewContext = viewContext;
        this.refreshAutomatically = refreshAutomatically;
        this.pagingLoader = createPagingLoader();
        this.customColumnsMetadataProvider = new CustomColumnsMetadataProvider();

        this.grid = createGrid(pagingLoader, gridId);
        this.pagingToolbar =
                new BrowserGridPagingToolBar(asActionInvoker(), viewContext, PAGE_SIZE, gridId);
        pagingToolbar.bind(pagingLoader);
        this.filterToolbar =
                new FilterToolbar<T>(viewContext, gridId, this, filterWidgets,
                        createApplyFiltersDelagator());
        final LayoutContainer bottomToolbars = createBottomToolbars(filterToolbar, pagingToolbar);
        this.contentPanel = createEmptyContentPanel();
        contentPanel.add(grid);
        contentPanel.setBottomComponent(bottomToolbars);
        contentPanel.setHeaderVisible(showHeader);
        contentPanel.setAutoWidth(true);
        columnListener = new ColumnListener<T, M>(grid);
        registerLinkClickListenerFor(Dict.CODE, new ICellListener<T>()
            {
                public void handle(T rowItem)
                {
                    showEntityViewer(rowItem, false);
                }
            });

        setLayout(new FitLayout());
        add(contentPanel);

        addRefreshDisplaySettingsListener();
    }

    private void addRefreshDisplaySettingsListener()
    {
        addListener(Events.AfterLayout, new Listener<BaseEvent>()
            {
                private Long lastRefreshCheckTime;

                public void handleEvent(BaseEvent be)
                {
                    if (lastRefreshCheckTime == null)
                    {
                        // No need to refresh when grid is displayed for the first time.
                    } else if (isModificationDoneInAnotherViewSinceLastRefresh())
                    {
                        // Grid settings have been modified in another view of the same type.
                        // Refresh this browser settings.
                        refreshColumnsAndFiltersWithCurrentModel();
                    }
                    lastRefreshCheckTime = System.currentTimeMillis();
                }

                private boolean isModificationDoneInAnotherViewSinceLastRefresh()
                {
                    final Modification lastModificationOrNull =
                            viewContext.getDisplaySettingsManager()
                                    .tryGetLastColumnSettingsModification(getGridDisplayTypeID());
                    return lastModificationOrNull != null
                            && lastModificationOrNull.getModifier()
                                    .equals(AbstractBrowserGrid.this) == false
                            && lastModificationOrNull.getTime() > lastRefreshCheckTime;
                }

            });
    }

    /** Refreshes the grid without showing the loading progress bar */
    protected final void refreshGridSilently()
    {
        grid.setLoadMask(false);
        refresh();
        grid.setLoadMask(true);
    }

    /**
     * Shows/hides the load mask immediately.
     * 
     * @param loadMask Load mask is shown if <code>true</code> otherwise it is hidden.
     */
    public void setLoadMaskImmediately(boolean loadMask)
    {
        if (loadMask)
        {
            grid.el().mask(GXT.MESSAGES.loadMask_msg());
        } else
        {
            grid.el().unmask();
        }

    }

    /**
     * Registers the specified listener for clicks on links in the specified column.
     * 
     * @param columnID Column ID. Not case sensitive.
     * @param listener Listener handle single clicks.
     */
    protected final void registerLinkClickListenerFor(final String columnID,
            final ICellListener<T> listener)
    {
        columnListener.registerLinkClickListener(columnID, listener);
    }

    /**
     * Allows multiple selection instead of single selection.
     */
    protected final void allowMultipleSelection()
    {
        grid.getSelectionModel().setSelectionMode(SelectionMode.MULTI);
    }

    protected final List<M> getGridModels()
    {
        return grid.getStore().getModels();
    }

    private List<PagingColumnFilter<T>> createFilterWidgets(
            List<IColumnDefinition<T>> filteredColumns)
    {
        return createFilterWidgets(filteredColumns, createApplyFiltersDelagator());
    }

    private IDelegatedAction createApplyFiltersDelagator()
    {
        return new IDelegatedAction()
            {
                public void execute()
                {
                    if (isHardRefreshNeeded() == false)
                    {
                        // TODO 2009-09-16, Piotr Buczek: reload of data is not needed in some cases
                        // - when filters with no text are removed,
                        // - when new filters are created,
                        // but when filter with text is removed it is needed.
                        reloadData();
                    }
                }
            };
    }

    private static <T> List<PagingColumnFilter<T>> createFilterWidgets(
            List<IColumnDefinition<T>> availableFilters, IDelegatedAction onFilterAction)
    {
        List<PagingColumnFilter<T>> filterWidgets = new ArrayList<PagingColumnFilter<T>>();
        for (IColumnDefinition<T> columnDefinition : availableFilters)
        {
            PagingColumnFilter<T> filterWidget =
                    new PagingColumnFilter<T>(columnDefinition, onFilterAction);
            filterWidgets.add(filterWidget);
        }
        return filterWidgets;
    }

    /** @return this grid as a disposable component with a specified toolbar at the top. */
    protected final DisposableEntityChooser<T> asDisposableWithToolbar(final Component toolbar)
    {
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());
        container.add(toolbar);
        container.add(this, new RowData(1, 1));

        return asDisposableEntityChooser(container);
    }

    /** @return this grid as a disposable component */
    protected final DisposableEntityChooser<T> asDisposableWithoutToolbar()
    {
        return asDisposableEntityChooser(this);
    }

    /**
     * @return this grid as a disposable component with a specified toolbar at the top and a tree on
     *         the left.
     */
    protected final DisposableEntityChooser<T> asDisposableWithToolbarAndTree(
            final Component toolbar, final Component tree)
    {
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());
        container.add(toolbar);

        final LayoutContainer subContainer = new LayoutContainer();
        subContainer.setLayout(new BorderLayout());
        subContainer.add(tree, createLeftBorderLayoutData());
        subContainer.add(this, createCenterBorderLayoutData());

        container.add(subContainer, new RowData(1, 1));

        return asDisposableEntityChooser(container);
    }

    private BorderLayoutData createLeftBorderLayoutData()
    {
        return BorderLayoutDataFactory.create(LayoutRegion.WEST, 200);
    }

    private BorderLayoutData createCenterBorderLayoutData()
    {
        return BorderLayoutDataFactory.create(LayoutRegion.CENTER);
    }

    private DisposableEntityChooser<T> asDisposableEntityChooser(final Component mainComponent)
    {
        final AbstractBrowserGrid<T, M> self = this;
        return new DisposableEntityChooser<T>()
            {
                public T tryGetSingleSelected()
                {
                    List<M> items = getSelectedItems();
                    if (items.isEmpty())
                    {
                        return null;
                    } else
                    {
                        return items.get(0).getBaseObject();
                    }
                }

                public void dispose()
                {
                    debug("dispose a tab");
                    self.disposeCache();
                }

                public Component getComponent()
                {
                    return mainComponent;
                }

                public DatabaseModificationKind[] getRelevantModifications()
                {
                    return self.getRelevantModifications();
                }

                public void update(Set<DatabaseModificationKind> observedModifications)
                {
                    self.update(observedModifications);
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

    private RpcProxy<PagingLoadConfig, PagingLoadResult<M>> createDataLoaderProxy()
    {
        return new RpcProxy<PagingLoadConfig, PagingLoadResult<M>>()
            {
                @Override
                public final void load(final PagingLoadConfig loadConfig,
                        final AsyncCallback<PagingLoadResult<M>> callback)
                {
                    List<GridFilterInfo<T>> appliedFilters = getAppliedFilters();
                    DefaultResultSetConfig<String, T> resultSetConfig =
                            createPagingConfig(loadConfig, columnDefinitions, appliedFilters,
                                    resultSetKey, tryGetCustomFilter(), getGridDisplayTypeID());
                    debug("create a refresh callback");
                    ListEntitiesCallback listCallback =
                            new ListEntitiesCallback(viewContext, callback, resultSetConfig);
                    listEntities(resultSetConfig, listCallback);
                }
            };
    }

    private void debug(String msg)
    {
        if (DEBUG)
        {
            String text =
                    "[grid: " + getGridDisplayTypeID() + ", cache: " + resultSetKey + "] " + msg;
            System.out.println(text);
        }
    }

    private CustomFilterInfo<T> tryGetCustomFilter()
    {
        return filterToolbar.tryGetCustomFilter();
    }

    // returns filters which user wants to apply to the data
    private List<GridFilterInfo<T>> getAppliedFilters()
    {
        List<GridFilterInfo<T>> filters = new ArrayList<GridFilterInfo<T>>();
        if (filterToolbar.isColumnFilterSelected())
        {
            for (PagingColumnFilter<T> filterWidget : filterWidgets)
            {
                filters.add(filterWidget.getFilter());
            }
        }
        return filters;
    }

    protected final List<IColumnDefinition<T>> asColumnFilters(
            IColumnDefinitionKind<T>[] filteredColumnKinds)
    {
        return asColumnFilters(filteredColumnKinds, viewContext);
    }

    private static <T> List<IColumnDefinition<T>> asColumnFilters(
            IColumnDefinitionKind<T>[] filteredColumnKinds, IMessageProvider messageProvider)
    {
        List<IColumnDefinition<T>> filters = new ArrayList<IColumnDefinition<T>>();
        for (IColumnDefinitionKind<T> colDefKind : filteredColumnKinds)
        {
            IColumnDefinition<T> codeColDef =
                    BaseEntityModel.createColumnDefinition(colDefKind, messageProvider);
            filters.add(codeColDef);
        }
        return filters;
    }

    private static <T> DefaultResultSetConfig<String, T> createPagingConfig(
            PagingLoadConfig loadConfig, Set<IColumnDefinition<T>> availableColumns,
            List<GridFilterInfo<T>> appliedFilters, String resultSetKey,
            CustomFilterInfo<T> customFilterInfo, String gridDisplayId)
    {
        int limit = loadConfig.getLimit();
        int offset = loadConfig.getOffset();
        com.extjs.gxt.ui.client.data.SortInfo sortInfo = loadConfig.getSortInfo();

        DefaultResultSetConfig<String, T> resultSetConfig = new DefaultResultSetConfig<String, T>();
        resultSetConfig.setLimit(limit);
        resultSetConfig.setOffset(offset);
        SortInfo<T> translatedSortInfo = translateSortInfo(sortInfo, availableColumns);
        resultSetConfig.setAvailableColumns(availableColumns);
        resultSetConfig.setSortInfo(translatedSortInfo);
        resultSetConfig.setFilterInfos(appliedFilters);
        resultSetConfig.setResultSetKey(resultSetKey);
        resultSetConfig.setCustomFilterInfo(customFilterInfo);
        resultSetConfig.setGridDisplayId(gridDisplayId);
        return resultSetConfig;
    }

    private static <T> SortInfo<T> translateSortInfo(
            com.extjs.gxt.ui.client.data.SortInfo sortInfo,
            Set<IColumnDefinition<T>> availableColumns)
    {
        return translateSortInfo(sortInfo.getSortField(), sortInfo.getSortDir(), availableColumns);
    }

    private static <T> SortInfo<T> translateSortInfo(String dortFieldId,
            com.extjs.gxt.ui.client.Style.SortDir sortDir,
            Set<IColumnDefinition<T>> availableColumns)
    {
        IColumnDefinition<T> sortColumnDefinition = null;
        if (dortFieldId != null)
        {
            Map<String, IColumnDefinition<T>> availableColumnsMap = asColumnIdMap(availableColumns);
            sortColumnDefinition = availableColumnsMap.get(dortFieldId);
            assert sortColumnDefinition != null : "sortColumnDefinition is null";
        }
        SortInfo<T> sortInfo = new SortInfo<T>();
        sortInfo.setSortField(sortColumnDefinition);
        sortInfo.setSortDir(translate(sortDir));
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

    private final class ListEntitiesCallback extends AbstractAsyncCallback<ResultSet<T>>
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
        public final void finishOnFailure(final Throwable caught)
        {
            grid.el().unmask();
            onComplete(false);
            pagingToolbar.enable(); // somehow enabling toolbar is lost in its handleEvent() method
            // no need to show error message - it should be shown by DEFAULT_CALLBACK_LISTENER
            caught.printStackTrace();
            delegate.onFailure(caught);
        }

        @Override
        protected final void process(final ResultSet<T> result)
        {
            // save the key of the result, later we can refer to the result in the cache using this
            // key
            saveCacheKey(result.getResultSetKey());
            totalCount = result.getTotalLength();
            List<GridCustomColumnInfo> customColumnMetadata =
                    result.getList().getCustomColumnsMetadata();
            customColumnsMetadataProvider.setCustomColumnsMetadata(customColumnMetadata);
            // convert the result to the model data for the grid control
            final List<M> models = createModels(result.getList());
            final PagingLoadResult<M> loadResult =
                    new BasePagingLoadResult<M>(models, resultSetConfig.getOffset(), result
                            .getTotalLength());
            delegate.onSuccess(loadResult);
            pagingToolbar.enableExportButton();
            pagingToolbar.updateDefaultConfigButton(true);
            onComplete(true);
        }

        // notify that the refresh is done
        private void onComplete(boolean wasSuccessful)
        {
            refreshCallback.postRefresh(wasSuccessful);
        }

        @Override
        /* Note: we want to differentiate between callbacks in different subclasses of this grid. */
        public String getCallbackId()
        {
            return grid.getId();
        }
    }

    private List<M> createModels(final GridRowModels<T> gridRowModels)
    {
        final List<M> result = new ArrayList<M>();
        for (final GridRowModel<T> entity : gridRowModels)
        {
            M model = createModel(entity);
            result.add(model);
        }
        return result;
    }

    // wraps this browser into the interface appropriate for the toolbar. If this class would just
    // implement the interface it could be very confusing for the code reader.
    protected final IBrowserGridActionInvoker asActionInvoker()
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

                public void configure()
                {
                    delegate.configureColumnSettings();
                }
            };
    }

    protected static interface ISelectedEntityInvoker<M>
    {
        void invoke(M selectedItem);
    }

    protected final ISelectedEntityInvoker<M> asShowEntityInvoker(final boolean editMode)
    {
        return new ISelectedEntityInvoker<M>()
            {
                public void invoke(M selectedItem)
                {
                    if (selectedItem != null)
                    {
                        showEntityViewer(selectedItem.getBaseObject(), editMode);
                    }
                }
            };
    }

    private ISelectedEntityInvoker<M> createNotImplementedInvoker()
    {
        return new ISelectedEntityInvoker<M>()
            {
                public void invoke(M selectedItem)
                {
                    MessageBox.alert(viewContext.getMessage(Dict.MESSAGEBOX_WARNING), viewContext
                            .getMessage(Dict.NOT_IMPLEMENTED), null);
                }
            };
    }

    /**
     * @return a button which has no action but is enabled only when one entity in the grid is
     *         selected. Useful only for writing prototypes.
     */
    protected final Button createSelectedItemDummyButton(final String title)
    {
        return createSelectedItemButton(title, createNotImplementedInvoker());
    }

    /**
     * @return like {@link #createSelectedItemButton(String, ISelectedEntityInvoker)} with button id
     *         set
     */
    protected final Button createSelectedItemButton(final String title, final String id,
            final ISelectedEntityInvoker<M> invoker)
    {
        final Button button = createSelectedItemButton(title, invoker);
        button.setId(id);
        return button;
    }

    /**
     * @return a button which is enabled only when one entity in the grid is selected. When button
     *         is pressed, the specified invoker action is performed.
     */
    protected final Button createSelectedItemButton(final String title,
            final ISelectedEntityInvoker<M> invoker)
    {
        final Button button = new Button(title, new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    List<M> selectedItems = getSelectedItems();
                    if (selectedItems.isEmpty() == false)
                    {
                        invoker.invoke(selectedItems.get(0));
                    }
                }
            });
        button.setEnabled(false);
        addGridSelectionChangeListener(new Listener<SelectionEvent<ModelData>>()
            {
                public void handleEvent(SelectionEvent<ModelData> se)
                {
                    boolean enabled = se.selection.size() == 1;
                    button.setEnabled(enabled);
                }

            });
        return button;
    }

    /**
     * @return a button which is enabled only when at least one entity in the grid is selected, and
     *         with specified selection listener set.
     */
    protected final Button createSelectedItemsButton(final String title,
            SelectionListener<ButtonEvent> listener)
    {
        Button button = new Button(title);
        button.addSelectionListener(listener);
        enableButtonOnSelectedItems(button);
        return button;
    }

    /** adds given <var>button</var> to grid {@link PagingToolBar} */
    protected final void addButton(Button button)
    {
        pagingToolbar.add(new AdapterToolItem(button));
    }

    /**
     * Given <var>button</var> will be enabled only if at least one item is selected in the grid.
     */
    protected final void enableButtonOnSelectedItems(final Button button)
    {
        button.setEnabled(false);
        addGridSelectionChangeListener(new Listener<SelectionEvent<ModelData>>()
            {
                public void handleEvent(SelectionEvent<ModelData> se)
                {
                    boolean enabled = se.selection.size() > 0;
                    button.setEnabled(enabled);
                }

            });
    }

    /**
     * Given <var>button</var> will have title changed depending on number of items selected in the
     * grid.
     */
    protected final void changeButtonTitleOnSelectedItems(final Button button,
            final String noSelectedItemsTitle, final String selectedItemsTitle)
    {
        addGridSelectionChangeListener(new Listener<SelectionEvent<ModelData>>()
            {
                public void handleEvent(SelectionEvent<ModelData> se)
                {
                    boolean noSelected = se.selection.size() == 0;
                    button.setText(noSelected ? noSelectedItemsTitle : selectedItemsTitle);
                }

            });
    }

    private void addGridSelectionChangeListener(Listener<SelectionEvent<ModelData>> listener)
    {
        grid.getSelectionModel().addListener(Events.SelectionChange, listener);
    }

    /**
     * Returns all models of selected items or an empty list if nothing selected.
     */
    protected final List<M> getSelectedItems()
    {
        return grid.getSelectionModel().getSelectedItems();
    }

    /**
     * Returns all base objects of selected items or an empty list if nothing selected.
     */
    protected final List<T> getSelectedBaseObjects()
    {
        List<M> items = getSelectedItems();
        List<T> data = new ArrayList<T>();
        for (BaseEntityModel<T> item : items)
        {
            data.add(item.getBaseObject());
        }
        return data;
    }

    protected final IDelegatedAction createRefreshGridAction()
    {
        return new IDelegatedAction()
            {
                public void execute()
                {
                    refresh();
                }
            };
    }

    protected final <D extends ModelData> SelectionChangedListener<D> createGridRefreshListener()
    {
        return new SelectionChangedListener<D>()
            {
                @Override
                public void selectionChanged(SelectionChangedEvent<D> se)
                {
                    pagingToolbar.disableExportButton();
                    pagingToolbar.updateDefaultConfigButton(false);
                    // export and config buttons are enabled when ListEntitiesCallback is complete
                    refresh();
                    filterToolbar.refresh();
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
     * @param externalRefreshCallbackOrNull external class can define it's own refresh callback
     *            method. It will be merged with the internal one.
     */
    protected final void refresh(final IDataRefreshCallback externalRefreshCallbackOrNull,
            String headerOrNull, boolean refreshColumnsDefinition)
    {
        pagingToolbar.updateDefaultRefreshButton(false);
        debug("clean cache for refresh");
        disposeCache();
        this.refreshCallback = createRefreshCallback(externalRefreshCallbackOrNull);
        setHeader(headerOrNull);
        if (columnDefinitions == null || refreshColumnsDefinition)
        {
            recreateColumnModelAndRefreshColumnsWithFilters();
        }
        reloadData();
        refreshColumnHeaderWidths();
    }

    private void refreshColumnHeaderWidths()
    {
        // Workaround for the problem of incorrect column header widths if column header is very
        // long
        ColumnModel columnModel = grid.getColumnModel();
        if (columnModel.getColumnCount() > 0)
        {
            columnModel.setColumnWidth(0, columnModel.getColumnWidth(0));
        }
    }

    protected final void recreateColumnModelAndRefreshColumnsWithFilters()
    {
        ColumnDefsAndConfigs<T> defsAndConfigs = createColumnsDefinition();
        // add custom columns
        List<IColumnDefinitionUI<T>> customColumnsDefs =
                createCustomColumnDefinitions(customColumnsMetadataProvider
                        .tryGetCustomColumnsMetadata());
        defsAndConfigs.addColumns(customColumnsDefs);

        this.columnDefinitions = defsAndConfigs.getColumnDefs();
        ColumnModel columnModel = createColumnModel(defsAndConfigs.getColumnConfigs());

        refreshColumnsAndFilters(columnModel);
    }

    private static <T> List<IColumnDefinitionUI<T>> createCustomColumnDefinitions(
            List<GridCustomColumnInfo> customColumnsMetadataOrNull)
    {
        List<IColumnDefinitionUI<T>> defs = new ArrayList<IColumnDefinitionUI<T>>();
        if (customColumnsMetadataOrNull == null)
        {
            return defs;
        }
        for (GridCustomColumnInfo columnMetadata : customColumnsMetadataOrNull)
        {
            IColumnDefinitionUI<T> colDef = new GridCustomColumnDefinition<T>(columnMetadata);
            defs.add(colDef);
        }
        return defs;
    }

    private void refreshColumnsAndFiltersWithCurrentModel()
    {
        refreshColumnsAndFilters(getColumnModel());
    }

    private void refreshColumnsAndFilters(ColumnModel columnModel)
    {
        ColumnModel newColumnModel = columnModel;
        GridDisplaySettings settings = tryApplyDisplaySettings(newColumnModel);
        if (settings != null)
        {
            newColumnModel = createColumnModel(settings.getColumnConfigs());
            rebuildFiltersFromIds(settings.getFilteredColumnIds());
        } else
        {
            rebuildFilters(getInitialFilters());
        }
        changeColumnModel(newColumnModel);

        refreshColumnHeaderWidths();
        hideLoadingMask();
    }

    private void hideLoadingMask()
    {
        if (grid.el() != null)
        {
            grid.el().unmask();
        }
    }

    private GridDisplaySettings tryApplyDisplaySettings(ColumnModel columnModel)
    {
        List<IColumnDefinition<T>> initialFilters = getInitialFilters();
        return viewContext.getDisplaySettingsManager().tryApplySettings(getGridDisplayTypeID(),
                columnModel, extractColumnIds(initialFilters));
    }

    private void changeColumnModel(ColumnModel columnModel)
    {
        grid.reconfigure(grid.getStore(), columnModel);
        registerGridSettingsChangesListener();
    }

    private void registerGridSettingsChangesListener()
    {
        viewContext.getDisplaySettingsManager().registerGridSettingsChangesListener(
                getGridDisplayTypeID(), createDisplaySettingsUpdater());
    }

    // Refreshes the data, does not clear the cache. Does not change the column model.
    private void reloadData()
    {
        pagingLoader.load(0, PAGE_SIZE);
    }

    private IDisplaySettingsGetter createDisplaySettingsUpdater()
    {
        return new IDisplaySettingsGetter()
            {
                public ColumnModel getColumnModel()
                {
                    return AbstractBrowserGrid.this.getColumnModel();
                }

                public List<String> getFilteredColumnIds()
                {
                    return extractFilteredColumnIds(filterWidgets);
                }

                public Object getModifier()
                {
                    return AbstractBrowserGrid.this;
                }

            };
    }

    // returns true if some filters have changed
    private boolean rebuildFiltersFromIds(List<String> filteredColumnIds)
    {
        if (filteredColumnIds.equals(extractFilteredColumnIds(filterWidgets)))
        {
            return false; // nothing to change
        }
        List<IColumnDefinition<T>> filteredColumns = getColumnDefinitions(filteredColumnIds);
        rebuildFilters(filteredColumns);
        return true;
    }

    // true if the toolbar with filters has just disappeared or appeared
    private void rebuildFilters(List<IColumnDefinition<T>> filteredColumns)
    {
        List<PagingColumnFilter<T>> newFilterWidgets = createFilterWidgets(filteredColumns);
        rebuildFilterWidgets(newFilterWidgets, this.filterWidgets, viewContext);

        this.filterWidgets = newFilterWidgets;
        filterToolbar.updateColumnFilter(filterWidgets);
    }

    public List<IColumnDefinition<T>> getColumnDefinitions(List<String> columnIds)
    {
        Map<String, IColumnDefinition<T>> colsMap = asColumnIdMap(columnDefinitions);
        List<IColumnDefinition<T>> columns = new ArrayList<IColumnDefinition<T>>();
        for (String columnId : columnIds)
        {
            IColumnDefinition<T> colDef = colsMap.get(columnId);
            assert colDef != null : "Cannot find a column '" + columnId;
            columns.add(colDef);
        }
        return columns;
    }

    protected final String createGridDisplayTypeID(String suffixOrNull)
    {
        if (displayTypeIDGenerator == null)
        {
            throw new IllegalStateException("Undefined display type ID generator.");
        }
        if (suffixOrNull == null)
        {
            return displayTypeIDGenerator.createID();
        } else
        {
            return displayTypeIDGenerator.createID(suffixOrNull);
        }
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
                    if (customColumnsMetadataProvider.getUnsettingHasChanged())
                    {
                        recreateColumnModelAndRefreshColumnsWithFilters();
                    }

                    updateDefaultRefreshButton();
                    if (wasSuccessful)
                    {
                        pagingToolbar.updateDefaultConfigButton(true);
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

    private List<IColumnDefinition<T>> getVisibleColumns(Set<IColumnDefinition<T>> availableColumns)
    {
        Map<String, IColumnDefinition<T>> availableColumnsMap = asColumnIdMap(availableColumns);
        final ColumnModel columnModel = grid.getColumnModel();
        return getVisibleColumns(availableColumnsMap, columnModel);
    }

    private void saveCacheKey(final String newResultSetKey)
    {
        String oldResultSetKey = resultSetKey;
        resultSetKey = newResultSetKey;
        debug("saving new cache key");
        if (oldResultSetKey != null && oldResultSetKey.equals(newResultSetKey) == false)
        {
            debug("cleaning old cache " + oldResultSetKey);
            removeResultSet(oldResultSetKey);
        }
    }

    private void disposeCache()
    {
        if (resultSetKey != null)
        {
            removeResultSet(resultSetKey);
            resultSetKey = null;
        }
    }

    private void removeResultSet(String resultSetKey2)
    {
        viewContext.getService().removeResultSet(resultSetKey2,
                new VoidAsyncCallback<Void>(viewContext));
    }

    private boolean isHardRefreshNeeded()
    {
        return resultSetKey == null;
    }

    /** Export always deals with data from the previous refresh operation */
    private void export()
    {
        export(new ExportEntitiesCallback(viewContext));
    }

    /**
     * Shows the dialog allowing to configure visibility and order of the table columns.
     */
    private void configureColumnSettings()
    {
        assert grid != null && grid.getColumnModel() != null : "Grid must be loaded";

        List<ColumnDataModel> settingsModel =
                createColumnsSettingsModel(getColumnModel(),
                        extractFilteredColumnIds(filterWidgets));
        AbstractColumnSettingsDataModelProvider provider =
                new AbstractColumnSettingsDataModelProvider(settingsModel)
                    {
                        @Override
                        public void onClose(List<ColumnDataModel> newColumnDataModels)
                        {
                            MoveableColumnModel cm = getColumnModel();
                            updateColumnsSettingsModel(cm, newColumnDataModels);

                            // refresh the whole grid if custom columns changed
                            List<GridCustomColumnInfo> newCustomColumns = tryGetCustomColumnsInfo();
                            if (newCustomColumns != null)
                            {
                                customColumnsMetadataProvider
                                        .setCustomColumnsMetadata(newCustomColumns);
                            }
                            boolean customColumnsChanged =
                                    customColumnsMetadataProvider.getUnsettingHasChanged();
                            if (customColumnsChanged)
                            {
                                recreateColumnModelAndRefreshColumnsWithFilters();
                            }
                            boolean filtersChanged =
                                    rebuildFiltersFromIds(getFilteredColumnIds(newColumnDataModels));
                            saveColumnSettings(cm);

                            if (customColumnsChanged)
                            {
                                debug("refreshing custom columns");
                                // if filters refresh filters, so we can ignore filtersChanged state
                                refresh();
                            } else
                            {
                                if (filtersChanged)
                                {
                                    // refresh the data - some filters may have been removed
                                    createApplyFiltersDelagator().execute();
                                }
                                debug("refreshing filters and columns settings");

                                // settings will be automatically stored because of event handling
                                refreshColumnsSettings();
                                refreshColumnHeaderWidths();
                                filterToolbar.refresh();
                            }
                        }
                    };
        ColumnSettingsDialog.show(viewContext, provider, getGridDisplayTypeID());
    }

    private static void saveColumnSettings(MoveableColumnModel cm)
    {
        cm.fireEvent(AppEvents.ColumnSettingsChanged, new ColumnModelEvent(cm));
    }

    // @Private - for tests
    public final void export(final AbstractAsyncCallback<String> callback)
    {
        final TableExportCriteria<T> exportCriteria = createTableExportCriteria();

        prepareExportEntities(exportCriteria, callback);
    }

    protected final TableExportCriteria<T> createTableExportCriteria()
    {
        assert columnDefinitions != null : "refresh before exporting!";
        assert resultSetKey != null : "refresh before exporting, resultSetKey is null!";

        final List<IColumnDefinition<T>> columnDefs = getVisibleColumns(columnDefinitions);
        SortInfo<T> sortInfo = getGridSortInfo();
        final TableExportCriteria<T> exportCriteria =
                new TableExportCriteria<T>(resultSetKey, sortInfo, getAppliedFilters(), columnDefs,
                        columnDefinitions, filterToolbar.tryGetCustomFilter(),
                        getGridDisplayTypeID());
        return exportCriteria;
    }

    // returns info about sorting in current grid
    private SortInfo<T> getGridSortInfo()
    {
        ListStore<M> store = grid.getStore();
        return translateSortInfo(store.getSortField(), store.getSortDir(), columnDefinitions);
    }

    /** @return the number of all objects cached in the browser */
    public int getTotalCount()
    {
        // NOTE: Maybe there is a better way to get this value without keeping it on field variable.
        // Bottom toolbar displays it so there has to be access to this, but e.g.
        // grid.getStore().getCount() returns only number of items displayed on the current page.
        return totalCount;
    }

    private void refreshColumnsSettings()
    {
        grid.setLoadMask(false);
        grid.getView().refresh(true);
        grid.setLoadMask(true);
    }

    protected final void addEntityOperationsLabel()
    {
        pagingToolbar.addEntityOperationsLabel();
    }

    protected final void addEntityOperationsSeparator()
    {
        pagingToolbar.add(new SeparatorToolItem());
    }

    protected final GridCellRenderer<BaseEntityModel<?>> createMultilineStringCellRenderer()
    {
        return new MultilineStringCellRenderer();
    }

    protected final GridCellRenderer<BaseEntityModel<?>> createInternalLinkCellRenderer()
    {
        return new InternalLinkCellRenderer();
    }

    // ------- generic static helpers

    private static List<String> getFilteredColumnIds(List<ColumnDataModel> result)
    {
        List<String> filteredColumnsIds = new ArrayList<String>();
        for (ColumnDataModel model : result)
        {
            if (model.hasFilter())
            {
                filteredColumnsIds.add(model.getColumnID());
            }
        }
        return filteredColumnsIds;
    }

    private static <T> List<String> extractFilteredColumnIds(List<PagingColumnFilter<T>> filters)
    {
        List<String> filteredColumnsIds = new ArrayList<String>();
        for (PagingColumnFilter<T> filter : filters)
        {
            filteredColumnsIds.add(filter.getFilteredColumnId());
        }
        return filteredColumnsIds;
    }

    private static <T> List<String> extractColumnIds(List<IColumnDefinition<T>> columns)
    {
        List<String> columnsIds = new ArrayList<String>();
        for (IColumnDefinition<T> column : columns)
        {
            columnsIds.add(column.getIdentifier());
        }
        return columnsIds;
    }

    /**
     * Updates specified model (<code>cm</code>) with visibility and order settings from
     * <code>columnModels</code>.
     */
    private static void updateColumnsSettingsModel(final MoveableColumnModel cm,
            List<ColumnDataModel> columnModels)
    {
        int newIndex = 0;
        for (ColumnDataModel m : columnModels)
        {
            String columnID = m.getColumnID();
            int oldIndex = cm.getIndexById(columnID);
            if (oldIndex != -1)
            {
                cm.setHidden(oldIndex, m.isVisible() == false);
                cm.move(oldIndex, newIndex++);
            } else
            { // new custom column has been added.
                cm.addAt(newIndex++, createTemporaryColumnConfig(m));
            }
        }
        // all deleted custom columns are now at the end starting from 'newIndex' - remove them
        while (newIndex < cm.getColumnCount())
        {
            cm.remove(cm.getColumnCount() - 1);
        }
    }

    // This column config is created just to make user settings persistent.
    // It must have been a custom column.
    // The config will be recreated in a proper form when data will be refreshed.
    private static ColumnConfig createTemporaryColumnConfig(ColumnDataModel m)
    {
        ColumnConfig columnConfig =
                new ColumnConfig(m.getColumnID(), m.getHeader(),
                        AbstractColumnDefinitionKind.DEFAULT_COLUMN_WIDTH);
        columnConfig.setHidden(m.isVisible() == false);
        return columnConfig;
    }

    private static List<ColumnDataModel> createColumnsSettingsModel(ColumnModel cm,
            List<String> filteredColumnsIds)
    {
        Set<String> filteredColumnsMap = new HashSet<String>(filteredColumnsIds);
        int cols = cm.getColumnCount();
        List<ColumnDataModel> list = new ArrayList<ColumnDataModel>();
        for (int i = 0; i < cols; i++)
        {
            if (cm.getColumnHeader(i) == null || cm.getColumnHeader(i).equals("") || cm.isFixed(i))
            {
                continue;
            }
            String columnId = cm.getColumnId(i);
            boolean isVisible = cm.isHidden(i) == false;
            boolean hasFilter = filteredColumnsMap.contains(columnId);
            list.add(new ColumnDataModel(cm.getColumnHeader(i), isVisible, hasFilter, columnId));
        }
        return list;
    }

    private static ContentPanel createEmptyContentPanel()
    {
        final ContentPanel contentPanel = new ContentPanel();
        contentPanel.setBorders(false);
        contentPanel.setBodyBorder(false);
        contentPanel.setLayout(new FitLayout());
        return contentPanel;
    }

    // creates filter and paging toolbars
    private static <T> LayoutContainer createBottomToolbars(Component filterToolbar,
            Component pagingToolbar)
    {
        LayoutContainer bottomToolbars = new LayoutContainer();
        bottomToolbars.setLayout(new RowLayout(com.extjs.gxt.ui.client.Style.Orientation.VERTICAL));
        bottomToolbars.add(filterToolbar);
        bottomToolbars.add(pagingToolbar);
        return bottomToolbars;
    }

    // Clears the filter toolbar and fills it with the specified filter widgets.
    private static <T> void rebuildFilterWidgets(List<PagingColumnFilter<T>> filterWidgets,
            List<PagingColumnFilter<T>> previousFilterWidgetsOrNull,
            IMessageProvider messageProvider)
    {
        if (filterWidgets.size() == 0)
        {
            return;
        }

        Map<String, PagingColumnFilter<T>> previousFiltersByColumnId =
                new HashMap<String, PagingColumnFilter<T>>();
        if (previousFilterWidgetsOrNull != null)
        {
            for (PagingColumnFilter<T> filter : previousFilterWidgetsOrNull)
            {
                previousFiltersByColumnId.put(filter.getFilteredColumnId(), filter);
            }
        }

        for (PagingColumnFilter<T> filterWidget : filterWidgets)
        {
            // restore previous value if set
            PagingColumnFilter<T> previousFilterWidgetOrNull =
                    previousFiltersByColumnId.get(filterWidget.getFilteredColumnId());
            if (previousFilterWidgetOrNull != null)
            {
                String previousValue = previousFilterWidgetOrNull.getRawValue();
                if (previousValue != "")
                {
                    // simply setting value with setValue does not work
                    filterWidget.setRawValue(previousFilterWidgetOrNull.getRawValue());
                    filterWidget.validate(); // a hack to show previous value as black text
                }
            }
        }
    }

    private static <T extends ModelData> Grid<T> createGrid(
            PagingLoader<PagingLoadConfig> dataLoader, String gridId)
    {
        ListStore<T> listStore = new ListStore<T>(dataLoader);
        ColumnModel columnModel = createColumnModel(new ArrayList<ColumnConfig>());
        Grid<T> grid = new Grid<T>(listStore, columnModel);
        grid.setId(gridId);
        grid.setLoadMask(true);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        return grid;
    }

    // this should be the only place where we create the grid column model.
    private static MoveableColumnModel createColumnModel(List<ColumnConfig> columConfigs)
    {
        return new MoveableColumnModel(columConfigs);
    }

    private MoveableColumnModel getColumnModel()
    {
        return (MoveableColumnModel) grid.getColumnModel();
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
            final Set<IColumnDefinition<T>> defs)
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
    private static <T/* column definition */> List<T> getVisibleColumns(
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

    /** Creates deletion callback that refreshes the grid. */
    protected final AbstractAsyncCallback<Void> createDeletionCallback(
            IBrowserGridActionInvoker invoker)
    {
        return new DeletionCallback(viewContext, invoker);
    }

    /** Deletion callback that refreshes the grid. */
    private static final class DeletionCallback extends AbstractAsyncCallback<Void>
    {
        private final IBrowserGridActionInvoker invoker;

        public DeletionCallback(IViewContext<?> viewContext, IBrowserGridActionInvoker invoker)
        {
            super(viewContext);
            this.invoker = invoker;
        }

        @Override
        protected void process(Void result)
        {
            invoker.refresh();
        }
    }

    /** {@link SelectionListener} that creates a dialog with selected data items. */
    protected abstract class AbstractCreateDialogListener extends SelectionListener<ButtonEvent>
    {
        @Override
        public void componentSelected(ButtonEvent ce)
        {
            List<T> data = getSelectedBaseObjects();
            IBrowserGridActionInvoker invoker = asActionInvoker();
            if (validateSelectedData(data))
            {
                createDialog(data, invoker).show();
            }
        }

        /**
         * If specified data is valid returns true, otherwise returns false. Dialog will be shown
         * only if this method returns true. Default implementation always returns true.
         */
        protected boolean validateSelectedData(List<T> data)
        {
            return true;
        }

        protected abstract Dialog createDialog(List<T> data, IBrowserGridActionInvoker invoker);
    }

    /**
     * If user selected some entities in given browser first a dialog is shown where he can select
     * between showing data sets related to selected/displayed entities. Then a tab is displayed
     * where these related data sets are listed.<br>
     * <br>
     * If no entities were selected in given browser the tab is displayed where data sets related to
     * all entities displayed in the grid are listed.
     */
    // NOTE: This method cannot be externalized from AbstractBrowserGrid because it uses some
    // AbstractBrowserGrid's protected methods
    protected static final <E extends IEntityInformationHolder> void showRelatedDataSets(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final AbstractBrowserGrid<E, ? extends BaseEntityModel<E>> browser)
    {
        final List<? extends IEntityInformationHolder> selectedEntities =
                browser.getSelectedBaseObjects();
        final TableExportCriteria<? extends IEntityInformationHolder> displayedEntities =
                browser.createTableExportCriteria();
        if (selectedEntities.isEmpty())
        {
            // no entity selected - show datasets related to all displayed
            RelatedDataSetCriteria relatedCriteria =
                    RelatedDataSetCriteria.createDisplayedEntities(displayedEntities);
            ShowRelatedDatasetsDialog.showRelatedDatasetsTab(viewContext, relatedCriteria);
        } else
        {
            // > 0 entity selected - show dialog with all/selected radio
            new ShowRelatedDatasetsDialog(viewContext, selectedEntities, displayedEntities, browser
                    .getTotalCount()).show();
        }
    }
}
