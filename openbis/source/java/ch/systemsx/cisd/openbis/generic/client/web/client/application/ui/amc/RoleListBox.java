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

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;

/**
 * {@link ListBox} with RoleSets.
 * 
 * @author Izabela Adamczyk
 */
public class RoleListBox extends ListBox
{
    public RoleListBox(final GroupSelectionWidget groupWidget)
    {
        RoleWithHierarchy[] values = RoleWithHierarchy.values();
        for (RoleWithHierarchy visibleRoleCode : values)
        {
            addItem(visibleRoleCode.toString());
        }
        setVisibleItemCount(1);
        updateWidgetsVisibility(groupWidget);

        addChangeHandler(new ChangeHandler()
            {

                public final void onChange(final ChangeEvent sender)
                {
                    updateWidgetsVisibility(groupWidget);
                }
            });

    }

    public final RoleWithHierarchy getValue()
    {
        return RoleWithHierarchy.values()[getSelectedIndex()];
    }

    private void updateWidgetsVisibility(final GroupSelectionWidget group)
    {
        int index = getSelectedIndex();
        RoleWithHierarchy[] roles = RoleWithHierarchy.values();
        if (index < 0 || index >= roles.length)
            return;
        boolean groupLevel = roles[index].isSpaceLevel();
        FieldUtil.setMandatoryFlag(group, groupLevel);
        group.setVisible(groupLevel);
    }
}
