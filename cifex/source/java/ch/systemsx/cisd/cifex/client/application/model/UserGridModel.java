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

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.IHistoryController.Page;
import ch.systemsx.cisd.cifex.client.application.ui.UserRenderer;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * An <code>AbstractUserGridModel</code> extension for user grid.
 * 
 * @author Basil Neff
 */
public final class UserGridModel extends AbstractUserGridModel
{
    private static final long serialVersionUID = Constants.VERSION;

    private final ViewContext context;

    public UserGridModel(final ViewContext viewContext, UserInfoDTO user)
    {
        super(viewContext.getMessageResources(), viewContext.getModel().getUser());
        this.context = viewContext;
        String registratorAnchor = null;
        if (user.getRegistrator() != null)
        {
            registratorAnchor = UserRenderer.createUserAnchor(user.getRegistrator());
        }
        set(ID, user.getID());// long
        set(USER_CODE, user.getUserCode());// String
        set(USER_EMAIL, user.getEmail());// String
        set(FULL_NAME, user.getUserFullName());// String
        set(STATUS, getUserRoleDescription(user));// String
        set(ACTIVE, new Boolean(user.isActive()));// Boolean
        set(REGISTRATOR, (registratorAnchor));// String
        set(ACTION, listActionsForUser(user));// String

    }

    public long getID()
    {
        return get(ID);
    }

    public final static List<UserGridModel> convert(ViewContext context,
            final List<UserInfoDTO> users)
    {
        final List<UserGridModel> result = new ArrayList<UserGridModel>();

        for (final UserInfoDTO user : users)
        {
            result.add(new UserGridModel(context, user));
        }

        return result;
    }

    static public final List<ColumnConfig> getColumnConfigs(IMessageResources messageResources)
    {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(createIdColumnConfig());
        configs.add(createUserCodeColumnConfig(messageResources));
        configs.add(createUserEmailColumnConfig(messageResources));
        configs.add(createFullNameColumnConfig(messageResources));
        configs.add(createStatusColumnConfig(messageResources));
        configs.add(createActiveColumnConfig(messageResources));
        configs.add(createRegistratorColumnConfig(messageResources));
        configs.add(createActionColumnConfig(messageResources));
        return configs;
    }

    protected String listActionsForUser(final UserInfoDTO user)
    {
        final String sep = " | ";
        String actionLabel =
                DOMUtils.createAnchor(messageResources.getActionEditLabel(), Constants.EDIT_ID);
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
        // An user can not delete itself.
        if (user.equals(currentUser) == false)
        {
            actionLabel +=
                    sep
                            + DOMUtils.createAnchor(messageResources.getActionDeleteLabel(),
                                    Constants.DELETE_ID);
        }
        return actionLabel;
    }

}
