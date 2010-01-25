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

package ch.systemsx.cisd.cifex.client.application.ui;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.cifex.client.application.utils.FileUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;

/**
 * A <code>Renderer</code> implementation for file size.
 * 
 * @author Christian Ribeaud
 */
public final class FileSizeRenderer implements GridCellRenderer<BaseModelData>
{

    public final static FileSizeRenderer FILE_SIZE_RENDERER = new FileSizeRenderer(false);

    public final static FileSizeRenderer FILE_SIZE_NULL_AS_MISSING_RENDERER =
            new FileSizeRenderer(true);

    private final boolean treatZeroAsNull;

    private FileSizeRenderer(boolean treatZeroAsNull)
    {
        this.treatZeroAsNull = treatZeroAsNull;
    }

    public Object render(BaseModelData model, String property, ColumnData config, int rowIndex,
            int colIndex, ListStore<BaseModelData> store, Grid<BaseModelData> grid)
    {
        final Number value = model.get(property);
        if (value == null)
        {
            return Constants.TABLE_NULL_VALUE;
        } 
        final long longValue = value.longValue();
        if (treatZeroAsNull && longValue == 0L)
        {
            return Constants.TABLE_NULL_VALUE;
        } else if (longValue == Long.MAX_VALUE)
        {
            return Constants.UNLIMITED_VALUE;
        } else
        {
            return FileUtils.byteCountToDisplaySize(longValue);
        }
    }

}
