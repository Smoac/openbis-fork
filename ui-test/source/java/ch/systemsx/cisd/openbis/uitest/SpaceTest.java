package ch.systemsx.cisd.openbis.uitest;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.infra.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.infra.Space;
import ch.systemsx.cisd.openbis.uitest.infra.User;
import ch.systemsx.cisd.openbis.uitest.page.SpaceBrowser;

public class SpaceTest extends SeleniumTest
{
    @BeforeClass
    public void login()
    {
        openbis.login(User.ADMIN);
    }

    @Test
    public void newSpaceIsListedInSpaceBrowser() throws Exception
    {
        Space space = new Space();

        openbis.createSpace(space);

        assertThat(SpaceBrowser.class, listsSpace(space));
    }

}
