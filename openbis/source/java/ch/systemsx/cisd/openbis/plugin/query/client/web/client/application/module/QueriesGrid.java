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

package ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomFilter;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Constants;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.DisplayTypeIDGenerator;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class QueriesGrid extends AbstractSimpleBrowserGrid<GridCustomFilter>
{
    
    private static final String BROWSER_ID = Constants.QUERY_ID_PREFIX + "queries_browser";
    private static final String GRID_ID = BROWSER_ID + "_grid";
    private final IViewContext<IQueryClientServiceAsync> viewContext;
    
    QueriesGrid(IViewContext<IQueryClientServiceAsync> viewContext)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, GRID_ID, DisplayTypeIDGenerator.QUERY_EDITOR);
        this.viewContext = viewContext;
    }

    @Override
    protected IColumnDefinitionKind<GridCustomFilter>[] getStaticColumnsDefinition()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected List<IColumnDefinition<GridCustomFilter>> getInitialFilters()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, GridCustomFilter> resultSetConfig,
            AbstractAsyncCallback<ResultSet<GridCustomFilter>> callback)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<GridCustomFilter> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        // TODO Auto-generated method stub
        
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
