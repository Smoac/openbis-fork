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
import ch.systemsx.cisd.openbis.uitest.type.Project;

public class RegisterProject extends NavigationPage
{

    @FindBy(id = "openbis_project-register_form_code-input")
    private WebElement code;

    @NotAlwaysPresent
    @FindBys(
        {
                @FindBy(id = "openbis_select_group-selectopenbis_project-register_form"),
                @FindBy(xpath = "img") })
    private WebElement spaceList;

    @FindBy(className = "x-combo-list-item")
    private List<WebElement> spaceChoices;

    @FindBy(id = "openbis_project-register_formsave-button")
    private WebElement saveButton;

    public void fillWith(Project project)
    {
        code.sendKeys(project.getCode());
        spaceList.click();
        select(spaceChoices, project.getSpace().getCode());
    }

    public RegisterProject save()
    {
        this.saveButton.click();
        return get(RegisterProject.class);
    }

    @Override
    public String toString()
    {
        return "Register Sample tab";
    }
}
