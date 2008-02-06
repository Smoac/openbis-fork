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
import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * A <code>Renderer</code> implementation suitable for {@link User}.
 * 
 * @author Christian Ribeaud
 */
public final class UserRenderer implements Renderer
{
    /** The unique instance of this class. */
    public final static Renderer USER_RENDERER = new UserRenderer();

    private UserRenderer()
    {
        // Can not be instantiated.
    }

    /** Creates an HTML A element for given <var>user</var> representation. */
    public final static String createUserAnchor(final User user)
    {
        final String userCode;

        userCode = user.getUserCode();
        final String email = user.getEmail();
        if (email != null)
        {
            final Element anchor = createAnchorElement(email);
            DOM.setInnerText(anchor, userCode);
            return DOM.toString(anchor);
        } else
        {
            return userCode;
        }
    }

    private final static Element createAnchorElement(final String email)
    {
        final Element anchor = DOM.createAnchor();
        DOM.setElementAttribute(anchor, "href", "mailto:" + email);
        DOM.setElementAttribute(anchor, "class", "cifex-a");
        DOM.setElementAttribute(anchor, "title", email);
        return anchor;
    }

    /** Creates an HTML A element for given <var>user</var> representation. */
    public final static String createUserAnchor(final String email)
    {
        final Element anchor = createAnchorElement(email);
        DOM.setInnerText(anchor, email);
        return DOM.toString(anchor);
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
        return createUserAnchor(value.toString());
    }

}
