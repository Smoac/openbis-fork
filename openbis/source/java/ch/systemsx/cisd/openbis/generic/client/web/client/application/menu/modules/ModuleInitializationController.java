/*
 * Copyright 2010 ETH Zuerich, CISD
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
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;

/**
 * This class controls initialization process of all modules and creates a submenu of
 * {@link ModulesMenu} when all initialization is finished.
 * 
 * @author Piotr Buczek
 */
public class ModuleInitializationController
{// FIXME 2010-05-12, IA: handle initialization for detail viewers
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

    private static class ModuleInitializationCallback implements AsyncCallback<Void>
    {

        private final ModuleInitializationController controller;

        private final IModule module;

        public ModuleInitializationCallback(ModuleInitializationController controller,
                IModule module)
        {
            this.controller = controller;
            this.module = module;
        }

        public void onFailure(Throwable caught)
        {
            controller.onInitializationFailure(caught, module);
        }

        public void onSuccess(Void result)
        {
            controller.onInitializationSuccess(module);
        }

    }

}
