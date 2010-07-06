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
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.IHistoryController.Page;
import ch.systemsx.cisd.cifex.client.application.grid.GridUtils;
import ch.systemsx.cisd.cifex.client.application.grid.GridWidget;
import ch.systemsx.cisd.cifex.client.application.model.AbstractFileGridModel;
import ch.systemsx.cisd.cifex.client.application.model.UserGridModel;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * @author Chandrasekhar Ramakrishnan
 */
final class InviteTabController extends AbstractMainPageTabController
{
    InviteTabController(final ViewContext context,
            final List<GridWidget<AbstractFileGridModel>> fileGridWidgets)
    {
        super(context, fileGridWidgets);
    }

    @Override
    protected final Widget getWidget()
    {
        final ContentPanel contentPanel = createOutermostWidgetContainer();
        final UserInfoDTO user = context.getModel().getUser();
        ContentPanel createUserPanel = createUserPanel(user.isAdmin(), context);
        addWidgetRow(contentPanel, createUserPanel);
        createListCreatedUserPanel(contentPanel, context);
        return contentPanel;
    }

    protected void onShow()
    {
        context.getPageController().setCurrentPage(Page.INVITE_PAGE);
    }

    static private void createListCreatedUserPanel(ContentPanel listCreatedUserPanel,
            ViewContext context)
    {
        final GridWidget<UserGridModel> gridWidget =
                GridUtils.createUserGrid(new ArrayList<UserInfoDTO>(), false, context);
        gridWidget.getGrid().getView().setEmptyText(msg(LIST_USERS_LOADING_MSG));
        // Delete user function
        gridWidget.getGrid().addListener(Events.CellClick,
                new UserActionGridCellListener(context, null, gridWidget));
        addTitleRow(listCreatedUserPanel, msg(LIST_OWNUSERS_GRID_TITLE));
        addWidgetRow(listCreatedUserPanel, gridWidget.getWidget());

        context.getCifexService().listUsersOwnedBy(context.getModel().getUser().getID(),
                new CreatedUserAsyncCallback(context, gridWidget));
    }

    private static final class CreatedUserAsyncCallback extends
            AbstractAsyncCallback<List<UserInfoDTO>>
    {

        private final GridWidget<UserGridModel> userGrid;

        CreatedUserAsyncCallback(ViewContext context, GridWidget<UserGridModel> userGrid)
        {
            super(context);
            this.userGrid = userGrid;
        }

        public final void onSuccess(final List<UserInfoDTO> result)
        {
            userGrid.getGrid().getView().setEmptyText(msg(LIST_USERS_EMPTY_MSG));
            userGrid.setDataAndRefresh(UserGridModel.convert(getViewContext(), result));
        }

        @Override
        public void onFailure(Throwable caught)
        {
            userGrid.getGrid().getView().setEmptyText(msg(LIST_USERS_EMPTY_MSG));
            super.onFailure(caught);
        }
    }

    @Override
    protected Page getPageIdentifier()
    {
        return Page.INVITE_PAGE;
    }
}
