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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data.DataSetReportColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;

/**
 * Grid displaying dataset reporting results. This grid is special comparing to other grids, because
 * it cannot be refreshed and it is ensured, that the data for the grid are cached before it is
 * created.
 * 
 * @author Tomasz Pylak
 */
public class DataSetReporterGrid extends
        AbstractBrowserGrid<TableModelRow, BaseEntityModel<TableModelRow>>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "DataSetReporterGrid";

    public static final String GRID_ID = BROWSER_ID + "_grid";

    /**
     * Do not display more than this amount of columns in the report, web browsers have problem with
     * it
     */
    private static final int MAX_SHOWN_COLUMNS = 200;

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            TableModelReference tableModelReference, DatastoreServiceDescription service)
    {
        final DataSetReporterGrid grid =
                new DataSetReporterGrid(viewContext, tableModelReference, service.getKey(), service
                        .getDownloadURL());
        return grid.asDisposableWithoutToolbar();
    }

    public static class DatasetReportColumnUI extends DataSetReportColumnDefinition implements
            IColumnDefinitionUI<TableModelRow>
    {
        private boolean isHidden;

        public DatasetReportColumnUI(TableModelColumnHeader columnHeader, String downloadURL,
                String sessionID, boolean isHidden)
        {
            super(columnHeader, downloadURL, sessionID);
            this.isHidden = isHidden;
        }

        public int getWidth()
        {
            return AbstractColumnDefinitionKind.DEFAULT_COLUMN_WIDTH;
        }

        public boolean isHidden()
        {
            return isHidden;
        }

        // GWT only
        @SuppressWarnings("unused")
        private DatasetReportColumnUI()
        {
            this(null, null, null, false);
        }
    }

    public static String createId(String idSuffix)
    {
        return BROWSER_ID + "_" + idSuffix;
    }

    private final List<TableModelColumnHeader> tableHeader;

    private final String resultSetKey;

    private final String reportKind;

    private final String downloadURL;

    private DataSetReporterGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            TableModelReference tableModelReference, String reportKind, String downloadURL)
    {
        super(viewContext, GRID_ID, false, true, DisplayTypeIDGenerator.DATA_SET_REPORTING_GRID);
        this.downloadURL = downloadURL;
        setId(BROWSER_ID);
        this.tableHeader = tableModelReference.getHeader();
        this.resultSetKey = tableModelReference.getResultSetKey();
        this.reportKind = reportKind;
        updateDefaultRefreshButton();
    }

    @Override
    public String getGridDisplayTypeID()
    {
        return createGridDisplayTypeID(reportKind);
    }

    @Override
    protected BaseEntityModel<TableModelRow> createModel(GridRowModel<TableModelRow> entity)
    {
        return new BaseEntityModel<TableModelRow>(entity, createColDefinitions());
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, TableModelRow> resultSetConfig,
            AbstractAsyncCallback<ResultSet<TableModelRow>> callback)
    {
        // in all cases the data should be taken from the cache, and we know the key already
        resultSetConfig.setResultSetKey(resultSetKey);
        viewContext.getService().listDatasetReport(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<TableModelRow> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportDatasetReport(exportCriteria, callback);
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        // do noting
    }

    @Override
    protected List<IColumnDefinition<TableModelRow>> getInitialFilters()
    {
        return Collections.emptyList();
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[] {};
    }

    @Override
    protected final boolean isRefreshEnabled()
    {
        /**
         * Refresh is not possible. The reason is that we would have to regenerate the report using
         * the datasets selected in another tab. But this tab may no longer exist!
         */
        return false;
    }

    @Override
    protected void refresh()
    {
        refresh(null, false);
    }

    @Override
    protected void showEntityViewer(TableModelRow entity, boolean editMode)
    {
        // do nothing
    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRow> createColumnsDefinition()
    {
        return ColumnDefsAndConfigs.create(createColDefinitions());
    }

    private List<IColumnDefinitionUI<TableModelRow>> createColDefinitions()
    {
        String sessionID = viewContext.getModel().getSessionContext().getSessionID();
        List<IColumnDefinitionUI<TableModelRow>> columns =
                new ArrayList<IColumnDefinitionUI<TableModelRow>>();
        int i = 0;
        for (TableModelColumnHeader columnHeader : tableHeader)
        {
            boolean isHidden = (i > MAX_SHOWN_COLUMNS);
            columns.add(new DatasetReportColumnUI(columnHeader, downloadURL, sessionID, isHidden));
            i++;
        }
        return columns;
    }
}
