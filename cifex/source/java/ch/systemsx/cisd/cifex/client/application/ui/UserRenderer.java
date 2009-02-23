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

import com.gwtext.client.data.Record;
import com.gwtext.client.data.Store;
import com.gwtext.client.widgets.grid.CellMetadata;
import com.gwtext.client.widgets.grid.Renderer;

import ch.systemsx.cisd.cifex.client.application.Constants;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.shared.basic.dto.BasicUser;

/**
 * A <code>Renderer</code> implementation that create an email anchor element with inner text
 * <code>value.toString()</code>.
 * 
 * @author Christian Ribeaud
 */
public final class UserRenderer implements Renderer
{

    /** The unique instance of <code>UserRenderer</code>. */
    public final static Renderer USER_RENDERER = new UserRenderer();

    private UserRenderer()
    {
    }

    /**
     * Nicely renders given <code>user</code>.
     * <p>
     * You can not use this method in a {@link Renderer} and you should not make sortable the column
     * where this method is used.
     * </p>
     */
    public final static String createUserAnchor(final BasicUser[] users)
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
     * You can not use this method in a {@link Renderer} and you should not make sortable the column
     * where this method is used.
     * </p>
     */
    public final static String createUserAnchor(final BasicUser user)
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

    //
    // Renderer
    //

    public final String render(final Object value, final CellMetadata cellMetadata,
            final Record record, final int rowIndex, final int colNum, final Store store)
    {
        if (value == null)
        {
            return Constants.TABLE_NULL_VALUE;
        }
        final String email = value.toString();
        return DOMUtils.createEmailAnchor(email, null);
    }
}
