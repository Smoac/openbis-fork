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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * {@link MenuItem} with action fired on selection.
 * 
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
public class ActionMenu extends MenuItem
{

    private ActionMenu(final String id, final String text)
    {
        super(text);
        setId(id);
    }

    private ActionMenu(final String id, final String name, final IDelegatedAction action)
    {
        this(id, name);
        addSelectionListener(new SelectionListener<ComponentEvent>()
            {

                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    action.execute();
                }
            });
    }

    public ActionMenu(final IActionMenuItem actionMenu, IMessageProvider messageProvider,
            final IDelegatedAction action)
    {
        this(actionMenu.getMenuId(), actionMenu.getMenuText(messageProvider), action);
    }

    public ActionMenu(final IActionMenuItem actionMenu, IMessageProvider messageProvider,
            final ITabItemFactory tabToOpen)
    {
        this(actionMenu, messageProvider, new IDelegatedAction()
            {
                public void execute()
                {
                    DispatcherHelper.dispatchNaviEvent(tabToOpen);
                }
            });
    }

}