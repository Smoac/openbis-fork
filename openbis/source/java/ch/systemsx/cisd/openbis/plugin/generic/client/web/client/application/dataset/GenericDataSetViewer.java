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
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SingleSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.IActionMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DataSetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivizationStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> dataset viewer.
 * 
 * @author Piotr Buczek
 */
abstract public class GenericDataSetViewer extends AbstractViewer<ExternalData> implements
        IDatabaseModificationObserver
{
    public static final String PREFIX = "generic-dataset-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    public static final String VIEW_BUTTON_ID_SUFFIX = "_view-button";

    private final BrowseButtonHolder browseButtonHolder;

    private final ProcessButtonHolder processButtonHolder;

    protected final TechId datasetId;

    private final IViewContext<?> viewContext;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final IIdentifiable identifiable)
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
            final IIdentifiable identifiable)
    {
        super(viewContext, createId(identifiable));
        setLayout(new BorderLayout());
        this.viewContext = viewContext;
        this.datasetId = TechId.create(identifiable);
        this.browseButtonHolder = new BrowseButtonHolder();
        this.processButtonHolder = new ProcessButtonHolder();
        extendToolBar();
    }

    abstract protected void loadDatasetInfo(TechId datasetTechId,
            AsyncCallback<ExternalData> asyncCallback);

    /**
     * To be subclassed. Creates additional panels of the viewer in the right side section besides
     * components, datasets and attachments
     */
    protected List<SingleSectionPanel> createAdditionalSectionPanels()
    {
        return new ArrayList<SingleSectionPanel>();
    }

    private void extendToolBar()
    {
        addToolBarButton(browseButtonHolder.getButton());

        addToolBarButton(createDeleteButton(new IDelegatedAction()
            {
                public void execute()
                {
                    new DataSetListDeletionConfirmationDialog(viewContext.getCommonViewContext(),
                            getOriginalData(), createDeletionCallback()).show();
                }

            }));

        addToolBarButton(processButtonHolder.getButton());
    }

    public static final String createId(final IIdentifiable identifiable)
    {
        return createId(TechId.create(identifiable));
    }

    public static final String createId(final TechId datasetId)
    {
        return ID_PREFIX + datasetId;
    }

    private final String createChildId(String childIdSuffix)
    {
        return getId() + childIdSuffix;
    }

    /**
     * Load the dataset information.
     */
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
        final SectionsPanel container = new SectionsPanel(viewContext.getCommonViewContext());
        final String displayIdSuffix = getDisplayIdSuffix(dataset.getDataSetType().getCode());

        List<SingleSectionPanel> additionalPanels = createAdditionalSectionPanels();
        for (SingleSectionPanel panel : additionalPanels)
        {
            container.addPanel(panel);
        }

        // parents
        final SingleSectionPanel parentsSection = new DataSetParentsSection(viewContext, dataset);
        parentsSection.setDisplayID(DisplayTypeIDGenerator.DATA_SET_PARENTS_SECTION,
                displayIdSuffix);
        container.addPanel(parentsSection, false);

        // children
        final SingleSectionPanel childrenSection = new DataSetChildrenSection(viewContext, dataset);
        childrenSection.setDisplayID(DisplayTypeIDGenerator.DATA_SET_CHILDREN_SECTION,
                displayIdSuffix);
        container.addPanel(childrenSection, false);

        // data
        final SingleSectionPanel dataSection = new DataViewSection(viewContext, dataset);
        dataSection.setDisplayID(DisplayTypeIDGenerator.DATA_SET_DATA_SECTION, displayIdSuffix);
        container.addPanel(dataSection);

        container.layout();
        return container;
    }

    private static final String getDisplayIdSuffix(String suffix)
    {
        return PREFIX + suffix;
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
            if (result.getStatus() != DataSetArchivizationStatus.ACTIVE)
            {
                genericDataSetViewer.setupNonActiveDataSetView(result);
            }
            genericDataSetViewer.removeAll();
            // Left panel
            final Component leftPanel = genericDataSetViewer.createLeftPanel(result);
            genericDataSetViewer.add(leftPanel, createLeftBorderLayoutData());
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

    /** Updates data displayed in the browser when shown dataset is not active. */
    public void setupNonActiveDataSetView(final ExternalData result)
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
        browseButtonHolder.setupData(result);
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
                    DatabaseModificationKind.edit(ObjectKind.VOCABULARY_TERM), };
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        reloadAllData(); // reloads everything
    }

    /**
     * Holder of a {@link Button} that goes to external data browsing on selection. The button is
     * disabled until data is successfully loaded by the viewer.
     */
    private class BrowseButtonHolder
    {
        private final Button button;

        public BrowseButtonHolder()
        {
            this.button = createBrowseButton();
        }

        private Button createBrowseButton()
        {
            Button result = new Button(viewContext.getMessage(Dict.BUTTON_VIEW));
            GWTUtils.setToolTip(result, viewContext.getMessage(Dict.TOOLTIP_VIEW_DATASET));
            result.setId(createChildId(VIEW_BUTTON_ID_SUFFIX));
            result.disable();
            return result;
        }

        public Button getButton()
        {
            return this.button;
        }

        /** @param data external data that will be browsed after selection */
        public void setupData(final ExternalData data)
        {
            button.addSelectionListener(new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent ce)
                    {
                        DataSetUtils.showDataSet(data, viewContext.getModel());
                    }
                });
        }
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
