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

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
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
                AbstractBrowserGrid.createColumnsSettingsModel(browserGrid.getColumnModel(),
                        filterToolbar.extractFilteredColumnIds());
        AbstractColumnSettingsDataModelProvider provider =
                new AbstractColumnSettingsDataModelProvider(settingsModel)
                    {
                        @Override
                        public void onClose(List<ColumnDataModel> newColumnDataModels)
                        {
                            MoveableColumnModel cm = browserGrid.getColumnModel();
                            AbstractBrowserGrid.updateColumnsSettingsModel(cm, newColumnDataModels);

                            // refresh the whole grid if custom columns changed
                            List<GridCustomColumnInfo> newCustomColumns = tryGetCustomColumnsInfo();
                            if (newCustomColumns != null)
                            {
                                customColumnsMetadataProvider
                                        .setCustomColumnsMetadata(newCustomColumns);
                            }
                            boolean customColumnsChanged =
                                    customColumnsMetadataProvider.getHasChangedAndSetFalse();
                            if (customColumnsChanged)
                            {
                                browserGrid.recreateColumnModelAndRefreshColumnsWithFilters();
                            }

                            boolean columnFiltersChanged =
                                    browserGrid.rebuildFiltersFromIds(AbstractBrowserGrid
                                            .getFilteredColumnIds(newColumnDataModels));
                            browserGrid.saveColumnDisplaySettings();

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
}
