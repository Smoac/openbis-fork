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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.google.gwt.user.client.DOM;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.IActionMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.EntityGridModelFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data.CommonExternalDataColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractEntityBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesCriteriaProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DataSetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUploadParameters;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginTaskDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractExternalDataGrid
        extends
        AbstractEntityBrowserGrid<ExternalData, BaseEntityModel<ExternalData>, PropertyTypesCriteria>
{
    static final class UploadCallback extends AbstractAsyncCallback<String>
    {
        private UploadCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(String message)
        {
            if (message.length() > 0)
            {
                MessageBox.alert("Warning", message, null);
            }
        }
    }

    private final class UploadConfirmationDialog extends
            AbstractDataConfirmationDialog<List<ExternalData>>
    {
        private static final int FIELD_WIDTH_IN_UPLOAD_DIALOG = 200;

        private static final int LABEL_WIDTH_IN_UPLOAD_DIALOG = 120;

        private String cifexURL;

        private TextField<String> passwordField;

        private TextField<String> fileNameField;

        private TextArea commentField;

        private TextField<String> userField;

        public UploadConfirmationDialog(List<ExternalData> dataSets)
        {
            super(viewContext, dataSets, viewContext.getMessage(Dict.CONFIRM_DATASET_UPLOAD_TITLE));
            setWidth(LABEL_WIDTH_IN_UPLOAD_DIALOG + FIELD_WIDTH_IN_UPLOAD_DIALOG + 50);
        }

        @Override
        protected String createMessage()
        {
            return viewContext.getMessage(Dict.CONFIRM_DATASET_UPLOAD_MSG, data.size(), cifexURL);
        }

        @Override
        protected final void extendForm()
        {
            formPanel.setLabelWidth(LABEL_WIDTH_IN_UPLOAD_DIALOG);
            formPanel.setFieldWidth(FIELD_WIDTH_IN_UPLOAD_DIALOG);
            formPanel.setBodyBorder(false);
            formPanel.setHeaderVisible(false);

            fileNameField = new TextField<String>();
            fileNameField.setFieldLabel(viewContext
                    .getMessage(Dict.CONFIRM_DATASET_UPLOAD_FILE_NAME_FIELD));
            fileNameField.setSelectOnFocus(true);
            fileNameField.setMaxLength(BasicConstant.MAX_LENGTH_OF_FILE_NAME + ".zip".length());
            fileNameField.setAutoValidate(true);
            fileNameField.addKeyListener(keyListener);
            formPanel.add(fileNameField);

            commentField = new TextArea();
            commentField.setMaxLength(BasicConstant.MAX_LENGTH_OF_CIFEX_COMMENT);
            commentField.setFieldLabel(viewContext
                    .getMessage(Dict.CONFIRM_DATASET_UPLOAD_COMMENT_FIELD));
            commentField.addKeyListener(keyListener);
            commentField.setAutoValidate(true);
            formPanel.add(commentField);

            userField = new TextField<String>();
            userField.setFieldLabel(viewContext.getMessage(Dict.CONFIRM_DATASET_UPLOAD_USER_FIELD));
            userField.setValue(viewContext.getModel().getSessionContext().getUser().getUserName());
            FieldUtil.setMandatoryFlag(userField, true);
            userField.addKeyListener(keyListener);
            userField.setAutoValidate(true);
            formPanel.add(userField);

            passwordField = new TextField<String>();
            passwordField.setPassword(true);
            passwordField.setFieldLabel(viewContext
                    .getMessage(Dict.CONFIRM_DATASET_UPLOAD_PASSWORD_FIELD));
            FieldUtil.setMandatoryFlag(passwordField, true);
            passwordField.addKeyListener(keyListener);
            passwordField.setAutoValidate(true);
            formPanel.add(passwordField);
        }

        @Override
        protected void executeConfirmedAction()
        {
            DataSetUploadParameters parameters = new DataSetUploadParameters();
            parameters.setCifexURL(cifexURL);
            parameters.setFileName(fileNameField.getValue());
            parameters.setComment(commentField.getValue());
            parameters.setUserID(userField.getValue());
            parameters.setPassword(passwordField.getValue());
            viewContext.getCommonService().uploadDataSets(getDataSetCodes(data), parameters,
                    new UploadCallback(viewContext));
        }
    }

    private List<String> getDataSetCodes(List<ExternalData> dataSets)
    {
        List<String> dataSetCodes = new ArrayList<String>();
        for (ExternalData externalData : dataSets)
        {
            dataSetCodes.add(externalData.getCode());
        }
        return dataSetCodes;
    }

    public static final String SHOW_DETAILS_BUTTON_ID_SUFFIX = "_show-details-button";

    /**
     * @param displayOnlyDatasetProperties if false the grid columns will consist of all property
     *            types relevant anyhow to datasets, not only property types directly connected to
     *            datasets.
     */
    protected AbstractExternalDataGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            String browserId, String gridId, boolean displayOnlyDatasetProperties)
    {

        super(viewContext, gridId, false, false, createCriteriaProvider(viewContext,
                displayOnlyDatasetProperties));
        setId(browserId);
        setEntityKindForDisplayTypeIDGeneration(EntityKind.DATA_SET);
        super.updateCriteriaProviderAndRefresh();

        addEntityOperationsLabel();
        pagingToolbar.add(createComputeMenu());
        addButton(createBrowseExternalDataButton());
        addButton(createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_SHOW_DETAILS),
                browserId + SHOW_DETAILS_BUTTON_ID_SUFFIX, asShowEntityInvoker(false)));
        addButton(createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_EDIT),
                asShowEntityInvoker(true)));
        addButton(createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_DELETE),
                new AbstractCreateDialogListener()
                    {
                        @Override
                        protected Dialog createDialog(List<ExternalData> dataSets,
                                IBrowserGridActionInvoker invoker)
                        {
                            return new DataSetListDeletionConfirmationDialog(viewContext, dataSets,
                                    createDeletionCallback(invoker));
                        }
                    }));
        addButton(createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_UPLOAD_DATASETS),
                new AbstractCreateDialogListener()
                    {
                        @Override
                        protected Dialog createDialog(List<ExternalData> dataSets,
                                IBrowserGridActionInvoker invoker)
                        {
                            return new UploadConfirmationDialog(dataSets);
                        }
                    }));
        addEntityOperationsSeparator();
        allowMultipleSelection();

        registerLinkClickListenerFor(CommonExternalDataColDefKind.PARENT.id(),
                new ICellListener<ExternalData>()
                    {
                        public void handle(ExternalData rowItem)
                        {
                            // don't need to check whether the value is null
                            // because there will not be a link for null value
                            final ExternalData parent = rowItem.getParent();

                            final IEntityInformationHolder entity = parent;
                            new OpenEntityDetailsTabAction(entity, viewContext).execute();
                        }
                    });

    }

    private Button createBrowseExternalDataButton()
    {
        String text = viewContext.getMessage(Dict.BUTTON_VIEW);
        String title = viewContext.getMessage(Dict.TOOLTIP_VIEW_DATASET);

        Button result = createSelectedItemButton(text, asBrowseExternalDataInvoker());
        result.setTitle(title);
        return result;
    }

    private final ISelectedEntityInvoker<BaseEntityModel<ExternalData>> asBrowseExternalDataInvoker()
    {
        return new ISelectedEntityInvoker<BaseEntityModel<ExternalData>>()
            {
                public void invoke(BaseEntityModel<ExternalData> selectedItem)
                {
                    if (selectedItem != null)
                    {
                        DataSetUtils.showDataSet(selectedItem.getBaseObject(), viewContext
                                .getModel());
                    }
                }
            };
    }

    private static ICriteriaProvider<PropertyTypesCriteria> createCriteriaProvider(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            boolean displayOnlyDatasetProperties)
    {
        EntityKind entityKindOrNull = displayOnlyDatasetProperties ? EntityKind.DATA_SET : null;
        return new PropertyTypesCriteriaProvider(viewContext, entityKindOrNull);
    }

    @Override
    protected BaseEntityModel<ExternalData> createModel(ExternalData entity)
    {
        BaseEntityModel<ExternalData> model = getColumnsFactory().createModel(entity);
        model.renderAsLinkWithAnchor(CommonExternalDataColDefKind.PARENT.id());
        renderShowDetailsLinkAsLink(model);
        return model;
    }

    private void renderShowDetailsLinkAsLink(ModelData model)
    {
        String showDetailsLinkID = CommonExternalDataColDefKind.SHOW_DETAILS_LINK.id();
        String originalValue = model.get(showDetailsLinkID);
        model.set(showDetailsLinkID, LinkRenderer.renderAsLinkWithAnchor(viewContext
                .getMessage(Dict.SHOW_DETAILS_LINK_TEXT_VALUE), originalValue, true));
    }

    @Override
    protected ColumnDefsAndConfigs<ExternalData> createColumnsDefinition()
    {
        return getColumnsFactory().createColumnsSchema(viewContext, criteria.tryGetPropertyTypes());
    }

    private EntityGridModelFactory<ExternalData> getColumnsFactory()
    {
        return new EntityGridModelFactory<ExternalData>(getStaticColumnsDefinition());
    }

    @Override
    protected IColumnDefinitionKind<ExternalData>[] getStaticColumnsDefinition()
    {
        return CommonExternalDataColDefKind.values();
    }

    @Override
    protected List<IColumnDefinition<ExternalData>> getInitialFilters()
    {
        return asColumnFilters(new CommonExternalDataColDefKind[]
            { CommonExternalDataColDefKind.CODE, CommonExternalDataColDefKind.FILE_FORMAT_TYPE });
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<ExternalData> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportDataSetSearchHits(exportCriteria, callback);
    }

    @Override
    public Set<DatabaseModificationKind> getGridRelevantModifications()
    {
        return getGridRelevantModifications(ObjectKind.DATA_SET);
    }

    @Override
    protected boolean hasColumnsDefinitionChanged(PropertyTypesCriteria newCriteria)
    {
        List<PropertyType> newPropertyTypes = newCriteria.tryGetPropertyTypes();
        List<PropertyType> prevPropertyTypes =
                (criteria == null ? null : criteria.tryGetPropertyTypes());
        if (newPropertyTypes == null)
        {
            return false; // nothing chosen
        }
        if (prevPropertyTypes == null)
        {
            return true; // first selection
        }
        return newPropertyTypes.equals(prevPropertyTypes);
    }

    @Override
    protected String createHeader()
    {
        return null;
    }

    @Override
    protected void showEntityViewer(ExternalData dataSet, boolean editMode)
    {
        final EntityKind entityKind = EntityKind.DATA_SET;
        ITabItemFactory tabView;
        final IClientPluginFactory clientPluginFactory =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        dataSet.getDataSetType());

        final IClientPlugin<EntityType, IIdentifiable> createClientPlugin =
                clientPluginFactory.createClientPlugin(entityKind);
        if (editMode)
        {
            tabView = createClientPlugin.createEntityEditor(dataSet);
        } else
        {
            tabView = createClientPlugin.createEntityViewer(dataSet);
        }
        DispatcherHelper.dispatchNaviEvent(tabView);
    }

    //
    // Compute menu
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

    private final ToolItem createComputeMenu()
    {
        TextToolItem computeMenu = new TextToolItem(viewContext.getMessage(Dict.MENU_COMPUTE));

        Menu menu = new Menu();
        addMenuItem(menu, ActionMenuKind.COMPUTE_MENU_QUERIES);
        addMenuItem(menu, ActionMenuKind.COMPUTE_MENU_PROCESSING);

        computeMenu.setMenu(menu);
        return computeMenu;
    }

    private final void addMenuItem(Menu menu, ActionMenuKind menuItemKind)
    {
        final IDelegatedAction menuItemAction =
                createComputeMenuAction(menuItemKind.getMenuText(viewContext));
        menu.add(new ActionMenu(menuItemKind, viewContext, menuItemAction));
    }

    private IDelegatedAction createComputeMenuAction(final String computationName)
    {
        return new IDelegatedAction()
            {

                public void execute()
                {
                    List<PluginTaskDescription> plugins = getPlugins();
                    List<BaseEntityModel<ExternalData>> items = getSelectedItems();
                    List<ExternalData> selectedDataSets = new ArrayList<ExternalData>();
                    Set<DataSetType> selectedDataSetTypes = new HashSet<DataSetType>();
                    if (items.isEmpty() == false)
                    {
                        for (BaseEntityModel<ExternalData> item : items)
                        {
                            ExternalData dataSet = item.getBaseObject();
                            selectedDataSets.add(dataSet);
                            selectedDataSetTypes.add(dataSet.getDataSetType());
                        }
                    }
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
                    return new PerformComputationDialog(viewContext, data, title);
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
