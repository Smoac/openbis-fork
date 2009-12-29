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

package ch.systemsx.cisd.cifex.client.application.page;

import java.util.List;

import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.IHistoryController.Page;
import ch.systemsx.cisd.cifex.client.application.grid.GridWidget;
import ch.systemsx.cisd.cifex.client.application.model.UserGridModel;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * A <code>AbstractAsyncCallback</code> extension to refresh the list of users.
 * 
 * @author Christian Ribeaud
 */
public final class UserGridRefresherCallback extends AbstractAsyncCallback<Void>
{

    private final GridWidget<UserGridModel> userGrid;

    public UserGridRefresherCallback(final ViewContext context, final GridWidget<UserGridModel> userGrid)
    {
        super(context);
        this.userGrid = userGrid;
    }

    //
    // AbstractAsyncCallback
    //

    public final void onSuccess(final Void object)
    {
        final ViewContext viewContext = getViewContext();
        // Only administrators have access to the admin page, so no need to check the currently
        // logged user here.
        if (viewContext.getHistoryController().getCurrentPage() == Page.ADMIN_PAGE)
        {
            viewContext.getCifexService().listUsers(new ListUsersCallback());
        } else
        {
            viewContext.getCifexService().listUsersRegisteredBy(
                    viewContext.getModel().getUser().getUserCode(), new ListUsersCallback());
        }
    }

    //
    // Helper classes
    //

    private final class ListUsersCallback extends AbstractAsyncCallback<List<UserInfoDTO>>
    {

        ListUsersCallback()
        {
            super(UserGridRefresherCallback.this.getViewContext());
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final List<UserInfoDTO> res)
        {
            userGrid.setDataAndRefresh(UserGridModel.convert(getViewContext(), res));
        }
    }
}