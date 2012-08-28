package ch.systemsx.cisd.openbis.uitest;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.infra.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.page.HomePage;
import ch.systemsx.cisd.openbis.uitest.page.InvalidPasswordDialog;
import ch.systemsx.cisd.openbis.uitest.page.LoginPage;

public class AuthorizationTest extends SeleniumTest
{
    @Test
    public void loginSucceedsWithValidCredentials() throws Exception
    {
        loginPage.loginAs("selenium", PWD);
        assertThat(browser(), isShowing(HomePage.class));
    }

    @Test
    public void loginFailsWithInvalidUserName() throws Exception
    {
        loginPage.loginAs("invalid", PWD);
        get(InvalidPasswordDialog.class).dismiss();
        assertThat(browser(), isShowing(LoginPage.class));
    }

    @Test
    public void loginFailsWithValidUserNameAndInvalidPassword() throws Exception
    {
        loginPage.loginAs("selenium", "invalid");
        get(InvalidPasswordDialog.class).dismiss();
        assertThat(browser(), isShowing(LoginPage.class));
    }
}
