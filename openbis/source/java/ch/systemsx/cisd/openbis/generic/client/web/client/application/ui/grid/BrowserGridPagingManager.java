/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.data.Loader;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ComponentEventLogger;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ComponentEventLogger.EventPair;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.filter.FilterToolbar;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridFilters;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ColumnSetting;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo;

/**
 * Manages logic for dealing with paging in AbstractBrowserGrids.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class BrowserGridPagingManager<T, M extends BaseEntityModel<T>>
{
    private static final int PAGE_SIZE = 50;

    private final AbstractBrowserGrid<T, M> browserGrid;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final PagingLoader<PagingLoadResult<M>> pagingLoader;

    private final ContentPanel contentPanel;

    // the toolbar has the refresh and export buttons besides the paging controls
    private final BrowserGridPagingToolBar pagingToolbar;

    // used to change displayed filter widgets
    private final FilterToolbar<T> filterToolbar;

    public BrowserGridPagingManager(AbstractBrowserGrid<T, M> browserGrid,
            IViewContext<ICommonClientServiceAsync> viewContext,
            PagingLoader<PagingLoadResult<M>> pagingLoader, ContentPanel contentPanel,
            IBrowserGridActionInvoker actionInvoker, String gridId, FilterToolbar<T> filterToolbar)
    {
        this.browserGrid = browserGrid;
        this.viewContext = viewContext;
        this.pagingLoader = pagingLoader;
        this.contentPanel = contentPanel;
        this.pagingToolbar =
                new BrowserGridPagingToolBar(actionInvoker, viewContext, PAGE_SIZE, gridId);
        pagingToolbar.bind(this.pagingLoader);
        this.filterToolbar = filterToolbar;

        configureBottomToolbarSyncSize();
    }

    public BrowserGridPagingToolBar getPagingToolbar()
    {
        return pagingToolbar;
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

    public void load(int offset)
    {
        pagingLoader.load(offset, PAGE_SIZE);
    }

    /** adds given <var>button</var> to grid {@link PagingToolBar} */
    public void addButton(Button button)
    {
        pagingToolbar.add(button);
    }

    public void addEntityOperationsLabel()
    {
        pagingToolbar.addEntityOperationsLabel();
    }

    public void addEntityOperationsSeparator()
    {
        pagingToolbar.add(new SeparatorToolItem());
    }

    public void postRefresh()
    {
        pagingToolbar.updateDefaultConfigButton(true);
        pagingToolbar.enableExportButton();
    }

    public void refresh()
    {
        pagingToolbar.updateDefaultRefreshButton(false);
    }

    public void refreshGridWithFilters()
    {
        // export and config buttons are enabled when ListEntitiesCallback is complete
        pagingToolbar.disableExportButton();
        pagingToolbar.updateDefaultConfigButton(false);
    }

    public void updateDefaultRefreshButton(boolean isEnabled)
    {
        pagingToolbar.updateDefaultRefreshButton(isEnabled);
    }

    public void process()
    {
        pagingToolbar.enableExportButton();
        pagingToolbar.updateDefaultConfigButton(true);
    }

    public void finishOnFailure()
    {
        pagingToolbar.enable(); // somehow enabling toolbar is lost in its handleEvent() method
    }

    /** @return the number of all objects cached in the browser */
    public int getTotalCount()
    {
        return pagingToolbar.getTotalCount();
    }

    public void configureLoggingBetweenEvents(ComponentEventLogger logger)
    {
        logger.prepareLoggingBetweenEvents(pagingToolbar, EventPair.LAYOUT);
    }

    public DefaultResultSetConfig<String, T> createPagingConfig(PagingLoadConfig loadConfig,
            GridFilters<T> filters, String gridDisplayId,
            Set<IColumnDefinition<T>> columnDefinitions,
            ResultSetFetchConfig<String> pendingFetchConfigOrNull)
    {
        int limit = loadConfig.getLimit();
        int offset = loadConfig.getOffset();
        com.extjs.gxt.ui.client.data.SortInfo sortInfo = loadConfig.getSortInfo();

        DefaultResultSetConfig<String, T> resultSetConfig = new DefaultResultSetConfig<String, T>();
        resultSetConfig.setLimit(limit);
        resultSetConfig.setOffset(offset);
        SortInfo<T> translatedSortInfo =
                AbstractBrowserGrid.translateSortInfo(sortInfo, columnDefinitions);
        resultSetConfig.setAvailableColumns(columnDefinitions);
        Set<String> columnIDs = getIDsOfColumnsToBeShown(columnDefinitions);
        resultSetConfig.setIDsOfPresentedColumns(columnIDs);
        resultSetConfig.setSortInfo(translatedSortInfo);
        resultSetConfig.setFilters(filters);
        resultSetConfig.setCacheConfig(pendingFetchConfigOrNull);
        resultSetConfig.setGridDisplayId(gridDisplayId);
        resultSetConfig.setCustomColumnErrorMessageLong(viewContext.getDisplaySettingsManager()
                .isDisplayCustomColumnDebuggingErrorMessages());
        return resultSetConfig;
    }

    private Set<String> getIDsOfColumnsToBeShown(Set<IColumnDefinition<T>> columnDefinitions)
    {
        Set<String> columnIDs = new HashSet<String>();
        DisplaySettingsManager manager = viewContext.getDisplaySettingsManager();
        List<ColumnSetting> columnSettings =
                manager.getColumnSettings(browserGrid.getGridDisplayTypeID());
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
        List<IColumnDefinition<T>> visibleColumns =
                browserGrid.getVisibleColumns(columnDefinitions);
        for (IColumnDefinition<T> definition : visibleColumns)
        {
            columnIDs.add(definition.getIdentifier());
        }
        return columnIDs;
    }

}
