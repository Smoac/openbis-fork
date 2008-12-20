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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.RolesView;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.MainTabPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension for creating a role.
 * 
 * @author Christian Ribeaud
 */
public final class CreateRole extends AbstractDefaultTestCommand
{

    private final String groupNameOrNull;

    private final String personName;

    private final String roleNameOrNull;

    public CreateRole(final String groupNameOrNull, final String personName,
            final String roleNameOrNull)
    {
        super(RolesView.ListRolesCallback.class);
        assert groupNameOrNull == null && roleNameOrNull == null || groupNameOrNull != null
                && roleNameOrNull != null;
        this.groupNameOrNull = groupNameOrNull;
        this.personName = personName;
        this.roleNameOrNull = roleNameOrNull;
    }

    //
    // AbstractDefaultTestCommand
    //

    public final void execute()
    {
        GWTTestUtil.selectTabItemWithId(MainTabPanel.ID, RolesView.ID + MainTabPanel.TAB_SUFFIX);
        GWTTestUtil.clickButtonWithID(RolesView.ADD_BUTTON_ID);
        final RoleListBox listBox =
                (RoleListBox) GWTTestUtil.getListBoxWithID(AddRoleDialog.ROLE_FIELD_ID);
        if (groupNameOrNull != null)
        {
            GWTTestUtil.getTextFieldWithID(AddRoleDialog.GROUP_FIELD_ID).setValue(groupNameOrNull);
            GWTUtils.setSelectedItem(listBox, roleNameOrNull);
        } else
        {
            GWTUtils.setSelectedItem(listBox, RoleListBox.INSTANCE_ADMIN);
        }
        GWTTestUtil.getTextFieldWithID(AddRoleDialog.PERSON_FIELD_ID).setValue(personName);
        GWTTestUtil.clickButtonWithID(AddRoleDialog.SAVE_BUTTON_ID);
    }
}
