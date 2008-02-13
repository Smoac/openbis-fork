package ch.systemsx.cisd.cifex.client.application;

import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.Record;
import com.gwtext.client.widgets.LayoutDialog;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.grid.Grid;
import com.gwtext.client.widgets.grid.event.GridCellListenerAdapter;

import ch.systemsx.cisd.cifex.client.application.model.IDataGridModel;
import ch.systemsx.cisd.cifex.client.application.model.UserGridModel;
import ch.systemsx.cisd.cifex.client.application.ui.ModelBasedGrid;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * The <code>GridCellListenerAdapter</code> for users grid.
 * 
 * @author Christian Ribeaud
 */
final class UserActionGridCellListener extends GridCellListenerAdapter
{

    private final ViewContext viewContext;

    UserActionGridCellListener(final ViewContext viewContext)
    {
        this.viewContext = viewContext;
    }

    //
    // GridCellListenerAdapter
    //

    public final void onCellClick(final Grid grid, final int rowIndex, final int colIndex, final EventObject e)
    {
        final IMessageResources messageResources = viewContext.getMessageResources();
        if (grid.getColumnModel().getDataIndex(colIndex).equals(UserGridModel.ACTION))
        {
            final Record record = grid.getStore().getAt(rowIndex);
            final String userCode = record.getAsString(UserGridModel.USER_CODE);
            final String userDescription = getUserDescription(record);
            // Delete user
            if (e.getTarget(Constants.DELETE_TARGET, 1) != null)
            {
                if (userCode.equals(viewContext.getModel().getUser().getUserCode()))
                {
                    MessageBox.alert(messageResources.getMessageBoxErrorTitle(), messageResources
                            .getUserDeleteHimself());
                    return;
                }
                MessageBox.confirm(messageResources.getUserDeleteTitle(), messageResources
                        .getUserDeleteConfirmText(userDescription), new MessageBox.ConfirmCallback()
                    {

                        //
                        // ConfirmCallback
                        //

                        public final void execute(final String btnID)
                        {
                            if (btnID.equals("yes"))
                            {
                                viewContext.getCifexService().deleteUser(userCode,
                                        new DeleteUserAsyncCallback((ModelBasedGrid) grid));
                            }
                        }
                    });

            } else if (e.getTarget(Constants.EDIT_TARGET, 1) != null)
            {
                if (userCode.equals(viewContext.getModel().getUser().getUserCode()))
                {
                    viewContext.getPageController().createEditCurrentUserPage();
                    return;
                }
                // Edit User
                viewContext.getCifexService().tryFindUserByUserCode(userCode, new FindUserAsyncCallback(viewContext));

            } else if (e.getTarget(Constants.RENEW_TARGET, 1) != null)
            {
                // renew User
                viewContext.getCifexService().tryFindUserByUserCode(userCode,
                        new RenewUserAsyncCallback(viewContext, (ModelBasedGrid) grid));

            }
        }
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

    //
    // Helper classes
    //

    private final class DeleteUserAsyncCallback extends AbstractAsyncCallback
    {

        private final ModelBasedGrid modelBasedGrid;

        DeleteUserAsyncCallback(final ModelBasedGrid modelBasedGrid)
        {
            super(viewContext);
            this.modelBasedGrid = modelBasedGrid;
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final Object result)
        {
            final IDataGridModel model = modelBasedGrid.getModel();
            // Basil Neff: Also normal user need this action, to edit own user. But there is a permission problem on the
            // server side. So also check the permissions here.
            if (viewContext.getModel().getUser().isAdmin())
            {
                viewContext.getCifexService().listUsers(new AbstractAsyncCallback(viewContext)
                    {
                        public final void onSuccess(final Object res)
                        {
                            modelBasedGrid.reloadStore((User[]) res, model);
                        }
                    });
            } else
            {
                viewContext.getCifexService().listUsersRegisteredBy(viewContext.getModel().getUser(),
                        new AbstractAsyncCallback(viewContext)
                            {
                                public final void onSuccess(final Object res)
                                {
                                    modelBasedGrid.reloadStore((User[]) res, model);
                                }
                            });
            }
        }
    }

    private final class FindUserAsyncCallback extends AbstractAsyncCallback
    {

        //
        // AbstractAsyncCallback
        //

        public FindUserAsyncCallback(ViewContext context)
        {
            super(context);
        }

        public final void onSuccess(final Object result)
        {
            LayoutDialog dialog = new EditUserDialog(viewContext, (User) result);
            dialog.show();
        }
    }

    private final class RenewUserAsyncCallback extends AbstractAsyncCallback
    {
        private final ModelBasedGrid modelBasedGrid;

        public RenewUserAsyncCallback(ViewContext context, final ModelBasedGrid modelBasedGrid)
        {
            super(context);
            this.modelBasedGrid = modelBasedGrid;
        }

        public final void onSuccess(final Object result)
        {
            if (((User) result).isPermanent() == false)
            {
                ((User) result).setExpirationDate(null);
                viewContext.getCifexService().updateUser(((User) result), null, new AbstractAsyncCallback(viewContext)
                    {
                        public final void onSuccess(final Object UpdateResult)
                        {
                            final IDataGridModel model = modelBasedGrid.getModel();

                            // Update the Grid
                            // TODO 2008-02-07, Basil Neff: Move this logic to a method, the same is also used
                            // for delete and update user.
                            if (viewContext.getModel().getUser().isAdmin())
                            {
                                viewContext.getCifexService().listUsers(new AbstractAsyncCallback(viewContext)
                                    {
                                        public final void onSuccess(final Object res)
                                        {
                                            modelBasedGrid.reloadStore((User[]) res, model);
                                        }
                                    });
                            } else
                            {
                                viewContext.getCifexService().listUsersRegisteredBy(viewContext.getModel().getUser(),
                                        new AbstractAsyncCallback(viewContext)
                                            {
                                                public final void onSuccess(final Object res)
                                                {
                                                    modelBasedGrid.reloadStore((User[]) res, model);
                                                }
                                            });
                            }
                        }
                    });

            } else
            {
                MessageBox.alert(viewContext.getMessageResources().getMessageBoxErrorTitle(), viewContext
                        .getMessageResources().getPermanentUserFailure());
            }
        }
    }

}