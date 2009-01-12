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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.PropertyTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.ETPTRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GridWithRPCProxy;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;

/**
 * {@link GridWithRPCProxy} displaying property types.
 * 
 * @author Izabela Adamczyk
 */
class PropertyTypeGrid extends GridWithRPCProxy<PropertyType, PropertyTypeModel>
{
    private final CommonViewContext viewContext;

    public PropertyTypeGrid(CommonViewContext viewContext, String id)
    {
        super(createColumnModel(viewContext), id);
        this.viewContext = viewContext;
    }

    private static ColumnModel createColumnModel(IViewContext<?> context)
    {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(ColumnConfigFactory.createDefaultColumnConfig(context.getMessage(Dict.LABEL),
                ModelDataPropertyNames.LABEL));
        configs.add(ColumnConfigFactory.createCodeColumnConfig(context));
        configs.add(ColumnConfigFactory.createDefaultColumnConfig(context
                .getMessage(Dict.DATA_TYPE), ModelDataPropertyNames.DATA_TYPE));
        configs.add(ColumnConfigFactory.createDefaultColumnConfig(context
                .getMessage(Dict.VOCABULARY), ModelDataPropertyNames.CONTROLLED_VOCABULARY));
        configs.add(ColumnConfigFactory.createDefaultColumnConfig(context
                .getMessage(Dict.DESCRIPTION), ModelDataPropertyNames.DESCRIPTION));
        configs.add(defineEtptColumn(Dict.SAMPLE_TYPES, ModelDataPropertyNames.SAMPLE_TYPES,
                context));
        configs.add(defineEtptColumn(Dict.EXPERIMENT_TYPES,
                ModelDataPropertyNames.EXPERIMENT_TYPES, context));
        configs.add(defineEtptColumn(Dict.MATERIAL_TYPES, ModelDataPropertyNames.MATERIAL_TYPES,
                context));
        return new ColumnModel(configs);
    }

    @Override
    protected void loadDataFromService(AsyncCallback<BaseListLoadResult<PropertyTypeModel>> callback)
    {
        viewContext.getService().listPropertyTypes(
                new ListPropertyTypesCallback(viewContext, callback));
    }

    class ListPropertyTypesCallback extends DelegatingAsyncCallback
    {
        public ListPropertyTypesCallback(IViewContext<?> context,
                AsyncCallback<BaseListLoadResult<PropertyTypeModel>> callback)
        {
            super(context, callback);
        }

        @Override
        protected List<PropertyTypeModel> convert(List<PropertyType> result)
        {
            return PropertyTypeModel.convert(result);
        }
    }

    private static ColumnConfig defineEtptColumn(String dictCode, String id, IViewContext<?> context)
    {
        final ColumnConfig column =
                ColumnConfigFactory.createDefaultColumnConfig(context.getMessage(dictCode), id);
        column.setRenderer(new ETPTRenderer());
        return column;
    }
}