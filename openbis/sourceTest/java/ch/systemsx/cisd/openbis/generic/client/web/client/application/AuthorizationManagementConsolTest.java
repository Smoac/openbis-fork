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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.AddPersonDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CheckGroupTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CheckPersonTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CheckRoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CreateGroup;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.CreatePerson;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.FillRoleAssignmentForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.OpenRoleAssignmentDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.RoleAssignmentRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.GroupColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.PersonColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.FailureExpectation;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.Row;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleSetCode;

/**
 * A {@link AbstractGWTTestCase} extension to test <i>AMC</i>.
 * 
 * @author Franz-Josef Elmer
 */
public class AuthorizationManagementConsolTest extends AbstractGWTTestCase
{

    private static final String TEST_GROUP = "test-group";

    private static final String ADMINS_GROUP = "admins-group";

    public final void testCreateGroup()
    {
        final String groupCode = TEST_GROUP;
        loginAndInvokeAction(ActionMenuKind.ADMINISTRATION_MENU_MANAGE_GROUPS);

        CreateGroup createGroupCommand = new CreateGroup(groupCode);
        remoteConsole.prepare(createGroupCommand);
        final CheckGroupTable table = new CheckGroupTable();
        table.expectedRow(new Row().withCell(GroupColDefKind.CODE.id(), groupCode.toUpperCase()));
        remoteConsole.prepare(table);

        launchTest(20 * SECOND);
    }

    public final void testCreatePerson()
    {
        // This userId must be one of the ones located on 'etc/passwd' (file based authentication).
        final String userId = TestConstants.USER_ID_O;
        loginAndInvokeAction(ActionMenuKind.AUTHORIZATION_MENU_USERS);

        CreatePerson command = new CreatePerson(userId);
        remoteConsole.prepare(command);
        final CheckPersonTable table = new CheckPersonTable();
        table.expectedRow(new Row().withCell(PersonColDefKind.USER_ID.id(), userId));
        remoteConsole.prepare(table);

        launchTest(20000);
    }

    public final void testCreateRoleAssignment()
    {
        loginAndInvokeAction(ActionMenuKind.AUTHORIZATION_MENU_ROLES);

        remoteConsole.prepare(new OpenRoleAssignmentDialog());
        remoteConsole.prepare(FillRoleAssignmentForm.fillPersonRole(TEST_GROUP.toUpperCase(),
                TestConstants.USER_ID_O, RoleSetCode.OBSERVER.toString()));
        final CheckRoleAssignmentTable table = new CheckRoleAssignmentTable();
        table.expectedRow(RoleAssignmentRow.personRoleRow(TEST_GROUP.toUpperCase(),
                TestConstants.USER_ID_O, RoleSetCode.OBSERVER.toString()));
        remoteConsole.prepare(table);

        launchTest(DEFAULT_TIMEOUT);
    }

    public final void testCreateAuthorizationGroupRoleAssignment()
    {
        loginAndInvokeAction(ActionMenuKind.AUTHORIZATION_MENU_ROLES);

        remoteConsole.prepare(new OpenRoleAssignmentDialog());
        remoteConsole.prepare(FillRoleAssignmentForm.fillAuthorizationGroupRole(TEST_GROUP
                .toUpperCase(), TestConstants.ADMINS_GROUP, RoleSetCode.OBSERVER.toString()));
        final CheckRoleAssignmentTable table = new CheckRoleAssignmentTable();
        table.expectedRow(RoleAssignmentRow.authorizationGroupRoleRow(TEST_GROUP.toUpperCase(),
                ADMINS_GROUP, RoleSetCode.OBSERVER.toString()));
        remoteConsole.prepare(table);

        launchTest(DEFAULT_TIMEOUT);
    }

    /**
     * Tests that authorization annotations of {@link ICommonServer#registerPerson(String, String)}
     * are obeyed.
     */
    public final void testCreatePersonByAnUnauthorizedUser()
    {
        loginAndInvokeAction("o", "o", ActionMenuKind.AUTHORIZATION_MENU_USERS);
        final String userId = "u";
        CreatePerson command = new CreatePerson(userId);
        remoteConsole.prepare(command);
        FailureExpectation failureExpectation =
                new FailureExpectation(AddPersonDialog.SaveDialogCallback.class)
                        .with("Authorization failure: None of method roles '[INSTANCE.ADMIN]' "
                                + "could be found in roles of user 'o'.");
        remoteConsole.prepare(failureExpectation);

        launchTest(DEFAULT_TIMEOUT);
    }

}
