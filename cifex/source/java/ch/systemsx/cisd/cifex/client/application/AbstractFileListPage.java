package ch.systemsx.cisd.cifex.client.application;

import ch.systemsx.cisd.cifex.shared.basic.Constants;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.i18n.client.NumberFormat;

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
            final boolean showDownload, ViewContext context,
            IQuotaInformationUpdater quotaUpdaterOrNull)
    {
        if (showDownload)
        {
            final DownloadFileAsyncCallback callback =
                    new DownloadFileAsyncCallback(context, contentPanel);
            context.getCifexService().listDownloadFiles(callback);
        } else
        {
            final OwnerFileAsyncCallback callback =
                    new OwnerFileAsyncCallback(context, contentPanel, quotaUpdaterOrNull);
            context.getCifexService().listOwnedFiles(callback);
        }
    }

    public AbstractFileListPage(ViewContext context)
    {
        super(context);
    }

}