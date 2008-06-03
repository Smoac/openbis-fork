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

import com.google.gwt.user.client.Element;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.Record;
import com.gwtext.client.widgets.grid.Grid;
import com.gwtext.client.widgets.grid.event.GridCellListenerAdapter;

import ch.systemsx.cisd.cifex.client.application.model.FileShareUserGridModel;

/**
 * GridCell action listener for link for sharing file with a user.
 * 
 * @author Izabela Adamczyk
 */
final class FileShareUserGridCellListener extends GridCellListenerAdapter
{

    private final AbstractFileShareUserDialog parentDialog;

    FileShareUserGridCellListener(AbstractFileShareUserDialog parentDialog)
    {
        this.parentDialog = parentDialog;
    }

    //
    // GridCellListenerAdapter
    //

    public final void onCellClick(final Grid grid, final int rowIndex, final int colIndex,
            final EventObject e)
    {
        final Record record = grid.getStore().getAt(rowIndex);
        final Element element = e.getTarget();
        if (element == null)
        {
            return;
        }
        if (grid.getColumnModel().getDataIndex(colIndex).equals(FileShareUserGridModel.SHARE_FILE)
                && e.getTarget(".checkbox", 1) != null)
        {
            record.set(FileShareUserGridModel.SHARE_FILE, !record
                    .getAsBoolean(FileShareUserGridModel.SHARE_FILE));

            parentDialog.checkboxChangeAction();
        }
    }
}