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

package ch.systemsx.cisd.openbis.uitest.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import ch.systemsx.cisd.openbis.uitest.infra.Space;

public class AddSpaceDialog extends Page
{

    @FindBy(id = "openbis_dialog-code-field-input")
    private WebElement code;

    @FindBy(id = "openbis_dialog-save-button")
    private WebElement saveButton;

    public SpaceBrowser addSpace(String name, String description)
    {
        this.code.sendKeys(name);
        saveButton.click();
        wait(By.xpath("//div[.=\"" + name.toUpperCase() + "\"]"));
        return get(SpaceBrowser.class);
    }

    public void fillWith(Space space)
    {
        this.code.sendKeys(space.getCode());
    }

    public SpaceBrowser save()
    {
        this.saveButton.click();
        return get(SpaceBrowser.class);
    }
}
