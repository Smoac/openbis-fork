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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc;

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
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.DataModelPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.RoleModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnFilter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;

/**
 * {@link LayoutContainer} with roles functionality.
 * 
 * @author Izabela Adamczyk
 */
public class RolesView extends LayoutContainer
{

    private static final String PREFIX = "roles-view_";

    static final String ADD_BUTTON_ID = GenericConstants.ID_PREFIX + PREFIX + "add-button";

    static final String TABLE_ID = GenericConstants.ID_PREFIX + PREFIX + "table";

    private final GenericViewContext viewContext;

    public RolesView(final GenericViewContext viewContext)
    {
        this.viewContext = viewContext;
        setLayout(new FitLayout());

    }

    private void display(final List<RoleAssignment> roles)
    {
        removeAll();

        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        final ColumnConfig userIdColumnConfig = new ColumnConfig();
        userIdColumnConfig.setId(DataModelPropertyNames.PERSON);
        userIdColumnConfig.setHeader("Person");
        userIdColumnConfig.setWidth(COL_PERSON);
        configs.add(userIdColumnConfig);

        final ColumnConfig groupColumnConfig = new ColumnConfig();
        groupColumnConfig.setId(DataModelPropertyNames.GROUP);
        groupColumnConfig.setHeader("Group");
        groupColumnConfig.setWidth(COL_GROUP);
        configs.add(groupColumnConfig);

        final ColumnConfig instanceColumnConfig = new ColumnConfig();
        instanceColumnConfig.setId(DataModelPropertyNames.INSTANCE);
        instanceColumnConfig.setHeader("Database Instance");
        instanceColumnConfig.setWidth(COL_DB_INSTANCE);
        configs.add(instanceColumnConfig);

        final ColumnConfig roleColumnConfig = new ColumnConfig();
        roleColumnConfig.setId(DataModelPropertyNames.ROLE);
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
                            if (StringUtils.isBlank((String) rm.get(DataModelPropertyNames.GROUP)))
                            {
                                viewContext.getService().deleteInstanceRole(
                                        (String) rm.get(DataModelPropertyNames.ROLE),
                                        (String) rm.get(DataModelPropertyNames.PERSON), roleListRefreshCallback);
                            } else
                            {
                                viewContext.getService().deleteGroupRole(
                                        (String) rm.get(DataModelPropertyNames.ROLE),
                                        (String) rm.get(DataModelPropertyNames.GROUP),
                                        (String) rm.get(DataModelPropertyNames.PERSON), roleListRefreshCallback);
                            }
                        }
                    });

        final ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem("Filter:"));
        toolBar.add(new AdapterToolItem(new ColumnFilter<RoleModel>(store, DataModelPropertyNames.PERSON,
                "person")));
        toolBar.add(new AdapterToolItem(
                new ColumnFilter<RoleModel>(store, DataModelPropertyNames.GROUP, "group")));
        toolBar.add(new AdapterToolItem(new ColumnFilter<RoleModel>(store, DataModelPropertyNames.INSTANCE,
                "instance")));
        toolBar
                .add(new AdapterToolItem(new ColumnFilter<RoleModel>(store, DataModelPropertyNames.ROLE, "role")));
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
        removeAll();
        add(new Text("data loading..."));
        viewContext.getService().listRoles(new ListRolesCallback(viewContext));
    }

    //
    // Helper classes
    //

    final class ListRolesCallback extends AbstractAsyncCallback<List<RoleAssignment>>
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
