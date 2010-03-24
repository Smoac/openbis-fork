/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.menu.Menu;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.IActionMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid.SelectedAndDisplayedItems;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DialogWithOnlineHelpUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.TextToolItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivizationStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;

/**
 * 'Compute' menu for Data Sets.
 * 
 * @author Piotr Buczek
 */
public class DataSetComputeMenu extends TextToolItem
{

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final IDelegatedActionWithResult<SelectedAndDisplayedItems> selectedDataSetsGetter;

    private final IDelegatedAction postArchivizationAction;

    public DataSetComputeMenu(IViewContext<ICommonClientServiceAsync> viewContext,
            IDelegatedActionWithResult<SelectedAndDisplayedItems> selectedDataSetsGetter,
            IDelegatedAction postArchivizationAction)
    {
        super(viewContext.getMessage(Dict.MENU_COMPUTE));
        this.viewContext = viewContext;
        this.selectedDataSetsGetter = selectedDataSetsGetter;
        this.postArchivizationAction = postArchivizationAction;

        Menu submenu = new Menu();
        addMenuItem(submenu, DssTaskActionMenuKind.COMPUTE_MENU_QUERIES);
        addMenuItem(submenu, DssTaskActionMenuKind.COMPUTE_MENU_PROCESSING);
        addMenuItem(submenu, DssTaskActionMenuKind.COMPUTE_MENU_ARCHIVIZATION);
        addMenuItem(submenu, DssTaskActionMenuKind.COMPUTE_MENU_UNARCHIVIZATION);
        addMenuItem(submenu, DssTaskActionMenuKind.COMPUTE_MENU_LOCK);
        addMenuItem(submenu, DssTaskActionMenuKind.COMPUTE_MENU_UNLOCK);
        setMenu(submenu);
    }

    //

    /** {@link ActionMenu} kind enum with names matching dictionary keys */
    private static enum DssTaskActionMenuKind implements IActionMenuItem
    {
        COMPUTE_MENU_QUERIES(DataStoreServiceKind.QUERIES), COMPUTE_MENU_PROCESSING(
                DataStoreServiceKind.PROCESSING), COMPUTE_MENU_ARCHIVIZATION(
                DataStoreServiceKind.ARCHIVIZATION), COMPUTE_MENU_UNARCHIVIZATION(
                DataStoreServiceKind.UNARCHIVIZATION),
        COMPUTE_MENU_LOCK(DataStoreServiceKind.LOCK), COMPUTE_MENU_UNLOCK(
                DataStoreServiceKind.UNLOCK);

        private final DataStoreServiceKind dssTaskKind;

        DssTaskActionMenuKind(DataStoreServiceKind dssTaskKind)
        {
            this.dssTaskKind = dssTaskKind;
        }

        public DataStoreServiceKind getDssTaskKind()
        {
            return dssTaskKind;
        }

        public String getMenuId()
        {
            return this.name();
        }

        public String getMenuText(IMessageProvider messageProvider)
        {
            return messageProvider.getMessage(this.name());
        }
    }

    private final void addMenuItem(Menu submenu, DssTaskActionMenuKind menuItemKind)
    {
        final IDelegatedAction menuItemAction =
                createComputeMenuAction(menuItemKind.getDssTaskKind());
        submenu.add(new ActionMenu(menuItemKind, viewContext, menuItemAction));
    }

    private IDelegatedAction createComputeMenuAction(final DataStoreServiceKind dssTaskKind)
    {
        return new IDelegatedAction()
            {
                public void execute()
                {
                    final SelectedAndDisplayedItems selectedAndDisplayedItems =
                            selectedDataSetsGetter.execute();
                    final IComputationAction computationAction =
                            createComputationAction(selectedAndDisplayedItems, dssTaskKind);
                    final ComputationData data =
                            new ComputationData(dssTaskKind, computationAction,
                                    selectedAndDisplayedItems);
                    createPerformComputationDialog(data).show();
                }

                private Window createPerformComputationDialog(ComputationData data)
                {
                    final String title = "Perform " + dssTaskKind.getDescription();
                    switch (dssTaskKind)
                    {
                        case ARCHIVIZATION:
                        case UNARCHIVIZATION:
                        case LOCK:
                        case UNLOCK:
                            return new PerformArchivizationDialog(viewContext, data, title);
                        default:
                            return new PerformComputationDialog(viewContext, data, title);
                    }
                }
            };
    }

    private IComputationAction createComputationAction(
            final SelectedAndDisplayedItems selectedAndDisplayedItems,
            final DataStoreServiceKind dssTaskKind)
    {
        return new IComputationAction()
            {
                public void execute(DatastoreServiceDescription service, boolean computeOnSelected)
                {
                    DisplayedOrSelectedDatasetCriteria criteria =
                            createCriteria(selectedAndDisplayedItems, computeOnSelected);
                    switch (dssTaskKind)
                    {
                        case QUERIES:
                            DataSetReportGenerator.generate(viewContext, service, criteria);
                            break;
                        case PROCESSING:
                            viewContext.getService().processDatasets(service, criteria,
                                    new ProcessingDisplayCallback(viewContext));
                            break;
                        case ARCHIVIZATION:
                            viewContext.getService().archiveDatasets(
                                    criteria,
                                    new ArchivizationDisplayCallback(viewContext, dssTaskKind
                                            .getDescription()));
                            break;
                        case UNARCHIVIZATION:
                            viewContext.getService().unarchiveDatasets(
                                    criteria,
                                    new ArchivizationDisplayCallback(viewContext, dssTaskKind
                                            .getDescription()));
                            break;
                        case LOCK:
                            viewContext.getService().lockDatasets(
                                    criteria,
                                    new ArchivizationDisplayCallback(viewContext, dssTaskKind
                                            .getDescription()));
                            break;
                        case UNLOCK:
                            viewContext.getService().unlockDatasets(
                                    criteria,
                                    new ArchivizationDisplayCallback(viewContext, dssTaskKind
                                            .getDescription()));
                            break;
                    }
                }
            };
    }

    private static DisplayedOrSelectedDatasetCriteria createCriteria(
            SelectedAndDisplayedItems selectedAndDisplayedItems, boolean computeOnSelected)
    {
        return selectedAndDisplayedItems.createCriteria(computeOnSelected);
    }

    private final class ProcessingDisplayCallback extends AbstractAsyncCallback<Void>
    {
        private ProcessingDisplayCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        public final void process(final Void result)
        {
            MessageBox.info("Processing", "Processing has been scheduled successfully.", null);
        }
    }

    private final class ArchivizationDisplayCallback extends AbstractAsyncCallback<Void>
    {
        private final String actionName;

        private ArchivizationDisplayCallback(IViewContext<?> viewContext, String actionName)
        {
            super(viewContext);
            this.actionName = actionName;
        }

        @Override
        public final void process(final Void result)
        {
            MessageBox.info(actionName, actionName + " has been scheduled successfully.", null);
            postArchivizationAction.execute();
        }
    }

    private static class ComputationData
    {
        private final DataStoreServiceKind dssTaskKind;

        private final IComputationAction computationAction;

        private final SelectedAndDisplayedItems selectedAndDisplayedItems;

        public ComputationData(DataStoreServiceKind dssTaskKind,
                IComputationAction computationAction,
                SelectedAndDisplayedItems selectedAndDisplayedItems)
        {
            super();
            this.dssTaskKind = dssTaskKind;
            this.computationAction = computationAction;
            this.selectedAndDisplayedItems = selectedAndDisplayedItems;
        }

        public DataStoreServiceKind getDssTaskKind()
        {
            return dssTaskKind;
        }

        public IComputationAction getComputationAction()
        {
            return computationAction;
        }

        public List<ExternalData> getSelectedDataSets()
        {
            return selectedAndDisplayedItems.getSelectedItems();
        }
    }

    private static class PerformArchivizationDialog extends
            AbstractDataConfirmationDialog<ComputationData>
    {
        private static final int LABEL_WIDTH = ColumnConfigFactory.DEFAULT_COLUMN_WIDTH - 20;

        private static final int FIELD_WIDTH = 2 * ColumnConfigFactory.DEFAULT_COLUMN_WIDTH - 20;

        private static final int DIALOG_WIDTH = 4 * ColumnConfigFactory.DEFAULT_COLUMN_WIDTH + 30;

        private final IViewContext<ICommonClientServiceAsync> viewContext;

        private final ComputationDataSetsRadioProvider radioProvider;

        protected PerformArchivizationDialog(IViewContext<ICommonClientServiceAsync> viewContext,
                ComputationData data, String title)
        {
            super(viewContext, data, title);
            this.viewContext = viewContext;
            this.radioProvider = new ComputationDataSetsRadioProvider(data);

            setWidth(DIALOG_WIDTH);
        }

        @Override
        protected String createMessage()
        {
            int size = data.getSelectedDataSets().size();
            String computationName = data.getDssTaskKind().getDescription();
            String requiredStatusName = getRequiredStatus(data.getDssTaskKind()).getDescription();
            if (size == 0)
            {
                final String msgIntroduction = viewContext.getMessage(Dict.NO_DATASETS_SELECTED);
                String dictKey = Dict.PERFORM_ARCHIVIZATION_ON_ALL_DATASETS_MSG_TEMPLATE;
                return viewContext.getMessage(dictKey, msgIntroduction, computationName,
                        requiredStatusName);
            } else
            {
                String dictKey =
                        Dict.PERFORM_ARCHIVIZATION_ON_SELECTED_OR_ALL_DATASETS_MSG_TEMPLATE;
                return viewContext.getMessage(dictKey, computationName, size, requiredStatusName);
            }
        }

        private DataSetArchivizationStatus getRequiredStatus(DataStoreServiceKind dssTaskKind)
        {
            switch (dssTaskKind)
            {
                case ARCHIVIZATION:
                    return DataSetArchivizationStatus.ACTIVE;
                case UNARCHIVIZATION:
                    return DataSetArchivizationStatus.ARCHIVED;
                case LOCK:
                    return DataSetArchivizationStatus.ACTIVE;
                case UNLOCK:
                    return DataSetArchivizationStatus.LOCKED;
                default:
                    return null; // not possible
            }
        }

        @Override
        protected void executeConfirmedAction()
        {
            final IComputationAction computationAction = data.getComputationAction();
            final boolean computeOnSelected = getComputeOnSelected();
            computationAction.execute(null, computeOnSelected);
        }

        @Override
        protected void extendForm()
        {
            formPanel.setLabelWidth(LABEL_WIDTH);
            formPanel.setFieldWidth(FIELD_WIDTH);

            if (data.getSelectedDataSets().size() > 0)
            {
                formPanel.add(createComputationDataSetsRadio());
            }
            Button confirmButton = getButtonById(Dialog.OK);
            confirmButton.setText("Run");
        }

        private final RadioGroup createComputationDataSetsRadio()
        {
            return radioProvider.createComputationDataSetsRadio();
        }

        private boolean getComputeOnSelected()
        {
            return radioProvider.getComputeOnSelected();
        }

    }

    private static class PerformComputationDialog extends
            AbstractDataConfirmationDialog<ComputationData>
    {

        private static final int LABEL_WIDTH = ColumnConfigFactory.DEFAULT_COLUMN_WIDTH - 20;

        private static final int FIELD_WIDTH = 2 * ColumnConfigFactory.DEFAULT_COLUMN_WIDTH - 20;

        private static final int DIALOG_WIDTH = 4 * ColumnConfigFactory.DEFAULT_COLUMN_WIDTH + 30;

        private static final String BR = "<br/>";

        private final IViewContext<ICommonClientServiceAsync> viewContext;

        private List<String> selectedDataSetTypeCodes;

        // not null only if all selected datasets come from the same datastore
        private final DataStore dataStoreOrNull;

        private final ComputationDataSetsRadioProvider radioProvider;

        private Html selectedDataSetTypesText;

        private DataStoreServicesGrid servicesGrid;

        protected PerformComputationDialog(IViewContext<ICommonClientServiceAsync> viewContext,
                ComputationData data, String title)
        {
            super(viewContext, data, title);
            this.viewContext = viewContext;
            this.radioProvider = new ComputationDataSetsRadioProvider(data);

            this.dataStoreOrNull = tryGetSingleDatastore(data);
            setWidth(DIALOG_WIDTH);

            DialogWithOnlineHelpUtils.addHelpButton(viewContext, this, createHelpPageIdentifier());
        }

        @Override
        protected void initializeData()
        {
            super.initializeData();

            Set<DataSetType> selectedDataSetTypes = getSelectedDataSetTypes();
            selectedDataSetTypeCodes = new ArrayList<String>(selectedDataSetTypes.size());
            for (DataSetType type : selectedDataSetTypes)
            {
                selectedDataSetTypeCodes.add(type.getCode());
            }
        }

        private Set<DataSetType> getSelectedDataSetTypes()
        {
            Set<DataSetType> result = new TreeSet<DataSetType>();
            for (ExternalData dataSet : data.getSelectedDataSets())
            {
                result.add(dataSet.getDataSetType());
            }
            return result;
        }

        @Override
        protected String createMessage()
        {
            int size = data.getSelectedDataSets().size();
            String computationName = data.getDssTaskKind().getDescription();
            if (size == 0)
            {
                final String msgIntroduction = viewContext.getMessage(Dict.NO_DATASETS_SELECTED);
                return viewContext.getMessage(
                        Dict.PERFORM_COMPUTATION_ON_ALL_DATASETS_MSG_TEMPLATE, msgIntroduction,
                        computationName);
            } else
            {
                if (isSingleDatastore())
                {
                    return viewContext.getMessage(
                            Dict.PERFORM_COMPUTATION_ON_SELECTED_OR_ALL_DATASETS_MSG_TEMPLATE,
                            computationName, size);
                } else
                {
                    final String msgIntroduction =
                            viewContext.getMessage(Dict.DATASETS_FROM_DIFFERENT_STORES_SELECTED);
                    return viewContext.getMessage(
                            Dict.PERFORM_COMPUTATION_ON_ALL_DATASETS_MSG_TEMPLATE, msgIntroduction,
                            computationName);
                }
            }
        }

        @Override
        protected boolean validate()
        {
            final DatastoreServiceDescription selectedPluginTaskOrNull = tryGetSelectedPluginTask();
            if (selectedPluginTaskOrNull == null)
            {
                return false;
            }
            final boolean computeOnSelected = getComputeOnSelected();
            if (computeOnSelected)
            {
                // show error message if plugin does not support all types of selected data sets
                Set<String> supportedDataSetTypes =
                        getSupportedDataSetTypes(selectedPluginTaskOrNull);
                List<String> unsupportedDataSetTypes = new ArrayList<String>();
                for (String selectedDataSetType : selectedDataSetTypeCodes)
                {
                    if (supportedDataSetTypes.contains(selectedDataSetType) == false)
                    {
                        unsupportedDataSetTypes.add(selectedDataSetType);
                    }
                }
                if (unsupportedDataSetTypes.size() > 0)
                {
                    final String msg = createUnsupportedDataSetTypesText(unsupportedDataSetTypes);
                    MessageBox.alert("Error", msg, null);
                    return false;
                }
            }
            return super.validate();
        }

        @Override
        protected void executeConfirmedAction()
        {
            final IComputationAction computationAction = data.getComputationAction();
            final DatastoreServiceDescription selectedPluginTask = getSelectedPluginTask();
            final boolean computeOnSelected = getComputeOnSelected();
            computationAction.execute(selectedPluginTask, computeOnSelected);
        }

        private Set<String> getSupportedDataSetTypes(DatastoreServiceDescription plugin)
        {
            return new HashSet<String>(Arrays.asList(plugin.getDatasetTypeCodes()));
        }

        private DatastoreServiceDescription getSelectedPluginTask()
        {
            DatastoreServiceDescription selectedPluginOrNull = tryGetSelectedPluginTask();
            assert selectedPluginOrNull != null : "no plugin selected!";
            return selectedPluginOrNull;
        }

        private DatastoreServiceDescription tryGetSelectedPluginTask()
        {
            return servicesGrid.tryGetSelectedItem();
        }

        @Override
        protected void extendForm()
        {
            formPanel.setLabelWidth(LABEL_WIDTH);
            formPanel.setFieldWidth(FIELD_WIDTH);
            servicesGrid = new DataStoreServicesGrid(viewContext);

            if (data.getSelectedDataSets().size() > 0 && isSingleDatastore())
            {
                formPanel.add(createComputationDataSetsRadio());
                selectedDataSetTypesText = formPanel.addText(createSelectedDataSetTypesText());
                updateComputationDataSetsState();
            }

            formPanel.add(servicesGrid);
            loadAvailableServices();

            Button confirmButton = getButtonById(Dialog.OK);
            confirmButton.setText("Run");
            servicesGrid
                    .registerGridSelectionChangeListener(new Listener<SelectionChangedEvent<ModelData>>()
                        {
                            public void handleEvent(SelectionChangedEvent<ModelData> se)
                            {
                                updateOkButtonState();
                            }
                        });
        }

        private void loadAvailableServices()
        {
            viewContext.getService().listDataStoreServices(data.getDssTaskKind(),
                    new ListServicesDescriptionsCallback(viewContext));
        }

        private final class ListServicesDescriptionsCallback extends
                AbstractAsyncCallback<List<DatastoreServiceDescription>>
        {
            private ListServicesDescriptionsCallback(
                    final IViewContext<ICommonClientServiceAsync> viewContext)
            {
                super(viewContext);
            }

            @Override
            public final void process(final List<DatastoreServiceDescription> plugins)
            {
                servicesGrid.display(plugins);
                updateAvailablePlugins();
            }
        }

        private final String createSelectedDataSetTypesText()
        {
            return createDataSetTypeMsg("Types of selected Data Sets", selectedDataSetTypeCodes);
        }

        private final String createUnsupportedDataSetTypesText(List<String> dataSetTypes)
        {
            return createDataSetTypeMsg(
                    "Selected service does not support all types of selected Data Sets. " + BR + BR
                            + "Unsupported Data Set types", dataSetTypes);
        }

        private final String createDataSetTypeMsg(String msgPrefix, List<String> dataSetTypes)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(msgPrefix);
            sb.append(": ");
            sb.append(StringUtils.joinList(dataSetTypes));
            return sb.toString();
        }

        private final RadioGroup createComputationDataSetsRadio()
        {
            return radioProvider.createComputationDataSetsRadio();
        }

        private boolean getComputeOnSelected()
        {
            return radioProvider.getComputeOnSelected();
        }

        private final void updateComputationDataSetsState()
        {
            boolean showSelectedDataSetTypes = getComputeOnSelected();
            selectedDataSetTypesText.setVisible(showSelectedDataSetTypes);
        }

        private void updateAvailablePlugins()
        {
            if (getComputeOnSelected())
            {
                assert isSingleDatastore() : "cannot use selected datasets, they belong to different data store";
                servicesGrid.filterServicesByDataStore(dataStoreOrNull);
            } else
            {
                servicesGrid.filterServicesByDataStore(null);
            }
        }

        private boolean isSingleDatastore()
        {
            return dataStoreOrNull != null;
        }

        private static DataStore tryGetSingleDatastore(ComputationData data)
        {
            return tryGetSingleDatastore(data.getSelectedDataSets());
        }

        // if all datasets come from one datastore, that datastore is returned. Otherwise returns
        // null.
        private static DataStore tryGetSingleDatastore(List<ExternalData> datasets)
        {
            if (datasets.size() == 0)
            {
                return null;
            }
            DataStore store = datasets.get(0).getDataStore();
            for (ExternalData dataset : datasets)
            {
                if (store.equals(dataset.getDataStore()) == false)
                {
                    return null;
                }
            }
            return store;
        }

        private HelpPageIdentifier createHelpPageIdentifier()
        {
            return new HelpPageIdentifier(HelpPageIdentifier.HelpPageDomain.PERFORM_COMPUTATION,
                    HelpPageIdentifier.HelpPageAction.ACTION);
        }
    }

    private static interface IComputationAction
    {
        void execute(DatastoreServiceDescription pluginTask, boolean computeOnSelected);
    }

    private static class ComputationDataSetsRadioProvider
    {
        private Radio computeOnSelectedRadio;

        private Radio computeOnAllRadio;

        private final ComputationData data;

        public ComputationDataSetsRadioProvider(ComputationData data)
        {
            this.data = data;
        }

        public boolean getComputeOnSelected()
        {
            if (computeOnSelectedRadio == null)
            {
                return false;
            } else
            {
                return computeOnSelectedRadio.getValue();
            }
        }

        public final RadioGroup createComputationDataSetsRadio()
        {
            final RadioGroup result = new RadioGroup();
            result.setFieldLabel("Data Sets");
            result.setSelectionRequired(true);
            result.setOrientation(Orientation.HORIZONTAL);
            computeOnAllRadio = createRadio("all");
            computeOnSelectedRadio =
                    createRadio("selected (" + data.getSelectedDataSets().size() + ")");
            result.add(computeOnSelectedRadio);
            result.add(computeOnAllRadio);
            result.setValue(computeOnSelectedRadio);
            result.setAutoHeight(true);
            return result;
        }

        private final Radio createRadio(final String label)
        {
            Radio result = new Radio();
            result.setBoxLabel(label);
            return result;
        }
    }

}
