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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.Record;
import com.gwtext.client.widgets.LayoutDialog;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.grid.Grid;
import com.gwtext.client.widgets.grid.event.GridCellListenerAdapter;

import ch.systemsx.cisd.cifex.client.application.IHistoryController.Page;
import ch.systemsx.cisd.cifex.client.application.model.UserGridModel;
import ch.systemsx.cisd.cifex.client.application.ui.ModelBasedGrid;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.client.dto.UserInfoDTO;

/**
 * The <code>GridCellListenerAdapter</code> for users grid.
 * <p>
 * This is used on {@link AdminMainPage} and on {@link MainPage}.
 * </p>
 * 
 * @author Christian Ribeaud
 */
final class UserActionGridCellListener extends GridCellListenerAdapter
{

    private final ViewContext viewContext;

    private final ModelBasedGrid fileGrid;

    UserActionGridCellListener(final ViewContext viewContext, final ModelBasedGrid fileGrid)
    {
        this.viewContext = viewContext;
        this.fileGrid = fileGrid;
    }

    private final static String getUserDescription(final Record record)
    {
        final String fullName = record.getAsString(UserGridModel.FULL_NAME);
        final String userCode = record.getAsString(UserGridModel.USER_CODE);
        if (StringUtils.isBlank(fullName))
        {
            return userCode;
        }
        return fullName;
    }

    public final void onCellClick(final Grid grid, final int rowIndex, final int colIndex,
            final EventObject e)
    {
        final IMessageResources messageResources = viewContext.getMessageResources();
        final ModelBasedGrid userGrid = (ModelBasedGrid) grid;
        if (grid.getColumnModel().getDataIndex(colIndex).equals(UserGridModel.ACTION))
        {
            final Record record = grid.getStore().getAt(rowIndex);
            final String userCode = record.getAsString(UserGridModel.USER_CODE);
            final String userDescription = getUserDescription(record);
            final Element element = e.getTarget();
            if (element == null)
            {
                return;
            }
            final String targetId = DOM.getElementAttribute(e.getTarget(), "id");
            // Delete user
            if (Constants.DELETE_ID.equals(targetId))
            {
                assert userCode.equals(viewContext.getModel().getUser().getUserCode()) == false : "An user can not delete himself.";
                MessageBox.confirm(messageResources.getUserDeleteTitle(), messageResources
                        .getUserDeleteConfirmText(userDescription),
                        new MessageBox.ConfirmCallback()
                            {
                                public final void execute(final String btnID)
                                {
                                    if (btnID.equals("yes"))
                                    {
                                        viewContext.getCifexService()
                                                .deleteUser(
                                                        userCode,
                                                        new UserGridRefresherCallback(viewContext,
                                                                userGrid));
                                    }
                                }
                            });
            } else if (Constants.EDIT_ID.equals(targetId))
            {
                if (userCode.equals(viewContext.getModel().getUser().getUserCode()))
                {
                    viewContext.getPageController().createEditCurrentUserPage();
                    return;
                }
                // Edit User
                viewContext.getCifexService().tryFindUserByUserCode(userCode,
                        new FindUserAsyncCallback(viewContext, userGrid));
            } else if (Constants.RENEW_ID.equals(targetId))
            {
                // Renew User
                viewContext.getCifexService().tryFindUserByUserCode(userCode,
                        new RenewUserAsyncCallback(viewContext, userGrid));
            } else if (Constants.CHANGE_USER_CODE_ID.equals(targetId))
            {
                // Change users code
                assert userCode.equals(viewContext.getModel().getUser().getUserCode()) == false : "An user cannot change his own code.";
                MessageBox.prompt(messageResources.getRenamePromptTitle(), userCode,
                        new MessageBox.PromptCallback()
                            {

                                //
                                // MessageBox.PromptCallback
                                //

                                public final void execute(final String btnIDPrompt,
                                        final String userCodeAfterRenaming)
                                {
                                    if (btnIDPrompt.equals("ok")
                                            && StringUtils.isBlank(userCodeAfterRenaming) == false)
                                    {
                                        if (StringUtils.matches(Constants.USER_CODE_REGEX,
                                                userCodeAfterRenaming) == false)
                                        {
                                            MessageBox.alert("Invalid user code",
                                                    Constants.VALID_USER_CODE_DESCRIPTION);
                                        } else
                                        {
                                            MessageBox.confirm(messageResources
                                                    .getRenameConfirmTitle(), messageResources
                                                    .getRenameConfirmText(userCode,
                                                            userCodeAfterRenaming),
                                                    new RenamingConfirmCallback(userCode,
                                                            userCodeAfterRenaming, userGrid));
                                        }
                                    }
                                }

                            });

            }
        }
    }

    //
    // Helper classes
    //

    private final class RenamingConfirmCallback implements MessageBox.ConfirmCallback
    {
        private final String userCode;

        private final String userCodeAfterRenaming;

        private final ModelBasedGrid userGrid;

        private RenamingConfirmCallback(final String userCode, final String userCodeAfterRenaming,
                final ModelBasedGrid userGrid)
        {
            this.userCode = userCode;
            this.userCodeAfterRenaming = userCodeAfterRenaming;
            this.userGrid = userGrid;
        }

        //
        // MessageBox.ConfirmCallback
        //

        public final void execute(final String btnIDConfirm)
        {
            if (btnIDConfirm.equals("yes"))
            {
                viewContext.getCifexService().changeUserCode(userCode, userCodeAfterRenaming,
                        new UsersFilesRefresherCallback(viewContext, userGrid, fileGrid));
            }
        }
    }

    private static final class UsersFilesRefresherCallback extends AbstractAsyncCallback
    {
        private final ModelBasedGrid userGrid;

        private final ModelBasedGrid fileGrid;

        private final ViewContext context;

        public UsersFilesRefresherCallback(final ViewContext context,
                final ModelBasedGrid userGrid, final ModelBasedGrid fileGrid)
        {
            super(context);
            this.userGrid = userGrid;
            this.fileGrid = fileGrid;
            this.context = context;

        }

        //
        // AbstractAsyncCallback
        //

        public void onSuccess(final Object result)
        {

            boolean adminView = false;
            if (context.getHistoryController().getCurrentPage() == Page.ADMIN_PAGE)
            {
                adminView = true;
            }
            new UserGridRefresherCallback(context, userGrid).onSuccess(result);
            if (fileGrid != null)
            {
                new UpdateFileAsyncCallback(fileGrid, context, adminView).onSuccess(result);
            }
        }

    }

    private final class FindUserAsyncCallback extends AbstractAsyncCallback
    {
        private final ModelBasedGrid grid;

        public FindUserAsyncCallback(final ViewContext context, final ModelBasedGrid grid)
        {
            super(context);
            this.grid = grid;
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final Object result)
        {
            final LayoutDialog dialog = new EditUserDialog(viewContext, (UserInfoDTO) result, grid);
            dialog.show(grid.getEl());
        }
    }

    private final class RenewUserAsyncCallback extends AbstractAsyncCallback
    {
        final ModelBasedGrid modelBasedGrid;

        public RenewUserAsyncCallback(final ViewContext context, final ModelBasedGrid modelBasedGrid)
        {
            super(context);
            this.modelBasedGrid = modelBasedGrid;
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final Object result)
        {
            final UserInfoDTO user = (UserInfoDTO) result;
            assert user.isPermanent() == false : "Regular user can not be renewed.";
            user.setExpirationDate(null);
            viewContext.getCifexService().updateUser(user, null, false,
                    new UserGridRefresherCallback(viewContext, modelBasedGrid));
        }
    }

}