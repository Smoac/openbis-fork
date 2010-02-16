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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactoryProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * Modules top menu.
 * 
 * @author Piotr Buczek
 */
public class ModulesMenu extends TopMenuItem
{

    public ModulesMenu(IMessageProvider messageProvider,
            IClientPluginFactoryProvider clientPluginFactoryProvider)
    {
        super(messageProvider.getMessage(Dict.MENU_MODULES));

        setId(TopMenu.ID + "_MODULES");
        Menu submenu = new Menu();
        setMenu(submenu);
        hide();
        initialize(clientPluginFactoryProvider.getModules());
    }

    private void initialize(List<IModule> modules)
    {
        ModuleInitializationController.initialize(modules, this);
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

    private static class ModuleInitializationController
    {
        public static void initialize(List<IModule> modules, ModulesMenu modulesMenu)
        {
            ModuleInitializationController controller =
                    new ModuleInitializationController(modules, modulesMenu);
            for (IModule module : modules)
            {
                module.initialize(new ModuleInitializationCallback(controller, module));
            }
        }

        private int remainingModulesCounter;

        private final List<IModule> successfullyInitializedModules = new ArrayList<IModule>();

        private final List<IModule> uninitializedModules = new ArrayList<IModule>();

        private final ModulesMenu modulesMenu;

        private ModuleInitializationController(List<IModule> allModules, ModulesMenu modulesMenu)
        {
            this.modulesMenu = modulesMenu;
            successfullyInitializedModules.addAll(allModules);
            remainingModulesCounter = allModules.size();
        }

        private void onInitializationFailure(Throwable caught, IModule module)
        {
            successfullyInitializedModules.remove(module);
            uninitializedModules.add(module);
            onModuleInitializationComplete();
        }

        private void onInitializationSuccess(IModule module)
        {
            onModuleInitializationComplete();
        }

        private void onModuleInitializationComplete()
        {
            remainingModulesCounter--;
            if (remainingModulesCounter == 0)
            {
                modulesMenu.addModuleItems(successfullyInitializedModules);
                showErrorMessageIfNecessary();
            }
        }

        private void showErrorMessageIfNecessary()
        {
            if (uninitializedModules.size() == 0)
            {
                return;
            }

            final StringBuilder sb = new StringBuilder();
            sb.append("Initialization of these utilities failed: ");
            for (IModule module : uninitializedModules)
            {
                sb.append(module.getName() + ", ");
            }
            sb.setLength(sb.length() - 2);

            MessageBox.alert("Error", sb.toString(), null);
        }

    }

    private static class ModuleInitializationCallback implements AsyncCallback<Void>
    {

        private final ModuleInitializationController manager;

        private final IModule module;

        public ModuleInitializationCallback(ModuleInitializationController manager, IModule module)
        {
            this.manager = manager;
            this.module = module;
        }

        public void onFailure(Throwable caught)
        {
            manager.onInitializationFailure(caught, module);
        }

        public void onSuccess(Void result)
        {
            manager.onInitializationSuccess(module);
        }

    }
}
