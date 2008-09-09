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

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.table.DateTimeCellRenderer;
import com.extjs.gxt.ui.client.widget.table.Table;
import com.extjs.gxt.ui.client.widget.table.TableColumn;
import com.extjs.gxt.ui.client.widget.table.TableColumnModel;
import com.extjs.gxt.ui.client.widget.table.TableItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.TextCellRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;

/**
 * @author Franz-Josef Elmer
 */
public class GroupsView extends VerticalPanel
{
    private LayoutContainer groupsPanel;

    private final GenericViewContext viewContext;

    public GroupsView(GenericViewContext viewContext)
    {
        this.viewContext = viewContext;
        add(new Text("Groups:"));
        groupsPanel = new LayoutContainer();
        groupsPanel.add(new Text("data loading..."));
        add(groupsPanel);

        viewContext.getService().listGroups(null,
                new AbstractAsyncCallback<List<Group>>(viewContext)
                    {

                        public void onSuccess(List<Group> groups)
                        {
                            fillGroupsPanel(groups);
                        }

                    });
    }

    private void fillGroupsPanel(List<Group> groups)
    {
        List<TableColumn> columns = new ArrayList<TableColumn>();
        columns.add(new TableColumn("code", "Code", 100));
        TableColumn codeColumn = new TableColumn("description", "Description", 200);
        codeColumn.setRenderer(new TextCellRenderer("-"));
        columns.add(codeColumn);
        TableColumn dateColumn = new TableColumn("registrationDate", "Registration Date", 250);
        dateColumn.setRenderer(new DateTimeCellRenderer<Component>("yyyy-MM-dd HH:mm:ss zzz"));
        columns.add(dateColumn);
        columns.add(new TableColumn("registrator", "Registrator", 100));

        Table table = new Table(new TableColumnModel(columns));
        table.setHorizontalScroll(true);

        for (Group group : groups)
        {
            Object[] row = new Object[4];
            row[0] = group.getCode();
            row[1] = group.getDescription();
            row[2] = group.getRegistrationDate();
            Person registrator = group.getRegistrator();
            if (registrator != null)
            {
                row[3] = registrator.getFirstName() + " " + group.getRegistrator().getLastName();
            }
            table.add(new TableItem(row));
        }

        ContentPanel contentPanel = new ContentPanel();
        contentPanel.setHeaderVisible(false);
        contentPanel.add(table);
        contentPanel.setTopComponent(createToolBar());
        groupsPanel.removeAll();
        groupsPanel.add(contentPanel);
    }

    private ToolBar createToolBar()
    {
        ToolBar toolBar = new ToolBar();
        TextToolItem addGroup = new TextToolItem("Add", "icon-add");
        addGroup.addSelectionListener(new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    new GroupDialog(viewContext).show();
                }
            });
        toolBar.add(addGroup);
        return toolBar;
    }
}
