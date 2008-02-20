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
import ch.systemsx.cisd.cifex.client.application.Model;
import ch.systemsx.cisd.cifex.client.application.ui.UserRenderer;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.client.application.utils.DateTimeUtils;
import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * An <code>AbstractDataGridModel</code> extension for user grid.
 * 
 * @author Basil Neff
 */
public final class UserGridModel extends AbstractDataGridModel
{
    public static final String USER_CODE = "userCode";

    public static final String USER_EMAIL = "userEmail";

    public static final String FULL_NAME = "fullName";

    public static final String STATUS = "status";

    public static final String REGISTRATOR = "registrator";

    public static final String ACTION = "action";

    /**
     * The currently logged-in user.
     * <p>
     * You typically call {@link Model#getUser()} to get him.
     * </p>
     */
    private final User currentUser;

    public UserGridModel(final IMessageResources messageResources, final User currentUser)
    {
        super(messageResources);
        this.currentUser = currentUser;
    }

    private final ColumnConfig createActionColumnConfig()
    {
        final ColumnConfig actionColumn = createSortableColumnConfig(ACTION, messageResources.getActionLabel(), 120);
        return actionColumn;
    }

    private final ColumnConfig createUserCodeColumnConfig()
    {
        final ColumnConfig columnConfig =
                createSortableColumnConfig(USER_CODE, messageResources.getUserCodeLabel(), 180);
        return columnConfig;
    }

    private final ColumnConfig createUserEmailColumnConfig()
    {
        final ColumnConfig columnConfig =
                createSortableColumnConfig(USER_EMAIL, messageResources.getUserEmailLabel(), 180);
        columnConfig.setRenderer(UserRenderer.USER_RENDERER);
        return columnConfig;
    }

    /**
     * Note that this column is NOT sortable.
     */
    private final ColumnConfig createRegistratorColumnConfig()
    {
        final ColumnConfig columnConfig = createColumnConfig(REGISTRATOR, messageResources.getRegistratorLabel(), 180);
        return columnConfig;
    }

    private final ColumnConfig createStatusColumnConfig()
    {
        return createSortableColumnConfig(STATUS, messageResources.getStatusLabel(), 250);
    }

    private final ColumnConfig createFullNameColumnConfig()
    {
        return createSortableColumnConfig(FULL_NAME, messageResources.getUserFullNameLabel(), 180);
    }

    private final String getUserRoleDescription(final User user)
    {
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
                            + " User expires on ".concat(DateTimeUtils.formatDate(user.getExpirationDate()));
        }
        return stateField;
    }

    private final String listActionsForUser(final User user)
    {
        final String sep = " | ";
        String actionLabel = DOMUtils.createAnchor(messageResources.getActionEditLabel(), Constants.EDIT_ID);
        // Regular user cannot be renewed.
        if (user.isPermanent() == false)
        {
            actionLabel += sep + DOMUtils.createAnchor(messageResources.getActionRenewLabel(), Constants.RENEW_ID);
        }
        // An user can not delete itself.
        if (user.equals(currentUser) == false)
        {
            actionLabel += sep + DOMUtils.createAnchor(messageResources.getActionDeleteLabel(), Constants.DELETE_ID);
        }
        return actionLabel;
    }

    //
    // AbstractDataGridModel
    //

    public final List getColumnConfigs()
    {
        final List configs = new ArrayList();
        configs.add(createUserCodeColumnConfig());
        configs.add(createUserEmailColumnConfig());
        configs.add(createFullNameColumnConfig());
        configs.add(createStatusColumnConfig());
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
            final String stateField = getUserRoleDescription(user);
            final String actions = listActionsForUser(user);
            final Object[] objects =
                    new Object[]
                        { user.getUserCode(), user.getEmail(), user.getUserFullName(), stateField,
                                UserRenderer.createUserAnchor(user.getRegistrator()), actions };
            list.add(objects);
        }
        return list;
    }

    public final List getFieldDefs()
    {
        final List fieldDefs = new ArrayList();
        fieldDefs.add(new StringFieldDef(USER_CODE));
        fieldDefs.add(new StringFieldDef(USER_EMAIL));
        fieldDefs.add(new StringFieldDef(FULL_NAME));
        fieldDefs.add(new StringFieldDef(STATUS));
        fieldDefs.add(new StringFieldDef(REGISTRATOR));
        fieldDefs.add(new StringFieldDef(ACTION));
        return fieldDefs;
    }

}
