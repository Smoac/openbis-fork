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

package ch.systemsx.cisd.cifex.client.application.grid;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

/**
 * A field to filter grid rows by the value of one specified column.
 * 
 * @see ColumnFilter
 * 
 * @author Tomasz Pylak
 */
public class ColumnFilterField<M extends ModelData> extends AbstractFilterField<M>
{
    private final ColumnFilter filter;
    
    public ColumnFilterField(String filterPropertyKey, String title,
            GridCellRenderer<ModelData> rendererOrNull)
    {
        super(filterPropertyKey, title);
        this.filter = new ColumnFilter(rendererOrNull, filterPropertyKey);
    }

    @Override
    protected void onFilter()
    {
        filter.setFilterValue(getRawValue());
        super.onFilter();
    }

    @Override
    public boolean isMatching(M record)
    {
        return filter.passes(record);
    }

}
