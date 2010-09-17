/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;

/**
 * Helper methods for creating a panel with welcome information.
 * 
 * @author Piotr Buczek
 */
public class WelcomePanelHelper
{
    private static final String APPLICATION_VIEW_MODE_PAGE_BASE_NAME = "welcomePage";

    private static final String SIMPLE_VIEW_MODE_PAGE_BASE_NAME = "welcomePageSimple";

    public static final Component createWelcomePanel(IViewContext<?> viewContext, String idPrefix)
    {
        final LayoutContainer layoutContainer = new LayoutContainer(new FitLayout());
        layoutContainer.setId(idPrefix + "welcome");
        HtmlPage welcomePage = new HtmlPage(getWelcomePageBaseName(viewContext));
        layoutContainer.add(welcomePage);
        return layoutContainer;
    }

    private static final String getWelcomePageBaseName(IViewContext<?> viewContext)
    {
        return viewContext.isSimpleMode() ? SIMPLE_VIEW_MODE_PAGE_BASE_NAME
                : APPLICATION_VIEW_MODE_PAGE_BASE_NAME;
    }

}
