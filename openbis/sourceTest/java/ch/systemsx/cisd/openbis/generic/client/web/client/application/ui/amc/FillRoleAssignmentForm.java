/*
 * Copyright 2009 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AuthorizationGroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PersonSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractSaveDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleSetCode;

/**
 * Wait until all fields are loaded and fill role assignment form.
 * 
 * @author Izabela Adamczyk
 */
public class FillRoleAssignmentForm extends AbstractDefaultTestCommand
{

    private final String groupNameOrNull;

    private final String personName;

    private final String roleNameOrNull;

    public FillRoleAssignmentForm(final String groupNameOrNull, final String personName,
            final String roleNameOrNull)
    {
        super(PersonSelectionWidget.ListItemsCallback.class);
        assert groupNameOrNull == null && roleNameOrNull == null || groupNameOrNull != null
                && roleNameOrNull != null;
        addCallbackClass(GroupSelectionWidget.ListGroupsCallback.class);
        addCallbackClass(AuthorizationGroupSelectionWidget.ListItemsCallback.class);
        this.groupNameOrNull = groupNameOrNull;
        this.personName = personName;
        this.roleNameOrNull = roleNameOrNull;
    }

    public void execute()
    {
        final RoleListBox listBox =
                (RoleListBox) GWTTestUtil.getListBoxWithID(AddRoleAssignmentDialog.ROLE_FIELD_ID);
        if (groupNameOrNull != null)
        {
            String groupSelectorId =
                    GroupSelectionWidget.ID + GroupSelectionWidget.SUFFIX
                            + AddRoleAssignmentDialog.PREFIX;
            GWTTestUtil.selectValueInSelectionWidget(groupSelectorId, ModelDataPropertyNames.CODE,
                    groupNameOrNull);
            GWTUtils.setSelectedItem(listBox, roleNameOrNull);
        } else
        {
            GWTUtils.setSelectedItem(listBox, RoleSetCode.INSTANCE_ADMIN.toString());
        }
        GWTTestUtil.selectValueInSelectionWidget(PersonSelectionWidget.ID
                + PersonSelectionWidget.SUFFIX + AddRoleAssignmentDialog.PREFIX,
                ModelDataPropertyNames.CODE, personName);
        GWTTestUtil.clickButtonWithID(AbstractSaveDialog.SAVE_BUTTON_ID);
    }

}
