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

import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.cifex.client.application.model.UserGridModel;
import ch.systemsx.cisd.cifex.client.application.utils.WidgetUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.AdminFileInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * An <code>AbstractMainPage</code> extension for administrators.
 * 
 * @author Franz-Josef Elmer
 */
final class AdminMainPage extends AbstractMainPage
{

    AdminMainPage(final ViewContext context)
    {
        super(context);
    }

    @Override
    protected final LayoutContainer createMainPanel()
    {
        final LayoutContainer mainPanel = new LayoutContainer();
        LayoutContainer createUserPanel = createUserPanel(true, context);

        LayoutContainer listFilesPanel = createContainer();
        Grid<AdminFileGridModel> filesGrid = createFileTable(new AdminFileInfoDTO[0], context);
        addTitlePart(listFilesPanel, context.getMessageResources().getFilesPartTitle());
        listFilesPanel.add(filesGrid);

        LayoutContainer listUserPanel = createContainer();
        createListUserGrid(listUserPanel, filesGrid, context);

        loadListFileGrid(filesGrid, context);

        mainPanel.add(createUserPanel);
        mainPanel.add(listUserPanel);
        mainPanel.add(listFilesPanel);
        return mainPanel;
    }

    static private final void loadListFileGrid(Grid<AdminFileGridModel> filesGrid,
            ViewContext context)
    {
        context.getCifexService().listFiles(new FileAdminAsyncCallback(context, filesGrid));
    }

    static private final void createListUserGrid(LayoutContainer listUserPanel,
            Grid<AdminFileGridModel> filesGrid, ViewContext context)
    {
        context.getCifexService().listUsers(
                new UserAsyncCallback(listUserPanel, context, filesGrid));
    }

    private static final Grid<AdminFileGridModel> createFileTable(final AdminFileInfoDTO[] files,
            ViewContext context)
    {
        ListStore<AdminFileGridModel> store = new ListStore<AdminFileGridModel>();
        store.add(AdminFileGridModel.convert(context.getMessageResources(), Arrays.asList(files)));
        final Grid<AdminFileGridModel> fileGrid =
                new Grid<AdminFileGridModel>(store, new ColumnModel(AdminFileGridModel
                        .getColumnConfigs(context.getMessageResources())));
        fileGrid.setHeight(Constants.GRID_HEIGHT);
        fileGrid.addListener(Events.CellClick, new FileDownloadGridCellListener());
        fileGrid.addListener(Events.CellClick, new AdminFileActionGridCellListener(context));
        fileGrid.addListener(Events.CellClick, new FileCommentGridCellListener(context));
        return fileGrid;
    }

    /**
     * An {@link com.google.gwt.user.client.rpc.AsyncCallback} that creates a table with all users
     * on success.
     */
    private static final class UserAsyncCallback extends AbstractAsyncCallback<List<UserInfoDTO>>
    {

        private final LayoutContainer listUserPanel;

        private final ViewContext context;

        private final Grid<AdminFileGridModel> filesGrid;

        UserAsyncCallback(LayoutContainer listUserPanel, ViewContext context,
                Grid<AdminFileGridModel> filesGrid)
        {
            super(context);
            this.listUserPanel = listUserPanel;
            this.context = context;
            this.filesGrid = filesGrid;
        }

        private Widget createUserTable(final List<UserInfoDTO> users)
        {
            ListStore<UserGridModel> store = new ListStore<UserGridModel>();
            store.add(UserGridModel.convert(context, users));
            final Grid<UserGridModel> userGrid =
                    new Grid<UserGridModel>(store, new ColumnModel(UserGridModel
                            .getColumnConfigs(context.getMessageResources())));
            userGrid.setHeight(Constants.GRID_HEIGHT);
            // Delete user and change code function
            userGrid.addListener(Events.CellClick,
                    new UserActionGridCellListener<AdminFileGridModel>(context, filesGrid));
            return userGrid;
        }

        public final void onSuccess(final List<UserInfoDTO> result)
        {
            addTitlePart(listUserPanel, context.getMessageResources().getUsersPartTitle());
            listUserPanel.add(createUserTable(result));
            listUserPanel.layout();
        }
    }

    /**
     * An {@link com.google.gwt.user.client.rpc.AsyncCallback} that creates a table with all files
     * on success.
     */
    private static final class FileAdminAsyncCallback extends
            AbstractAsyncCallback<List<AdminFileInfoDTO>>
    {

        private final ViewContext context;

        private final Grid<AdminFileGridModel> filesGrid;

        FileAdminAsyncCallback(ViewContext context, Grid<AdminFileGridModel> filesGrid)
        {
            super(context);
            this.context = context;
            this.filesGrid = filesGrid;
        }

        public final void onSuccess(final List<AdminFileInfoDTO> result)
        {
            WidgetUtils.reloadStore(filesGrid, AdminFileGridModel.convert(context
                    .getMessageResources(), result));
        }
    }

}
