package ch.systemsx.cisd.openbis.uitest;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.infra.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.type.Space;

@Test(groups =
    { "login-admin" })
public class SpaceTest extends SeleniumTest
{

    @Test
    public void newSpaceIsListedInSpaceBrowser() throws Exception
    {
        Space space = new Space();

        openbis.create(space);

        assertThat(spaceBrowser(), lists(space));
    }

}
