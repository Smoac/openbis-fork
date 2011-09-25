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

    private final HDF5CompoundType<SegmentedObjectBox> indexType;

    final class ImageSegmentation implements IImageSegmentation
    {
        ImageSegmentation(String name)
        {
            this.name = name;
        }

        private final String name;

        public String getName()
        {
            return name;
        }

        String getObjectPath()
        {
            return CellLevelSegmentationDataset.this.getObjectPath() + "/Segmentation_" + name;
        }

        String getObjectPath(WellFieldId id, String prefix)
        {
            return CellLevelSegmentationDataset.this.getObjectPath() + "/Segmentation_" + name
                    + "/" + id.createObjectName(prefix);
        }

    }

    CellLevelSegmentationDataset(IHDF5Reader reader, String datasetCode,
            WellFieldGeometry geometry, ImageGeometry imageGeometry)
    {
        super(reader, datasetCode, geometry);
        this.imageGeometry = imageGeometry;
        this.indexType = reader.getInferredCompoundType(SegmentedObjectBox.class);
    }

    HDF5CompoundType<SegmentedObjectBox> getIndexType()
    {
        return indexType;
    }

    String getSegmentationsFilename()
    {
        return getObjectPath() + "/imageSegmentations";
    }

    public CellLevelDatasetType getType()
    {
        return CellLevelDatasetType.SEGMENTATION;
    }

    public ImageGeometry getImageGeometry()
    {
        return imageGeometry;
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

    public IImageSegmentation getSegmentation(String name)
    {
        final String segmentationFile = getSegmentationsFilename();
        final String[] segmentations = reader.readStringArray(segmentationFile);
        for (String s : segmentations)
        {
            if (s.equals(name))
            {
                return new ImageSegmentation(name);
            }
        }
        throw new IllegalArgumentException("No segmentation '" + name + "' found.");
    }

    public SegmentedObject getObject(WellFieldId wellId, IImageSegmentation segmentation,
            int objectId, boolean withEdge)
    {
        final ImageSegmentation seg = (ImageSegmentation) segmentation;
        final SegmentedObjectBox objectBox =
                reader.readCompoundArrayBlock(seg.getObjectPath(wellId, INDEX_PREFIX), indexType,
                        1, objectId)[0];
        return getObject(wellId, seg, objectBox, withEdge);
    }

    public SegmentedObject tryFindObject(WellFieldId wellId, IImageSegmentation segmentation,
            int x, int y, boolean withEdge)
    {
        final ImageSegmentation seg = (ImageSegmentation) segmentation;
        final String objectPath = seg.getObjectPath(wellId, INDEX_PREFIX);
        final SegmentedObjectBox[] objectBoxes = reader.readCompoundArray(objectPath, indexType);
        for (int id = 0; id < objectBoxes.length; ++id)
        {
            final SegmentedObjectBox box = objectBoxes[id];
            if (box.inBox(x, y))
            {
                final int bitIndex = box.getAbsoluteBitIndex(x, y);
                if (reader.isBitSetInBitField(objectPath, bitIndex))
                {
                    return getObject(wellId, seg, box, withEdge);
                }
            }
        }
        return null;
    }

    SegmentedObject getObject(WellFieldId wellId, final ImageSegmentation seg,
            final SegmentedObjectBox objectBox, boolean withEdge)
    {
        final BitSet mask =
                reader.readBitFieldBlockWithOffset(seg.getObjectPath(wellId, MASKS_PREFIX),
                        objectBox.getSizeInWords(), objectBox.getOffsetInWords());
        final BitSet edgeMask;
        if (withEdge)
        {
            final String edgeMaskObjectPath = seg.getObjectPath(wellId, EDGE_MASKS_PREFIX);
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

    public SegmentedObject[] getObjects(WellFieldId wellId, IImageSegmentation segmentation,
            boolean withEdge)
    {
        final ImageSegmentation seg = (ImageSegmentation) segmentation;
        final String objectPath = seg.getObjectPath(wellId, INDEX_PREFIX);
        final SegmentedObjectBox[] objectBoxes = reader.readCompoundArray(objectPath, indexType);
        final BitSet[] masks =
                getMasks(wellId, seg.getObjectPath(wellId, MASKS_PREFIX), objectBoxes);
        final SegmentedObject[] results = new SegmentedObject[objectBoxes.length];
        if (withEdge)
        {
            final String edgeMasksPath = seg.getObjectPath(wellId, EDGE_MASKS_PREFIX);
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

    BitSet[] getMasks(WellFieldId wellId, String objectPath, SegmentedObjectBox[] objectBoxes)
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
        return getObjectPath(datasetCode) + "/imageGeometry";
    }

}
