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

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.renderer.AbstractPropertyColRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.EntityPropertyColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesFilterUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

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

    private final IColumnDefinitionKind<T>[] staticColumnDefinitions;

    public EntityGridModelFactory(IColumnDefinitionKind<T>[] staticColumnDefinitions)
    {
        this.staticColumnDefinitions = staticColumnDefinitions;
    }

    public BaseEntityModel<T> createModel(GridRowModel<T> entity)
    {
        List<IColumnDefinitionUI<T>> allColumnsDefinition =
                new EntityGridModelFactory<T>(staticColumnDefinitions)
                        .createColumnsSchemaForRendering(entity);
        return new BaseEntityModel<T>(entity, allColumnsDefinition);
    }

    /**
     * here we create the columns definition having just one table row. We need them only to render
     * column values (headers have been already created), so no message provider is needed.
     */
    public List<IColumnDefinitionUI<T>> createColumnsSchemaForRendering(GridRowModel<T> rowModel)
    {
        List<IColumnDefinitionUI<T>> list = createStaticColumnDefinitions(null);
        for (IEntityProperty prop : rowModel.getOriginalObject().getProperties())
        {
            PropertyType propertyType = prop.getPropertyType();
            EntityPropertyColDef<T> colDef = new EntityPropertyColDef<T>(propertyType, true, null);
            list.add(AbstractPropertyColRenderer.getPropertyColRenderer(colDef));
        }
        return list;
    }

    public ColumnDefsAndConfigs<T> createColumnsSchema(IMessageProvider messageProvider,
            EntityType selectedTypeOrNull)
    {
        List<PropertyType> propertyTypesOrNull = null;
        if (selectedTypeOrNull != null)
        {
            propertyTypesOrNull = PropertyTypesFilterUtil.extractPropertyTypes(selectedTypeOrNull);
        }
        return createColumnsSchema(messageProvider, propertyTypesOrNull);
    }

    public ColumnDefsAndConfigs<T> createColumnsSchema(IMessageProvider messageProvider,
            List<PropertyType> propertyTypesOrNull)
    {
        ColumnDefsAndConfigs<T> columns = createStaticColumnDefsAndConfigs(messageProvider);
        if (propertyTypesOrNull != null)
        {
            List<IColumnDefinitionUI<T>> propertyColumnsSchema =
                    createPropertyColumnsSchema(propertyTypesOrNull);
            columns.addColumns(propertyColumnsSchema);
        }
        return columns;
    }

    private ColumnDefsAndConfigs<T> createStaticColumnDefsAndConfigs(
            IMessageProvider messageProvider)
    {
        List<IColumnDefinitionUI<T>> commonColumnsSchema =
                createStaticColumnDefinitions(messageProvider);
        ColumnDefsAndConfigs<T> columns = ColumnDefsAndConfigs.create(commonColumnsSchema);
        return columns;
    }

    private List<IColumnDefinitionUI<T>> createStaticColumnDefinitions(
            IMessageProvider msgProviderOrNull)
    {
        return BaseEntityModel.createColumnsDefinition(staticColumnDefinitions, msgProviderOrNull);
    }

    private static <T extends IEntityPropertiesHolder> List<IColumnDefinitionUI<T>> createPropertyColumnsSchema(
            List<PropertyType> propertyTypes)
    {
        List<IColumnDefinitionUI<T>> list = createColDefList();
        for (PropertyType propertyType : propertyTypes)
        {
            list.add(new EntityPropertyColDef<T>(propertyType, true, propertyTypes));
        }
        return list;
    }

    private static <T> ArrayList<IColumnDefinitionUI<T>> createColDefList()
    {
        return new ArrayList<IColumnDefinitionUI<T>>();
    }
}
