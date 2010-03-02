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
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.AddGroupDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.GroupColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * Grid displaying groups.
 * 
 * @author Piotr Buczek
 */
public class GroupGrid extends AbstractSimpleBrowserGrid<Group>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "group-browser";

    public static final String GRID_ID = BROWSER_ID + "_grid";

    public static final String ADD_BUTTON_ID = BROWSER_ID + "_add-button";

    public static final String DELETE_BUTTON_ID = BROWSER_ID + "_delete-button";

    public static final String EDIT_BUTTON_ID = BROWSER_ID + "-edit-button";

    private final IDelegatedAction postRegistrationCallback;

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final GroupGrid grid = new GroupGrid(viewContext);
        grid.extendBottomToolbar();
        return grid.asDisposableWithoutToolbar();
    }

    private GroupGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID, DisplayTypeIDGenerator.GROUPS_BROWSER_GRID);
        postRegistrationCallback = createRefreshGridAction();
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        final Button addGroupButton =
                new Button(viewContext.getMessage(Dict.BUTTON_ADD, viewContext
                        .getMessage(Dict.GROUP)), new SelectionListener<ButtonEvent>()
                    {
                        @Override
                        public void componentSelected(ButtonEvent ce)
                        {
                            AddGroupDialog dialog =
                                    new AddGroupDialog(viewContext, createRefreshGridAction());
                            dialog.show();
                        }
                    });
        addGroupButton.setId(ADD_BUTTON_ID);
        addButton(addGroupButton);

        Button editButton =
                createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_EDIT),
                        new ISelectedEntityInvoker<BaseEntityModel<Group>>()
                            {
                                public void invoke(BaseEntityModel<Group> selectedItem)
                                {
                                    Group group = selectedItem.getBaseObject();
                                    createEditDialog(group).show();
                                }
                            });
        editButton.setId(EDIT_BUTTON_ID);
        addButton(editButton);

        Button deleteButton =
                createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_DELETE),
                        new AbstractCreateDialogListener()
                            {
                                @Override
                                protected Dialog createDialog(List<Group> groups,
                                        IBrowserGridActionInvoker invoker)
                                {
                                    return new GroupListDeletionConfirmationDialog(viewContext,
                                            groups, createDeletionCallback(invoker));
                                }
                            });
        addButton(deleteButton);
        allowMultipleSelection(); // we allow deletion of multiple projects

        addEntityOperationsSeparator();
    }

    private Window createEditDialog(final Group group)
    {
        final String code = group.getCode();
        final String description = group.getDescription();
        final String title =
                viewContext.getMessage(Dict.EDIT_TITLE, viewContext.getMessage(Dict.GROUP), code);

        return new AbstractRegistrationDialog(viewContext, title, postRegistrationCallback)
            {
                private final DescriptionField descriptionField;

                {
                    boolean mandatory = false;

                    descriptionField = createDescriptionField(viewContext, mandatory);
                    descriptionField.setValue(StringEscapeUtils.unescapeHtml(description));
                    addField(descriptionField);
                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {
                    group.setDescription(descriptionField.getValue());

                    viewContext.getService().updateGroup(group, registrationCallback);
                }
            };
    }

    @Override
    protected IColumnDefinitionKind<Group>[] getStaticColumnsDefinition()
    {
        return GroupColDefKind.values();
    }

    @Override
    protected ColumnDefsAndConfigs<Group> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<Group> schema = super.createColumnsDefinition();
        schema.setGridCellRendererFor(GroupColDefKind.DESCRIPTION.id(),
                createMultilineStringCellRenderer());
        return schema;
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, Group> resultSetConfig,
            AbstractAsyncCallback<ResultSet<Group>> callback)
    {
        viewContext.getService().listGroups(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<Group> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportGroups(exportCriteria, callback);
    }

    @Override
    protected List<IColumnDefinition<Group>> getInitialFilters()
    {
        return asColumnFilters(new GroupColDefKind[]
            { GroupColDefKind.CODE });
    }

    @Override
    protected void showEntityViewer(final Group group, boolean editMode)
    {
        assert false : "not implemented";
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { DatabaseModificationKind.createOrDelete(ObjectKind.SPACE),
                    DatabaseModificationKind.edit(ObjectKind.SPACE) };
    }

    private static final class GroupListDeletionConfirmationDialog extends
            AbstractDataListDeletionConfirmationDialog<Group>
    {

        private final IViewContext<ICommonClientServiceAsync> viewContext;

        private final AbstractAsyncCallback<Void> callback;

        public GroupListDeletionConfirmationDialog(
                IViewContext<ICommonClientServiceAsync> viewContext, List<Group> data,
                AbstractAsyncCallback<Void> callback)
        {
            super(viewContext, data);
            this.viewContext = viewContext;
            this.callback = callback;
        }

        @Override
        protected void executeConfirmedAction()
        {
            viewContext.getCommonService().deleteGroups(TechId.createList(data), reason.getValue(),
                    callback);
        }

        @Override
        protected String getEntityName()
        {
            return messageProvider.getMessage(Dict.GROUP);
        }

    }
}
