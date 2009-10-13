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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel.TableModelColumnType;

/**
 * Definition of dataset report table columns.
 * 
 * @author Tomasz Pylak
 */
public class DataSetReportColumnDefinition implements IColumnDefinition<TableModelRow>
{
    private TableModelColumnHeader columnHeader;

    public DataSetReportColumnDefinition(TableModelColumnHeader columnHeader)
    {
        this.columnHeader = columnHeader;
    }

    public Comparable<?> getComparableValue(GridRowModel<TableModelRow> rowModel)
    {
        TableModelColumnType type = columnHeader.getType();
        String value = getValue(rowModel);
        if (type == TableModelColumnType.REAL)
        {
            try
            {
                return StringUtils.isEmpty(value) ? Double.MIN_VALUE : new Double(value);
            } catch (NumberFormatException e)
            {
                return Double.MIN_VALUE;
            }
        } else if (type == TableModelColumnType.INTEGER)
        {
            try
            {
                return StringUtils.isEmpty(value) ? Long.MIN_VALUE : new Long(value);
            } catch (NumberFormatException e)
            {
                return Long.MIN_VALUE;
            }
        } else
        {
            return value;
        }
    }

    public String getHeader()
    {
        return columnHeader.getTitle();
    }

    public String getIdentifier()
    {
        return "colIndex_" + columnHeader.getIndex();
    }

    public String getValue(GridRowModel<TableModelRow> rowModel)
    {
        int index = columnHeader.getIndex();
        return rowModel.getOriginalObject().getValues().get(index);
    }

    public String tryToGetProperty(String key)
    {
        return null;
    }

    // GWT only
    @SuppressWarnings("unused")
    private DataSetReportColumnDefinition()
    {
    }

    // GWT only
    @SuppressWarnings("unused")
    private void setColumnHeader(TableModelColumnHeader columnHeader)
    {
        this.columnHeader = columnHeader;
    }

}
