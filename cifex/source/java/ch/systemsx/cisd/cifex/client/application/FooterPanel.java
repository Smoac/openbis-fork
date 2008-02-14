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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.client.dto.Configuration;

/**
 * Panel displaying footer information. Administrator email is taken from service.properties, version from
 * BuildAndEnvironmentInfo. Other texts to display are taken from IMessageResources.properties.
 * 
 * @author Izabela Adamczyk
 */
final class FooterPanel extends HorizontalPanel
{
    private static final String SEPARATOR = " - ";

    FooterPanel(final ViewContext context)
    {
        final Configuration configuration = context.getModel().getConfiguration();
        assert configuration != null : "Must not be null reached this point.";
        final IMessageResources messageResources = context.getMessageResources();
        final String poweredBy = messageResources.getFooterPoweredBy();
        final String applicationDescription = messageResources.getFooterApplicationDescription();
        final String contactAdministrator = createContactAdministrator(configuration, messageResources);
        final String version = createVersionDiv(configuration);
        final HTML html =
                new HTML(poweredBy + SEPARATOR + applicationDescription + SEPARATOR + version + SEPARATOR
                        + contactAdministrator + SEPARATOR + createDisclaimerLink(messageResources));
        setWidth("100%");
        setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        add(html);
    }

    private final static String createDisclaimerLink(final IMessageResources messageResources)
    {
        return DOMUtils.createAnchor(messageResources.getFooterDisclaimerLinkLabel(), "disclaimer.html", "_new");
    }

    private final static String createVersionDiv(final Configuration configuration)
    {
        final Element versionDiv = DOM.createDiv();
        DOM.setElementAttribute(versionDiv, "class", "cifex-light-div");
        DOM.setInnerText(versionDiv, "(Version: " + configuration.getSystemVersion() + ")");
        System.out.println(versionDiv.toString());
        return DOM.toString(versionDiv);
    }

    private final static String createContactAdministrator(final Configuration configuration,
            final IMessageResources messageResources)
    {
        return DOMUtils.createEmailAnchor(configuration.getAdministratorEmail(), messageResources
                .getFooterContactAdministrator());
    }
}
