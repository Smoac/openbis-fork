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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.AddPersonDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CheckGroup;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CheckPerson;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CheckRole;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CreateGroup;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CreatePerson;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CreateRole;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.FailureExpectation;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleSetCode;

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
        final String groupCode = TEST_GROUP;
        loginAndGotoTab(CategoriesBuilder.MenuCategoryKind.GROUPS,
                CategoriesBuilder.MenuElementKind.BROWSE);
        remoteConsole.prepare(new CreateGroup(groupCode));
        remoteConsole.prepare(new CheckGroup(groupCode.toUpperCase()));

        launchTest(20000);
    }

    public final void testCreatePerson()
    {
        // This userId must be one of the ones located on 'etc/passwd' (file based authentication).
        final String userId = USER_ID;
        loginAndGotoTab(CategoriesBuilder.MenuCategoryKind.PERSONS,
                CategoriesBuilder.MenuElementKind.BROWSE);
        remoteConsole.prepare(new CreatePerson(userId));
        remoteConsole.prepare(new CheckPerson(userId));

        launchTest(20000);
    }

    public final void testCreateRole()
    {
        loginAndGotoTab(CategoriesBuilder.MenuCategoryKind.ROLES,
                CategoriesBuilder.MenuElementKind.BROWSE);
        remoteConsole.prepare(new CreateRole(TEST_GROUP.toUpperCase(), USER_ID,
                RoleSetCode.OBSERVER.toString()));
        remoteConsole.prepare(new CheckRole(TEST_GROUP.toUpperCase(), USER_ID, RoleSetCode.OBSERVER
                .toString()));

        launchTest(20000);
    }

    /**
     * Tests that authorization annotations of {@link ICommonServer#registerPerson(String, String)}
     * are obeyed.
     */
    public final void testCreatePersonByAnUnauthorizedUser()
    {
        loginAndGotoTab("o",
                "o", CategoriesBuilder.MenuCategoryKind.PERSONS, CategoriesBuilder.MenuElementKind.BROWSE);
        final String userId = "u";
        remoteConsole.prepare(new CreatePerson(userId));
        FailureExpectation failureExpectation =
                new FailureExpectation(AddPersonDialog.RegisterDialogCallback.class)
                        .with("Authorization failure: None of method roles '[INSTANCE.ADMIN]' "
                                + "could be found in roles of user 'o'.");
        remoteConsole.prepare(failureExpectation);

        launchTest(20000);
    }
}
