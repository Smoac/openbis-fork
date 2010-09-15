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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;

/**
 * Creates list of lines with tab separated columns;
 * 
 * @author Tomasz Pylak
 */
public class TSVRenderer
{
    private static final String TAB = "\t";

    private final String lineSeparator;

    /**
     * @param entities list of entities which will be exported
     * @param list column definitions. Each definition know column's header and is able to extract
     *            an appropriate value from the entity.
     * @param lineSeparator character used as a lineSeparator separator
     */
    public static <T> String createTable(List<GridRowModel<T>> entities,
            List<IColumnDefinition<T>> list, String lineSeparator)
    {
        return new TSVRenderer(lineSeparator).createTable(entities, list);
    }

    private <T> String createTable(List<GridRowModel<T>> entities, List<IColumnDefinition<T>> list)
    {
        StringBuffer sb = new StringBuffer();
        appendHeader(list, sb);
        for (GridRowModel<T> entity : entities)
        {
            appendEntity(entity, list, sb);
        }
        return sb.toString();
    }

    private TSVRenderer(String lineSeparator)
    {
        this.lineSeparator = lineSeparator;
    }

    private <T> void appendEntity(GridRowModel<T> entity, List<IColumnDefinition<T>> list,
            StringBuffer sb)
    {
        boolean isFirst = true;
        for (IColumnDefinition<T> column : list)
        {
            if (isFirst == false)
            {
                sb.append(TAB);
            } else
            {
                isFirst = false;
            }
            Comparable<?> value = column.tryGetComparableValue(entity);
            sb.append(cleanWhitespaces(value == null ? "" : value.toString()));
        }
        sb.append(lineSeparator);
    }

    /**
     * @return <var>value</var> with white-spaces cleaned in the same way that HTML works (all
     *         contiguous white-spaces are replaced with single space)
     */
    private String cleanWhitespaces(String value)
    {
        String[] tokens = StringUtils.split(value);
        String result = StringUtils.join(tokens, " ");
        return result;
    }

    private <T> void appendHeader(List<IColumnDefinition<T>> columnDefs, StringBuffer sb)
    {
        boolean isFirst = true;
        for (IColumnDefinition<T> column : columnDefs)
        {
            if (isFirst == false)
            {
                sb.append(TAB);
            } else
            {
                isFirst = false;
            }
            sb.append(column.getHeader());
        }
        sb.append(lineSeparator);
    }
}
