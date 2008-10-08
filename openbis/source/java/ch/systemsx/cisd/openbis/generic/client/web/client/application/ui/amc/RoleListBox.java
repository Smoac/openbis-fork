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

import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * {@link ListBox} with RoleSets.
 * 
 * @author Izabela Adamczyk
 */
public class RoleListBox extends ListBox
{
    public static final String OBSERVER = "OBSERVER";

    public static final String INSTANCE_ADMIN = "INSTANCE_ADMIN";

    public RoleListBox(final TextField<String> group)
    {
        // TODO 2008-10-08, Christian Ribeaud: Get this from the database or, at least, make an
        // enumeration.
        addItem(OBSERVER);
        addItem("USER");
        addItem("GROUP_ADMIN");
        addItem(INSTANCE_ADMIN);
        setVisibleItemCount(1);

        addChangeListener(new ChangeListener()
            {
                //
                // ChangeListener
                //

                public final void onChange(final Widget sender)
                {
                    if (getSelectedIndex() != 3)
                    {
                        group.show();
                        group.setAllowBlank(false);
                    } else
                    {
                        group.hide();
                        group.setAllowBlank(true);
                    }
                }
            });

    }

    public final String getValue()
    {
        return getValue(getSelectedIndex());
    }
}
