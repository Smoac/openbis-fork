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

package ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.application;

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ListProteinSummaryByExperimentCriteria;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ProteinSummaryGridColumnIDs;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinSummary;

/**
 * @author Franz-Josef Elmer
 */
class ProteinSummaryGrid extends TypedTableGrid<ProteinSummary>
{
    private static final String PREFIX =
            GenericConstants.ID_PREFIX + "protein-summary";

    // browser consists of the grid and additional toolbars (paging, filtering)
    public static final String BROWSER_ID = PREFIX + "_main";

    public static final String GRID_ID = PREFIX + TypedTableGrid.GRID_POSTFIX;

    static IDisposableComponent create(IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        return new ProteinSummaryGrid(viewContext).asDisposableWithoutToolbar();
    }

    private final IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext;

    private ListProteinSummaryByExperimentCriteria criteria;

    ProteinSummaryGrid(IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, true,
                PhosphoNetXDisplayTypeIDGenerator.PROTEIN_SUMMARY_BROWSER_GRID);
        specificViewContext = viewContext;
    }

    void update(TechId experimentID)
    {
        criteria = new ListProteinSummaryByExperimentCriteria();
        criteria.setExperimentID(experimentID);
        refresh(true);
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return ProteinSummaryGridColumnIDs.FDR.equals(columnID) ? "false_discovery_rate_column"
                : columnID.toLowerCase();
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(ProteinSummaryGridColumnIDs.FDR);
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<ProteinSummary>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<ProteinSummary>> callback)
    {
        if (criteria != null)
        {
            criteria.copyPagingConfig(resultSetConfig);
            specificViewContext.getService().listProteinSummariesByExperiment(criteria, callback);
        }
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<TableModelRowWithObject<ProteinSummary>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        specificViewContext.getService().prepareExportProteinSummary(exportCriteria, callback);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[0];
    }

}
