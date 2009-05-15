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

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DataSetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUploadParameters;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractExternalDataGrid
        extends
        AbstractEntityBrowserGrid<ExternalData, BaseEntityModel<ExternalData>, PropertyTypesCriteria>
{
    private static abstract class AbstractConfirmationDialog extends Dialog
    {
        protected final IViewContext<?> viewContext;

        protected final List<String> dataSetCodes;

        protected final IBrowserGridActionInvoker invoker;

        AbstractConfirmationDialog(IViewContext<?> viewContext, List<ExternalData> dataSets,
                IBrowserGridActionInvoker invoker, String titleKey)
        {
            this.viewContext = viewContext;
            this.invoker = invoker;
            dataSetCodes = new ArrayList<String>();
            for (ExternalData externalData : dataSets)
            {
                dataSetCodes.add(externalData.getCode());
            }
            setHeading(viewContext.getMessage(titleKey));
            setButtons(Dialog.OKCANCEL);
            setHideOnButtonClick(true);
            setModal(true);
        }

    }

    static final class DeletionCallback extends AbstractAsyncCallback<Void>
    {
        private final IBrowserGridActionInvoker invoker;

        private DeletionCallback(IViewContext<?> viewContext, IBrowserGridActionInvoker invoker)
        {
            super(viewContext);
            this.invoker = invoker;
        }

        @Override
        protected void process(Void result)
        {
            invoker.refresh();
        }
    }

    private static final class DeletionConfirmationDialog extends AbstractConfirmationDialog
    {
        private final TextField<String> reason;

        public DeletionConfirmationDialog(IViewContext<?> viewContext, List<ExternalData> dataSets,
                IBrowserGridActionInvoker invoker)
        {
            super(viewContext, dataSets, invoker, Dict.CONFIRM_DATASET_DELETION_TITLE);
            addText(viewContext.getMessage(Dict.CONFIRM_DATASET_DELETION_MSG, dataSets.size()));
            reason = new TextField<String>();
            reason.setSelectOnFocus(true);
            reason.setHideLabel(true);
            reason.setWidth("100%");
            reason.setMaxLength(250);
            reason.addKeyListener(new KeyListener()
                {
                    @Override
                    public void handleEvent(ComponentEvent ce)
                    {
                        okBtn.setEnabled(reason.isValid());
                    }
                });
            add(reason);
        }

        @Override
        protected void onButtonPressed(Button button)
        {
            super.onButtonPressed(button);
            if (button.getItemId().equals(Dialog.OK) && reason.isValid())
            {
                viewContext.getCommonService().deleteDataSets(dataSetCodes, reason.getValue(),
                        new DeletionCallback(viewContext, invoker));
            }
        }
    }

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

    private static final class UploadConfirmationDialog extends AbstractConfirmationDialog
    {
        private static final int FIELD_WIDTH_IN_UPLOAD_DIALOG = 200;

        private static final int LABEL_WIDTH_IN_UPLOAD_DIALOG = 120;

        private final String cifexURL;

        private final TextField<String> passwordField;

        private TextField<String> fileNameField;

        private TextArea commentField;

        private TextField<String> userField;

        private FormPanel form;

        public UploadConfirmationDialog(IViewContext<?> viewContext, List<ExternalData> dataSets,
                IBrowserGridActionInvoker invoker)
        {
            super(viewContext, dataSets, invoker, Dict.CONFIRM_DATASET_UPLOAD_TITLE);
            cifexURL = viewContext.getModel().getApplicationInfo().getCIFEXURL();
            addText(viewContext.getMessage(Dict.CONFIRM_DATASET_UPLOAD_MSG, dataSets.size(),
                    cifexURL));
            form = new FormPanel();
            form.setLabelWidth(LABEL_WIDTH_IN_UPLOAD_DIALOG);
            form.setFieldWidth(FIELD_WIDTH_IN_UPLOAD_DIALOG);
            form.setBodyBorder(false);
            form.setHeaderVisible(false);
            fileNameField = new TextField<String>();
            fileNameField.setFieldLabel(viewContext
                    .getMessage(Dict.CONFIRM_DATASET_UPLOAD_FILE_NAME_FIELD));
            fileNameField.setSelectOnFocus(true);
            fileNameField.setMaxLength(BasicConstant.MAX_LENGTH_OF_FILE_NAME + ".zip".length());
            fileNameField.setAutoValidate(true);
            KeyListener keyListener = new KeyListener()
                {
                    @Override
                    public void handleEvent(ComponentEvent ce)
                    {
                        okBtn.setEnabled(form.isValid());
                    }
                };
            fileNameField.addKeyListener(keyListener);
            form.add(fileNameField);
            commentField = new TextArea();
            commentField.setMaxLength(BasicConstant.MAX_LENGTH_OF_CIFEX_COMMENT);
            commentField.setFieldLabel(viewContext
                    .getMessage(Dict.CONFIRM_DATASET_UPLOAD_COMMENT_FIELD));
            commentField.addKeyListener(keyListener);
            commentField.setAutoValidate(true);
            form.add(commentField);
            userField = new TextField<String>();
            userField.setFieldLabel(viewContext.getMessage(Dict.CONFIRM_DATASET_UPLOAD_USER_FIELD));
            userField.setValue(viewContext.getModel().getSessionContext().getUser().getUserName());
            FieldUtil.setMandatoryFlag(userField, true);
            userField.addKeyListener(keyListener);
            userField.setAutoValidate(true);
            form.add(userField);
            passwordField = new TextField<String>();
            passwordField.setPassword(true);
            passwordField.setFieldLabel(viewContext
                    .getMessage(Dict.CONFIRM_DATASET_UPLOAD_PASSWORD_FIELD));
            FieldUtil.setMandatoryFlag(passwordField, true);
            passwordField.addKeyListener(keyListener);
            passwordField.setAutoValidate(true);
            form.add(passwordField);
            add(form);
            setWidth(LABEL_WIDTH_IN_UPLOAD_DIALOG + FIELD_WIDTH_IN_UPLOAD_DIALOG + 50);
        }

        @Override
        protected void onRender(Element parent, int pos)
        {
            super.onRender(parent, pos);
            okBtn.setEnabled(false);
        }

        @Override
        protected void onButtonPressed(Button button)
        {
            super.onButtonPressed(button);
            if (button.getItemId().equals(Dialog.OK) && form.isValid())
            {
                DataSetUploadParameters parameters = new DataSetUploadParameters();
                parameters.setCifexURL(cifexURL);
                parameters.setFileName(fileNameField.getValue());
                parameters.setComment(commentField.getValue());
                parameters.setUserID(userField.getValue());
                parameters.setPassword(passwordField.getValue());
                viewContext.getCommonService().uploadDataSets(dataSetCodes, parameters,
                        new UploadCallback(viewContext));
            }
        }
    }

    private abstract class AbstractDataSetAction extends SelectionListener<ButtonEvent>
    {
        @Override
        public void componentSelected(ButtonEvent ce)
        {
            List<BaseEntityModel<ExternalData>> items = getSelectedItems();
            if (items.isEmpty() == false)
            {
                List<ExternalData> dataSets = new ArrayList<ExternalData>();
                for (BaseEntityModel<ExternalData> item : items)
                {
                    dataSets.add(item.getBaseObject());
                }
                IBrowserGridActionInvoker invoker = asActionInvoker();
                createDialog(dataSets, invoker).show();
            }
        }

        protected abstract Dialog createDialog(List<ExternalData> dataSets,
                IBrowserGridActionInvoker invoker);
    }

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

        registerCellClickListenerFor(CommonExternalDataColDefKind.CODE.id(),
                new ICellListener<ExternalData>()
                    {
                        public void handle(ExternalData rowItem)
                        {
                            DataSetUtils.showDataSet(rowItem, viewContext.getModel());
                        }
                    });
        addButton(Dict.BUTTON_DELETE_DATASETS, new AbstractDataSetAction()
            {
                @Override
                protected Dialog createDialog(List<ExternalData> dataSets,
                        IBrowserGridActionInvoker invoker)
                {
                    return new DeletionConfirmationDialog(viewContext, dataSets, invoker);
                }
            });
        addButton(Dict.BUTTON_UPLOAD_DATASETS, new AbstractDataSetAction()
            {
                @Override
                protected Dialog createDialog(List<ExternalData> dataSets,
                        IBrowserGridActionInvoker invoker)
                {
                    return new UploadConfirmationDialog(viewContext, dataSets, invoker);
                }
            });
        addButton(createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_EDIT),
                asShowEntityInvoker(true)));
        addButton(createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_SHOW_DETAILS),
                asShowEntityInvoker(false)));
        allowMultipleSelection();

    }

    private static ICriteriaProvider<PropertyTypesCriteria> createCriteriaProvider(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            boolean displayOnlyDatasetProperties)
    {
        EntityKind entityKindOrNull = displayOnlyDatasetProperties ? EntityKind.DATA_SET : null;
        return new PropertyTypesCriteriaProvider(viewContext, entityKindOrNull);
    }

    private void addButton(String labelKey, SelectionListener<ButtonEvent> action)
    {
        Button button = new Button(viewContext.getMessage(labelKey));
        button.addSelectionListener(action);
        addButton(button);
    }

    private void addButton(Button button)
    {
        pagingToolbar.add(new AdapterToolItem(button));
    }

    @Override
    protected BaseEntityModel<ExternalData> createModel(ExternalData entity)
    {
        BaseEntityModel<ExternalData> model = getColumnsFactory().createModel(entity);
        renderCodeAsLink(model);
        renderShowDetailsLinkAsLink(model);
        return model;
    }

    private void renderCodeAsLink(ModelData model)
    {
        String columnID = CommonExternalDataColDefKind.CODE.id();
        String originalValue = String.valueOf(model.get(columnID));
        model.set(columnID, LinkRenderer.renderAsLink(originalValue));
    }

    private void renderShowDetailsLinkAsLink(ModelData model)
    {
        String showDetailsLinkID = CommonExternalDataColDefKind.SHOW_DETAILS_LINK.id();
        String originalValue = String.valueOf(model.get(showDetailsLinkID));
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
    protected List<IColumnDefinition<ExternalData>> getAvailableFilters()
    {
        return asColumnFilters(new CommonExternalDataColDefKind[]
            { CommonExternalDataColDefKind.CODE, CommonExternalDataColDefKind.LOCATION,
                    CommonExternalDataColDefKind.FILE_FORMAT_TYPE });
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
}
