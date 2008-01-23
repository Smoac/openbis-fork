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

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import ch.systemsx.cisd.cifex.client.application.ui.LoginWidget;

/**
 * The login page.
 * 
 * @author Christian Ribeaud
 */
final class LoginPage extends VerticalPanel
{
    private static final int CELL_SPACING = 20;

    LoginPage(final ViewContext context)
    {
        setSpacing(CELL_SPACING);
        setWidth("100%");
        setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        final LoginWidget loginWidget = new LoginWidget(context);
        // Encapsulate loginWidget in a dummy panel. Otherwise it will get the alignment of this panel.
        DockPanel loginPanel = new DockPanel();
        loginPanel.add(loginWidget, DockPanel.CENTER);
        add(loginPanel);
    }
}
