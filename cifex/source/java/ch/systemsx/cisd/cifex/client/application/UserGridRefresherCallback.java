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

import ch.systemsx.cisd.cifex.client.application.IHistoryController.Page;
import ch.systemsx.cisd.cifex.client.application.ui.ModelBasedGrid;
import ch.systemsx.cisd.cifex.client.dto.UserInfoDTO;

/**
 * A <code>AbstractAsyncCallback</code> extension to refresh the list of users.
 * 
 * @author Christian Ribeaud
 */
final class UserGridRefresherCallback extends AbstractAsyncCallback
{

    private final ModelBasedGrid userGrid;

    UserGridRefresherCallback(final ViewContext context, final ModelBasedGrid userGrid)
    {
        super(context);
        this.userGrid = userGrid;
    }

    //
    // AbstractAsyncCallback
    //

    public final void onSuccess(final Object object)
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

    private final class ListUsersCallback extends AbstractAsyncCallback
    {

        ListUsersCallback()
        {
            super(UserGridRefresherCallback.this.getViewContext());
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final Object res)
        {
            userGrid.reloadStore((UserInfoDTO[]) res);
        }
    }
}