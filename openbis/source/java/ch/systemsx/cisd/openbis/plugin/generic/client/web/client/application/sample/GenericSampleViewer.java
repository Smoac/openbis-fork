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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseListLoadConfig;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.CommonSampleColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGeneration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> sample viewer.
 * 
 * @author Christian Ribeaud
 */
public final class GenericSampleViewer extends AbstractViewer<IGenericClientServiceAsync>
{
    private static final String PREFIX = "generic-sample-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    public static final String PROPERTIES_ID_PREFIX =
            GenericConstants.ID_PREFIX + "generic-sample-properties-viewer_";

    public static final String COMPONENTS_POSTFIX = "-components";

    public static final String DATA_POSTFIX = "-data";

    private Grid<SampleModel> partOfSamplesGrid;

    private final String sampleIdentifier;

    private DisposableComponent disposableBrowser;

    public GenericSampleViewer(final IViewContext<IGenericClientServiceAsync> viewContext,
            final String sampleIdentifier)
    {
        super(viewContext);
        setId(createId(sampleIdentifier));
        this.sampleIdentifier = sampleIdentifier;
    }

    public static final String createId(String sampleIdentifier)
    {
        return ID_PREFIX + sampleIdentifier;
    }

    private final static BorderLayoutData createRightBorderLayoutData()
    {
        final BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
        return data;
    }

    private final Component createRightPanel()
    {
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());
        // 'Part of' samples
        ContentPanel panel = createContentPanel(viewContext.getMessage(Dict.PART_OF_HEADING));
        final ListLoader<BaseListLoadConfig> sampleLoader =
                createListLoader(createRpcProxyForPartOfSamples());
        final ListStore<SampleModel> sampleListStore = createListStore(sampleLoader);
        partOfSamplesGrid =
                new Grid<SampleModel>(sampleListStore, createPartOfSamplesColumnModel());
        partOfSamplesGrid.setId(getId() + COMPONENTS_POSTFIX);
        partOfSamplesGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        partOfSamplesGrid.setLoadMask(true);
        panel.add(partOfSamplesGrid);
        container.add(panel, new RowData(1, 0.5, new Margins(0, 5, 5, 0)));
        // External data
        panel = createContentPanel(viewContext.getMessage(Dict.EXTERNAL_DATA_HEADING));
        disposableBrowser =
                SampleDataSetBrowser.create(viewContext, sampleIdentifier, getId() + DATA_POSTFIX);
        panel.add(disposableBrowser.getComponent());
        container.add(panel, new RowData(1, 0.5, new Margins(0, 5, 0, 0)));
        return container;
    }

    @Override
    protected void onDetach()
    {
        disposableBrowser.dispose();
        super.onDetach();
    }

    private final static ContentPanel createContentPanel(final String heading)
    {
        final ContentPanel panel = new ContentPanel();
        panel.setHeading(heading);
        panel.setLayout(new FitLayout());
        panel.setBodyBorder(true);
        panel.setBorders(false);
        return panel;
    }

    private final <T> ListLoader<BaseListLoadConfig> createListLoader(
            final RpcProxy<BaseListLoadConfig, BaseListLoadResult<T>> rpcProxy)
    {
        final BaseListLoader<BaseListLoadConfig, BaseListLoadResult<T>> baseListLoader =
                new BaseListLoader<BaseListLoadConfig, BaseListLoadResult<T>>(rpcProxy);
        return baseListLoader;
    }

    private final RpcProxy<BaseListLoadConfig, BaseListLoadResult<SampleModel>> createRpcProxyForPartOfSamples()
    {
        return new RpcProxy<BaseListLoadConfig, BaseListLoadResult<SampleModel>>()
            {

                //
                // RpcProxy
                //

                @Override
                public final void load(final BaseListLoadConfig loadConfig,
                        final AsyncCallback<BaseListLoadResult<SampleModel>> callback)
                {
                    final ListSampleCriteria sampleCriteria = new ListSampleCriteria();
                    sampleCriteria.setContainerIdentifier(sampleIdentifier);
                    viewContext.getCommonService().listSamples(sampleCriteria,
                            new ListSamplesCallback(viewContext, callback));
                }
            };
    }

    private final <T extends ModelData> ListStore<T> createListStore(
            final ListLoader<BaseListLoadConfig> loader)
    {
        return new ListStore<T>(loader);
    }

    private final ColumnModel createPartOfSamplesColumnModel()
    {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(ColumnConfigFactory.createCodeColumnConfig(viewContext,
                CommonSampleColDefKind.CODE.id()));
        configs.add(ColumnConfigFactory.createRegistrationDateColumnConfig(viewContext,
                CommonSampleColDefKind.REGISTRATION_DATE.id()));
        configs.add(ColumnConfigFactory.createRegistratorColumnConfig(viewContext,
                CommonSampleColDefKind.REGISTRATOR.id()));
        return new ColumnModel(configs);
    }

    private final static BorderLayoutData createLeftBorderLayoutData()
    {
        final BorderLayoutData data = new BorderLayoutData(LayoutRegion.WEST, 300, 100, 500);
        data.setMargins(new Margins(0, 5, 0, 0));
        data.setSplit(true);
        data.setCollapsible(true);
        data.setFloatable(false);
        return data;
    }

    private final static Map<String, Object> createProperties(
            final IMessageProvider messageProvider, final SampleGeneration sampleGeneration)
    {
        final Map<String, Object> properties = new LinkedHashMap<String, Object>();
        final Sample sample = sampleGeneration.getGenerator();
        final SampleType sampleType = sample.getSampleType();
        final Invalidation invalidation = sample.getInvalidation();
        final Sample[] generated = sampleGeneration.getGenerated();
        properties.put(messageProvider.getMessage(Dict.SAMPLE), sample.getCode());
        properties.put(messageProvider.getMessage(Dict.SAMPLE_TYPE), sampleType);
        properties.put(messageProvider.getMessage(Dict.REGISTRATOR), sample.getRegistrator());
        properties.put(messageProvider.getMessage(Dict.REGISTRATION_DATE), sample
                .getRegistrationDate());
        
        // TODO 2009-03-24 Piotr Buczek: Uncomment after lazy initialization problem is resolved
        // final Procedure procedure = sample.getValidProcedure();
        // if (procedure != null)
        // {
        // properties.put(messageProvider.getMessage(Dict.EXPERIMENT), procedure.getExperiment());
        // }

        if (generated.length > 0)
        {
            properties.put(messageProvider.getMessage(Dict.GENERATED_SAMPLES), generated);
        }
        if (invalidation != null)
        {
            properties.put(messageProvider.getMessage(Dict.INVALIDATION), invalidation);
        }
        Sample generatedFrom = sample.getGeneratedFrom();
        for (int i = 0; i < sampleType.getGeneratedFromHierarchyDepth() && generatedFrom != null; i++)
        {
            properties.put(messageProvider.getMessage(Dict.GENERATED_FROM, i + 1), generatedFrom);
            generatedFrom = generatedFrom.getGeneratedFrom();
        }
        final List<SampleProperty> sampleProperties = sample.getProperties();
        Collections.sort(sampleProperties, new Comparator<SampleProperty>()
            {
                public final int compare(final SampleProperty s1, final SampleProperty s2)
                {
                    return s1.getEntityTypePropertyType().getPropertyType().getLabel().compareTo(
                            s2.getEntityTypePropertyType().getPropertyType().getLabel());
                }
            });
        for (final SampleProperty property : sampleProperties)
        {
            final String label = property.getEntityTypePropertyType().getPropertyType().getLabel();
            properties.put(label, property);
        }
        return properties;
    }

    private final Component createLeftPanel(final SampleGeneration sampleGeneration)
    {
        final ContentPanel panel = new ContentPanel();
        panel.setScrollMode(Scroll.AUTOY);
        panel.setHeading(viewContext.getMessage(Dict.SAMPLE_PROPERTIES_HEADING));
        panel.add(createPropertyGrid(sampleIdentifier, sampleGeneration, viewContext));
        return panel;
    }

    public static PropertyGrid createPropertyGrid(String sampleIdentifier,
            final SampleGeneration sampleGeneration, final IViewContext<?> viewContext)
    {
        final Map<String, Object> properties = createProperties(viewContext, sampleGeneration);
        final PropertyGrid propertyGrid = new PropertyGrid(viewContext, properties.size());
        propertyGrid.getElement().setId(PROPERTIES_ID_PREFIX + sampleIdentifier);
        propertyGrid.registerPropertyValueRenderer(Person.class, PropertyValueRenderers
                .createPersonPropertyValueRenderer(viewContext));
        propertyGrid.registerPropertyValueRenderer(SampleType.class, PropertyValueRenderers
                .createSampleTypePropertyValueRenderer(viewContext));
        propertyGrid.registerPropertyValueRenderer(Sample.class, PropertyValueRenderers
                .createSamplePropertyValueRenderer(viewContext, true));
        propertyGrid.registerPropertyValueRenderer(Invalidation.class, PropertyValueRenderers
                .createInvalidationPropertyValueRenderer(viewContext));
        propertyGrid.registerPropertyValueRenderer(SampleProperty.class, PropertyValueRenderers
                .createSamplePropertyPropertyValueRenderer(viewContext));
        propertyGrid.setProperties(properties);

        return propertyGrid;
    }

    private final void loadStores()
    {
        partOfSamplesGrid.getStore().getLoader().load();
    }

    /**
     * Load the sample information.
     */
    @Override
    public void loadData()
    {
        viewContext.getService().getSampleInfo(sampleIdentifier,
                new SampleGenerationInfoCallback(viewContext, this));
    }

    //
    // Helper classes
    //

    final static class ListSamplesCallback extends AbstractAsyncCallback<ResultSet<Sample>>
    {
        private final AsyncCallback<BaseListLoadResult<SampleModel>> delegate;

        ListSamplesCallback(final IViewContext<IGenericClientServiceAsync> viewContext,
                final AsyncCallback<BaseListLoadResult<SampleModel>> callback)
        {
            super(viewContext);
            this.delegate = callback;
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        protected void finishOnFailure(final Throwable caught)
        {
            delegate.onFailure(caught);
        }

        @Override
        protected final void process(final ResultSet<Sample> result)
        {
            final List<SampleModel> sampleModels = SampleModel.asSampleModels(result.getList());
            final BaseListLoadResult<SampleModel> baseListLoadResult =
                    new BaseListLoadResult<SampleModel>(sampleModels);
            delegate.onSuccess(baseListLoadResult);
        }
    }

    public static final class SampleGenerationInfoCallback extends
            AbstractAsyncCallback<SampleGeneration>
    {
        private final GenericSampleViewer genericSampleViewer;

        private SampleGenerationInfoCallback(
                final IViewContext<IGenericClientServiceAsync> viewContext,
                final GenericSampleViewer genericSampleViewer)
        {
            super(viewContext);
            this.genericSampleViewer = genericSampleViewer;
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Sets the {@link SampleGeneration} for this <var>generic</var> sample viewer.
         * <p>
         * This method triggers the whole <i>GUI</i> construction.
         * </p>
         */
        @Override
        protected final void process(final SampleGeneration result)
        {
            genericSampleViewer.removeAll();
            genericSampleViewer.setLayout(new BorderLayout());
            // Left panel
            final Component leftPanel = genericSampleViewer.createLeftPanel(result);
            genericSampleViewer.add(leftPanel, GenericSampleViewer.createLeftBorderLayoutData());
            // Right panel
            final Component rightPanel = genericSampleViewer.createRightPanel();
            genericSampleViewer.add(rightPanel, GenericSampleViewer.createRightBorderLayoutData());
            genericSampleViewer.layout();
            genericSampleViewer.loadStores();
        }
    }
}