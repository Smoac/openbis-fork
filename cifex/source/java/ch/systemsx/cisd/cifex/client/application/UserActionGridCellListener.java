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

import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;

import ch.systemsx.cisd.cifex.client.application.IHistoryController.Page;
import ch.systemsx.cisd.cifex.client.application.model.UserGridModel;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * The <code>GridCellListenerAdapter</code> for users grid.
 * <p>
 * This is used on {@link AdminMainPage} and on {@link MainPage}.
 * </p>
 * 
 * @author Christian Ribeaud
 */
final class UserActionGridCellListener<F extends AbstractFileGridModel> implements
        Listener<GridEvent<UserGridModel>>
{

    private final ViewContext viewContext;

    private final Grid<F> fileGridOrNull;

    UserActionGridCellListener(final ViewContext viewContext, final Grid<F> filesGrid)
    {
        this.viewContext = viewContext;
        this.fileGridOrNull = filesGrid;
    }

    private final static String getUserDescription(final UserGridModel model)
    {
        final String fullName = model.get(UserGridModel.FULL_NAME);
        final String userCode = model.get(UserGridModel.USER_CODE);
        if (StringUtils.isBlank(fullName))
        {
            return userCode;
        }
        return fullName;
    }

    // w
    // Helper classes
    //

    private final class RenamingConfirmCallback implements Listener<MessageBoxEvent>
    {
        private final String userCode;

        private final String userCodeAfterRenaming;

        private final Grid<UserGridModel> userGrid;

        private RenamingConfirmCallback(final String userCode, final String userCodeAfterRenaming,
                final Grid<UserGridModel> userGrid)
        {
            this.userCode = userCode;
            this.userCodeAfterRenaming = userCodeAfterRenaming;
            this.userGrid = userGrid;
        }

        public void handleEvent(MessageBoxEvent be)
        {
            if (be.getButtonClicked().getItemId().equals(Dialog.YES))
            {
                viewContext.getCifexService().changeUserCode(userCode, userCodeAfterRenaming,
                        new UsersFilesRefresherCallback<F>(viewContext, userGrid, fileGridOrNull));
            }
        }
    }

    private static final class UsersFilesRefresherCallback<F extends AbstractFileGridModel> extends
            AbstractAsyncCallback<Void>
    {
        private final Grid<UserGridModel> userGrid;

        private final Grid<F> fileGrid;

        private final ViewContext context;

        public UsersFilesRefresherCallback(final ViewContext context,
                final Grid<UserGridModel> userGrid, final Grid<F> fileGridOrNull)
        {
            super(context);
            this.userGrid = userGrid;
            this.fileGrid = fileGridOrNull;
            this.context = context;

        }

        @SuppressWarnings("unchecked")
        public void onSuccess(final Void result)
        {

            boolean adminView = false;
            if (context.getHistoryController().getCurrentPage() == Page.ADMIN_PAGE)
            {
                adminView = true;
            }
            new UserGridRefresherCallback(context, userGrid).onSuccess(result);
            if (fileGrid != null)
            {
                if (adminView)
                {
                    new UpdateAdminFileAsyncCallback((Grid<AdminFileGridModel>) fileGrid, context)
                            .onSuccess(result);
                } else
                {
                    new UpdateUploadedFileAsyncCallback((Grid<UploadedFileGridModel>) fileGrid,
                            context).onSuccess(result);
                }
            }
        }
    }

    private final class FindUserAsyncCallback extends AbstractAsyncCallback<UserInfoDTO>
    {
        private final Grid<UserGridModel> grid;

        public FindUserAsyncCallback(final ViewContext context, final Grid<UserGridModel> grid)
        {
            super(context);
            this.grid = grid;
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final UserInfoDTO result)
        {
            new EditUserDialog(viewContext, result, grid).show();
        }
    }

    private final class RenewUserAsyncCallback extends AbstractAsyncCallback<UserInfoDTO>
    {
        final Grid<UserGridModel> modelBasedGrid;

        public RenewUserAsyncCallback(final ViewContext context,
                final Grid<UserGridModel> modelBasedGrid)
        {
            super(context);
            this.modelBasedGrid = modelBasedGrid;
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final UserInfoDTO result)
        {
            final UserInfoDTO user = result;
            assert user.isPermanent() == false : "Regular user can not be renewed.";
            user.setExpirationDate(null);
            viewContext.getCifexService().updateUser(user, null, false,
                    new UserGridRefresherCallback(viewContext, modelBasedGrid));
        }
    }

    public void handleEvent(GridEvent<UserGridModel> be)
    {
        final Grid<UserGridModel> grid = be.getGrid();
        final int rowIndex = be.getRowIndex();
        final int colIndex = be.getColIndex();
        final Event e = be.getEvent();

        final IMessageResources messageResources = viewContext.getMessageResources();
        final Grid<UserGridModel> userGrid = grid;
        if (grid.getColumnModel().getDataIndex(colIndex).equals(UserGridModel.ACTION))
        {
            final UserGridModel model = grid.getStore().getAt(rowIndex);
            final String userCode = model.get(UserGridModel.USER_CODE);
            final String userDescription = getUserDescription(model);
            final EventTarget element = e.getEventTarget();
            if (element == null)
            {
                return;
            }
            final String targetId =
                    DOM.getElementAttribute((com.google.gwt.user.client.Element) Element.as(e
                            .getEventTarget()), "id");
            // Delete user
            if (Constants.DELETE_ID.equals(targetId))
            {
                assert userCode.equals(viewContext.getModel().getUser().getUserCode()) == false : "An user can not delete himself.";
                MessageBox.confirm(messageResources.getUserDeleteTitle(), messageResources
                        .getUserDeleteConfirmText(userDescription), new Listener<MessageBoxEvent>()
                    {

                        public void handleEvent(MessageBoxEvent messageEvent)
                        {
                            if (messageEvent.getButtonClicked().getItemId().equals(Dialog.YES))
                            {
                                viewContext.getCifexService().deleteUser(userCode,
                                        new UserGridRefresherCallback(viewContext, userGrid));
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
                        new Listener<MessageBoxEvent>()
                            {

                                public void handleEvent(MessageBoxEvent messageEvent)
                                {
                                    String userCodeAfterRenaming = messageEvent.getValue();
                                    if (messageEvent.getButtonClicked().getItemId().equals(
                                            Dialog.OK)
                                            && StringUtils.isBlank(userCodeAfterRenaming) == false)
                                    {
                                        if (StringUtils.matches(Constants.USER_CODE_REGEX,
                                                userCodeAfterRenaming) == false)
                                        {
                                            MessageBox.alert("Invalid user code",
                                                    Constants.VALID_USER_CODE_DESCRIPTION, null);
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

}