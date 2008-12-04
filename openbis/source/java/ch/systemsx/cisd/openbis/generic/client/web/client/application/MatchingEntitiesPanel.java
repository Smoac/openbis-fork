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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.XDOM;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.PagingToolBar;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.MatchingEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.PagingToolBarWithoutRefresh;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GxtTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;

/**
 * A {@link LayoutContainer} extension which displays the matching entities.
 * 
 * @author Christian Ribeaud
 */
final class MatchingEntitiesPanel extends ContentPanel implements Listener<TabPanelEvent>
{
    static final int PAGE_SIZE = 50;

    static final String PREFIX = GenericConstants.ID_PREFIX + "matching-entities-panel_";

    static final String GRID_ID = PREFIX + "grid";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final PagingLoader<PagingLoadConfig> matchingEntitiesLoader;

    private final SearchableEntity searchableEntity;

    private final String queryText;

    private String resultSetKey;

    private ResultSet<MatchingEntity> firstResultSet;

    MatchingEntitiesPanel(final IViewContext<ICommonClientServiceAsync> viewContext,
            final SearchableEntity searchableEntity, final String queryText)
    {
        this.viewContext = viewContext;
        this.searchableEntity = searchableEntity;
        this.queryText = queryText;
        matchingEntitiesLoader = createMatchingEntitiesLoader();
        setId(PREFIX + XDOM.getUniqueId());
        setLayout(new FitLayout());
        add(createGrid());
        final PagingToolBar toolBar = createPagingToolBar();
        toolBar.bind(matchingEntitiesLoader);
        setHeaderVisible(false);
        setBottomComponent(toolBar);
    }

    private final PagingLoader<PagingLoadConfig> createMatchingEntitiesLoader()
    {
        final RpcProxy<PagingLoadConfig, PagingLoadResult<MatchingEntityModel>> proxy =
                createRpcProxy();
        final BasePagingLoader<PagingLoadConfig, PagingLoadResult<MatchingEntityModel>> pagingLoader =
                new BasePagingLoader<PagingLoadConfig, PagingLoadResult<MatchingEntityModel>>(proxy);
        pagingLoader.setRemoteSort(true);
        return pagingLoader;
    }

    private final RpcProxy<PagingLoadConfig, PagingLoadResult<MatchingEntityModel>> createRpcProxy()
    {
        return new RpcProxy<PagingLoadConfig, PagingLoadResult<MatchingEntityModel>>()
            {

                //
                // RpcProxy
                //

                @Override
                public final void load(final PagingLoadConfig loadConfig,
                        final AsyncCallback<PagingLoadResult<MatchingEntityModel>> callback)
                {
                    final int offset = loadConfig.getOffset();
                    final ListMatchingEntitiesCallback listMatchingEntitiesCallback =
                            new ListMatchingEntitiesCallback(viewContext, callback, offset);
                    if (firstResultSet != null)
                    {
                        listMatchingEntitiesCallback.onSuccess(firstResultSet);
                        firstResultSet = null;
                        return;
                    }
                    final DefaultResultSetConfig<String> resultSetConfig =
                            new DefaultResultSetConfig<String>();
                    resultSetConfig.setOffset(offset);
                    resultSetConfig.setLimit(loadConfig.getLimit());
                    resultSetConfig.setSortInfo(GxtTranslator.translate(loadConfig.getSortInfo()));
                    resultSetConfig.setResultSetKey(resultSetKey);
                    viewContext.getService().listMatchingEntities(searchableEntity, queryText,
                            resultSetConfig, listMatchingEntitiesCallback);
                }

            };
    }

    private final Grid<MatchingEntityModel> createGrid()
    {
        final ColumnModel columnModel = createColumnModel();

        final Grid<MatchingEntityModel> grid =
                new Grid<MatchingEntityModel>(new ListStore<MatchingEntityModel>(
                        matchingEntitiesLoader), columnModel);
        grid.setId(GRID_ID);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid.setLoadMask(true);
        grid.addListener(Events.CellDoubleClick, new Listener<GridEvent>()
            {

                //
                // Listener
                //

                public final void handleEvent(final GridEvent be)
                {
                    final MatchingEntityModel matchingEntityModel =
                            (MatchingEntityModel) be.grid.getStore().getAt(be.rowIndex);
                    final MatchingEntity matchingEntity =
                            (MatchingEntity) matchingEntityModel.get(ModelDataPropertyNames.OBJECT);
                    final String identifier =
                            matchingEntityModel.get(ModelDataPropertyNames.IDENTIFIER);
                    final String typeCode = matchingEntity.getEntityType().getCode();
                    if (matchingEntity.getEntityKind() == EntityKind.SAMPLE)
                    {
                        final ITabItem tabView =
                                viewContext.getClientPluginFactoryProvider()
                                        .getClientPluginFactory(typeCode)
                                        .createViewClientForSampleType(typeCode)
                                        .createSampleViewer(identifier);
                        Dispatcher.get().dispatch(DispatcherHelper.createNaviEvent(tabView));
                    }
                }
            });
        return grid;
    }

    private final ColumnModel createColumnModel()
    {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(createEntityKindColumnConfig());
        configs.add(createEntityTypeColumnConfig());
        configs.add(createIdentifierColumnConfig());
        configs.add(ColumnConfigFactory.createRegistratorColumnConfig(viewContext
                .getMessageProvider()));
        configs.add(createMatchedFieldColumnConfig());
        // configs.add(createMatchedTextColumnConfig());
        final ColumnModel columnModel = new ColumnModel(configs);
        return columnModel;
    }

    // private final ColumnConfig createMatchedTextColumnConfig()
    // {
    // return createColumnConfig(ModelDataPropertyNames.MATCHING_TEXT, "matching_text");
    // }

    private final ColumnConfig createMatchedFieldColumnConfig()
    {
        ColumnConfig config = createColumnConfig(ModelDataPropertyNames.MATCHING_FIELD, "matching_field");
        config.setWidth(140);
        return config;
    }

    private final ColumnConfig createEntityKindColumnConfig()
    {
        return createColumnConfig(ModelDataPropertyNames.ENTITY_KIND, "entity_kind");
    }

    private final ColumnConfig createIdentifierColumnConfig()
    {
        final ColumnConfig columnConfig =
                ColumnConfigFactory.createDefaultConfig(viewContext.getMessageProvider(),
                        ModelDataPropertyNames.IDENTIFIER);
        columnConfig.setWidth(140);
        return columnConfig;
    }

    private final ColumnConfig createEntityTypeColumnConfig()
    {
        return createColumnConfig(ModelDataPropertyNames.ENTITY_TYPE, "entity_type");
    }

    private ColumnConfig createColumnConfig(final String id, final String headerKey)
    {
        return ColumnConfigFactory.createDefaultConfig(viewContext.getMessageProvider(), id,
                headerKey);
    }

    private final static PagingToolBar createPagingToolBar()
    {
        return new PagingToolBarWithoutRefresh(PAGE_SIZE);
    }

    /**
     * Sets the first result set loaded.
     */
    final void setFirstResulSet(final ResultSet<MatchingEntity> resultSet)
    {
        firstResultSet = resultSet;
        matchingEntitiesLoader.load(0, PAGE_SIZE);
    }

    //
    // Listener
    //

    public final void handleEvent(final TabPanelEvent be)
    {
        if (be.type == Events.Close && resultSetKey != null)
        {
            viewContext.getService().removeResultSet(resultSetKey,
                    new VoidAsyncCallback<Void>(viewContext));
        }
    }

    //
    // Helper classes
    //

    private final class ListMatchingEntitiesCallback extends
            AbstractAsyncCallback<ResultSet<MatchingEntity>>
    {
        private final AsyncCallback<PagingLoadResult<MatchingEntityModel>> delegate;

        private final int offset;

        ListMatchingEntitiesCallback(final IViewContext<ICommonClientServiceAsync> viewContext,
                final AsyncCallback<PagingLoadResult<MatchingEntityModel>> delegate,
                final int offset)
        {
            super(viewContext);
            this.delegate = delegate;
            this.offset = offset;
        }

        private void setResultSetKey(final String resultSetKey)
        {
            if (MatchingEntitiesPanel.this.resultSetKey == null)
            {
                MatchingEntitiesPanel.this.resultSetKey = resultSetKey;
            } else
            {
                assert MatchingEntitiesPanel.this.resultSetKey.equals(resultSetKey) : "Result set keys not the same.";
            }
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        protected final void finishOnFailure(final Throwable caught)
        {
            delegate.onFailure(caught);
        }

        @Override
        protected final void process(final ResultSet<MatchingEntity> result)
        {
            setResultSetKey(result.getResultSetKey());
            final List<MatchingEntityModel> matchingEntities =
                    MatchingEntityModel.convert(result.getList());
            final PagingLoadResult<MatchingEntityModel> loadResult =
                    new BasePagingLoadResult<MatchingEntityModel>(matchingEntities, offset, result
                            .getTotalLength());
            delegate.onSuccess(loadResult);
        }
    }

}
