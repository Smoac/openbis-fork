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

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

import ch.systemsx.cisd.cifex.client.dto.Configuration;

/**
 * Panel displaying footer information. Administrator email is taken from service.properties, version from
 * BuildAndEnvironmentInfo. Other texts to display are taken from IMessageResources.properties.
 * 
 * @author Izabela Adamczyk
 */
final class FooterPanel extends HorizontalPanel
{

    FooterPanel(final ViewContext context)
    {
        final Configuration configuration = context.getModel().getConfiguration();
        assert configuration != null : "Must not be null reached this point.";
        final IMessageResources mr = context.getMessageResources();
        final String poweredBy = mr.getFooterPoweredBy();
        final String contactAdmin = mr.getFooterContactAdministrator(configuration.getAdministratorEmail());
        final String version = "(Version: " + configuration.getSystemVersion() + ")";
        final String footerText = getFooterText(poweredBy, version, contactAdmin, " - ", "cifex-light-div");
        final HTML html = new HTML(footerText);
        setWidth("100%");
        setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        add(html);
    }

    private final String getFooterText(final String poweredBy, final String version, final String contact,
            final String separator, final String versionStyle)
    {

        final String formattedVersion = "<div class=\"" + versionStyle + "\">" + version + "</div>";
        return poweredBy + separator + formattedVersion + separator + contact;
    }

}
