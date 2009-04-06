/*
 * Copyright 2008 ETH Zuerich, CISD
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

import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.renderer.AbstractPropertyColRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractPropertyColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.material.CommonMaterialColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.material.PropertyMaterialColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialTypePropertyType;

/**
 * {@link ModelData} for {@link Material}
 * 
 * @author Izabela Adamczyk
 */
public final class MaterialModel extends BaseEntityModel<Material>
{
    private static final long serialVersionUID = 1L;

    public MaterialModel(final Material entity)
    {
        super(entity, createColumnsSchemaForRendering(entity));
    }

    // here we create the columns definition having just one table row. We need them only to render
    // column values (headers have been already created), so no message provider is needed.
    private static List<IColumnDefinitionUI<Material>> createColumnsSchemaForRendering(
            Material entity)
    {
        List<IColumnDefinitionUI<Material>> list = createCommonColumnsSchema(null);
        for (MaterialProperty prop : entity.getProperties())
        {
            MaterialTypePropertyType etpt = prop.getEntityTypePropertyType();
            AbstractPropertyColDef<Material> colDef =
                    new PropertyMaterialColDef(etpt.getPropertyType());
            list.add(AbstractPropertyColRenderer.getPropertyColRenderer(colDef));
        }
        return list;
    }

    public static ColumnDefsAndConfigs<Material> createColumnsSchema(
            IMessageProvider messageProvider, MaterialType selectedTypeOrNull)
    {
        List<IColumnDefinitionUI<Material>> commonColumnsSchema =
                createCommonColumnsSchema(messageProvider);
        ColumnDefsAndConfigs<Material> columns = ColumnDefsAndConfigs.create(commonColumnsSchema);
        if (selectedTypeOrNull != null)
        {
            List<IColumnDefinitionUI<Material>> propertyColumnsSchema =
                    createPropertyColumnsSchema(selectedTypeOrNull);
            columns.addColumns(propertyColumnsSchema);
        }
        return columns;
    }

    private static List<IColumnDefinitionUI<Material>> createPropertyColumnsSchema(
            MaterialType selectedType)
    {
        List<MaterialTypePropertyType> entityTypePropertyTypes =
                selectedType.getMaterialTypePropertyTypes();
        List<IColumnDefinitionUI<Material>> list = createColDefList();
        for (MaterialTypePropertyType etpt : entityTypePropertyTypes)
        {
            list.add(new PropertyMaterialColDef(etpt.getPropertyType()));
        }
        return list;
    }

    private static List<IColumnDefinitionUI<Material>> createCommonColumnsSchema(
            IMessageProvider msgProviderOrNull)
    {
        return createColumnsDefinition(CommonMaterialColDefKind.values(), msgProviderOrNull);
    }

    private static ArrayList<IColumnDefinitionUI<Material>> createColDefList()
    {
        return new ArrayList<IColumnDefinitionUI<Material>>();
    }
}
