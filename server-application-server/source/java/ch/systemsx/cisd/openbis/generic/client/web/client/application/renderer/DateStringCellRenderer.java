/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer;

import java.util.Date;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;

/**
 * @author Franz-Josef Elmer
 *
 */
public class DateStringCellRenderer implements GridCellRenderer<BaseEntityModel<?>>
{

    @Override
    public Object render(BaseEntityModel<?> model, String property, ColumnData config,
            int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store,
            Grid<BaseEntityModel<?>> grid)
    {
        Object obj = model.get(property);
        String originalValue = obj == null ? null : obj.toString();
        if (StringUtils.isBlank(originalValue))
        {
            return originalValue;
        } else
        {
            try
            {
                Date date = DateRenderer.SHORT_DATE_TIME_FORMAT.parse(originalValue);
                return DateRenderer.renderDate(date, DateRenderer.SHORT_DATE_FORMAT_PATTERN);
            } catch (Exception ex)
            {
                return originalValue;
            }
        }
    }

}
