/*
 * Copyright 2012 ETH Zuerich, CISD
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

import ncsa.hdf.hdf5lib.exceptions.HDF5SymbolTableException;

import ch.systemsx.cisd.hdf5.IHDF5Reader;

/**
 * Dataset of object tracking.
 * 
 * @author Bernd Rinn
 */
public class CellLevelTrackingDataset extends CellLevelDataset implements ICellLevelTrackingDataset
{
    static final String FORMAT_TYPE = "2DLinkArray";

    static final int CURRENT_FORMAT_VERSION_NUMBER = 1;

    static final String OBJECT_TRACKING_TYPE_DATASET_NAME = "ObjectTrackingTypes";

    private final ObjectTrackingTypes objectTrackingTypes;

    CellLevelTrackingDataset(IHDF5Reader reader, String datasetCode,
            ImageQuantityStructure quantityStructure, String formatType, int formatVersionNumber)
    {
        super(reader, datasetCode, quantityStructure, formatVersionNumber);
        checkFormat(FORMAT_TYPE, formatType);
        final String objectTrackingTypePath = getObjectPath(OBJECT_TRACKING_TYPE_DATASET_NAME);
        this.objectTrackingTypes =
                new ObjectTrackingTypes(
                        reader.exists(objectTrackingTypePath) ? reader.readCompoundArray(
                                objectTrackingTypePath, ObjectTrackingType.class)
                                : new ObjectTrackingType[0]);
        for (ObjectTrackingType type : objectTrackingTypes.list())
        {
            type.setDataset(this);
        }
    }

    @Override
    public CellLevelDatasetType getType()
    {
        return CellLevelDatasetType.TRACKING;
    }

    @Override
    public ICellLevelFeatureDataset toFeatureDataset() throws WrongDatasetTypeException
    {
        throw new WrongDatasetTypeException(datasetCode, CellLevelDatasetType.TRACKING,
                CellLevelDatasetType.FEATURES);
    }

    @Override
    public ICellLevelClassificationDataset toClassificationDataset()
            throws WrongDatasetTypeException
    {
        throw new WrongDatasetTypeException(datasetCode, CellLevelDatasetType.TRACKING,
                CellLevelDatasetType.CLASSIFICATION);
    }

    @Override
    public ICellLevelSegmentationDataset toSegmentationDataset() throws WrongDatasetTypeException
    {
        throw new WrongDatasetTypeException(datasetCode, CellLevelDatasetType.TRACKING,
                CellLevelDatasetType.SEGMENTATION);
    }

    @Override
    public ICellLevelTrackingDataset toTrackingDataset() throws WrongDatasetTypeException
    {
        return this;
    }

    @Override
    public ObjectTrackingTypes getObjectTrackingTypes()
    {
        return objectTrackingTypes;
    }

    @Override
    public ObjectTracking getObjectTracking(ImageSequenceId imageSequenceId,
            ObjectTrackingType objectTrackingType)
    {
        if (equals(objectTrackingType.getDataset()) == false)
        {
            throw new WrongDatasetException(datasetCode,
                    objectTrackingType.getDataset().datasetCode);
        }
        try
        {
            return new ObjectTracking(objectTrackingType, reader.int32().readMDArray(objectTrackingType
                    .getObjectPath(imageSequenceId)));
        } catch (HDF5SymbolTableException ex)
        {
            if ("Object not found".equals(ex.getMinorError()))
            {
                throw new IllegalArgumentException("No object tracking for " + objectTrackingType
                        + " and " + imageSequenceId + ".");
            } else
            {
                throw ex;
            }
        }
    }

    @Override
    public boolean hasObjectTracking(ImageSequenceId imageSequenceId,
            ObjectTrackingType objectTrackingType) throws IllegalArgumentException
    {
        if (equals(objectTrackingType.getDataset()) == false)
        {
            throw new WrongDatasetException(datasetCode,
                    objectTrackingType.getDataset().datasetCode);
        }
        return reader.exists(objectTrackingType.getObjectPath(imageSequenceId));
    }

}
