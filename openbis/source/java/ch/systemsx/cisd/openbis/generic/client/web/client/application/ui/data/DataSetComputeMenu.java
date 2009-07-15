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

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.IActionMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid.SelectedAndDisplayedItems;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;

/**
 * 'Compute' menu for Data Sets.
 * 
 * @author Piotr Buczek
 */
public class DataSetComputeMenu extends TextToolItem
{

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final IDelegatedActionWithResult<SelectedAndDisplayedItems> selectedDataSetsGetter;

    public DataSetComputeMenu(IViewContext<ICommonClientServiceAsync> viewContext,
            IDelegatedActionWithResult<SelectedAndDisplayedItems> selectedDataSetsGetter)
    {
        super(viewContext.getMessage(Dict.MENU_COMPUTE));
        this.viewContext = viewContext;
        this.selectedDataSetsGetter = selectedDataSetsGetter;

        Menu menu = new Menu();
        addMenuItem(menu, PluginTaskActionMenuKind.COMPUTE_MENU_QUERIES);
        addMenuItem(menu, PluginTaskActionMenuKind.COMPUTE_MENU_PROCESSING);
        setMenu(menu);
    }

    //

    /** {@link ActionMenu} kind enum with names matching dictionary keys */
    public static enum PluginTaskActionMenuKind implements IActionMenuItem
    {
        COMPUTE_MENU_QUERIES(DataStoreServiceKind.QUERIES), COMPUTE_MENU_PROCESSING(
                DataStoreServiceKind.PROCESSING);

        private final DataStoreServiceKind pluginTaskKind;

        PluginTaskActionMenuKind(DataStoreServiceKind pluginTaskKind)
        {
            this.pluginTaskKind = pluginTaskKind;
        }

        public DataStoreServiceKind getPluginTaskKind()
        {
            return pluginTaskKind;
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

    private final void addMenuItem(Menu menu, PluginTaskActionMenuKind menuItemKind)
    {
        final IDelegatedAction menuItemAction =
                createComputeMenuAction(menuItemKind.getPluginTaskKind());
        menu.add(new ActionMenu(menuItemKind, viewContext, menuItemAction));
    }

    private IDelegatedAction createComputeMenuAction(final DataStoreServiceKind pluginTaskKind)
    {
        return new IDelegatedAction()
            {

                public void execute()
                {
                    final SelectedAndDisplayedItems selectedAndDisplayedItems =
                            selectedDataSetsGetter.execute();
                    final IComputationAction computationAction =
                            createComputationAction(selectedAndDisplayedItems);
                    final ComputationData data =
                            new ComputationData(pluginTaskKind, computationAction,
                                    selectedAndDisplayedItems);
                    createPerformComputationDialog(data).show();
                }

                private Window createPerformComputationDialog(ComputationData data)
                {
                    final String title =
                            "Perform " + pluginTaskKind.getDescription() + " Computation";
                    return new PerformComputationDialog(viewContext, data, title);
                }

                private IComputationAction createComputationAction(
                        final SelectedAndDisplayedItems selectedAndDisplayedItems)
                {
                    return new IComputationAction()
                        {
                            public void execute(DatastoreServiceDescription service,
                                    boolean computeOnSelected)
                            {
                                DisplayedOrSelectedDatasetCriteria criteria =
                                        createCriteria(selectedAndDisplayedItems, computeOnSelected);
                                if (pluginTaskKind == DataStoreServiceKind.QUERIES)
                                {
                                    Dialog progressBar = createAndShowProgressBar();
                                    viewContext.getService().createReportFromDatasets(service,
                                            criteria,
                                            new ReportDisplayCallback(viewContext, progressBar));
                                } else
                                {
                                    viewContext.getService().processDatasets(service, criteria,
                                            new ProcessingDisplayCallback(viewContext));
                                }
                            }
                        };
                }

            };
    }

    private static Dialog createAndShowProgressBar()
    {
        ProgressBar progressBar = new ProgressBar();
        progressBar.auto();

        Dialog dialog = new Dialog();
        String title = "Generating the report...";
        dialog.setTitle(title);

        dialog.add(progressBar);
        dialog.setButtons("");
        dialog.setAutoHeight(true);
        dialog.setClosable(false);
        dialog.addText(title);
        dialog.setResizable(false);
        dialog.show();
        return dialog;
    }

    private static DisplayedOrSelectedDatasetCriteria createCriteria(
            SelectedAndDisplayedItems selectedAndDisplayedItems, boolean computeOnSelected)
    {
        return selectedAndDisplayedItems.createCriteria(computeOnSelected);
    }

    public final class ProcessingDisplayCallback extends AbstractAsyncCallback<Void>
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

    private static final class ReportDisplayCallback extends
            AbstractAsyncCallback<TableModelReference>
    {
        private final IViewContext<ICommonClientServiceAsync> viewContext;

        private final Dialog progressBar;

        public ReportDisplayCallback(IViewContext<ICommonClientServiceAsync> viewContext,
                Dialog progressBar)
        {
            super(viewContext);
            this.viewContext = viewContext;
            this.progressBar = progressBar;
        }

        @Override
        protected void process(final TableModelReference tableModelReference)
        {
            progressBar.close();
            final ITabItemFactory tabFactory = new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        IDisposableComponent component =
                                DataSetReporterGrid.create(viewContext, tableModelReference);
                        return DefaultTabItem.create("Data Store Report", component, viewContext);
                    }

                    public String getId()
                    {
                        return DataSetReporterGrid.createId(tableModelReference.getResultSetKey());
                    }
                };
            DispatcherHelper.dispatchNaviEvent(tabFactory);
        }
    }

    private class ComputationData
    {
        private final DataStoreServiceKind pluginTaskKind;

        private final IComputationAction computationAction;

        private final SelectedAndDisplayedItems selectedAndDisplayedItems;

        public ComputationData(DataStoreServiceKind pluginTaskKind,
                IComputationAction computationAction,
                SelectedAndDisplayedItems selectedAndDisplayedItems)
        {
            super();
            this.pluginTaskKind = pluginTaskKind;
            this.computationAction = computationAction;
            this.selectedAndDisplayedItems = selectedAndDisplayedItems;
        }

        public DataStoreServiceKind getPluginTaskKind()
        {
            return pluginTaskKind;
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

    private class PerformComputationDialog extends AbstractDataConfirmationDialog<ComputationData>
    {

        private static final int LABEL_WIDTH = ColumnConfigFactory.DEFAULT_COLUMN_WIDTH - 20;

        private static final int FIELD_WIDTH = 2 * ColumnConfigFactory.DEFAULT_COLUMN_WIDTH - 20;

        private static final int DIALOG_WIDTH = 4 * ColumnConfigFactory.DEFAULT_COLUMN_WIDTH + 30;

        private static final String BR = "<br/>";

        private List<String> selectedDataSetTypeCodes;

        private Radio computeOnSelectedRadio;

        private Radio computeOnAllRadio;

        private Html selectedDataSetTypesText;

        private DataStoreServicesGrid pluginTasksGrid;

        protected PerformComputationDialog(IViewContext<ICommonClientServiceAsync> messageProvider,
                ComputationData data, String title)
        {
            super(messageProvider, data, title);
            setWidth(DIALOG_WIDTH);
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
            String computationName = data.getPluginTaskKind().getDescription();
            // TODO 2009-07-03, Piotr Buczek: externalize to dictionary with parameters
            switch (size)
            {
                case 0:
                    return "No Data Sets were selected. "
                            + "Select a data store service to perform " + computationName
                            + " computation on all Data Sets "
                            + "of appropriate types and click on a Run button.";
                case 1:
                    return "Select between performing " + computationName
                            + " computation only on selected Data Sets "
                            + "or on all Data Sets of appropriate types, "
                            + "then select a data store service and click on a Run button.";
                default:
                    return "Select between performing " + computationName + " computation only on "
                            + size
                            + " selected Data Sets or on all Data Sets of appropriate types, "
                            + "then select a data store service and click on a Run button.";
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
            return pluginTasksGrid.tryGetSelectedItem();
        }

        private boolean getComputeOnSelected()
        {
            if (computeOnSelectedRadio == null)
            {
                return false;
            } else
            {
                return computeOnSelectedRadio.getValue();
            }
        }

        @Override
        protected void extendForm()
        {
            formPanel.setLabelWidth(LABEL_WIDTH);
            formPanel.setFieldWidth(FIELD_WIDTH);

            if (data.getSelectedDataSets().size() > 0)
            {
                formPanel.add(createComputationDataSetsRadio());
                selectedDataSetTypesText = formPanel.addText(createSelectedDataSetTypesText());
                updateComputationDataSetsState();
            }

            pluginTasksGrid = new DataStoreServicesGrid(viewContext, data.getPluginTaskKind());
            formPanel.add(pluginTasksGrid);

            Button confirmButton = getButtonById(Dialog.OK);
            confirmButton.setText("Run");
            pluginTasksGrid
                    .registerGridSelectionChangeListener(new Listener<SelectionEvent<ModelData>>()
                        {
                            public void handleEvent(SelectionEvent<ModelData> se)
                            {
                                updateOkButtonState();
                            }
                        });
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
            final RadioGroup result = new RadioGroup();
            result.setFieldLabel("Data Sets");
            result.setSelectionRequired(true);
            result.setOrientation(Orientation.HORIZONTAL);
            result.addListener(Events.Change, new Listener<BaseEvent>()
                {
                    public void handleEvent(BaseEvent be)
                    {
                        updateComputationDataSetsState();
                        updateOkButtonState();
                    }
                });
            computeOnAllRadio = createRadio("all");
            computeOnSelectedRadio =
                    createRadio("selected (" + data.getSelectedDataSets().size() + ")");
            result.add(computeOnSelectedRadio);
            result.add(computeOnAllRadio);
            result.setValue(computeOnSelectedRadio);
            result.setAutoHeight(true);
            return result;
        }

        private final void updateComputationDataSetsState()
        {
            boolean showSelectedDataSetTypes = getComputeOnSelected();
            selectedDataSetTypesText.setVisible(showSelectedDataSetTypes);
        }

        private final Radio createRadio(final String label)
        {
            Radio result = new Radio();
            result.setBoxLabel(label);
            return result;
        }

    }

    private static interface IComputationAction
    {
        void execute(DatastoreServiceDescription pluginTask, boolean computeOnSelected);
    }

}