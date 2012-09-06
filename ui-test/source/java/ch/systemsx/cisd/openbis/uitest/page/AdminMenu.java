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

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import ch.systemsx.cisd.openbis.uitest.infra.SeleniumTest;

public class AdminMenu extends PrivatePage
{

    @FindBy(id = "openbis_top-menu_ADMINISTRATION_MENU_MANAGE_GROUPS")
    private WebElement spaces;

    @FindBy(id = "openbis_top-menu_VOCABULARY_MENU_BROWSE")
    private WebElement vocabularies;

    @FindBy(id = "ADMINISTRATION_MENU_MANAGE_TYPES")
    private WebElement types;

    @FindBy(id = "openbis_top-menu_SAMPLE_MENU_TYPES")
    private WebElement sampleTypes;

    @FindBy(id = "ADMINISTRATION_MENU_MANAGE_AUTHORIZATION")
    private WebElement authorization;

    public SpaceBrowser spaces()
    {
        spaces.click();
        return get(SpaceBrowser.class);
    }

    public VocabularyBrowser vocabularies()
    {
        vocabularies.click();
        return get(VocabularyBrowser.class);
    }

    public AdminMenu types()
    {
        Actions builder = new Actions(SeleniumTest.driver);
        builder.moveToElement(types).build().perform();
        return get(AdminMenu.class);
    }

    public SampleTypeBrowser sampleTypes()
    {
        sampleTypes.click();
        return get(SampleTypeBrowser.class);
    }

    public AuthorizationMenu authorization()
    {
        Actions builder = new Actions(SeleniumTest.driver);
        builder.moveToElement(authorization).build().perform();
        return get(AuthorizationMenu.class);
    }
}
