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

package ch.systemsx.cisd.cifex.client.application.page;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.ui.DefaultLayoutDialog;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class HelpDialogController
{
    final ViewContext context;

    final String panelTitle;

    /**
     * @param context
     */
    public HelpDialogController(final ViewContext context)
    {
        this.context = context;
        this.panelTitle = this.context.getMessageResources().getHelpPageTooltipLabel();
    }

    public DefaultLayoutDialog getDialog()
    {
        final DefaultLayoutDialog layoutDialog =
                new DefaultLayoutDialog(context.getMessageResources(), this.panelTitle,
                        DefaultLayoutDialog.DEFAULT_WIDTH, DefaultLayoutDialog.DEFAULT_HEIGHT,
                        true, true);
        layoutDialog.addText(getHelpPageHTMLString());
        return layoutDialog;
    }

    private final String getHelpPageHTMLString()
    {
        IMessageResources messageResources = context.getMessageResources();
        StringBuffer sb = new StringBuffer();
        appendBlockAnchor(sb, "documentation.html", messageResources
                .getFooterDocumentationLinkLabel());
        appendBlockAnchor(sb, "disclaimer.html", messageResources.getFooterDisclaimerLinkLabel());
        appendBlock(sb, DOMUtils.createEmailAnchor(context.getModel().getConfiguration()
                .getAdministratorEmail(), messageResources.getFooterContactAdministrator()));
        sb.append(getApplicationDescriptionHTMLString());

        return sb.toString();
    }

    private final String getApplicationDescriptionHTMLString()
    {
        IMessageResources messageResources = context.getMessageResources();
        final Element versionSpan = DOM.createSpan();
        DOM.setElementAttribute(versionSpan, "class", "cifex-light-div");
        DOM.setInnerText(versionSpan, "(Version: "
                + context.getModel().getConfiguration().getSystemVersion() + ")");

        final Element descDiv = DOM.createDiv();
        StringBuffer sb = new StringBuffer();
        sb.append(messageResources.getFooterPoweredBy());
        sb.append(" ");
        sb.append(messageResources.getFooterApplicationDescription());
        sb.append(" ");
        sb.append(DOM.toString(versionSpan));
        DOM.setInnerHTML(descDiv, sb.toString());

        return DOM.toString(descDiv);
    }

    private final void appendBlockAnchor(StringBuffer sb, String url, String text)
    {
        Element anchor = DOMUtils.createBasicAnchorElement();
        DOM.setElementAttribute(anchor, "target", "_blank");
        DOM.setElementAttribute(anchor, "href", "url");
        DOM.setInnerText(anchor, text);
        appendBlock(sb, DOM.toString(anchor));
    }

    private final void appendBlock(StringBuffer sb, String html)
    {
        sb.append("<div>");
        sb.append(html);
        sb.append("</div>");
    }
}
