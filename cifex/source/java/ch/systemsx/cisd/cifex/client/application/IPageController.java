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

/**
 * An interface to control the creation of (or switching to) current application pages.
 * 
 * @author Christian Ribeaud
 */
public interface IPageController
{
    /** Instantiates <code>LoginPage</code> and adds it to the <code>RootPanel</code>. */
    public void createLoginPage();

    /** Instantiates <code>MainPage</code> and adds it to the <code>RootPanel</code>. */
    public void createMainPage();

    /** Instantiates <code>AdminPage</code> and adds it to the <code>RootPanel</code>. */
    public void createAdminPage();

    /** Instantiates <code>UpdateUserPage</code> with the given User fields and adds it to the <code>RootPanel</code>. */
    public void createEditCurrentUserPage();
}
