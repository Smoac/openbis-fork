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

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Writer;

/**
 * A writer for HCS cell level data.
 * 
 * @author Bernd Rinn
 */
class CellLevelDataWriter extends CellLevelDataReader implements ICellLevelDataWriter
{
    private final IHDF5Writer writer;

    CellLevelDataWriter(File file)
    {
        this(HDF5Factory.open(file), true);
    }

    CellLevelDataWriter(IHDF5Writer writer)
    {
        this(writer, false);
    }

    CellLevelDataWriter(IHDF5Writer writer, boolean manageWriter)
    {
        super(writer, manageWriter, false);
        this.writer = writer;
        if (writer.hasAttribute("/", getCLDFormatTagAttributeName()) == false)
        {
            if (writer.getGroupMembers("/").isEmpty() == false)
            {
                throw new UnsupportedFileFormatException(
                        "File is HDF5, but doesn't have a proper CLD tag or version.");
            }
            writer.setStringAttribute("/", getCLDFormatTagAttributeName(),
                    EXPECTED_DESCRIPTOR.getFormatTag());
            writer.setIntAttribute("/", getCLDMajorVersionObjectPath(),
                    EXPECTED_DESCRIPTOR.getMajorVersion());
            writer.setIntAttribute("/", getCLDMinorVersionObjectPath(),
                    EXPECTED_DESCRIPTOR.getMinorVersion());
        }
    }

    public ICellLevelFeatureWritableDataset addFeatureDataset(String datasetCode,
            ImageQuantityStructure geometry)
    {
        return new CellLevelFeatureWritableDataset(writer, datasetCode, geometry, getHints(),
                getHdf5DatasetTypeEnum());
    }

    public ICellLevelClassificationWritableDataset addClassificationDataset(String datasetCode,
            ImageQuantityStructure geometry, Class<? extends Enum<?>> enumType)
    {
        return new CellLevelClassificationWritableDataset(writer, datasetCode, geometry,
                getHdf5DatasetTypeEnum(), enumType);
    }

    public ICellLevelClassificationWritableDataset addClassificationDataset(String datasetCode,
            ImageQuantityStructure geometry, List<String> options)
    {
        return new CellLevelClassificationWritableDataset(writer, datasetCode, geometry,
                getHdf5DatasetTypeEnum(), options);
    }

    public ICellLevelSegmentationWritableDataset addSegmentationDataset(String datasetCode,
            String segmentedObjectTypeName, ImageQuantityStructure geometry,
            ImageGeometry imageGeometry, boolean storeEdgeMasks)
    {
        return new CellLevelSegmentationWritableDataset(writer, datasetCode,
                segmentedObjectTypeName, geometry, imageGeometry, getHdf5DatasetTypeEnum(),
                storeEdgeMasks);
    }
}
