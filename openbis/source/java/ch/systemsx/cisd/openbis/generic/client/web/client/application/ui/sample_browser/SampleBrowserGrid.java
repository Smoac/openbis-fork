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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser;

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

import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.VoidAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ParentColumns;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyColumns;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SortInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SortInfo.SortDir;

/**
 * A {@link LayoutContainer} which contains the grid where the samples are displayed.
 * 
 * @author Christian Ribeaud
 */
final class SampleBrowserGrid extends LayoutContainer
{
    private static final int PAGE_SIZE = 50;

    private static final String PREFIX = "sample-browser-grid_";

    static final String GRID_ID = GenericConstants.ID_PREFIX + PREFIX + "grid";

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    private final CommonColumns commonColumns;

    private final ParentColumns parentColumns;

    private final PropertyColumns propertyColumns;

    private ContentPanel contentPanel;

    private Grid<SampleModel> grid;

    private final PagingLoader<PagingLoadConfig> sampleLoader;

    private ListSampleCriteria criteria;

    private String resultSetKey;

    SampleBrowserGrid(final IViewContext<IGenericClientServiceAsync> viewContext,
            final CommonColumns commonColumns, final ParentColumns parentColumns,
            final PropertyColumns propertyColumns)
    {
        this.viewContext = viewContext;
        this.commonColumns = commonColumns;
        this.parentColumns = parentColumns;
        this.propertyColumns = propertyColumns;
        sampleLoader = createSampleLoader();
        createUI();
    }

    private final void createUI()
    {
        setLayout(new FitLayout());
        contentPanel = createContentPanel();
        grid = createGrid();
        contentPanel.add(grid);
        final PagingToolBar toolBar = createPagingToolBar();
        toolBar.bind(sampleLoader);
        contentPanel.setBottomComponent(toolBar);
        add(contentPanel);
    }

    private final PagingLoader<PagingLoadConfig> createSampleLoader()
    {
        final RpcProxy<PagingLoadConfig, PagingLoadResult<SampleModel>> proxy = createSampleProxy();
        final BasePagingLoader<PagingLoadConfig, PagingLoadResult<SampleModel>> pagingLoader =
                new BasePagingLoader<PagingLoadConfig, PagingLoadResult<SampleModel>>(proxy);
        pagingLoader.setRemoteSort(true);
        return pagingLoader;
    }

    private final RpcProxy<PagingLoadConfig, PagingLoadResult<SampleModel>> createSampleProxy()
    {
        return new RpcProxy<PagingLoadConfig, PagingLoadResult<SampleModel>>()
            {

                private final SortInfo translate(
                        final com.extjs.gxt.ui.client.data.SortInfo sortInfo)
                {
                    final SortInfo result = new SortInfo();
                    result.setSortField(sortInfo.getSortField());
                    result.setSortDir(SortDir.valueOf(sortInfo.getSortDir().name()));
                    return result;
                }

                //
                // RpcProxy
                //

                @Override
                public final void load(final PagingLoadConfig loadConfig,
                        final AsyncCallback<PagingLoadResult<SampleModel>> callback)
                {
                    final int offset = loadConfig.getOffset();
                    criteria.setLimit(loadConfig.getLimit());
                    criteria.setOffset(offset);
                    criteria.setSortInfo(translate(loadConfig.getSortInfo()));
                    criteria.setResultSetKey(resultSetKey);
                    viewContext.getService().listSamples(criteria,
                            new ListSamplesCallback(viewContext, callback, offset));
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

    private final Grid<SampleModel> createGrid()
    {
        final Grid<SampleModel> sampleGrid =
                new Grid<SampleModel>(new ListStore<SampleModel>(sampleLoader), new ColumnModel(
                        new ArrayList<ColumnConfig>()));
        sampleGrid.setId(GRID_ID);
        sampleGrid.setLoadMask(true);
        sampleGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        sampleGrid.addListener(Events.CellDoubleClick, new Listener<GridEvent>()
            {

                //
                // Listener
                //

                public final void handleEvent(final GridEvent be)
                {
                    final SampleModel sampleModel =
                            (SampleModel) be.grid.getStore().getAt(be.rowIndex);
                    final SampleType sampleType =
                            (SampleType) sampleModel.get(ModelDataPropertyNames.SAMPLE_TYPE);
                    final String sampleIdentifier =
                            sampleModel.get(ModelDataPropertyNames.SAMPLE_IDENTIFIER);
                    final String code = sampleType.getCode();
                    final ITabItem tabView =
                            viewContext.getClientPluginFactoryProvider().getClientPluginFactory(
                                    code).createViewClientForSampleType(code).createSampleViewer(
                                    sampleIdentifier);
                    Dispatcher.get().dispatch(DispatcherHelper.createNaviEvent(tabView));
                }
            });
        return sampleGrid;
    }

    private final static PagingToolBar createPagingToolBar()
    {
        return new PagingToolBarWithoutRefresh(PAGE_SIZE);
    }

    private final ColumnModel createColumnModel()
    {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.addAll(commonColumns.getColumns());
        configs.addAll(parentColumns.getColumns());
        configs.addAll(propertyColumns.getColumns());
        return new ColumnModel(configs);
    }

    private final String createHeader(final SampleType sampleType, final String selectedGroupCode,
            final boolean showGroup, final boolean showInstance)
    {
        final StringBuilder builder = new StringBuilder("Samples");
        builder.append(" of type ");
        builder.append(sampleType.getCode());
        if (showGroup)
        {
            builder.append(" belonging to the group ");
            builder.append(selectedGroupCode);
        }
        if (showInstance)
        {
            if (showGroup)
            {
                builder.append(" or shared");
            } else
            {
                builder.append(" which are shared among all the groups");
            }
        }
        return builder.toString();
    }

    /**
     * Refreshes the sample browser grid up to given parameters.
     * <p>
     * Note that, doing so, the result set associated with this <code>resultSetKey</code> on the
     * server side will be refreshed as well.
     * </p>
     */
    final void refresh(final SampleType selectedType, final String selectedGroupCode,
            final Boolean includeGroup, final Boolean includeInstance)
    {
        if (resultSetKey != null)
        {
            viewContext.getService().removeResultSet(resultSetKey,
                    new VoidAsyncCallback<Void>(viewContext));
            resultSetKey = null;
        }
        criteria = new ListSampleCriteria();
        criteria.setSampleType(selectedType);
        criteria.setGroupCode(selectedGroupCode);
        criteria.setIncludeGroup(includeGroup);
        criteria.setIncludeInstance(includeInstance);
        contentPanel.setHeading(createHeader(selectedType, selectedGroupCode, includeGroup,
                includeInstance));
        grid.reconfigure(grid.getStore(), createColumnModel());
        sampleLoader.load(0, PAGE_SIZE);
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

    final class ListSamplesCallback extends AbstractAsyncCallback<ResultSet<Sample>>
    {
        private final AsyncCallback<PagingLoadResult<SampleModel>> delegate;

        private final int offset;

        ListSamplesCallback(final IViewContext<IGenericClientServiceAsync> viewContext,
                final AsyncCallback<PagingLoadResult<SampleModel>> delegate, final int offset)
        {
            super(viewContext);
            this.delegate = delegate;
            this.offset = offset;
        }

        private void setResultSetKey(final String resultSetKey)
        {
            if (SampleBrowserGrid.this.resultSetKey == null)
            {
                SampleBrowserGrid.this.resultSetKey = resultSetKey;
            } else
            {
                assert SampleBrowserGrid.this.resultSetKey.equals(resultSetKey) : "Result set keys not the same.";
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
        protected final void process(final ResultSet<Sample> result)
        {
            setResultSetKey(result.getResultSetKey());
            final List<SampleModel> sampleModels = SampleModel.asSampleModels(result.getList());
            final PagingLoadResult<SampleModel> loadResult =
                    new BasePagingLoadResult<SampleModel>(sampleModels, offset, result
                            .getTotalLength());
            delegate.onSuccess(loadResult);
        }
    }

}
