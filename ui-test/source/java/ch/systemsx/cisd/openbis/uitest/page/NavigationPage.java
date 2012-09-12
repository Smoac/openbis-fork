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

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import ch.systemsx.cisd.openbis.uitest.page.menu.AdminMenu;
import ch.systemsx.cisd.openbis.uitest.page.menu.BrowseMenu;
import ch.systemsx.cisd.openbis.uitest.page.menu.NewMenu;
import ch.systemsx.cisd.openbis.uitest.page.menu.UserMenu;
import ch.systemsx.cisd.openbis.uitest.page.tab.Trash;

/**
 * @author anttil
 */
public abstract class NavigationPage extends Page
{
    @FindBy(id = "admin_menu")
    private WebElement adminMenuButton;

    @FindBy(id = "user_menu")
    private WebElement userMenuButton;

    @FindBy(id = "browse_menu")
    private WebElement browseMenuButton;

    @FindBy(id = "new_menu")
    private WebElement newMenuButton;

    @FindBy(id = "trash-button")
    private WebElement trash;

    @FindBy(className = "x-tab-strip-close")
    private List<WebElement> tabCloseButtons;

    public AdminMenu admin()
    {
        adminMenuButton.click();
        return get(AdminMenu.class);
    }

    public UserMenu user()
    {
        userMenuButton.click();
        return get(UserMenu.class);
    }

    public BrowseMenu browse()
    {
        browseMenuButton.click();
        return get(BrowseMenu.class);
    }

    public Trash trash()
    {
        trash.click();
        return get(Trash.class);
    }

    public NewMenu newMenu()
    {
        newMenuButton.click();
        return get(NewMenu.class);
    }

    public void closeTabs()
    {
        for (WebElement e : tabCloseButtons)
        {
            if (e.isDisplayed())
            {
                e.click();
            }
        }
    }

}
