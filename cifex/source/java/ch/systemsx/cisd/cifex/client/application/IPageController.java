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
     * Instantiates <code>LoginPage</code> and adds it to the <code>RootPanel</code>.
     */
    public void showLoginPage();

    /**
     * Instantiates <code>InboxPage</code> and adds it to the <code>RootPanel</code>.
     */
    public void showInboxPage();

    /**
     * Instantiates <code>SharePage</code> and adds it to the <code>RootPanel</code>.
     */
    public void showSharePage();

    /**
     * Instantiates <code>InvitePage</code> and adds it to the <code>RootPanel</code>.
     */
    public void showInvitePage();

    /**
     * Instantiates <code>InvitePage</code> and adds it to the <code>RootPanel</code>.
     */
    public void showHelpPage();

    /**
     * Instantiates <code>AdminPage</code> and adds it to the <code>RootPanel</code>.
     * <p>
     * Calling this method creates an {@link AdminMainPage} for <i>administrators</i>.
     * </p>
     */
    public void showAdminPage();

    /**
     * Instantiates <code>UpdateUserPage</code> with the given User fields and adds it to the
     * <code>RootPanel</code>.
     */
    public void showEditCurrentUserPage();

    /** Creates page specified by given <var>page</var>. */
    public void showPage(final Page page);

    /**
     * Instantiates <code>ExternalAuthenticationPage</code> and adds it to the
     * <code>RootPanel</code>.
     */
    public void createExternalAuthenticationPage();

}
