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

    private final void createListFileGrid()
    {
        context.getCifexService().listUploadedFiles(new FileAdminAsyncCallback());
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

        listUserPanel = createVerticalPanelPart();
        createListUserGrid();

        listFilesPanel = createVerticalPanelPart();
        createListFileGrid();

        mainPanel.add(createUserPanel);
        mainPanel.add(listUserPanel);
        mainPanel.add(listFilesPanel);
        return mainPanel;
    }

    //
    // Helper classes
    //

    private final class UserAsyncCallback extends AbstractAsyncCallback
    {

        UserAsyncCallback()
        {
            super(context);
        }

        private Widget createUserTable(final User[] users)
        {
            final IDataGridModel gridModel = new UserGridModel(context.getMessageResources());
            final Grid userGrid = new ModelBasedGrid(context.getMessageResources(), users, gridModel, null);
            // Delete user function
            userGrid.addGridCellListener(new UserDeleteGridCellListener(context));
            return userGrid;
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final Object result)
        {
            listUserPanel.add(createPartTitle(messageResources.getUsersPartTitle()));
            listUserPanel.add(createUserTable((User[]) result));
        }
    }

    private final class FileAdminAsyncCallback extends AbstractAsyncCallback
    {

        FileAdminAsyncCallback()
        {
            super(context);
        }

        private final Widget createFileTable(final File[] files)
        {
            final IDataGridModel gridModel = new AdminFileGridModel(context.getMessageResources());
            final ModelBasedGrid fileGrid = new ModelBasedGrid(context.getMessageResources(), files, gridModel, null);
            final FileTableMap fileTableMap = new FileTableMap(files);
            fileGrid.addGridCellListener(new FileDownloadGridCellListener(fileTableMap));
            fileGrid.addGridCellListener(new FileDeleteGridCellListener(context));
            return fileGrid;
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final Object result)
        {
            listFilesPanel.add(createPartTitle(context.getMessageResources().getFilesPartTitle()));
            listFilesPanel.add(createFileTable((File[]) result));
        }
    }

}
