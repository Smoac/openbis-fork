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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.AuthorizationGroupColDefKind;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroupUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * Grid displaying authorization groups.
 * 
 * @author Izabela Adamczyk
 */
public class AuthorizationGroupGrid extends AbstractSimpleBrowserGrid<AuthorizationGroup>
{
    public static final String BROWSER_ID =
            GenericConstants.ID_PREFIX + "authorization-group-browser";

    public static final String GRID_ID = BROWSER_ID + "_grid";

    public static final String ADD_BUTTON_ID = BROWSER_ID + "_add-button";

    public static final String DELETE_BUTTON_ID = BROWSER_ID + "_delete-button";

    public static final String USERS_BUTTON_ID = BROWSER_ID + "_users-button";

    private final IDelegatedAction postRegistrationCallback;

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final AuthorizationGroupGrid grid = new AuthorizationGroupGrid(viewContext);
        grid.extendBottomToolbar();
        return grid.asDisposableWithoutToolbar();
    }

    private AuthorizationGroupGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID,
                DisplayTypeIDGenerator.AUTHORIZATION_GROUP_BROWSER_GRID);
        postRegistrationCallback = createRefreshGridAction();
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        Button showDetailsButton =
                createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_SHOW_USERS),
                        new ISelectedEntityInvoker<BaseEntityModel<AuthorizationGroup>>()
                            {
                                public void invoke(BaseEntityModel<AuthorizationGroup> selectedItem)
                                {
                                    showEntityViewer(selectedItem.getBaseObject(), false);
                                }
                            });
        showDetailsButton.setId(USERS_BUTTON_ID);
        addButton(showDetailsButton);

        final Button addAuthorizationGroupButton =
                new Button(viewContext.getMessage(Dict.BUTTON_ADD, "Group"),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    AddAuthorizationGroupDialog dialog =
                                            new AddAuthorizationGroupDialog(viewContext,
                                                    createRefreshGridAction());
                                    dialog.show();
                                }
                            });
        addAuthorizationGroupButton.setId(ADD_BUTTON_ID);
        addButton(addAuthorizationGroupButton);

        addButton(createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_EDIT),
                new ISelectedEntityInvoker<BaseEntityModel<AuthorizationGroup>>()
                    {

                        public void invoke(BaseEntityModel<AuthorizationGroup> selectedItem)
                        {
                            final AuthorizationGroup authGroup = selectedItem.getBaseObject();
                            createEditDialog(authGroup).show();
                        }
                    }));

        Button deleteButton =
                createSelectedItemsButton(viewContext.getMessage(Dict.BUTTON_DELETE),
                        new AbstractCreateDialogListener()
                            {
                                @Override
                                protected Dialog createDialog(List<AuthorizationGroup> selected,
                                        IBrowserGridActionInvoker invoker)
                                {
                                    return new GroupListDeletionConfirmationDialog(viewContext,
                                            selected, createDeletionCallback(invoker));
                                }
                            });
        deleteButton.setId(DELETE_BUTTON_ID);
        addButton(deleteButton);

        allowMultipleSelection();

        addEntityOperationsSeparator();
    }

    @Override
    protected IColumnDefinitionKind<AuthorizationGroup>[] getStaticColumnsDefinition()
    {
        return AuthorizationGroupColDefKind.values();
    }

    @Override
    protected ColumnDefsAndConfigs<AuthorizationGroup> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<AuthorizationGroup> schema = super.createColumnsDefinition();
        schema.setGridCellRendererFor(GroupColDefKind.DESCRIPTION.id(),
                createMultilineStringCellRenderer());
        schema.setGridCellRendererFor(GroupColDefKind.CODE.id(), LinkRenderer.createLinkRenderer());
        return schema;
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, AuthorizationGroup> resultSetConfig,
            AbstractAsyncCallback<ResultSet<AuthorizationGroup>> callback)
    {
        viewContext.getService().listAuthorizationGroups(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<AuthorizationGroup> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportAuthorizationGroups(exportCriteria, callback);
    }

    @Override
    protected List<IColumnDefinition<AuthorizationGroup>> getInitialFilters()
    {
        return asColumnFilters(new AuthorizationGroupColDefKind[]
            { AuthorizationGroupColDefKind.CODE });
    }

    @Override
    protected void showEntityViewer(final AuthorizationGroup group, boolean editMode)
    {
        final ITabItemFactory tabFactory = new ITabItemFactory()
            {
                public ITabItem create()
                {
                    IDisposableComponent component =
                            PersonGrid.createForAuthorizationGroup(viewContext, group);
                    String tabTitle =
                            viewContext.getMessage(Dict.AUTHORIZATION_GROUP_USERS, group.getCode());
                    return DefaultTabItem.create(tabTitle, component, viewContext);
                }

                public String getId()
                {
                    return PersonGrid.createBrowserId(group);
                }
            };
        DispatcherHelper.dispatchNaviEvent(tabFactory);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { DatabaseModificationKind.createOrDelete(ObjectKind.AUTHORIZATION_GROUP),
                    DatabaseModificationKind.edit(ObjectKind.AUTHORIZATION_GROUP) };
    }

    private static final class GroupListDeletionConfirmationDialog extends
            AbstractDataListDeletionConfirmationDialog<AuthorizationGroup>
    {

        private final IViewContext<ICommonClientServiceAsync> viewContext;

        private final AbstractAsyncCallback<Void> callback;

        public GroupListDeletionConfirmationDialog(
                IViewContext<ICommonClientServiceAsync> viewContext, List<AuthorizationGroup> data,
                AbstractAsyncCallback<Void> callback)
        {
            super(viewContext, data);
            this.viewContext = viewContext;
            this.callback = callback;
        }

        @Override
        protected void executeConfirmedAction()
        {
            viewContext.getCommonService().deleteAuthorizationGroups(TechId.createList(data),
                    reason.getValue(), callback);
        }

        @Override
        protected String getEntityName()
        {
            return messageProvider.getMessage(Dict.AUTHORIZATION_GROUP);
        }

    }

    private Window createEditDialog(final AuthorizationGroup authGroup)
    {
        final String title =
                viewContext.getMessage(Dict.EDIT_TITLE, "User Group", authGroup.getCode());

        return new AbstractRegistrationDialog(viewContext, title, postRegistrationCallback)
            {
                private final DescriptionField descriptionField;

                {
                    descriptionField = createDescriptionField(viewContext, false);
                    descriptionField.setValue(StringEscapeUtils.unescapeHtml(authGroup
                            .getDescription()));
                    addField(descriptionField);
                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {
                    AuthorizationGroupUpdates updates = new AuthorizationGroupUpdates();
                    updates.setDescription(descriptionField.getValue());
                    updates.setId(TechId.create(authGroup));
                    viewContext.getService()
                            .updateAuthorizationGroup(updates, registrationCallback);
                }
            };
    }

}
