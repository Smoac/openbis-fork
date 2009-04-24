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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AttachmentsSection;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleModelFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.CommonSampleColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGeneration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> sample viewer.
 * 
 * @author Christian Ribeaud
 */
public final class GenericSampleViewer extends AbstractViewer<IGenericClientServiceAsync> implements
        IDatabaseModificationObserver
{
    private static final String PREFIX = "generic-sample-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    public static final String PROPERTIES_ID_PREFIX =
            GenericConstants.ID_PREFIX + "generic-sample-properties-viewer_";

    public static final String COMPONENTS_POSTFIX = "-components";

    public static final String DATA_POSTFIX = "-data";

    private Grid<ModelData> partOfSamplesGrid;

    private IDelegatedAction showComponentsPanelAction;

    private final String sampleIdentifier;

    private IDisposableComponent disposableBrowser;

    private PropertyGrid propertyGrid;

    private AttachmentsSection<Sample> attachmentsPanel;

    public static DatabaseModificationAwareComponent create(
            IViewContext<IGenericClientServiceAsync> viewContext, String sampleIdentifier)
    {
        GenericSampleViewer viewer = new GenericSampleViewer(viewContext, sampleIdentifier);
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    private GenericSampleViewer(final IViewContext<IGenericClientServiceAsync> viewContext,
            final String sampleIdentifier)
    {
        super(viewContext);
        setId(createId(sampleIdentifier));
        this.sampleIdentifier = sampleIdentifier;
        setHeading("Sample " + sampleIdentifier);
        reloadAllData();
    }

    protected void reloadAllData()
    {
        reloadSampleGenerationData(new SampleGenerationInfoCallback(viewContext, this));
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

    private final Component createRightPanel(SampleGeneration sampleGeneration)
    {
        final LayoutContainer container = new LayoutContainer();
        final double defaultHeight = 1.0 / 3; // all child panels have the same height as default
        attachmentsPanel = createAttachmentsSection(sampleGeneration.getGenerator());
        container.add(attachmentsPanel, new RowData(1, defaultHeight, new Margins(5, 5, 0, 0)));
        container.setLayout(new RowLayout());
        // 'Part of' samples
        final ContentPanel componentsPanel =
                createContentPanel(viewContext.getMessage(Dict.PART_OF_HEADING));
        final ListLoader<BaseListLoadConfig> sampleLoader =
                createListLoader(createRpcProxyForPartOfSamples());
        final ListStore<ModelData> sampleListStore = createListStore(sampleLoader);
        partOfSamplesGrid = new Grid<ModelData>(sampleListStore, createPartOfSamplesColumnModel());
        partOfSamplesGrid.setId(getId() + COMPONENTS_POSTFIX);
        String displayTypeID =
                DisplayTypeIDGenerator.SAMPLE_DETAILS_GRID.createID(EntityKind.SAMPLE, null);
        viewContext.getDisplaySettingsManager().prepareGrid(displayTypeID, partOfSamplesGrid);
        partOfSamplesGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        partOfSamplesGrid.setLoadMask(true);
        componentsPanel.add(partOfSamplesGrid);
        componentsPanel.setVisible(false);
        container.add(componentsPanel, new RowData(1, defaultHeight, new Margins(5, 5, 0, 0)));
        // External data
        final ContentPanel externalDataPanel =
                createContentPanel(viewContext.getMessage(Dict.EXTERNAL_DATA_HEADING));
        disposableBrowser =
                SampleDataSetBrowser.create(viewContext, sampleIdentifier, getId() + DATA_POSTFIX);
        // external data panel has the height doubled when components panel is not visible
        externalDataPanel.add(disposableBrowser.getComponent());
        final RowData externalDataRow = new RowData(1, 2 * defaultHeight, new Margins(5, 5, 0, 0));
        container.add(externalDataPanel, externalDataRow);

        showComponentsPanelAction = new IDelegatedAction()
            {
                public void execute()
                {
                    componentsPanel.setVisible(true);
                    // switching back to default size of external data panel
                    externalDataRow.setHeight(defaultHeight);
                    container.layout();
                }

            };
        return container;
    }

    private AttachmentsSection<Sample> createAttachmentsSection(final Sample sample)
    {
        final AttachmentsSection<Sample> attachmentsSection =
                new AttachmentsSection<Sample>(sample, viewContext);
        attachmentsSection.setReloadDataAction(new IDelegatedAction()
            {
                public void execute()
                {
                    reloadSampleData(attachmentsSection.getReloadDataCallback());
                }
            });
        return attachmentsSection;
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

    private final RpcProxy<BaseListLoadConfig, BaseListLoadResult<ModelData>> createRpcProxyForPartOfSamples()
    {
        return new RpcProxy<BaseListLoadConfig, BaseListLoadResult<ModelData>>()
            {

                //
                // RpcProxy
                //

                @Override
                public final void load(final BaseListLoadConfig loadConfig,
                        final AsyncCallback<BaseListLoadResult<ModelData>> callback)
                {
                    final ListSampleCriteria sampleCriteria =
                            ListSampleCriteria.createForContainer(sampleIdentifier);
                    ListSamplesCallback listCallback =
                            new ListSamplesCallback(viewContext, callback,
                                    showComponentsPanelAction);
                    viewContext.getCommonService().listSamples(sampleCriteria, true, listCallback);
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

        Experiment experiment = sample.getExperiment();
        if (experiment != null)
        {
            properties.put(messageProvider.getMessage(Dict.EXPERIMENT), experiment);
        }

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
        Collections.sort(sampleProperties);
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
        propertyGrid = createPropertyGrid(sampleIdentifier, sampleGeneration, viewContext);
        panel.add(propertyGrid);
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
        propertyGrid.registerPropertyValueRenderer(Experiment.class, PropertyValueRenderers
                .createExperimentPropertyValueRenderer(viewContext));
        propertyGrid.setProperties(properties);

        return propertyGrid;
    }

    public final void reloadProperties(final SampleGeneration sampleGeneration)
    {
        final Map<String, Object> properties = createProperties(viewContext, sampleGeneration);
        propertyGrid.resizeRows(properties.size());
        propertyGrid.setProperties(properties);
    }

    private final void loadStores()
    {
        partOfSamplesGrid.getStore().getLoader().load();
    }

    /**
     * Load the {@link SampleGeneration} information.
     */
    protected void reloadSampleGenerationData(AbstractAsyncCallback<SampleGeneration> callback)
    {
        viewContext.getService().getSampleGenerationInfo(sampleIdentifier, callback);
    }

    /**
     * Load the {@link Sample} information.
     */
    protected void reloadSampleData(AbstractAsyncCallback<Sample> callback)
    {
        viewContext.getService().getSampleInfo(sampleIdentifier, callback);
    }

    /**
     * Load the sample information for property grid.
     */
    private void reloadPropertyGridData()
    {
        reloadSampleGenerationData(new ReloadPropertyGridCallback(viewContext, this));
    }

    //
    // Helper classes
    //

    final static class ListSamplesCallback extends AbstractAsyncCallback<ResultSet<Sample>>
    {
        private final AsyncCallback<BaseListLoadResult<ModelData>> delegate;

        private final IDelegatedAction showResultsAction;

        ListSamplesCallback(final IViewContext<IGenericClientServiceAsync> viewContext,
                final AsyncCallback<BaseListLoadResult<ModelData>> callback,
                final IDelegatedAction noResultsAction)
        {
            super(viewContext);
            this.delegate = callback;
            this.showResultsAction = noResultsAction;
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
            final List<ModelData> sampleModels =
                    SampleModelFactory.asSampleModels(result.getList());
            final BaseListLoadResult<ModelData> baseListLoadResult =
                    new BaseListLoadResult<ModelData>(sampleModels);
            delegate.onSuccess(baseListLoadResult);
            if (result.getTotalLength() > 0)
            {
                showResultsAction.execute();
            }
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
            final Component rightPanel = genericSampleViewer.createRightPanel(result);
            genericSampleViewer.add(rightPanel, GenericSampleViewer.createRightBorderLayoutData());
            genericSampleViewer.layout();
            genericSampleViewer.loadStores();
        }
    }

    public static final class ReloadPropertyGridCallback extends
            AbstractAsyncCallback<SampleGeneration>
    {
        private final GenericSampleViewer genericSampleViewer;

        private ReloadPropertyGridCallback(
                final IViewContext<IGenericClientServiceAsync> viewContext,
                final GenericSampleViewer genericSampleViewer)
        {
            super(viewContext);
            this.genericSampleViewer = genericSampleViewer;
        }

        //
        // AbstractAsyncCallback
        //

        /** This method triggers reloading of the {@link PropertyGrid} data. */
        @Override
        protected final void process(final SampleGeneration result)
        {
            genericSampleViewer.reloadProperties(result);

        }
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return createDatabaseModificationObserver().getRelevantModifications();
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        createDatabaseModificationObserver().update(observedModifications);
    }

    // TODO 2009-04-01, Tomasz Pylak: add auto-refresh for contained samples
    private CompositeDatabaseModificationObserver createDatabaseModificationObserver()
    {
        CompositeDatabaseModificationObserver observer =
                new CompositeDatabaseModificationObserver();
        if (disposableBrowser != null)
        {
            observer.addObserver(disposableBrowser);
        }
        if (propertyGrid != null)
        {
            observer.addObserver(new PropertyGridDatabaseModificationObserver());
        }
        if (attachmentsPanel != null)
        {
            observer.addObserver(attachmentsPanel.getDatabaseModificationObserver());
        }
        return observer;
    }

    private class PropertyGridDatabaseModificationObserver implements IDatabaseModificationObserver
    {

        public DatabaseModificationKind[] getRelevantModifications()
        {
            return new DatabaseModificationKind[]
                {
                        DatabaseModificationKind.edit(ObjectKind.SAMPLE),
                        DatabaseModificationKind
                                .createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                        DatabaseModificationKind.createOrDelete(ObjectKind.VOCABULARY_TERM) };
        }

        public void update(Set<DatabaseModificationKind> observedModifications)
        {
            reloadPropertyGridData();
        }
    }
}