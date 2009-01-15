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
import com.extjs.gxt.ui.client.data.ModelData;
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

import ch.systemsx.cisd.openbis.generic.client.shared.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.VoidAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.SampleModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.PagingToolBarWithoutRefresh;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GxtTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
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

    private final PagingLoader<PagingLoadConfig> dataLoader;

    private final ContentPanel contentPanel;

    private final Grid<SampleModel> grid;

    private SampleType selectedSampleType;

    private ColumnDefsAndConfigs<Sample> columns;

    private ListSampleCriteria criteria;

    private String resultSetKey;

    private IDataRefreshCallback refreshCallback;

    interface IDataRefreshCallback
    {
        // called after the grid is refreshed with new data
        void postRefresh();
    }

    SampleBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
        this.dataLoader = createSampleLoader();
        redefineColumns(null);

        // create the UI
        setId(BROWSER_ID);

        this.grid = createGrid(dataLoader, createSampleViewerHandler());
        this.contentPanel = createContentPanel();

        setLayout(new FitLayout());
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

                //
                // RpcProxy
                //

                @Override
                public final void load(final PagingLoadConfig loadConfig,
                        final AsyncCallback<PagingLoadResult<SampleModel>> callback)
                {
                    setupCriteria(criteria, loadConfig);
                    viewContext.getService().listSamples(criteria,
                            new ListSamplesCallback(viewContext, callback, loadConfig.getOffset()));
                }

                private void setupCriteria(DefaultResultSetConfig<String> resultSetConfig,
                        PagingLoadConfig loadConfig)
                {
                    resultSetConfig.setLimit(loadConfig.getLimit());
                    resultSetConfig.setOffset(loadConfig.getOffset());
                    resultSetConfig.setSortInfo(GxtTranslator.translate(loadConfig.getSortInfo()));
                    resultSetConfig.setResultSetKey(resultSetKey);
                }

            };
    }

    private Listener<GridEvent> createSampleViewerHandler()
    {
        return new Listener<GridEvent>()
            {
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
            };
    }

    private static final String createHeader(ListSampleCriteria criteria)
    {
        final StringBuilder builder = new StringBuilder("Samples");
        builder.append(" of type ");
        builder.append(criteria.getSampleType().getCode());
        if (criteria.isIncludeGroup())
        {
            builder.append(" belonging to the group ");
            builder.append(criteria.getGroupCode());
        }
        if (criteria.isIncludeInstance())
        {
            if (criteria.isIncludeGroup())
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
    public final void refresh(ListSampleCriteria listCriteria,
            final IDataRefreshCallback newRefreshCallback)
    {
        redefineColumns(listCriteria.getSampleType());

        this.criteria = listCriteria;
        String newHeader = createHeader(listCriteria);

        refresh(newRefreshCallback, newHeader, columns.getColumnConfigs());
    }

    /** Export always deals with data from the previous refresh operation */
    public final void export()
    {
        export(new ExportEntitiesCallback(viewContext));
    }

    // for tests
    final void export(final AbstractAsyncCallback<String> callback)
    {
        if (resultSetKey == null)
        {
            return;
        }
        final TableExportCriteria<Sample> exportCriteria =
                createExportCriteria(columns.getColumnDefs());
        viewContext.getService().prepareExportSamples(exportCriteria, callback);
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
            saveCacheKey(result.getResultSetKey());
            final List<SampleModel> sampleModels = SampleModel.asSampleModels(result.getList());
            final PagingLoadResult<SampleModel> loadResult =
                    new BasePagingLoadResult<SampleModel>(sampleModels, offset, result
                            .getTotalLength());
            delegate.onSuccess(loadResult);
            refreshCallback.postRefresh();
        }
    }

    private static ColumnDefsAndConfigs<Sample> defineColumns(IMessageProvider messageProvider,
            SampleType selectedTypeOrNull)
    {
        ColumnDefsAndConfigs<Sample> columns = new ColumnDefsAndConfigs<Sample>();
        columns.addColumns(SampleModel.createCommonColumnsSchema(messageProvider), true);
        if (selectedTypeOrNull != null)
        {
            List<IColumnDefinitionUI<Sample>> parentColumnsSchema =
                    SampleModel.createParentColumnsSchema(messageProvider, selectedTypeOrNull);
            columns.addColumns(parentColumnsSchema, false);

            List<IColumnDefinitionUI<Sample>> propertyColumnsSchema =
                    SampleModel.createPropertyColumnsSchema(selectedTypeOrNull);
            columns.addColumns(propertyColumnsSchema, false);
        }
        return columns;
    }

    private void redefineColumns(SampleType selectedType)
    {
        if (selectedType == null || selectedType.equals(selectedSampleType) == false)
        {
            this.columns = defineColumns(viewContext, selectedType);
            selectedSampleType = selectedType;
        }
    }

    // --------------- generic part

    // creates a panel with grid and a toolbar for paging
    private ContentPanel createContentPanel()
    {
        ContentPanel panel = createEmptyContentPanel();
        panel.add(grid);
        final PagingToolBar toolBar = new PagingToolBarWithoutRefresh(PAGE_SIZE);
        toolBar.bind(dataLoader);
        panel.setBottomComponent(toolBar);
        return panel;
    }

    private void refresh(final IDataRefreshCallback newRefreshCallback, String newHeader,
            List<ColumnConfig> columnConfigs)
    {
        disposeCache();
        this.refreshCallback = newRefreshCallback;
        this.contentPanel.setHeading(newHeader);

        ColumnModel columnModel = new ColumnModel(columnConfigs);
        grid.reconfigure(grid.getStore(), columnModel);
        GWTUtils.setAutoExpandOnLastVisibleColumn(grid);
        dataLoader.load(0, PAGE_SIZE);
    }

    private <T> TableExportCriteria<T> createExportCriteria(
            List<IColumnDefinition<T>> availableColumns)
    {
        final List<IColumnDefinition<T>> columnDefs = getSelectedColumns(availableColumns);
        final SortInfo sortInfo = criteria.getSortInfo();
        final TableExportCriteria<T> exportCriteria =
                new TableExportCriteria<T>(resultSetKey, sortInfo, columnDefs);
        return exportCriteria;
    }

    private <T> List<IColumnDefinition<T>> getSelectedColumns(
            List<IColumnDefinition<T>> availableColumns)
    {
        Map<String, IColumnDefinition<T>> availableColumnsMap = asColumnIdMap(availableColumns);
        final ColumnModel columnModel = grid.getColumnModel();
        return getSelectedColumns(availableColumnsMap, columnModel);
    }

    private void saveCacheKey(final String newResultSetKey)
    {
        if (resultSetKey == null)
        {
            resultSetKey = newResultSetKey;
        } else
        {
            assert resultSetKey.equals(newResultSetKey) : "Result set keys not the same.";
        }
    }

    public void disposeCache()
    {
        if (resultSetKey != null)
        {
            viewContext.getService().removeResultSet(resultSetKey,
                    new VoidAsyncCallback<Void>(viewContext));
            resultSetKey = null;
        }
    }

    // ------- generic static helpers

    private final static ContentPanel createEmptyContentPanel()
    {
        final ContentPanel contentPanel = new ContentPanel();
        contentPanel.setBorders(false);
        contentPanel.setBodyBorder(false);
        contentPanel.setLayout(new FitLayout());
        return contentPanel;
    }

    private static final <T extends ModelData> Grid<T> createGrid(
            PagingLoader<PagingLoadConfig> dataLoader, Listener<GridEvent> detailsViewer)
    {
        ListStore<T> listStore = new ListStore<T>(dataLoader);
        ColumnModel columnModel = new ColumnModel(new ArrayList<ColumnConfig>());
        Grid<T> sampleGrid = new Grid<T>(listStore, columnModel);
        sampleGrid.setId(GRID_ID);
        sampleGrid.setLoadMask(true);
        sampleGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        sampleGrid.addListener(Events.CellDoubleClick, detailsViewer);
        return sampleGrid;
    }

    public static final class ExportEntitiesCallback extends AbstractAsyncCallback<String>
    {
        private ExportEntitiesCallback(final IViewContext<ICommonClientServiceAsync> viewContext)
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

    // creates a map to quickly find a definition by its identifier
    private static <T> Map<String, IColumnDefinition<T>> asColumnIdMap(
            final List<IColumnDefinition<T>> defs)
    {
        final Map<String, IColumnDefinition<T>> map = new HashMap<String, IColumnDefinition<T>>();
        for (final IColumnDefinition<T> def : defs)
        {
            map.put(def.getIdentifier(), def);
        }
        return map;
    }

    /**
     * @param availableColumns map of all available columns definitions.
     * @param columnModel describes the visual properties of the columns. Connected with
     *            availableColumns by column id.
     * @return list of columns definitions for those columns which are currently shown
     */
    private static <T/* column definition */> List<T> getSelectedColumns(
            final Map<String, T> availableColumns, final ColumnModel columnModel)
    {
        final List<T> selectedColumnDefs = new ArrayList<T>();
        final int columnCount = columnModel.getColumnCount();
        for (int i = 0; i < columnCount; i++)
        {
            if (columnModel.isHidden(i) == false)
            {
                final String columnId = columnModel.getColumnId(i);
                selectedColumnDefs.add(availableColumns.get(columnId));
            }
        }
        return selectedColumnDefs;
    }

}
