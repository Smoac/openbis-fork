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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.User;

/**
 * Implements functionality of the top menu.
 * 
 * @author Franz-Josef Elmer
 * @author Izabela Adamczyk
 */
public class TopMenu extends LayoutContainer
{
    public TopMenu(final GenericViewContext viewContext)
    {
        setLayout(new FlowLayout());
        setBorders(true);
        ToolBar toolBar = new ToolBar();
        LabelToolItem userInfoText = new LabelToolItem(createUserInfo(viewContext));
        toolBar.add(userInfoText);
        toolBar.add(new SeparatorToolItem());
        toolBar.add(new LogoutButton(viewContext));

        add(toolBar);
    }

    class LogoutButton extends TextToolItem
    {
        public LogoutButton(final GenericViewContext viewContext)
        {
            super(viewContext.getMessage("header_logoutButtonLabel"));
            setIconStyle("icon-menu-show");
            SelectionListener<ComponentEvent> listener = new SelectionListener<ComponentEvent>()
                {
                    @Override
                    public void componentSelected(ComponentEvent ce)
                    {
                        viewContext.getService().logout(
                                new AbstractAsyncCallback<Void>(viewContext)
                                    {
                                        public void onSuccess(Void result)
                                        {
                                            viewContext.getPageController().reload();
                                        }
                                    });
                    }
                };
            addSelectionListener(listener);
        }
    }

    private String createUserInfo(GenericViewContext viewContext)
    {
        SessionContext sessionContext = viewContext.getModel().getSessionContext();
        User user = sessionContext.getUser();
        String userName = user.getUserName();
        String homeGroup = user.getHomeGroupCode();
        if (homeGroup == null)
        {
            return viewContext.getMessage("header_userWithoutHomegroup", userName);
        }
        return viewContext.getMessage("header_userWithHomegroup", userName, homeGroup);
    }
}
