/*
 * Copyright 2013 ETH Zuerich, Scientific IT Services
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
package ch.ethz.sis.hcscld;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * An identifier for an image well. Wells in an HCS screen are identified by:
 * <ul>
 * <li>Row</li>
 * <li>Column</li>
 * </ul>
 * All indices start with 0.
 *
 * @author Bernd Rinn
 */
public class WellId
{
    protected final int row;

    protected final int column;

    public WellId(int row, int column)
    {
        this.row = row;
        this.column = column;
    }

    /**
     * Returns the row index of the well (0-based).
     */
    public int getRow()
    {
        return row;
    }

    /**
     * Returns the column index of the well (0-based).
     */
    public int getColumn()
    {
        return column;
    }
    
    /**
     * Return the list of well ids from a given list of image ids. 
     */
    public static WellId[] getWells(ImageId[] imageIds)
    {
        final Set<WellId> wellIdSet = new HashSet<WellId>();
        for (ImageId id : imageIds)
        {
            wellIdSet.add(new WellId(id.getRow(), id.getColumn()));
        }
        final WellId[] wellIds = wellIdSet.toArray(new WellId[wellIdSet.size()]);
        Arrays.sort(wellIds, new Comparator<WellId>()
            {
                @Override
                public int compare(WellId o1, WellId o2)
                {
                    if (o1.row < o2.row)
                    {
                        return -1;
                    } else if (o1.row > o2.row)
                    {
                        return 1;
                    }
                    if (o1.column < o2.column)
                    {
                        return -1;
                    } else if (o1.column > o2.column)
                    {
                        return 1;
                    }
                    return 0;
                }
            });
        return wellIds;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + column;
        result = prime * result + row;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final WellId other = (WellId) obj;
        if (column != other.column)
        {
            return false;
        }
        if (row != other.row)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "WellId [row=" + row + ", column=" + column + "]";
    }

}
