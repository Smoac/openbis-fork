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

package ch.systemsx.cisd.openbis.generic.shared.util;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;

/**
 * Utility functions for table cells.
 *
 * @author Franz-Josef Elmer
 */
public class TableCellUtil
{
    /**
     * Returns an appropriate table cell for the specified string token. If it can be parsed as
     * an integer number a {@link IntegerTableCell} is returned. If it can be parsed as a floating
     * point number a {@link DoubleTableCell} is returned. Otherwise a {@link StringTableCell}
     * is returned.
     */
    public static ISerializableComparable createTableCell(String token)
    {
        try
        {
            return new IntegerTableCell(Long.parseLong(token));
        } catch (NumberFormatException ex)
        {
            try
            {
                return new DoubleTableCell(Double.parseDouble(token));
            } catch (NumberFormatException ex1)
            {
                // ignored
            }
        }
        return new StringTableCell(token);
    }
    
    private TableCellUtil()
    {
    }

}
