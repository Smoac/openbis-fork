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

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.VerticalPanel;

import ch.systemsx.cisd.cifex.client.application.page.MainPage;
import ch.systemsx.cisd.cifex.client.application.ui.LoginPanelAutofill;
import ch.systemsx.cisd.cifex.client.application.utils.ImageUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

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
        // WORKAROUND: avoid having a horizontal scrollbar on the login page in Safari by setting
        // the height to 97% rather than 100%
        setHeight("97%");
        setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        // Encapsulate loginWidget in a dummy panel. Otherwise it will get the alignment of this
        // panel.
        DockPanel loginPanel = new DockPanel();
        final LoginPanelAutofill loginPanelAutofill = createLoginPanelAutofill();
        loginPanel.add(loginPanelAutofill, DockPanel.CENTER);

        Image cisdLogo = createImage();
        Anchor logo =
                new Anchor(cisdLogo.getElement().getString(), true, "http://www.cisd.ethz.ch/",
                        "_blank");

        final FooterPanel footerPanel = new FooterPanel(viewContext);
        final HTML welcomePanel = createWelcomePanel();
        final CellPanel northPanel = createNorthPanel();
        northPanel.add(logo);
        northPanel.add(welcomePanel);
        add(getBannersPage());
        add(northPanel);
        add(loginPanel);
        add(new InlineHTML("Or you use CIFEX from the desktop via the CIFEX App for <a href='/CIFEXMacApp.app.zip'>Mac</a> and <a href='/CIFEXApp.zip'>others OS supporting Java</a> or the <a href='/cifex_cli.zip'>CIFEX command line client</a>."));
        add(footerPanel);
        this.setCellVerticalAlignment(footerPanel, HasVerticalAlignment.ALIGN_BOTTOM);
    }

    private final LoginPanelAutofill createLoginPanelAutofill()
    {
        final LoginPanelAutofill loginPanel = LoginPanelAutofill.get(viewContext);
        final Map<String, String> urlParams = viewContext.getModel().getUrlParams();
        final String userCode = urlParams.get(Constants.USERCODE_PARAMETER);
        if (StringUtils.isBlank(userCode) == false)
        {
            loginPanel.getUsernameElement().setValue(userCode);
        }
        return loginPanel;
    }

    private final static CellPanel createNorthPanel()
    {
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setSpacing(20);
        return horizontalPanel;
    }

    private final Image createImage()
    {
        final Image image = ImageUtils.getCIFEXLogoImage();
        image.setTitle(MainPage.CISD_LOGO_TITLE);
        image.setPixelSize(175, 107);
        return image;
    }

    private final HTML createWelcomePanel()
    {
        final HTML html = new HTML("CISD File EXchanger");
        html.setStyleName("cifex-welcome");
        return html;
    }

    private HTML getBannersPage()
    {
        HTML html = new HtmlPage("loginHeader");
        html.setStyleName("cifex-login-header");
        return html;
    }

}
