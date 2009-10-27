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

package ch.systemsx.cisd.cifex.client.application.ui;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.cifex.client.application.model.AbstractUserGridModel;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.BasicUserInfoDTO;

/**
 * A <code>Renderer</code> implementation that create an email anchor element with inner text
 * <code>value.toString()</code>.
 * 
 * @author Christian Ribeaud
 */
public final class UserRenderer implements GridCellRenderer<AbstractUserGridModel>
{

    /** The unique instance of <code>UserRenderer</code>. */
    public final static UserRenderer USER_RENDERER = new UserRenderer();

    private UserRenderer()
    {
    }

    /**
     * Nicely renders given <code>user</code>.
     * <p>
     * You can not use this method in a {@link GridCellRenderer} and you should not make sortable
     * the column where this method is used.
     * </p>
     */
    public final static String createUserAnchor(final BasicUserInfoDTO[] users)
    {
        assert users != null : "Unspecified user.";

        if (users.length == 0)
        {
            return Constants.TABLE_NULL_VALUE;
        }
        String anchor = "";
        for (int i = 0; i < users.length; ++i)
        {
            if (i < users.length - 1)
            {
                anchor += users[i].getUserCode() + ", ";
            } else
            {
                anchor += users[i].getUserCode();
            }
        }
        return anchor;
    }

    /**
     * Nicely renders given <code>user</code>.
     * <p>
     * You can not use this method in a {@link GridCellRenderer} and you should not make sortable
     * the column where this method is used.
     * </p>
     */
    public final static String createUserAnchor(final BasicUserInfoDTO user)
    {
        assert user != null : "Unspecified user.";

        final String fullName = user.getUserFullName();
        final String userCode = user.getUserCode();
        final String email = user.getEmail();
        final String userDescription = StringUtils.isBlank(fullName) ? userCode : fullName;
        if (StringUtils.isBlank(userDescription))
        {
            return Constants.TABLE_NULL_VALUE;
        }
        if (StringUtils.isBlank(email))
        {
            return userDescription;
        } else
        {
            return DOMUtils.createEmailAnchor(email, userDescription);
        }
    }

    public Object render(AbstractUserGridModel model, String property, ColumnData config,
            int rowIndex, int colIndex, ListStore<AbstractUserGridModel> store,
            Grid<AbstractUserGridModel> grid)
    {
        String value = String.valueOf(model.get(property));
        if (value == null)
        {
            return Constants.TABLE_NULL_VALUE;
        }
        final String email = value.toString();
        return DOMUtils.createEmailAnchor(email, null);
    }
}
