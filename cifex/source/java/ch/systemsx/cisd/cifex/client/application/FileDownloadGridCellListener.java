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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.cifex.client.application.model.AbstractFileGridModel;
import ch.systemsx.cisd.cifex.client.application.ui.FileDownloadHelper;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.cifex.client.application.utils.WindowUtils;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;

/**
 * A <code>GridCellListener</code> implementation for the {@link FileInfoDTO} table.
 * 
 * @author Christian Ribeaud
 */
public final class FileDownloadGridCellListener implements Listener<GridEvent<ModelData>>
{

    public FileDownloadGridCellListener()
    {
    }

    public void handleEvent(GridEvent<ModelData> be)
    {
        Grid<ModelData> grid = be.getGrid();
        int colIndex = be.getColIndex();
        int rowIndex = be.getRowIndex();
        final String dataIndex = grid.getColumnModel().getDataIndex(colIndex);
        if (dataIndex.equals(AbstractFileGridModel.NAME))
        {
            final Element element = be.getTarget();
            if (element == null)
            {
                return;
            }
            final String targetId = DOM.getElementAttribute(element, "href");
            if (StringUtils.isBlank(targetId) == false)
            {
                ModelData record = grid.getStore().getAt(rowIndex);
                final long id = record.get(AbstractFileGridModel.ID);
                final String url = FileDownloadHelper.createDownloadUrl(id);
                WindowUtils.openNewDependentWindow(url);
            }

        }

    }
}