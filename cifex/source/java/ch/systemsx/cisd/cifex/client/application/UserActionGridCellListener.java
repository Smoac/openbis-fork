package ch.systemsx.cisd.cifex.client.application;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.Record;
import com.gwtext.client.widgets.LayoutDialog;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.grid.Grid;
import com.gwtext.client.widgets.grid.event.GridCellListenerAdapter;

import ch.systemsx.cisd.cifex.client.application.model.UserGridModel;
import ch.systemsx.cisd.cifex.client.application.ui.ModelBasedGrid;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.client.dto.User;

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

    UserActionGridCellListener(final ViewContext viewContext)
    {
        this.viewContext = viewContext;
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
    // GridCellListenerAdapter
    //

    public final void onCellClick(final Grid grid, final int rowIndex, final int colIndex, final EventObject e)
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
                // renew User
                viewContext.getCifexService().tryFindUserByUserCode(userCode,
                        new RenewUserAsyncCallback(viewContext, userGrid));
            }
        }
    }

    //
    // Helper classes
    //

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
            final LayoutDialog dialog = new EditUserDialog(viewContext, (User) result, grid);
            dialog.show(grid.getEl());
        }
    }

    private final class RenewUserAsyncCallback extends AbstractAsyncCallback
    {
        final ModelBasedGrid modelBasedGrid;

        public RenewUserAsyncCallback(ViewContext context, final ModelBasedGrid modelBasedGrid)
        {
            super(context);
            this.modelBasedGrid = modelBasedGrid;
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final Object result)
        {
            if (((User) result).isPermanent() == false)
            {
                ((User) result).setExpirationDate(null);
                viewContext.getCifexService().updateUser(((User) result), null,
                        new UserGridRefresherCallback(viewContext, modelBasedGrid));

            } else
            {
                MessageBox.alert(viewContext.getMessageResources().getMessageBoxErrorTitle(), viewContext
                        .getMessageResources().getPermanentUserFailure());
            }
        }
    }

}