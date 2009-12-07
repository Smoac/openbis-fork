package ch.systemsx.cisd.cifex.client.application;

import java.util.List;

import ch.systemsx.cisd.cifex.client.application.grid.AbstractFilterField;
import ch.systemsx.cisd.cifex.client.application.grid.GridWidget;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractFileListPage extends AbstractMainPage
{

    protected static final boolean DOWNLOAD = true;

    protected static final boolean UPLOAD = false;

    private static final long MB = 1024 * 1024;

    protected static final String getMaxFileSize(final Long maxFileSizeInMBOrNull)
    {
        if (maxFileSizeInMBOrNull == null)
        {
            return Constants.UNLIMITED_VALUE;
        } else
        {
            return maxFileSizeInMBOrNull + " MB";
        }
    }

    protected static final String getMaxFileCount(final Integer maxFileCountOrNull)
    {
        if (maxFileCountOrNull == null)
        {
            return Constants.UNLIMITED_VALUE;
        } else
        {
            return Integer.toString(maxFileCountOrNull);
        }
    }

    protected static final String getCurrentFileSizeInMB(final long currentFileSize)
    {
        NumberFormat fmt = NumberFormat.getDecimalFormat();
        return fmt.format((double) currentFileSize / MB) + " MB";
    }

    protected static void createListFilesGrid(final LayoutContainer contentPanel,
            final boolean showDownload, ViewContext context)
    {
        final FileAsyncCallback callback =
                new FileAsyncCallback(context, contentPanel, showDownload);
        if (showDownload)
        {
            context.getCifexService().listDownloadFiles(callback);
        } else
        {
            context.getCifexService().listOwnedFiles(callback);
        }
    }

    protected static final class FileAsyncCallback extends AbstractAsyncCallback<List<FileInfoDTO>>
    {

        private final LayoutContainer contentPanel;

        private final boolean showDownloaded;

        private final ViewContext context;

        FileAsyncCallback(final ViewContext context, final LayoutContainer contentPanel,
                final boolean showDownload)
        {
            super(context);
            this.context = context;
            this.contentPanel = contentPanel;
            this.showDownloaded = showDownload;
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
            final LayoutContainer verticalPanel = createContainer();
            addTitlePart(verticalPanel, createFileGridTitle());
            verticalPanel.add(widget);

            if (showDownloaded)
            {
                addWebStartDownloadClientLink(messageResources, verticalPanel);
            }

            contentPanel.add(verticalPanel);
            contentPanel.layout();
        }

        private void addWebStartDownloadClientLink(IMessageResources messageResources,
                final LayoutContainer verticalPanel)
        {
            addTitlePart(verticalPanel, messageResources.getDownloadFilesPartTitleGreater2GB());
            String webStartLink = messageResources.getDownloadFilesHelpJavaDownloaderLink();
            String webStartTitle = messageResources.getDownloadFilesHelpJavaDownloaderTitle();
            String anchorWebstart =
                    DOMUtils.createAnchor(webStartTitle, webStartLink,
                            ServletPathConstants.FILE2GB_DOWNLOAD_SERVLET_NAME, null, null, false);
            verticalPanel.add(new HTML(messageResources
                    .getDownloadFilesHelpJavaDownload(anchorWebstart)));
        }

        private Widget createFileGrid(final List<FileInfoDTO> files,
                IMessageResources messageResources)
        {
            List<ColumnConfig> columnConfigs;
            List<AbstractFileGridModel> data;
            if (showDownloaded)
            {
                columnConfigs = DownloadFileGridModel.getColumnConfigs(messageResources);
                data = DownloadFileGridModel.convert(messageResources, files);
            } else
            {
                columnConfigs = OwnedFileGridModel.getColumnConfigs(messageResources);
                data = OwnedFileGridModel.convert(messageResources, files);
            }

            return createFileGrid(columnConfigs, data);
        }

        private Widget createFileGrid(List<ColumnConfig> columnConfigs,
                List<AbstractFileGridModel> data)
        {
            List<AbstractFilterField<AbstractFileGridModel>> filterItems =
                    AbstractFileGridModel.createFilterItems(getMessageResources(), columnConfigs);

            GridWidget<AbstractFileGridModel> gridWidget =
                    GridWidget.create(columnConfigs, data, filterItems, getMessageResources());
            Grid<AbstractFileGridModel> grid = gridWidget.getGrid();

            grid.addListener(Events.CellClick, new FileDownloadGridCellListener());
            grid.addListener(Events.CellClick, new FileCommentGridCellListener(context));
            if (showDownloaded == false)
            {
                grid.addListener(Events.CellClick, new UploadedFileActionGridCellListener(context,
                        gridWidget));
            }
            return gridWidget.getWidget();
        }

        private IMessageResources getMessageResources()
        {
            return context.getMessageResources();
        }

        private String createFileGridTitle()
        {
            IMessageResources messageResources = getMessageResources();
            if (showDownloaded)
            {
                return messageResources.getDownloadFilesPartTitle();
            } else
            {
                return messageResources.getUploadedFilesPartTitle();
            }
        }

        private HTML createNoFilesLabel(IMessageResources messageResources)
        {
            final HTML html = new HTML();
            html.setText(showDownloaded ? messageResources.getDownloadFilesEmpty()
                    : messageResources.getUploadedFilesEmpty());
            return html;
        }
    }

    public AbstractFileListPage(ViewContext context)
    {
        super(context);
    }

}