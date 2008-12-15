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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.VoidAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExperimentModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.YesNoRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.InvalidableWithCodeRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.PagingToolBarWithoutRefresh;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GxtTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;

/**
 * A {@link LayoutContainer} which contains the grid where the experiments are displayed.
 * 
 * @author Christian Ribeaud
 * @author Izabela Adamczyk
 */
public final class ExperimentBrowserGrid extends LayoutContainer
{
    private static final int PAGE_SIZE = 50;

    private static final String PREFIX = "experiment-browser-grid_";

    public static final String GRID_ID = GenericConstants.ID_PREFIX + PREFIX + "grid";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private ContentPanel contentPanel;

    private Grid<ExperimentModel> grid;

    private final PagingLoader<PagingLoadConfig> experimentLoader;

    private ListExperimentsCriteria criteria;

    private String resultSetKey;

    ExperimentBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
        experimentLoader = createExperimentLoader();
        createUI();
    }

    private final void createUI()
    {
        setLayout(new FitLayout());
        contentPanel = createContentPanel();
        grid = createGrid();
        contentPanel.add(grid);
        final PagingToolBar toolBar = createPagingToolBar();
        toolBar.bind(experimentLoader);
        contentPanel.setBottomComponent(toolBar);
        add(contentPanel);
    }

    private final PagingLoader<PagingLoadConfig> createExperimentLoader()
    {
        final RpcProxy<PagingLoadConfig, PagingLoadResult<ExperimentModel>> proxy = createProxy();
        final BasePagingLoader<PagingLoadConfig, PagingLoadResult<ExperimentModel>> pagingLoader =
                new BasePagingLoader<PagingLoadConfig, PagingLoadResult<ExperimentModel>>(proxy);
        pagingLoader.setRemoteSort(true);
        return pagingLoader;
    }

    private final RpcProxy<PagingLoadConfig, PagingLoadResult<ExperimentModel>> createProxy()
    {
        return new RpcProxy<PagingLoadConfig, PagingLoadResult<ExperimentModel>>()
            {

                //
                // RpcProxy
                //

                @Override
                public final void load(final PagingLoadConfig loadConfig,
                        final AsyncCallback<PagingLoadResult<ExperimentModel>> callback)
                {
                    final int offset = loadConfig.getOffset();
                    criteria.setLimit(loadConfig.getLimit());
                    criteria.setOffset(offset);
                    criteria.setSortInfo(GxtTranslator.translate(loadConfig.getSortInfo()));
                    criteria.setResultSetKey(resultSetKey);
                    viewContext.getService().listExperiments(criteria,
                            new ListExperimentsCallback(viewContext, callback, offset));
                }

            };
    }

    private final static ContentPanel createContentPanel()
    {
        final ContentPanel contentPanel = new ContentPanel();
        contentPanel.setBorders(false);
        contentPanel.setBodyBorder(false);
        contentPanel.setLayout(new FitLayout());
        return contentPanel;
    }

    private final Grid<ExperimentModel> createGrid()
    {
        final Grid<ExperimentModel> experimentGrid =
                new Grid<ExperimentModel>(new ListStore<ExperimentModel>(experimentLoader),
                        new ColumnModel(new ArrayList<ColumnConfig>()));
        experimentGrid.setId(GRID_ID);
        experimentGrid.setLoadMask(true);
        experimentGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        experimentGrid.addListener(Events.CellDoubleClick, new Listener<GridEvent>()
            {

                //
                // Listener
                //

                public final void handleEvent(final GridEvent be)
                {
                    final ExperimentModel experimentModel =
                            (ExperimentModel) be.grid.getStore().getAt(be.rowIndex);
                    final ExperimentType experimentType =
                            (ExperimentType) experimentModel
                                    .get(ModelDataPropertyNames.EXPERIMENT_TYPE);
                    final String experimentIdentifier =
                            experimentModel.get(ModelDataPropertyNames.EXPERIMENT_IDENTIFIER);
                    final String code = experimentType.getCode();
                    final ITabItem tabView =
                            viewContext.getClientPluginFactoryProvider().getClientPluginFactory(
                                    code).createViewClientForExperimentType(code)
                                    .createExperimentViewer(experimentIdentifier);
                    Dispatcher.get().dispatch(DispatcherHelper.createNaviEvent(tabView));
                }
            });
        return experimentGrid;
    }

    private final static PagingToolBar createPagingToolBar()
    {
        return new PagingToolBarWithoutRefresh(PAGE_SIZE);
    }

    private final ColumnModel createColumnModel()
    {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        final ColumnConfig codeColumn = ColumnConfigFactory.createCodeColumnConfig(viewContext);
        codeColumn.setRenderer(new InvalidableWithCodeRenderer());
        configs.add(codeColumn);

        configs.add(ColumnConfigFactory.createDefaultColumnConfig(viewContext
                .getMessage(Dict.EXPERIMENT_TYPE),
                ModelDataPropertyNames.EXPERIMENT_TYPE_CODE_FOR_EXPERIMENT));
        configs.add(ColumnConfigFactory.createDefaultColumnConfig(viewContext
                .getMessage(Dict.GROUP), ModelDataPropertyNames.GROUP_FOR_EXPERIMENT));
        configs.add(ColumnConfigFactory.createDefaultColumnConfig(viewContext
                .getMessage(Dict.PROJECT), ModelDataPropertyNames.PROJECT));

        configs.add(ColumnConfigFactory.createRegistratorColumnConfig(viewContext));
        configs.add(ColumnConfigFactory.createRegistrationDateColumnConfig(viewContext));
        final ColumnConfig isInvalidColumn =
                ColumnConfigFactory.createDefaultColumnConfig(viewContext
                        .getMessage(Dict.IS_INVALID), ModelDataPropertyNames.IS_INVALID);
        isInvalidColumn.setRenderer(new YesNoRenderer());
        configs.add(isInvalidColumn);
        return new ColumnModel(configs);
    }

    private final String createHeader(final ExperimentType experimentType,
            final Project selectedProject)
    {
        final StringBuilder builder = new StringBuilder("Experiments");
        builder.append(" of type ");
        builder.append(experimentType.getCode());
        builder.append(" belonging to the project ");
        builder.append(selectedProject.getCode());
        builder.append(" from group ");
        builder.append(selectedProject.getGroup().getCode());
        return builder.toString();
    }

    final void refresh(final ExperimentType selectedType, final Project selectedProject)
    {
        if (resultSetKey != null)
        {
            viewContext.getService().removeResultSet(resultSetKey,
                    new VoidAsyncCallback<Void>(viewContext));
            resultSetKey = null;
        }
        criteria = new ListExperimentsCriteria();
        criteria.setExperimentType(selectedType);
        criteria.setProjectCode(selectedProject.getCode());
        criteria.setGroupCode(selectedProject.getGroup().getCode());
        contentPanel.setHeading(createHeader(selectedType, selectedProject));
        grid.reconfigure(grid.getStore(), createColumnModel());
        GWTUtils.setAutoExpandOnLastVisibleColumn(grid);
        experimentLoader.load(0, PAGE_SIZE);
    }

    /**
     * Returns the result set key.
     */
    final String getResultSetKey()
    {
        return resultSetKey;
    }

    /**
     * Returns the current grid column model.
     */
    final ColumnModel getColumnModel()
    {
        return grid.getColumnModel();
    }

    //
    // Helper classes
    //

    public final class ListExperimentsCallback extends AbstractAsyncCallback<ResultSet<Experiment>>
    {
        private final AsyncCallback<PagingLoadResult<ExperimentModel>> delegate;

        private final int offset;

        ListExperimentsCallback(final IViewContext<ICommonClientServiceAsync> viewContext,
                final AsyncCallback<PagingLoadResult<ExperimentModel>> delegate, final int offset)
        {
            super(viewContext);
            this.delegate = delegate;
            this.offset = offset;
        }

        private void setResultSetKey(final String resultSetKey)
        {
            if (ExperimentBrowserGrid.this.resultSetKey == null)
            {
                ExperimentBrowserGrid.this.resultSetKey = resultSetKey;
            } else
            {
                assert ExperimentBrowserGrid.this.resultSetKey.equals(resultSetKey) : "Result set keys not the same.";
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
        protected final void process(final ResultSet<Experiment> result)
        {
            setResultSetKey(result.getResultSetKey());
            final List<ExperimentModel> experimentModels =
                    ExperimentModel.asExperimentModels(result.getList());
            final PagingLoadResult<ExperimentModel> loadResult =
                    new BasePagingLoadResult<ExperimentModel>(experimentModels, offset, result
                            .getTotalLength());
            delegate.onSuccess(loadResult);
        }
    }

}
