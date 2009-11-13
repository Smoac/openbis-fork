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
import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.AddRoleAssignmentDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.RoleAssignmentColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * Grid displaying persons.
 * 
 * @author Piotr Buczek
 */
public class RoleAssignmentGrid extends AbstractSimpleBrowserGrid<RoleAssignment>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "role-browser";

    public static final String GRID_ID = BROWSER_ID + "_grid";

    public static final String ASSIGN_BUTTON_ID = BROWSER_ID + "_assign-button";

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final RoleAssignmentGrid grid = new RoleAssignmentGrid(viewContext);
        grid.extendBottomToolbar();
        return grid.asDisposableWithoutToolbar();
    }

    private RoleAssignmentGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID, DisplayTypeIDGenerator.PROJECT_BROWSER_GRID);
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        final Button addGroupButton =
                new Button(viewContext.getMessage(Dict.BUTTON_ASSIGN_ROLE),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    AddRoleAssignmentDialog dialog =
                                            new AddRoleAssignmentDialog(viewContext,
                                                    createRefreshGridAction());
                                    dialog.show();
                                }
                            });
        addGroupButton.setId(ASSIGN_BUTTON_ID);
        addButton(addGroupButton);

        // refactor
        final Button removeRoleButton =
                createSelectedItemButton(viewContext
                        .getMessage(Dict.BUTTON_RELEASE_ROLE_ASSIGNMENT),
                        new ISelectedEntityInvoker<BaseEntityModel<RoleAssignment>>()
                            {

                                public void invoke(BaseEntityModel<RoleAssignment> selectedItem)
                                {
                                    RoleAssignment assignment = selectedItem.getBaseObject();
                                    new RemoveRoleDialog(assignment).show();
                                }
                            });
        addButton(removeRoleButton);

        addEntityOperationsSeparator();
    }

    // TODO 2009-02-20, Piotr Buczek: no event is created in DB
    class RemoveRoleDialog extends ConfirmationDialog
    {

        private final RoleAssignment selectedRoleAssignment;

        public RemoveRoleDialog(final RoleAssignment selectedRoleAssignment)
        {
            super(viewContext.getMessage(Dict.CONFIRM_ROLE_REMOVAL_TITLE), viewContext
                    .getMessage(Dict.CONFIRM_ROLE_REMOVAL_MSG));
            this.selectedRoleAssignment = selectedRoleAssignment;
        }

        private void deleteRole()
        {
            final AbstractAsyncCallback<Void> roleListRefreshCallback =
                    new AbstractAsyncCallback<Void>(viewContext)
                        {
                            @Override
                            public void process(Void result)
                            {
                                asActionInvoker().refresh();
                            }
                        };

            Person person = selectedRoleAssignment.getPerson();
            Grantee grantee =
                    (person != null && StringUtils.isBlank(person.getUserId()) == false) ? Grantee
                            .createPerson(person.getUserId()) : Grantee
                            .createAuthorizationGroup(selectedRoleAssignment
                                    .getAuthorizationGroup().getCode());
            if (selectedRoleAssignment.getGroup() == null)
            {
                viewContext.getService().deleteInstanceRole(
                        selectedRoleAssignment.getRoleSetCode(), grantee, roleListRefreshCallback);
            } else
            {
                viewContext.getService().deleteGroupRole(selectedRoleAssignment.getRoleSetCode(),
                        selectedRoleAssignment.getGroup().getCode(), grantee,
                        roleListRefreshCallback);
            }
        }

        @Override
        protected void onYes()
        {
            deleteRole();
        }
    }

    @Override
    protected IColumnDefinitionKind<RoleAssignment>[] getStaticColumnsDefinition()
    {
        return RoleAssignmentColDefKind.values();
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, RoleAssignment> resultSetConfig,
            AbstractAsyncCallback<ResultSet<RoleAssignment>> callback)
    {
        viewContext.getService().listRoleAssignments(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<RoleAssignment> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportRoleAssignments(exportCriteria, callback);
    }

    @Override
    protected List<IColumnDefinition<RoleAssignment>> getInitialFilters()
    {
        return asColumnFilters(RoleAssignmentColDefKind.values());
    }

    @Override
    protected void showEntityViewer(final RoleAssignment roleAssignment, boolean editMode)
    {
        assert false : "not implemented";
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { DatabaseModificationKind.createOrDelete(ObjectKind.ROLE_ASSIGNMENT),
                    DatabaseModificationKind.edit(ObjectKind.ROLE_ASSIGNMENT) };
    }
}
