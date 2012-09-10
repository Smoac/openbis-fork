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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

import ch.systemsx.cisd.openbis.uitest.infra.NotAlwaysPresent;
import ch.systemsx.cisd.openbis.uitest.page.Fragment;
import ch.systemsx.cisd.openbis.uitest.page.NavigationPage;
import ch.systemsx.cisd.openbis.uitest.page.fragment.TermSelector;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeDataType;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;

public class RegisterSample extends NavigationPage
{

    @FindBys(
        {
                @FindBy(id = "openbis_select_sample-typeopenbis_sample-registration"),
                @FindBy(xpath = "img") })
    private WebElement sampleTypeList;

    @FindBy(className = "x-combo-list-item")
    private List<WebElement> sampleTypeChoices;

    @NotAlwaysPresent
    @FindBy(id = "openbis_generic-sample-register_formcode-input")
    private WebElement code;

    @NotAlwaysPresent
    @FindBy(id = "openbis_generic-sample-register_formexperiment-input")
    private WebElement experiment;

    @NotAlwaysPresent
    @FindBys(
        {
                @FindBy(id = "register-sample-space-selection"),
                @FindBy(xpath = "img") })
    private WebElement spaceList;

    @FindBy(className = "x-combo-list-item")
    private List<WebElement> spaceChoices;

    @NotAlwaysPresent
    @FindBy(id = "openbis_generic-sample-register_formsave-button")
    private WebElement saveButton;

    @FindBys(
        {
                @FindBy(xpath = "//div[starts-with(@id, 'openbis_generic-sample-register_form')]"),
                @FindBy(xpath = "../../label") })
    private List<WebElement> labels;

    @FindBys(
        {
                @FindBy(id = "openbis_generic-sample-register_form"),
                @FindBy(xpath = "//form") })
    private WebElement mainForm;

    public void fillWith(Sample sample)
    {
        code.sendKeys(sample.getCode());
        spaceList.click();
        select(spaceChoices, sample.getSpace().getCode());

        Map<PropertyType, Object> properties = sample.getProperties();

        for (PropertyType propertyType : properties.keySet())
        {
            Fragment propertyFragment = getFragment(propertyType);
            propertyFragment.fillWith(properties.get(propertyType));
        }
    }

    private Fragment getFragment(PropertyType propertyType)
    {
        PropertyTypeDataType type = propertyType.getDataType();
        Fragment fragment;
        if (type.equals(PropertyTypeDataType.CONTROLLED_VOCABULARY))
        {
            fragment = get(TermSelector.class);
        } else
        {
            throw new IllegalArgumentException("unknown type " + type);
        }

        fragment.setElement(getPropertyElement(propertyType));
        return fragment;
    }

    private WebElement getPropertyElement(PropertyType type)
    {
        return this.findElement(this.mainForm, "//div[@id='openbis_generic-sample-register_form"
                + type.getCode() + "']");
    }

    public RegisterSample selectSampleType(SampleType sampleType)
    {
        sampleTypeList.click();
        select(sampleTypeChoices, sampleType.getCode());
        return get(RegisterSample.class);
    }

    public Collection<String> getProperties()
    {
        Collection<String> properties = new HashSet<String>();
        for (WebElement label : labels)
        {
            properties.add(label.getText().replace(":", "").replace("*", "").trim());
        }
        return properties;
    }

    public RegisterSample save()
    {
        this.saveButton.click();
        return get(RegisterSample.class);
    }

    @Override
    public String toString()
    {
        return "Register Sample tab with properties " + this.getProperties();
    }
}
