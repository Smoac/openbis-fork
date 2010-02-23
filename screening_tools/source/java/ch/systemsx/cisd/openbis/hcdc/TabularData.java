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

package ch.systemsx.cisd.openbis.hcdc;

/**
 * Simple table to store e.g. tsv file content.
 * 
 * @author Tomasz Pylak
 */
public class TabularData
{
    private final String[] headers;

    private final boolean[] isNumeric;

    private final String[][] rows;

    public TabularData(String[] header, String[][] rows, boolean[] isNumeric)
    {
        this.headers = header;
        this.isNumeric = isNumeric;
        this.rows = rows;
    }

    public String[] getHeader()
    {
        return headers;
    }

    public String[][] getRows()
    {
        return rows;
    }

    public int getColumnIndex(String columnName)
    {
        for (int i = 0; i < headers.length; i++)
        {
            if (headers[i].equalsIgnoreCase(columnName))
            {
                return i;
            }
        }
        return -1;
    }

    public boolean isNumericColumn(int index)
    {
        return isNumeric[index];
    }
}
