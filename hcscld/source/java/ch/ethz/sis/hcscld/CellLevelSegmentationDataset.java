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

package ch.ethz.sis.hcscld;

import java.util.BitSet;

import ch.systemsx.cisd.hdf5.BitSetConversionUtils;
import ch.systemsx.cisd.hdf5.HDF5CompoundType;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

/**
 * Implementation of {@link ICellLevelSegmentationDataset}.
 * 
 * @author Bernd Rinn
 */
class CellLevelSegmentationDataset extends CellLevelDataset implements
        ICellLevelSegmentationDataset
{
    static final String FORMAT_TYPE = "BitFieldWithCompoundArrayIndex";

    static final int CURRENT_FORMAT_VERSION_NUMBER = 1;

    static final String INDEX_PREFIX = "Index";

    static final String MASKS_PREFIX = "Masks";

    static final String EDGE_MASKS_PREFIX = "EdgeMasks";

    private final ImageGeometry imageGeometry;

    private final HDF5CompoundType<SegmentedObjectBox> indexType;

    CellLevelSegmentationDataset(IHDF5Reader reader, String datasetCode,
            ImageQuantityStructure quantityStructure, ImageGeometry imageGeometry,
            String formatType, int formatVersionNumber)
    {
        super(reader, datasetCode, quantityStructure, formatVersionNumber);
        checkFormat(FORMAT_TYPE, formatType);
        this.imageGeometry = imageGeometry;
        this.indexType = reader.compound().getInferredType(SegmentedObjectBox.class);
    }

    HDF5CompoundType<SegmentedObjectBox> getIndexType()
    {
        return indexType;
    }

    @Override
    public CellLevelDatasetType getType()
    {
        return CellLevelDatasetType.SEGMENTATION;
    }

    @Override
    public ImageGeometry getImageGeometry()
    {
        return imageGeometry;
    }

    @Override
    public ICellLevelClassificationDataset toClassificationDataset()
    {
        throw new WrongDatasetTypeException(datasetCode, CellLevelDatasetType.CLASSIFICATION,
                CellLevelDatasetType.SEGMENTATION);
    }

    @Override
    public ICellLevelFeatureDataset toFeatureDataset()
    {
        throw new WrongDatasetTypeException(datasetCode, CellLevelDatasetType.FEATURES,
                CellLevelDatasetType.SEGMENTATION);
    }

    @Override
    public ICellLevelTrackingDataset toTrackingDataset() throws WrongDatasetTypeException
    {
        throw new WrongDatasetTypeException(datasetCode, CellLevelDatasetType.TRACKING,
                CellLevelDatasetType.SEGMENTATION);
    }

    @Override
    public ICellLevelSegmentationDataset toSegmentationDataset()
    {
        return this;
    }

    @Override
    public SegmentedObject getObject(ImageId wellId, ObjectType objectType, int objectId,
            boolean withEdge)
    {
        final SegmentedObjectBox objectBox =
                reader.compound().readArrayBlock(
                        getObjectPath(wellId, INDEX_PREFIX, objectType.getId()), indexType, 1,
                        objectId)[0];
        return getObject(wellId, objectType, objectId, objectBox, withEdge);
    }

    @Override
    public SegmentedObject tryFindObject(ImageId wellId, ObjectType objectType, int x, int y,
            boolean withEdge)
    {
        final String objectPath = getObjectPath(wellId, INDEX_PREFIX, objectType.getId());
        final SegmentedObjectBox[] objectBoxes = reader.compound().readArray(objectPath, indexType);
        for (int id = 0; id < objectBoxes.length; ++id)
        {
            final SegmentedObjectBox box = objectBoxes[id];
            if (box.inBox(x, y))
            {
                final int bitIndex = box.getAbsoluteBitIndex(x, y);
                if (reader.bool().isBitSet(
                        getObjectPath(wellId, MASKS_PREFIX, objectType.getId()), bitIndex))
                {
                    return getObject(wellId, objectType, id, box, withEdge);
                }
            }
        }
        return null;
    }

    @Override
    public SegmentedObject tryFindObject(ImageId wellId, int x, int y, boolean withEdge)
    {
        for (ObjectType objectType : getObjectTypes())
        {
            final SegmentedObject objectOrNull = tryFindObject(wellId, objectType, x, y, withEdge);
            if (objectOrNull != null)
            {
                return objectOrNull;
            }
        }
        return null;
    }

    SegmentedObject getObject(ImageId wellId, ObjectType objectType, int objectIndex,
            SegmentedObjectBox objectBox, boolean withEdge)
    {
        final BitSet mask =
                reader.bool().readBitFieldBlockWithOffset(
                        getObjectPath(wellId, MASKS_PREFIX, objectType.getId()),
                        objectBox.getSizeInWords(), objectBox.getOffsetInWords());
        final BitSet edgeMask;
        if (withEdge)
        {
            final String edgeMaskObjectPath =
                    getObjectPath(wellId, EDGE_MASKS_PREFIX, objectType.getId());
            if (reader.exists(edgeMaskObjectPath))
            {
                edgeMask =
                        reader.bool().readBitFieldBlockWithOffset(edgeMaskObjectPath,
                                objectBox.getSizeInWords(), objectBox.getOffsetInWords());
            } else
            {
                final CannyEdgeDetector edgeDetector = new CannyEdgeDetector();
                edgeDetector.setSourceMask(mask, 0, objectBox.getWidth(), objectBox.getHeight());
                edgeDetector.process();
                edgeMask = edgeDetector.getEdgeMask(false);

            }
        } else
        {
            edgeMask = null;
        }
        return new SegmentedObject(objectBox, objectIndex, mask, edgeMask);
    }

    @Override
    public SegmentedObject[] getObjects(ImageId wellId, ObjectType objectType, boolean withEdge)
    {
        final String objectPath = getObjectPath(wellId, INDEX_PREFIX, objectType.getId());
        final SegmentedObjectBox[] objectBoxes = reader.compound().readArray(objectPath, indexType);
        final BitSet[] masks =
                getMasks(wellId, getObjectPath(wellId, MASKS_PREFIX, objectType.getId()),
                        objectBoxes);
        final SegmentedObject[] results = new SegmentedObject[objectBoxes.length];
        if (withEdge)
        {
            final String edgeMasksPath =
                    getObjectPath(wellId, EDGE_MASKS_PREFIX, objectType.getId());
            final BitSet[] edgeMasks =
                    (reader.exists(edgeMasksPath)) ? getMasks(wellId, edgeMasksPath, objectBoxes)
                            : computeEdgeMasks(objectBoxes, masks);
            for (int id = 0; id < objectBoxes.length; ++id)
            {
                results[id] = new SegmentedObject(objectBoxes[id], id, masks[id], edgeMasks[id]);
            }
        } else
        {
            for (int id = 0; id < objectBoxes.length; ++id)
            {
                results[id] = new SegmentedObject(objectBoxes[id], id, masks[id]);
            }
        }
        return results;
    }

    @Override
    public boolean hasObjects(ImageId wellId, ObjectType objectType)
    {
        return reader.exists(getObjectPath(wellId, INDEX_PREFIX, objectType.getId()));
    }

    BitSet[] computeEdgeMasks(final SegmentedObjectBox[] objectBoxes, final BitSet[] masks)
    {
        final BitSet[] edgeMasks;
        edgeMasks = new BitSet[objectBoxes.length];
        for (int i = 0; i < objectBoxes.length; ++i)
        {
            final CannyEdgeDetector edgeDetector = new CannyEdgeDetector();
            edgeDetector.setSourceMask(masks[i], 0, objectBoxes[i].getWidth(),
                    objectBoxes[i].getHeight());
            edgeDetector.process();
            edgeMasks[i] = edgeDetector.getEdgeMask(false);
        }
        return edgeMasks;
    }

    BitSet[] getMasks(ImageId wellId, String objectPath, SegmentedObjectBox[] objectBoxes)
    {
        final long[] maskArray =
                BitSetConversionUtils.toStorageForm(reader.readBitField(objectPath));
        final BitSet[] result = new BitSet[objectBoxes.length];
        for (int i = 0; i < objectBoxes.length; ++i)
        {
            final int sizeInWords = objectBoxes[i].getSizeInWords();
            final int offsetInWords = objectBoxes[i].getOffsetInWords();
            final long[] mArr = new long[sizeInWords];
            System.arraycopy(maskArray, offsetInWords, mArr, 0,
                    Math.min(sizeInWords, maskArray.length - offsetInWords));
            result[i] = BitSetConversionUtils.fromStorageForm(mArr);
        }
        return result;
    }

    String getImageGeometryObjectPath()
    {
        return getImageGeometryObjectPath(datasetCode);
    }

    static String getImageGeometryObjectPath(String datasetCode)
    {
        return getDatasetPath(datasetCode) + "/imageGeometry";
    }

}
