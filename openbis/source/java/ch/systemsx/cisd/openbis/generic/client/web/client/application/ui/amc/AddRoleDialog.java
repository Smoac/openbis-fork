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

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.RolesView;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;

/**
 * {@link Window} containing role registration form.
 * 
 * @author Izabela Adamczyk
 */
public class AddRoleDialog extends Window
{
    private static final String PREFIX = "add-role_";

    static final String GROUP_FIELD_ID = GenericConstants.ID_PREFIX + PREFIX + "group-field";

    static final String PERSON_FIELD_ID = GenericConstants.ID_PREFIX + PREFIX + "person-field";

    static final String ROLE_FIELD_ID = GenericConstants.ID_PREFIX + PREFIX + "role-field";

    static final String SAVE_BUTTON_ID = GenericConstants.ID_PREFIX + PREFIX + "save-button";

    private final TextField<String> group;

    private final TextField<String> user;

    private final AdapterField roleBox;

    public AddRoleDialog(final CommonViewContext viewContext, final RolesView roleList)
    {

        setHeading("Add a new role");
        setModal(true);
        setWidth(400);
        FormPanel form = new FormPanel();
        form.setHeaderVisible(false);
        form.setBorders(false);
        form.setBodyBorder(false);
        add(form);

        group = new TextField<String>();
        group.setWidth(100);
        group.setFieldLabel("Group");
        group.setAllowBlank(false);
        group.setId(GROUP_FIELD_ID);

        roleBox = new AdapterField(new RoleListBox(group));
        roleBox.setFieldLabel("Role");
        roleBox.setWidth(100);
        roleBox.setId(ROLE_FIELD_ID);
        form.add(roleBox);
        form.add(group);

        user = new TextField<String>();
        user.setWidth(100);
        user.setFieldLabel("Person");
        user.setAllowBlank(false);
        user.setId(PERSON_FIELD_ID);
        form.add(user);

        addButton(createSaveButton(viewContext, roleList));
        addButton(new Button("Cancel", new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    hide();
                }
            }));
    }

    private final Button createSaveButton(final CommonViewContext viewContext,
            final RolesView roleList)
    {
        final Button button = new Button("Save", new SelectionListener<ComponentEvent>()
            {
                //
                // SelectionListener
                //

                @Override
                public final void componentSelected(final ComponentEvent ce)
                {
                    final AbstractAsyncCallback<Void> roleLoadingCallback =
                            new AbstractAsyncCallback<Void>(viewContext)
                                {
                                    //
                                    // AbstractAsyncCallback
                                    //

                                    @Override
                                    public final void process(final Void result)
                                    {
                                        hide();
                                        roleList.refresh();
                                    }
                                };
                    if (StringUtils.isBlank(group.getValue()))
                    {
                        viewContext.getService().registerInstanceRole(
                                ((RoleListBox) roleBox.getWidget()).getValue(), user.getValue(),
                                roleLoadingCallback);
                    } else
                    {
                        viewContext.getService().registerGroupRole(
                                ((RoleListBox) roleBox.getWidget()).getValue(), group.getValue(),
                                user.getValue(), roleLoadingCallback);
                    }
                }
            });
        button.setId(SAVE_BUTTON_ID);
        return button;
    }
}
