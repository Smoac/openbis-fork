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
import ch.systemsx.cisd.openbis.uitest.page.dialog.AddSampleDialog;

public class SampleBrowser extends BrowserPage
{

    @FindBys(
        {
                @FindBy(id = "openbis_sample-type-browser-grid"),
                @FindBy(xpath = "//*[contains(@class, \"x-grid\") and contains(@class, \"-header \")]") })
    private List<WebElement> columns;

    @FindBys(
        {
                @FindBy(id = "openbis_sample-type-browser-grid"),
                @FindBy(xpath = "//*[contains(@class, \"x-grid\") and contains(@class, \"-col \")]") })
    private List<WebElement> data;

    @FindBy(id = "openbis_sample-browser_main_add-button")
    private WebElement addSampleButton;

    @FindBys(
        {
                @FindBy(id = "openbis_select_group-selectsample-browser-toolbar"),
                @FindBy(xpath = "img") })
    private WebElement sampleTypeList;

    @FindBy(className = "x-combo-list-item")
    private List<WebElement> choices;

    public AddSampleDialog addSample()
    {
        addSampleButton.click();
        return get(AddSampleDialog.class);
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

    public SampleBrowser selectSampleType(String sampleType)
    {
        sampleTypeList.click();
        select(choices, sampleType);
        return get(SampleBrowser.class);
    }

    public List<String> getSampleTypes()
    {
        List<String> sampleTypes = new ArrayList<String>();

        sampleTypeList.click();
        for (WebElement choice : choices)
        {
            sampleTypes.add(choice.getText());
        }
        sampleTypeList.click();

        return sampleTypes;
    }
}
