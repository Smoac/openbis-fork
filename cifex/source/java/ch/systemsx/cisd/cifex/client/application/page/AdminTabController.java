/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.FileCommentGridCellListener;
import ch.systemsx.cisd.cifex.client.application.FileDownloadGridCellListener;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.IHistoryController.Page;
import ch.systemsx.cisd.cifex.client.application.grid.AbstractFilterField;
import ch.systemsx.cisd.cifex.client.application.grid.GridUtils;
import ch.systemsx.cisd.cifex.client.application.grid.GridWidget;
import ch.systemsx.cisd.cifex.client.application.model.AbstractFileGridModel;
import ch.systemsx.cisd.cifex.client.application.model.AdminFileGridModel;
import ch.systemsx.cisd.cifex.client.application.model.UserGridModel;
import ch.systemsx.cisd.cifex.shared.basic.dto.OwnerFileInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * @author Chandrasekhar Ramakrishnan
 */
class AdminTabController extends AbstractMainPageTabController
{

    /**
     * @param context
     */
    public AdminTabController(ViewContext context,
            final List<GridWidget<AbstractFileGridModel>> fileGridWidgets)
    {
        super(context, fileGridWidgets);
    }

    @Override
    protected Widget getWidget()
    {
        final ContentPanel mainPanel = createOutermostWidgetContainer();
        ContentPanel createUserPanel = createUserPanel(true, context);
        addWidgetRow(mainPanel, createUserPanel);

        GridWidget<AbstractFileGridModel> filesGrid =
                createFileTable(new OwnerFileInfoDTO[0], context, fileGridWidgets);
        addTitleRow(mainPanel, msg(LIST_FILES_TITLE));
        addWidgetRow(mainPanel, filesGrid.getWidget());

        createListUserGrid(mainPanel, filesGrid, context);
        loadListFileGrid(filesGrid, context);

        return mainPanel;
    }

    static private final void loadListFileGrid(GridWidget<AbstractFileGridModel> filesGrid,
            ViewContext context)
    {
        context.getCifexService().listFiles(new FileAdminAsyncCallback(context, filesGrid));
    }

    static private final void createListUserGrid(ContentPanel listUserPanel,
            GridWidget<AbstractFileGridModel> filesGrid, ViewContext context)
    {
        addTitleRow(listUserPanel, msg(LIST_USERS_GRID_TITLE));
        final GridWidget<UserGridModel> userGridWidget =
                GridUtils.createUserGrid(new ArrayList<UserInfoDTO>(), true, context);
        userGridWidget.getGrid().getView().setEmptyText(msg(LIST_USERS_LOADING_MSG));
        // Delete user and change code function
        userGridWidget.getGrid().addListener(Events.CellClick,
                new UserActionGridCellListener(context, filesGrid, userGridWidget));
        addWidgetRow(listUserPanel, userGridWidget.getWidget());
        context.getCifexService().listUsers(new UserAsyncCallback(context, userGridWidget));
    }

    private static final GridWidget<AbstractFileGridModel> createFileTable(
            final OwnerFileInfoDTO[] files, ViewContext context,
            final List<GridWidget<AbstractFileGridModel>> fileGridWidgets)
    {
        List<AbstractFileGridModel> modelData = AdminFileGridModel.convert(Arrays.asList(files));
        List<ColumnConfig> columnConfigs = AdminFileGridModel.getColumnConfigs();
        List<AbstractFilterField<AbstractFileGridModel>> filterItems =
                AbstractFileGridModel.createFilterItems(columnConfigs);

        GridWidget<AbstractFileGridModel> gridWidget =
                GridWidget.create(columnConfigs, modelData, filterItems);

        Grid<AbstractFileGridModel> grid = gridWidget.getGrid();
        grid.getView().setEmptyText(msg(LIST_FILES_LOADING_MSG));

        fileGridWidgets.add(gridWidget);

        grid.addListener(Events.CellClick, new FileDownloadGridCellListener());
        grid.addListener(Events.CellClick, new AdminFileActionGridCellListener(context, gridWidget,
                fileGridWidgets));
        grid.addListener(Events.CellClick, new FileCommentGridCellListener());
        return gridWidget;
    }

    /**
     * An {@link com.google.gwt.user.client.rpc.AsyncCallback} that creates a table with all users
     * on success.
     */
    private static final class UserAsyncCallback extends AbstractAsyncCallback<List<UserInfoDTO>>
    {

        private final ViewContext context;

        private final GridWidget<UserGridModel> userGrid;

        UserAsyncCallback(ViewContext context, GridWidget<UserGridModel> userGrid)
        {
            super(context);
            this.context = context;
            this.userGrid = userGrid;
        }

        public final void onSuccess(final List<UserInfoDTO> result)
        {
            userGrid.getGrid().getView().setEmptyText(msg(LIST_USERS_EMPTY_MSG));
            userGrid.setDataAndRefresh(UserGridModel.convert(context, result));
        }

        @Override
        public void onFailure(Throwable caught)
        {
            userGrid.getGrid().getView().setEmptyText(msg(LIST_USERS_EMPTY_MSG));
            super.onFailure(caught);
        }
    }

    /**
     * An {@link com.google.gwt.user.client.rpc.AsyncCallback} that creates a table with all files
     * on success.
     */
    private static final class FileAdminAsyncCallback extends
            AbstractAsyncCallback<List<OwnerFileInfoDTO>>
    {

        private final GridWidget<AbstractFileGridModel> filesGrid;

        FileAdminAsyncCallback(ViewContext context, GridWidget<AbstractFileGridModel> filesGrid)
        {
            super(context);
            this.filesGrid = filesGrid;
        }

        public final void onSuccess(final List<OwnerFileInfoDTO> result)
        {
            filesGrid.getGrid().getView().setEmptyText(msg(LIST_FILES_EMPTY_MSG));
            filesGrid.setDataAndRefresh(AdminFileGridModel.convert(result));
        }

        @Override
        public void onFailure(Throwable caught)
        {
            filesGrid.getGrid().getView().setEmptyText(msg(LIST_FILES_EMPTY_MSG));
            super.onFailure(caught);
        }
    }

    @Override
    protected Page getPageIdentifier()
    {
        return Page.ADMIN_PAGE;
    }

}
