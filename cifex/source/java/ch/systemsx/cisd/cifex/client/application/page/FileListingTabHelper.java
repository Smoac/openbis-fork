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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.HTML;

import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.FileCommentGridCellListener;
import ch.systemsx.cisd.cifex.client.application.FileDownloadGridCellListener;
import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.IQuotaInformationUpdater;
import ch.systemsx.cisd.cifex.client.application.ServletPathConstants;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.grid.AbstractFilterField;
import ch.systemsx.cisd.cifex.client.application.grid.GridWidget;
import ch.systemsx.cisd.cifex.client.application.model.AbstractFileGridModel;
import ch.systemsx.cisd.cifex.client.application.model.DownloadFileGridModel;
import ch.systemsx.cisd.cifex.client.application.model.OwnedFileGridModel;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
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

    static void createListDownloadFilesGrid(final LayoutContainer contentPanel,
            ViewContext context, final List<GridWidget<AbstractFileGridModel>> fileGridWidgets,
            IQuotaInformationUpdater quotaUpdaterOrNull)
    {
        final IMessageResources messageResources = context.getMessageResources();
        final List<ColumnConfig> columnConfigs =
                DownloadFileGridModel.getColumnConfigs(messageResources);
        final List<AbstractFilterField<AbstractFileGridModel>> filterItems =
                AbstractFileGridModel.createFilterItems(messageResources, columnConfigs);

        final GridWidget<AbstractFileGridModel> gridWidget =
                GridWidget.create(columnConfigs, new ArrayList<AbstractFileGridModel>(),
                        filterItems, messageResources);
        final Grid<AbstractFileGridModel> grid = gridWidget.getGrid();
        grid.getView().setEmptyText(messageResources.getDownloadFilesLoading());

        fileGridWidgets.add(gridWidget);

        grid.addListener(Events.CellClick, new FileDownloadGridCellListener());
        grid.addListener(Events.CellClick, new FileCommentGridCellListener(context));

        final LayoutContainer verticalPanel = AbstractMainPageTabController.createContainer();
        AbstractMainPageTabController.addTitlePart(verticalPanel, messageResources
                .getDownloadFilesPartTitle());
        verticalPanel.add(gridWidget.getWidget());

        addWebStartDownloadClientLink(messageResources, verticalPanel);

        contentPanel.add(verticalPanel);
        contentPanel.layout();

        context.getCifexService().listDownloadFiles(
                new AbstractAsyncCallback<List<FileInfoDTO>>(context)
                    {
                        public void onSuccess(List<FileInfoDTO> result)
                        {
                            grid.getView().setEmptyText(messageResources.getDownloadFilesEmpty());
                            gridWidget.setDataAndRefresh(DownloadFileGridModel.convert(
                                    messageResources, result));
                        }

                        @Override
                        public void onFailure(Throwable caught)
                        {
                            grid.getView().setEmptyText(messageResources.getDownloadFilesEmpty());
                            super.onFailure(caught);
                        }
                    });
    }

    private static void addWebStartDownloadClientLink(IMessageResources messageResources,
            final LayoutContainer verticalPanel)
    {
        AbstractMainPageTabController.addTitlePart(verticalPanel, messageResources
                .getDownloadFilesPartTitleGreater2GB());
        String webStartLink = messageResources.getDownloadFilesHelpJavaDownloaderLink();
        String webStartTitle = messageResources.getDownloadFilesHelpJavaDownloaderTitle();
        String anchorWebstart =
                DOMUtils.createAnchor(webStartTitle, webStartLink,
                        ServletPathConstants.FILE2GB_DOWNLOAD_SERVLET_NAME, null, null, false);
        verticalPanel.add(new HTML(messageResources
                .getDownloadFilesHelpJavaDownload(anchorWebstart)));
    }

    static void createListOwnedFilesGrid(ViewContext context, final LayoutContainer contentPanel,
            final List<GridWidget<AbstractFileGridModel>> fileGridWidgets,
            IQuotaInformationUpdater quotaUpdaterOrNull)
    {
        final IMessageResources messageResources = context.getMessageResources();
        final List<ColumnConfig> columnConfigs =
                OwnedFileGridModel.getColumnConfigs(messageResources);
        final List<AbstractFilterField<AbstractFileGridModel>> filterItems =
                AbstractFileGridModel.createFilterItems(messageResources, columnConfigs);

        final GridWidget<AbstractFileGridModel> gridWidget =
                GridWidget.create(columnConfigs, new ArrayList<AbstractFileGridModel>(),
                        filterItems, messageResources);
        final Grid<AbstractFileGridModel> grid = gridWidget.getGrid();
        grid.getView().setEmptyText(messageResources.getSharedFilesLoading());

        fileGridWidgets.add(gridWidget);

        grid.addListener(Events.CellClick, new FileDownloadGridCellListener());
        grid.addListener(Events.CellClick, new FileCommentGridCellListener(context));
        grid.addListener(Events.CellClick, new UploadedFileActionGridCellListener(context,
                gridWidget, fileGridWidgets, quotaUpdaterOrNull));
        AbstractMainPageTabController.addTitlePart(contentPanel, messageResources
                .getSharedFilesPartTitle());
        contentPanel.add(gridWidget.getWidget());
        context.getCifexService().listOwnedFiles(
                new AbstractAsyncCallback<List<OwnerFileInfoDTO>>(context)
                    {
                        public void onSuccess(List<OwnerFileInfoDTO> result)
                        {
                            grid.getView().setEmptyText(messageResources.getSharedFilesEmpty());
                            gridWidget.setDataAndRefresh(OwnedFileGridModel.convert(
                                    messageResources, result));
                        }

                        @Override
                        public void onFailure(Throwable caught)
                        {
                            grid.getView().setEmptyText(messageResources.getSharedFilesEmpty());
                            super.onFailure(caught);
                        }
                    });
    }
}
