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

package ch.systemsx.cisd.openbis.uitest.page.common;

import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.page.menu.AdminMenu;
import ch.systemsx.cisd.openbis.uitest.page.menu.BrowseMenu;
import ch.systemsx.cisd.openbis.uitest.page.menu.NewMenu;
import ch.systemsx.cisd.openbis.uitest.page.menu.UserMenu;
import ch.systemsx.cisd.openbis.uitest.page.tab.Trash;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.Text;

/**
 * @author anttil
 */
public abstract class TopBar extends Page
{
    @Locate("admin_menu")
    private Button adminMenu;

    @Locate("user_menu")
    private Button userMenu;

    @Locate("browse_menu")
    private Button browseMenu;

    @Locate("new_menu")
    private Button newMenu;

    @Locate("trash-button")
    private Button trash;

    @SuppressWarnings("unused")
    @Locate("openbis_search-widget_text-field-input")
    private Text searchTextBox;

    public AdminMenu admin()
    {
        adminMenu.click();
        return get(AdminMenu.class);
    }

    public UserMenu user()
    {
        userMenu.click();
        return get(UserMenu.class);
    }

    public BrowseMenu browse()
    {
        browseMenu.click();
        return get(BrowseMenu.class);
    }

    public Trash trash()
    {
        trash.click();
        return get(Trash.class);
    }

    public NewMenu newMenu()
    {
        newMenu.click();
        return get(NewMenu.class);
    }
}
