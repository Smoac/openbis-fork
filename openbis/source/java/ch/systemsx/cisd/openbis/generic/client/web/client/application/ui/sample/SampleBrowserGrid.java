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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.VoidAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.AbstractColumnsConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.SampleModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.PagingToolBarWithoutRefresh;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GxtTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SortInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;

/**
 * A {@link LayoutContainer} which contains the grid where the samples are displayed.
 * 
 * @author Christian Ribeaud
 */
public final class SampleBrowserGrid extends LayoutContainer
{
    private static final int PAGE_SIZE = 50;

    private static final String PREFIX = "sample-browser-grid_";

    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + PREFIX + "sample_browser";

    public static final String GRID_ID = GenericConstants.ID_PREFIX + PREFIX + "grid";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final AbstractColumnsConfig commonColumns;

    private final AbstractColumnsConfig parentColumns;

    private final AbstractColumnsConfig propertyColumns;

    private final PagingLoader<PagingLoadConfig> sampleLoader;

    private ContentPanel contentPanel;

    private Grid<SampleModel> grid;

    private ListSampleCriteria criteria;

    private String resultSetKey;

    private IDataRefreshCallback refreshCallback;

    interface IDataRefreshCallback
    {
        // called after the grid is refreshed with new data
        void postRefresh();
    }

    SampleBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            final AbstractColumnsConfig commonColumns, final AbstractColumnsConfig parentColumns,
            final AbstractColumnsConfig propertyColumns)
    {
        this.viewContext = viewContext;
        this.commonColumns = commonColumns;
        this.parentColumns = parentColumns;
        this.propertyColumns = propertyColumns;
        this.sampleLoader = createSampleLoader();
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
        setId(BROWSER_ID);
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
                    criteria.setSortInfo(GxtTranslator.translate(loadConfig.getSortInfo()));
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
                    final Sample sample = sampleModel.getSample();
                    final EntityKind entityKind = EntityKind.SAMPLE;
                    final ITabItemFactory tabView =
                            viewContext.getClientPluginFactoryProvider().getClientPluginFactory(
                                    entityKind, sample.getSampleType()).createClientPlugin(
                                    entityKind).createEntityViewer(sample);
                    DispatcherHelper.dispatchNaviEvent(tabView);
                }
            });
        return sampleGrid;
    }

    private final static PagingToolBar createPagingToolBar()
    {
        return new PagingToolBarWithoutRefresh(PAGE_SIZE);
    }

    private final Map<String/* column id */, IColumnDefinition<Sample>> getColumnDefs()
    {
        final List<IColumnDefinition<Sample>> defs = new ArrayList<IColumnDefinition<Sample>>();
        defs.addAll(commonColumns.getColumnDefs());
        defs.addAll(parentColumns.getColumnDefs());
        defs.addAll(propertyColumns.getColumnDefs());
        return asColumnIdMap(defs);
    }

    private static Map<String, IColumnDefinition<Sample>> asColumnIdMap(
            final List<IColumnDefinition<Sample>> defs)
    {
        final Map<String, IColumnDefinition<Sample>> map =
                new HashMap<String, IColumnDefinition<Sample>>();
        for (final IColumnDefinition<Sample> def : defs)
        {
            map.put(def.getIdentifier(), def);
        }
        return map;
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
     * Note that, doing so, the result set associated on the server side with this
     * <code>resultSetKey</code> will be removed.
     * </p>
     */
    public final void refresh(final SampleType selectedType, final String selectedGroupCode,
            final Boolean includeGroup, final Boolean includeInstance,
            final IDataRefreshCallback newRefreshCallback)
    {
        if (resultSetKey != null)
        {
            viewContext.getService().removeResultSet(resultSetKey,
                    new VoidAsyncCallback<Void>(viewContext));
            resultSetKey = null;
        }
        this.refreshCallback = newRefreshCallback;
        criteria = new ListSampleCriteria();
        criteria.setSampleType(selectedType);
        criteria.setGroupCode(selectedGroupCode);
        criteria.setIncludeGroup(includeGroup);
        criteria.setIncludeInstance(includeInstance);
        contentPanel.setHeading(createHeader(selectedType, selectedGroupCode, includeGroup,
                includeInstance));
        final ColumnModel columnModel = createColumnModel();
        grid.reconfigure(grid.getStore(), columnModel);
        GWTUtils.setAutoExpandOnLastVisibleColumn(grid);
        sampleLoader.load(0, PAGE_SIZE);
    }

    public final void export()
    {
        export(new ExportSamplesCallback(viewContext));
    }

    // for tests
    final void export(final AbstractAsyncCallback<String> callback)
    {
        if (resultSetKey == null)
        {
            return;
        }
        final List<IColumnDefinition<Sample>> columns = getSelectedColumnDefs();
        final SortInfo sortInfo = criteria.getSortInfo();
        final TableExportCriteria<Sample> exportCriteria =
                new TableExportCriteria<Sample>(resultSetKey, sortInfo, columns);
        viewContext.getService().prepareExportSamples(exportCriteria, callback);
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

    private List<IColumnDefinition<Sample>> getSelectedColumnDefs()
    {
        final ColumnModel columnModel = grid.getColumnModel();
        return getSelectedColumnDefs(getColumnDefs(), columnModel);
    }

    private static <T> List<IColumnDefinition<T>> getSelectedColumnDefs(
            final Map<String, IColumnDefinition<T>> columnsDef, final ColumnModel columnModel)
    {
        final List<IColumnDefinition<T>> selectedColumnDefs = new ArrayList<IColumnDefinition<T>>();
        final int columnCount = columnModel.getColumnCount();
        for (int i = 0; i < columnCount; i++)
        {
            if (columnModel.isHidden(i) == false)
            {
                final String columnId = columnModel.getColumnId(i);
                selectedColumnDefs.add(columnsDef.get(columnId));
            }
        }
        return selectedColumnDefs;
    }

    //
    // Helper classes
    //

    public final class ListSamplesCallback extends AbstractAsyncCallback<ResultSet<Sample>>
    {
        private final AsyncCallback<PagingLoadResult<SampleModel>> delegate;

        private final int offset;

        ListSamplesCallback(final IViewContext<ICommonClientServiceAsync> viewContext,
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
            refreshCallback.postRefresh();
        }
    }

    public final class ExportSamplesCallback extends AbstractAsyncCallback<String>
    {
        private ExportSamplesCallback(final IViewContext<ICommonClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final String exportDataKey)
        {
            final URLMethodWithParameters methodWithParameters =
                    new URLMethodWithParameters(
                            GenericConstants.FILE_EXPORTER_DOWNLOAD_SERVLET_NAME);
            methodWithParameters.addParameter(GenericConstants.EXPORT_CRITERIA_KEY_PARAMETER,
                    exportDataKey);
            WindowUtils.openWindow(methodWithParameters.toString());
        }
    }

    protected boolean isExportEnabled()
    {
        return resultSetKey != null;
    }

}
