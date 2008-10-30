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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.Date;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;

/**
 * The login page.
 * 
 * @author Christian Ribeaud
 */
public class LoginPage extends LayoutContainer
{

    public LoginPage(final IViewContext<IGenericClientServiceAsync> viewContext)
    {
        setStyleName("login-page");
        setLayout(new CenterLayout());
        final VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setSpacing(30);
        verticalPanel.setWidth(600);
        verticalPanel.setHorizontalAlign(HorizontalAlignment.CENTER);

        final HorizontalPanel headerPanel = new HorizontalPanel();
        headerPanel.setSpacing(10);
        headerPanel.add(viewContext.getImageBundle().getLogo().createImage());

        final Text welcomeLabel =
                new Text(viewContext.getMessageProvider().getMessage("welcome", new Date()));
        welcomeLabel.setStyleName("login-welcome-text");

        headerPanel.add(welcomeLabel);
        verticalPanel.add(headerPanel);

        verticalPanel.add(new HorizontalPanel());
        verticalPanel.add(new LoginWidget(viewContext));

        add(verticalPanel);

    }
}