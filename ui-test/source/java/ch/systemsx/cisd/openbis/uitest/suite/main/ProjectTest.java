package ch.systemsx.cisd.openbis.uitest.suite.main;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.infra.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.type.Project;

@Test(groups =
    { "login-admin" })
public class ProjectTest extends SeleniumTest
{

    @Test
    public void newProjectIsListedInProjectBrowser() throws Exception
    {
        Project project = create(aProject());

        assertThat(browserEntryOf(project), exists());
    }

}
