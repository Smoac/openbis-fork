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

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Ext;
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
 * @author Franz-Josef Elmer
 */
class AdminMainPage extends AbstractMainPage
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
            List users = (List) result;
            listUserPanel.add(createPartTitle(messageResources.getUsersPartTitle()));
            listUserPanel.add(createUserTable((User[]) users.toArray(new User[users.size()])));
        }

        private Widget createUserTable(User[] users)
        {
            final IDataGridModel gridModel = new UserGridModel(context.getMessageResources());
            final Grid userGrid = new ModelBasedGrid(context.getMessageResources(), users, gridModel, null);
            // Delete user function
            userGrid.addGridCellListener(new GridCellListenerAdapter()
                {
                    public void onCellClick(Grid grid, int rowIndex, int colIndex, EventObject e)
                    {
                        ModelBasedGrid modelBasedGrid = (ModelBasedGrid) grid;
                        if (grid.getColumnModel().getDataIndex(colIndex).equals(
                                context.getMessageResources().getActionLabel()))
                        {
                            final User user = ((User) modelBasedGrid.getObjects()[rowIndex]);
                            if (user.getEmail().equals(context.getModel().getUser().getEmail()))
                            {
                                MessageBox.alert(messageResources.getMessageBoxErrorTitle(),
                                        "You cannot delete yourself.");
                                return;
                            }
                            MessageBox.confirm(messageResources.getMessageBoxInfoTitle(), messageResources.getDeleteUserConfirmText(user.getEmail()), new MessageBox.ConfirmCallback()
                                {
                                    public void execute(String btnID)
                                    {
                                        if (btnID.equals("yes"))
                                        {
                                            context.getCifexService().tryToDeleteUser(user,
                                                    new DeleteUserAsyncCallback());
                                        }
                                    }
                                });

                        }
                    }
                });

            return userGrid;
        }
    }

    private final class DeleteUserAsyncCallback extends AbstractAsyncCallback
    {

        DeleteUserAsyncCallback()
        {
            super(context);
        }

        public void onSuccess(Object result)
        {
            listUserPanel.clear();
            createListUserGrid();
        }
    }

    // TODO, 2008-01-29, Franz-Josef Elmer, same functionality as in MainPage.FileAsyncCallback
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

        private Widget createFileTable(File[] files)
        {
            final IDataGridModel gridModel = new DownloadFileGridModel(context.getMessageResources());
            final Grid fileGrid = new ModelBasedGrid(context.getMessageResources(), files, gridModel, null);
            return fileGrid;
        }
    }

}
