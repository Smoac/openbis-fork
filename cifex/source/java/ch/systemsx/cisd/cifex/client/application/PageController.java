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

import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * Controller for creating pages.
 * 
 * @author Franz-Josef Elmer
 */
final class PageController implements IPageController, IHistoryController
{
    private ViewContext viewContext;

    private Page currentPage;

    private Page previousPage;

    final void setViewContext(final ViewContext viewContext)
    {
        this.viewContext = viewContext;
    }

    /**
     * This method clears <code>RootPanel</code>.
     * <p>
     * Note that this method should be called in a very early stage, before building any new GUI
     * stuff. Otherwise <code>RootPanel.get().clear()</code> may destroy some of the created GUI
     * elements.
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
        setCurrentPage(Page.LOGIN_PAGE);
        RootPanel.get().add(loginPage);
    }

    public final void createMainPage()
    {
        clearRootPanel();
        final MainPage mainPage = new MainPage(viewContext);
        setCurrentPage(Page.MAIN_PAGE);
        RootPanel.get().add(mainPage);
    }

    public final void createAdminPage()
    {
        clearRootPanel();
        final UserInfoDTO user = viewContext.getModel().getUser();
        final AbstractMainPage mainPage;
        if (user.isAdmin())
        {
            mainPage = new AdminMainPage(viewContext);
            setCurrentPage(Page.ADMIN_PAGE);
        } else
        {
            mainPage = new MainPage(viewContext);
            setCurrentPage(Page.MAIN_PAGE);
        }
        RootPanel.get().add(mainPage);
    }

    public final void createEditCurrentUserPage()
    {
        clearRootPanel();
        final EditCurrentUserPage editUserPage = new EditCurrentUserPage(viewContext);
        setCurrentPage(Page.EDIT_PROFILE);
        RootPanel.get().add(editUserPage);
    }

    /**
     * Creates the given Page to the root panel. If the page is not known, it creates the main page.
     */
    public final void createPage(final Page page)
    {
        if (page == Page.ADMIN_PAGE)
        {
            createAdminPage();
        } else if (page == Page.EDIT_PROFILE)
        {
            createEditCurrentUserPage();
        } else if (page == Page.LOGIN_PAGE)
        {
            createLoginPage();
        } else if (page == Page.EXTERNAL_AUTHENTICATION)
        {
            createExternalAuthenticationPage();
        } else
        {
            createMainPage();
        }
    }

    //
    // IHistoryController
    //

    public final Page getCurrentPage()
    {
        return currentPage;
    }

    public final Page getPreviousPage()
    {
        return previousPage;
    }

    /**
     * Sets the given page to the current page, if it differ from the the current page and moves the
     * current page to the previous page. If the given page is the same as the current page,
     * everything stays as it is.
     */
    public final void setCurrentPage(final Page page)
    {
        if (previousPage == null)
        {
            previousPage = currentPage;
            currentPage = page;

        } else if (previousPage != page)
        {
            previousPage = currentPage;
            currentPage = page;
        }
    }

    public void createExternalAuthenticationPage()
    {
        clearRootPanel();
        final ExternalAuthenticationPage externalAuthenticationPage =
                new ExternalAuthenticationPage(viewContext);
        setCurrentPage(Page.EXTERNAL_AUTHENTICATION);
        RootPanel.get().add(externalAuthenticationPage);
    }
}
