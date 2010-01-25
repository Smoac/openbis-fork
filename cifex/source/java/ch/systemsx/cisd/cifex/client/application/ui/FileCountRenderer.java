/*
 * Copyright 2010 ETH Zuerich, CISD
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

import ch.systemsx.cisd.cifex.shared.basic.Constants;

/**
 * A renderer for the (custom) file count quota.
 * 
 * @author Bernd Rinn
 */
public class FileCountRenderer implements GridCellRenderer<BaseModelData>
{

    public final static FileCountRenderer FILE_COUNT_RENDERER =
            new FileCountRenderer();

    private FileCountRenderer()
    {
        // Can not be instantiated.
    }

    public Object render(BaseModelData model, String property, ColumnData config, int rowIndex,
            int colIndex, ListStore<BaseModelData> store, Grid<BaseModelData> grid)
    {
        final Integer value = model.get(property);
        if (value == null || value == 0)
        {
            return Constants.TABLE_NULL_VALUE;
        } else if (value == Integer.MAX_VALUE)
        {
            return Constants.UNLIMITED_VALUE;
        } else
        {
            return value.toString();
        }
    }

}
