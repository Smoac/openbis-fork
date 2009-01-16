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

import java.util.List;

import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.shared.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.SampleModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;

/**
 * A {@link LayoutContainer} which contains the grid where the samples are displayed.
 * 
 * @author Christian Ribeaud
 */
public final class SampleBrowserGrid extends AbstractBrowserGrid<Sample, SampleModel>
{
    private static final String PREFIX = "sample-browser-grid_";

    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + PREFIX + "sample_browser";

    public static final String GRID_ID = GenericConstants.ID_PREFIX + PREFIX + "grid";

    private SampleType selectedSampleType;

    private ColumnDefsAndConfigs<Sample> columns;

    private ListSampleCriteria criteria;

    SampleBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, GRID_ID);
        redefineColumns(null);
        setId(BROWSER_ID);
    }

    @Override
    protected final PagingLoader<PagingLoadConfig> createPagingLoader()
    {
        final RpcProxy<PagingLoadConfig, PagingLoadResult<SampleModel>> proxy =
                createDataLoaderProxy();
        final BasePagingLoader<PagingLoadConfig, PagingLoadResult<SampleModel>> pagingLoader =
                new BasePagingLoader<PagingLoadConfig, PagingLoadResult<SampleModel>>(proxy);
        pagingLoader.setRemoteSort(true);
        return pagingLoader;
    }

    private final RpcProxy<PagingLoadConfig, PagingLoadResult<SampleModel>> createDataLoaderProxy()
    {
        return new RpcProxy<PagingLoadConfig, PagingLoadResult<SampleModel>>()
            {
                @Override
                public final void load(final PagingLoadConfig loadConfig,
                        final AsyncCallback<PagingLoadResult<SampleModel>> callback)
                {
                    setupCriteria(criteria, loadConfig);
                    viewContext.getService()
                            .listSamples(
                                    criteria,
                                    new ListEntitiesCallback(viewContext, callback, loadConfig
                                            .getOffset()));
                }
            };
    }

    @Override
    protected List<SampleModel> createModels(List<Sample> entities)
    {
        return SampleModel.asSampleModels(entities);
    }

    @Override
    protected Listener<GridEvent> createSampleViewerHandler()
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
    public final void refresh(ListSampleCriteria newCriteria,
            final IDataRefreshCallback newRefreshCallback)
    {
        redefineColumns(newCriteria.getSampleType());

        this.criteria = newCriteria;
        String newHeader = createHeader(newCriteria);

        super.refresh(newRefreshCallback, newHeader, columns.getColumnConfigs());
    }

    /** Export always deals with data from the previous refresh operation */
    public final void export()
    {
        export(new ExportEntitiesCallback(viewContext));
    }

    // for tests
    final void export(final AbstractAsyncCallback<String> callback)
    {
        assert columns != null : "refresh before exporting!";

        final TableExportCriteria<Sample> exportCriteria =
                createExportCriteria(columns.getColumnDefs(), criteria.getSortInfo());
        viewContext.getService().prepareExportSamples(exportCriteria, callback);
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
            this.selectedSampleType = selectedType;
        }
    }
}
