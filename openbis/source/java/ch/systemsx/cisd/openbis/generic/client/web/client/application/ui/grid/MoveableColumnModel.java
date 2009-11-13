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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.List;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;

/**
 * {@link ColumnModel} extension allowing to move columns.
 * 
 * @author Izabela Adamczyk
 */
public class MoveableColumnModel extends ColumnModel
{

    public MoveableColumnModel(List<ColumnConfig> columns)
    {
        super(columns);
    }

    /**
     * Moves the column from one position to another.
     * 
     * @param fromIndex the old column index
     * @param toIndex the new column index
     */
    public void move(int fromIndex, int toIndex)
    {
        if (fromIndex != toIndex)
        {
            ColumnConfig c = configs.get(fromIndex);
            configs.remove(fromIndex);
            configs.add(toIndex, c);
            // one event is triggered when all columns are set
        }
    }

    /**
     * Removes the column from the specified position.
     */
    public void remove(int fromIndex)
    {
        configs.remove(fromIndex);
    }

    /**
     * Adds the column at the specified position.
     */
    public void addAt(int index, ColumnConfig columnConfig)
    {
        configs.add(index, columnConfig);
    }

}
