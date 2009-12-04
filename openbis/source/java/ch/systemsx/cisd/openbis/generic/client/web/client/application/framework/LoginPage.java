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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import java.util.Date;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.LoginWidget;

/**
 * The login page.
 * 
 * @author Christian Ribeaud
 */
final class LoginPage extends com.google.gwt.user.client.ui.VerticalPanel
{
    private static final int CELL_SPACING = 20;

    LoginPage(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        setSpacing(CELL_SPACING);
        setWidth("100%");
        this.setHeight("100%");
        setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        final LoginWidget loginWidget = new LoginWidget(viewContext);
        // Encapsulate loginWidget in a dummy panel. Otherwise it will get the alignment of this
        // panel.
        DockPanel loginPanel = new DockPanel();
        loginPanel.add(loginWidget, DockPanel.CENTER);
        Image cisdLogo = viewContext.getImageBundle().getLogo().createImage();
        final Widget footerPanel = createFooter(viewContext);
        final HTML welcomePanel = new HTML(viewContext.getMessage(Dict.WELCOME, new Date()));
        welcomePanel.setStyleName("login-welcome-text");
        final CellPanel northPanel = createNorthPanel();
        northPanel.add(cisdLogo);
        northPanel.add(welcomePanel);
        add(getBannersPage());
        add(northPanel);
        add(loginPanel);
        add(footerPanel);
        this.setCellVerticalAlignment(footerPanel, VerticalPanel.ALIGN_BOTTOM);

    }

    private final static CellPanel createNorthPanel()
    {
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setSpacing(20);
        return horizontalPanel;
    }

    private Widget createFooter(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        HorizontalPanel footer = new HorizontalPanel();
        final HTML html = new HTML("Click <a href='help.html' target='_blank'>here</a> for help.");
        footer.add(html);
        return footer;
    }

    private HTML getBannersPage()
    {
        HTML html = new HtmlPage("loginHeader");
        html.setStyleName("login-header");
        return html;
    }
}
