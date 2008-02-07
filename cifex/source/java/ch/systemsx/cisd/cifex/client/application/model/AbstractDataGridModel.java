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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.gwtext.client.widgets.grid.ColumnConfig;

import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ui.LinkRenderer;
import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * Abstract data grid model with convenient methods.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractDataGridModel implements IDataGridModel
{
    protected final IMessageResources messageResources;

    public AbstractDataGridModel(final IMessageResources messageResources)
    {
        this.messageResources = messageResources;
    }

    protected final ColumnConfig createSortableColumnWithLinkConfig(final String code, final String title,
            final int width)
    {
        final ColumnConfig columnConfig = createColumnConfig(code, title, width);
        columnConfig.setSortable(true);
        columnConfig.setRenderer(LinkRenderer.LINK_RENDERER);
        return columnConfig;
    }

    protected final ColumnConfig createSortableColumnConfig(final String code, final String title, final int width)
    {
        final ColumnConfig columnConfig = createColumnConfig(code, title, width);
        columnConfig.setSortable(true);
        return columnConfig;
    }

    protected final ColumnConfig createColumnConfig(final String code, final String title, final int width)
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setDataIndex(code);
        columnConfig.setHeader(title);
        columnConfig.setWidth(width);
        return columnConfig;
    }
    
    /** Creates an HTML A element for given <var>user</var> representation. */
    protected final static String createUserAnchor(final User user)
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

}
