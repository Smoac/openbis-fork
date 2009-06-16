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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application;

import com.extjs.gxt.ui.client.widget.menu.Menu;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.IActionMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class QueryMenu extends TopMenuItem
{
    public static final String ID = GenericConstants.ID_PREFIX + "-phosphonetx-";
    
    private static enum ActionMenuKind implements IActionMenuItem
    {
        ALL_PROTEINS_OF_AN_EXPERIMENT;

        public String getMenuId()
        {
            return ID + "_" + this.name();
        }

        public String getMenuText(IMessageProvider messageProvider)
        {
            return messageProvider.getMessage(this.name());
        }
    }
    
    private static final class TabItemFactory implements ITabItemFactory
    {
        protected final IViewContext<IPhosphoNetXClientServiceAsync> viewContext;
        private final String id;
        private final String tabLabelKey;
        private final QueryWidget widget;

        TabItemFactory(IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
                String tabLabelKey, QueryWidget widget)
        {
            this.viewContext = viewContext;
            this.tabLabelKey = tabLabelKey;
            this.widget = widget;
            this.id = ID + tabLabelKey;
        }

        public String getId()
        {
            return id;
        }
        
        public ITabItem create()
        {
            String menuItemText = viewContext.getMessage(tabLabelKey);
            DatabaseModificationAwareComponent component =
                new DatabaseModificationAwareComponent(widget, widget);
            return DefaultTabItem.create(menuItemText, component, viewContext, false);
        }
    }
    
    public QueryMenu(IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        super(viewContext.getMessage(Dict.QUERY_MENU_TITLE));
        setIconStyle(TopMenu.ICON_STYLE);
        
        Menu menu = new Menu();
        QueryWidget widget = new ProteinByExperimentBrowserWidget(viewContext);
        TabItemFactory factory =
                new TabItemFactory(viewContext, Dict.QUERY_ALL_PROTEINS_BY_EXPERIMENT, widget);
        ActionMenu actionMenu =
                new ActionMenu(ActionMenuKind.ALL_PROTEINS_OF_AN_EXPERIMENT, viewContext, factory);
        menu.add(actionMenu);
        setMenu(menu);
    }
}
