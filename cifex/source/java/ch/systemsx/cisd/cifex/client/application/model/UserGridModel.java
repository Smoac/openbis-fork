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
import ch.systemsx.cisd.cifex.client.application.ui.UserRenderer;
import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * An <code>AbstractDataGridModel</code> extension for user grid.
 * 
 * @author Basil Neff
 */
public class UserGridModel extends AbstractDataGridModel
{

    public static final String USER_CODE = "UserCode";

    public static final String FULL_NAME = "FullName";

    public static final String STATUS = "Status";

    public static final String REGISTRATOR = "Registrator";

    public static final String ACTION = "Action";

    /**
     * User Code which is only internal needed, to identify the user in the column.
     * In this row is no renderer set, that you can get the user with the String function.
     */
    public static final String INTERNAL_USER_CODE = "IntUserCode";

    public UserGridModel(final IMessageResources messageResources)
    {
        super(messageResources);
    }

    private final ColumnConfig createActionColumnConfig()
    {
        final ColumnConfig actionColumn = createSortableColumnConfig(ACTION, messageResources.getActionLabel(), 120);
        return actionColumn;
    }

    private final ColumnConfig createUserCodeColumnConfig()
    {
        final ColumnConfig columnConfig = createSortableColumnConfig(USER_CODE, messageResources.getUserCodeLabel(), 180);
        return columnConfig;
    }

    private final ColumnConfig createRegistratorColumnConfig()
    {
        final ColumnConfig columnConfig =
                createSortableColumnConfig(REGISTRATOR, messageResources.getRegistratorLabel(), 180);
        columnConfig.setRenderer(UserRenderer.USER_RENDERER);
        return columnConfig;
    }

    //
    // AbstractDataGridModel
    //

    public final List getColumnConfigs()
    {
        final List configs = new ArrayList();
        configs.add(createUserCodeColumnConfig());
        configs.add(createSortableColumnConfig(FULL_NAME, messageResources.getUserFullNameLabel(), 120));
        configs.add(createSortableColumnConfig(STATUS, messageResources.getStatusLabel(), 250));
        configs.add(createRegistratorColumnConfig());
        configs.add(createActionColumnConfig());
        return configs;
    }

    public final List getData(final Object[] data)
    {
        final List list = new ArrayList();
        for (int i = 0; i < data.length; i++)
        {
            final User user = (User) data[i];
            String stateField = "";
            if (user.isAdmin())
            {
                stateField = messageResources.getAdminRoleName();
            } else if (user.isPermanent())
            {
                stateField += messageResources.getPermanentRoleName() + " User";
            } else
            {
                stateField +=
                        messageResources.getTemporaryRoleName()
                                + " User expires on ".concat(Constants.defaultDateTimeFormat.format(user
                                        .getExpirationDate()));
            }
            String actionLabel =
                    "<a href=\"#\" class=\"edit\" id=\"edit\">"+messageResources.getActionEditLabel()+"</a> | <a href=\"#\" class=\"delete\" id=\"delete\">"
                            + messageResources.getActionDeleteLabel() + "</a>";
            final Object[] objects =
                    new Object[]
                        { UserRenderer.createUserAnchor(user), user.getUserFullName(), stateField,
                                user.getRegistrator().getEmail(), actionLabel, user.getUserCode() };
            list.add(objects);
        }
        return list;
    }

    public final List getFieldDefs()
    {
        final List fieldDefs = new ArrayList();
        fieldDefs.add(new StringFieldDef(USER_CODE));
        fieldDefs.add(new StringFieldDef(FULL_NAME));
        fieldDefs.add(new StringFieldDef(STATUS));
        fieldDefs.add(new StringFieldDef(REGISTRATOR));
        fieldDefs.add(new StringFieldDef(ACTION));
        fieldDefs.add(new StringFieldDef(INTERNAL_USER_CODE));
        return fieldDefs;
    }

}
