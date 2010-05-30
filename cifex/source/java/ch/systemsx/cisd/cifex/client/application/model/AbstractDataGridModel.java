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

package ch.systemsx.cisd.cifex.client.application.model;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

import ch.systemsx.cisd.cifex.client.application.grid.AbstractFilterField;
import ch.systemsx.cisd.cifex.client.application.grid.ContainFilterField;
import ch.systemsx.cisd.cifex.shared.basic.Constants;

/**
 * Abstract data grid model with convenient methods.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractDataGridModel extends BaseModelData implements IModelDataWithID
{
    private static final long serialVersionUID = Constants.VERSION;

    public static final String ID = "id";

    protected final static ColumnConfig createSortableColumnConfig(final String code,
            final String title, final int width)
    {
        final ColumnConfig columnConfig = createColumnConfig(code, title, width);
        columnConfig.setSortable(true);
        return columnConfig;
    }

    protected final static ColumnConfig createIdColumnConfig()
    {
        final ColumnConfig columnConfig = createSortableColumnConfig(ID, "Id", 20);
        columnConfig.setHidden(true);
        columnConfig.setFixed(true);
        return columnConfig;
    }

    protected final static ColumnConfig createColumnConfig(final String code, final String title,
            final int width)
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setDataIndex(code);
        columnConfig.setId(code);
        columnConfig.setHeader(title);
        columnConfig.setWidth(width);
        columnConfig.setMenuDisabled(true);
        return columnConfig;
    }

    protected static <M extends ModelData> List<AbstractFilterField<M>> createFilterItems(
            List<ColumnConfig> columnConfigs, List<String> initialFilters)
    {
        List<AbstractFilterField<M>> filterFields = new ArrayList<AbstractFilterField<M>>();
        for (ColumnConfig cc : columnConfigs)
        {
            if (false == cc.isHidden() || false == cc.isFixed())
            {
                ContainFilterField<M> field =
                        new ContainFilterField<M>(cc.getId(), cc.getHeader(), cc.getRenderer());
                boolean initiallyVisible = initialFilters.contains(cc.getId());
                field.setVisible(initiallyVisible);
                field.setEnabled(initiallyVisible);
                filterFields.add(field);
            }
        }
        return filterFields;
    }
}
