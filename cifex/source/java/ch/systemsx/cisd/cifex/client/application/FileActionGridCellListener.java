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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.Record;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.grid.Grid;
import com.gwtext.client.widgets.grid.event.GridCellListenerAdapter;

import ch.systemsx.cisd.cifex.client.application.ui.DefaultLayoutDialog;
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

    public final void onCellClick(final Grid grid, final int rowIndex, final int colindex,
            final EventObject e)
    {
        final Record record = grid.getStore().getAt(rowIndex);
        final String idStr = record.getAsString(AbstractFileGridModel.ID);
        final String name = record.getAsString(AbstractFileGridModel.NAME);
        final String dataIndex = grid.getColumnModel().getDataIndex(colindex);
        if (dataIndex.equals(AbstractFileGridModel.ACTION)
                || dataIndex.equals(AbstractFileGridModel.COMMENT))
        {
            final IMessageResources messageResources = viewContext.getMessageResources();
            final Element element = e.getTarget();
            if (element == null)
            {
                return;
            }
            final String targetId = DOM.getElementAttribute(e.getTarget(), "id");
            // Delete
            if (Constants.DELETE_ID.equals(targetId))
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
                                viewContext.getCifexService().deleteFile(idStr,
                                        new DeleteFileAsyncCallback((ModelBasedGrid) grid));
                            }
                        }
                    });
            }
            // Renew
            if (Constants.RENEW_ID.equals(targetId))
            {
                viewContext.getCifexService().updateFileExpiration(idStr, null,
                        new UpdateFileAsyncCallback((ModelBasedGrid) grid));
            }
            // Show Comment
            if (Constants.SHOW_COMMENT_ID.equals(targetId))
            {
                final String comment =
                        DOM.getElementAttribute(e.getTarget(), "title").replaceAll("\n", "<br>");
                final DefaultLayoutDialog layoutDialog =
                        new DefaultLayoutDialog(viewContext.getMessageResources(), messageResources
                                .getFileCommentTitle(), DefaultLayoutDialog.DEFAULT_WIDTH,
                                DefaultLayoutDialog.DEFAULT_HEIGHT, true, true);
                layoutDialog.addContentPanel();
                layoutDialog.show();
                layoutDialog.getContentPanel().setContent(comment);
            }
        }
    }

    //
    // Helper classes
    //

    /**
     * An {@link AsyncCallback} that updates the list of files after a file has been deleted.
     */
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
            final AsyncCallback callback = new AbstractAsyncCallback(viewContext)
                {

                    //
                    // AbstractAsyncCallback
                    //

                    public final void onSuccess(final Object res)
                    {
                        modelBasedGrid.reloadStore((File[]) res);
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

    /**
     * An {@link AsyncCallback} that updates the list of files after a file has been updated.
     */
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
            final AbstractAsyncCallback callback = new AbstractAsyncCallback(viewContext)
                {
                    //
                    // AbstractAsyncCallback
                    //

                    public final void onSuccess(final Object res)
                    {
                        modelBasedGrid.reloadStore((File[]) res);
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