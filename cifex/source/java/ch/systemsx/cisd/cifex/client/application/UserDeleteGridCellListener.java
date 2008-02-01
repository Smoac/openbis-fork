package ch.systemsx.cisd.cifex.client.application;

import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.Record;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.grid.Grid;
import com.gwtext.client.widgets.grid.event.GridCellListenerAdapter;

import ch.systemsx.cisd.cifex.client.application.model.IDataGridModel;
import ch.systemsx.cisd.cifex.client.application.model.UserGridModel;
import ch.systemsx.cisd.cifex.client.application.ui.ModelBasedGrid;
import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * The <code>GridCellListenerAdapter</code> for users grid.
 * 
 * @author Christian Ribeaud
 */
final class UserDeleteGridCellListener extends GridCellListenerAdapter
{

    private final ViewContext viewContext;

    UserDeleteGridCellListener(final ViewContext viewContext)
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
            final String email = record.getAsString(UserGridModel.EMAIL);
            final String name = record.getAsString(UserGridModel.FULL_NAME);
            if (email.equals(viewContext.getModel().getUser().getEmail()))
            {
                MessageBox.alert(messageResources.getMessageBoxErrorTitle(), messageResources.getUserDeleteHimself());
                return;
            }
            MessageBox.confirm(messageResources.getUserDeleteTitle(), messageResources.getUserDeleteConfirmText(name),
                    new MessageBox.ConfirmCallback()
                        {

                            //
                            // ConfirmCallback
                            //

                            public final void execute(final String btnID)
                            {
                                if (btnID.equals("yes"))
                                {
                                    viewContext.getCifexService().tryToDeleteUser(email,
                                            new DeleteUserAsyncCallback((ModelBasedGrid) grid));
                                }
                            }
                        });

        }
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
            viewContext.getCifexService().listUsers(new AbstractAsyncCallback(viewContext)
                {

                    //
                    // AbstractAsyncCallback
                    //

                    public final void onSuccess(final Object res)
                    {
                        modelBasedGrid.reloadStore((User[]) res, model);
                    }
                });
        }
    }

}