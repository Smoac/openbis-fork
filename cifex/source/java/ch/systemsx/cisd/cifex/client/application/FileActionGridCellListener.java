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
import com.gwtext.client.widgets.BaseExtWidget;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.grid.Grid;
import com.gwtext.client.widgets.grid.event.GridCellListenerAdapter;

import ch.systemsx.cisd.cifex.client.application.ui.ModelBasedGrid;
import ch.systemsx.cisd.cifex.client.dto.UserInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;

/**
 * A <code>GridCellListenerAdapter</code> extension for deleting, renewing or sharing with other
 * users a <code>File</code>.
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
        if (dataIndex.equals(AbstractFileGridModel.ACTION))
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
                viewContext.getCifexService().updateFileExpiration(idStr,
                        new UpdateFileAsyncCallback((ModelBasedGrid) grid, viewContext, adminView));
            }
            // Shared
            if (Constants.SHARED_ID.equals(targetId))
            {
                viewContext.getCifexService()
                        .listUsersFileSharedWith(
                                idStr,
                                new ShowUsersFileSharedWithAsyncCallback((ModelBasedGrid) grid,
                                        name, idStr));
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
                        modelBasedGrid.reloadStore((FileInfoDTO[]) res);
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
     * An {@link AsyncCallback} that shows the list of users a file has been shared with.
     */
    private final class ShowUsersFileSharedWithAsyncCallback extends AbstractAsyncCallback
    {

        final String fileName;

        final String fileId;

        private final BaseExtWidget modelBasedGrid;

        ShowUsersFileSharedWithAsyncCallback(final ModelBasedGrid modelBasedGrid,
                final String name, final String idStr)
        {
            super(viewContext);
            this.fileName = name;
            this.fileId = idStr;
            this.modelBasedGrid = modelBasedGrid;
        }

        public final void onSuccess(final Object result)
        {
            final UserInfoDTO[] users = (UserInfoDTO[]) result;
            final FileShareUpdateUserDialog dialog =
                    new FileShareUpdateUserDialog(viewContext, users, fileName, fileId);
            dialog.show(modelBasedGrid.getEl());

        }
    }

}