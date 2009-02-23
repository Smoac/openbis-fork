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
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.IHistoryController.Page;
import ch.systemsx.cisd.cifex.client.application.ui.UserRenderer;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.client.dto.UserInfoDTO;

/**
 * An <code>AbstractUserGridModel</code> extension for user grid.
 * 
 * @author Basil Neff
 */
public final class UserGridModel extends AbstractUserGridModel
{

    private final ViewContext context;

    public UserGridModel(final ViewContext viewContext)
    {
        super(viewContext.getMessageResources(), viewContext.getModel().getUser());
        this.context = viewContext;
    }

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
            final UserInfoDTO user = (UserInfoDTO) data[i];
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

    protected String listActionsForUser(final UserInfoDTO user)
    {
        final String sep = " | ";
        String actionLabel =
                DOMUtils.createAnchor(messageResources.getActionEditLabel(), Constants.EDIT_ID);
        // Regular user cannot be renewed.
        if (user.isPermanent() == false)
        {
            actionLabel +=
                    sep
                            + DOMUtils.createAnchor(messageResources.getActionRenewLabel(),
                                    Constants.RENEW_ID);
        }
        // An user can not delete itself.
        if (user.equals(currentUser) == false)
        {
            actionLabel +=
                    sep
                            + DOMUtils.createAnchor(messageResources.getActionDeleteLabel(),
                                    Constants.DELETE_ID);
        }
        // Change user code
        if (user.equals(currentUser) == false && currentUser.isAdmin()
                && user.isExternallyAuthenticated() == false
                && context.getHistoryController().getCurrentPage() == Page.ADMIN_PAGE)
        {
            actionLabel +=
                    sep
                            + DOMUtils.createAnchor(messageResources.getActionRenameLabel(),
                                    Constants.CHANGE_USER_CODE_ID);
        }
        return actionLabel;
    }

}
