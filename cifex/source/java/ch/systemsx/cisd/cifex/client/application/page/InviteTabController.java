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

import java.util.List;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.Model;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.IHistoryController.Page;
import ch.systemsx.cisd.cifex.client.application.grid.GridUtils;
import ch.systemsx.cisd.cifex.client.application.grid.GridWidget;
import ch.systemsx.cisd.cifex.client.application.model.UserGridModel;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * @author Chandrasekhar Ramakrishnan
 */
final class InviteTabController extends AbstractMainPageTabController
{
    InviteTabController(final ViewContext context)
    {
        super(context);
    }

    @Override
    protected final Widget getWidget()
    {
        final LayoutContainer contentPanel = createOutermostWidgetContainer();
        Model model = context.getModel();
        final UserInfoDTO user = model.getUser();
        LayoutContainer createUserPanel = createUserPanel(user.isAdmin(), context);
        LayoutContainer listCreatedUserPanel = createContainer();
        createListCreatedUserPanel(listCreatedUserPanel, context);
        if (user.isPermanent() && user.isAdmin() == false)
        {
            contentPanel.add(createUserPanel);
        }
        contentPanel.add(listCreatedUserPanel);
        return contentPanel;
    }

    protected void onShow()
    {
        context.getPageController().setCurrentPage(Page.INVITE_PAGE);
    }

    static private void createListCreatedUserPanel(LayoutContainer listCreatedUserPanel,
            ViewContext context)
    {
        context.getCifexService().listUsersRegisteredBy(context.getModel().getUser().getUserCode(),
                new CreatedUserAsyncCallback(listCreatedUserPanel, context));
    }

    private static final class CreatedUserAsyncCallback extends
            AbstractAsyncCallback<List<UserInfoDTO>>
    {

        private final LayoutContainer listCreatedUserPanel;

        private final ViewContext context;

        CreatedUserAsyncCallback(LayoutContainer listCreatedUserPanel, ViewContext context)
        {
            super(context);
            this.listCreatedUserPanel = listCreatedUserPanel;
            this.context = context;
        }

        private Widget createUserTable(final List<UserInfoDTO> users)
        {
            GridWidget<UserGridModel> gridWidget = GridUtils.createUserGrid(users, context);
            // Delete user function
            gridWidget.getGrid().addListener(Events.CellClick,
                    new UserActionGridCellListener(context, null, gridWidget));
            return gridWidget.getWidget();
        }

        private IMessageResources getMessageResources()
        {
            return context.getMessageResources();
        }

        public final void onSuccess(final List<UserInfoDTO> result)
        {
            if (result.size() > 0)
            {
                addTitlePart(listCreatedUserPanel, getMessageResources().getOwnUserTitle());
                listCreatedUserPanel.add(createUserTable(result));
                listCreatedUserPanel.layout();
            }
        }
    }
    
    @Override
    protected Page getPageIdentifier()
    {
        return Page.INVITE_PAGE;
    }
}
