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

import com.google.gwt.user.client.ui.RootPanel;

import ch.systemsx.cisd.cifex.client.dto.User;


/**
 * Controller for creating pages.
 * 
 * @author Franz-Josef Elmer
 */
class PageController implements IPageController
{
    private ViewContext viewContext;

    final void setViewContext(ViewContext viewContext)
    {
        this.viewContext = viewContext;
    }

    /**
     * This method clears <code>RootPanel</code>.
     * <p>
     * Note that this method should be called in a very early stage, before building any new GUI stuff. Otherwise
     * <code>RootPanel.get().clear()</code> may destroy some of the created GUI elements.
     * </p>
     */
    private final void clearRootPanel()
    {
        final RootPanel rootPanel = RootPanel.get();
        rootPanel.clear();
    }
    public final void createLoginPage()
    {
        clearRootPanel();
        final LoginPage loginPage = new LoginPage(viewContext);
        RootPanel.get().add(loginPage);
    }

    public final void createMainPage()
    {
        clearRootPanel();
        MainPage mainPage = new MainPage(viewContext);
        RootPanel.get().add(mainPage);
    }

    public final void createAdminPage()
    {
        clearRootPanel();
        final User user = viewContext.getModel().getUser();
        final AbstractMainPage mainPage;
        if (user.isAdmin())
        {
            mainPage = new AdminMainPage(viewContext);
        } else
        {
            mainPage = new MainPage(viewContext);
        }
        RootPanel.get().add(mainPage);
    }

    public void createEditCurrentUserPage()
    {
        clearRootPanel();
        EditCurrentUserPage editUserPage = new EditCurrentUserPage(viewContext);
        RootPanel.get().add(editUserPage);

    }

}
