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

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExternalDataModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;

/**
 * @author Franz-Josef Elmer
 */
public class ExternalDataGrid extends AbstractBrowserGrid<ExternalData, ExternalDataModel>
{
    public ExternalDataGrid(IViewContext<ICommonClientServiceAsync> viewContext, String gridId)
    {
        super(viewContext, gridId);
    }

    @Override
    protected boolean isRefreshEnabled()
    {
        return true;
    }

    @Override
    protected ColumnDefsAndConfigs<ExternalData> createColumnsDefinition()
    {
        List<IColumnDefinitionUI<ExternalData>> list =
                ExternalDataModel.createColumnsSchema(viewContext);
        return ColumnDefsAndConfigs.create(list);
    }

    @Override
    protected ExternalDataModel createModel(ExternalData entity)
    {
        return new ExternalDataModel(entity);
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, ExternalData> resultSetConfig,
            AbstractAsyncCallback<ResultSet<ExternalData>> callback)
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<ExternalData> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void showEntityViewer(ExternalDataModel modelData)
    {
        // TODO Auto-generated method stub
    }

    @Override
    protected void refresh()
    {
        // TODO Auto-generated method stub
    }

}
