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

package ch.systemsx.cisd.openbis.uitest.page.menu;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import ch.systemsx.cisd.openbis.uitest.page.Page;
import ch.systemsx.cisd.openbis.uitest.page.tab.RegisterExperiment;
import ch.systemsx.cisd.openbis.uitest.page.tab.RegisterProject;
import ch.systemsx.cisd.openbis.uitest.page.tab.RegisterSample;

public class NewMenu extends Page
{

    @FindBy(id = "openbis_top-menu_SAMPLE_MENU_NEW")
    private WebElement sample;

    @FindBy(id = "openbis_top-menu_PROJECT_MENU_NEW")
    private WebElement project;

    @FindBy(id = "openbis_top-menu_EXPERIMENT_MENU_NEW")
    private WebElement experiment;

    public RegisterSample sample()
    {
        sample.click();
        return get(RegisterSample.class);
    }

    public RegisterProject project()
    {
        project.click();
        return get(RegisterProject.class);
    }

    public RegisterExperiment experiment()
    {
        experiment.click();
        return get(RegisterExperiment.class);
    }
}
