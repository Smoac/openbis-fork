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

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.Footer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupsView;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PersonsView;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TopMenu;

/**
 * Panel of the application.
 * 
 * @author Franz-Josef Elmer
 * @author Izabela Adamczyk
 */
public class Application extends Viewport
{
    private static final int MARGIN_SIZE = 3;

    Application(GenericViewContext viewContext)
    {
        createGUI(viewContext);
    }

    private void createGUI(final GenericViewContext viewContext)
    {

        setLayout(new FitLayout());
        LayoutContainer panels = new LayoutContainer();
        panels.setLayout(new RowLayout());

        TopMenu north = new TopMenu(viewContext);
        MainPanel center = new MainPanel(viewContext);
        Footer south = new Footer(viewContext);

        RowData northData = new RowData(1, -1, new Margins(MARGIN_SIZE));
        RowData centerData = new RowData(1, 1, new Margins(MARGIN_SIZE));
        RowData southData = new RowData(1, -1, new Margins(MARGIN_SIZE));

        panels.add(north, northData);
        panels.add(center, centerData);
        panels.add(south, southData);

        add(panels);
    }

    class MainPanel extends TabPanel
    {

        public MainPanel(GenericViewContext viewContext)
        {

            TabItem groupsTab = new TabItem(viewContext.getMessage("groupsView_heading"));
            groupsTab.addStyleName("pad-text");
            GroupsView groupList = new GroupsView(viewContext);
            groupList.refresh();
            groupsTab.add(groupList);

            TabItem personsTab = new TabItem(viewContext.getMessage("personsView_heading"));
            personsTab.addStyleName("pad-text");
            PersonsView personList = new PersonsView(viewContext);
            personList.refresh();
            personsTab.add(personList);

            TabItem instanceTab = new TabItem(viewContext.getMessage("instanceView_heading"));
            instanceTab.addStyleName("pad-text");
            instanceTab.add(new InstanceView(viewContext));

            add(personsTab);
            add(groupsTab);
            // add(instance);
        }
    }

    class InstanceView extends LayoutContainer
    {

        public InstanceView(GenericViewContext viewContext)
        {

        }

    }

}
