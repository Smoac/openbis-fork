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

import com.extjs.gxt.ui.client.widget.menu.Menu;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.IActionMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.plugin.demo.client.web.client.IDemoClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.demo.client.web.client.application.Dict;

/**
 * Main menu of {@link DemoModule}.
 * 
 * @author Izabela Adamczyk
 */
public class TopMenuItemDemoModuleMenu extends TopMenuItem
{
    public static final String ID = GenericConstants.ID_PREFIX;

    private final class ExperimentStatisticsTabItemFactory implements ITabItemFactory
    {
        private final IViewContext<IDemoClientServiceAsync> viewContext;

        private ExperimentStatisticsTabItemFactory(IViewContext<IDemoClientServiceAsync> viewContext)
        {
            this.viewContext = viewContext;
        }

        public ITabItem create()
        {
            return DefaultTabItem.create(viewContext.getMessage(Dict.STATISTICS_DEMO_TAB_HEADER),
                    StatisticsWidget.create(viewContext), viewContext, false);
        }

        public String getId()
        {
            return StatisticsWidget.ID;
        }
    }

    public static enum ActionMenuKind implements IActionMenuItem
    {
        STATISTICS;

        public String getMenuId()
        {
            return ID + "_" + this.name();
        }

        public String getMenuText(IMessageProvider messageProvider)
        {
            return messageProvider.getMessage(this.name());
        }

    }

    public TopMenuItemDemoModuleMenu(final IViewContext<IDemoClientServiceAsync> viewContext)
    {
        super(viewContext.getMessage(Dict.MODULE_MENU_TITLE));

        Menu menu = new Menu();
        menu.add(new ActionMenu(ActionMenuKind.STATISTICS, viewContext,
                new ExperimentStatisticsTabItemFactory(viewContext)));
        setMenu(menu);
    }
}
