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
    static final String INDEX_PREFIX = "Index_";

    static final String MASKS_PREFIX = "Masks_";

    static final String EDGE_MASKS_PREFIX = "EdgeMasks_";

    private final ImageGeometry imageGeometry;
    
    String segmentedObjectTypeName;

    private final HDF5CompoundType<SegmentedObjectBox> indexType;

    CellLevelSegmentationDataset(IHDF5Reader reader, String datasetCode,
            ImageQuantityStructure quantityStructure, ImageGeometry imageGeometry)
    {
        super(reader, datasetCode, quantityStructure);
        this.imageGeometry = imageGeometry;
        this.indexType = reader.getInferredCompoundType(SegmentedObjectBox.class);
    }

    HDF5CompoundType<SegmentedObjectBox> getIndexType()
    {
        return indexType;
    }

    String getSegmentedObjectFileName()
    {
        return getObjectPath() + "/segmentedObjectType";
    }

    public CellLevelDatasetType getType()
    {
        return CellLevelDatasetType.SEGMENTATION;
    }

    public ImageGeometry getImageGeometry()
    {
        return imageGeometry;
    }

    public String getSegmentedObjectTypeName()
    {
        if (segmentedObjectTypeName == null)
        {
            segmentedObjectTypeName = reader.readString(getSegmentedObjectFileName());
        }
        return segmentedObjectTypeName;
    }

    public ICellLevelClassificationDataset toClassificationDataset()
    {
        throw new WrongDatasetTypeException(datasetCode, CellLevelDatasetType.CLASSIFICATION,
                CellLevelDatasetType.SEGMENTATION);
    }

    public ICellLevelFeatureDataset toFeatureDataset()
    {
        throw new WrongDatasetTypeException(datasetCode, CellLevelDatasetType.FEATURES,
                CellLevelDatasetType.SEGMENTATION);
    }

    public ICellLevelSegmentationDataset toSegmentationDataset()
    {
        return this;
    }

    public SegmentedObject getObject(ImageId wellId, int objectId, boolean withEdge)
    {
        final SegmentedObjectBox objectBox =
                reader.readCompoundArrayBlock(getObjectPath(wellId, INDEX_PREFIX), indexType,
                        1, objectId)[0];
        return getObject(wellId, objectBox, withEdge);
    }

    public SegmentedObject tryFindObject(ImageId wellId, int x, int y, boolean withEdge)
    {
        final String objectPath = getObjectPath(wellId, INDEX_PREFIX);
        final SegmentedObjectBox[] objectBoxes = reader.readCompoundArray(objectPath, indexType);
        for (int id = 0; id < objectBoxes.length; ++id)
        {
            final SegmentedObjectBox box = objectBoxes[id];
            if (box.inBox(x, y))
            {
                final int bitIndex = box.getAbsoluteBitIndex(x, y);
                if (reader.isBitSetInBitField(objectPath, bitIndex))
                {
                    return getObject(wellId, box, withEdge);
                }
            }
        }
        return null;
    }

    SegmentedObject getObject(ImageId wellId, final SegmentedObjectBox objectBox,
            boolean withEdge)
    {
        final BitSet mask =
                reader.readBitFieldBlockWithOffset(getObjectPath(wellId, MASKS_PREFIX),
                        objectBox.getSizeInWords(), objectBox.getOffsetInWords());
        final BitSet edgeMask;
        if (withEdge)
        {
            final String edgeMaskObjectPath = getObjectPath(wellId, EDGE_MASKS_PREFIX);
            if (reader.exists(edgeMaskObjectPath))
            {
                edgeMask =
                        reader.readBitFieldBlockWithOffset(edgeMaskObjectPath,
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
        return new SegmentedObject(objectBox, mask, edgeMask);
    }

    public SegmentedObject[] getObjects(ImageId wellId, boolean withEdge)
    {
        final String objectPath = getObjectPath(wellId, INDEX_PREFIX);
        final SegmentedObjectBox[] objectBoxes = reader.readCompoundArray(objectPath, indexType);
        final BitSet[] masks =
                getMasks(wellId, getObjectPath(wellId, MASKS_PREFIX), objectBoxes);
        final SegmentedObject[] results = new SegmentedObject[objectBoxes.length];
        if (withEdge)
        {
            final String edgeMasksPath = getObjectPath(wellId, EDGE_MASKS_PREFIX);
            final BitSet[] edgeMasks =
                    (reader.exists(edgeMasksPath)) ? getMasks(wellId, edgeMasksPath, objectBoxes)
                            : computeEdgeMasks(objectBoxes, masks);
            for (int i = 0; i < objectBoxes.length; ++i)
            {
                results[i] = new SegmentedObject(objectBoxes[i], masks[i], edgeMasks[i]);
            }
        } else
        {
            for (int i = 0; i < objectBoxes.length; ++i)
            {
                results[i] = new SegmentedObject(objectBoxes[i], masks[i]);
            }
        }
        return results;
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
            System.arraycopy(maskArray, offsetInWords, mArr, 0, sizeInWords);
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
