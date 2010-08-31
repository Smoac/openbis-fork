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

package eu.basysbio.cisd.dss;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.etlserver.utils.Column;


/**
 * @author Franz-Josef Elmer
 */
class Util
{
    static int parseIntegerWithPlusSign(String number)
    {
        return Integer.parseInt(number.startsWith("+") ? number.substring(1) : number);
    }
    
    static Double parseDouble(String number)
    {
        double value = Double.parseDouble(number);
        return Double.isNaN(value) ? null : value;
    }
    
    static Integer parseInteger(Column column, int rowIndex)
    {
        String value = getValueAt(column, rowIndex);
        try
        {
            return StringUtils.isBlank(value) ? null : new Integer(value);
        } catch (NumberFormatException e)
        {
            throw new IllegalArgumentException((rowIndex + 1) + " data point of column '"
                    + column.getHeader() + "' is not an integer number: " + value);
        }
    }

    static Double parseDouble(Column column, int rowIndex)
    {
        String value = getValueAt(column, rowIndex);
        try
        {
            return parseDouble(value);
        } catch (NumberFormatException e)
        {
            throw new IllegalArgumentException((rowIndex + 1) + " data point of column '"
                    + column.getHeader() + "' is not a floating-point number: " + value);
        }
    }
    
    private static String getValueAt(Column column, int rowIndex)
    {
        List<String> values = column.getValues();
        if (rowIndex >= values.size())
        {
            throw new IllegalArgumentException("Column '" + column.getHeader() + "' has only "
                    + values.size() + " data points instead of " + (rowIndex + 1) + " or more.");
        }
        return values.get(rowIndex);
    }
}
