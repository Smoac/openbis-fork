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
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentType;
import ch.systemsx.cisd.openbis.uitest.type.Sample;

public class RegisterExperiment extends NavigationPage
{

    @FindBys(
        {
                @FindBy(id = "openbis_select_experiment-typeopenbis_experiment-registration"),
                @FindBy(xpath = "img") })
    private WebElement experimentTypeList;

    @FindBy(className = "x-combo-list-item")
    private List<WebElement> experimentTypeChoices;

    @NotAlwaysPresent
    @FindBy(id = "openbis_generic-experiment-register_formcode-input")
    private WebElement code;

    @NotAlwaysPresent
    @FindBys(
        {
                @FindBy(id = "openbis_select_projectgeneric-experiment-register_form"),
                @FindBy(xpath = "img") })
    private WebElement projectList;

    @FindBy(className = "x-combo-list-item")
    private List<WebElement> projectChoices;

    @NotAlwaysPresent
    @FindBy(id = "generic-experiment-register_form_samples-input")
    private WebElement samples;

    @NotAlwaysPresent
    @FindBy(id = "openbis_generic-experiment-register_formsave-button")
    private WebElement saveButton;

    public void fillWith(Experiment experiment)
    {
        code.sendKeys(experiment.getCode());
        projectList.click();
        select(projectChoices, experiment.getProject().getCode() + " ("
                + experiment.getProject().getSpace().getCode() + ")");

        samples.clear();
        for (Sample sample : experiment.getSamples())
        {
            samples.sendKeys(sample.getCode() + ", ");
        }
    }

    public RegisterExperiment selectExperimentType(ExperimentType experimentType)
    {
        experimentTypeList.click();
        select(experimentTypeChoices, experimentType.getCode());
        return get(RegisterExperiment.class);
    }

    public RegisterExperiment save()
    {
        this.saveButton.click();
        return get(RegisterExperiment.class);
    }

    @Override
    public String toString()
    {
        return "Register Experiment tab";
    }
}
