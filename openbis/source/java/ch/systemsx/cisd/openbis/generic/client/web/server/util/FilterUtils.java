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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ParameterWithValue;
import ch.systemsx.cisd.openbis.generic.client.web.server.calculator.RowCalculator;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;

/**
 * Utility class containing functions helpful with dealing with filters.
 * 
 * @author Izabela Adamczyk
 */
public class FilterUtils
{
    /**
     * Applies the filter described by <code>customFilterInfo</code> to
     * <code>allRows<code> and adds the result to the
     * <code>filterdRows<code>.
     */
    public static <T> void applyCustomFilter(final List<T> allRows,
            Set<IColumnDefinition<T>> availableColumns, CustomFilterInfo<T> customFilterInfo,
            List<T> filterdRows)
    {
        String expression = StringEscapeUtils.unescapeHtml(customFilterInfo.getExpression());
        Set<ParameterWithValue> parameters = customFilterInfo.getParameters();
        RowCalculator<T> calculator =
                new RowCalculator<T>(availableColumns, expression, parameters);
        for (T rowData : allRows)
        {
            calculator.setRowData(rowData);
            if (calculator.evalToBoolean())
            {
                filterdRows.add(rowData);
            }
        }
    }

}
