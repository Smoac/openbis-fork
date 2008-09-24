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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;

/**
 * @author Izabela Adamczyk
 */
public class RolesView extends LayoutContainer
{

    private final GenericViewContext viewContext;

    public RolesView(GenericViewContext viewContext)
    {
        this.viewContext = viewContext;
        setLayout(new FlowLayout(5));

    }

    private void display(final List<RoleAssignment> roles)
    {
        removeAll();

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig userIdColumnConfig = new ColumnConfig();
        userIdColumnConfig.setId("person");
        userIdColumnConfig.setHeader("Person");
        userIdColumnConfig.setWidth(80);
        configs.add(userIdColumnConfig);

        ColumnConfig groupColumnConfig = new ColumnConfig();
        groupColumnConfig.setId("group");
        groupColumnConfig.setHeader("Group");
        groupColumnConfig.setWidth(80);
        configs.add(groupColumnConfig);

        ColumnConfig instanceColumnConfig = new ColumnConfig();
        instanceColumnConfig.setId("instance");
        instanceColumnConfig.setHeader("Database Instance");
        instanceColumnConfig.setWidth(80);
        configs.add(instanceColumnConfig);

        ColumnConfig roleColumnConfig = new ColumnConfig();
        roleColumnConfig.setId("role");
        roleColumnConfig.setHeader("Role");
        roleColumnConfig.setWidth(120);
        configs.add(roleColumnConfig);

        ColumnModel cm = new ColumnModel(configs);

        final ListStore<RoleModel> store = new ListStore<RoleModel>();
        store.add(getRoleModels(roles));

        ContentPanel cp = new ContentPanel();
        cp.setBodyBorder(false);
        cp.setHeading("Role list");
        cp.setButtonAlign(HorizontalAlignment.CENTER);
        cp.setIconStyle("icon-table");
        final RolesView roleList = this;

        cp.setLayout(new FitLayout());
        cp.setSize(700, 300);

        final Grid<RoleModel> grid = new Grid<RoleModel>(store, cm);
        grid.setBorders(true);

        cp.add(grid);

        Button addRoleButton = new Button("Add role", new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    new RolesDialog(viewContext, roleList).show();
                }
            });

        Button removeRoleButton = new Button("Remove role", new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    final RoleModel rm = grid.getSelectionModel().getSelectedItem();
                    viewContext.getService().deleteRole((String) rm.get("role"),
                            (String) rm.get("group"), (String) rm.get("person"),
                            new AbstractAsyncCallback<Void>(viewContext)
                                {
                                    public void onSuccess(Void result)
                                    {
                                        roleList.refresh();
                                    }
                                });
                }
            });

        ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem("Filter:"));
        toolBar.add(new AdapterToolItem(new ColumnFilter<RoleModel>(store, "person", "person")));
        toolBar.add(new AdapterToolItem(new ColumnFilter<RoleModel>(store, "group", "group")));
        toolBar
                .add(new AdapterToolItem(new ColumnFilter<RoleModel>(store, "instance", "instance")));
        toolBar.add(new AdapterToolItem(new ColumnFilter<RoleModel>(store, "role", "role")));
        toolBar.add(new SeparatorToolItem());
        toolBar.add(new AdapterToolItem(addRoleButton));
        toolBar.add(new SeparatorToolItem());
        toolBar.add(new AdapterToolItem(removeRoleButton));
        cp.setBottomComponent(toolBar);
        add(cp);
        layout();

    }

    List<RoleModel> getRoleModels(List<RoleAssignment> roles)
    {
        List<RoleModel> roleModel = new ArrayList<RoleModel>();
        for (RoleAssignment role : roles)
        {
            roleModel.add(new RoleModel(role));
        }
        return roleModel;
    }

    public void refresh()
    {
        removeAll();
        add(new Text("data loading..."));
        viewContext.getService().listRoles(
                new AbstractAsyncCallback<List<RoleAssignment>>(viewContext)
                    {
                        public void onSuccess(List<RoleAssignment> roles)
                        {
                            display(roles);
                        }

                    });
    }

}
