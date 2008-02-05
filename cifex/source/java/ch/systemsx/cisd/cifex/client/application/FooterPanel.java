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
import com.google.gwt.user.client.ui.HorizontalPanel;

import ch.systemsx.cisd.cifex.client.dto.FooterData;

/**
 * Panel displaying footer information. Administrator email is taken from service.properties, version from
 * BuildAndEnvironmentInfo. Other texts to display are taken from IMessageResources.properties.
 * 
 * @author Izabela Adamczyk
 */
public class FooterPanel extends HorizontalPanel
{
    ViewContext context;

    FooterPanel(final ViewContext context)
    {
        this.context = context;
        context.getCifexService().getFooterData(new FooterAsyncCallback(this));
    }

    private class FooterAsyncCallback extends AbstractAsyncCallback
    {
        final FooterPanel panel;

        public FooterAsyncCallback(FooterPanel panel)
        {
            super(context);
            this.panel = panel;
        }

        public final void onSuccess(final Object result)
        {
            FooterData footerData = (FooterData) result;
            IMessageResources mr = context.getMessageResources();
            String poweredBy = mr.getFooterPoweredBy();
            String contactAdmin = mr.getFooterContactAdministrator(footerData.getAdministratorEmail());
            String version = "(Version: " + footerData.getSystemVesrion() + ")";
            String footerText = getFooterText(poweredBy, version, contactAdmin, " - ", "cifex-light-div");
            HTML html = new HTML(footerText);
            panel.setWidth("100%");
            panel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
            panel.add(html);
        }

        private String getFooterText(String poweredBy, String version, String contact, String separator,
                String versionStyle)
        {

            final String formattedVersion = "<div class=\"" + versionStyle + "\">" + version + "</div>";
            return poweredBy + separator + formattedVersion + separator + contact;
        }
    }
}
