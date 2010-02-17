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

package ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module;

import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.IActionMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.QueryModule;

/**
 * Main menu of {@link QueryModule}.
 * 
 * @author Piotr Buczek
 */
public class QueryModuleDatabaseMenuItem extends MenuItem
{
    public static final String ID = GenericConstants.ID_PREFIX;

    public QueryModuleDatabaseMenuItem(IViewContext<IQueryClientServiceAsync> viewContext,
            String databaseLabel)
    {
        super(viewContext.getMessage(Dict.QUERY_DATABASE_MENU_TITLE_TEMPLATE, databaseLabel));

        Menu submenu = new Menu();
        submenu.add(new ActionMenu(ActionMenuKind.RUN_CUSTOM_QUERY, viewContext,
                new CustomQueryExecutionTabItemFactory(viewContext)));
        setSubMenu(submenu);
    }

    public static enum ActionMenuKind implements IActionMenuItem
    {
        RUN_CUSTOM_QUERY;

        public String getMenuId()
        {
            return ID + "_" + this.name();
        }

        public String getMenuText(IMessageProvider messageProvider)
        {
            return messageProvider.getMessage(this.name());
        }

    }

    private final class CustomQueryExecutionTabItemFactory implements ITabItemFactory
    {
        private final IViewContext<IQueryClientServiceAsync> viewContext;

        private CustomQueryExecutionTabItemFactory(
                IViewContext<IQueryClientServiceAsync> viewContext)
        {
            this.viewContext = viewContext;
        }

        public ITabItem create()
        {
            return DefaultTabItem.create("Custom Query Executor", CustomQueryViewer
                    .create(viewContext), viewContext, false);
        }

        public String getId()
        {
            return CustomQueryViewer.ID;
        }

        public HelpPageIdentifier getHelpPageIdentifier()
        {
            return HelpPageIdentifier.createSpecific("Custom Query Executor");
        }
    }

}
