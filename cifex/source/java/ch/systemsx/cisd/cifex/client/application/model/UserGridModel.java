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

import ch.systemsx.cisd.cifex.client.application.Constants;
import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ui.UserLinkRenderer;
import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * @author Basil Neff
 */
public class UserGridModel extends AbstractDataGridModel
{

    private final String USER_NAME = "User";

    private final String STATUS = "Status";

    private final String MODIFY_USER = "Modify";

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

     // TODO 2008-1-28 Basil Neff: Get Field from MessageResource (2nd Parameter)
        configs.add(createSortableColumnConfig(USER_NAME, USER_NAME, 120));
        configs.add(createSortableColumnConfig(STATUS, STATUS, 250));
        configs.add(createSortableColumnConfig(MODIFY_USER, MODIFY_USER, 120));
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
                stateField = "Administrator, ";
            }

            if (user.isPermanent())
            {
                stateField += "permanent";
            } else
            {
                stateField += "expires on ".concat(Constants.defaultDateTimeFormat.format(user.getExpirationDate()));
            }
            final Object[] objects = new Object[]
                { UserLinkRenderer.createUserAnchor(user), stateField, "delete" };
            list.add(objects);
        }
        return list;
    }

    public List getFieldDefs()
    {
        final List fieldDefs = new ArrayList();
        fieldDefs.add(new StringFieldDef(USER_NAME));
        fieldDefs.add(new StringFieldDef(STATUS));
        fieldDefs.add(new StringFieldDef(MODIFY_USER));
        return fieldDefs;
    }

}
