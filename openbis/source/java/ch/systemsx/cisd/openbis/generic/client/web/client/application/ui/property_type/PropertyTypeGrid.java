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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.shared.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.PropertyTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GridWithRPCProxy;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;

/**
 * {@link GridWithRPCProxy} displaying property types.
 * 
 * @author Izabela Adamczyk
 */
public class PropertyTypeGrid extends AbstractBrowserGrid<PropertyType, PropertyTypeModel>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "property-type-browser";

    public static final String GRID_ID = BROWSER_ID + "_grid";

    public static DisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        return new PropertyTypeGrid(viewContext).asDisposableWithoutToolbar();
    }

    private PropertyTypeGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, GRID_ID, false, true);
        setId(BROWSER_ID);
        updateDefaultRefreshButton();
    }

    @Override
    protected ColumnDefsAndConfigs<PropertyType> createColumnsDefinition()
    {
        return ColumnDefsAndConfigs.create(PropertyTypeModel.createColumnsSchema(viewContext));
    }

    @Override
    protected List<PropertyTypeModel> createModels(List<PropertyType> entities)
    {
        return PropertyTypeModel.convert(entities);
    }

    @Override
    protected boolean isRefreshEnabled()
    {
        return true;
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, PropertyType> resultSetConfig,
            AbstractAsyncCallback<ResultSet<PropertyType>> callback)
    {
        viewContext.getService().listPropertyTypes(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<PropertyType> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportPropertyTypes(exportCriteria, callback);
    }

    @Override
    protected void refresh()
    {
        super.refresh(null, false);
    }

    @Override
    protected void showEntityViewer(PropertyTypeModel modelData)
    {
        // do nothing
    }
}