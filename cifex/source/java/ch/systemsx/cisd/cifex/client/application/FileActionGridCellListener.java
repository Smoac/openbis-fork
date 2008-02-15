package ch.systemsx.cisd.cifex.client.application;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.Record;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.grid.Grid;
import com.gwtext.client.widgets.grid.event.GridCellListenerAdapter;

import ch.systemsx.cisd.cifex.client.application.model.IDataGridModel;
import ch.systemsx.cisd.cifex.client.application.ui.ModelBasedGrid;
import ch.systemsx.cisd.cifex.client.dto.File;

/**
 * A <code>GridCellListenerAdapter</code> extension for deleting or renewing a <code>File</code>.
 * 
 * @author Christian Ribeaud
 */
final class FileActionGridCellListener extends GridCellListenerAdapter
{

    private final ViewContext viewContext;

    private final boolean adminView;

    FileActionGridCellListener(final boolean adminView, final ViewContext viewContext)
    {
        this.adminView = adminView;
        this.viewContext = viewContext;
    }

    //
    // GridCellListenerAdapter
    //

    public final void onCellClick(final Grid grid, final int rowIndex, final int colindex, final EventObject e)
    {
        final Record record = grid.getStore().getAt(rowIndex);
        final int id = record.getAsInteger(AbstractFileGridModel.ID);
        final String name = record.getAsString(AbstractFileGridModel.NAME);
        if (grid.getColumnModel().getDataIndex(colindex).equals(AbstractFileGridModel.ACTION))
        {
            final IMessageResources messageResources = viewContext.getMessageResources();
            // Delete
            if (e.getTarget(Constants.DELETE_TARGET, 1) != null)
            {
                MessageBox.confirm(messageResources.getFileDeleteTitle(), messageResources
                        .getFileDeleteConfirmText(name), new MessageBox.ConfirmCallback()
                    {
                        //
                        // ConfirmCallback
                        //

                        public final void execute(final String btnID)
                        {
                            if (btnID.equals("yes"))
                            {
                                viewContext.getCifexService().deleteFile(id,
                                        new DeleteFileAsyncCallback((ModelBasedGrid) grid));
                            }
                        }
                    });
                // Renew
            }
            if (e.getTarget(Constants.RENEW_TARGET, 1) != null)
            {
                viewContext.getCifexService().updateFileExpiration(id, null,
                        new UpdateFileAsyncCallback((ModelBasedGrid) grid));
            }

        }
    }

    //
    // Helper classes
    //

    private final class DeleteFileAsyncCallback extends AbstractAsyncCallback
    {
        private final ModelBasedGrid modelBasedGrid;

        DeleteFileAsyncCallback(final ModelBasedGrid modelBasedGrid)
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
            final AsyncCallback callback = new AbstractAsyncCallback(viewContext)
                {

                    //
                    // AbstractAsyncCallback
                    //

                    public final void onSuccess(final Object res)
                    {
                        modelBasedGrid.reloadStore((File[]) res, model);
                    }
                };
            if (adminView)
            {
                viewContext.getCifexService().listFiles(callback);
            } else
            {
                viewContext.getCifexService().listUploadedFiles(callback);
            }
        }
    }

    private final class UpdateFileAsyncCallback extends AbstractAsyncCallback
    {
        private final ModelBasedGrid modelBasedGrid;

        UpdateFileAsyncCallback(final ModelBasedGrid modelBasedGrid)
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
            AbstractAsyncCallback callback = new AbstractAsyncCallback(viewContext)
                {
                    //
                    // AbstractAsyncCallback
                    //

                    public final void onSuccess(final Object res)
                    {
                        modelBasedGrid.reloadStore((File[]) res, model);
                    }
                };
            if (adminView)
            {
                viewContext.getCifexService().listFiles(callback);
            } else
            {
                viewContext.getCifexService().listUploadedFiles(callback);
            }
        }
    }
}