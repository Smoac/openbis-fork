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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.IActionMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewerWithVerticalSplit;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.deletion.RevertDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> dataset viewer.
 * 
 * @author Piotr Buczek
 */
abstract public class GenericDataSetViewer extends AbstractViewerWithVerticalSplit<ExternalData>
        implements IDatabaseModificationObserver
{
    public static final String PREFIX = "generic-dataset-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final ProcessButtonHolder processButtonHolder;

    protected final TechId datasetId;

    private final IViewContext<?> viewContext;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final IIdAndCodeHolder identifiable)
    {
        GenericDataSetViewer viewer = new GenericDataSetViewer(viewContext, identifiable)
            {
                @Override
                protected void loadDatasetInfo(TechId datasetTechId,
                        AsyncCallback<ExternalData> asyncCallback)
                {
                    viewContext.getService().getDataSetInfo(datasetTechId, asyncCallback);
                }
            };
        viewer.reloadAllData();
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    protected GenericDataSetViewer(final IViewContext<?> viewContext,
            final IIdAndCodeHolder identifiable)
    {
        super(viewContext, createId(identifiable));
        setLayout(new BorderLayout());
        this.viewContext = viewContext;
        this.datasetId = TechId.create(identifiable);
        this.processButtonHolder = new ProcessButtonHolder();
        extendToolBar();
    }

    abstract protected void loadDatasetInfo(TechId datasetTechId,
            AsyncCallback<ExternalData> asyncCallback);

    /**
     * To be subclassed. Creates additional panels of the viewer in the right side section besides
     * components, datasets and attachments
     */
    protected List<TabContent> createAdditionalSectionPanels(ExternalData dataset)
    {
        return new ArrayList<TabContent>();
    }

    @Override
    protected void fillBreadcrumbWidgets(List<Widget> widgets)
    {
        Widget spaceBreadcrumb =
                createSpaceLink(originalData.getExperiment().getProject().getSpace());
        Widget projectBreadcrumb = createProjectLink(originalData.getExperiment().getProject());
        Widget experimentBreadcrumb = createEntityLink(originalData.getExperiment());
        widgets.add(spaceBreadcrumb);
        widgets.add(projectBreadcrumb);
        widgets.add(experimentBreadcrumb);

        if (originalData.getSample() != null)
        {
            Widget sampleBreadcrumb = createEntityLink(originalData.getSample());
            widgets.add(sampleBreadcrumb);
        }

        super.fillBreadcrumbWidgets(widgets);
    }

    private void extendToolBar()
    {
        if (viewContext.isSimpleOrEmbeddedMode())
        {
            return;
        }
        addToolBarButton(createDeleteButton(new IDelegatedAction()
            {
                public void execute()
                {
                    new DataSetListDeletionConfirmationDialog(viewContext.getCommonViewContext(),
                            createDeletionCallback(), getOriginalData()).show();
                }

            }));
        addToolBarButton(createRevertDeletionButton(new IDelegatedAction()
            {
                public void execute()
                {
                    new RevertDeletionConfirmationDialog(viewContext.getCommonViewContext(),
                            getOriginalData(), createRevertDeletionCallback()).show();
                }
            }));

        addToolBarButton(processButtonHolder.getButton());
    }

    public static final String createId(final IIdAndCodeHolder identifiable)
    {
        return createId(TechId.create(identifiable));
    }

    public static final String createId(final TechId datasetId)
    {
        return ID_PREFIX + datasetId;
    }

    /**
     * Load the dataset information.
     */
    @Override
    protected void reloadAllData()
    {
        loadDatasetInfo(datasetId, new DataSetInfoCallback(viewContext, this));
    }

    private final Component createLeftPanel(final ExternalData dataset)
    {
        final ContentPanel panel = createDataSetPropertiesPanel(dataset);
        panel.setScrollMode(Scroll.AUTOY);

        return panel;
    }

    private ContentPanel createDataSetPropertiesPanel(final ExternalData dataset)
    {
        return new DataSetPropertiesPanel(dataset, viewContext);
    }

    private final Component createRightPanel(final ExternalData dataset)
    {
        final SectionsPanel container =
                new SectionsPanel(viewContext.getCommonViewContext(), ID_PREFIX + dataset.getId());
        container.setDisplayID(DisplayTypeIDGenerator.GENERIC_DATASET_VIEWER, displayIdSuffix);

        List<TabContent> additionalPanels = createAdditionalSectionPanels(dataset);
        for (TabContent panel : additionalPanels)
        {
            container.addSection(panel);
        }
        // data
        final TabContent dataSection = new DataViewSection(viewContext, dataset);
        container.addSection(dataSection);

        if (dataset.isContainer())
        {
            final TabContent containedSection = new DataSetContainedSection(viewContext, dataset);
            container.addSection(containedSection);
        }

        // parents
        final TabContent parentsSection = new DataSetParentsSection(viewContext, dataset);
        container.addSection(parentsSection);

        // children
        final TabContent childrenSection = new DataSetChildrenSection(viewContext, dataset);
        container.addSection(childrenSection);

        // managed properties
        attachManagedPropertiesSections(container, dataset);

        moduleSectionManager.initialize(container, dataset);

        return container;
    }

    private static final class DataSetInfoCallback extends AbstractAsyncCallback<ExternalData>
    {
        private final GenericDataSetViewer genericDataSetViewer;

        private DataSetInfoCallback(final IViewContext<?> viewContext,
                final GenericDataSetViewer genericSampleViewer)
        {
            super(viewContext);
            this.genericDataSetViewer = genericSampleViewer;
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Sets the {@link ExternalData} for this <var>generic</var> dataset viewer.
         * <p>
         * This method triggers the whole <i>GUI</i> construction.
         * </p>
         */
        @Override
        protected final void process(final ExternalData result)
        {
            genericDataSetViewer.updateOriginalData(result);
            DataSet dataSet = result.tryGetAsDataSet();
            if (dataSet != null && dataSet.getStatus().isAvailable() == false)
            {
                genericDataSetViewer.setupUnavailableDataSetView(dataSet);
            }
            genericDataSetViewer.removeAll();
            // Left panel
            final Component leftPanel = genericDataSetViewer.createLeftPanel(result);
            genericDataSetViewer.add(leftPanel, genericDataSetViewer.createLeftBorderLayoutData());
            genericDataSetViewer.configureLeftPanel(leftPanel);
            // Right panel
            final Component rightPanel = genericDataSetViewer.createRightPanel(result);
            genericDataSetViewer.add(rightPanel, createRightBorderLayoutData());

            genericDataSetViewer.layout();

        }

        @Override
        public void finishOnFailure(Throwable caught)
        {
            genericDataSetViewer.setupRemovedEntityView();
        }
    }

    /** Updates data displayed in the browser when shown dataset is not available. */
    public void setupUnavailableDataSetView(final DataSet result)
    {
        setToolBarButtonsEnabled(false);
        updateTitle(getOriginalDataDescription() + " (not available)");
        String msg =
                viewContext.getMessage(Dict.DATASET_NOT_AVAILABLE_MSG, result.getCode(), result
                        .getStatus().getDescription().toLowerCase());
        MessageBox.info("Data not available", msg, null);
    }

    @Override
    protected void updateOriginalData(final ExternalData result)
    {
        super.updateOriginalData(result);
        processButtonHolder.setupData(result);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { DatabaseModificationKind.edit(ObjectKind.DATA_SET),
                    DatabaseModificationKind.createOrDelete(ObjectKind.DATA_SET),
                    DatabaseModificationKind.createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    DatabaseModificationKind.edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    DatabaseModificationKind.createOrDelete(ObjectKind.VOCABULARY_TERM),
                    DatabaseModificationKind.edit(ObjectKind.VOCABULARY_TERM),
                    DatabaseModificationKind.createOrDelete(ObjectKind.EXPERIMENT),
                    DatabaseModificationKind.edit(ObjectKind.EXPERIMENT),
                    DatabaseModificationKind.createOrDelete(ObjectKind.SAMPLE),
                    DatabaseModificationKind.edit(ObjectKind.SAMPLE), };
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        reloadAllData(); // reloads everything
    }

    /**
     * Holder of a {@link Button} that has a menu with items that schedule dataset plugin
     * processing. The button is hidden at the beginning. When data set is successfully loaded by
     * the viewer and there is a nonempty list of plugins assigned to its data type data then the
     * menu is filled and button is shown.
     */
    private class ProcessButtonHolder
    {
        private final Button button;

        public ProcessButtonHolder()
        {
            this.button = createProcessButton();
        }

        private Button createProcessButton()
        {
            final Button result = new Button(viewContext.getMessage(Dict.BUTTON_PROCESS));
            // need to set menu here, otherwise menu button will not be displayed
            result.setMenu(new Menu());
            result.hide();
            return result;
        }

        public Button getButton()
        {
            return this.button;
        }

        /** @param data external data that will be processed */
        public void setupData(final ExternalData data)
        {
            viewContext.getCommonService().listDataStoreServices(DataStoreServiceKind.PROCESSING,
                    new ProcessingServicesCallback(viewContext, getOriginalData(), button));
        }
    }

    private static final class ProcessingServicesCallback extends
            AbstractAsyncCallback<List<DatastoreServiceDescription>>
    {

        private final ExternalData dataset;

        private final Button processButton;

        public ProcessingServicesCallback(IViewContext<?> viewContext, ExternalData dataset,
                Button processButton)
        {
            super(viewContext);
            this.dataset = dataset;
            this.processButton = processButton;
        }

        @Override
        protected void process(List<DatastoreServiceDescription> result)
        {
            List<DatastoreServiceDescription> matchingServices = filterNotMatching(result);
            if (matchingServices.size() > 0)
            {
                processButton.setMenu(createPerformProcessingMenu(matchingServices));
                processButton.show();
            }
        }

        private List<DatastoreServiceDescription> filterNotMatching(
                List<DatastoreServiceDescription> services)
        {
            List<DatastoreServiceDescription> matchingServices =
                    new ArrayList<DatastoreServiceDescription>();
            for (DatastoreServiceDescription service : services)
            {
                if (DatastoreServiceDescription.isMatching(service, dataset))
                {
                    matchingServices.add(service);
                }
            }
            return matchingServices;
        }

        private Menu createPerformProcessingMenu(List<DatastoreServiceDescription> services)
        {
            Menu result = new Menu();
            final DisplayedOrSelectedDatasetCriteria criteria =
                    DisplayedOrSelectedDatasetCriteria.createSelectedItems(Arrays.asList(dataset
                            .getCode()));
            for (DatastoreServiceDescription service : services)
            {
                result.add(new ActionMenu(createActionMenuItem(service), viewContext,
                        createProcessDatasetAction(service, criteria)));
            }
            return result;
        }

        private IActionMenuItem createActionMenuItem(final DatastoreServiceDescription service)
        {
            return new IActionMenuItem()
                {
                    public String getMenuId()
                    {
                        return service.getKey();
                    }

                    public String getMenuText(IMessageProvider messageProvider)
                    {
                        return service.getLabel();
                    }
                };
        }

        private IDelegatedAction createProcessDatasetAction(
                final DatastoreServiceDescription service,
                final DisplayedOrSelectedDatasetCriteria criteria)
        {
            return new IDelegatedAction()
                {
                    public void execute()
                    {
                        viewContext.getCommonService().processDatasets(service, criteria,
                                new ProcessingDisplayCallback(viewContext, service));
                    }
                };
        }

    }

    private static final class ProcessingDisplayCallback extends AbstractAsyncCallback<Void>
    {
        private final DatastoreServiceDescription service;

        private ProcessingDisplayCallback(IViewContext<?> viewContext,
                DatastoreServiceDescription service)
        {
            super(viewContext);
            this.service = service;
        }

        @Override
        public final void process(final Void result)
        {
            final String title = viewContext.getMessage(Dict.PROCESSING_INFO_TITLE);
            final String msg = viewContext.getMessage(Dict.PROCESSING_INFO_MSG, service.getLabel());
            MessageBox.info(title, msg, null);
        }
    }

}
