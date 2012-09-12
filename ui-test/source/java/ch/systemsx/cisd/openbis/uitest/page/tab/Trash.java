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

package ch.systemsx.cisd.openbis.uitest.page.tab;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import ch.systemsx.cisd.openbis.uitest.page.NavigationPage;

public class Trash extends NavigationPage
{

    @FindBy(id = "empty-trash-button")
    private WebElement empty;

    public Trash empty()
    {
        this.empty.click();
        findElement(this.empty,
                "//*[@id='deletion-confirmation-dialog']//button[text()='OK' or text()='Yes']")
                .click();

        return get(Trash.class);
    }

    @Override
    public String toString()
    {
        return "Trash";
    }
}
