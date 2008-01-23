/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.client.application.ui;

import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Ext;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.ButtonConfig;
import com.gwtext.client.widgets.Toolbar;
import com.gwtext.client.widgets.ToolbarButton;
import com.gwtext.client.widgets.ToolbarTextItem;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.layout.ContentPanel;

import ch.systemsx.cisd.cifex.client.application.AsyncCallbackAdapter;
import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ViewContext;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
abstract class AbstractToolbarPanel extends ContentPanel
{
    protected final ViewContext context;
    
    private final Toolbar toolbar;

    AbstractToolbarPanel(ViewContext context)
    {
        super(Ext.generateId());
        this.context = context;
        toolbar = new Toolbar(Ext.generateId());
        toolbar.addItem(createUsernameItem());
        toolbar.addButton(createLogoutButton());
        System.out.println("add " + toolbar);
        add(toolbar);
    }

    private ToolbarTextItem createUsernameItem()
    {
        return new ToolbarTextItem(this.context.getModel().getUser().getEmail());
    }
    
    private final ToolbarButton createLogoutButton()
    {
        final IMessageResources messageResources = context.getMessageResources();
        final ToolbarButton logoutButton = new ToolbarButton(messageResources.getLogoutLinkLabel(), new ButtonConfig()
            {
                {
                    setTooltip(messageResources.getLogoutLinkTooltip());
                }
            });
        logoutButton.addButtonListener(new ButtonListenerAdapter()
            {
                public final void onClick(Button button, EventObject e)
                {
                    context.getCifexService().logout(AsyncCallbackAdapter.EMPTY_ASYNC_CALLBACK);
                }
            });
        return logoutButton;
    }
}
