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

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import ch.systemsx.cisd.openbis.uitest.page.Page;
import ch.systemsx.cisd.openbis.uitest.page.tab.VocabularyBrowser;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;

public class AddVocabularyDialog extends Page
{

    @FindBy(id = "openbis_vocabulary-registration_formvocabulary_registration_field_set_code-input")
    private WebElement code;

    @FindBy(id = "openbis_vocabulary-registration_formvocabulary_registration_field_set_description-input")
    private WebElement description;

    @FindBy(id = "openbis_vocabulary-registration_formvocabulary_registration_field_set_terms-input")
    private WebElement terms;

    @FindBy(id = "vocabulary_registration_field_set-url-input")
    private WebElement url;

    @FindBy(id = "openbis_vocabulary-registration_formsave-button")
    private WebElement saveButton;

    public void fillWith(Vocabulary vocabulary)
    {
        this.code.sendKeys(vocabulary.getCode());
        this.description.sendKeys(vocabulary.getDescription());
        for (String term : vocabulary.getTerms())
        {
            this.terms.sendKeys(term + ", ");
        }
        this.url.sendKeys(vocabulary.getUrl());
    }

    public VocabularyBrowser save()
    {
        this.saveButton.click();
        return get(VocabularyBrowser.class);
    }
}
