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
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.hdf5.HDF5EnumerationType;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

/**
 * A reader for HCS cell level data.
 * 
 * @author Bernd Rinn
 */
class CellLevelDataReader implements ICellLevelDataReader
{
    private final IHDF5Reader reader;

    private final HDF5EnumerationType hdf5DatasetTypeEnum;

    private final boolean manageReader;

    CellLevelDataReader(File file)
    {
        this(HDF5Factory.open(file), true);
    }

    CellLevelDataReader(IHDF5Reader reader)
    {
        this(reader, false);
    }

    CellLevelDataReader(IHDF5Reader reader, boolean manageReader)
    {
        this.reader = reader;
        this.hdf5DatasetTypeEnum =
                reader.getEnumType("datasetTypes",
                        new String[]
                            { CellLevelDatasetType.SEGMENTATION.name(),
                                    CellLevelDatasetType.FEATURES.name(),
                                    CellLevelDatasetType.CLASSIFICATION.name() });
        this.manageReader = manageReader;

    }

    private CellLevelDatasetType getDatasetType(String datasetCode)
    {
        return CellLevelDatasetType.valueOf(reader.getEnumAttributeAsString(
                CellLevelDataset.getObjectPath(datasetCode), "datasetType"));
    }

    public List<ICellLevelDataset> getDataSets()
    {
        final List<String> codes = reader.getGroupMembers("/");
        final List<ICellLevelDataset> result = new ArrayList<ICellLevelDataset>(codes.size());
        for (String code : codes)
        {
            switch (getDatasetType(code))
            {
                case CLASSIFICATION:
                    result.add(new CellLevelClassificationDataset(reader, code, reader
                            .readCompound(CellLevelDataset.getGeometryObjectPath(code),
                                    WellFieldGeometry.class)));
                    break;
                case FEATURES:
                    result.add(new CellLevelFeatureDataset(reader, code, reader.readCompound(
                            CellLevelDataset.getGeometryObjectPath(code), WellFieldGeometry.class)));
                    break;
                case SEGMENTATION:
                    result.add(new CellLevelSegmentationDataset(reader, code, reader.readCompound(
                            CellLevelDataset.getGeometryObjectPath(code), WellFieldGeometry.class),
                            reader.readCompound(
                                    CellLevelSegmentationDataset.getImageGeometryObjectPath(code),
                                    ImageGeometry.class)));
                    break;
                default:
                    throw new Error("Unknown enum type.");
            }
        }
        return result;
    }

    public ICellLevelDataset getDataSet(String datasetCode)
    {
        switch (getDatasetType(datasetCode))
        {
            case CLASSIFICATION:
                return new CellLevelClassificationDataset(reader, datasetCode, reader.readCompound(
                        CellLevelDataset.getGeometryObjectPath(datasetCode),
                        WellFieldGeometry.class));
            case FEATURES:
                return new CellLevelFeatureDataset(reader, datasetCode, reader.readCompound(
                        CellLevelDataset.getGeometryObjectPath(datasetCode),
                        WellFieldGeometry.class));
            case SEGMENTATION:
                return new CellLevelSegmentationDataset(reader, datasetCode, reader.readCompound(
                        CellLevelDataset.getGeometryObjectPath(datasetCode),
                        WellFieldGeometry.class), reader.readCompound(
                        CellLevelSegmentationDataset.getImageGeometryObjectPath(datasetCode),
                        ImageGeometry.class));
            default:
                throw new Error("Unknown enum type.");
        }
    }

    HDF5EnumerationType getHdf5DatasetTypeEnum()
    {
        return hdf5DatasetTypeEnum;
    }

    public void close()
    {
        if (manageReader)
        {
            reader.close();
        }
    }

}
