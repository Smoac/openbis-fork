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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.List;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseListLoadConfig;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;

/**
 * Grid with RPCProxy Loader.
 * 
 * @author Izabela Adamczyk
 */
public abstract class GridWithRPCProxy<M, T extends ModelData> extends Grid<T>
{

    private final IViewContext<?> viewContext;

    public GridWithRPCProxy(IViewContext<?> viewContext, String idPrefix)
    {
        super(null, null);
        this.viewContext = viewContext;
        ListLoader<BaseListLoadConfig> loader =
                new BaseListLoader<BaseListLoadConfig, BaseListLoadResult<T>>(createRpcProxy());
        reconfigure(new ListStore<T>(loader), createColumnModel(viewContext));
        setView(new GridView());
        getView().setForceFit(true);
        setLoadMask(true);
        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        setId(idPrefix + "_grid");
    }

    public void load()
    {
        getStore().getLoader().load();
    }

    protected abstract ColumnModel createColumnModel(IViewContext<?> context);

    private final RpcProxy<BaseListLoadConfig, BaseListLoadResult<T>> createRpcProxy()
    {
        return new RpcProxy<BaseListLoadConfig, BaseListLoadResult<T>>()
            {

                @Override
                public final void load(final BaseListLoadConfig loadConfig,
                        final AsyncCallback<BaseListLoadResult<T>> callback)
                {
                    loadDataFromService(createCallback(viewContext, callback));
                }

            };
    }

    abstract protected DelegatingAsyncCallback createCallback(IViewContext<?> context,
            AsyncCallback<BaseListLoadResult<T>> callback);

    abstract protected void loadDataFromService(DelegatingAsyncCallback callback);

    abstract protected List<T> convert(List<M> result);

    protected class DelegatingAsyncCallback extends AbstractAsyncCallback<List<M>>
    {

        private final AsyncCallback<BaseListLoadResult<T>> delegate;

        public DelegatingAsyncCallback(IViewContext<?> viewContext,
                final AsyncCallback<BaseListLoadResult<T>> callback)
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
        protected final void process(final List<M> result)
        {
            delegate.onSuccess(new BaseListLoadResult<T>(convert(result)));
        }

    }
}
