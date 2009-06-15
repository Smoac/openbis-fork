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

package ch.systemsx.cisd.openbis.plugin.demo.client.web.client.application.module;

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;
import ch.systemsx.cisd.openbis.plugin.demo.client.web.client.IDemoClientServiceAsync;

/**
 * Simple demo module.
 * 
 * @author Izabela Adamczyk
 */
public class DemoModule implements IModule
{

    private final IViewContext<IDemoClientServiceAsync> viewContext;

    public DemoModule(final IViewContext<IDemoClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
    }

    private IViewContext<IDemoClientServiceAsync> getViewContext()
    {
        return viewContext;
    }

    public Widget getMenu()
    {
        return new TopMenuItemDemoModuleMenu(getViewContext());
        // Uncomment to see customized top menu .
        // return new CustomizedWidgetDemoModuleMenu(getViewContext());
    }
}
