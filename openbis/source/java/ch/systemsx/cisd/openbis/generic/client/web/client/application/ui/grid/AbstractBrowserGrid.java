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

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.Loader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Container;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.VoidAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager.GridDisplaySettings;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplaySettingsGetter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.MultilineStringCellRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.RealNumberRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ComponentEventLogger;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ComponentEventLogger.EventPair;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.GridCustomColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.filter.FilterToolbar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridCustomColumnInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridFilters;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ColumnSetting;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
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
    abstract protected void showEntityViewer(T entity, boolean editMode, boolean inBackground);

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
     * {@link #refresh(IDataRefreshCallback, boolean) at some point. }
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

    protected final ICellListener<T> showEntityViewerLinkClickListener;

    // ------ private section. NOTE: it should remain unaccessible to subclasses! ---------------

    private static final int PAGE_SIZE = 50;

    // set to true to see some useful debugging messages
    private static final boolean DEBUG = false;

    private final PagingLoader<PagingLoadResult<M>> pagingLoader;

    private final ContentPanel contentPanel;

    private final Grid<M> grid;

    private final ColumnListener<T, M> columnListener;

    private final boolean refreshAutomatically;

    // the toolbar has the refresh and export buttons besides the paging controls
    private final BrowserGridPagingToolBar pagingToolbar;

    // used to change displayed filter widgets
    private final FilterToolbar<T> filterToolbar;

    private final IDisplayTypeIDGenerator displayTypeIDGenerator;

    // --------- private non-final fields

    // available columns definitions
    private Set<IColumnDefinition<T>> columnDefinitions;

    private final CustomColumnsMetadataProvider customColumnsMetadataProvider;

    // result set key of the last refreshed data
    private String resultSetKeyOrNull;

    // Keeps track of the pending fetch (only tracks 1)
    private final PendingFetchManager pendingFetchManager;

    private IDataRefreshCallback refreshCallback;

    private LayoutContainer bottomToolbars;

    private ColumnModel fullColumnModel;

    protected AbstractBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            String gridId, IDisplayTypeIDGenerator displayTypeIDGenerator)
    {
        this(viewContext, gridId, false, displayTypeIDGenerator);
    }

    /**
     * @param refreshAutomatically should the data be automatically loaded when the grid is rendered
     *            for the first time?
     * @param gridId unique id of the grid which will can used by testframework to identify the grid
     *            and identify the callback which fills it in.
     */
    protected AbstractBrowserGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            String gridId, boolean refreshAutomatically,
            IDisplayTypeIDGenerator displayTypeIDGenerator)
    {
        pendingFetchManager = new PendingFetchManager();
        this.displayTypeIDGenerator = displayTypeIDGenerator;
        this.viewContext = viewContext;
        int logID = log("create browser grid " + gridId);
        this.refreshAutomatically = refreshAutomatically;
        this.pagingLoader = createPagingLoader();
        this.customColumnsMetadataProvider = new CustomColumnsMetadataProvider();
        this.grid = createGrid(pagingLoader, gridId);
        // WORKAROUND
        // Lazy loading of rows causes tests using experiment browser fail (selection of
        // project in project tree grid doesn't work).
        // Turning it off for all grids is the safest solution for our system tests framework
        // and should improve GUI speed in development mode a bit.
        grid.setLazyRowRender(0);
        this.pagingToolbar =
                new BrowserGridPagingToolBar(asActionInvoker(), viewContext, PAGE_SIZE, gridId);
        pagingToolbar.bind(pagingLoader);
        this.filterToolbar =
                new FilterToolbar<T>(viewContext, gridId, this, createApplyFiltersDelagator());

        this.contentPanel = createEmptyContentPanel();
        bottomToolbars = createBottomToolbars(contentPanel, pagingToolbar);
        configureBottomToolbarSyncSize();
        contentPanel.add(grid);
        contentPanel.setBottomComponent(bottomToolbars);
        contentPanel.setHeaderVisible(false);
        columnListener = new ColumnListener<T, M>(grid);
        showEntityViewerLinkClickListener = createShowEntityViewerLinkClickListener();
        registerLinkClickListenerFor(Dict.CODE, showEntityViewerLinkClickListener);
        setLayout(new FitLayout());
        add(contentPanel);

        configureLoggingBetweenEvents(logID);

        grid.addListener(Events.HeaderContextMenu, new Listener<GridEvent<ModelData>>()
            {
                public void handleEvent(final GridEvent<ModelData> ge)
                {
                    Menu menu = ge.getMenu();
                    int itemCount = menu.getItemCount();
                    for (int i = 2; i < itemCount; i++)
                    {
                        menu.remove(menu.getItem(2));
                    }
                }
            });
    }

    private ICellListener<T> createShowEntityViewerLinkClickListener()
    {
        return new ICellListener<T>()
            {
                public void handle(T rowItem, boolean keyPressed)
                {
                    showEntityViewer(rowItem, false, keyPressed);
                }
            };
    }

    private void configureBottomToolbarSyncSize()
    {
        // fixes problems with:
        // - no 'overflow' button when some buttons don't fit into pagingToolbar
        pagingLoader.addListener(Loader.Load, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    pagingToolbar.syncSize();
                }
            });
        // - hidden paging toolbar
        pagingToolbar.addListener(Events.AfterLayout, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    contentPanel.syncSize();
                }
            });
        // - bottom toolbar is not resized when new filter row appears
        filterToolbar.addListener(Events.AfterLayout, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    contentPanel.syncSize();
                }
            });
    }

    private void configureLoggingBetweenEvents(int logID)
    {
        if (viewContext.isLoggingEnabled())
        {
            ComponentEventLogger logger = new ComponentEventLogger(viewContext, getId());
            logger.prepareLoggingBetweenEvents(contentPanel, EventPair.RENDER);
            logger.prepareLoggingBetweenEvents(this, EventPair.LAYOUT);
            logger.prepareLoggingBetweenEvents(grid, EventPair.LAYOUT);
            logger.prepareLoggingBetweenEvents(contentPanel, EventPair.LAYOUT);
            logger.prepareLoggingBetweenEvents(bottomToolbars, EventPair.LAYOUT);
            logger.prepareLoggingBetweenEvents(filterToolbar, EventPair.LAYOUT);
            logger.prepareLoggingBetweenEvents(pagingToolbar, EventPair.LAYOUT);
            viewContext.logStop(logID);
        }
    }

    protected int log(String message)
    {
        return viewContext.log(message + " [" + getId() + "]");
    }

    protected void showEntityInformationHolderViewer(IEntityInformationHolderWithPermId entity,
            boolean editMode, boolean inBackground)
    {
        final EntityKind entityKind = entity.getEntityKind();
        final AbstractTabItemFactory tabView;
        BasicEntityType entityType = entity.getEntityType();
        final IClientPluginFactory clientPluginFactory =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        entityType);
        final IClientPlugin<BasicEntityType, IEntityInformationHolderWithPermId> createClientPlugin =
                clientPluginFactory.createClientPlugin(entityKind);
        if (editMode)
        {
            tabView = createClientPlugin.createEntityEditor(entity);
        } else
        {
            tabView = createClientPlugin.createEntityViewer(entity);
        }
        tabView.setInBackground(inBackground);
        DispatcherHelper.dispatchNaviEvent(tabView);
    }

    /** Refreshes the grid without showing the loading progress bar */
    protected final void refreshGridSilently()
    {
        grid.setLoadMask(false);
        int id = log("refresh silently");
        refresh();
        grid.setLoadMask(true);
        viewContext.logStop(id);
    }

    /**
     * Shows/hides the load mask immediately.
     * 
     * @param loadMask Load mask is shown if <code>true</code> otherwise it is hidden.
     */
    public void setLoadMaskImmediately(boolean loadMask)
    {
        if (grid.isRendered())
        {
            if (loadMask)
            {
                grid.el().mask(GXT.MESSAGES.loadMask_msg());
            } else
            {
                grid.el().unmask();
            }
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
        if (viewContext.isSimpleMode() == false)
        {
            columnListener.registerLinkClickListener(columnID, listener);
        }
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

    private IDelegatedAction createApplyFiltersDelagator()
    {
        return new IDelegatedAction()
            {
                public void execute()
                {
                    if (resultSetKeyOrNull != null && pendingFetchManager.hasNoPendingFetch())
                    {
                        ResultSetFetchConfig<String> fetchConfig =
                                ResultSetFetchConfig.createFetchFromCache(resultSetKeyOrNull);
                        reloadData(fetchConfig);
                    }
                }
            };
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
            final Component toolbar, final Component tree, String headerOrNull)
    {
        // WORKAROUND: BorderLayout causes problems when rendered in a tab
        // We use RowLayout here but we loose the split this way.
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout(Orientation.VERTICAL));
        container.add(toolbar, new RowData(1, -1));

        final LayoutContainer subContainer = new LayoutContainer();
        subContainer.setLayout(new RowLayout(Orientation.HORIZONTAL));
        subContainer.add(tree, new RowData(300, 1));
        if (headerOrNull != null)
        {
            this.contentPanel.setHeaderVisible(true);
            this.contentPanel.setHeading(headerOrNull);
        }
        subContainer.add(this, new RowData(1, 1));
        container.add(subContainer, new RowData(1, 1));

        return asDisposableEntityChooser(container);
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
                    debug("dispose a browser");
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
            int id = log("layout automatically");
            layout();
            viewContext.logStop(id);
            id = log("refresh automatically");
            refresh();
            viewContext.logStop(id);
        }
    }

    private PagingLoader<PagingLoadResult<M>> createPagingLoader()
    {
        final RpcProxy<PagingLoadResult<M>> proxy = new RpcProxy<PagingLoadResult<M>>()
            {

                @Override
                protected void load(Object loadConfig, AsyncCallback<PagingLoadResult<M>> callback)
                {
                    loadData((PagingLoadConfig) loadConfig, callback);
                }
            };
        final BasePagingLoader<PagingLoadResult<M>> newPagingLoader =
                new BasePagingLoader<PagingLoadResult<M>>(proxy);
        newPagingLoader.setRemoteSort(true);
        return newPagingLoader;
    }

    private void loadData(final PagingLoadConfig loadConfig,
            final AsyncCallback<PagingLoadResult<M>> callback)
    {
        if (pendingFetchManager.hasNoPendingFetch())
        {
            // this can happen when user wants to sort data - the refresh method is not called
            if (resultSetKeyOrNull == null)
            {
                // data are not yet cached, so we ignore this call - should not really happen
                return;
            }
            pendingFetchManager.pushPendingFetchConfig(ResultSetFetchConfig
                    .createFetchFromCache(resultSetKeyOrNull));
        }
        GridFilters<T> filters = filterToolbar.getFilters();
        final DefaultResultSetConfig<String, T> resultSetConfig =
                createPagingConfig(loadConfig, filters, getGridDisplayTypeID());
        debug("create a refresh callback " + pendingFetchManager.tryTopPendingFetchConfig());
        final ListEntitiesCallback listCallback =
                new ListEntitiesCallback(viewContext, callback, resultSetConfig);

        listEntities(resultSetConfig, listCallback);
    }

    // Default visibility so that friend classes can use -- should otherwise be considered private
    void debug(String msg)
    {
        if (DEBUG)
        {
            String text =
                    "[grid: " + getGridDisplayTypeID() + ", cache: " + resultSetKeyOrNull + "] "
                            + msg;
            System.out.println(text);
        }
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

    private DefaultResultSetConfig<String, T> createPagingConfig(PagingLoadConfig loadConfig,
            GridFilters<T> filters, String gridDisplayId)
    {
        int limit = loadConfig.getLimit();
        int offset = loadConfig.getOffset();
        com.extjs.gxt.ui.client.data.SortInfo sortInfo = loadConfig.getSortInfo();

        DefaultResultSetConfig<String, T> resultSetConfig = new DefaultResultSetConfig<String, T>();
        resultSetConfig.setLimit(limit);
        resultSetConfig.setOffset(offset);
        SortInfo<T> translatedSortInfo = translateSortInfo(sortInfo, columnDefinitions);
        resultSetConfig.setAvailableColumns(columnDefinitions);
        Set<String> columnIDs = getIDsOfColumnsToBeShown();
        resultSetConfig.setIDsOfPresentedColumns(columnIDs);
        resultSetConfig.setSortInfo(translatedSortInfo);
        resultSetConfig.setFilters(filters);
        resultSetConfig.setCacheConfig(pendingFetchManager.tryTopPendingFetchConfig());
        resultSetConfig.setGridDisplayId(gridDisplayId);
        resultSetConfig.setCustomColumnErrorMessageLong(viewContext.getDisplaySettingsManager()
                .isDisplayCustomColumnDebuggingErrorMessages());
        return resultSetConfig;
    }

    private Set<String> getIDsOfColumnsToBeShown()
    {
        Set<String> columnIDs = new HashSet<String>();
        DisplaySettingsManager manager = viewContext.getDisplaySettingsManager();
        List<ColumnSetting> columnSettings = manager.getColumnSettings(getGridDisplayTypeID());
        if (columnSettings != null)
        {
            for (ColumnSetting columnSetting : columnSettings)
            {
                if (columnSetting.isHidden() == false)
                {
                    columnIDs.add(columnSetting.getColumnID());
                }
            }
        }
        List<IColumnDefinition<T>> visibleColumns = getVisibleColumns(columnDefinitions);
        for (IColumnDefinition<T> definition : visibleColumns)
        {
            columnIDs.add(definition.getIdentifier());
        }
        return columnIDs;
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

    public final class ListEntitiesCallback extends AbstractAsyncCallback<ResultSet<T>>
    {
        private final AsyncCallback<PagingLoadResult<M>> delegate;

        // configuration with which the listing was called
        private final DefaultResultSetConfig<String, T> resultSetConfig;

        private int logID;

        public ListEntitiesCallback(final IViewContext<?> viewContext,
                final AsyncCallback<PagingLoadResult<M>> delegate,
                final DefaultResultSetConfig<String, T> resultSetConfig)
        {
            super(viewContext);
            this.delegate = delegate;
            this.resultSetConfig = resultSetConfig;
            logID = log("load data");
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
            viewContext.logStop(logID);
            logID = log("process loaded data");
            // save the key of the result, later we can refer to the result in the cache using this
            // key
            saveCacheKey(result.getResultSetKey());
            GridRowModels<T> rowModels = result.getList();
            List<GridCustomColumnInfo> customColumnMetadata = rowModels.getCustomColumnsMetadata();
            customColumnsMetadataProvider.setCustomColumnsMetadata(customColumnMetadata);
            // convert the result to the model data for the grid control
            final List<M> models = createModels(rowModels);
            final PagingLoadResult<M> loadResult =
                    new BasePagingLoadResult<M>(models, resultSetConfig.getOffset(),
                            result.getTotalLength());

            delegate.onSuccess(loadResult);
            pagingToolbar.enableExportButton();
            pagingToolbar.updateDefaultConfigButton(true);

            filterToolbar.refreshColumnFiltersDistinctValues(rowModels.getColumnDistinctValues());
            onComplete(true);

            viewContext.logStop(logID);
        }

        // notify that the refresh is done
        private void onComplete(boolean wasSuccessful)
        {
            pendingFetchManager.popPendingFetch();
            refreshCallback.postRefresh(wasSuccessful);
        }

        private List<M> createModels(final GridRowModels<T> gridRowModels)
        {
            final List<M> result = new ArrayList<M>();
            initializeModelCreation();
            for (final GridRowModel<T> entity : gridRowModels)
            {
                M model = createModel(entity);
                result.add(model);
            }
            return result;
        }

        @Override
        /* Note: we want to differentiate between callbacks in different subclasses of this grid. */
        public String getCallbackId()
        {
            return grid.getId();
        }
    }

    /**
     * Initializes creation of model from received data. This is a hook method called before {
     * {@link AbstractBrowserGrid#createModel(GridRowModel)} is invoked for all rows. This
     * implementation does nothing. Subclasses usually override this method by creating an instance
     * attribute which holds a list of visible column definitions. This speeds up invocation of
     * {@link AbstractBrowserGrid#createModel(GridRowModel)}.
     */
    protected void initializeModelCreation()
    {
    }

    protected Set<String> getIDsOfVisibleColumns()
    {
        Set<String> visibleColumnIds = new HashSet<String>();
        for (int i = 0, n = fullColumnModel.getColumnCount(); i < n; i++)
        {
            ColumnConfig column = fullColumnModel.getColumn(i);
            if (column.isHidden() == false)
            {
                visibleColumnIds.add(column.getId());
            }
        }
        return visibleColumnIds;
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
                    int id = log("refresh in action invoker");
                    delegate.refresh();
                    viewContext.logStop(id);
                }

                public void configure()
                {
                    delegate.configureColumnSettings();
                }

                public void toggleFilters(boolean show)
                {
                    if (show)
                    {
                        int logId = log("adding filters");
                        bottomToolbars.insert(filterToolbar, 0);
                        bottomToolbars.layout();
                        viewContext.logStop(logId);
                    } else
                    {
                        int logId = log("removing filters");
                        bottomToolbars.remove(filterToolbar);
                        bottomToolbars.layout();
                        viewContext.logStop(logId);
                    }
                }
            };
    }

    protected static interface ISelectedEntityInvoker<M>
    {
        void invoke(M selectedItem, boolean keyPressed);
    }

    protected final ISelectedEntityInvoker<M> asShowEntityInvoker(final boolean editMode)
    {
        return new ISelectedEntityInvoker<M>()
            {
                public void invoke(M selectedItem, boolean keyPressed)
                {
                    if (selectedItem != null)
                    {
                        showEntityViewer(selectedItem.getBaseObject(), editMode, keyPressed);
                    }
                }
            };
    }

    private ISelectedEntityInvoker<M> createNotImplementedInvoker()
    {
        return new ISelectedEntityInvoker<M>()
            {
                public void invoke(M selectedItem, boolean keyPressed)
                {
                    MessageBox.alert(viewContext.getMessage(Dict.MESSAGEBOX_WARNING),
                            viewContext.getMessage(Dict.NOT_IMPLEMENTED), null);
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
                        invoker.invoke(selectedItems.get(0), false);
                    }
                }
            });
        button.setEnabled(false);
        addGridSelectionChangeListener(new Listener<SelectionChangedEvent<ModelData>>()
            {
                public void handleEvent(SelectionChangedEvent<ModelData> se)
                {
                    boolean enabled = se.getSelection().size() == 1;
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
        pagingToolbar.add(button);
    }

    /**
     * Given <var>button</var> will be enabled only if at least one item is selected in the grid.
     */
    protected final void enableButtonOnSelectedItems(final Button button)
    {
        button.setEnabled(false);
        addGridSelectionChangeListener(new Listener<SelectionChangedEvent<ModelData>>()
            {
                public void handleEvent(SelectionChangedEvent<ModelData> se)
                {
                    boolean enabled = se.getSelection().size() > 0;
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
        addGridSelectionChangeListener(new Listener<SelectionChangedEvent<ModelData>>()
            {
                public void handleEvent(SelectionChangedEvent<ModelData> se)
                {
                    boolean noSelected = se.getSelection().size() == 0;
                    button.setText(noSelected ? noSelectedItemsTitle : selectedItemsTitle);
                }

            });
    }

    private void addGridSelectionChangeListener(Listener<SelectionChangedEvent<ModelData>> listener)
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
                    int id = log("execute refrish grid action");
                    refresh();
                    viewContext.logStop(id);
                }
            };
    }

    /** Refreshes grid and filters (resets filter selection) */
    protected final void refreshGridWithFilters()
    {
        // N.B. -- The order in which things are refreshed and configured is significant

        // export and config buttons are enabled when ListEntitiesCallback is complete
        pagingToolbar.disableExportButton();
        pagingToolbar.updateDefaultConfigButton(false);

        // Need to reset filter fields *before* refreshing the grid so the list can
        // be correctly retrieved
        filterToolbar.resetFilterFields();
        filterToolbar.resetFilterSelectionWithoutApply();

        int id = log("refresh grid with filters");
        refresh();
        viewContext.logStop(id);

        // Need to refresh the filter toolbar *after* refreshing the grid, because it
        // has a dependency on information from the grid that gets updated with the refresh
        filterToolbar.refresh();
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
    protected final void refresh(boolean refreshColumnsDefinition)
    {
        refresh(null, refreshColumnsDefinition);
    }

    /**
     * @param externalRefreshCallbackOrNull external class can define it's own refresh callback
     *            method. It will be merged with the internal one.
     */
    protected final void refresh(final IDataRefreshCallback externalRefreshCallbackOrNull,
            boolean refreshColumnsDefinition)
    {
        int id = log("refresh (refreshColumnsDefinition=" + refreshColumnsDefinition + ")");
        pagingToolbar.updateDefaultRefreshButton(false);
        debug("clean cache for refresh");
        this.refreshCallback = createRefreshCallback(externalRefreshCallbackOrNull);
        if (columnDefinitions == null || refreshColumnsDefinition)
        {
            recreateColumnModelAndRefreshColumnsWithFilters();
        }
        reloadData(createDisposeAndRefreshFetchMode());
        viewContext.logStop(id);
    }

    private ResultSetFetchConfig<String> createDisposeAndRefreshFetchMode()
    {
        if (resultSetKeyOrNull != null)
        {
            return ResultSetFetchConfig.createClearComputeAndCache(resultSetKeyOrNull);
        } else
        {
            return ResultSetFetchConfig.createComputeAndCache();
        }
    }

    protected final void recreateColumnModelAndRefreshColumnsWithFilters()
    {
        int logId = log("recreateColumnModelAndRefreshColumnsWithFilters");

        ColumnDefsAndConfigs<T> defsAndConfigs = createColumnsDefinition();
        // add custom columns
        List<GridCustomColumnInfo> customColumnsMetadata =
                customColumnsMetadataProvider.getCustomColumnsMetadata();
        if (customColumnsMetadata.size() > 0)
        {
            List<IColumnDefinitionUI<T>> customColumnsDefs =
                    createCustomColumnDefinitions(customColumnsMetadata);
            defsAndConfigs.addColumns(customColumnsDefs);

            RealNumberRenderer renderer =
                    new RealNumberRenderer(viewContext.getDisplaySettingsManager()
                            .getRealNumberFormatingParameters());
            for (GridCustomColumnInfo gridCustomColumnInfo : customColumnsMetadata)
            {
                if (gridCustomColumnInfo.getDataType() == DataTypeCode.REAL)
                {
                    defsAndConfigs.setGridCellRendererFor(gridCustomColumnInfo.getCode(), renderer);
                }
            }
        }

        this.columnDefinitions = defsAndConfigs.getColumnDefs();
        ColumnModel columnModel = createColumnModel(defsAndConfigs.getColumnConfigs());

        refreshColumnsAndFilters(columnModel);

        viewContext.logStop(logId);
    }

    private static <T> List<IColumnDefinitionUI<T>> createCustomColumnDefinitions(
            List<GridCustomColumnInfo> customColumnsMetadata)
    {
        List<IColumnDefinitionUI<T>> defs = new ArrayList<IColumnDefinitionUI<T>>();
        for (GridCustomColumnInfo columnMetadata : customColumnsMetadata)
        {
            IColumnDefinitionUI<T> colDef = new GridCustomColumnDefinition<T>(columnMetadata);
            defs.add(colDef);
        }
        return defs;
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
            filterToolbar.rebuildColumnFilters(getInitialFilters());
        }
        changeColumnModel(newColumnModel);
    }

    private void hideLoadingMask()
    {
        if (grid.isRendered() && grid.el() != null)
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
        fullColumnModel = columnModel;
        int logId = log("grid reconfigure");
        ColumnModel columnModelOfVisible = trimToVisibleColumns(columnModel);
        grid.reconfigure(grid.getStore(), columnModelOfVisible);
        viewContext.logStop(logId);
        registerGridSettingsChangesListener();
        // add listeners of full column model to trimmed model
        List<Listener<? extends BaseEvent>> listeners =
            fullColumnModel.getListeners(Events.WidthChange);
        for (Listener<? extends BaseEvent> listener : listeners)
        {
            columnModelOfVisible.addListener(Events.WidthChange, listener);
        }
    }
    
    private ColumnModel trimToVisibleColumns(ColumnModel columnModel)
    {
        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        for (int i = 0, n = columnModel.getColumnCount(); i < n; i++)
        {
            ColumnConfig column = columnModel.getColumn(i);
            if (column.isHidden() == false)
            {
                columns.add(column);
            }
        }
        ColumnModel trimmedModel = createColumnModel(columns);
        return trimmedModel;
    }

    private void registerGridSettingsChangesListener()
    {
        viewContext.getDisplaySettingsManager().registerGridSettingsChangesListener(
                getGridDisplayTypeID(), createDisplaySettingsUpdater());
    }

    // Refreshes the data, does not clear the cache. Does not change the column model.
    // Default visibility so that friend classes can use -- should otherwise be considered private
    void reloadData(ResultSetFetchConfig<String> resultSetFetchConfig)
    {
        if (pendingFetchManager.hasPendingFetch())
        {
            debug("Cannot reload the data with the mode '" + resultSetFetchConfig
                    + "'; there is an unfinished request already: "
                    + pendingFetchManager.tryTopPendingFetchConfig());
            return;
        }
        pendingFetchManager.pushPendingFetchConfig(resultSetFetchConfig);
        pagingLoader.load(0, PAGE_SIZE);
    }

    private IDisplaySettingsGetter createDisplaySettingsUpdater()
    {
        return new IDisplaySettingsGetter()
            {
                public ColumnModel getColumnModel()
                {
                    return AbstractBrowserGrid.this.getFullColumnModel();
                }

                public List<String> getFilteredColumnIds()
                {
                    return filterToolbar.extractFilteredColumnIds();
                }

                public Object getModifier()
                {
                    return AbstractBrowserGrid.this;
                }

            };
    }

    // returns true if some filters have changed
    // Default visibility so that friend classes can use -- should otherwise be considered private
    boolean rebuildFiltersFromIds(List<String> filteredColumnIds)
    {
        List<IColumnDefinition<T>> filteredColumns = getColumnDefinitions(filteredColumnIds);
        return filterToolbar.rebuildColumnFilters(filteredColumns);
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
                    if (customColumnsMetadataProvider.getHasChangedAndSetFalse())
                    {
                        recreateColumnModelAndRefreshColumnsWithFilters();
                    }

                    updateDefaultRefreshButton();

                    if (wasSuccessful)
                    {
                        hideLoadingMask();
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

    private List<IColumnDefinition<T>> getVisibleColumns(Set<IColumnDefinition<T>> availableColumns)
    {
        Map<String, IColumnDefinition<T>> availableColumnsMap = asColumnIdMap(availableColumns);
        return getVisibleColumns(availableColumnsMap, fullColumnModel);
    }

    private void saveCacheKey(final String newResultSetKey)
    {
        resultSetKeyOrNull = newResultSetKey;
        debug("saving new cache key");
    }

    private void disposeCache()
    {
        if (resultSetKeyOrNull != null)
        {
            removeResultSet(resultSetKeyOrNull);
            resultSetKeyOrNull = null;
        }
    }

    private void removeResultSet(String resultSetKey2)
    {
        viewContext.getService().removeResultSet(resultSetKey2,
                new VoidAsyncCallback<Void>(viewContext));
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
        ColumnSettingsConfigurer<T, M> columnSettingsConfigurer =
                new ColumnSettingsConfigurer<T, M>(this, viewContext, filterToolbar,
                        customColumnsMetadataProvider, resultSetKeyOrNull,
                        pendingFetchManager.tryTopPendingFetchConfig());
        columnSettingsConfigurer.showDialog();
    }

    // Default visibility so that friend classes can use -- should otherwise be considered private
    void saveColumnDisplaySettings()
    {
        IDisplaySettingsGetter settingsUpdater = createDisplaySettingsUpdater();
        viewContext.getDisplaySettingsManager().storeSettings(getGridDisplayTypeID(),
                settingsUpdater, false);
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
        assert resultSetKeyOrNull != null : "refresh before exporting, resultSetKey is null!";

        final List<IColumnDefinition<T>> columnDefs = getVisibleColumns(columnDefinitions);
        SortInfo<T> sortInfo = getGridSortInfo();
        final TableExportCriteria<T> exportCriteria =
                new TableExportCriteria<T>(resultSetKeyOrNull, sortInfo,
                        filterToolbar.getFilters(), columnDefs, columnDefinitions,
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
        return pagingToolbar.getTotalCount();
    }

    // Default visibility so that friend classes can use -- should otherwise be considered private
    void refreshColumnsSettings()
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

    protected GridCellRenderer<BaseEntityModel<?>> createInternalLinkCellRenderer()
    {
        return LinkRenderer.createLinkRenderer();
    }

    // ------- generic static helpers

    private static <T> List<String> extractColumnIds(List<IColumnDefinition<T>> columns)
    {
        List<String> columnsIds = new ArrayList<String>();
        for (IColumnDefinition<T> column : columns)
        {
            columnsIds.add(column.getIdentifier());
        }
        return columnsIds;
    }

    // Default visibility so that friend classes can use -- should otherwise be considered private
    static List<ColumnDataModel> createColumnsSettingsModel(ColumnModel cm,
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
    private static <T> LayoutContainer createBottomToolbars(final Container<?> parentContainer,
            ToolBar pagingToolbar)
    {
        LayoutContainer bottomToolbars = new ContainerKeeper(parentContainer);
        bottomToolbars.setMonitorWindowResize(true);
        bottomToolbars.setLayout(new RowLayout(com.extjs.gxt.ui.client.Style.Orientation.VERTICAL));
        // Adding paging toolbar before data are loaded fixes problems with the toolbar not visible
        // if user quickly changes tab / hides section before it is layouted. On the other hand
        // it is slower than adding it after requesting server for data.
        bottomToolbars.add(pagingToolbar, new RowData(1, -1));
        // filter toolbar is added on request
        return bottomToolbars;
    }

    private static final class ContainerKeeper extends LayoutContainer
    {
        private final Container<?> parentContainer;

        private ContainerKeeper(Container<?> parentContainer)
        {
            this.parentContainer = parentContainer;
        }

        @Override
        protected void onWindowResize(int aWidth, int aHeight)
        {
            super.onWindowResize(aWidth, aHeight);
            if (isVisible())
            {
                this.setWidth(parentContainer.getWidth());
            }
        }
    }

    private static <T extends ModelData> Grid<T> createGrid(
            PagingLoader<PagingLoadResult<T>> dataLoader, String gridId)
    {

        ListStore<T> listStore = new ListStore<T>(dataLoader);
        ColumnModel columnModel = createColumnModel(new ArrayList<ColumnConfig>());
        final Grid<T> grid = new Grid<T>(listStore, columnModel);
        grid.setId(gridId);
        grid.setLoadMask(true);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid.setView(new ExtendedGridView());
        grid.setStripeRows(true);
        return grid;
    }

    // this should be the only place where we create the grid column model.
    private static ColumnModel createColumnModel(List<ColumnConfig> columConfigs)
    {
        return new ColumnModel(columConfigs);
    }

    protected ColumnModel getFullColumnModel()
    {
        return fullColumnModel;
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

}
