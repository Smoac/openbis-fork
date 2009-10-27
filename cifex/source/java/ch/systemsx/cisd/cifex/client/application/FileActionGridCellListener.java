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

import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.cifex.client.application.utils.WidgetUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.AdminFileInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * A <code>GridCellListenerAdapter</code> extension for deleting, renewing or sharing with other
 * users a <code>File</code>.
 * 
 * @author Christian Ribeaud
 */
abstract class FileActionGridCellListener<T extends AbstractFileGridModel> implements
        Listener<GridEvent<T>>
{

    private final ViewContext viewContext;

    private final boolean adminView;

    FileActionGridCellListener(final boolean adminView, final ViewContext viewContext)
    {
        this.adminView = adminView;
        this.viewContext = viewContext;
    }

    //
    // Helper classes
    //

    /**
     * An {@link AsyncCallback} that updates the list of files after a file has been deleted.
     */
    private final class DeleteUploadedFileAsyncCallback extends AbstractAsyncCallback<Void>
    {
        private final Grid<AbstractFileGridModel> modelBasedGrid;

        DeleteUploadedFileAsyncCallback(final Grid<AbstractFileGridModel> modelBasedGrid)
        {
            super(viewContext);
            this.modelBasedGrid = modelBasedGrid;
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final Void result)
        {
            assert adminView == false;
            viewContext.getCifexService().listUploadedFiles(
                    new AbstractAsyncCallback<List<FileInfoDTO>>(viewContext)
                        {
                            public final void onSuccess(final List<FileInfoDTO> res)
                            {
                                WidgetUtils.reloadStore(modelBasedGrid, UploadedFileGridModel
                                        .convert(viewContext.getMessageResources(), res));
                            }
                        });
        }
    }

    /**
     * An {@link AsyncCallback} that updates the list of files after a file has been deleted.
     */
    private final class AdminDeleteFileAsyncCallback extends AbstractAsyncCallback<Void>
    {
        private final Grid<AbstractFileGridModel> modelBasedGrid;

        AdminDeleteFileAsyncCallback(final Grid<AbstractFileGridModel> grid)
        {
            super(viewContext);
            this.modelBasedGrid = grid;
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final Void result)
        {
            assert adminView;
            viewContext.getCifexService().listFiles(
                    new AbstractAsyncCallback<List<AdminFileInfoDTO>>(viewContext)
                        {
                            public final void onSuccess(final List<AdminFileInfoDTO> res)
                            {
                                WidgetUtils.reloadStore(modelBasedGrid, AdminFileGridModel.convert(
                                        viewContext.getMessageResources(), res));
                            }
                        });
        }
    }

    /**
     * An {@link AsyncCallback} that shows the list of users a file has been shared with.
     */
    private final class ShowUsersFileSharedWithAsyncCallback extends
            AbstractAsyncCallback<List<UserInfoDTO>>
    {

        final String fileName;

        final String fileId;

        ShowUsersFileSharedWithAsyncCallback(final String name, final String idStr)
        {
            super(viewContext);
            this.fileName = name;
            this.fileId = idStr;
        }

        public final void onSuccess(final List<UserInfoDTO> result)
        {
            final List<UserInfoDTO> users = result;
            final FileShareUpdateUserDialog dialog =
                    new FileShareUpdateUserDialog(viewContext, users, fileName, fileId);
            dialog.show();

        }
    }

    public void handleEvent(GridEvent<T> be)
    {
        final Grid<T> grid = be.getGrid();
        int rowIndex = be.getRowIndex();
        int colindex = be.getColIndex();
        final ModelData record = grid.getStore().getAt(rowIndex);
        final String idStr = record.get(AbstractFileGridModel.ID);
        final String name = record.get(AbstractFileGridModel.NAME);
        final String dataIndex = grid.getColumnModel().getDataIndex(colindex);
        if (dataIndex.equals(AbstractFileGridModel.ACTION))
        {
            final IMessageResources messageResources = viewContext.getMessageResources();
            final Element element = be.getTarget();
            if (element == null)
            {
                return;
            }
            final String targetId = DOM.getElementAttribute(be.getTarget(), "id");
            // Delete
            if (Constants.DELETE_ID.equals(targetId))
            {
                MessageBox.confirm(messageResources.getFileDeleteTitle(), messageResources
                        .getFileDeleteConfirmText(name), new Listener<MessageBoxEvent>()
                    {

                        @SuppressWarnings("unchecked")
                        public void handleEvent(MessageBoxEvent messageEvent)
                        {
                            if (messageEvent.getButtonClicked().getItemId().equals(Dialog.YES))
                            {
                                if (adminView)
                                {
                                    viewContext.getCifexService().deleteFile(
                                            idStr,
                                            new AdminDeleteFileAsyncCallback(
                                                    (Grid<AbstractFileGridModel>) grid));
                                } else
                                {
                                    viewContext.getCifexService().deleteFile(
                                            idStr,
                                            new DeleteUploadedFileAsyncCallback(
                                                    (Grid<AbstractFileGridModel>) grid));
                                }

                            }
                        }

                    });
            }
            // Renew
            if (Constants.RENEW_ID.equals(targetId))
            {
                viewContext.getCifexService().updateFileExpiration(idStr,
                        createUpdateFilesCallback(grid, viewContext));
            }
            // Shared
            if (Constants.SHARED_ID.equals(targetId))
            {
                viewContext.getCifexService().listUsersFileSharedWith(idStr,
                        new ShowUsersFileSharedWithAsyncCallback(name, idStr));
            }
        }
    }

    protected abstract AsyncCallback<Void> createUpdateFilesCallback(Grid<T> grid,
            ViewContext context);

}