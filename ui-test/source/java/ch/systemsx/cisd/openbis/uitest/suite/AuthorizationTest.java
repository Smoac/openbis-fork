package ch.systemsx.cisd.openbis.uitest.suite;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.page.dialog.InvalidPasswordDialog;
import ch.systemsx.cisd.openbis.uitest.page.menu.TopBar;
import ch.systemsx.cisd.openbis.uitest.page.tab.LoginPage;
import ch.systemsx.cisd.openbis.uitest.page.tab.RoleAssignmentBrowser;

@Test(groups =
    { "no-login" })
public class AuthorizationTest extends SeleniumTest
{

    @Test
    public void loginFailsWithInvalidUserName() throws Exception
    {
        openbis.login("invalid", SeleniumTest.ADMIN_PASSWORD);
        get(InvalidPasswordDialog.class).dismiss();
    }

    @Test
    public void loginFailsWithValidUserNameAndInvalidPassword() throws Exception
    {
        openbis.login(SeleniumTest.ADMIN_USER + "begfga", "invalid");
        get(InvalidPasswordDialog.class).dismiss();
    }

    @Test
    public void loginSucceedsWithValidCredentials() throws Exception
    {
        openbis.login(SeleniumTest.ADMIN_USER, SeleniumTest.ADMIN_PASSWORD);
        assertThat(browser(), isShowing(TopBar.class));

        openbis.logout();
        assertThat(browser(), isShowing(LoginPage.class));
    }

    @Test
    public void adminCanOpenRoleAssignmentBrowser() throws Exception
    {
        openbis.login(SeleniumTest.ADMIN_USER, SeleniumTest.ADMIN_PASSWORD);
        try
        {
            openbis.browseToRoleAssignmentBrowser();
            assertThat(browser(), isShowing(RoleAssignmentBrowser.class));
        } finally
        {
            openbis.logout();
        }
    }
}
