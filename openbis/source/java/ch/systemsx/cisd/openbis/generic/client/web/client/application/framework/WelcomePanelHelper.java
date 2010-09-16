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
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;

/**
 * Helper methods for creating a panel with welcome information.
 * 
 * @author Piotr Buczek
 */
public class WelcomePanelHelper
{
    public static final Component createWelcomePanel(IViewContext<?> viewContext, String idPrefix)
    {
        final LayoutContainer layoutContainer = new LayoutContainer(new CenterLayout());
        layoutContainer.setId(idPrefix + "welcome");
        layoutContainer.addText(createWelcomeText(viewContext));
        return layoutContainer;
    }

    private static final String createWelcomeText(IViewContext<?> viewContext)
    {
        final Element div = DOM.createDiv();
        div.setClassName("intro-tab");
        div.setInnerText(viewContext.getMessage(Dict.WELCOME));
        return div.getString();
    }

}
