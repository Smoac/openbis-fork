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
import ch.systemsx.cisd.openbis.uitest.page.tab.ExperimentTypeBrowser;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentType;

public class AddExperimentTypeDialog extends Page
{

    @FindBy(id = "openbis_dialog-code-field-input")
    private WebElement code;

    @FindBy(id = "openbis_add-type-dialog-description-field-input")
    private WebElement description;

    @FindBy(id = "openbis_dialog-save-button")
    private WebElement saveButton;

    @FindBy(id = "openbis_dialog-cancel-button")
    private WebElement cancelButton;

    public ExperimentTypeBrowser save()
    {
        saveButton.click();
        return get(ExperimentTypeBrowser.class);
    }

    public ExperimentTypeBrowser cancel()
    {
        cancelButton.click();
        return get(ExperimentTypeBrowser.class);
    }

    public void fillWith(ExperimentType experimentType)
    {
        this.code.sendKeys(experimentType.getCode());
        this.description.sendKeys(experimentType.getDescription());
    }

}
