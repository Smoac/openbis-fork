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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.google.gwt.user.client.DOM;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.IActionMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginTaskDescription;

/**
 * 'Compute' menu for Data Sets.
 * 
 * @author Piotr Buczek
 */
public class DataSetComputeMenu extends TextToolItem
{
    private final IMessageProvider messageProvider;

    private final IDelegatedActionWithResult<List<ExternalData>> getSelectedDataSetsAction;

    public DataSetComputeMenu(IMessageProvider messageProvider,
            IDelegatedActionWithResult<List<ExternalData>> getSelectedDataSetsAction)
    {
        super(messageProvider.getMessage(Dict.MENU_COMPUTE));
        this.messageProvider = messageProvider;
        this.getSelectedDataSetsAction = getSelectedDataSetsAction;

        Menu menu = new Menu();
        addMenuItem(menu, ActionMenuKind.COMPUTE_MENU_QUERIES);
        addMenuItem(menu, ActionMenuKind.COMPUTE_MENU_PROCESSING);
        setMenu(menu);
    }

    //

    /** {@link ActionMenu} kind enum with names matching dictionary keys */
    public static enum ActionMenuKind implements IActionMenuItem
    {
        COMPUTE_MENU_QUERIES, COMPUTE_MENU_PROCESSING;

        public String getMenuId()
        {
            return this.name();
        }

        public String getMenuText(IMessageProvider messageProvider)
        {
            return messageProvider.getMessage(this.name());
        }
    }

    private final void addMenuItem(Menu menu, ActionMenuKind menuItemKind)
    {
        final IDelegatedAction menuItemAction =
                createComputeMenuAction(menuItemKind.getMenuText(messageProvider));
        menu.add(new ActionMenu(menuItemKind, messageProvider, menuItemAction));
    }

    private IDelegatedAction createComputeMenuAction(final String computationName)
    {
        return new IDelegatedAction()
            {

                public void execute()
                {
                    List<PluginTaskDescription> plugins = getPlugins();
                    List<ExternalData> selectedDataSets = getSelectedDataSetsAction.execute();
                    createPerformComputationDialog(plugins, selectedDataSets, null).show();
                }

                private List<PluginTaskDescription> getPlugins()
                {
                    List<PluginTaskDescription> plugins = new ArrayList<PluginTaskDescription>();
                    // TODO fill with plugins from server
                    String[] testDataSetTypeCodes =
                        { "UNKNOWN", "HCS_IMAGE", "HCS_IMAGE_ANALYSIS_DATA" };
                    plugins.add(new PluginTaskDescription("key1", "label1", new String[]
                        { testDataSetTypeCodes[0], testDataSetTypeCodes[1] }));
                    plugins.add(new PluginTaskDescription("key2", "label2", new String[]
                        { testDataSetTypeCodes[1], testDataSetTypeCodes[2] }));
                    plugins.add(new PluginTaskDescription("key3", "label3", new String[]
                        { testDataSetTypeCodes[2], testDataSetTypeCodes[0] }));
                    return plugins;
                }

                private Window createPerformComputationDialog(
                        final List<PluginTaskDescription> plugins,
                        final List<ExternalData> selectedDataSets,
                        final IComputationAction computationAction)
                {
                    final String title = "Perform " + computationName + " Computation";
                    final ComputationData data =
                            new ComputationData(computationName, computationAction,
                                    selectedDataSets, plugins);
                    return new PerformComputationDialog(messageProvider, data, title);
                }

            };
    }

    private class ComputationData
    {
        private final String computationName;

        private final IComputationAction computationAction;

        private final List<ExternalData> selectedDataSets;

        private final List<PluginTaskDescription> plugins;

        public ComputationData(String computationName, IComputationAction computationAction,
                List<ExternalData> selectedDataSets, List<PluginTaskDescription> plugins)
        {
            super();
            this.computationName = computationName;
            this.computationAction = computationAction;
            this.selectedDataSets = selectedDataSets;
            this.plugins = plugins;
        }

        public String getComputationName()
        {
            return computationName;
        }

        public IComputationAction getComputationAction()
        {
            return computationAction;
        }

        public List<ExternalData> getSelectedDataSets()
        {
            return selectedDataSets;
        }

        public List<PluginTaskDescription> getPlugins()
        {
            return plugins;
        }
    }

    private class PerformComputationDialog extends AbstractDataConfirmationDialog<ComputationData>
    {

        private static final int LABEL_WIDTH = 80;

        private static final int FIELD_WIDTH = 180;

        private Radio computeOnSelected;

        private Radio computeOnAll;

        private Html selectedDataSetTypesText;

        protected PerformComputationDialog(IMessageProvider messageProvider, ComputationData data,
                String title)
        {
            super(messageProvider, data, title);
            setWidth(LABEL_WIDTH + FIELD_WIDTH + 50);
        }

        @Override
        protected String createMessage()
        {
            int size = data.getSelectedDataSets().size();
            String computationName = data.getComputationName();
            // TODO 2009-07-03, Piotr Buczek: externalize to dictionary with parameters
            switch (size)
            {
                case 0:
                    return "No Data Sets were selected. " + "Select a plugin to perform "
                            + computationName + " computation on all Data Sets "
                            + "of appropriate types and click on Run button.";
                case 1:
                    return "Select between performing " + computationName
                            + " computation only on selected Data Set "
                            + "or on all Data Sets of appropriate types, "
                            + "then select a plugin and click on Run button.";
                default:
                    return "Select between performing " + computationName + " computation only on "
                            + size
                            + "selected Data Sets or on all Data Sets of appropriate types, "
                            + "then select a plugin and click on Run button.";
            }
        }

        @Override
        protected void executeConfirmedAction()
        {
            final IComputationAction computationAction = data.getComputationAction();
            final List<PluginTaskDescription> plugins = data.getPlugins();
            computationAction.execute(plugins.get(0), getComputeOnSelectedValue(), data
                    .getSelectedDataSets());
        }

        private boolean getComputeOnSelectedValue()
        {
            return computeOnSelected.getValue();
        }

        @Override
        protected void extendForm()
        {
            formPanel.setLabelWidth(LABEL_WIDTH);
            formPanel.setFieldWidth(FIELD_WIDTH);

            getButtonById(Dialog.OK).setText("Run");

            if (data.getSelectedDataSets().size() > 0)
            {
                formPanel.add(createInputRadio());
                selectedDataSetTypesText = formPanel.addText(createSelectedDataSetTypesText());
            }
        }

        private final String createSelectedDataSetTypesText()
        {
            Set<DataSetType> selectedDataSetTypes = createSelectedDataSetTypes();
            List<String> codes = new ArrayList<String>(selectedDataSetTypes.size());
            for (DataSetType type : selectedDataSetTypes)
            {
                codes.add(type.getCode());
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Type(s) of selected Data Set(s): " + DOM.toString(DOM.createElement("br")));
            sb.append(StringUtils.joinList(codes));
            return sb.toString();
        }

        private Set<DataSetType> createSelectedDataSetTypes()
        {
            Set<DataSetType> result = new TreeSet<DataSetType>();
            for (ExternalData dataSet : data.getSelectedDataSets())
            {
                result.add(dataSet.getDataSetType());
            }
            return result;
        }

        private final RadioGroup createInputRadio()
        {
            final RadioGroup result = new RadioGroup();
            result.setFieldLabel("Computation Data Sets");
            result.setSelectionRequired(true);
            result.setOrientation(Orientation.HORIZONTAL);
            result.addListener(Events.Change, new Listener<BaseEvent>()
                {
                    public void handleEvent(BaseEvent be)
                    {
                        boolean showSelectedDataSetTypes = getComputeOnSelectedValue();
                        selectedDataSetTypesText.setVisible(showSelectedDataSetTypes);
                    }
                });

            computeOnAll = createRadio("all");
            computeOnSelected = createRadio("selected");
            result.add(computeOnAll);
            result.add(computeOnSelected);
            result.setValue(computeOnAll);
            return result;
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
        void execute(PluginTaskDescription plugin, boolean computeOnSelected,
                List<ExternalData> selectedDataSets);
    }

}