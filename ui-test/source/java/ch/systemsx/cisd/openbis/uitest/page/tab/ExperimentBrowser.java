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

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

import ch.systemsx.cisd.openbis.uitest.page.BrowserPage;

public class ExperimentBrowser extends BrowserPage
{

    @FindBys(
        {
                @FindBy(id = "openbis_select-project"),
                @FindBy(xpath = "..//span[not(*) and @class='gwt-InlineHTML']") })
    private List<WebElement> spacesAndProjects;

    @FindBy(id = "openbis_experiment-browser_delete-button")
    private WebElement deleteAllButton;

    public ExperimentBrowser space(String spaceCode)
    {
        for (WebElement element : spacesAndProjects)
        {
            if (spaceCode.equalsIgnoreCase(element.getText()))
            {
                element.click();
            }
        }
        return this;
    }

    public void deleteAll()
    {
        this.deleteAllButton.click();
        for (WebElement ellu : this.findElements(deleteAllButton,
                "//*[@id='deletion-confirmation-dialog']//textarea"))
        {
            ellu.sendKeys("reason for deletion");
        }

        List<WebElement> e = new ArrayList<WebElement>(this.findElements(deleteAllButton,
                "//*[@id='deletion-confirmation-dialog']//button[text()='OK' or text()='Yes']"));
        if (e.size() > 0)
        {
            e.get(0).click();
        }
    }

    @Override
    protected WebElement getDeleteButton()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected List<WebElement> getColumns()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected List<WebElement> getData()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
