/*
 * Copyright 2012 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.uitest.page.dialog;

import ch.systemsx.cisd.openbis.uitest.infra.Locate;
import ch.systemsx.cisd.openbis.uitest.page.Page;
import ch.systemsx.cisd.openbis.uitest.page.tab.VocabularyBrowser;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.Text;
import ch.systemsx.cisd.openbis.uitest.widget.TextArea;

public class AddVocabularyDialog extends Page
{

    @Locate("openbis_vocabulary-registration_formvocabulary_registration_field_set_code")
    private Text code;

    @Locate("openbis_vocabulary-registration_formvocabulary_registration_field_set_description")
    private TextArea description;

    @Locate("openbis_vocabulary-registration_formvocabulary_registration_field_set_terms")
    private TextArea terms;

    @Locate("vocabulary_registration_field_set-url")
    private Text url;

    @Locate("openbis_vocabulary-registration_formsave-button")
    private Button save;

    public void fillWith(Vocabulary vocabulary)
    {
        code.write(vocabulary.getCode());
        description.write(vocabulary.getDescription());

        terms.clear();
        for (String term : vocabulary.getTerms())
        {
            terms.append(term + ", ");
        }

        url.write(vocabulary.getUrl());
    }

    public VocabularyBrowser save()
    {
        save.click();
        return get(VocabularyBrowser.class);
    }
}
