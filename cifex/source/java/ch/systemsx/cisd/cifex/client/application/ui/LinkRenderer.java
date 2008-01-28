/*
 * Copyright 2007 ETH Zuerich, CISD
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

/**
 * A <code>Renderer</code> implementation that create an anchor element with inner text <code>value.toString()</code>.
 * 
 * @author Christian Ribeaud
 */
public class LinkRenderer implements Renderer
{

    /** The unique instance of <code>LinkRenderer</code>. */
    public final static Renderer LINK_RENDERER = new LinkRenderer();

    protected LinkRenderer()
    {
    }

    protected Element createAnchorElement(final Object value)
    {
        final Element anchor = DOM.createAnchor();
        DOM.setElementAttribute(anchor, "href", "javascript:return void;");
        DOM.setElementAttribute(anchor, "class", "lims-a");
        return anchor;
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
        final Element anchor = createAnchorElement(value);
        DOM.setInnerText(anchor, value.toString());
        return DOM.toString(anchor);
    }

}
