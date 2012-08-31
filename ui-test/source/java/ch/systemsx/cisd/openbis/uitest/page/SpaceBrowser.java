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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class SpaceBrowser extends Page
{

    @FindBy(className = "x-grid3-col-CODE")
    private List<WebElement> spaceNames;

    public AddSpaceDialog addSpace()
    {
        WebElement addSpaceButton = findElementWithText("Add Space", By.className("x-btn-text"));
        addSpaceButton.click();
        return get(AddSpaceDialog.class);
    }

    public Collection<String> getSpaces()
    {
        Collection<String> spaces = new HashSet<String>();
        for (WebElement element : spaceNames)
        {
            spaces.add(element.getText());
        }
        return spaces;
    }

}
