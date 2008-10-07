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

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.util.ClientConstants.COL_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.application.util.ClientConstants.COL_GROUP;
import static ch.systemsx.cisd.openbis.generic.client.web.client.application.util.ClientConstants.COL_PERSON;
import static ch.systemsx.cisd.openbis.generic.client.web.client.application.util.ClientConstants.FIT_SIZE;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.core.XTemplate;
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
import com.extjs.gxt.ui.client.widget.grid.RowExpander;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnFilter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;

/**
 * {@link LayoutContainer} with groups functionality.
 * 
 * @author Izabela Adamczyk
 */
public class GroupsView extends LayoutContainer
{
    private static final String PREFIX = "groups-view_";

    static final String TABLE_ID = GenericConstants.ID_PREFIX + PREFIX + "table";

    static final String ADD_BUTTON_ID = GenericConstants.ID_PREFIX + PREFIX + "add-button";

    final class ListGroupsCallback extends AbstractAsyncCallback<List<Group>>
    {
        private ListGroupsCallback(GenericViewContext viewContext)
        {
            super(viewContext);
        }

        @Override
        public void process(List<Group> groups)
        {
            display(groups);
        }
    }

    private final GenericViewContext viewContext;

    public GroupsView(GenericViewContext viewContext)
    {
        this.viewContext = viewContext;
        setLayout(new FitLayout());
    }

    private void display(final List<Group> groups)
    {
        removeAll();

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        XTemplate tpl = XTemplate.create("<p>" + "<b>Description:</b> {description}</p>");
        RowExpander expander = new RowExpander();
        expander.setTemplate(tpl);
        configs.add(expander);

        ColumnConfig codeColumnConfig = new ColumnConfig();
        codeColumnConfig.setId(GroupModel.CODE);
        codeColumnConfig.setHeader("Code");
        codeColumnConfig.setWidth(COL_GROUP);
        configs.add(codeColumnConfig);

        ColumnConfig leaderColumnConfig = new ColumnConfig();
        leaderColumnConfig.setId(GroupModel.LEADER);
        leaderColumnConfig.setHeader("Leader");
        leaderColumnConfig.setWidth(COL_PERSON);
        leaderColumnConfig.setRenderer(new PersonRenderer());
        configs.add(leaderColumnConfig);

        ColumnConfig registratorColumnConfig = new ColumnConfig();
        registratorColumnConfig.setId(GroupModel.REGISTRATOR);
        registratorColumnConfig.setHeader("Registrator");
        registratorColumnConfig.setWidth(COL_PERSON);
        registratorColumnConfig.setRenderer(new PersonRenderer());
        configs.add(registratorColumnConfig);

        ColumnConfig registrationDateColumnConfig = new ColumnConfig();
        registrationDateColumnConfig.setId(GroupModel.REGISTRATION_DATE);
        registrationDateColumnConfig.setHeader("Registration Date");
        registrationDateColumnConfig.setWidth(COL_DATE);
        registrationDateColumnConfig.setAlignment(HorizontalAlignment.RIGHT);
        registrationDateColumnConfig.setDateTimeFormat(DateTimeFormat.getShortDateFormat());
        configs.add(registrationDateColumnConfig);

        final GroupsView groupList = this;

        ColumnModel cm = new ColumnModel(configs);

        ListStore<GroupModel> store = new ListStore<GroupModel>();
        store.add(getGroupModels(groups));

        ContentPanel cp = new ContentPanel();
        cp.setBodyBorder(false);
        cp.setHeading("Group list");
        cp.setButtonAlign(HorizontalAlignment.CENTER);

        cp.setLayout(new FitLayout());
        cp.setSize(FIT_SIZE, FIT_SIZE);

        Grid<GroupModel> grid = new Grid<GroupModel>(store, cm);
        grid.addPlugin(expander);
        grid.setBorders(true);
        grid.setId(TABLE_ID);

        cp.add(grid);
        Button addGroupButton = new Button("Add group", new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    new AddGroupDialog(viewContext, groupList).show();
                }
            });
        addGroupButton.setId(ADD_BUTTON_ID);

        ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem("Filter:"));
        toolBar.add(new AdapterToolItem(
                new ColumnFilter<GroupModel>(store, GroupModel.CODE, "code")));
        toolBar.add(new SeparatorToolItem());
        toolBar.add(new AdapterToolItem(addGroupButton));
        cp.setBottomComponent(toolBar);

        add(cp);
        layout();

    }

    List<GroupModel> getGroupModels(List<Group> groups)
    {
        List<GroupModel> gms = new ArrayList<GroupModel>();
        for (Group g : groups)
        {
            gms.add(new GroupModel(g));
        }
        return gms;
    }

    public void refresh()
    {
        removeAll();
        add(new Text("data loading..."));
        viewContext.getService().listGroups(null, new ListGroupsCallback(viewContext));
    }
}
