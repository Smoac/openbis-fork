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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.script;

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.ScriptColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListScriptsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
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

    private final EntityKind entityKindOrNull;

    public static DisposableEntityChooser<Script> create(
            final IViewContext<ICommonClientServiceAsync> viewContext, EntityKind entityKindOrNull)
    {
        final ScriptGrid grid = new ScriptGrid(viewContext, entityKindOrNull);
        grid.extendBottomToolbar();
        return grid.asDisposableWithoutToolbar();
    }

    private ScriptGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            EntityKind entityKindOrNull)
    {
        super(viewContext, BROWSER_ID, GRID_ID, DisplayTypeIDGenerator.SCRIPTS_BROWSER_GRID);
        this.entityKindOrNull = entityKindOrNull;
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
                            DispatcherHelper.dispatchNaviEvent(new ComponentProvider(viewContext)
                                    .getScriptRegistration(entityKindOrNull));
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
                                    openEditor(selectedItem, keyPressed);
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
        allowMultipleSelection();

        addEntityOperationsSeparator();
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
        ListScriptsCriteria criteria = new ListScriptsCriteria();
        criteria.copyPagingConfig(resultSetConfig);
        criteria.setEntityKind(entityKindOrNull);
        viewContext.getService().listScripts(criteria, callback);
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

    private void openEditor(BaseEntityModel<Script> selectedItem, boolean keyPressed)
    {
        final Script script = selectedItem.getBaseObject();
        final TechId scriptId = TechId.create(script);
        AbstractTabItemFactory tabFactory = new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    Component component = ScriptEditForm.create(viewContext, scriptId);
                    return DefaultTabItem.createUnaware(getTabTitle(), component, true);
                }

                @Override
                public String getId()
                {
                    return ScriptEditForm.createId(scriptId);
                }

                @Override
                public String getTabTitle()
                {
                    return viewContext.getMessage(Dict.EDIT_TITLE,
                            viewContext.getMessage(Dict.SCRIPT), "");
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.SCRIPT, HelpPageAction.EDIT);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
        tabFactory.setInBackground(keyPressed);
        DispatcherHelper.dispatchNaviEvent(tabFactory);
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
