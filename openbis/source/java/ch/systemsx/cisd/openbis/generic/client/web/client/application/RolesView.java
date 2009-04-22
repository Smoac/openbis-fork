/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.RoleModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnFilter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.AddRoleDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;

/**
 * Encapsulates roles listing functionality.
 * 
 * @author Izabela Adamczyk
 */
public class RolesView extends ContentPanel
{

    private static final String LIST_ROLES = "Roles Browser";

    private static final String BUTTON_ADD_ROLE = "Add Role";

    private static final String BUTTON_REMOVE_ROLE = "Remove Role";

    private static final String PERSON = "Person";

    public static final String ID = GenericConstants.ID_PREFIX + "roles-view";

    public static final String ADD_BUTTON_ID = ID + "_add-button";

    public static final String TABLE_ID = ID + "_table";

    private final CommonViewContext viewContext;

    public RolesView(final CommonViewContext viewContext)
    {
        this.viewContext = viewContext;
        setLayout(new FitLayout());
        setHeaderVisible(false);
        setHeading(LIST_ROLES);
        setId(ID);

    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        refresh();
    }

    private void display(final List<RoleAssignment> roles)
    {
        removeAll();

        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        final ColumnConfig userIdColumnConfig = new ColumnConfig();
        userIdColumnConfig.setId(ModelDataPropertyNames.PERSON);
        userIdColumnConfig.setHeader(PERSON);
        userIdColumnConfig.setWidth(ColumnConfigFactory.DEFAULT_COLUMN_WIDTH);
        configs.add(userIdColumnConfig);

        final ColumnConfig groupColumnConfig = new ColumnConfig();
        groupColumnConfig.setId(ModelDataPropertyNames.GROUP);
        groupColumnConfig.setHeader(viewContext.getMessage(Dict.GROUP));
        groupColumnConfig.setWidth(ColumnConfigFactory.DEFAULT_COLUMN_WIDTH);
        configs.add(groupColumnConfig);

        final ColumnConfig instanceColumnConfig = new ColumnConfig();
        instanceColumnConfig.setId(ModelDataPropertyNames.DATABASE_INSTANCE);
        instanceColumnConfig.setHeader(viewContext.getMessage(Dict.DATABASE_INSTANCE));
        instanceColumnConfig.setWidth(ColumnConfigFactory.DEFAULT_COLUMN_WIDTH);
        configs.add(instanceColumnConfig);

        final ColumnConfig roleColumnConfig = new ColumnConfig();
        roleColumnConfig.setId(ModelDataPropertyNames.ROLE);
        roleColumnConfig.setHeader(viewContext.getMessage(Dict.ROLE));
        roleColumnConfig.setWidth(150);
        configs.add(roleColumnConfig);

        final ColumnModel cm = new ColumnModel(configs);

        final ListStore<RoleModel> store = new ListStore<RoleModel>();
        store.add(getRoleModels(roles));

        final ContentPanel cp = new ContentPanel();
        cp.setBodyBorder(false);
        cp.setHeaderVisible(false);
        cp.setButtonAlign(HorizontalAlignment.CENTER);
        final RolesView roleList = this;

        cp.setLayout(new FillLayout());
        cp.setSize("90%", "90%");

        final Grid<RoleModel> grid = new Grid<RoleModel>(store, cm);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid.setBorders(true);
        grid.setId(TABLE_ID);
        GWTUtils.setAutoExpandOnLastVisibleColumn(grid);
        String displayTypeID = DisplayTypeIDGenerator.ROLE_ASSIGNMENT_BROWSER_GRID.createID(null, null);
        viewContext.getDisplaySettingsManager().prepareGrid(displayTypeID, grid);

        cp.add(grid);

        final Button addRoleButton =
                new Button(BUTTON_ADD_ROLE, new SelectionListener<ComponentEvent>()
                    {
                        //
                        // SelectionListener
                        //

                        @Override
                        public final void componentSelected(final ComponentEvent ce)
                        {
                            new AddRoleDialog(viewContext, roleList).show();
                        }
                    });
        addRoleButton.setId(ADD_BUTTON_ID);

        class RemoveRoleDialog extends ConfirmationDialog
        {

            private final RoleModel selectedRole;

            public RemoveRoleDialog(final RoleModel selectedRole)
            {
                super(viewContext.getMessage(Dict.CONFIRM_ROLE_REMOVAL_TITLE), viewContext
                        .getMessage(Dict.CONFIRM_ROLE_REMOVAL_MSG));
                this.selectedRole = selectedRole;
            }

            private void deleteRole()
            {
                final AbstractAsyncCallback<Void> roleListRefreshCallback =
                        new AbstractAsyncCallback<Void>(viewContext)
                            {
                                @Override
                                public void process(Void result)
                                {
                                    roleList.refresh();
                                }
                            };

                if (StringUtils.isBlank((String) selectedRole.get(ModelDataPropertyNames.GROUP)))
                {
                    viewContext.getService().deleteInstanceRole(
                            selectedRole.getRoleSetCode(),
                            (String) selectedRole.get(ModelDataPropertyNames.PERSON),
                            roleListRefreshCallback);
                } else
                {
                    viewContext.getService().deleteGroupRole(
                            selectedRole.getRoleSetCode(),
                            (String) selectedRole.get(ModelDataPropertyNames.GROUP),
                            (String) selectedRole.get(ModelDataPropertyNames.PERSON),
                            roleListRefreshCallback);
                }
            }

            @Override
            protected void onYes()
            {
                deleteRole();
            }
        }

        final Button removeRoleButton =
                new Button(BUTTON_REMOVE_ROLE, new SelectionListener<ComponentEvent>()
                    {
                        @Override
                        public final void componentSelected(final ComponentEvent ce)
                        {
                            final RoleModel rm = grid.getSelectionModel().getSelectedItem();
                            if (rm == null)
                            {
                                return;
                            }
                            new RemoveRoleDialog(rm).show();
                        }

                    });

        final ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem(viewContext.getMessage(Dict.FILTER)
                + GenericConstants.LABEL_SEPARATOR));
        toolBar.add(new AdapterToolItem(new ColumnFilter<RoleModel>(store,
                ModelDataPropertyNames.PERSON, PERSON)));
        toolBar.add(new AdapterToolItem(new ColumnFilter<RoleModel>(store,
                ModelDataPropertyNames.GROUP, viewContext.getMessage(Dict.GROUP))));
        toolBar.add(new AdapterToolItem(new ColumnFilter<RoleModel>(store,
                ModelDataPropertyNames.DATABASE_INSTANCE, viewContext
                        .getMessage(Dict.DATABASE_INSTANCE))));
        toolBar.add(new AdapterToolItem(new ColumnFilter<RoleModel>(store,
                ModelDataPropertyNames.ROLE, viewContext.getMessage(Dict.ROLE))));
        toolBar.add(new SeparatorToolItem());
        toolBar.add(new AdapterToolItem(addRoleButton));
        toolBar.add(new SeparatorToolItem());
        toolBar.add(new AdapterToolItem(removeRoleButton));
        cp.setBottomComponent(toolBar);
        add(cp);
        layout();
    }

    List<RoleModel> getRoleModels(final List<RoleAssignment> roles)
    {
        final List<RoleModel> roleModel = new ArrayList<RoleModel>();
        for (final RoleAssignment role : roles)
        {
            roleModel.add(new RoleModel(role));
        }
        return roleModel;
    }

    public void refresh()
    {
        viewContext.getService().listRoles(new ListRolesCallback(viewContext));
    }

    //
    // Helper classes
    //

    public final class ListRolesCallback extends AbstractAsyncCallback<List<RoleAssignment>>
    {

        private ListRolesCallback(final CommonViewContext viewContext)
        {
            super(viewContext);
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        public final void process(final List<RoleAssignment> roles)
        {
            display(roles);
        }
    }
}
