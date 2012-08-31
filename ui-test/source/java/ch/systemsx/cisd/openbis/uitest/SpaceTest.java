package ch.systemsx.cisd.openbis.uitest;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.UUID;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.infra.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.page.AddSpaceDialog;
import ch.systemsx.cisd.openbis.uitest.page.HomePage;
import ch.systemsx.cisd.openbis.uitest.page.SpaceBrowser;

public class SpaceTest extends SeleniumTest
{
    @Test
    public void newSpaceIsListedInSpaceBrowser() throws Exception
    {
        String spaceName = "selenium-spacetest-" + UUID.randomUUID();

        HomePage home = loginPage.loginAs("selenium", PWD);
        AddSpaceDialog addSpaceDialog = home.adminMenu().spaces().addSpace();
        SpaceBrowser spaceBrowser = addSpaceDialog.addSpace(spaceName, "description");
        assertThat(spaceBrowser, listsSpace(spaceName));
    }

}
