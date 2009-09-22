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

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.google.gwt.user.client.ui.HTML;
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
final class LoginPage extends LayoutContainer
{

    LoginPage(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        setStyleName("login-page");
        setLayout(new CenterLayout());
        final VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setSpacing(30);
        verticalPanel.setWidth(600);
        verticalPanel.setHorizontalAlign(HorizontalAlignment.CENTER);

        verticalPanel.add(getBannersPage());

        final HorizontalPanel headerPanel = new HorizontalPanel();
        headerPanel.setSpacing(10);
        headerPanel.add(viewContext.getImageBundle().getLogo().createImage());

        final Text welcomeLabel = new Text(viewContext.getMessage(Dict.WELCOME, new Date()));
        welcomeLabel.setStyleName("login-welcome-text");

        headerPanel.add(welcomeLabel);
        verticalPanel.add(headerPanel);

        verticalPanel.add(new HorizontalPanel());
        verticalPanel.add(new LoginWidget(viewContext));
        verticalPanel.add(createFooter(viewContext));
        add(verticalPanel);

        layout();
    }

    private Widget createFooter(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        HorizontalPanel footer = new HorizontalPanel();
        footer.setStyleName("login-help");
        final HTML html = new HTML("Click <a href='help.html' target='_blank'>here</a> for help.");
        footer.add(html);
        footer.setVerticalAlign(VerticalAlignment.BOTTOM);
        return footer;
    }

    private HTML getBannersPage()
    {
        HTML html = new HtmlPage("loginHeader");
        html.setStyleName("login-header");
        return html;
    }
}
