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

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

import ch.systemsx.cisd.openbis.uitest.page.BrowserPage;
import ch.systemsx.cisd.openbis.uitest.page.dialog.AddExperimentTypeDialog;

public class ExperimentTypeBrowser extends BrowserPage
{

    @FindBy(id = "add-entity-type-ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentTypeGrid")
    private WebElement addButton;

    @FindBy(id = "edit-entity-type-ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentTypeGrid")
    private WebElement editButton;

    @FindBy(id = "delete-entity-type-ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentTypeGrid")
    private WebElement deleteButton;

    @FindBys(
        {
                @FindBy(id = "openbis_experiment-type-browser-grid"),
                @FindBy(xpath = ".//td[not(ancestor::div[contains(@style,'display:none')]) and contains(@class, 'x-grid') and contains(@class, '-header ')]//span[not(*)]") })
    private List<WebElement> columns;

    @FindBys(
        {
                @FindBy(id = "openbis_experiment-type-browser-grid"),
                @FindBy(xpath = ".//td[not(ancestor::div[contains(@style,'display:none')]) and contains(@class, 'x-grid') and contains(@class, '-col ')]//*[not(*)]") })
    private List<WebElement> data;

    public AddExperimentTypeDialog add()
    {
        addButton.click();
        return get(AddExperimentTypeDialog.class);
    }

    @Override
    protected List<WebElement> getColumns()
    {
        return this.columns;
    }

    @Override
    protected List<WebElement> getData()
    {
        return this.data;
    }

    @Override
    protected WebElement getDeleteButton()
    {
        return this.deleteButton;
    }
}