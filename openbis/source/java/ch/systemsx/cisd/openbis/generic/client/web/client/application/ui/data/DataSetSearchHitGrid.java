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
 * 
 */

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data.DataSetSearchHitColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellClickListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DataSetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetSearchHit;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * Grid with data set search results.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetSearchHitGrid extends
        AbstractBrowserGrid<DataSetSearchHit, DataSetSearchHitModel>
{

    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID =
            GenericConstants.ID_PREFIX + "data-set-search-hit-browser";

    public static final String GRID_ID = BROWSER_ID + "_grid";

    public static DisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        DataSetSearchHitGrid grid = new DataSetSearchHitGrid(viewContext);
        final DataSetSearchWindow searchWindow = new DataSetSearchWindow(viewContext);
        final DataSetSearchToolbar toolbar =
                new DataSetSearchToolbar(grid, viewContext.getMessage(Dict.BUTTON_CHANGE_QUERY),
                        searchWindow);
        searchWindow.setUpdateListener(toolbar);
        return grid.asDisposableWithToolbar(toolbar);
    }

    private DataSetSearchCriteria criteria;

    private List<PropertyType> availablePropertyTypes;

    private DataSetSearchHitGrid(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, GRID_ID, false, true);
        setId(BROWSER_ID);
        updateDefaultRefreshButton();
        registerCellClickListenerFor(DataSetSearchHitColDefKind.CODE.id(),
                new ICellClickListener<DataSetSearchHit>()
                    {
                        public void handle(DataSetSearchHit rowItem)
                        {
                            DataSetUtils.showDataSet(rowItem.getDataSet(), viewContext.getModel());
                        }
                    });
    }

    @Override
    protected List<IColumnDefinition<DataSetSearchHit>> getAvailableFilters()
    {
        return asColumnFilters(new DataSetSearchHitColDefKind[]
            { DataSetSearchHitColDefKind.CODE, DataSetSearchHitColDefKind.LOCATION,
                    DataSetSearchHitColDefKind.FILE_TYPE });
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, DataSetSearchHit> resultSetConfig,
            AbstractAsyncCallback<ResultSet<DataSetSearchHit>> callback)
    {
        viewContext.getService().searchForDataSets(criteria, resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<DataSetSearchHit> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportDataSetSearchHits(exportCriteria, callback);
    }

    public void refresh(DataSetSearchCriteria newCriteria, List<PropertyType> propertyTypes)
    {
        criteria = newCriteria;
        availablePropertyTypes = propertyTypes;
        refresh();
    }

    @Override
    protected void refresh()
    {
        if (criteria == null)
        {
            return;
        }
        super.refresh(null, false);
    }

    @Override
    protected final boolean isRefreshEnabled()
    {
        return true;
    }

    @Override
    protected final void showEntityViewer(DataSetSearchHitModel modelData)
    {
        // do nothing
    }

    @Override
    protected DataSetSearchHitModel createModel(DataSetSearchHit entity)
    {
        return new DataSetSearchHitModel(entity);
    }

    @Override
    protected ColumnDefsAndConfigs<DataSetSearchHit> createColumnsDefinition()
    {
        return DataSetSearchHitModel.createColumnsSchema(viewContext, availablePropertyTypes);
    }

}