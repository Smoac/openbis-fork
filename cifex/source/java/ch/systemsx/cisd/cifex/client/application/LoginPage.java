/*
 * Copyright 2007 ETH Zuerich, CISD
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

import java.util.Map;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

import ch.systemsx.cisd.cifex.client.application.ui.LoginWidget;
import ch.systemsx.cisd.cifex.client.application.utils.ImageUtils;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;

/**
 * The login page.
 * 
 * @author Christian Ribeaud
 */
final class LoginPage extends VerticalPanel
{
    private static final int CELL_SPACING = 20;

    private final ViewContext viewContext;

    LoginPage(final ViewContext viewContext)
    {
        this.viewContext = viewContext;
        setSpacing(CELL_SPACING);
        setWidth("100%");
        setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        final LoginWidget loginWidget = createLoginWidget();
        // Encapsulate loginWidget in a dummy panel. Otherwise it will get the alignment of this
        // panel.
        DockPanel loginPanel = new DockPanel();
        loginPanel.add(loginWidget, DockPanel.CENTER);
        Image cisdLogo = createImage();
        final FooterPanel footerPanel = new FooterPanel(viewContext);
        final HTML welcomePanel = createWelcomePanel();
        final CellPanel northPanel = createNorthPanel();
        northPanel.add(cisdLogo);
        northPanel.add(welcomePanel);
        add(northPanel);
        add(loginPanel);
        add(footerPanel);
        this.setHeight("100%");
        this.setCellVerticalAlignment(footerPanel, VerticalPanel.ALIGN_BOTTOM);
    }

    private final LoginWidget createLoginWidget()
    {
        final LoginWidget loginWidget = new LoginWidget(viewContext);
        final Map urlParams = viewContext.getModel().getUrlParams();
        final String userCode = (String) urlParams.get(Constants.USERCODE_PARAMETER);
        if (StringUtils.isBlank(userCode) == false)
        {
            loginWidget.getUserField().setValue(userCode);
        }
        return loginWidget;
    }

    private final static CellPanel createNorthPanel()
    {
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setSpacing(20);
        return horizontalPanel;
    }

    private final Image createImage()
    {
        final Image image = ImageUtils.getLogoImage();
        image.setTitle(viewContext.getMessageResources().getCISDLogoTitle());
        return image;
    }

    private final HTML createWelcomePanel()
    {
        final HTML html = new HTML(viewContext.getMessageResources().getLoginWelcomeText());
        html.setStyleName("cifex-font-huge");
        return html;
    }

}
