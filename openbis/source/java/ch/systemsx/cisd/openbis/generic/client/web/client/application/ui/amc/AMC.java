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

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;

public class AMC extends TabPanel
{

    public AMC(GenericViewContext viewContext)
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

        TabItem rolesTab = new TabItem(viewContext.getMessage("rolesView_heading"));
        rolesTab.addStyleName("pad-text");
        RolesView roleList = new RolesView(viewContext);
        roleList.refresh();
        rolesTab.add(roleList);

        add(personsTab);
        add(groupsTab);
        add(rolesTab);

    }
}