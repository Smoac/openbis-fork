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

package ch.systemsx.cisd.cifex.client.application.page;

import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.*;

import java.util.Date;
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

import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.EditFileDialog;
import ch.systemsx.cisd.cifex.client.application.FileShareUpdateUserDialog;
import ch.systemsx.cisd.cifex.client.application.IQuotaInformationUpdater;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.grid.GridWidget;
import ch.systemsx.cisd.cifex.client.application.model.AbstractFileGridModel;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * A <code>GridCellListenerAdapter</code> extension for deleting, renewing or sharing with other
 * users a <code>File</code>.
 * 
 * @author Christian Ribeaud
 */
abstract class FileActionGridCellListener implements Listener<GridEvent<AbstractFileGridModel>>
{

    private final ViewContext viewContext;

    private final boolean adminView;

    private final GridWidget<AbstractFileGridModel> myFileGridWidget;

    private final List<GridWidget<AbstractFileGridModel>> fileGridWidgets;

    private final IQuotaInformationUpdater quotaUpdaterOrNull;

    FileActionGridCellListener(final boolean adminView, final ViewContext viewContext,
            final GridWidget<AbstractFileGridModel> myFileGridWidget,
            final List<GridWidget<AbstractFileGridModel>> fileGridWidgets,
            final IQuotaInformationUpdater quotaUpdaterOrNull)
    {
        this.adminView = adminView;
        this.viewContext = viewContext;
        this.myFileGridWidget = myFileGridWidget;
        this.fileGridWidgets = fileGridWidgets;
        this.quotaUpdaterOrNull = quotaUpdaterOrNull;
    }

    //
    // Helper classes
    //

    /**
     * An {@link AsyncCallback} that updates the list of files after a file has been deleted.
     */
    private final class DeleteOwnedFileAsyncCallback extends AbstractAsyncCallback<Void>
    {
        private final long id;

        DeleteOwnedFileAsyncCallback(final long id)
        {
            super(viewContext);
            this.id = id;
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final Void result)
        {
            assert adminView == false;
            for (GridWidget<AbstractFileGridModel> widget : fileGridWidgets)
            {
                widget.removeItem(id);
            }
            if (quotaUpdaterOrNull != null)
            {
                quotaUpdaterOrNull.triggerUpdate();
            }
        }
    }

    /**
     * An {@link AsyncCallback} that updates the list of files after a file has been deleted.
     */
    private final class AdminDeleteFileAsyncCallback extends AbstractAsyncCallback<Void>
    {
        private final long id;

        AdminDeleteFileAsyncCallback(final long id)
        {
            super(viewContext);
            this.id = id;
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final Void result)
        {
            assert adminView;
            for (GridWidget<AbstractFileGridModel> widget : fileGridWidgets)
            {
                widget.removeItem(id);
            }
        }
    }

    /**
     * An {@link AsyncCallback} that shows the list of users a file has been shared with.
     */
    private final class ShowUsersFileSharedWithAsyncCallback extends
            AbstractAsyncCallback<List<UserInfoDTO>>
    {
        private final GridWidget<AbstractFileGridModel> grid;

        final String fileName;

        final long fileId;

        ShowUsersFileSharedWithAsyncCallback(final String name, final long id,
                final GridWidget<AbstractFileGridModel> grid)
        {
            super(viewContext);
            this.fileName = name;
            this.fileId = id;
            this.grid = grid;
        }

        public final void onSuccess(final List<UserInfoDTO> result)
        {
            final FileShareUpdateUserDialog dialog =
                    new FileShareUpdateUserDialog(viewContext, result, fileName, fileId,
                            createUpdateFilesCallback(grid, getViewContext()));
            dialog.show();

        }
    }

    public void handleEvent(GridEvent<AbstractFileGridModel> be)
    {
        Grid<AbstractFileGridModel> grid = myFileGridWidget.getGrid();
        int rowIndex = be.getRowIndex();
        int colindex = be.getColIndex();
        final ModelData record = grid.getStore().getAt(rowIndex);
        final long id = record.get(AbstractFileGridModel.ID);
        final String name = record.get(AbstractFileGridModel.NAME);
        final String dataIndex = grid.getColumnModel().getDataIndex(colindex);
        if (dataIndex.equals(AbstractFileGridModel.ACTION))
        {
            final Element element = be.getTarget();
            if (element == null)
            {
                return;
            }
            final String targetId = DOM.getElementAttribute(be.getTarget(), "id");
            // Delete
            if (Constants.DELETE_ID.equals(targetId))
            {
                MessageBox.confirm(msg(DELETE_FILE_MSGBOX_TITLE), msg(DELETE_FILE_CONFIRM_LABEL,
                        name), new Listener<MessageBoxEvent>()
                    {

                        public void handleEvent(MessageBoxEvent messageEvent)
                        {
                            if (messageEvent.getButtonClicked().getItemId().equals(Dialog.YES))
                            {
                                if (adminView)
                                {
                                    viewContext.getCifexService().deleteFile(id,
                                            new AdminDeleteFileAsyncCallback(id));
                                } else
                                {
                                    viewContext.getCifexService().deleteFile(id,
                                            new DeleteOwnedFileAsyncCallback(id));
                                }

                            }
                        }

                    });
            }
            // Edit
            if (Constants.EDIT_ID.equals(targetId))
            {
                // Edit File
                viewContext.getCifexService().getFile(id,
                        new EditFileAsyncCallback(viewContext, myFileGridWidget));
            }
            // Shared
            if (Constants.SHARED_ID.equals(targetId))
            {
                viewContext.getCifexService().listUsersFileSharedWith(id,
                        new ShowUsersFileSharedWithAsyncCallback(name, id, myFileGridWidget));
            }
        }
    }

    private final class EditFileAsyncCallback extends AbstractAsyncCallback<FileInfoDTO>
    {
        private final GridWidget<AbstractFileGridModel> grid;

        public EditFileAsyncCallback(final ViewContext context,
                final GridWidget<AbstractFileGridModel> grid)
        {
            super(context);
            this.grid = grid;
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final FileInfoDTO resultOrNull)
        {
            new EditFileDialog(getViewContext(), resultOrNull,
                    createVoidAdapter(createUpdateFilesCallback(grid, getViewContext()))).show();
        }

        private AsyncCallback<Date> createVoidAdapter(final AsyncCallback<Void> callback)
        {
            return new AbstractAsyncCallback<Date>(getViewContext())
                {
                    public void onSuccess(Date result)
                    {
                        callback.onSuccess(null);
                    }
                };
        }
    }

    protected abstract AsyncCallback<Void> createUpdateFilesCallback(
            GridWidget<AbstractFileGridModel> grid, ViewContext context);

}