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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.StoreFilterField;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;

/**
 * {@link StoreFilterField} extension for filtering columns in cached grid with paging.
 * 
 * @author Tomasz Pylak
 */
public class PagingColumnFilter<T/* entity */> extends StoreFilterField<ModelData>
{
    private final IColumnDefinition<T> filteredField;

    public PagingColumnFilter(IColumnDefinition<T> filteredField)
    {
        this.filteredField = filteredField;
        setWidth(100);
        String label = filteredField.getHeader() + "...";
        setEmptyText(label);
    }

    /** @return filter with the pattern or null if pattern was not specified by the user */
    public GridFilterInfo<T> tryGetFilter()
    {
        String pattern = getRawValue();
        if (pattern != null && pattern.length() > 0)
        {
            return new GridFilterInfo<T>(filteredField, pattern);
        } else
        {
            return null;
        }
    }

    /** We do not use this method, data are filtered on the server side */
    @Override
    protected boolean doSelect(Store<ModelData> store, ModelData parent, ModelData record,
            String property, String filterText)
    {
        return true;
    }
}
