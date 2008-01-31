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

package ch.systemsx.cisd.cifex.client.application.model;

import java.util.ArrayList;
import java.util.List;

import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.widgets.grid.ColumnConfig;

import ch.systemsx.cisd.cifex.client.application.Constants;
import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ui.LinkRenderer;
import ch.systemsx.cisd.cifex.client.application.ui.UserLinkRenderer;
import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * @author Basil Neff
 */
public class UserGridModel extends AbstractDataGridModel
{

    public static final String EMAIL = "Email";

    public static final String FULL_NAME = "FullName";

    public static final String STATUS = "Status";

    public static final String REGISTRATOR = "Registrator";

    public static final String ACTION = "Action";

    /**
     * @param messageResources
     */
    public UserGridModel(IMessageResources messageResources)
    {
        super(messageResources);
    }

    public List getColumnConfigs()
    {
        final List configs = new ArrayList();

        configs.add(createSortableColumnConfig(EMAIL, messageResources.getUserEmailLabel(), 180));
        configs.add(createSortableColumnConfig(FULL_NAME, messageResources.getUserNameLabel(), 120));
        configs.add(createSortableColumnConfig(STATUS, messageResources.getStatusLabel(), 250));
        configs.add(createSortableColumnConfig(REGISTRATOR, messageResources.getRegistratorLabel(), 180));
        ColumnConfig actionColumn = createSortableColumnConfig(ACTION, messageResources.getActionLabel(), 120);
        actionColumn.setRenderer(LinkRenderer.LINK_RENDERER);
        configs.add(actionColumn);
        return configs;
    }

    public List getData(Object[] data)
    {
        final List list = new ArrayList();
        for (int i = 0; i < data.length; i++)
        {
            final User user = (User) data[i];

            String stateField = "";
            if (user.isAdmin())
            {
                stateField = messageResources.getAdminRoleName();
            }
            else if (user.isPermanent())
            {
                stateField += messageResources.getPermanentRoleName() +" User";
            } else
            {
                stateField += messageResources.getTemporaryRoleName()+" User expires on ".concat(Constants.defaultDateTimeFormat.format(user.getExpirationDate()));
            }
            final Object[] objects = new Object[]
                { UserLinkRenderer.createMailAnchor(user.getEmail()), user.getUserName(), stateField, 
                    UserLinkRenderer.createMailAnchor(user.getRegistrator().getEmail()), "Delete" };
            list.add(objects);
        }
        return list;
    }

    public List getFieldDefs()
    {
        final List fieldDefs = new ArrayList();
        fieldDefs.add(new StringFieldDef(EMAIL));
        fieldDefs.add(new StringFieldDef(FULL_NAME));
        fieldDefs.add(new StringFieldDef(STATUS));
        fieldDefs.add(new StringFieldDef(REGISTRATOR));
        fieldDefs.add(new StringFieldDef(ACTION));
        return fieldDefs;
    }

}
