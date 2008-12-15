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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseListLoadConfig;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ETPTModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.YesNoRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnFilter;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;

/**
 * Encapsulates property type assignments listing functionality.
 * 
 * @author Izabela Adamczyk
 */
public class PropertyTypeAssignmentBrowser extends AbstractViewer<ICommonClientServiceAsync>
{
    public static final String ID =
            GenericConstants.ID_PREFIX + "property-type-assignments-browser";

    public static final String GRID_ID = ID + "_grid";

    private Grid<ETPTModel> grid;

    private ContentPanel panel;

    private ToolBar toolbar;

    public PropertyTypeAssignmentBrowser(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext);
        setId(ID);
        setLayout(new FitLayout());
        add(getPanel());
    }

    private ContentPanel getPanel()
    {
        if (panel == null)
        {
            panel = new ContentPanel();
            panel.setLayout(new FitLayout());
            panel.add(getGrid());
            panel.setBottomComponent(getToolbar());
        }
        return panel;
    }

    private ToolBar getToolbar()
    {
        if (toolbar == null)
        {
            toolbar = new ToolBar();
            toolbar.add(new LabelToolItem("Filter:"));
            Store<ETPTModel> store = getGrid().getStore();
            toolbar.add(new AdapterToolItem(new ColumnFilter<ETPTModel>(store,
                    ModelDataPropertyNames.PROPERTY_TYPE_CODE, viewContext
                            .getMessage(Dict.PROPERTY_TYPE_CODE))));
            toolbar.add(new AdapterToolItem(new ColumnFilter<ETPTModel>(store,
                    ModelDataPropertyNames.ENTITY_TYPE_CODE, viewContext
                            .getMessage(Dict.ASSIGNED_TO))));
            toolbar.add(new AdapterToolItem(new ColumnFilter<ETPTModel>(store,
                    ModelDataPropertyNames.ENTITY_KIND, viewContext.getMessage(Dict.TYPE_OF))));
        }
        return toolbar;
    }

    private Grid<ETPTModel> getGrid()
    {
        if (grid == null)
        {
            final ListLoader<BaseListLoadConfig> loader = createListLoader(createRpcProxy());
            final ListStore<ETPTModel> listStore = new ListStore<ETPTModel>(loader);
            grid = new Grid<ETPTModel>(listStore, createColumnModel());
            grid.setId(GRID_ID);
            grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            grid.setLoadMask(true);
        }
        return grid;
    }

    private ColumnModel createColumnModel()
    {
        final ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        configs.add(ColumnConfigFactory.createDefaultColumnConfig(viewContext
                .getMessage(Dict.PROPERTY_TYPE_CODE), ModelDataPropertyNames.PROPERTY_TYPE_CODE));

        configs.add(ColumnConfigFactory.createDefaultColumnConfig(viewContext
                .getMessage(Dict.ASSIGNED_TO), ModelDataPropertyNames.ENTITY_TYPE_CODE));

        configs.add(ColumnConfigFactory.createDefaultColumnConfig(viewContext
                .getMessage(Dict.TYPE_OF), ModelDataPropertyNames.ENTITY_KIND));

        final ColumnConfig mandatory =
                ColumnConfigFactory.createDefaultColumnConfig(viewContext
                        .getMessage(Dict.IS_MANDATORY), ModelDataPropertyNames.IS_MANDATORY);
        mandatory.setRenderer(new YesNoRenderer());
        configs.add(mandatory);
        return new ColumnModel(configs);
    }

    @Override
    public void loadData()
    {
        grid.getStore().getLoader().load();
    }

    private final <T> ListLoader<BaseListLoadConfig> createListLoader(
            final RpcProxy<BaseListLoadConfig, BaseListLoadResult<T>> rpcProxy)
    {
        final BaseListLoader<BaseListLoadConfig, BaseListLoadResult<T>> baseListLoader =
                new BaseListLoader<BaseListLoadConfig, BaseListLoadResult<T>>(rpcProxy);
        return baseListLoader;
    }

    private final RpcProxy<BaseListLoadConfig, BaseListLoadResult<ETPTModel>> createRpcProxy()
    {
        return new RpcProxy<BaseListLoadConfig, BaseListLoadResult<ETPTModel>>()
            {
                @Override
                public final void load(final BaseListLoadConfig loadConfig,
                        final AsyncCallback<BaseListLoadResult<ETPTModel>> callback)
                {
                    viewContext.getService().listPropertyTypes(
                            new ListPropertyTypesCallback(viewContext, callback));
                }
            };
    }

    class ListPropertyTypesCallback extends AbstractAsyncCallback<List<PropertyType>>
    {

        private final AsyncCallback<BaseListLoadResult<ETPTModel>> delegate;

        public ListPropertyTypesCallback(IViewContext<ICommonClientServiceAsync> viewContext,
                AsyncCallback<BaseListLoadResult<ETPTModel>> callback)
        {
            super(viewContext);
            this.delegate = callback;
        }

        @Override
        protected void finishOnFailure(final Throwable caught)
        {
            delegate.onFailure(caught);
        }

        @Override
        protected void process(List<PropertyType> result)
        {
            final List<ETPTModel> models = ETPTModel.asModels(result);
            final BaseListLoadResult<ETPTModel> baseListLoadResult =
                    new BaseListLoadResult<ETPTModel>(models);
            delegate.onSuccess(baseListLoadResult);
        }
    }

}
