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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.List;

import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.renderer.AbstractPropertyColRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.RealNumberRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.EntityPropertyColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesFilterUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RealNumberFormatingParameters;

/**
 * Factory for creating model or column definitions for grids which displays entities with
 * properties.
 * <p>
 * Note that we do not create {@link IColumnDefinition} for custom columns. We do not need this both
 * in the case of row values rendering (row model has all needed information) and in case of grid
 * header construction (custom columns definition will be constructed later when the row data will
 * be fetched).
 * </p>
 * 
 * @author Tomasz Pylak
 */
public class EntityGridModelFactory<T extends IEntityPropertiesHolder>
{
    private static final long serialVersionUID = 1L;

    private final IViewContext<?> viewContext;

    private final IColumnDefinitionKind<T>[] staticColumnDefinitions;

    public EntityGridModelFactory(final IViewContext<?> viewContext,
            IColumnDefinitionKind<T>[] staticColumnDefinitions)
    {
        this.viewContext = viewContext;
        this.staticColumnDefinitions = staticColumnDefinitions;
    }

    public BaseEntityModel<T> createModel(GridRowModel<T> entity,
            RealNumberFormatingParameters realNumberFormatingParameters)
    {
        List<IColumnDefinitionUI<T>> allColumnsDefinition =
                new EntityGridModelFactory<T>(viewContext, staticColumnDefinitions)
                        .createColumnsSchemaForRendering(entity, realNumberFormatingParameters);
        return new BaseEntityModel<T>(entity, allColumnsDefinition);
    }

    /**
     * here we create the columns definition having just one table row. We need them only to render
     * column values (headers have been already created), so no message provider is needed.
     */
    private List<IColumnDefinitionUI<T>> createColumnsSchemaForRendering(GridRowModel<T> rowModel,
            RealNumberFormatingParameters realNumberFormatingParameters)
    {
        List<IColumnDefinitionUI<T>> list = createStaticColumnDefinitions(null);
        for (IEntityProperty prop : rowModel.getOriginalObject().getProperties())
        {
            PropertyType propertyType = prop.getPropertyType();
            EntityPropertyColDef<T> colDef = new EntityPropertyColDef<T>(propertyType, true, null);
            list.add(AbstractPropertyColRenderer.getPropertyColRenderer(viewContext, colDef,
                    realNumberFormatingParameters));
        }
        return list;
    }

    public ColumnDefsAndConfigs<T> createColumnsSchema(IMessageProvider messageProvider,
            EntityType selectedTypeOrNull,
            RealNumberFormatingParameters realNumberFormatingParameters)
    {
        List<PropertyType> propertyTypesOrNull = null;
        if (selectedTypeOrNull != null)
        {
            propertyTypesOrNull = PropertyTypesFilterUtil.extractPropertyTypes(selectedTypeOrNull);
        }
        return createColumnsSchema(messageProvider, propertyTypesOrNull,
                realNumberFormatingParameters);
    }

    public ColumnDefsAndConfigs<T> createColumnsSchema(IMessageProvider messageProvider,
            List<PropertyType> propertyTypesOrNull,
            RealNumberFormatingParameters realNumberFormatingParameters)
    {
        ColumnDefsAndConfigs<T> columns = createStaticColumnDefsAndConfigs(messageProvider);
        if (propertyTypesOrNull != null)
        {
            createPropertyColumnsSchema(columns, propertyTypesOrNull, realNumberFormatingParameters);
        }
        return columns;
    }

    private ColumnDefsAndConfigs<T> createStaticColumnDefsAndConfigs(
            IMessageProvider messageProvider)
    {
        List<IColumnDefinitionUI<T>> commonColumnsSchema =
                createStaticColumnDefinitions(messageProvider);
        return ColumnDefsAndConfigs.create(commonColumnsSchema);
    }

    private List<IColumnDefinitionUI<T>> createStaticColumnDefinitions(
            IMessageProvider msgProviderOrNull)
    {
        return BaseEntityModel.createColumnsDefinition(staticColumnDefinitions, msgProviderOrNull);
    }

    public static <T extends IEntityPropertiesHolder> void createPropertyColumnsSchema(
            ColumnDefsAndConfigs<T> columns, List<PropertyType> propertyTypes,
            RealNumberFormatingParameters realNumberFormatingParameters)
    {
        for (PropertyType propertyType : propertyTypes)
        {
            EntityPropertyColDef<T> def =
                    new EntityPropertyColDef<T>(propertyType, true, propertyTypes);
            GridCellRenderer<BaseEntityModel<?>> renderer = null;
            DataTypeCode dataTypeCode = propertyType.getDataType().getCode();
            if (dataTypeCode == DataTypeCode.REAL)
            {
                renderer = new RealNumberRenderer(realNumberFormatingParameters);
            }
            columns.addColumn(def, renderer);
        }
    }
}
