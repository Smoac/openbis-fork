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

import ch.systemsx.cisd.cifex.client.application.IHistoryController.Page;

/**
 * An interface to control the creation of (or switching to) current application pages.
 * 
 * @author Christian Ribeaud
 */
public interface IPageController
{
    /**
     * Show the login page
     */
    public void showLoginPage();

    /**
     * Make the inbox tab visible
     */
    public void showInboxPage();

    /**
     * Make the share tab visible
     */
    public void showSharePage();

    /**
     * Make the invite tab visible
     */
    public void showInvitePage();

    /**
     * Show the help page (a dialog window)
     */
    public void showHelpPage();

    /**
     * Make the admin tab visible
     */
    public void showAdminPage();

    /**
     * Show the edit user page (a dialog window)
     */
    public void showEditCurrentUserPage();

    /**
     * Creates page specified by given <var>page</var>.
     */
    public void showPage(final Page page);

    /**
     * Instantiates <code>ExternalAuthenticationPage</code> and adds it to the
     * <code>RootPanel</code>.
     */
    public void createExternalAuthenticationPage();

    /**
     * Refreshes the main page -- this is sometimes necessary after changes have rendered the
     * information on the main page stale.
     */
    public void refreshMainPage();

    /**
     * Sets the current page -- this is used to inform the page controller about tab changes.
     */
    public void setCurrentPage(Page invitePage);

}
