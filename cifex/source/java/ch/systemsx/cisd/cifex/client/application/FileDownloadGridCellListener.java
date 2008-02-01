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

import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.Record;
import com.gwtext.client.widgets.grid.Grid;
import com.gwtext.client.widgets.grid.event.GridCellListenerAdapter;

import ch.systemsx.cisd.cifex.client.application.utils.WindowUtils;
import ch.systemsx.cisd.cifex.client.dto.File;

/**
 * A <code>GridCellListener</code> implementation for the {@link File} table.
 * 
 * @author Christian Ribeaud
 */
final class FileDownloadGridCellListener extends GridCellListenerAdapter
{

    FileDownloadGridCellListener()
    {
    }

    private final static String createDownloadUrl(final long id)
    {
        return Constants.FILE_DOWNLOAD_SERVLET_NAME + "?" + Constants.FILE_ID_PARAMETER + "=" + id;
    }

    //
    // GridCellListenerAdapter
    //

    public final void onCellClick(final Grid grid, final int rowIndex, final int colIndex, final EventObject e)
    {
        final String dataIndex = grid.getColumnModel().getDataIndex(colIndex);
        if (dataIndex.equals(AbstractFileGridModel.NAME))
        {
            final Record record = grid.getStore().getAt(rowIndex);
            final int id = record.getAsInteger(AbstractFileGridModel.ID);
            final String url = createDownloadUrl(id);
            WindowUtils.openNewDependentWindow(url);
        }
    }
}