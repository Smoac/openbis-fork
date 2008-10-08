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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;

public class AMC extends TabPanel
{
    private static final String PREFIX = "tabbed-pane";

    static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    static final String GROUPS_TAB = ID + "_groups";

    static final String PERSONS_TAB = ID + "_persons";

    static final String ROLES_TAB = ID + "_roles";

    public AMC(final GenericViewContext viewContext)
    {
        setId(ID);

        final TabItem groupsTab = new TabItem(viewContext.getMessage("groupsView_heading"));
        groupsTab.setId(GROUPS_TAB);
        groupsTab.addStyleName("pad-text");
        final GroupsView groupList = new GroupsView(viewContext);
        groupList.refresh();
        groupsTab.add(groupList);

        final TabItem personsTab = new TabItem(viewContext.getMessage("personsView_heading"));
        personsTab.setId(PERSONS_TAB);
        personsTab.addStyleName("pad-text");
        final PersonsView personList = new PersonsView(viewContext);
        personList.refresh();
        personsTab.add(personList);

        final TabItem rolesTab = new TabItem(viewContext.getMessage("rolesView_heading"));
        rolesTab.setId(ROLES_TAB);
        rolesTab.addStyleName("pad-text");
        final RolesView roleList = new RolesView(viewContext);
        roleList.refresh();
        rolesTab.add(roleList);

        add(personsTab);
        add(groupsTab);
        add(rolesTab);

    }
}