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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ETPTModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.YesNoRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GridWithRPCProxy;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;

/**
 * {@link GridWithRPCProxy} displaying 'entity type' - 'property type' assignments.
 * 
 * @author Izabela Adamczyk
 */
public class PropertyTypeAssignmentGrid extends GridWithRPCProxy<PropertyType, ETPTModel>
{
    private final CommonViewContext viewContext;

    public PropertyTypeAssignmentGrid(CommonViewContext viewContext, String id)
    {
        super(viewContext, id);
        this.viewContext = viewContext;
    }

    @Override
    protected List<ETPTModel> convert(List<PropertyType> result)
    {
        return ETPTModel.asModels(result);
    }

    @Override
    protected ListPropertyTypesCallback createCallback(IViewContext<?> context,
            AsyncCallback<BaseListLoadResult<ETPTModel>> callback)
    {
        return new ListPropertyTypesCallback(viewContext, callback);
    }

    @Override
    protected ColumnModel createColumnModel(IViewContext<?> context)
    {
        final ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(ColumnConfigFactory.createDefaultColumnConfig(context
                .getMessage(Dict.PROPERTY_TYPE_CODE), ModelDataPropertyNames.PROPERTY_TYPE_CODE));
        configs.add(ColumnConfigFactory.createDefaultColumnConfig(context
                .getMessage(Dict.ASSIGNED_TO), ModelDataPropertyNames.ENTITY_TYPE_CODE));
        configs.add(ColumnConfigFactory.createDefaultColumnConfig(context.getMessage(Dict.TYPE_OF),
                ModelDataPropertyNames.ENTITY_KIND));
        final ColumnConfig mandatory =
                ColumnConfigFactory.createDefaultColumnConfig(
                        context.getMessage(Dict.IS_MANDATORY), ModelDataPropertyNames.IS_MANDATORY);
        mandatory.setRenderer(new YesNoRenderer());
        configs.add(mandatory);
        return new ColumnModel(configs);
    }

    @Override
    protected void loadDataFromService(DelegatingAsyncCallback callback)
    {
        viewContext.getService().listPropertyTypes(callback);
    }

    class ListPropertyTypesCallback extends DelegatingAsyncCallback
    {
        public ListPropertyTypesCallback(IViewContext<?> context,
                AsyncCallback<BaseListLoadResult<ETPTModel>> callback)
        {
            super(context, callback);
        }
    }
}