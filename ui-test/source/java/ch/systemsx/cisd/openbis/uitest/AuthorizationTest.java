package ch.systemsx.cisd.openbis.uitest;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.infra.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.infra.User;
import ch.systemsx.cisd.openbis.uitest.page.HomePage;
import ch.systemsx.cisd.openbis.uitest.page.InvalidPasswordDialog;
import ch.systemsx.cisd.openbis.uitest.page.LoginPage;

@Test(groups =
    { "no-login" })
public class AuthorizationTest extends SeleniumTest
{

    @Test
    public void loginFailsWithInvalidUserName() throws Exception
    {
        openbis.login("invalid", User.ADMIN.getPassword());
        get(InvalidPasswordDialog.class).dismiss();
    }

    @Test
    public void loginFailsWithValidUserNameAndInvalidPassword() throws Exception
    {
        openbis.login(User.ADMIN.getName(), "invalid");
        get(InvalidPasswordDialog.class).dismiss();
    }

    @Test
    public void loginSucceedsWithValidCredentials() throws Exception
    {
        openbis.login(User.ADMIN);
        assertThat(browser(), isShowing(HomePage.class));

        openbis.logout();
        assertThat(browser(), isShowing(LoginPage.class));
    }

}
