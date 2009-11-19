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

package ch.systemsx.cisd.cifex.client.application.ui;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;

import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;

/**
 * A field to filter grid rows by the value of one specified column.
 * 
 * @author Tomasz Pylak
 */
public class ContainFilterField<M extends ModelData> extends StoreFilterField<M>
{

    /**
     * @param filteredPropertyKey id of the grid column
     * @param title title of the filter
     */
    public ContainFilterField(String filteredPropertyKey, String title)
    {
        super();
        setProperty(filteredPropertyKey);
        setEmptyText(title);
    }

    @Override
    protected boolean doSelect(Store<M> store, M parent, M record, String property,
            final String filterText)
    {
        StoreFilter<M> storeFilter = createStoreFilter(filterText);
        return storeFilter.select(store, parent, record, property);
    }

    private static <M extends ModelData> StoreFilter<M> createStoreFilter(final String filterText)
    {
        return new StoreFilter<M>()
            {
                public boolean select(Store<M> store, M parent, M item, String property)
                {
                    if (StringUtils.isBlank(filterText))
                    {
                        return true;
                    }
                    String rawValue = (String) item.get(property);
                    return (rawValue != null && rawValue.toLowerCase().contains(
                            filterText.toLowerCase()));
                }
            };
    }

}
