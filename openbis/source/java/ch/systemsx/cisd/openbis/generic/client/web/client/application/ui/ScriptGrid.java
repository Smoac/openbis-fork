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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.ScriptColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ScriptField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;

/**
 * Grid displaying scripts.
 * 
 * @author Izabela Adamczyk
 */
public class ScriptGrid extends AbstractSimpleBrowserGrid<Script>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "script-browser";

    public static final String GRID_ID = BROWSER_ID + "_grid";

    public static final String ADD_BUTTON_ID = BROWSER_ID + "_add-button";

    public static final String DELETE_BUTTON_ID = BROWSER_ID + "_delete-button";

    public static final String EDIT_BUTTON_ID = BROWSER_ID + "-edit-button";

    private final IDelegatedAction postRegistrationCallback;

    public static DisposableEntityChooser<Script> create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final ScriptGrid grid = new ScriptGrid(viewContext);
        grid.extendBottomToolbar();
        return grid.asDisposableWithoutToolbar();
    }

    private ScriptGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID, DisplayTypeIDGenerator.SCRIPTS_BROWSER_GRID);
        postRegistrationCallback = createRefreshGridAction();
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        final Button addScriptButton =
                new Button(viewContext.getMessage(Dict.BUTTON_ADD,
                        viewContext.getMessage(Dict.SCRIPT)), new SelectionListener<ButtonEvent>()
                    {
                        @Override
                        public void componentSelected(ButtonEvent ce)
                        {
                            AddScriptDialog dialog =
                                    new AddScriptDialog(viewContext, createRefreshGridAction());
                            dialog.show();
                        }
                    });
        addScriptButton.setId(ADD_BUTTON_ID);
        addButton(addScriptButton);

        Button editButton =
                createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_EDIT),
                        new ISelectedEntityInvoker<BaseEntityModel<Script>>()
                            {
                                public void invoke(BaseEntityModel<Script> selectedItem,
                                        boolean keyPressed)
                                {
                                    Script script = selectedItem.getBaseObject();
                                    createEditDialog(script).show();
                                }
                            });
        editButton.setId(EDIT_BUTTON_ID);
        addButton(editButton);

        Button deleteButton =
                createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_DELETE),
                        new AbstractCreateDialogListener()
                            {
                                @Override
                                protected Dialog createDialog(List<Script> scripts,
                                        IBrowserGridActionInvoker invoker)
                                {
                                    return new ScriptListDeletionConfirmationDialog(viewContext,
                                            scripts, createDeletionCallback(invoker));
                                }
                            });
        addButton(deleteButton);
        allowMultipleSelection(); // we allow deletion of multiple projects

        addEntityOperationsSeparator();
    }

    private Window createEditDialog(final Script script)
    {
        final String name = script.getName();
        final String description = script.getDescription();
        final String title =
                viewContext.getMessage(Dict.EDIT_TITLE, viewContext.getMessage(Dict.SCRIPT), name);

        return new AbstractRegistrationDialog(viewContext, title, postRegistrationCallback)
            {
                private final TextField<String> nameField;

                private final DescriptionField descriptionField;

                private final MultilineVarcharField scriptField;

                {
                    this.nameField = new VarcharField(viewContext.getMessage(Dict.NAME), true);
                    this.nameField.setValue(StringEscapeUtils.unescapeHtml(script.getName()));
                    addField(nameField);

                    descriptionField = createDescriptionField(viewContext);
                    descriptionField.setValue(StringEscapeUtils.unescapeHtml(description));
                    addField(descriptionField);

                    this.scriptField = new ScriptField(viewContext);
                    // new MultilineVarcharField(viewContext.getMessage(Dict.SCRIPT), true, 20);
                    this.scriptField.setValue(StringEscapeUtils.unescapeHtml(script.getScript()));
                    addField(scriptField);
                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {
                    script.setDescription(descriptionField.getValue());
                    script.setScript(scriptField.getValue());
                    script.setName(nameField.getValue());

                    viewContext.getService().updateScript(script, registrationCallback);
                }
            };
    }

    @Override
    protected IColumnDefinitionKind<Script>[] getStaticColumnsDefinition()
    {
        return ScriptColDefKind.values();
    }

    @Override
    protected ColumnDefsAndConfigs<Script> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<Script> schema = super.createColumnsDefinition();
        schema.setGridCellRendererFor(ScriptColDefKind.DESCRIPTION.id(),
                createMultilineStringCellRenderer());
        schema.setGridCellRendererFor(ScriptColDefKind.SCRIPT.id(),
                createMultilineStringCellRenderer());
        return schema;
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, Script> resultSetConfig,
            AbstractAsyncCallback<ResultSet<Script>> callback)
    {
        viewContext.getService().listScripts(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<Script> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportScripts(exportCriteria, callback);
    }

    @Override
    protected List<IColumnDefinition<Script>> getInitialFilters()
    {
        return asColumnFilters(new ScriptColDefKind[]
            { ScriptColDefKind.NAME });
    }

    @Override
    protected void showEntityViewer(final Script script, boolean editMode, boolean inBackground)
    {
        assert false : "not implemented";
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { DatabaseModificationKind.createOrDelete(ObjectKind.SCRIPT),
                    DatabaseModificationKind.edit(ObjectKind.SCRIPT) };
    }

    private static final class ScriptListDeletionConfirmationDialog extends
            AbstractDataListDeletionConfirmationDialog<Script>
    {

        private final IViewContext<ICommonClientServiceAsync> viewContext;

        private final AbstractAsyncCallback<Void> callback;

        public ScriptListDeletionConfirmationDialog(
                IViewContext<ICommonClientServiceAsync> viewContext, List<Script> data,
                AbstractAsyncCallback<Void> callback)
        {
            super(viewContext, data);
            this.viewContext = viewContext;
            this.callback = callback;
        }

        @Override
        protected void executeConfirmedAction()
        {
            viewContext.getCommonService().deleteScripts(TechId.createList(data), callback);
        }

        @Override
        protected String getEntityName()
        {
            return messageProvider.getMessage(Dict.SCRIPT);
        }

    }
}
