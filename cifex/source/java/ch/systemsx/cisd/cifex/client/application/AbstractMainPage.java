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

package ch.systemsx.cisd.cifex.client.application;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Ext;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.ButtonConfig;
import com.gwtext.client.widgets.Toolbar;
import com.gwtext.client.widgets.ToolbarButton;
import com.gwtext.client.widgets.ToolbarTextItem;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.layout.BorderLayout;
import com.gwtext.client.widgets.layout.ContentPanel;
import com.gwtext.client.widgets.layout.LayoutRegionConfig;

import ch.systemsx.cisd.cifex.client.application.ui.CreateUserWidget;
import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * @author Franz-Josef Elmer
 */
abstract class AbstractMainPage extends BorderLayout
{
    protected IMessageResources messageResources;

    protected VerticalPanel createUserPanel;

    private final static LayoutRegionConfig createCenterRegion()
    {
        LayoutRegionConfig center = new LayoutRegionConfig();
        center.setTitlebar(false);
        center.setAutoScroll(true);
        return center;
    }

    private final static LayoutRegionConfig createNorthRegion()
    {
        LayoutRegionConfig north = new LayoutRegionConfig();
        north.setSplit(false);
        north.setInitialSize(30);
        north.setTitlebar(false);
        north.setAutoScroll(false);
        return north;
    }

    protected static final Widget createPartTitle(final String text)
    {
        final HTML html = new HTML(text);
        html.setStyleName("cifex-heading");
        return html;
    }

    protected final ViewContext context;

    AbstractMainPage(ViewContext context)
    {
        super("100%", "100%", createNorthRegion(), null, null, null, createCenterRegion());
        this.context = context;
        this.messageResources = context.getMessageResources();
        add(LayoutRegionConfig.NORTH, createToolbarPanel());
        add(LayoutRegionConfig.CENTER, createMainPanel());
    }

    private ContentPanel createToolbarPanel()
    {
        User user = context.getModel().getUser();
        ContentPanel contentPanel = new ContentPanel("cifex-toolbar-panel");
        Toolbar toolbar = new Toolbar(Ext.generateId());
        toolbar.addItem(createUserDescription(user));
        toolbar.addButton(createLogoutButton());
        contentPanel.add(toolbar);
        return contentPanel;
    }

    private ToolbarTextItem createUserDescription(User user)
    {

        StringBuffer buffer = new StringBuffer();
        String userName = user.getUserName();
        if (userName != null)
        {
            buffer.append(userName);
        } else
        {
            buffer.append(user.getEmail());
        }
        buffer.append(" (Status: ");
        if (user.isAdmin())
        {
            buffer.append("administrator");
        } else if (user.isPermanent())
        {
            buffer.append("permanent");
        } else
        {
            buffer.append("temporary account: expiration date: ").append(
                    Constants.defaultDateTimeFormat.format(user.getExpirationDate()));
        }
        buffer.append(")");
        return new ToolbarTextItem(buffer.toString());
    }

    private final ToolbarButton createLogoutButton()
    {
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
                    context.getModel().getUrlParams().clear();
                    context.getPageController().createLoginPage();
                }
            });
        return logoutButton;
    }

    private final Widget createCreateUserWidget(final boolean allowPermanentUsers)
    {
        return new CreateUserWidget(context, allowPermanentUsers);

    }

    final static VerticalPanel createVerticalPanelPart()
    {
        final VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setWidth("100%");
        verticalPanel.setSpacing(5);
        return verticalPanel;
    }

    protected final void createUserPanel(final boolean allowPermanentUsers)
    {
        createUserPanel = createVerticalPanelPart();
        createUserPanel.add(createPartTitle(messageResources.getCreateUserLabel()));
        createUserPanel.add(createCreateUserWidget(allowPermanentUsers));
    }

    protected abstract ContentPanel createMainPanel();
}
