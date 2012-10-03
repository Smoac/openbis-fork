package ch.systemsx.cisd.openbis.uitest.suite.authentication;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.infra.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.page.dialog.InvalidPasswordDialog;
import ch.systemsx.cisd.openbis.uitest.page.menu.AdminMenu;
import ch.systemsx.cisd.openbis.uitest.page.menu.TopBar;
import ch.systemsx.cisd.openbis.uitest.page.tab.RoleAssignmentBrowser;

public class AuthenticationTest extends SeleniumTest
{

    @Test
    public void loginFailsWithInvalidUserName() throws Exception
    {
        login("invalid", SeleniumTest.ADMIN_PASSWORD);

        assertThat(browser(), displays(InvalidPasswordDialog.class));

        assumePage(InvalidPasswordDialog.class).dismiss();
    }

    @Test
    public void loginFailsWithValidUserNameAndInvalidPassword() throws Exception
    {
        login(SeleniumTest.ADMIN_USER + "bagfa", "invalid");

        assertThat(browser(), displays(InvalidPasswordDialog.class));

        assumePage(InvalidPasswordDialog.class).dismiss();
    }

    @Test
    public void loginSucceedsWithValidCredentials() throws Exception
    {
        login(SeleniumTest.ADMIN_USER, SeleniumTest.ADMIN_PASSWORD);

        assertThat(loggedInAs(), is(SeleniumTest.ADMIN_USER));

        logout();
    }

    @Test
    public void adminCanOpenRoleAssignmentBrowser() throws Exception
    {
        login(SeleniumTest.ADMIN_USER, SeleniumTest.ADMIN_PASSWORD);
        assumePage(TopBar.class).admin();
        assumePage(AdminMenu.class).roles();

        assertThat(browser(), displays(RoleAssignmentBrowser.class));

        logout();
    }
}