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

package ch.systemsx.cisd.cifex.client.application;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;

/**
 * A collection of static methods for creating some useful widgets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class WidgetFactory
{
    /**
     * Create an anchor (link) displaying the given text and invoking the provided handler when
     * clicked.
     * 
     * @param text The text for the link
     * @param handler The click handler to invoke when the link is clicked
     * @return A new widget.
     */
    public static Widget getLinkWidget(final String text, final ClickHandler handler)
    {
        Anchor link = new Anchor();
        link.setText(text);
        link.setStyleName("cifex-a");
        if (handler != null)
        {
            link.addClickHandler(handler);
        }
        return link;
    }

    public static Anchor createClickableHTMLWidget(String title, String tooltip)
    {
        Anchor html = new Anchor(title);
        html.setStyleName("cifex-a");
        html.setTitle(tooltip);
        return html;
    }
}
