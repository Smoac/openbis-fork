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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;

/**
 * {@link Window} containing role registration form.
 * 
 * @author Izabela Adamczyk
 */
public class AddRoleDialog extends Window
{

    public AddRoleDialog(final GenericViewContext viewContext, final RolesView roleList)
    {

        setHeading("Add a new role");
        setModal(true);
        setWidth(400);
        FormPanel form = new FormPanel();
        form.setHeaderVisible(false);
        form.setBorders(false);
        form.setBodyBorder(false);
        add(form);

        final TextField<String> group = new TextField<String>();
        group.setWidth(100);
        group.setFieldLabel("Group");
        group.setAllowBlank(false);

        final AdapterField roleBox = new AdapterField(new RoleListBox(group));
        roleBox.setFieldLabel("Role");
        roleBox.setWidth(100);
        form.add(roleBox);
        form.add(group);

        final TextField<String> user = new TextField<String>();
        user.setWidth(100);
        user.setFieldLabel("Person");
        user.setAllowBlank(false);
        form.add(user);

        addButton(new Button("Save", new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    final AbstractAsyncCallback<Void> roleLoadingCallback =
                            new AbstractAsyncCallback<Void>(viewContext)
                                {
                                    @Override
                                    public void process(Void result)
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
            }));
        addButton(new Button("Cancel", new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    hide();
                }
            }));
    }
}
