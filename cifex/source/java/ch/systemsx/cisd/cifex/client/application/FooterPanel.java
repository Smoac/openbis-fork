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

package ch.systemsx.cisd.cifex.client.application;

import static ch.systemsx.cisd.cifex.client.application.WidgetFactory.getLinkWidget;
import static ch.systemsx.cisd.cifex.client.application.utils.InfoDictionary.*;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.*;

import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.cifex.client.Configuration;
import ch.systemsx.cisd.cifex.client.application.page.HelpDialogController;
import ch.systemsx.cisd.cifex.client.application.ui.DefaultLayoutDialog;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;

/**
 * Panel displaying footer information.
 * <p>
 * Administrator / Support email is taken from <code>info-dictionary.js</code> file, version from
 * <code>BuildAndEnvironmentInfo</code> class. Other texts to display are taken from
 * <code>message-dictionary.js</code> file.
 * </p>
 * 
 * @author Izabela Adamczyk
 */
final class FooterPanel extends HorizontalPanel
{

    FooterPanel(final ViewContext context)
    {
        final Configuration configuration = context.getModel().getConfiguration();
        assert configuration != null : "Must not be null reached this point.";
        
        final Widget applicationDescription =
                new Html(HelpDialogController.FOOTER_APPLICATION_DESCRIPTION);
        final Widget contactAdministrator = new Html(createContactAdministrator(configuration));
        final Widget version = new Html(createVersionDiv(configuration));
        final Widget disclaimerLink = createDisclaimerLink();
        final Widget documentationLink = createFAQLink();
        add(applicationDescription);
        add(createSeparator());
        add(version);
        add(createSeparator());
        add(contactAdministrator);
        add(createSeparator());
        add(disclaimerLink);
        add(createSeparator());
        add(documentationLink);
    }

    Html createSeparator()
    {
        return new Html("&nbsp;-&nbsp;");
    }

    private final Widget createDisclaimerLink()
    {
        return getLinkWidget(msg(HELP_DISCLAIMER_LABEL), new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    try
                    {
                        new RequestBuilder(RequestBuilder.GET, HelpDialogController.DISCLAIMER_HTML)
                                .sendRequest(null, new HTMLRequestCallback(
                                        msg(HELP_DISCLAIMER_TITLE)));
                    } catch (final RequestException ex)
                    {
                        showErrorMessage(ex);
                    }
                }
            });
    }

    private final Widget createFAQLink()
    {
        return getLinkWidget(msg(HELP_FAQ_LABEL), new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    try
                    {
                        new RequestBuilder(RequestBuilder.GET, HelpDialogController.FAQ_HTML)
                                .sendRequest(null, new HTMLRequestCallback(msg(HELP_FAQ_TITLE),
                                        DefaultLayoutDialog.DEFAULT_WIDTH * 2,
                                        DefaultLayoutDialog.DEFAULT_HEIGHT * 2));
                    } catch (final RequestException ex)
                    {
                        showErrorMessage(ex);
                    }
                }
            });
    }

    private final static String createVersionDiv(final Configuration configuration)
    {
        final Element versionDiv = DOM.createDiv();
        DOM.setElementAttribute(versionDiv, "class", "cifex-light-div");
        DOM.setInnerText(versionDiv, "(Version: " + configuration.getSystemVersion() + ")");
        return DOM.toString(versionDiv);
    }

    private final static String createContactAdministrator(final Configuration configuration)
    {
        return DOMUtils.createEmailAnchor(info(SUPPORT_EMAIL), msg(HELP_CONTACT_SUPPORT_LABEL));
    }

    private final void showErrorMessage(final Throwable ex)
    {
        final String msg;
        final String message = ex.getMessage();
        if (StringUtils.isBlank(message))
        {
            msg = msg(UNKNOWN_FAILURE_MSG, ex.getClass().getName());
        } else
        {
            msg = message;
        }
        MessageBox.alert(msg(MESSAGE_BOX_ERROR_TITLE), msg, null);
    }

}
