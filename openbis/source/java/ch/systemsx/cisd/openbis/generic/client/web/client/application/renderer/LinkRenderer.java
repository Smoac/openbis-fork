/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Hyperlink;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.InlineHyperlink;

/**
 * @author    Franz-Josef Elmer
 * @author Piotr Buczek
 */
public class LinkRenderer
{
    public static GridCellRenderer<BaseEntityModel<?>> createLinkRenderer()
    {
        return new GridCellRenderer<BaseEntityModel<?>>()
        {
            public String render(BaseEntityModel<?> model, String property, ColumnData config,
                    int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store)
            {
                return LinkRenderer.renderAsLinkWithAnchor(model.get(property).toString());
            }
        };
    }
    
    public static GridCellRenderer<BaseEntityModel<?>> createExternalLinkRenderer()
    {
        return new GridCellRenderer<BaseEntityModel<?>>()
            {
                public String render(BaseEntityModel<?> model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store)
                {
                    return LinkRenderer.renderAsLinkWithAnchor(model.get(property).toString(),
                            model.get(property).toString(), true);
                }
            };
    }

    /** renders a div witch looks like an anchor (hand cursor is on div - block) */
    public static String renderAsLink(final String message)
    {
        final Element div = DOM.createDiv();
        div.setInnerHTML(message);
        div.setClassName("link-style");
        return DOM.toString(div);
    }

    /** renders a div with an inline anchor inside (hand cursor is on anchor - inline) */
    public static String renderAsLinkWithAnchor(final String text)
    {
        return renderAsLinkWithAnchor(text, "#", false);
    }

    /** renders a div with an inline anchor inside (hand cursor is on anchor - inline) */
    public static String renderAsLinkWithAnchor(final String text, final String href,
            final boolean openInNewWindow)
    {
        final Element anchor = DOM.createAnchor();
        DOM.setInnerText(anchor, text);

        DOM.setElementProperty(anchor, "href", href);
        if (openInNewWindow)
        {
            DOM.setElementProperty(anchor, "target", "blank");
        }
        return DOM.toString(anchor);
    }

    /**
     * @return {@link Hyperlink} GWT widget that is displayed as a link with given <var>text</var>
     *         and a <var>listener</var> registered on the click event. The link display style is
     *         default (not invalidated).
     */
    public static Hyperlink getLinkWidget(final String text, final ClickListener listener)
    {
        return getLinkWidget(text, listener, false);
    }

    /**
     * @return {@link Hyperlink} GWT widget that is displayed as a link with given <var>text</var>
     *         and a <var>listener</var> registered on the click event. The link display style is
     *         based on <var>invalidate</var> (default style is for false).
     */
    public static Hyperlink getLinkWidget(final String text, final ClickListener listener,
            boolean invalidate)
    {
        Hyperlink link = new InlineHyperlink(text);
        if (listener != null)
        {
            link.addClickListener(listener);
        }
        if (invalidate)
        {
            link.addStyleName("invalid");
        }
        return link;
    }

}
