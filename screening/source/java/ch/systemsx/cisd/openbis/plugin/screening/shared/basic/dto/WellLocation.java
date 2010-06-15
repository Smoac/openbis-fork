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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Describes position of the well on the plate.
 * 
 * @author Tomasz Pylak
 */
public class WellLocation implements IsSerializable
{
    private int row;

    private int column;

    // GWT only
    @SuppressWarnings("unused")
    private WellLocation()
    {
    }

    public WellLocation(int row, int column)
    {
        this.row = row;
        this.column = column;
    }

    /** Starts with 1 */
    public int getRow()
    {
        return row;
    }

    /** Starts with 1 */
    public int getColumn()
    {
        return column;
    }

    @Override
    public String toString()
    {
        return "(" + row + "," + column + ")";
    }

}
