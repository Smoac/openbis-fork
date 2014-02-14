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

import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.DOWNLOAD_FILES_EMPTY_MSG;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.DOWNLOAD_FILES_LOADING_MSG;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.DOWNLOAD_FILES_PANEL_TITLE;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.LIST_FILES_SHARED_EMPTY_MSG;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.LIST_FILES_SHARED_LOADING_MSG;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.LIST_FILES_SHARED_TITLE;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.msg;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.i18n.client.NumberFormat;

import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.FileCommentGridCellListener;
import ch.systemsx.cisd.cifex.client.application.FileDownloadGridCellListener;
import ch.systemsx.cisd.cifex.client.application.IQuotaInformationUpdater;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.grid.AbstractFilterField;
import ch.systemsx.cisd.cifex.client.application.grid.GridWidget;
import ch.systemsx.cisd.cifex.client.application.model.AbstractFileGridModel;
import ch.systemsx.cisd.cifex.client.application.model.DownloadFileGridModel;
import ch.systemsx.cisd.cifex.client.application.model.OwnedFileGridModel;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.OwnerFileInfoDTO;

/**
 * A class with some methods that are useful for tabs that show lists of files.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class FileListingTabHelper
{

    private static final long MB = 1024 * 1024;

    private static final NumberFormat FILE_SIZE_FORMAT = NumberFormat.getFormat("#.#");

    static final String getMaxFileSize(final Long maxFileSizeInMBOrNull)
    {
        if (maxFileSizeInMBOrNull == null)
        {
            return Constants.UNLIMITED_VALUE;
        } else
        {
            return maxFileSizeInMBOrNull + " MB";
        }
    }

    static final String getMaxFileCount(final Integer maxFileCountOrNull)
    {
        if (maxFileCountOrNull == null)
        {
            return Constants.UNLIMITED_VALUE;
        } else
        {
            return Integer.toString(maxFileCountOrNull);
        }
    }

    static final String getCurrentFileSizeInMB(final long currentFileSize)
    {
        return FILE_SIZE_FORMAT.format((double) currentFileSize / MB) + " MB";
    }

    static void createListDownloadFilesGrid(final ContentPanel contentPanel,
            final ViewContext context,
            final List<GridWidget<AbstractFileGridModel>> fileGridWidgets,
            final IQuotaInformationUpdater quotaUpdaterOrNull)
    {
        final List<ColumnConfig> columnConfigs = DownloadFileGridModel.getColumnConfigs();
        final List<AbstractFilterField<AbstractFileGridModel>> filterItems =
                AbstractFileGridModel.createFilterItems(columnConfigs);

        final GridWidget<AbstractFileGridModel> gridWidget =
                GridWidget.create(columnConfigs, new ArrayList<AbstractFileGridModel>(),
                        filterItems);
        final Grid<AbstractFileGridModel> grid = gridWidget.getGrid();
        grid.getView().setEmptyText(msg(DOWNLOAD_FILES_LOADING_MSG));

        fileGridWidgets.add(gridWidget);

        grid.addListener(Events.CellClick, new FileDownloadGridCellListener());
        grid.addListener(Events.CellClick, new FileCommentGridCellListener());

        AbstractMainPageTabController.addTitleRow(contentPanel, msg(DOWNLOAD_FILES_PANEL_TITLE));
        AbstractMainPageTabController.addWidgetRow(contentPanel, gridWidget.getWidget());

        context.getCifexService().listDownloadFiles(
                new AbstractAsyncCallback<List<FileInfoDTO>>(context)
                    {
                        @Override
                        public void onSuccess(List<FileInfoDTO> result)
                        {
                            grid.getView().setEmptyText(msg(DOWNLOAD_FILES_EMPTY_MSG));
                            gridWidget.setDataAndRefresh(DownloadFileGridModel.convert(result));
                        }

                        @Override
                        public void onFailure(Throwable caught)
                        {
                            grid.getView().setEmptyText(msg(DOWNLOAD_FILES_EMPTY_MSG));
                            super.onFailure(caught);
                        }
                    });
    }

    static void createListOwnedFilesGrid(final ViewContext context,
            final ContentPanel contentPanel,
            final List<GridWidget<AbstractFileGridModel>> fileGridWidgets,
            final IQuotaInformationUpdater quotaUpdaterOrNull)
    {
        final List<ColumnConfig> columnConfigs = OwnedFileGridModel.getColumnConfigs();
        final List<AbstractFilterField<AbstractFileGridModel>> filterItems =
                AbstractFileGridModel.createFilterItems(columnConfigs);

        final GridWidget<AbstractFileGridModel> gridWidget =
                GridWidget.create(columnConfigs, new ArrayList<AbstractFileGridModel>(),
                        filterItems);
        final Grid<AbstractFileGridModel> grid = gridWidget.getGrid();
        grid.getView().setEmptyText(msg(LIST_FILES_SHARED_LOADING_MSG));

        fileGridWidgets.add(gridWidget);

        grid.addListener(Events.CellClick, new FileDownloadGridCellListener());
        grid.addListener(Events.CellClick, new FileCommentGridCellListener());
        grid.addListener(Events.CellClick, new UploadedFileActionGridCellListener(context,
                gridWidget, fileGridWidgets, quotaUpdaterOrNull));
        AbstractMainPageTabController.addTitleRow(contentPanel, msg(LIST_FILES_SHARED_TITLE));
        AbstractMainPageTabController.addWidgetRow(contentPanel, gridWidget.getWidget());

        context.getCifexService().listOwnedFiles(
                new AbstractAsyncCallback<List<OwnerFileInfoDTO>>(context)
                    {
                        @Override
                        public void onSuccess(List<OwnerFileInfoDTO> result)
                        {
                            grid.getView().setEmptyText(msg(LIST_FILES_SHARED_EMPTY_MSG));
                            gridWidget.setDataAndRefresh(OwnedFileGridModel.convert(result));
                        }

                        @Override
                        public void onFailure(Throwable caught)
                        {
                            grid.getView().setEmptyText(msg(LIST_FILES_SHARED_EMPTY_MSG));
                            super.onFailure(caught);
                        }
                    });
    }
}
