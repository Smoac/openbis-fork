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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.plateviewer;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * @author Tomasz Pylak
 */
public class PlateContentGrid extends AbstractSimpleBrowserGrid<WellData>
{

    protected PlateContentGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            String browserId, String gridId, IDisplayTypeIDGenerator displayTypeIDGenerator)
    {
        super(viewContext, browserId, gridId, true, displayTypeIDGenerator);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected IColumnDefinitionKind<WellData>[] getStaticColumnsDefinition()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected List<IColumnDefinition<WellData>> getInitialFilters()
    {
        return new ArrayList<IColumnDefinition<WellData>>();
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, WellData> resultSetConfig,
            AbstractAsyncCallback<ResultSet<WellData>> callback)
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<WellData> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        // TODO Auto-generated method stub

    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[] {};
    }

}
