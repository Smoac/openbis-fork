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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;

/**
 * {@link StoreFilterField} extension for filtering columns.
 * 
 * @author Izabela Adamczyk
 */
public class ColumnFilter<T extends ModelData> extends StoreFilterField<T>
{

    private final String column;

    public ColumnFilter(Store<T> store, String columnName, String label)
    {
        this.column = columnName;
        setWidth(100);
        setEmptyText(label);
        bind(store);
    }

    @Override
    protected boolean doSelect(Store<T> store, T parent, T record, String property,
            String filterText)
    {
        String columnValue = record.get(column);
        columnValue = columnValue.toLowerCase();
        if (columnValue.startsWith(filterText.toLowerCase()))
        {
            return true;
        }
        return false;
    }
}
