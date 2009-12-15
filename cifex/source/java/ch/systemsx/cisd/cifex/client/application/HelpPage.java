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

import static ch.systemsx.cisd.cifex.client.application.WidgetFactory.getLinkWidget;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
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
 * @author Chandrasekhar Ramakrishnan
 */
public class HelpPage extends AbstractMainPage
{
    /**
     * @param context
     */
    HelpPage(ViewContext context)
    {
        super(context);
    }

    @Override
    protected final LayoutContainer createMainPanel()
    {
        final Configuration configuration = context.getModel().getConfiguration();
        assert configuration != null : "Must not be null reached this point.";

        LayoutContainer container = new LayoutContainer();
        RowLayout layout = new RowLayout(Orientation.VERTICAL);
        container.setLayout(layout);
        final IMessageResources messageResources = context.getMessageResources();
        final Html contactAdministrator =
                new Html(createContactAdministrator(configuration, messageResources));
        final Widget disclaimerLink = createDisclaimerLink(messageResources);
        final Widget documentationLink = createDocumentationLink(messageResources);
        container.add(documentationLink);
        container.add(disclaimerLink);
        container.add(contactAdministrator);
        container.add(createApplicationDescriptionHTML(messageResources, configuration));

        return container;
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

    private final Html createApplicationDescriptionHTML(final IMessageResources messageResources,
            final Configuration configuration)
    {
        final Element versionSpan = DOM.createSpan();
        DOM.setElementAttribute(versionSpan, "class", "cifex-light-div");
        DOM.setInnerText(versionSpan, "(Version: " + configuration.getSystemVersion() + ")");

        final Element descDiv = DOM.createDiv();
        StringBuffer sb = new StringBuffer();
        sb.append(messageResources.getFooterPoweredBy());
        sb.append(" ");
        sb.append(messageResources.getFooterApplicationDescription());
        sb.append(" ");
        sb.append(DOM.toString(versionSpan));
        DOM.setInnerHTML(descDiv, sb.toString());

        final Html applicationDescription = new Html(DOM.toString(descDiv));
        return applicationDescription;
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
        final IMessageResources messageResources = context.getMessageResources();
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
                    new DefaultLayoutDialog(context.getMessageResources(), this.panelTitle,
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
