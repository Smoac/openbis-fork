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

import ch.systemsx.cisd.openbis.uitest.infra.NotAlwaysPresent;
import ch.systemsx.cisd.openbis.uitest.page.NavigationPage;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;

public class AssignSamplePropertyType extends NavigationPage
{

    @FindBys(
        {
                @FindBy(id = "openbis_select_property-typeopenbis_property-type-assignment_SAMPLEproperty_type"),
                @FindBy(xpath = "img") })
    private WebElement propertyTypeDropDownOpener;

    @FindBy(className = "x-combo-list-item")
    private List<WebElement> propertyTypeChoices;

    @FindBys(
        {
                @FindBy(id = "openbis_select_sample-typeopenbis_property-type-assignment_sample_type"),
                @FindBy(xpath = "img") })
    private WebElement sampleTypeDropDownOpener;

    @FindBy(className = "x-combo-list-item")
    private List<WebElement> sampleTypeChoices;

    @FindBys(
        {
                @FindBy(id = "openbis_property-type-assignment_SAMPLEmandatory_checkbox"),
                @FindBy(xpath = "input") })
    private WebElement mandatoryCheckbox;

    @NotAlwaysPresent
    @FindBy(xpath = "//*[starts-with(@id, 'openbis_property-type-assignment_SAMPLEdefault_value') and contains(@id, '-input')]")
    private WebElement initialValue;

    @FindBy(id = "openbis_property-type-assignment_SAMPLEsave-button")
    private WebElement saveButton;

    public void fillWith(PropertyType propertyType, SampleType sampleType, String initialValue)
    {
        this.propertyTypeDropDownOpener.click();
        select(propertyTypeChoices, propertyType.getLabel());

        this.sampleTypeDropDownOpener.click();
        select(sampleTypeChoices, sampleType.getCode());

        checkbox(mandatoryCheckbox, initialValue != null);

        if (initialValue != null)
        {
            this.initialValue.sendKeys(initialValue);
        }
    }

    public AssignSamplePropertyType save()
    {
        this.saveButton.click();
        return get(AssignSamplePropertyType.class);
    }
}
