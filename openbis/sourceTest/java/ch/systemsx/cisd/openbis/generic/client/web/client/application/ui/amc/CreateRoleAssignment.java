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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.MainTabPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.RoleAssignmentGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleSetCode;

/**
 * A {@link AbstractDefaultTestCommand} extension for assigning a role to a person.
 * 
 * @author Christian Ribeaud
 */
public final class CreateRoleAssignment extends CheckTableCommand
{

    private final String groupNameOrNull;

    private final String personName;

    private final String roleNameOrNull;

    public CreateRoleAssignment(final String groupNameOrNull, final String personName,
            final String roleNameOrNull)
    {
        super(RoleAssignmentGrid.GRID_ID);
        assert groupNameOrNull == null && roleNameOrNull == null || groupNameOrNull != null
                && roleNameOrNull != null;
        this.groupNameOrNull = groupNameOrNull;
        this.personName = personName;
        this.roleNameOrNull = roleNameOrNull;
    }

    //
    // AbstractDefaultTestCommand
    //

    @Override
    public final void execute()
    {
        GWTTestUtil.selectTabItemWithId(MainTabPanel.ID, RoleAssignmentGrid.BROWSER_ID
                + MainTabPanel.TAB_SUFFIX);
        GWTTestUtil.clickButtonWithID(RoleAssignmentGrid.ASSIGN_BUTTON_ID);
        final RoleListBox listBox =
                (RoleListBox) GWTTestUtil.getListBoxWithID(AddRoleAssignmentDialog.ROLE_FIELD_ID);
        if (groupNameOrNull != null)
        {
            GWTTestUtil.getTextFieldWithID(AddRoleAssignmentDialog.GROUP_FIELD_ID).setValue(
                    groupNameOrNull);
            GWTUtils.setSelectedItem(listBox, roleNameOrNull);
        } else
        {
            GWTUtils.setSelectedItem(listBox, RoleSetCode.INSTANCE_ADMIN.toString());
        }
        GWTTestUtil.getTextFieldWithID(AddRoleAssignmentDialog.PERSON_FIELD_ID)
                .setValue(personName);
        GWTTestUtil.clickButtonWithID(AddRoleAssignmentDialog.SAVE_BUTTON_ID);
    }
}
