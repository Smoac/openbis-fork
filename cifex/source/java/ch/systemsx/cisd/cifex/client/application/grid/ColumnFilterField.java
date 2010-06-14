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

import ch.systemsx.cisd.cifex.client.application.utils.ObjectUtils;
import ch.systemsx.cisd.common.shared.basic.AlternativesStringFilter;

/**
 * A field to filter grid rows by the value of one specified column.
 * 
 * @see AlternativesStringFilter
 * @author Tomasz Pylak
 */
public class ColumnFilterField<M extends ModelData> extends AbstractFilterField<M>
{
    private final AlternativesStringFilter filter;

    private final GridCellRenderer<ModelData> rendererOrNull;

    public ColumnFilterField(String filterPropertyKey, String title,
            GridCellRenderer<ModelData> rendererOrNull)
    {
        super(filterPropertyKey, title);
        this.rendererOrNull = rendererOrNull;
        this.filter = new AlternativesStringFilter();
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
        return filter.passes(getValue(record));
    }

    private String getValue(ModelData record)
    {
        final String renderedText;
        if (rendererOrNull == null)
        {
            renderedText = ObjectUtils.toString(record.get(getProperty())).toLowerCase();
        } else
        {
            renderedText =
                    ((String) rendererOrNull.render(record, getProperty(), null, 0, 0, null, null))
                            .toLowerCase();
        }
        return renderedText;
    }
}
