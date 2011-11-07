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
import ch.systemsx.cisd.hdf5.CompoundType;
import ch.systemsx.cisd.hdf5.HDF5EnumerationType;
import ch.systemsx.cisd.hdf5.HDF5EnumerationValue;

/**
 * A descriptor for the cell-level dataset type.
 * 
 * @author Bernd Rinn
 */
@CompoundType(mapAllFields = false)
public class CellLevelDatasetTypeDescriptor
{
    private CellLevelDatasetType datasetType;

    @CompoundElement
    private HDF5EnumerationValue datasetTypePersistentOrNull;

    @CompoundElement(dimensions =
        { 50 })
    private String formatType;

    @CompoundElement
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
        writeToDataset(writeableDataset);
    }

    private void writeToDataset(CellLevelBaseWritableDataset writeableDataset)
    {
        if (datasetTypePersistentOrNull == null)
        {
            final HDF5EnumerationType enumType =
                    writeableDataset.addEnumGlobal("CellLevelDatasetType",
                            CellLevelDatasetType.class);
            datasetTypePersistentOrNull = new HDF5EnumerationValue(enumType, datasetType.ordinal());
        }
        writeableDataset.writer.createGroup(writeableDataset.getObjectPath());
        writeableDataset.writer.setCompoundAttribute(writeableDataset.getObjectPath(),
                CellLevelBaseWritableDataset.getDatasetTypeAttributeName(), this);
    }

    public CellLevelDatasetType getDatasetType()
    {
        if (datasetType == null && datasetTypePersistentOrNull != null)
        {
            datasetType = CellLevelDatasetType.values()[datasetTypePersistentOrNull.getOrdinal()];
        }
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
