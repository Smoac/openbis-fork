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

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Ext;
import com.gwtext.client.data.Record;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.grid.Grid;
import com.gwtext.client.widgets.grid.event.GridCellListenerAdapter;
import com.gwtext.client.widgets.layout.ContentPanel;

import ch.systemsx.cisd.cifex.client.application.model.IDataGridModel;
import ch.systemsx.cisd.cifex.client.application.model.UserGridModel;
import ch.systemsx.cisd.cifex.client.application.ui.ModelBasedGrid;
import ch.systemsx.cisd.cifex.client.dto.File;
import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * An <code>AbstractMainPage</code> extension for administrators.
 * 
 * @author Franz-Josef Elmer
 */
final class AdminMainPage extends AbstractMainPage
{

    private VerticalPanel listUserPanel;

    private VerticalPanel listFilesPanel;

    AdminMainPage(ViewContext context)
    {
        super(context);
    }

    protected ContentPanel createMainPanel()
    {
        ContentPanel mainPanel = new ContentPanel(Ext.generateId());
        createUserPanel(true);

        listUserPanel = createVerticalPanelPart();
        createListUserGrid();

        listFilesPanel = createVerticalPanelPart();
        createListFileGrid();

        mainPanel.add(createUserPanel);
        mainPanel.add(listUserPanel);
        mainPanel.add(listFilesPanel);
        return mainPanel;
    }

    private final void createListFileGrid()
    {
        context.getCifexService().listUploadedFiles(new FileAdminAsyncCallback());
    }

    private final void createListUserGrid()
    {
        context.getCifexService().listUsers(new UserAsyncCallback());
    }

    private final class UserAsyncCallback extends AbstractAsyncCallback
    {

        UserAsyncCallback()
        {
            super(context);
        }

        public void onSuccess(Object result)
        {
            listUserPanel.add(createPartTitle(messageResources.getUsersPartTitle()));
            listUserPanel.add(createUserTable((User[]) result));
        }

        private Widget createUserTable(final User[] users)
        {
            final IDataGridModel gridModel = new UserGridModel(context.getMessageResources());
            final Grid userGrid = new ModelBasedGrid(context.getMessageResources(), users, gridModel, null);
            // Delete user function
            userGrid.addGridCellListener(new UserGridCellListener(context));
            return userGrid;
        }
    }

    private final class DeleteFileAsyncCallback extends AbstractAsyncCallback
    {

        DeleteFileAsyncCallback()
        {
            super(context);
        }

        public void onSuccess(Object result)
        {
            listFilesPanel.clear();
            createListFileGrid();
        }
    }

    private final class FileAdminAsyncCallback extends AbstractAsyncCallback
    {

        FileAdminAsyncCallback()
        {
            super(context);
        }

        public void onSuccess(Object result)
        {
            File[] files = (File[]) result;
            listFilesPanel.add(createPartTitle(context.getMessageResources().getFilesPartTitle()));
            listFilesPanel.add(createFileTable(files));
        }

        private Widget createFileTable(final File[] files)
        {
            final IDataGridModel gridModel = new AdminFileGridModel(context.getMessageResources());
            final ModelBasedGrid fileGrid = new ModelBasedGrid(context.getMessageResources(), files, gridModel, null);
            final FileTableMap fileTableMap = new FileTableMap(files);
            fileGrid.addGridCellListener(new FileGridCellListener(fileTableMap));
            fileGrid.addGridCellListener(new GridCellListenerAdapter()
                {

                    public final void onCellClick(final Grid grid, final int rowIndex, final int colindex,
                            final EventObject e)
                    {
                        final Record record = grid.getStore().getAt(rowIndex);
                        final int id = record.getAsInteger(AbstractFileGridModel.ID);
                        final String name = record.getAsString(AbstractFileGridModel.NAME);
                        if (grid.getColumnModel().getDataIndex(colindex).equals(AbstractFileGridModel.ACTION))
                        {
                            MessageBox.confirm("Delete File", "Are you sure you want to delete [" + name + "] ?",
                                    new MessageBox.ConfirmCallback()
                                        {
                                            public void execute(String btnID)
                                            {
                                                if (btnID.equals("yes"))
                                                {
                                                    context.getCifexService().tryToDeleteFile(id,
                                                            new DeleteFileAsyncCallback());
                                                }
                                            }
                                        });

                        }
                    }
                });
            return fileGrid;
        }
    }

}
