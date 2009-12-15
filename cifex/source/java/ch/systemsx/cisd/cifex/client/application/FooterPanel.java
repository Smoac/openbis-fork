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

import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.cifex.client.Configuration;
import ch.systemsx.cisd.cifex.client.application.ui.DefaultLayoutDialog;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;

/**
 * Panel displaying footer information.
 * <p>
 * Administrator email is taken from <code>service.properties</code> file, version from
 * <code>BuildAndEnvironmentInfo</code> class. Other texts to display are taken from
 * <code>IMessageResources.properties</code> file.
 * </p>
 * 
 * @author Izabela Adamczyk
 */
final class FooterPanel extends HorizontalPanel
{

    private final ViewContext viewContext;

    private final Widget disclaimerLink;

    private final Widget documentationLink;

    FooterPanel(final ViewContext context)
    {
        final Configuration configuration = context.getModel().getConfiguration();
        assert configuration != null : "Must not be null reached this point.";
        this.viewContext = context;
        final IMessageResources messageResources = context.getMessageResources();
        final Html poweredBy = new Html(messageResources.getFooterPoweredBy());
        final Html applicationDescription =
                new Html(messageResources.getFooterApplicationDescription());
        final Html contactAdministrator =
                new Html(createContactAdministrator(configuration, messageResources));
        final Html version = new Html(createVersionDiv(configuration));
        disclaimerLink = createDisclaimerLink(messageResources);
        documentationLink = createDocumentationLink(messageResources);
        add(poweredBy);
        add(createSeparator());
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

    private final Widget createDisclaimerLink(final IMessageResources messageResources)
    {
        return getLinkWidget(messageResources.getFooterDisclaimerLinkLabel(), new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    try
                    {
                        new RequestBuilder(RequestBuilder.GET, "disclaimer.html").sendRequest(null,
                                new HTMLRequestCallback(messageResources
                                        .getFooterDisclaimerDialogTitle()));
                    } catch (final RequestException ex)
                    {
                        showErrorMessage(ex);
                    }
                }
            });
    }

    private final Widget createDocumentationLink(final IMessageResources messageResources)
    {
        return getLinkWidget(messageResources.getFooterDocumentationLinkLabel(), new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    try
                    {
                        new RequestBuilder(RequestBuilder.GET, "documentation.html").sendRequest(
                                null, new HTMLRequestCallback(messageResources
                                        .getFooterDocumentationDialogTitle()));
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

    private final static String createContactAdministrator(final Configuration configuration,
            final IMessageResources messageResources)
    {
        return DOMUtils.createEmailAnchor(configuration.getAdministratorEmail(), messageResources
                .getFooterContactAdministrator());
    }

    private final void showErrorMessage(final Throwable ex)
    {
        final String msg;
        final String message = ex.getMessage();
        final IMessageResources messageResources = viewContext.getMessageResources();
        if (StringUtils.isBlank(message))
        {
            msg = messageResources.getExceptionWithoutMessage(ex.getClass().getName());
        } else
        {
            msg = message;
        }
        MessageBox.alert(messageResources.getMessageBoxErrorTitle(), msg, null);
    }

    /**
     * A {@link RequestCallback} that shows a legal disclaimer on success.
     */
    private final class HTMLRequestCallback implements RequestCallback
    {
        private final String panelTitle;

        public HTMLRequestCallback(String title)
        {
            this.panelTitle = title;
        }

        public final void onResponseReceived(final Request request, final Response response)
        {
            final DefaultLayoutDialog layoutDialog =
                    new DefaultLayoutDialog(viewContext.getMessageResources(), this.panelTitle,
                            DefaultLayoutDialog.DEFAULT_WIDTH, DefaultLayoutDialog.DEFAULT_HEIGHT,
                            true, true);
            layoutDialog.addText(response.getText());
            layoutDialog.show();
        }

        public void onError(final Request request, final Throwable exception)
        {
            showErrorMessage(exception);
        }
    }
}
