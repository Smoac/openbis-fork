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

import com.google.gwt.user.client.ui.DecoratedTabPanel;

import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public final class MainPageTabPanelGWT extends MainPageTabPanel
{

    private final DecoratedTabPanel tabPanel;

    private final ShareTabController shareTab;

    private final InboxTabController inboxTab;

    private final InviteTabController inviteTabOrNull;

    private final AdminTabController adminTabOrNull;

    private final ViewContext context;

    public MainPageTabPanelGWT(ViewContext context)
    {
        this.context = context;
        tabPanel = new DecoratedTabPanel();
        // Always create a share tab and an inbox tab
        shareTab = new ShareTabController(this.context);
        inboxTab = new InboxTabController(this.context);

        final UserInfoDTO user = context.getModel().getUser();
        // Only create the invite tab if the user is permanent and not an administrator
        if (user.isPermanent() && user.isAdmin() == false)
        {
            inviteTabOrNull = new InviteTabController(this.context);
        } else
        {
            inviteTabOrNull = null;
        }

        // Only create an admin tab if the user can access it
        adminTabOrNull = (user.isAdmin()) ? new AdminTabController(this.context) : null;

        initializePanel();
        setWidget(tabPanel);
    }

    @Override
    public void showTab(Tab tab)
    {
        switch (tab)
        {
            case INBOX_TAB:
                tabPanel.selectTab(1);
                break;
            case INVITE_TAB:
                if (inviteTabOrNull != null)
                    tabPanel.selectTab(2);
                break;
            case SHARE_TAB:
                tabPanel.selectTab(0);
                break;
            case ADMIN_TAB:
                if (adminTabOrNull != null)
                    tabPanel.selectTab(3);
                break;
        }
    }

    private final void initializePanel()
    {
        tabPanel.setAnimationEnabled(true);

        // Add the tabs
        tabPanel.add(shareTab.getWidget(), context.getMessageResources().getShareViewLinkLabel());
        tabPanel.add(inboxTab.getWidget(), context.getMessageResources().getInboxViewLinkLabel());

        if (context.getModel().getUser().isPermanent() && inviteTabOrNull != null)
            tabPanel.add(inviteTabOrNull.getWidget(), context.getMessageResources()
                    .getInviteViewLinkLabel());

        if (context.getModel().getUser().isAdmin() && adminTabOrNull != null)
        {
            tabPanel.add(adminTabOrNull.getWidget(), context.getMessageResources()
                    .getAdminViewLinkLabel());
        }

        // Select an initial tab
        tabPanel.selectTab(0);
        tabPanel.ensureDebugId("cifex-tabpanel");
    }
}
