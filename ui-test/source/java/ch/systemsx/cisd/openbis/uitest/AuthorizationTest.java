package ch.systemsx.cisd.openbis.uitest;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.infra.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.infra.User;
import ch.systemsx.cisd.openbis.uitest.page.HomePage;
import ch.systemsx.cisd.openbis.uitest.page.InvalidPasswordDialog;

public class AuthorizationTest extends SeleniumTest
{

    @Test
    public void loginFailsWithInvalidUserName() throws Exception
    {
        openbis.login("invalid", User.ADMIN.getPassword());
        assertThat(browser(), isShowing(InvalidPasswordDialog.class));
    }

    @Test
    public void loginFailsWithValidUserNameAndInvalidPassword() throws Exception
    {
        openbis.login(User.ADMIN.getName() + "adf", "invalid");
        assertThat(browser(), isShowing(InvalidPasswordDialog.class));
    }

    @Test
    public void loginSucceedsWithValidCredentials() throws Exception
    {
        openbis.login(User.ADMIN);
        assertThat(browser(), isShowing(HomePage.class));
    }

}
