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

import ch.systemsx.cisd.openbis.uitest.page.Page;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeDataType;

public class AddPropertyType extends Page
{

    @FindBy(id = "openbis_property-type-registration_form_code-input")
    private WebElement code;

    @FindBy(id = "openbis_property-type-registration_form_label-input")
    private WebElement label;

    @FindBy(id = "openbis_property-type-registration_form_description-input")
    private WebElement description;

    @FindBys(
        {
                @FindBy(id = "openbis_select_data-type"),
                @FindBy(xpath = "img") })
    private WebElement dataTypeDropDownOpener;

    @FindBy(className = "x-combo-list-item")
    private List<WebElement> dataTypeChoices;

    @FindBys(
        {
                @FindBy(id = "openbis_select_vocabulary-select"),
                @FindBy(xpath = "img") })
    private WebElement vocabularyDropDownOpener;

    @FindBy(className = "x-combo-list-item")
    private List<WebElement> vocabularyChoices;

    @FindBy(id = "openbis_property-type-registration_formsave-button")
    private WebElement saveButton;

    public void fillWith(PropertyType propertyType)
    {
        this.code.sendKeys(propertyType.getCode());
        this.label.sendKeys(propertyType.getLabel());
        this.description.sendKeys(propertyType.getDescription());
        this.dataTypeDropDownOpener.click();
        select(dataTypeChoices, propertyType.getDataType().getName());

        if (propertyType.getDataType().equals(PropertyTypeDataType.CONTROLLED_VOCABULARY))
        {
            this.vocabularyDropDownOpener.click();
            select(vocabularyChoices, propertyType.getVocabulary().getCode());
        }
    }

    public AddPropertyType save()
    {
        this.saveButton.click();
        return get(AddPropertyType.class);
    }
}
