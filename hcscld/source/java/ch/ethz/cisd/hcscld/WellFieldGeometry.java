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

import java.util.Iterator;

/**
 * The geometry of wells and fields of an HCS dataset.
 * 
 * @author Bernd Rinn
 */
public class WellFieldGeometry implements Iterable<WellFieldId>
{
    private int numberOfRows;

    private int numberOfColumns;

    private int numberOfFields;

    // Used by JHDF5 when constructing the geometry from a compound. 
    WellFieldGeometry()
    {
    }

    /**
     * Creates a new geometry with the given well rows, well columns and fields per well.
     *
     * @param numberOfRows The number of well rows of the HCS plate.
     * @param numberOfColumns The number of well columns of the HCS plate.
     * @param numberOfFields The number of fields per well of the HCS dataset.
     */
    public WellFieldGeometry(int numberOfRows, int numberOfColumns, int numberOfFields)
    {
        super();
        this.numberOfRows = numberOfRows;
        this.numberOfColumns = numberOfColumns;
        this.numberOfFields = numberOfFields;
    }

    public Iterator<WellFieldId> iterator()
    {
        return WellFieldRunner.iterator(WellFieldGeometry.this, null);
    }

    /**
     * Returns the number of well rows of an HCS plate.
     */
    public int getNumberOfRows()
    {
        return numberOfRows;
    }

    /**
     * Returns the number of well columns of an HCS plate.
     */
    public int getNumberOfColumns()
    {
        return numberOfColumns;
    }

    /**
     * Returns the number of fields (images) per well of an HCS plate.
     */
    public int getNumberOfFields()
    {
        return numberOfFields;
    }

}
