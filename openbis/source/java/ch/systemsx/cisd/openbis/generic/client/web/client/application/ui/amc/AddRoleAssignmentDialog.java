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

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;

/**
 * {@link Window} containing role assignment registration form.
 * 
 * @author Izabela Adamczyk
 */
public class AddRoleAssignmentDialog extends AbstractRegistrationDialog
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private static final String PREFIX = "add-role_";

    static final String GROUP_FIELD_ID = GenericConstants.ID_PREFIX + PREFIX + "group-field";

    static final String PERSON_FIELD_ID = GenericConstants.ID_PREFIX + PREFIX + "person-field";

    static final String ROLE_FIELD_ID = GenericConstants.ID_PREFIX + PREFIX + "role-field";

    private final TextField<String> group;

    private final TextField<String> user;

    private final AdapterField roleBox;

    public AddRoleAssignmentDialog(final IViewContext<ICommonClientServiceAsync> viewContext,
            final IDelegatedAction postRegistrationCallback)
    {
        super(viewContext, "Assign a Role to a Person", postRegistrationCallback);
        this.viewContext = viewContext;

        group = new VarcharField("Group", true);
        group.setWidth(100);
        group.setId(GROUP_FIELD_ID);

        roleBox = new AdapterField(new RoleListBox(group));
        roleBox.setFieldLabel("Role");
        roleBox.setWidth(100);
        roleBox.setId(ROLE_FIELD_ID);
        addField(roleBox);
        addField(group);

        user = new VarcharField("Person", true);
        user.setWidth(100);
        user.setId(PERSON_FIELD_ID);
        addField(user);
    }

    @Override
    protected void register(AsyncCallback<Void> registrationCallback)
    {
        if (StringUtils.isBlank(group.getValue()))
        {
            viewContext.getService().registerInstanceRole(
                    ((RoleListBox) roleBox.getWidget()).getValue(), user.getValue(),
                    registrationCallback);
        } else
        {
            viewContext.getService().registerGroupRole(
                    ((RoleListBox) roleBox.getWidget()).getValue(), group.getValue(),
                    user.getValue(), registrationCallback);
        }
    }
}
