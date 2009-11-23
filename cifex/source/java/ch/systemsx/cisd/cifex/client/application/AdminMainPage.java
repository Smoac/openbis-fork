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
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.cifex.client.application.GridUtils.GridWidget;
import ch.systemsx.cisd.cifex.client.application.model.UserGridModel;
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
        GridWidget<AbstractFileGridModel> filesGrid =
                createFileTable(new AdminFileInfoDTO[0], context);
        addTitlePart(listFilesPanel, context.getMessageResources().getFilesPartTitle());
        listFilesPanel.add(filesGrid.getWidget());

        LayoutContainer listUserPanel = createContainer();
        createListUserGrid(listUserPanel, filesGrid, context);

        loadListFileGrid(filesGrid, context);

        mainPanel.add(createUserPanel);
        mainPanel.add(listUserPanel);
        mainPanel.add(listFilesPanel);
        return mainPanel;
    }

    static private final void loadListFileGrid(GridWidget<AbstractFileGridModel> filesGrid,
            ViewContext context)
    {
        context.getCifexService().listFiles(new FileAdminAsyncCallback(context, filesGrid));
    }

    static private final void createListUserGrid(LayoutContainer listUserPanel,
            GridWidget<AbstractFileGridModel> filesGrid, ViewContext context)
    {
        context.getCifexService().listUsers(
                new UserAsyncCallback(listUserPanel, context, filesGrid));
    }

    private static final GridWidget<AbstractFileGridModel> createFileTable(
            final AdminFileInfoDTO[] files, ViewContext context)
    {
        List<AbstractFileGridModel> modelData =
                AdminFileGridModel.convert(context.getMessageResources(), Arrays.asList(files));
        List<ColumnConfig> columnConfigs =
                AdminFileGridModel.getColumnConfigs(context.getMessageResources());
        List<StoreFilterField<AbstractFileGridModel>> filterItems =
                AbstractFileGridModel.createFilterItems(context.getMessageResources());

        GridWidget<AbstractFileGridModel> gridWidget =
                GridUtils.createGrid(columnConfigs, modelData, filterItems, context
                        .getMessageResources());

        Grid<AbstractFileGridModel> grid = gridWidget.getGrid();
        grid.addListener(Events.CellClick, new FileDownloadGridCellListener());
        grid
                .addListener(Events.CellClick, new AdminFileActionGridCellListener(context,
                        gridWidget));
        grid.addListener(Events.CellClick, new FileCommentGridCellListener(context));
        return gridWidget;
    }

    /**
     * An {@link com.google.gwt.user.client.rpc.AsyncCallback} that creates a table with all users
     * on success.
     */
    private static final class UserAsyncCallback extends AbstractAsyncCallback<List<UserInfoDTO>>
    {

        private final LayoutContainer listUserPanel;

        private final ViewContext context;

        private final GridWidget<AbstractFileGridModel> filesGrid;

        UserAsyncCallback(LayoutContainer listUserPanel, ViewContext context,
                GridWidget<AbstractFileGridModel> filesGrid)
        {
            super(context);
            this.listUserPanel = listUserPanel;
            this.context = context;
            this.filesGrid = filesGrid;
        }

        private Widget createUserTable(final List<UserInfoDTO> users)
        {
            GridWidget<UserGridModel> uesrGridWidget = GridUtils.createUserGrid(users, context);
            // Delete user and change code function
            uesrGridWidget.getGrid().addListener(Events.CellClick,
                    new UserActionGridCellListener(context, filesGrid, uesrGridWidget));
            return uesrGridWidget.getWidget();
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

        private final GridWidget<AbstractFileGridModel> filesGrid;

        FileAdminAsyncCallback(ViewContext context, GridWidget<AbstractFileGridModel> filesGrid)
        {
            super(context);
            this.context = context;
            this.filesGrid = filesGrid;
        }

        public final void onSuccess(final List<AdminFileInfoDTO> result)
        {
            GridUtils.reloadStore(filesGrid, AdminFileGridModel.convert(context
                    .getMessageResources(), result));
        }
    }

}
