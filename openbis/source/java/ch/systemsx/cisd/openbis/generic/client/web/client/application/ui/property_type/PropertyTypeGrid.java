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

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.PropertyTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.columns.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * Grid displaying property types.
 * 
 * @author Tomasz Pylak
 */
public class PropertyTypeGrid extends AbstractSimpleBrowserGrid<PropertyType>
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
        super(viewContext, BROWSER_ID, GRID_ID);
    }

    @Override
    protected IColumnDefinitionKind<PropertyType>[] getStaticColumnsDefinition()
    {
        return PropertyTypeColDefKind.values();
    }

    @Override
    protected List<IColumnDefinition<PropertyType>> getAvailableFilters()
    {
        return asColumnFilters(new PropertyTypeColDefKind[]
            { PropertyTypeColDefKind.LABEL, PropertyTypeColDefKind.CODE,
                    PropertyTypeColDefKind.DATA_TYPE });
    }

    @Override
    protected PropertyTypeModel createModel(PropertyType entity)
    {
        return new PropertyTypeModel(entity, getStaticColumnsDefinition());
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
}