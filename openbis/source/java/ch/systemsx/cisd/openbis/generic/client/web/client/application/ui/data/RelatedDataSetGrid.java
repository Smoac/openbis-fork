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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RelatedDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * Grid with data sets related with specified entities.
 * 
 * @author Piotr Buczek
 */
public class RelatedDataSetGrid extends AbstractExternalDataGrid
{

    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "related-data-set-browser";

    public static final String GRID_ID = BROWSER_ID + "-grid";

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final RelatedDataSetCriteria relatedCriteria)
    {
        RelatedDataSetGrid grid = new RelatedDataSetGrid(viewContext, relatedCriteria);
        return grid.asDisposableWithoutToolbar();
    }

    private RelatedDataSetCriteria relatedCriteria;

    private RelatedDataSetGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            final RelatedDataSetCriteria relatedCriteria)
    {
        super(viewContext, BROWSER_ID, GRID_ID, DisplayTypeIDGenerator.RELATED_DATA_SET_GRID);
        this.relatedCriteria = relatedCriteria;
    }

    @Override
    protected void listDatasets(DefaultResultSetConfig<String, ExternalData> resultSetConfig,
            final AbstractAsyncCallback<ResultSetWithEntityTypes<ExternalData>> callback)
    {
        viewContext.getService().searchForDataSets(relatedCriteria, resultSetConfig, callback);
    }

    @Override
    protected void refresh()
    {
        if (relatedCriteria == null)
        {
            return;
        }
        super.refresh();
    }

    @Override
    protected DataSetSearchHitModel createModel(GridRowModel<ExternalData> entity)
    {
        return new DataSetSearchHitModel(entity);
    }

    @Override
    protected ColumnDefsAndConfigs<ExternalData> createColumnsSchema()
    {
        List<PropertyType> propertyTypes = criteria == null ? null : criteria.tryGetPropertyTypes();
        return DataSetSearchHitModel.createColumnsSchema(viewContext, propertyTypes);
    }

}
