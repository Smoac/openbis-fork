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
import com.gwtext.client.core.Ext;
import com.gwtext.client.widgets.grid.Grid;
import com.gwtext.client.widgets.layout.ContentPanel;

import ch.systemsx.cisd.cifex.client.application.model.IDataGridModel;
import ch.systemsx.cisd.cifex.client.application.model.UserGridModel;
import ch.systemsx.cisd.cifex.client.application.ui.ModelBasedGrid;
import ch.systemsx.cisd.cifex.client.dto.User;
import ch.systemsx.cisd.cifex.shared.basic.dto.File;

/**
 * An <code>AbstractMainPage</code> extension for administrators.
 * 
 * @author Franz-Josef Elmer
 */
final class AdminMainPage extends AbstractMainPage
{

    private VerticalPanel listUserPanel;

    private VerticalPanel listFilesPanel;

    private ModelBasedGrid filesGrid;

    AdminMainPage(final ViewContext context)
    {
        super(context);
    }

    private final void loadListFileGrid()
    {
        context.getCifexService().listFiles(new FileAdminAsyncCallback());
    }

    private final void createListUserGrid()
    {
        context.getCifexService().listUsers(new UserAsyncCallback());
    }

    //
    // AbstractMainPage
    //

    protected final ContentPanel createMainPanel()
    {
        final ContentPanel mainPanel = new ContentPanel(Ext.generateId());
        createUserPanel(true);

        listFilesPanel = createVerticalPanelPart();
        filesGrid = createFileTable(new File[0]);
        listFilesPanel.add(createPartTitle(context.getMessageResources().getFilesPartTitle()));
        listFilesPanel.add(filesGrid);

        listUserPanel = createVerticalPanelPart();
        createListUserGrid();

        loadListFileGrid();

        mainPanel.add(createUserPanel);
        mainPanel.add(listUserPanel);
        mainPanel.add(listFilesPanel);
        return mainPanel;
    }

    private final ModelBasedGrid createFileTable(final File[] files)
    {
        final IDataGridModel gridModel = new AdminFileGridModel(context.getMessageResources());
        final ModelBasedGrid fileGrid =
                new ModelBasedGrid(context.getMessageResources(), files, gridModel);
        fileGrid.addGridCellListener(new FileDownloadGridCellListener());
        fileGrid.addGridCellListener(new FileActionGridCellListener(true, context));
        fileGrid.addGridCellListener(new FileCommentGridCellListener(context));
        return fileGrid;
    }

    /**
     * An {@link com.google.gwt.user.client.rpc.AsyncCallback} that creates a table with all users
     * on success.
     */
    private final class UserAsyncCallback extends AbstractAsyncCallback
    {

        UserAsyncCallback()
        {
            super(context);
        }

        private Widget createUserTable(final User[] users)
        {
            final IDataGridModel gridModel = new UserGridModel(context);
            final Grid userGrid =
                    new ModelBasedGrid(context.getMessageResources(), users, gridModel);
            // Delete user and change code function
            userGrid.addGridCellListener(new UserActionGridCellListener(context, filesGrid));
            return userGrid;
        }

        public final void onSuccess(final Object result)
        {
            listUserPanel.add(createPartTitle(messageResources.getUsersPartTitle()));
            listUserPanel.add(createUserTable((User[]) result));
        }
    }

    /**
     * An {@link com.google.gwt.user.client.rpc.AsyncCallback} that creates a table with all files
     * on success.
     */
    private final class FileAdminAsyncCallback extends AbstractAsyncCallback
    {

        FileAdminAsyncCallback()
        {
            super(context);
        }

        public final void onSuccess(final Object result)
        {
            filesGrid.reloadStore((File[]) result);
        }
    }

}
