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

import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.*;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.ui.DefaultLayoutDialog;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;

/**
 * A pop-up dialog showing help information.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class HelpDialogController extends AbstractDialogController
{
    public static final String FOOTER_APPLICATION_DESCRIPTION =
            "<a href=\"" + getInternationalizedLabel(HEADER_WEBPAGE_LINK)
                    + "\" target=\"_blank\">CISD File EXchanger</a>";

    /**
     * @param context
     */
    public HelpDialogController(final ViewContext context)
    {
        super(context, context.getMessageResources().getHelpPageTooltipLabel());
    }

    @Override
    public Dialog getDialog()
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
        appendBlockAnchor(sb, "CIFEX-10.01-User_Manual.pdf", messageResources
                .getHelpDocumentationLinkLabel());
        appendBlockAnchor(sb, "documentation.html", messageResources.getHelpFAQLinkLabel());
        appendBlockAnchor(sb, "disclaimer.html", messageResources.getFooterDisclaimerLinkLabel());
        appendBlockAnchor(sb, "tools.html", messageResources.getHelpToolsLinkLabel());
        appendBlock(sb, DOMUtils.createEmailAnchor(getInternationalizedLabel(SUPPORT_EMAIL),
                getInternationalizedLabel(CONTACT_SUPPORT_LABEL)));
        sb.append(getApplicationDescriptionHTMLString());

        return sb.toString();
    }

    private final String getApplicationDescriptionHTMLString()
    {
        final Element versionSpan = DOM.createSpan();
        DOM.setElementAttribute(versionSpan, "class", "cifex-light-div");
        DOM.setInnerText(versionSpan, "(Version: "
                + context.getModel().getConfiguration().getSystemVersion() + ")");

        final Element descDiv = DOM.createDiv();
        StringBuffer sb = new StringBuffer();
        sb.append("<br/><br/>");
        sb.append(FOOTER_APPLICATION_DESCRIPTION);
        sb.append(" ");
        sb.append(DOM.toString(versionSpan));
        DOM.setInnerHTML(descDiv, sb.toString());

        return DOM.toString(descDiv);
    }

    private final void appendBlockAnchor(StringBuffer sb, String url, String text)
    {
        Element anchor = DOMUtils.createBasicAnchorElement();
        DOM.setElementAttribute(anchor, "target", "_blank");
        DOM.setElementAttribute(anchor, "href", url);
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
