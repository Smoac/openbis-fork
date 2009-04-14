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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import com.extjs.gxt.ui.client.data.BaseListLoadConfig;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
abstract class AbstractExperimentTableSection<T extends ModelData> extends SectionPanel
{
    protected final IViewContext<?> viewContext;

    protected final Experiment experiment;
    
    private final Grid<T> grid;

    public AbstractExperimentTableSection(final Experiment experiment,
            final IViewContext<?> viewContext, String sectionHeader, String idPrefix)
    {
        super(sectionHeader);
        this.experiment = experiment;
        this.viewContext = viewContext;
        final ListLoader<BaseListLoadConfig> loader = createListLoader(createRpcProxy());
        final ListStore<T> listStore = new ListStore<T>(loader);
        grid = createGrid(listStore);
        grid.setId(idPrefix + experiment.getIdentifier());
        grid.setLoadMask(true);
        setLayout(new RowLayout());
        add(grid, new RowData(-1, 200));
    }
    
    protected Grid<T> getGrid()
    {
        return grid;
    }

    protected Grid<T> createGrid(final ListStore<T> listStore)
    {
        return new Grid<T>(listStore, createColumnModel());
    }
    
    @Override
    protected void onAttach()
    {
        super.onAttach();
        grid.getStore().getLoader().load();
    }

    protected abstract ColumnModel createColumnModel();
    
    protected abstract RpcProxy<BaseListLoadConfig, BaseListLoadResult<T>> createRpcProxy();
    
    private final ListLoader<BaseListLoadConfig> createListLoader(
            final RpcProxy<BaseListLoadConfig, BaseListLoadResult<T>> rpcProxy)
    {
        final BaseListLoader<BaseListLoadConfig, BaseListLoadResult<T>> baseListLoader =
                new BaseListLoader<BaseListLoadConfig, BaseListLoadResult<T>>(rpcProxy);
        return baseListLoader;
    }
    
}
