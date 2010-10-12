/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific;

import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleDateRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DateTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;

/**
 * @author Franz-Josef Elmer
 */
public class GenericTableRowColumnDefinition implements IColumnDefinition<TableModelRow>
{
    protected GenericTableColumnHeader header;

    private String title;

    public GenericTableRowColumnDefinition(GenericTableColumnHeader header, String title)
    {
        this.header = header;
        this.title = title;
    }

    // GWT only
    @SuppressWarnings("unused")
    private GenericTableRowColumnDefinition()
    {
        this(null, null);
    }

    public Comparable<?> tryGetComparableValue(GridRowModel<TableModelRow> rowModel)
    {
        return getCellValue(rowModel);
    }

    public String getHeader()
    {
        return title;
    }

    public String getIdentifier()
    {
        return header.getCode();
    }

    public String getValue(GridRowModel<TableModelRow> rowModel)
    {
        ISerializableComparable value = getCellValue(rowModel);
        if (value instanceof DateTableCell)
        {
            return SimpleDateRenderer.renderDate(((DateTableCell) value).getDateTime());
        }
        return value == null ? "" : value.toString();
    }

    private ISerializableComparable getCellValue(GridRowModel<TableModelRow> rowModel)
    {
        return rowModel.getOriginalObject().getValues().get(header.getIndex());
    }

    public String tryToGetProperty(String key)
    {
        return null;
    }

}
