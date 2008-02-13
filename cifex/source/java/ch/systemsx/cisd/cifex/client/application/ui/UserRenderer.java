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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.Store;
import com.gwtext.client.widgets.grid.CellMetadata;
import com.gwtext.client.widgets.grid.Renderer;

import ch.systemsx.cisd.cifex.client.application.Constants;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.client.dto.User;

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
     * Creates an email anchor with given <var>email</var>.
     * 
     * @param innerText if blank, then given <var>email</var> is taken as inner text.
     */
    public final static String createEmailAnchor(final String email, final String innerText)
    {
        assert email != null : "Undefined email.";
        final Element anchor = LinkRenderer.createBasicAnchorElement();
        DOM.setElementAttribute(anchor, "href", "mailto:" + email);
        DOM.setElementAttribute(anchor, "title", email);
        DOM.setInnerText(anchor, StringUtils.isBlank(innerText) ? email : innerText);
        return DOM.toString(anchor);
    }

    /**
     * Nicely renders given <code>user</code>.
     * <p>
     * You can not use this method in a {@link Renderer} and you should not make sortable the column where this method
     * is used.
     * </p>
     */
    public final static String createUserAnchor(final User user)
    {
        assert user != null : "Unspecified user.";

        final String fullName = user.getUserFullName();
        final String userCode = user.getUserCode();
        final String email = user.getEmail();
        final String userDescription = StringUtils.isBlank(fullName) ? userCode : fullName;
        if (StringUtils.isBlank(userDescription))
        {
            return "-";
        }
        if (StringUtils.isBlank(email))
        {
            return userDescription;
        } else
        {
            return UserRenderer.createEmailAnchor(email, userDescription);
        }
    }

    //
    // Renderer
    //

    public final String render(final Object value, final CellMetadata cellMetadata, final Record record,
            final int rowIndex, final int colNum, final Store store)
    {
        if (value == null)
        {
            return Constants.TABLE_NULL_VALUE;
        }
        final String email = value.toString();
        return createEmailAnchor(email, null);
    }
}
