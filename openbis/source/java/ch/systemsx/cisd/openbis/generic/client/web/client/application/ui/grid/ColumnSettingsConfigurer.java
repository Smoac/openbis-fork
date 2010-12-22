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

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind.DEFAULT_COLUMN_WIDTH;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplaySettingsGetter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.filter.FilterToolbar;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridCustomColumnInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig;

/**
 * A class that manages the configuring of column settings in the AbstractBrowserGrid.
 * <p>
 * Considered a friend class to AbstractBrowserGried, i.e., it is allowed to use methods that are
 * otherwise private.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class ColumnSettingsConfigurer<T, M extends BaseEntityModel<T>>
{
    private final AbstractBrowserGrid<T, M> browserGrid;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final FilterToolbar<T> filterToolbar;

    private final CustomColumnsMetadataProvider customColumnsMetadataProvider;

    // result set key of the last refreshed data
    private final String resultSetKeyOrNull;

    // not null only if there is a pending request. No new request can be issued until this one is
    // finished.
    private final ResultSetFetchConfig<String> pendingFetchConfigOrNull;

    public ColumnSettingsConfigurer(AbstractBrowserGrid<T, M> browserGrid,
            IViewContext<ICommonClientServiceAsync> viewContext, FilterToolbar<T> filterToolbar,
            CustomColumnsMetadataProvider customColumnsMetadataProvider, String resultSetKeyOrNull,
            ResultSetFetchConfig<String> pendingFetchConfigOrNull)
    {
        this.browserGrid = browserGrid;
        this.viewContext = viewContext;
        this.filterToolbar = filterToolbar;
        this.customColumnsMetadataProvider = customColumnsMetadataProvider;
        this.resultSetKeyOrNull = resultSetKeyOrNull;
        this.pendingFetchConfigOrNull = pendingFetchConfigOrNull;
    }

    public void showDialog()
    {
        List<ColumnDataModel> settingsModel =
                AbstractBrowserGrid.createColumnsSettingsModel(browserGrid.getFullColumnModel(),
                        filterToolbar.extractFilteredColumnIds());
        AbstractColumnSettingsDataModelProvider provider =
                new AbstractColumnSettingsDataModelProvider(settingsModel)
                    {
                        @Override
                        public void onClose(List<ColumnDataModel> newColumnDataModels)
                        {
                            final ColumnModel newColumnModel =
                                    createNewColumnModel(newColumnDataModels);
                            final List<String> filteredColumnIds =
                                    getFilteredColumnIds(newColumnDataModels);
                            String gridDisplayTypeID = browserGrid.getGridDisplayTypeID();
                            DisplaySettingsManager displaySettingsManager =
                                    viewContext.getDisplaySettingsManager();
                            displaySettingsManager.storeSettings(gridDisplayTypeID,
                                    new IDisplaySettingsGetter()
                                        {

                                            public Object getModifier()
                                            {
                                                return browserGrid;
                                            }

                                            public List<String> getFilteredColumnIds()
                                            {
                                                return filteredColumnIds;
                                            }

                                            public ColumnModel getColumnModel()
                                            {
                                                return newColumnModel;
                                            }
                                        }, false);

                            // refresh the whole grid if custom columns changed
                            List<GridCustomColumnInfo> newCustomColumns = tryGetCustomColumnsInfo();
                            if (newCustomColumns != null)
                            {
                                customColumnsMetadataProvider
                                        .setCustomColumnsMetadata(newCustomColumns);
                            }
                            boolean customColumnsChanged =
                                    customColumnsMetadataProvider.getHasChangedAndSetFalse();
                            browserGrid.recreateColumnModelAndRefreshColumnsWithFilters();

                            boolean columnFiltersChanged =
                                    browserGrid.rebuildFiltersFromIds(filteredColumnIds);

                            if (customColumnsChanged || columnFiltersChanged)
                            {
                                browserGrid
                                        .debug("refreshing custom columns and/or filter distinct value in "
                                                + pendingFetchConfigOrNull + " mode");
                                // we do not need to reload data if custom filters changed (we do
                                // not need distinct column values)
                                browserGrid.reloadData(createRefreshSettingsFetchConfig());
                            }
                            // settings will be automatically stored because of event handling
                            browserGrid.refreshColumnsSettings();
                            filterToolbar.refresh();
                        }

                        private ResultSetFetchConfig<String> createRefreshSettingsFetchConfig()
                        {
                            if (pendingFetchConfigOrNull == null)
                            {
                                if (resultSetKeyOrNull == null)
                                {
                                    return ResultSetFetchConfig.createComputeAndCache();
                                } else
                                {
                                    return ResultSetFetchConfig
                                            .createFetchFromCacheAndRecompute(resultSetKeyOrNull);
                                }
                            } else
                            {
                                return pendingFetchConfigOrNull;
                            }
                        }
                    };
        ColumnSettingsDialog.show(viewContext, provider, browserGrid.getGridDisplayTypeID());
    }

    /**
     * Creates a new column model based on the specified column data models and the old full column
     * model as provided by {@link AbstractBrowserGrid#getFullColumnModel()}.
     */
    private ColumnModel createNewColumnModel(List<ColumnDataModel> newColumnDataModels)
    {
        Map<String, ColumnConfig> oldColumns = getOldColumns();
        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        for (ColumnDataModel columnDataModel : newColumnDataModels)
        {
            String columnID = columnDataModel.getColumnID();
            ColumnConfig column = oldColumns.get(columnID);
            if (column == null)
            {
                String header = columnDataModel.getHeader();
                column = new ColumnConfig(columnID, header, DEFAULT_COLUMN_WIDTH);
            }
            column.setHidden(columnDataModel.isVisible() == false);
            columns.add(column);
        }
        return new ColumnModel(columns);
    }
    
    private Map<String, ColumnConfig> getOldColumns()
    {
        List<ColumnConfig> columns = browserGrid.getFullColumnModel().getColumns();
        HashMap<String, ColumnConfig> map = new HashMap<String, ColumnConfig>();
        for (ColumnConfig columnConfig : columns)
        {
            map.put(columnConfig.getId(), columnConfig);
        }
        return map;
    }
    
    private static List<String> getFilteredColumnIds(List<ColumnDataModel> result)
    {
        List<String> filteredColumnsIds = new ArrayList<String>();
        for (ColumnDataModel model : result)
        {
            if (model.hasFilter() && filteredColumnsIds.size() < FilterToolbar.MAX_FILTER_FIELDS)
            {
                filteredColumnsIds.add(model.getColumnID());
            }
        }
        return filteredColumnsIds;
    }

}
