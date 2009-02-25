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

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.gwtext.client.widgets.MessageBox;

import ch.systemsx.cisd.cifex.client.application.ui.DefaultLayoutDialog;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.client.dto.Configuration;

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

    private static final String SEPARATOR = " - ";

    private final ViewContext viewContext;

    private final Element disclaimerLink;

    private final Element documentationLink;

    FooterPanel(final ViewContext context)
    {
        final Configuration configuration = context.getModel().getConfiguration();
        assert configuration != null : "Must not be null reached this point.";
        this.viewContext = context;
        final IMessageResources messageResources = context.getMessageResources();
        final String poweredBy = messageResources.getFooterPoweredBy();
        final String applicationDescription = messageResources.getFooterApplicationDescription();
        final String contactAdministrator =
                createContactAdministrator(configuration, messageResources);
        final String version = createVersionDiv(configuration);
        disclaimerLink = createDisclaimerLink(messageResources);
        documentationLink = createDocumentationLink(messageResources);
        final HTML html =
                new HTML(poweredBy + SEPARATOR + applicationDescription + SEPARATOR + version
                        + SEPARATOR + contactAdministrator + SEPARATOR
                        + DOM.toString(disclaimerLink) + SEPARATOR
                        + DOM.toString(documentationLink))
                    {

                        //
                        // HTML
                        //

                        public final void onBrowserEvent(final Event event)
                        {
                            super.onBrowserEvent(event);
                            if (DOM.eventGetType(event) == Event.ONCLICK)
                            {
                                final Element target = DOM.eventGetTarget(event);
                                // 'Element.equals' or 'DOM.compare' does not work here...
                                if (target.toString().indexOf(messageResources.getFooterDisclaimerLinkLabel()) > -1)
                                {
                                    try
                                    {
                                        new RequestBuilder(RequestBuilder.GET, "disclaimer.html")
                                                .sendRequest(null, new HTMLRequestCallback(
                                                        messageResources
                                                                .getFooterDisclaimerDialogTitle()));
                                    } catch (final RequestException ex)
                                    {
                                        showErrorMessage(ex);
                                    }
                                } else if (target.toString().indexOf(messageResources.getFooterDocumentationLinkLabel()) > -1)
                                {
                                    try
                                    {
                                        new RequestBuilder(RequestBuilder.GET, "documentation.html")
                                                .sendRequest(null, new HTMLRequestCallback(
                                                        messageResources
                                                                .getFooterDocumentationDialogTitle()));
                                    } catch (final RequestException ex)
                                    {
                                        showErrorMessage(ex);
                                    }
                                }

                            }
                        }
                    };
        setWidth("100%");
        setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        add(html);
    }

    private final static Element createDisclaimerLink(final IMessageResources messageResources)
    {
        final Element element = DOMUtils.createBasicAnchorElement();
        DOM.setInnerHTML(element, messageResources.getFooterDisclaimerLinkLabel());
        return element;
    }

    private final static Element createDocumentationLink(final IMessageResources messageResources)
    {
        final Element element = DOMUtils.createBasicAnchorElement();
        DOM.setInnerHTML(element, messageResources.getFooterDocumentationLinkLabel());
        return element;
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
            msg = messageResources.getExceptionWithoutMessage(GWT.getTypeName(ex));
        } else
        {
            msg = message;
        }
        MessageBox.alert(messageResources.getMessageBoxErrorTitle(), msg);
    }

    //
    // Helper classes
    //

    /**
     * A {@link RequestCallback} that shows a legal disclaimer on success.
     */
    private final class HTMLRequestCallback implements RequestCallback
    {
        private String panelTitle;

        public HTMLRequestCallback(String title)
        {
            this.panelTitle = title;
        }

        //
        // RequestCallback
        //

        public final void onResponseReceived(final Request request, final Response response)
        {
            final DefaultLayoutDialog layoutDialog =
                    new DefaultLayoutDialog(viewContext.getMessageResources(), this.panelTitle,
                            DefaultLayoutDialog.DEFAULT_WIDTH, DefaultLayoutDialog.DEFAULT_HEIGHT,
                            true, true);
            layoutDialog.addContentPanel();
            layoutDialog.show();
            layoutDialog.getContentPanel().setContent(response.getText(), true);
        }

        public void onError(final Request request, final Throwable exception)
        {
            showErrorMessage(exception);
        }
    }
}
