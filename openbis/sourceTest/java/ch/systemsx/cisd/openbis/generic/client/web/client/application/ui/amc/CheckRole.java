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

import junit.framework.Assert;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.DataModelPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.RoleModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension to check whether a given role is present.
 * 
 * @author Christian Ribeaud
 */
public final class CheckRole extends AbstractDefaultTestCommand
{
    private final String groupCode;

    private final String userId;

    private final String roleCode;

    public CheckRole(final String groupCode, final String userId, final String roleCode)
    {
        super(RolesView.ListRolesCallback.class);
        this.groupCode = groupCode;
        this.userId = userId;
        this.roleCode = roleCode;
    }

    //
    // AbstractDefaultTestCommand
    //

    @SuppressWarnings("unchecked")
    public final void execute()
    {
        final Widget widget = GWTTestUtil.getWidgetWithID(RolesView.TABLE_ID);
        Assert.assertTrue(widget instanceof Grid);
        final Grid<RoleModel> table = (Grid<RoleModel>) widget;
        final ListStore<RoleModel> store = table.getStore();
        for (int i = 0, n = store.getCount(); i < n; i++)
        {
            final RoleModel roleModel = store.getAt(i);
            final Object person = roleModel.get(DataModelPropertyNames.PERSON);
            final Object group = roleModel.get(DataModelPropertyNames.GROUP);
            final Object role = roleModel.get(DataModelPropertyNames.ROLE);
            if (userId.equals(person) && groupCode.equals(group) && roleCode.equals(role))
            {
                return;
            }
        }
        Assert.fail("No role assignment with person '" + userId + "', group '" + groupCode
                + "' and role '" + roleCode + "' found.");
    }
}
