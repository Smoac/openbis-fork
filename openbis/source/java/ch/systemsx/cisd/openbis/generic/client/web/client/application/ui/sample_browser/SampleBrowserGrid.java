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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.Events;
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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.CommonColumns;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.LoadableColumnConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ParentColumns;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyColumns;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

/**
 * The grid part of samples browser.
 * 
 * @author Christian Ribeaud
 */
public final class SampleBrowserGrid extends LayoutContainer
{
    private static final int PAGE_SIZE = 50;

    private static final String PREFIX = "sample-browser-grid";

    static final String GRID_ID = GenericConstants.ID_PREFIX + PREFIX + "grid";

    private final GenericViewContext viewContext;

    private ContentPanel contentPanel;

    private Grid<SampleModel> grid;

    private PagingToolBar toolBar;

    private final GridConfiguration oldConfiguration;

    private final GridConfiguration newConfiguration;

    private final CommonColumns commonColumns;

    private final ParentColumns parentColumns;

    private final PropertyColumns propertyColumns;

    private final PagingLoader<?> loader;

    public SampleBrowserGrid(final GenericViewContext viewContext,
            final CommonColumns commonColumns, final ParentColumns parentColumns,
            final PropertyColumns propertyColumns)
    {
        this.viewContext = viewContext;
        this.commonColumns = commonColumns;
        this.parentColumns = parentColumns;
        this.propertyColumns = propertyColumns;
        oldConfiguration = new GridConfiguration(); // Already loaded
        newConfiguration = new GridConfiguration(); // Requested
        loader = createSampleLoader();
        setLayout(new FitLayout());
    }

    private void display(final SampleType sampleType, final String selectedGroupCode,
            final boolean showGroup, final boolean showInstance)
    {
        newConfiguration.update(sampleType, selectedGroupCode, showGroup, showInstance);
        final ColumnModel columnModel = createColumnModel(sampleType);
        final String header = createHeader(sampleType, selectedGroupCode, showGroup, showInstance);
        getContentPanel().setHeading(header);
        createOrReconfigureGrid(columnModel);
        layout();
    }

    // loads samples using previous result. Calls a callback with the full new result when finished.
    private void loadSamples(final List<SampleModel> previousModelsOrNull,
            final AsyncCallback<List<SampleModel>> callback)
    {
        if (oldConfiguration.majorChange(newConfiguration))
        {
            viewContext.getService().listSamples(newConfiguration.getCriterias(),
                    propertyColumns.getDirtyColumns(),
                    new ListSamplesCallback(viewContext, callback));
            return;
        }
        assert previousModelsOrNull != null : "some samples should be already loaded";

        if (oldConfiguration.minorChange(newConfiguration))
        {
            removeUnnecessarySamples(previousModelsOrNull);
        }

        if (propertyColumns.isDirty())
        {
            final List<Sample> currentSamples = extractSamplesFromModel(previousModelsOrNull);
            if (currentSamples.size() > 0)
            {
                viewContext.getService().listSamplesProperties(newConfiguration.getCriterias(),
                        propertyColumns.getDirtyColumns(),
                        new UpdateSamplesCallback(viewContext, currentSamples, callback));
                return;
            }
        }

        callback.onSuccess(previousModelsOrNull);
    }

    private List<Sample> extractSamplesFromModel(final List<SampleModel> modelList)
    {
        final List<Sample> samples = new ArrayList<Sample>();
        for (final SampleModel model : modelList)
        {
            samples.add((Sample) model.get(ModelDataPropertyNames.OBJECT));
        }
        return samples;
    }

    protected void removeUnnecessarySamples(final List<SampleModel> models)
    {
        final Iterator<SampleModel> iterator = models.iterator();
        while (iterator.hasNext())
        {
            final SampleModel next = iterator.next();
            final Boolean isGroupLevelSample =
                    (Boolean) next.get(ModelDataPropertyNames.IS_GROUP_SAMPLE);
            final boolean isInstanceLevelSample = isGroupLevelSample == false;
            if (isGroupLevelSample && newConfiguration.isIncludeGroup() == false
                    || isInstanceLevelSample && newConfiguration.isIncludeInstance() == false)
            {
                iterator.remove();
            }
        }
        oldConfiguration.update(newConfiguration);
    }

    private final void createOrReconfigureGrid(final ColumnModel columnModel)
    {
        final ListStore<SampleModel> sampleStore = new ListStore<SampleModel>(loader);

        if (grid == null)
        {
            grid = new Grid<SampleModel>(sampleStore, columnModel);
            grid.setId(GRID_ID);
            grid.setLoadMask(true);
            grid.setDeferHeight(true);
            grid.addListener(Events.CellClick, new Listener<GridEvent>()
                {
                    public final void handleEvent(final GridEvent be)
                    {
                        final SampleModel sampleModel =
                                (SampleModel) be.grid.getStore().getAt(be.rowIndex);
                        final SampleType sampleType =
                                (SampleType) sampleModel.get(ModelDataPropertyNames.SAMPLE_TYPE);
                        final String sampleIdentifier =
                                sampleModel.get(ModelDataPropertyNames.SAMPLE_IDENTIFIER);
                        final String code = sampleType.getCode();
                        viewContext.getClientPluginFactoryProvider().getClientPluginFactory(code)
                                .createViewClientForSampleType(code).viewSample(sampleIdentifier);
                    }
                });
            toolBar = new PagingToolBar(PAGE_SIZE);
            toolBar.bind(loader);

            getContentPanel().add(grid);
            getContentPanel().setBottomComponent(toolBar);
        } else
        {
            grid.reconfigure(sampleStore, columnModel);
        }
        toolBar.first();
    }

    private final ContentPanel getContentPanel()
    {
        if (contentPanel == null)
        {
            contentPanel = new ContentPanel();
            contentPanel.setBorders(false);
            contentPanel.setBodyBorder(false);
            contentPanel.setInsetBorder(false);
            contentPanel.setLayout(new FitLayout());
            add(contentPanel);
        }
        return contentPanel;
    }

    private String createHeader(final SampleType sampleType, final String selectedGroupCode,
            final boolean showGroup, final boolean showInstance)
    {
        final StringBuilder sb = new StringBuilder("Samples");
        sb.append(" of type ");
        sb.append(sampleType.getCode());
        if (showGroup)
        {
            sb.append(" belonging to the group ");
            sb.append(selectedGroupCode);
        }
        if (showInstance)
        {
            if (showGroup)
            {
                sb.append(" or shared");
            } else
            {
                sb.append(" which are shared among all the groups");
            }
        }
        return sb.toString();
    }

    public final void refresh(final SampleType sampleType, final String selectedGroupCode,
            final boolean showGroup, final boolean showInstance)
    {
        display(sampleType, selectedGroupCode, showGroup, showInstance);
    }

    private final ColumnModel createColumnModel(final SampleType type)
    {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.addAll(commonColumns.getColumns());
        configs.addAll(parentColumns.getColumns());
        configs.addAll(propertyColumns.getColumns());
        return new ColumnModel(configs);
    }

    final class UpdateSamplesCallback extends
            AbstractAsyncCallback<Map<Long, List<SampleProperty>>>
    {
        private final AsyncCallback<List<SampleModel>> delegate;

        private final List<Sample> currentSamples;

        UpdateSamplesCallback(final GenericViewContext viewContext,
                final List<Sample> currentSamples, final AsyncCallback<List<SampleModel>> delegate)
        {
            super(viewContext);
            this.delegate = delegate;
            this.currentSamples = currentSamples;
        }

        @Override
        protected void finishOnFailure(final Throwable caught)
        {
            delegate.onFailure(caught);
        }

        @Override
        protected final void process(final Map<Long, List<SampleProperty>> result)
        {
            setSampleProperties(currentSamples, result);
            final List<SampleModel> sampleModels = SampleModel.asSampleModels(currentSamples);
            oldConfiguration.update(newConfiguration);
            updateLoadedPropertyColumns();
            delegate.onSuccess(sampleModels);
        }

        private void setSampleProperties(final List<Sample> samples,
                final Map<Long, List<SampleProperty>> propertiesMap)
        {
            for (final Sample sample : samples)
            {
                final long sampleId = sample.getId();
                List<SampleProperty> props = sample.getProperties();
                if (props == null)
                {
                    props = new ArrayList<SampleProperty>();
                }
                final List<SampleProperty> list = propertiesMap.get(sampleId);
                if (list != null)
                {
                    props.addAll(list);
                }
                sample.setProperties(props);
            }
        }
    }

    final class ListSamplesCallback extends AbstractAsyncCallback<List<Sample>>
    {
        private final AsyncCallback<List<SampleModel>> delegate;

        ListSamplesCallback(final GenericViewContext viewContext,
                final AsyncCallback<List<SampleModel>> delegate)
        {
            super(viewContext);
            this.delegate = delegate;
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
        protected final void process(final List<Sample> result)
        {
            final List<SampleModel> sampleModels = SampleModel.asSampleModels(result);
            oldConfiguration.update(newConfiguration);
            updateLoadedPropertyColumns();
            delegate.onSuccess(sampleModels);
        }
    }

    private RpcProxy<PagingLoadConfig, PagingLoadResult<SampleModel>> createSampleProxy()
    {
        return new RpcProxy<PagingLoadConfig, PagingLoadResult<SampleModel>>()
            {
                private List<SampleModel> samples = null;

                //
                // RpcProxy
                //

                @Override
                public final void load(final PagingLoadConfig loadConfig,
                        final AsyncCallback<PagingLoadResult<SampleModel>> callback)
                {
                    loadSamples(samples, createPageCallbackDelegator(loadConfig, callback));
                }

                private AsyncCallback<List<SampleModel>> createPageCallbackDelegator(
                        final PagingLoadConfig loadConfig,
                        final AsyncCallback<PagingLoadResult<SampleModel>> callback)
                {
                    return new AbstractAsyncCallback<List<SampleModel>>(viewContext)
                        {

                            //
                            // AbstractAsyncCallback
                            //

                            @Override
                            protected final void process(final List<SampleModel> result)
                            {
                                samples = result;
                                sort(samples, loadConfig);
                                final PagingLoadResult<SampleModel> pagingLoadResult =
                                        getSublist(samples, loadConfig);
                                callback.onSuccess(pagingLoadResult);
                            }

                            @Override
                            protected final void finishOnFailure(final Throwable caught)
                            {
                                callback.onFailure(caught);
                            }
                        };
                }
            };
    }

    private PagingLoader<?> createSampleLoader()
    {
        final RpcProxy<PagingLoadConfig, PagingLoadResult<SampleModel>> proxy = createSampleProxy();
        final BasePagingLoader<PagingLoadConfig, PagingLoadResult<SampleModel>> pagingLoader =
                new BasePagingLoader<PagingLoadConfig, PagingLoadResult<SampleModel>>(proxy);
        pagingLoader.setRemoteSort(true);
        return pagingLoader;
    }

    private static void sort(final List<SampleModel> samples, final PagingLoadConfig config)
    {
        if (config.getSortInfo().getSortField() != null)
        {
            final String sortField = config.getSortInfo().getSortField();
            if (sortField != null)
            {
                Collections.sort(samples, config.getSortInfo().getSortDir().comparator(
                        new Comparator<SampleModel>()
                            {
                                public int compare(final SampleModel p1, final SampleModel p2)
                                {
                                    return p1.get(sortField, "").compareTo(p2.get(sortField, ""));
                                }
                            }));
            }
        }
    }

    private static <T> PagingLoadResult<T> getSublist(final List<T> samples,
            final PagingLoadConfig config)
    {
        final ArrayList<T> sublist = new ArrayList<T>();
        final int start = config.getOffset();
        int limit = samples.size();
        if (config.getLimit() > 0)
        {
            limit = Math.min(start + config.getLimit(), limit);
        }
        for (int i = config.getOffset(); i < limit; i++)
        {
            sublist.add(samples.get(i));
        }
        return new BasePagingLoadResult<T>(sublist, config.getOffset(), samples.size());
    }

    private void updateLoadedPropertyColumns()
    {
        for (final LoadableColumnConfig cc : propertyColumns.getColumns())
        {
            if (cc.isDirty())
            {
                cc.setLoaded(true);
            }
        }
    }

}