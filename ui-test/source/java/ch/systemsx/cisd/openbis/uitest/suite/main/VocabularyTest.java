package ch.systemsx.cisd.openbis.uitest.suite.main;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.infra.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;

@Test(groups =
    { "login-admin" })
public class VocabularyTest extends SeleniumTest
{

    @Test
    public void newVocabularyIsListedInVocabularyBrowser() throws Exception
    {
        Vocabulary vocabulary = create(aVocabulary());

        assertThat(browserEntryOf(vocabulary), exists());
    }

}
