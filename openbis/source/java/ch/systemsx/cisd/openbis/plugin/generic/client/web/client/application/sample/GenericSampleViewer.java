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

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AttachmentVersionsSection;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractDatabaseModificationObserverWithCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserverWithMainObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PropertyTypeRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.IPropertyValueRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ExternalHyperlink;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.EntityPropertyUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> sample viewer.
 * 
 * @author Christian Ribeaud
 */
abstract public class GenericSampleViewer extends AbstractViewer<Sample> implements
        IDatabaseModificationObserver
{
    private static final String GENERIC_SAMPLE_VIEWER = "generic-sample-viewer";

    private static final String PREFIX = GENERIC_SAMPLE_VIEWER + "_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    public static final String PROPERTIES_ID_PREFIX =
            GenericConstants.ID_PREFIX + "generic-sample-properties-viewer_";

    public static final String SHOW_ONLY_DIRECTLY_CONNECTED_CHECKBOX_ID_POSTFIX =
            "-show_only_directly_connected_checkbox";

    private final IViewContext<?> viewContext;

    protected final TechId sampleId;

    private DisposableTabContent attachmentsSection;

    private DisposableTabContent containerSamplesSection;

    private DisposableTabContent derivedSamplesSection;

    private DisposableTabContent parentSamplesSection;

    private DisposableTabContent dataSetSection;

    private PropertyGrid propertyGrid;

    private String displayIdSuffix;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final IIdAndCodeHolder identifiable)
    {
        GenericSampleViewer viewer = new GenericSampleViewer(viewContext, identifiable)
            {
                @Override
                protected void loadSampleGenerationInfo(TechId sampleTechId,
                        AsyncCallback<SampleParentWithDerived> callback)
                {
                    TechId techId = TechId.create(identifiable);
                    viewContext.getService().getSampleGenerationInfo(techId, callback);
                }

            };
        viewer.reloadAllData();
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    abstract protected void loadSampleGenerationInfo(final TechId sampleTechId,
            AsyncCallback<SampleParentWithDerived> asyncCallback);

    protected GenericSampleViewer(final IViewContext<?> viewContext,
            final IIdAndCodeHolder identifiable)
    {
        super(viewContext, createId(identifiable));
        setLayout(new BorderLayout());
        this.sampleId = TechId.create(identifiable);
        this.viewContext = viewContext;
        extendToolBar();
    }

    private void extendToolBar()
    {
        if (viewContext.isSimpleMode())
        {
            return;
        }
        addToolBarButton(createDeleteButton(new IDelegatedAction()
            {
                public void execute()
                {
                    new SampleListDeletionConfirmationDialog(viewContext.getCommonViewContext(),
                            getOriginalDataAsSingleton(), createDeletionCallback(),
                            getOriginalData()).show();
                }
            }));
    }

    protected void reloadAllData()
    {
        reloadSampleGenerationData(new SampleGenerationInfoCallback(viewContext, this));
    }

    public static final String createId(final IIdAndCodeHolder identifiable)
    {
        return createId(TechId.create(identifiable));
    }

    public static final String createId(final TechId sampleId)
    {
        return ID_PREFIX + sampleId;
    }

    private final Component createRightPanel(SampleParentWithDerived sampleGeneration)
    {
        final Sample generator = sampleGeneration.getParent();
        displayIdSuffix = generator.getSampleType().getCode();

        final SectionsPanel container =
                new SectionsPanel(viewContext.getCommonViewContext(), getId());
        container.setDisplayID(DisplayTypeIDGenerator.GENERIC_SAMPLE_VIEWER, displayIdSuffix);
        List<TabContent> additionalPanels = createAdditionalSectionPanels();
        for (TabContent panel : additionalPanels)
        {
            container.addSection(panel);
        }
        // Contained samples
        containerSamplesSection = new ContainerSamplesSection(viewContext, generator);
        container.addSection(containerSamplesSection);
        // Derived samples
        derivedSamplesSection = new DerivedSamplesSection(viewContext, generator);
        container.addSection(derivedSamplesSection);
        // Parent samples
        parentSamplesSection = new ParentSamplesSection(viewContext, generator);
        container.addSection(parentSamplesSection);
        // Data Sets
        CheckBox showOnlyDirectlyConnectedCheckBox = createShowOnlyDirectlyConnectedCheckBox();
        dataSetSection =
                new SampleDataSetsSection(viewContext, showOnlyDirectlyConnectedCheckBox, sampleId,
                        generator.getSampleType());
        container.addSection(dataSetSection);

        // Attachments
        attachmentsSection = createAttachmentsSection(generator);
        container.addSection(attachmentsSection);

        container.layout();
        moduleSectionManager.initialize(container, generator);
        return container;
    }

    /**
     * To be subclassed. Creates additional panels of the viewer in the right side section besides
     * components, datasets and attachments
     */
    protected List<TabContent> createAdditionalSectionPanels()
    {
        return new ArrayList<TabContent>();
    }

    private CheckBox createShowOnlyDirectlyConnectedCheckBox()
    {
        CheckBox result = new CheckBox();
        result.setId(getId() + SHOW_ONLY_DIRECTLY_CONNECTED_CHECKBOX_ID_POSTFIX);
        result.setBoxLabel(viewContext.getMessage(Dict.SHOW_ONLY_DIRECTLY_CONNECTED));
        result.setValue(true);
        return result;
    }

    private AttachmentVersionsSection createAttachmentsSection(final Sample sample)
    {
        return new AttachmentVersionsSection(viewContext.getCommonViewContext(), sample);
    }

    private final static Map<String, Object> createProperties(
            final IMessageProvider messageProvider, final SampleParentWithDerived sampleGeneration)
    {
        final Map<String, Object> properties = new LinkedHashMap<String, Object>();
        final Sample sample = sampleGeneration.getParent();
        final SampleType sampleType = sample.getSampleType();
        final Invalidation invalidation = sample.getInvalidation();
        final Sample[] generated = sampleGeneration.getDerived();
        properties.put(messageProvider.getMessage(Dict.SAMPLE_PROPERTIES_PANEL_SAMPLE_IDENTIFIER),
                sample.getIdentifier());
        properties.put(messageProvider.getMessage(Dict.PERM_ID), new ExternalHyperlink(sample
                .getPermId(), sample.getPermlink()));
        properties.put(messageProvider.getMessage(Dict.SAMPLE_TYPE), sampleType);
        properties.put(messageProvider.getMessage(Dict.REGISTRATOR), sample.getRegistrator());
        properties.put(messageProvider.getMessage(Dict.REGISTRATION_DATE), sample
                .getRegistrationDate());
        if (invalidation != null)
        {
            properties.put(messageProvider.getMessage(Dict.INVALIDATION), invalidation);
        }

        Experiment experiment = sample.getExperiment();
        if (experiment != null)
        {
            properties.put(messageProvider.getMessage(Dict.PROJECT), experiment.getProject());
            properties.put(messageProvider.getMessage(Dict.EXPERIMENT), experiment);
        }

        // If there is only one Derived Sample it can be shown as a property,
        // otherwise show number of samples (users should use Derived Samples section).
        if (generated.length == 1)
        {
            properties.put(messageProvider.getMessage(Dict.DERIVED_SAMPLE), generated);
        } else if (generated.length > 1)
        {
            properties.put(messageProvider.getMessage(Dict.DERIVED_SAMPLE) + "s", generated.length);
        }
        final Set<Sample> parents = sample.getParents();
        final int parentsSize = parents.size();
        if (parentsSize == 1)
        {
            properties.put(messageProvider.getMessage(Dict.PARENT), parents.iterator().next());
        } else if (parentsSize > 1)
        {
            properties.put(messageProvider.getMessage(Dict.PARENTS), parentsSize);
        }
        Sample partOf = sample.getContainer();
        if (partOf != null)
        {
            properties.put(messageProvider.getMessage(Dict.PART_OF), partOf);
        }
        final List<IEntityProperty> sampleProperties = sample.getProperties();
        List<PropertyType> types = EntityPropertyUtils.extractTypes(sampleProperties);
        Collections.sort(sampleProperties);
        for (final IEntityProperty property : sampleProperties)
        {
            final String label =
                    PropertyTypeRenderer.getDisplayName(property.getPropertyType(), types);
            properties.put(label, property);
        }
        return properties;
    }

    private final Component createLeftPanel(final SampleParentWithDerived sampleGeneration)
    {
        final ContentPanel panel = new ContentPanel();
        panel.setScrollMode(Scroll.AUTOY);
        panel.setHeading(viewContext.getMessage(Dict.SAMPLE_PROPERTIES_HEADING));
        propertyGrid = createPropertyGrid(sampleId, sampleGeneration, viewContext);
        panel.add(propertyGrid);

        return panel;
    }

    public static PropertyGrid createPropertyGrid(final TechId sampleId,
            final SampleParentWithDerived sampleGeneration, final IViewContext<?> viewContext)
    {
        final Map<String, Object> properties = createProperties(viewContext, sampleGeneration);
        final PropertyGrid propertyGrid = new PropertyGrid(viewContext, properties.size());
        propertyGrid.registerPropertyValueRenderer(Person.class, PropertyValueRenderers
                .createPersonPropertyValueRenderer(viewContext));
        propertyGrid.registerPropertyValueRenderer(SampleType.class, PropertyValueRenderers
                .createSampleTypePropertyValueRenderer(viewContext));
        propertyGrid.registerPropertyValueRenderer(Sample.class, PropertyValueRenderers
                .createSamplePropertyValueRenderer(viewContext, true));
        propertyGrid.registerPropertyValueRenderer(Invalidation.class, PropertyValueRenderers
                .createInvalidationPropertyValueRenderer(viewContext));
        propertyGrid.registerPropertyValueRenderer(Project.class, PropertyValueRenderers
                .createProjectPropertyValueRenderer(viewContext));
        final IPropertyValueRenderer<IEntityProperty> propertyValueRenderer =
                PropertyValueRenderers.createEntityPropertyPropertyValueRenderer(viewContext);
        propertyGrid.registerPropertyValueRenderer(EntityProperty.class, propertyValueRenderer);
        propertyGrid.registerPropertyValueRenderer(GenericValueEntityProperty.class,
                propertyValueRenderer);
        propertyGrid.registerPropertyValueRenderer(VocabularyTermValueEntityProperty.class,
                propertyValueRenderer);
        propertyGrid.registerPropertyValueRenderer(MaterialValueEntityProperty.class,
                propertyValueRenderer);
        propertyGrid.registerPropertyValueRenderer(Experiment.class, PropertyValueRenderers
                .createExperimentPropertyValueRenderer(viewContext));
        propertyGrid.setProperties(properties);
        propertyGrid.getElement().setId(PROPERTIES_ID_PREFIX + sampleId);

        return propertyGrid;
    }

    public final void updateProperties(final SampleParentWithDerived sampleGeneration)
    {
        final Map<String, Object> properties = createProperties(viewContext, sampleGeneration);
        propertyGrid.resizeRows(properties.size());
        propertyGrid.setProperties(properties);
    }

    /**
     * Load the {@link SampleParentWithDerived} information.
     */
    protected void reloadSampleGenerationData(
            AbstractAsyncCallback<SampleParentWithDerived> callback)
    {
        loadSampleGenerationInfo(sampleId, callback);
    }

    /**
     * Adds listeners and sets up the initial left panel state.
     * <p>
     * The method {@link createRightPanel} must be called before this because it initializes state
     * used in this method.
     */
    private void configureLeftPanel()
    {
        // displayIdSuffix must be initialized first -- this happens in createRightPanel, so that
        // method must be called before this
        if (isLeftPanelInitiallyCollapsed(displayIdSuffix))
        {
            viewContext.log(displayIdSuffix + " Initially Collapsed");
            ((BorderLayout) getLayout()).collapse(com.extjs.gxt.ui.client.Style.LayoutRegion.WEST);
        }

        // Add the listeners after configuring the panel, so as not to cause confusion
        addLeftPanelCollapseExpandListeners(displayIdSuffix);
    }

    //
    // Helper classes
    //

    public static class DataSetConnectionTypeProvider
    {

        private final CheckBox showOnlyDirectlyConnectedCheckBox;

        private IDelegatedAction onChangeAction;

        public DataSetConnectionTypeProvider(final CheckBox showOnlyDirectlyConnectedCheckBox)
        {
            this.showOnlyDirectlyConnectedCheckBox = showOnlyDirectlyConnectedCheckBox;
            addChangeListener();
        }

        private void addChangeListener()
        {
            showOnlyDirectlyConnectedCheckBox.addListener(Events.Change, new Listener<FieldEvent>()
                {
                    public void handleEvent(FieldEvent be)
                    {
                        if (onChangeAction != null)
                        {
                            onChangeAction.execute();
                        }
                    }
                });
        }

        public void setOnChangeAction(IDelegatedAction onChangeAction)
        {
            this.onChangeAction = onChangeAction;
        }

        public boolean getShowOnlyDirectlyConnected()
        {
            return showOnlyDirectlyConnectedCheckBox.getValue();
        }
    }

    private static final class SampleGenerationInfoCallback extends
            AbstractAsyncCallback<SampleParentWithDerived>
    {
        private final GenericSampleViewer genericSampleViewer;

        private SampleGenerationInfoCallback(final IViewContext<?> viewContext,
                final GenericSampleViewer genericSampleViewer)
        {
            super(viewContext);
            this.genericSampleViewer = genericSampleViewer;
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Sets the {@link SampleParentWithDerived} for this <var>generic</var> sample viewer.
         * <p>
         * This method triggers the whole <i>GUI</i> construction.
         * </p>
         */
        @Override
        protected final void process(final SampleParentWithDerived result)
        {
            genericSampleViewer.updateOriginalData(result.getParent());
            genericSampleViewer.removeAll();
            // Left panel
            final Component leftPanel = genericSampleViewer.createLeftPanel(result);
            genericSampleViewer.add(leftPanel, createLeftBorderLayoutData());
            // Right panel
            final Component rightPanel = genericSampleViewer.createRightPanel(result);
            genericSampleViewer.add(rightPanel, createRightBorderLayoutData());

            genericSampleViewer.configureLeftPanel();

            genericSampleViewer.layout();
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

    private IDatabaseModificationObserver createDatabaseModificationObserver()
    {
        CompositeDatabaseModificationObserver observer =
                new CompositeDatabaseModificationObserverWithMainObserver(
                        new PropertyGridDatabaseModificationObserver());
        if (dataSetSection != null)
        {
            observer.addObserver(dataSetSection.tryGetDatabaseModificationObserver());
        }
        if (attachmentsSection != null)
        {
            observer.addObserver(attachmentsSection.tryGetDatabaseModificationObserver());
        }
        if (containerSamplesSection != null)
        {
            observer.addObserver(containerSamplesSection.tryGetDatabaseModificationObserver());
        }
        if (derivedSamplesSection != null)
        {
            observer.addObserver(derivedSamplesSection.tryGetDatabaseModificationObserver());
        }
        if (parentSamplesSection != null)
        {
            observer.addObserver(parentSamplesSection.tryGetDatabaseModificationObserver());
        }
        return observer;
    }

    private class PropertyGridDatabaseModificationObserver extends
            AbstractDatabaseModificationObserverWithCallback
    {

        public DatabaseModificationKind[] getRelevantModifications()
        {
            return new DatabaseModificationKind[]
                {
                        DatabaseModificationKind.createOrDelete(ObjectKind.SAMPLE),
                        DatabaseModificationKind.edit(ObjectKind.SAMPLE),
                        DatabaseModificationKind.createOrDelete(ObjectKind.EXPERIMENT),
                        DatabaseModificationKind.edit(ObjectKind.EXPERIMENT),
                        DatabaseModificationKind
                                .createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                        DatabaseModificationKind.edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                        DatabaseModificationKind.edit(ObjectKind.VOCABULARY_TERM) };
        }

        public void update(Set<DatabaseModificationKind> observedModifications)
        {
            reloadSampleGenerationData(new ReloadPropertyGridCallback(viewContext,
                    GenericSampleViewer.this));
        }

        private final class ReloadPropertyGridCallback extends
                AbstractAsyncCallback<SampleParentWithDerived>
        {
            private final GenericSampleViewer genericSampleViewer;

            private ReloadPropertyGridCallback(final IViewContext<?> viewContext,
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
            protected final void process(final SampleParentWithDerived result)
            {
                genericSampleViewer.updateOriginalData(result.getParent());
                genericSampleViewer.updateProperties(result);
                executeSuccessfulUpdateCallback();
            }

            @Override
            public void finishOnFailure(Throwable caught)
            {
                genericSampleViewer.setupRemovedEntityView();
            }
        }

    }

}
