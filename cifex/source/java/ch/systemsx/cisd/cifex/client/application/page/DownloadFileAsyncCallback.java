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

import java.util.List;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.AbstractFileGridModel;
import ch.systemsx.cisd.cifex.client.application.DownloadFileGridModel;
import ch.systemsx.cisd.cifex.client.application.FileCommentGridCellListener;
import ch.systemsx.cisd.cifex.client.application.FileDownloadGridCellListener;
import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ServletPathConstants;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.grid.AbstractFilterField;
import ch.systemsx.cisd.cifex.client.application.grid.GridWidget;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;

final class DownloadFileAsyncCallback extends AbstractAsyncCallback<List<FileInfoDTO>>
{

    private final LayoutContainer contentPanel;

    private final ViewContext context;

    DownloadFileAsyncCallback(final ViewContext context, final LayoutContainer contentPanel)
    {
        super(context);
        this.context = context;
        this.contentPanel = contentPanel;
    }

    //
    // AbstractAsyncCallback
    //

    public final void onSuccess(final List<FileInfoDTO> files)
    {
        final Widget widget;
        IMessageResources messageResources = getMessageResources();
        if (files.size() > 0)
        {
            widget = createFileGrid(files, messageResources);
        } else
        {
            widget = createNoFilesLabel(messageResources);
        }
        final LayoutContainer verticalPanel = AbstractMainPageTabController.createContainer();
        AbstractMainPageTabController.addTitlePart(verticalPanel, createFileGridTitle());
        verticalPanel.add(widget);

        addWebStartDownloadClientLink(messageResources, verticalPanel);

        contentPanel.add(verticalPanel);
        contentPanel.layout();
    }

    private void addWebStartDownloadClientLink(IMessageResources messageResources,
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

    private Widget createFileGrid(final List<FileInfoDTO> files, IMessageResources messageResources)
    {
        List<ColumnConfig> columnConfigs;
        List<AbstractFileGridModel> data;
        columnConfigs = DownloadFileGridModel.getColumnConfigs(messageResources);
        data = DownloadFileGridModel.convert(messageResources, files);

        return createFileGrid(columnConfigs, data);
    }

    private Widget createFileGrid(List<ColumnConfig> columnConfigs, List<AbstractFileGridModel> data)
    {
        List<AbstractFilterField<AbstractFileGridModel>> filterItems =
                AbstractFileGridModel.createFilterItems(getMessageResources(), columnConfigs);

        GridWidget<AbstractFileGridModel> gridWidget =
                GridWidget.create(columnConfigs, data, filterItems, getMessageResources());
        Grid<AbstractFileGridModel> grid = gridWidget.getGrid();

        grid.addListener(Events.CellClick, new FileDownloadGridCellListener());
        grid.addListener(Events.CellClick, new FileCommentGridCellListener(context));

        return gridWidget.getWidget();
    }

    private IMessageResources getMessageResources()
    {
        return context.getMessageResources();
    }

    private String createFileGridTitle()
    {
        IMessageResources messageResources = getMessageResources();
        return messageResources.getDownloadFilesPartTitle();
    }

    private HTML createNoFilesLabel(IMessageResources messageResources)
    {
        final HTML html = new HTML();
        html.setText(messageResources.getDownloadFilesEmpty());
        return html;
    }
}