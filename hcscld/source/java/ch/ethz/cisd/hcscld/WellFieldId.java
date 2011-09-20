/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.cisd.hcscld;

/**
 * An identifier for a field of a well in an HCS screen.
 * 
 * @author Bernd Rinn
 */
public class WellFieldId
{
    private final int row;

    private final int column;

    private final int field;

    public WellFieldId(int row, int column, int field)
    {
        this.row = row;
        this.column = column;
        this.field = field;
    }

    public int getRow()
    {
        return row;
    }

    public int getColumn()
    {
        return column;
    }

    public int getField()
    {
        return field;
    }

    String createObjectName(String prefixOrNull)
    {
        return (prefixOrNull == null) ? String.format("Row%d_col%d_field%d", row, column, field)
                : String.format("%s_row%d_col%d_field%d", prefixOrNull, row, column, field);
    }

    @Override
    public String toString()
    {
        return "WellFieldId [row=" + row + ", column=" + column + ", field=" + field + "]";
    }

}
