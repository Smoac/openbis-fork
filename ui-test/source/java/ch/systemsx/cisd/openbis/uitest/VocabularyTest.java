package ch.systemsx.cisd.openbis.uitest;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.infra.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.page.tab.VocabularyBrowser;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;

@Test(groups =
    { "login-admin" })
public class VocabularyTest extends SeleniumTest
{

    @Test
    public void newVocabularyIsListedInVocabularyBrowser() throws Exception
    {
        Vocabulary vocabulary = new Vocabulary();

        openbis.create(vocabulary);

        assertThat(VocabularyBrowser.class, listsVocabulary(vocabulary));
    }

}
