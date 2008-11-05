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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CategoriesBuilder;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.Login;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.OpenTab;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CheckGroup;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CheckPerson;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CheckRole;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CreateGroup;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CreatePerson;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CreateRole;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.RoleListBox;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;

/**
 * A {@link AbstractGWTTestCase} extension to test <i>AMC</i>.
 * 
 * @author Franz-Josef Elmer
 */
public class AuthorizationManagementConsolTest extends AbstractGWTTestCase
{

    private static final String USER_ID = "o";

    private static final String TEST_GROUP = "test-group";

    public final void testCreateGroup()
    {
        remoteConsole.prepare(new Login("test", "a"));
        final String groupCode = TEST_GROUP;
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.GROUPS,
                CategoriesBuilder.MENU_ELEMENTS.LIST));
        remoteConsole.prepare(new CreateGroup(groupCode));
        remoteConsole.prepare(new CheckGroup(groupCode.toUpperCase()));
        remoteConsole.finish(20000);

        client.onModuleLoad();
    }

    public final void testCreatePerson()
    {
        remoteConsole.prepare(new Login("test", "a"));
        // This userId must be one of the ones located on 'etc/passwd' (file based authentication).
        final String userId = USER_ID;
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.PERSONS,
                CategoriesBuilder.MENU_ELEMENTS.LIST));
        remoteConsole.prepare(new CreatePerson(userId));
        remoteConsole.prepare(new CheckPerson(userId));
        remoteConsole.finish(20000);

        client.onModuleLoad();
    }

    public final void testCreateRole()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.ROLES,
                CategoriesBuilder.MENU_ELEMENTS.LIST));
        remoteConsole.prepare(new CreateRole(TEST_GROUP.toUpperCase(), USER_ID,
                RoleListBox.OBSERVER));
        remoteConsole.prepare(
                new CheckRole(TEST_GROUP.toUpperCase(), USER_ID, RoleListBox.OBSERVER));
        remoteConsole.finish(20000);

        client.onModuleLoad();
    }
}
