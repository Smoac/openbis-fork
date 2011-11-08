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

import ch.systemsx.cisd.hdf5.CompoundElement;

/**
 * A descriptor for the cell-level dataset type.
 * 
 * @author Bernd Rinn
 */
public class CellLevelDatasetTypeDescriptor
{
    private CellLevelDatasetType datasetType;

    @CompoundElement(dimensions =
        { 50 })
    private String formatType;

    private int formatVersionNumber;

    // Used by HDF5 when reading
    CellLevelDatasetTypeDescriptor()
    {
    }

    CellLevelDatasetTypeDescriptor(CellLevelDatasetType datasetType, String formatType,
            int formatVersionNumber, CellLevelBaseWritableDataset writeableDataset)
    {
        this.datasetType = datasetType;
        this.formatType = formatType;
        this.formatVersionNumber = formatVersionNumber;
    }

    public CellLevelDatasetType getDatasetType()
    {
        return datasetType;
    }

    public String getFormatType()
    {
        return formatType;
    }

    public int getFormatVersionNumber()
    {
        return formatVersionNumber;
    }

}
