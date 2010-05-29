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

import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.*;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.grid.GridWidget;
import ch.systemsx.cisd.cifex.client.application.model.AbstractFileGridModel;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public final class MainPageTabPanel extends TabPanel
{
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

    /**
     * An enum for the various tabs of the tab panel
     */
    public static enum Tab
    {
        SHARE_TAB, INBOX_TAB, INVITE_TAB, ADMIN_TAB
    }

    public MainPageTabPanel(ViewContext context)
    {
        this.context = context;
        fileGridWidgets = new ArrayList<GridWidget<AbstractFileGridModel>>(3);

        // The Share tab and Inbox tab are always shown
        shareTab = new ShareTabController(this.context, fileGridWidgets);
        inboxTab = new InboxTabController(this.context, fileGridWidgets);

        shareTabItem = createTabItem(msg(SHARE_VIEW_LABEL), shareTab);
        inboxTabItem = createTabItem(msg(INBOX_VIEW_LABEL), inboxTab);

        // Only create the invite tab if the user is permanent and not an administrator
        final UserInfoDTO user = context.getModel().getUser();
        if (user.isPermanent() && user.isAdmin() == false)
        {
            inviteTabOrNull = new InviteTabController(this.context, fileGridWidgets);
            inviteTabItemOrNull = createTabItem(msg(INVITE_VIEW_LABEL), inviteTabOrNull);
        } else
        {
            inviteTabOrNull = null;
            inviteTabItemOrNull = null;
        }

        // Only create an admin tab if the user can access it
        if (user.isAdmin())
        {
            adminTabOrNull = new AdminTabController(this.context, fileGridWidgets);
            adminTabItemOrNull = createTabItem(msg(ADMIN_VIEW_LABEL), adminTabOrNull);
        } else
        {
            adminTabOrNull = null;
            adminTabItemOrNull = null;
        }

        initializePanel();
    }

    public void showTab(Tab tab)
    {
        switch (tab)
        {
            case INBOX_TAB:
                setSelection(inboxTabItem);
                break;
            case INVITE_TAB:
                if (inviteTabItemOrNull != null)
                {
                    setSelection(inviteTabItemOrNull);
                }
                break;
            case SHARE_TAB:
                setSelection(shareTabItem);
                break;
            case ADMIN_TAB:
                if (adminTabItemOrNull != null)
                {
                    setSelection(adminTabItemOrNull);
                }
                break;
        }
    }

    private final void initializePanel()
    {
        // Add the tabs
        add(shareTabItem);
        add(inboxTabItem);

        if (context.getModel().getUser().isPermanent() && inviteTabItemOrNull != null)
        {
            add(inviteTabItemOrNull);
        }

        if (context.getModel().getUser().isAdmin() && adminTabOrNull != null)
        {
            add(adminTabItemOrNull);
        }

        ensureDebugId("cifex-tabpanel");
    }

    private TabItem createTabItem(String name, final AbstractMainPageTabController tabController)
    {
        final TabItem tabItem = new TabItem(name);
        tabItem.setLayout(new FitLayout());
        tabItem.setClosable(false);
        tabItem.add(tabController.getWidget());
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
