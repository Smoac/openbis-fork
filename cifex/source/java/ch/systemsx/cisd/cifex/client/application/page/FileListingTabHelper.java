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

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.i18n.client.NumberFormat;

import ch.systemsx.cisd.cifex.client.application.IQuotaInformationUpdater;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.shared.basic.Constants;

/**
 * A class with some methods that are useful for tabs that show lists of files.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class FileListingTabHelper
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

}
