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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExternalDataModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data.CommonExternalDataColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DataSetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractExternalDataGrid extends AbstractSimpleBrowserGrid<ExternalData>
{
    public static final String GRID_POSFIX = "-grid";
    
    protected AbstractExternalDataGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            String browserId)
    {
        super(viewContext, browserId, GRID_POSFIX);
        registerCellClickListenerFor(CommonExternalDataColDefKind.CODE.id(),
                new ICellListener<ExternalData>()
                    {
                        public void handle(ExternalData rowItem)
                        {
                            DataSetUtils.showDataSet(rowItem, viewContext.getModel());
                        }
                    });
    }

    @Override
    protected IColumnDefinitionKind<ExternalData>[] getStaticColumnsDefinition()
    {
        return CommonExternalDataColDefKind.values();
    }

    @Override
    protected BaseEntityModel<ExternalData> createModel(ExternalData entity)
    {
        return new ExternalDataModel(entity);
    }
    
    @Override
    protected List<IColumnDefinition<ExternalData>> getAvailableFilters()
    {
        return asColumnFilters(new CommonExternalDataColDefKind[]
            { CommonExternalDataColDefKind.CODE, CommonExternalDataColDefKind.LOCATION,
                    CommonExternalDataColDefKind.FILE_FORMAT_TYPE });
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<ExternalData> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportDataSetSearchHits(exportCriteria, callback);
    }

}
