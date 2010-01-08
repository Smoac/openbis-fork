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

package ch.systemsx.cisd.cifex.client.application.page;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;

import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.grid.GridWidget;
import ch.systemsx.cisd.cifex.client.application.model.AbstractFileGridModel;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public final class MainPageTabPanelGXT extends MainPageTabPanel
{

    private final TabPanel tabPanel;

    private final ShareTabController shareTab;

    private final TabItem shareTabItem;

    private final InboxTabController inboxTab;

    private final TabItem inboxTabItem;

    private final InviteTabController inviteTabOrNull;

    private final TabItem inviteTabItemOrNull;

    private final AdminTabController adminTabOrNull;

    private final TabItem adminTabItemOrNull;

    private final ViewContext context;

    private final List<GridWidget<AbstractFileGridModel>> fileGridWidgets;

    public MainPageTabPanelGXT(ViewContext context)
    {
        this.context = context;
        fileGridWidgets = new ArrayList<GridWidget<AbstractFileGridModel>>(3);
        tabPanel = new TabPanel();

        // The Share tab and Inbox tab are always shown
        shareTab = new ShareTabController(this.context, fileGridWidgets);
        inboxTab = new InboxTabController(this.context, fileGridWidgets);

        shareTabItem =
                createTabItem(context.getMessageResources().getShareViewLinkLabel(), shareTab);
        inboxTabItem =
                createTabItem(context.getMessageResources().getInboxViewLinkLabel(), inboxTab);

        // Only create the invite tab if the user is permanent and not an administrator
        final UserInfoDTO user = context.getModel().getUser();
        if (user.isPermanent() && user.isAdmin() == false)
        {
            inviteTabOrNull = new InviteTabController(this.context, fileGridWidgets);
            inviteTabItemOrNull =
                    createTabItem(context.getMessageResources().getInviteViewLinkLabel(),
                            inviteTabOrNull);
        } else
        {
            inviteTabOrNull = null;
            inviteTabItemOrNull = null;
        }

        // Only create an admin tab if the user can access it
        if (user.isAdmin())
        {
            adminTabOrNull = new AdminTabController(this.context, fileGridWidgets);
            adminTabItemOrNull =
                    createTabItem(context.getMessageResources().getAdminViewLinkLabel(),
                            adminTabOrNull);
        } else
        {
            adminTabOrNull = null;
            adminTabItemOrNull = null;
        }

        initializePanel();

        setWidget(tabPanel);
    }

    @Override
    public void showTab(Tab tab)
    {
        switch (tab)
        {
            case INBOX_TAB:
                tabPanel.setSelection(inboxTabItem);
                break;
            case INVITE_TAB:
                if (inviteTabItemOrNull != null)
                {
                    tabPanel.setSelection(inviteTabItemOrNull);
                }
                break;
            case SHARE_TAB:
                tabPanel.setSelection(shareTabItem);
                break;
            case ADMIN_TAB:
                if (adminTabItemOrNull != null)
                {
                    tabPanel.setSelection(adminTabItemOrNull);
                }
                break;
        }
    }

    private final void initializePanel()
    {
        // Add the tabs
        tabPanel.add(shareTabItem);
        tabPanel.add(inboxTabItem);

        if (context.getModel().getUser().isPermanent() && inviteTabItemOrNull != null)
        {
            tabPanel.add(inviteTabItemOrNull);
        }

        if (context.getModel().getUser().isAdmin() && adminTabOrNull != null)
        {
            tabPanel.add(adminTabItemOrNull);
        }

        tabPanel.ensureDebugId("cifex-tabpanel");
    }

    private TabItem createTabItem(String name, final AbstractMainPageTabController tabController)
    {
        final TabItem tabItem = new TabItem(name);
        tabItem.setClosable(false);
        tabItem.add(tabController.getWidget());
        tabItem.setScrollMode(Scroll.AUTO);
        // WORKAROUND: set height to 95% to ensure also the toolbar of the bottom grid is shown.
        tabItem.setSize("100%", "95%");
        tabItem.addListener(Events.Select, new Listener<TabPanelEvent>()
            {

                public void handleEvent(TabPanelEvent be)
                {
                    context.getPageController().setCurrentPage(tabController.getPageIdentifier());

                }
            });
        return tabItem;
    }
}
