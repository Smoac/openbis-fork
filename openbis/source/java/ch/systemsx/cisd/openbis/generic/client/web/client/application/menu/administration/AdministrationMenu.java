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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.administration;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.menu.Menu;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * Administration top menu.
 * 
 * @author Piotr Buczek
 */
public class AdministrationMenu extends TopMenuItem
{
    public AdministrationMenu(final IViewContext<?> viewContext, ComponentProvider componentProvider)
    {
        super(viewContext.getMessage(Dict.MENU_ADMINISTRATION));

        IMessageProvider messageProvider = viewContext;
        Menu submenu = new Menu();
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.ADMINISTRATION_MENU_MANAGE_GROUPS,
                messageProvider, componentProvider.getGroupBrowser()));
        submenu.add(new ProjectMenu(messageProvider, componentProvider));
        submenu.add(new VocabularyMenu(messageProvider, componentProvider));
        submenu.add(new PropertyTypesMenu(messageProvider, componentProvider));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.DATA_SET_MENU_FILE_FORMATS,
                messageProvider, componentProvider.getFileFormatTypeBrowser()));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.ADMINISTRATION_MENU_SCRIPTS,
                messageProvider, componentProvider.getScriptBrowser()));
        submenu.add(new AuthorizationMenu(messageProvider, componentProvider));
        if (viewContext.isLoggingEnabled())
        {
            submenu.add(new ActionMenu(TopMenu.ActionMenuKind.LOGGING_CONSOLE, messageProvider,
                    componentProvider.getLoggingConsole()));
        }
        setMenu(submenu);
        submenu.addListener(Events.BeforeHide, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    viewContext.log("start hiding menu '" + getText() + "'");
                }
            });
        submenu.addListener(Events.Hide, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    viewContext.log("finished hiding menu '" + getText() + "'");
                }
            });
    }
}
