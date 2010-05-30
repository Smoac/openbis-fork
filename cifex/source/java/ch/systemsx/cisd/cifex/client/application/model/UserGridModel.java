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

import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.*;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

import ch.systemsx.cisd.cifex.client.application.ViewContext;
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

    private static final long MB = 1024 * 1024L;

    public UserGridModel(final ViewContext viewContext, UserInfoDTO user)
    {
        super(viewContext.getModel().getUser());
        set(ID, user.getID());// long
        set(USER_CODE, user.getUserCode());// String
        set(USER_EMAIL, user.getEmail());// String
        set(FULL_NAME, user.getUserFullName());// String
        set(FILE_SIZE, user.getCurrentFileSize());// long
        set(FILE_COUNT, user.getCurrentFileCount());// int
        set(STATUS, getUserRoleDescription(user));// String
        set(ACTIVE, new Boolean(user.isActive()));// Boolean
        set(QUOTA_SIZE, getMaxFileSizePerQuotaGroup(user));// Long
        set(QUOTA_COUNT, getMaxFileCountPerQuotaGroup(user));// Integer
        set(REGISTRATOR, user.getRegistrator());// UserInfoDTO
        set(ACTION, listActionsForUser(user));// String

    }

    private Long getMaxFileSizePerQuotaGroup(UserInfoDTO user)
    {
        if (user.isCustomMaxFileSizePerQuotaGroup())
        {
            final Long value = user.getMaxFileSizePerQuotaGroupInMB();
            if (value == null)
            {
                return Long.MAX_VALUE;
            } else
            {
                return value * MB;
            }
        } else
        {
            return null;
        }
    }

    private Integer getMaxFileCountPerQuotaGroup(UserInfoDTO user)
    {
        if (user.isCustomMaxFileCountPerQuotaGroup())
        {
            final Integer value = user.getMaxFileCountPerQuotaGroup();
            if (value == null)
            {
                return Integer.MAX_VALUE;
            } else
            {
                return value;
            }
        } else
        {
            return null;
        }
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

    static public final List<ColumnConfig> getColumnConfigs()
    {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(createIdColumnConfig());
        configs.add(createUserCodeColumnConfig());
        configs.add(createUserEmailColumnConfig());
        configs.add(createTotalFileSizeColumnConfig());
        configs.add(createTotalFileCountColumnConfig());
        configs.add(createFullNameColumnConfig());
        configs.add(createStatusColumnConfig());
        configs.add(createActiveColumnConfig());
        configs.add(createQuotaSizeColumnConfig());
        configs.add(createQuotaCountColumnConfig());
        configs.add(createRegistratorColumnConfig());
        configs.add(createActionColumnConfig());
        return configs;
    }

    protected String listActionsForUser(final UserInfoDTO user)
    {
        final String sep = " | ";
        String actionLabel = DOMUtils.createAnchor(msg(ACTION_EDIT_LABEL), Constants.EDIT_ID);
        // Change user code
        if (user.equals(currentUser) == false && currentUser.isAdmin()
                && user.isExternallyAuthenticated() == false)
        {
            actionLabel +=
                    sep
                            + DOMUtils.createAnchor(msg(ACTION_RENAME_LABEL),
                                    Constants.CHANGE_USER_CODE_ID);
        }
        // An user can not delete itself.
        if (user.equals(currentUser) == false)
        {
            actionLabel +=
                    sep + DOMUtils.createAnchor(msg(ACTION_DELETE_LABEL), Constants.DELETE_ID);
        }
        return actionLabel;
    }

}
