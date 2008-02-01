package ch.systemsx.cisd.cifex.client.application;

import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.Record;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.grid.Grid;
import com.gwtext.client.widgets.grid.event.GridCellListenerAdapter;

import ch.systemsx.cisd.cifex.client.application.model.IDataGridModel;
import ch.systemsx.cisd.cifex.client.application.ui.ModelBasedGrid;
import ch.systemsx.cisd.cifex.client.dto.File;

/**
 * A <code>GridCellListenerAdapter</code> extension for deleting <code>File</code>.
 * 
 * @author Christian Ribeaud
 */
final class FileDeleteGridCellListener extends GridCellListenerAdapter
{

    private final ViewContext viewContext;

    FileDeleteGridCellListener(final ViewContext viewContext)
    {
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
            MessageBox.confirm("Delete File", "Are you sure you want to delete [" + name + "] ?",
                    new MessageBox.ConfirmCallback()
                        {
                            //
                            // ConfirmCallback
                            //

                            public final void execute(final String btnID)
                            {
                                if (btnID.equals("yes"))
                                {
                                    viewContext.getCifexService().tryToDeleteFile(id,
                                            new DeleteFileAsyncCallback((ModelBasedGrid) grid));
                                }
                            }
                        });

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
            viewContext.getCifexService().listUploadedFiles(new AbstractAsyncCallback(viewContext)
                {

                    //
                    // AbstractAsyncCallback
                    //

                    public final void onSuccess(final Object res)
                    {
                        modelBasedGrid.reloadStore((File[]) res, model);
                    }
                });
        }
    }
}