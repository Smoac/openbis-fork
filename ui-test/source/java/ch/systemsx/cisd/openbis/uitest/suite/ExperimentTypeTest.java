package ch.systemsx.cisd.openbis.uitest.suite;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.type.ExperimentType;

@Test(groups =
    { "login-admin" })
public class ExperimentTypeTest extends SeleniumTest
{

    @Test
    public void newExperimentTypeIsListedInExperimentTypeBrowser() throws Exception
    {
        ExperimentType type = create(anExperimentType());

        assertThat(browserEntryOf(type), exists());
    }

}
