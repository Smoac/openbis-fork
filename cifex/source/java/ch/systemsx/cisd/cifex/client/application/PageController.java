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
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.cifex.client.application.page.HelpDialogController;
import ch.systemsx.cisd.cifex.client.application.page.MainPage;
import ch.systemsx.cisd.cifex.client.application.page.MainPageTabPanelGXT;
import ch.systemsx.cisd.cifex.client.application.page.SettingsDialogController;

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

    private MainPageTabPanelGXT tabPanel;

    private Widget mainPage;

    // Keeps track of whether or not the main page is visible
    private boolean isMainPageShowing;

    private LoginPage loginPage;

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
        isMainPageShowing = false;
    }

    /**
     * Makes sure the main page is visible.
     */
    private final void showMainPage()
    {
        if (isMainPageShowing)
            return;

        clearRootPanel();
        tabPanel = new MainPageTabPanelGXT(viewContext);
        mainPage = new MainPage(viewContext, tabPanel);
        isMainPageShowing = true;
        RootPanel.get().add(mainPage);
    }

    //
    // IPageController
    //
    public final void showLoginPage()
    {
        clearRootPanel();

        // lazy initialize the login page
        if (null == loginPage)
            loginPage = new LoginPage(viewContext);
        setCurrentPage(Page.LOGIN_PAGE);
        RootPanel.get().add(loginPage);
    }

    public final void showAdminPage()
    {
        showMainPage();
        tabPanel.showTab(MainPageTabPanelGXT.Tab.ADMIN_TAB);
        setCurrentPage(Page.ADMIN_PAGE);
    }

    public final void showInboxPage()
    {
        showMainPage();
        tabPanel.showTab(MainPageTabPanelGXT.Tab.INBOX_TAB);
        setCurrentPage(Page.INBOX_PAGE);
    }

    public final void showSharePage()
    {
        showMainPage();
        tabPanel.showTab(MainPageTabPanelGXT.Tab.SHARE_TAB);
        setCurrentPage(Page.SHARE_PAGE);
    }

    public final void showInvitePage()
    {
        showMainPage();
        tabPanel.showTab(MainPageTabPanelGXT.Tab.INVITE_TAB);
        setCurrentPage(Page.INVITE_PAGE);
    }

    public final void showHelpPage()
    {
        HelpDialogController helpDialog = new HelpDialogController(viewContext);
        helpDialog.getDialog().show();
    }

    public final void showEditCurrentUserPage()
    {
        SettingsDialogController settingsDialog = new SettingsDialogController(viewContext);
        settingsDialog.getDialog().show();
    }

    public final void refreshMainPage()
    {
        clearRootPanel();
        showPage(currentPage);
    }

    /**
     * Creates the given Page to the root panel. If the page is not known, it creates the main page.
     */
    public final void showPage(final Page page)
    {
        if (page == Page.ADMIN_PAGE)
        {
            showAdminPage();
        } else if (page == Page.LOGIN_PAGE)
        {
            showLoginPage();
        } else if (page == Page.INBOX_PAGE)
        {
            showInboxPage();
        } else if (page == Page.SHARE_PAGE)
        {
            showSharePage();
        } else if (page == Page.INVITE_PAGE)
        {
            showInvitePage();
        } else
        {
            showSharePage();
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
        } else if (currentPage != page)
        {
            previousPage = currentPage;
            currentPage = page;
        }
    }
}
