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

package ch.systemsx.cisd.openbis.metadata;

/**
 * Describes well location.
 * 
 * @author Tomasz Pylak
 */
public class WellLocation
{
    private final String barcode, row, col;

    public WellLocation(String barcode, String row, String col)
    {
        this.barcode = barcode;
        this.row = row;
        this.col = col;
    }

    public String getBarcode()
    {
        return barcode;
    }

    public String getRow()
    {
        return row;
    }

    public String getCol()
    {
        return col;
    }

    @Override
    public boolean equals(Object o)
    {
        WellLocation loc = (WellLocation) o;
        return barcode.equals(loc.barcode) && row.equals(loc.row) && col.equals(loc.col);
    }

    @Override
    public int hashCode()
    {
        return barcode.hashCode() ^ row.hashCode() ^ col.hashCode();
    }

}