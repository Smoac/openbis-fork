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

import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.CommonSampleColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * A {@link LayoutContainer} which contains the grid where the samples are displayed.
 * 
 * @author Christian Ribeaud
 * @author Tomasz Pylak
 */
public final class SampleBrowserGrid extends AbstractBrowserGrid<Sample, SampleModel>
{
    private static final String PREFIX = GenericConstants.ID_PREFIX + "sample-browser";

    // browser consists of the grid and additional toolbars (paging, filtering)
    public static final String BROWSER_ID = PREFIX + "_main";

    public static final String GRID_ID = PREFIX + "_grid";

    private final ICriteriaProvider criteriaProvider;

    // criteria used in the previous refresh operation or null if it has not occurred yet
    private ListSampleCriteria criteria;

    private interface ICriteriaProvider
    {
        ListSampleCriteria tryGetCriteria();
    }

    public static DisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final SampleBrowserToolbar toolbar = new SampleBrowserToolbar(viewContext);
        ICriteriaProvider criteriaProvider = asCriteriaProvider(toolbar);
        final SampleBrowserGrid browserGrid =
                new SampleBrowserGrid(viewContext, criteriaProvider, GRID_ID, true, false);
        browserGrid.extendTopToolbar(toolbar);
        return browserGrid.asDisposableWithToolbar(toolbar);
    }

    private static ICriteriaProvider asCriteriaProvider(final SampleBrowserToolbar toolbar)
    {
        return new ICriteriaProvider()
            {
                public ListSampleCriteria tryGetCriteria()
                {
                    return toolbar.tryGetCriteria();
                }
            };
    }

    public static DisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final String experimentIdentifier, String gridId)
    {
        ICriteriaProvider criteriaProvider = new ICriteriaProvider()
            {
                public ListSampleCriteria tryGetCriteria()
                {
                    ListSampleCriteria criteria = new ListSampleCriteria();
                    criteria.setExperimentIdentifier(experimentIdentifier);
                    return criteria;
                }
            };

        final SampleBrowserGrid browserGrid =
                new SampleBrowserGrid(viewContext, criteriaProvider, gridId, false, true);
        return browserGrid.asDisposableWithoutToolbar();
    }

    private SampleBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            ICriteriaProvider criteriaProvider, String gridId, boolean showHeader,
            boolean refreshAutomatically)
    {
        super(viewContext, gridId, showHeader, refreshAutomatically);
        this.criteriaProvider = criteriaProvider;
        setId(BROWSER_ID);
    }

    // adds show, show-details and invalidate buttons
    private void extendTopToolbar(SampleBrowserToolbar toolbar)
    {
        String showDetailsTitle = viewContext.getMessage(Dict.BUTTON_SHOW_DETAILS);
        Button showDetailsButton =
                createSelectedItemButton(showDetailsTitle, asShowEntityInvoker());

        SelectionChangedListener<?> refreshButtonListener = addRefreshButton(toolbar);
        toolbar.setCriteriaChangedListener(refreshButtonListener);

        toolbar.add(new FillToolItem());
        toolbar.add(new AdapterToolItem(showDetailsButton));
    }

    @Override
    protected boolean isRefreshEnabled()
    {
        return criteriaProvider.tryGetCriteria() != null;
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, Sample> resultSetConfig,
            AbstractAsyncCallback<ResultSet<Sample>> callback)
    {
        copyPagingConfig(resultSetConfig);
        viewContext.getService().listSamples(criteria, callback);
    }

    private void copyPagingConfig(DefaultResultSetConfig<String, Sample> resultSetConfig)
    {
        criteria.setLimit(resultSetConfig.getLimit());
        criteria.setOffset(resultSetConfig.getOffset());
        criteria.setSortInfo(resultSetConfig.getSortInfo());
        criteria.setFilterInfos(resultSetConfig.getFilterInfos());
        criteria.setResultSetKey(resultSetConfig.getResultSetKey());
    }

    @Override
    protected SampleModel createModel(Sample entity)
    {
        return new SampleModel(entity);
    }

    @Override
    protected List<IColumnDefinition<Sample>> getAvailableFilters()
    {
        return asColumnFilters(new CommonSampleColDefKind[]
            { CommonSampleColDefKind.CODE, CommonSampleColDefKind.EXPERIMENT,
                    CommonSampleColDefKind.PROJECT });
    }

    @Override
    protected void showEntityViewer(SampleModel sampleModel)
    {
        final Sample sample = sampleModel.getBaseObject();
        final EntityKind entityKind = EntityKind.SAMPLE;
        final ITabItemFactory tabView =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        sample.getSampleType()).createClientPlugin(entityKind).createEntityViewer(
                        sample);
        DispatcherHelper.dispatchNaviEvent(tabView);
    }

    private static final String createHeader(ListSampleCriteria criteria)
    {
        final StringBuilder builder = new StringBuilder("Samples");
        if (criteria.getSampleType() != null)
        {
            builder.append(" of type ");
            builder.append(criteria.getSampleType().getCode());
        }
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
        if (criteria.getExperimentIdentifier() != null)
        {
            return null;
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
    @Override
    protected final void refresh()
    {
        ListSampleCriteria newCriteria = criteriaProvider.tryGetCriteria();
        if (newCriteria == null)
        {
            return;
        }
        boolean refreshColumnsDefinition = hasColumnsDefinitionChanged(newCriteria);
        this.criteria = newCriteria;
        String newHeader = createHeader(newCriteria);

        super.refresh(newHeader, refreshColumnsDefinition);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<Sample> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportSamples(exportCriteria, callback);
    }

    @Override
    protected ColumnDefsAndConfigs<Sample> createColumnsDefinition()
    {
        assert criteria != null : "criteria not set!";
        return SampleModel.createColumnsSchema(viewContext, criteria.getSampleType());
    }

    private boolean hasColumnsDefinitionChanged(ListSampleCriteria newCriteria)
    {

        SampleType sampleType = newCriteria.getSampleType();
        return criteria == null || sampleType != null
                && sampleType.equals(criteria.getSampleType()) == false;
    }
}
