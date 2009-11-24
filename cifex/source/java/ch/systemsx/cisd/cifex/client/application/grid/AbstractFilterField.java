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
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;

import ch.systemsx.cisd.cifex.client.application.utils.IDelegatedAction;

/**
 * Abstract superclass for grid filters which filter the whole store externally.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractFilterField<M extends ModelData> extends StoreFilterField<M>
{
    public abstract boolean isMatching(M record);

    private IDelegatedAction onFilterAction;

    /**
     * @param filteredPropertyKey id of the grid column
     * @param title title of the filter
     */
    public AbstractFilterField(String filteredPropertyKey, String title)
    {
        super();
        this.onFilterAction = IDelegatedAction.EMPTY_ACTION;
        setProperty(filteredPropertyKey);
        setEmptyText(title);
    }

    @Override
    protected boolean doSelect(Store<M> store, M parent, M record, String property,
            final String filterText)
    {
        // not used, the default filter filters items on each grid page, leaving some pages
        // potentially empty. We delegate filtering to the specified delegator, see bind method.
        return true;
    }

    /** binds to the action which will be executed when filtering will be requested */
    public void bind(IDelegatedAction onFilterActionParam)
    {
        this.onFilterAction = onFilterActionParam;
    }

    @Override
    protected void onFilter()
    {
        super.onFilter();
        onFilterAction.execute();
    }
}
