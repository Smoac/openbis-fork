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

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.cifex.client.application.model.FileShareUserGridModel;

/**
 * GridCell action listener for link for sharing file with a user.
 * 
 * @author Izabela Adamczyk
 */
public final class FileShareUserGridCellListener implements Listener<GridEvent<ModelData>>
{

    private final AbstractFileShareUserDialog parentDialog;

    FileShareUserGridCellListener(AbstractFileShareUserDialog parentDialog)
    {
        this.parentDialog = parentDialog;
    }

    public void handleEvent(GridEvent<ModelData> be)
    {
        Grid<ModelData> grid = be.getGrid();
        int rowIndex = be.getRowIndex();
        int colIndex = be.getColIndex();
        final ModelData record = grid.getStore().getAt(rowIndex);
        final Element element = be.getTarget();
        if (element == null)
        {
            return;
        }
        if (grid.getColumnModel().getDataIndex(colIndex).equals(FileShareUserGridModel.SHARE_FILE)
                && be.getTarget(".checkbox", 1) != null)
        {
            record.set(FileShareUserGridModel.SHARE_FILE, false == (Boolean) record
                    .get(FileShareUserGridModel.SHARE_FILE));

            parentDialog.checkboxChangeAction();
        }
    }
}