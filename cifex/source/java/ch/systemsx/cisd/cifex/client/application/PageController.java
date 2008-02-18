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
final class PageController implements IPageController
{
    public final static String MAIN_PAGE = "mainPage";

    public final static String ADMIN_PAGE = "adminPage";

    public final static String LOGIN_PAGE = "loginPage";

    public final static String EDIT_CURRENT_USER_PAGE = "editCurrentUserPage";

    private ViewContext viewContext;

    private String activePage;

    final void setViewContext(final ViewContext viewContext)
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

    //
    // IPageController
    //

    public final void createLoginPage()
    {
        clearRootPanel();
        final LoginPage loginPage = new LoginPage(viewContext);
        activePage = LOGIN_PAGE;
        RootPanel.get().add(loginPage);
    }

    public final void createMainPage()
    {
        clearRootPanel();
        final MainPage mainPage = new MainPage(viewContext);
        activePage = MAIN_PAGE;
        RootPanel.get().add(mainPage);
    }

    public final void createAdminPage()
    {
        clearRootPanel();
        final User user = viewContext.getModel().getUser();
        final AbstractMainPage mainPage;
        if (user.isAdmin())
        {
            activePage = ADMIN_PAGE;
            mainPage = new AdminMainPage(viewContext);
        } else
        {
            activePage = MAIN_PAGE;
            mainPage = new MainPage(viewContext);
        }
        RootPanel.get().add(mainPage);
    }

    public final void createEditCurrentUserPage()
    {
        clearRootPanel();
        final EditCurrentUserPage editUserPage = new EditCurrentUserPage(viewContext);
        activePage = EDIT_CURRENT_USER_PAGE;
        RootPanel.get().add(editUserPage);
    }

    public final String getActivePage()
    {
        return activePage;
    }

}
