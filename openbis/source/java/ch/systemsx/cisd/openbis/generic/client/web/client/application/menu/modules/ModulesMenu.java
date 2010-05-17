/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.modules;

import java.util.List;

import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactoryProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModuleInitializationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * Modules top menu.
 * 
 * @author Piotr Buczek
 */
public class ModulesMenu extends TopMenuItem implements IModuleInitializationObserver
{

    public ModulesMenu(IMessageProvider messageProvider,
            IClientPluginFactoryProvider clientPluginFactoryProvider)
    {
        super(messageProvider.getMessage(Dict.MENU_MODULES));

        setId(TopMenu.ID + "_MODULES");
        Menu submenu = new Menu();
        setMenu(submenu);
        hide();
        clientPluginFactoryProvider.registerModuleInitializationObserver(this);
    }

    /**
     * Adds menu items supplied by the specified <var>modules</var> to the this menu and then shows
     * the menu. Additional simplification of the menu structure is done if there is only one item
     * in the submenu.
     */
    void addModuleItems(List<IModule> modules)
    {
        Menu submenu = getMenu();
        for (IModule module : modules)
        {
            for (MenuItem menuItem : module.getMenuItems())
            {
                submenu.add(menuItem);
            }
        }
        simplifyIfNecessary(this);
        if (submenu.getItemCount() > 0)
        {
            show();
        }
    }

    /**
     * If there is only one item in specified <var>topMenu</var> and that item has a sub menu then
     * 'pull up' this one item into the top menu.
     */
    private static void simplifyIfNecessary(TopMenuItem topMenu)
    {
        if (topMenu.getMenu().getItemCount() == 1)
        {
            MenuItem menuItem = (MenuItem) topMenu.getMenu().getItem(0);
            if (menuItem.getSubMenu() != null)
            {
                topMenu.setText(menuItem.getText());
                topMenu.setMenu(menuItem.getSubMenu());
            }
        }
    }

    public void notify(List<IModule> successfullyInitializedModules)
    {
        addModuleItems(successfullyInitializedModules);
    }

}
