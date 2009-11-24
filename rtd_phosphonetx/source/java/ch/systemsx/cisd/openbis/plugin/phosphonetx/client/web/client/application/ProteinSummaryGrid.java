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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.columns.ProteinSummaryColDefKind;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinSummaryByExperimentCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSummary;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class ProteinSummaryGrid extends AbstractSimpleBrowserGrid<ProteinSummary>
{
    private static final String PREFIX =
            GenericConstants.ID_PREFIX + "protein-summary";

    // browser consists of the grid and additional toolbars (paging, filtering)
    public static final String BROWSER_ID = PREFIX + "_main";

    public static final String GRID_ID = PREFIX + "_grid";

    static IDisposableComponent create(IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        return new ProteinSummaryGrid(viewContext).asDisposableWithoutToolbar();
    }

    private final IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext;

    private ListProteinSummaryByExperimentCriteria criteria;

    ProteinSummaryGrid(IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, GRID_ID, false,
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
    protected IColumnDefinitionKind<ProteinSummary>[] getStaticColumnsDefinition()
    {
        return ProteinSummaryColDefKind.values();
    }

    @Override
    protected List<IColumnDefinition<ProteinSummary>> getInitialFilters()
    {
        return asColumnFilters(new ProteinSummaryColDefKind[0]);
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, ProteinSummary> resultSetConfig,
            AbstractAsyncCallback<ResultSet<ProteinSummary>> callback)
    {
        if (criteria != null)
        {
            criteria.copyPagingConfig(resultSetConfig);
            specificViewContext.getService().listProteinSummariesByExperiment(criteria, callback);
        }
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<ProteinSummary> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        specificViewContext.getService().prepareExportProteinSummary(exportCriteria, callback);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[0];
    }

}
