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

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.util.ClientConstants.COL_DB_INSTANCE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.application.util.ClientConstants.COL_GROUP;
import static ch.systemsx.cisd.openbis.generic.client.web.client.application.util.ClientConstants.COL_PERSON;
import static ch.systemsx.cisd.openbis.generic.client.web.client.application.util.ClientConstants.COL_ROLE;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnFilter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.AddRoleDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;

/**
 * Encapsulates roles listing functionality.
 * 
 * @author Izabela Adamczyk
 */
public class RolesView extends ContentPanel
{

    public static final String ID = GenericConstants.ID_PREFIX + "roles-view";

    public static final String ADD_BUTTON_ID = ID + "_add-button";

    public static final String TABLE_ID = ID + "_table";

    private final GenericViewContext viewContext;

    public RolesView(final GenericViewContext viewContext)
    {
        this.viewContext = viewContext;
        setLayout(new FitLayout());
        setHeaderVisible(false);
        setHeading("List roles");
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
        userIdColumnConfig.setHeader("Person");
        userIdColumnConfig.setWidth(COL_PERSON);
        configs.add(userIdColumnConfig);

        final ColumnConfig groupColumnConfig = new ColumnConfig();
        groupColumnConfig.setId(ModelDataPropertyNames.GROUP);
        groupColumnConfig.setHeader("Group");
        groupColumnConfig.setWidth(COL_GROUP);
        configs.add(groupColumnConfig);

        final ColumnConfig instanceColumnConfig = new ColumnConfig();
        instanceColumnConfig.setId(ModelDataPropertyNames.INSTANCE);
        instanceColumnConfig.setHeader("Database Instance");
        instanceColumnConfig.setWidth(COL_DB_INSTANCE);
        configs.add(instanceColumnConfig);

        final ColumnConfig roleColumnConfig = new ColumnConfig();
        roleColumnConfig.setId(ModelDataPropertyNames.ROLE);
        roleColumnConfig.setHeader("Role");
        roleColumnConfig.setWidth(COL_ROLE);
        configs.add(roleColumnConfig);

        final ColumnModel cm = new ColumnModel(configs);

        final ListStore<RoleModel> store = new ListStore<RoleModel>();
        store.add(getRoleModels(roles));

        final ContentPanel cp = new ContentPanel();
        cp.setBodyBorder(false);
        cp.setHeading("Role list");
        cp.setButtonAlign(HorizontalAlignment.CENTER);
        final RolesView roleList = this;

        cp.setLayout(new FillLayout());
        cp.setSize("90%", "90%");

        final Grid<RoleModel> grid = new Grid<RoleModel>(store, cm);
        grid.setBorders(true);
        grid.setId(TABLE_ID);

        cp.add(grid);

        final Button addRoleButton = new Button("Add role", new SelectionListener<ComponentEvent>()
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

        final Button removeRoleButton =
                new Button("Remove role", new SelectionListener<ComponentEvent>()
                    {
                        //
                        // ComponentEvent
                        //

                        @Override
                        public final void componentSelected(final ComponentEvent ce)
                        {
                            final RoleModel rm = grid.getSelectionModel().getSelectedItem();
                            if (rm == null)
                            {
                                return;
                            }
                            final AbstractAsyncCallback<Void> roleListRefreshCallback =
                                    new AbstractAsyncCallback<Void>(viewContext)
                                        {
                                            @Override
                                            public void process(Void result)
                                            {
                                                roleList.refresh();
                                            }
                                        };
                            if (StringUtils.isBlank((String) rm.get(ModelDataPropertyNames.GROUP)))
                            {
                                viewContext.getService().deleteInstanceRole(
                                        (String) rm.get(ModelDataPropertyNames.ROLE),
                                        (String) rm.get(ModelDataPropertyNames.PERSON),
                                        roleListRefreshCallback);
                            } else
                            {
                                viewContext.getService().deleteGroupRole(
                                        (String) rm.get(ModelDataPropertyNames.ROLE),
                                        (String) rm.get(ModelDataPropertyNames.GROUP),
                                        (String) rm.get(ModelDataPropertyNames.PERSON),
                                        roleListRefreshCallback);
                            }
                        }
                    });

        final ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem("Filter:"));
        toolBar.add(new AdapterToolItem(new ColumnFilter<RoleModel>(store,
                ModelDataPropertyNames.PERSON, "person")));
        toolBar.add(new AdapterToolItem(new ColumnFilter<RoleModel>(store,
                ModelDataPropertyNames.GROUP, "group")));
        toolBar.add(new AdapterToolItem(new ColumnFilter<RoleModel>(store,
                ModelDataPropertyNames.INSTANCE, "instance")));
        toolBar.add(new AdapterToolItem(new ColumnFilter<RoleModel>(store,
                ModelDataPropertyNames.ROLE, "role")));
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

        private ListRolesCallback(final GenericViewContext viewContext)
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
