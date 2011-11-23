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

import java.util.List;

import ch.systemsx.cisd.hdf5.BitSetConversionUtils;
import ch.systemsx.cisd.hdf5.HDF5EnumerationType;
import ch.systemsx.cisd.hdf5.HDF5GenericStorageFeatures;
import ch.systemsx.cisd.hdf5.HDF5TimeDurationArray;
import ch.systemsx.cisd.hdf5.IHDF5Writer;

/**
 * Implementation of {@link ICellLevelSegmentationWritableDataset}
 * 
 * @author Bernd Rinn
 */
class CellLevelSegmentationWritableDataset extends CellLevelSegmentationDataset implements
        ICellLevelSegmentationWritableDataset
{
    private final CellLevelBaseWritableDataset base;

    private final boolean storeEdgeMasks;

    CellLevelSegmentationWritableDataset(final IHDF5Writer writer, final String datasetCode,
            final String segmentedObjectType, final ImageQuantityStructure quantityStructure,
            final ImageGeometry imageGeometry, final HDF5EnumerationType hdf5KindEnum,
            final boolean storeEdgeMasks)
    {
        super(writer, datasetCode, quantityStructure, imageGeometry, FORMAT_TYPE,
                CURRENT_FORMAT_VERSION_NUMBER);
        this.base =
                new CellLevelBaseWritableDataset(writer, datasetCode, quantityStructure,
                        hdf5KindEnum, CellLevelDatasetType.SEGMENTATION, FORMAT_TYPE,
                        CURRENT_FORMAT_VERSION_NUMBER);
        this.storeEdgeMasks = storeEdgeMasks;
        writer.writeCompound(getImageGeometryObjectPath(), imageGeometry);
        this.segmentedObjectType = segmentedObjectType;
        writer.writeString(getSegmentedObjectTypeFileName(), segmentedObjectType);
    }

    @Override
    public ICellLevelFeatureWritableDataset toFeatureDataset()
    {
        return (ICellLevelFeatureWritableDataset) super.toFeatureDataset();
    }

    @Override
    public ICellLevelSegmentationWritableDataset toSegmentationDataset()
    {
        return this;
    }

    @Override
    public ICellLevelClassificationWritableDataset toClassificationDataset()
    {
        return (ICellLevelClassificationWritableDataset) super.toClassificationDataset();
    }

    public ObjectType addObjectType(String objectTypeId, ObjectType... companions)
    {
        return base.addObjectType(objectTypeId, companions);
    }

    public void setTimeSeriesSequenceAnnotation(HDF5TimeDurationArray timeValues)
    {
        base.setTimeSeriesSequenceAnnotation(timeValues);
    }

    public void setDepthScanSequenceAnnotation(DepthScanAnnotation zValues)
    {
        base.setDepthScanSequenceAnnotation(zValues);
    }

    public void setCustomSequenceAnnotation(String[] customSequenceDescriptions)
    {
        base.setCustomSequenceAnnotation(customSequenceDescriptions);
    }

    public void setPlateBarcode(String plateBarcode)
    {
        base.setPlateBarcode(plateBarcode);
    }

    public void setParentDatasetCode(String parentDatasetCode)
    {
        base.setParentDatasetCode(parentDatasetCode);
    }

    public void addDatasetAnnotation(String annotationKey, String annotation)
    {
        base.addDatasetAnnotation(annotationKey, annotation);
    }

    public void writeImageSegmentation(ImageId id, List<SegmentedObject> objects)
    {
        base.persistObjectTypes();
        int offset = 0;
        for (SegmentedObject o : objects)
        {
            o.checkInitialized();
            o.setOffset(offset * 64);
            offset += o.getSizeInWords();
        }
        ++offset;
        final long[] allMasksArray = new long[offset];
        final long[] allEdgeMasksArray = storeEdgeMasks ? new long[offset] : null;
        for (SegmentedObject o : objects)
        {
            final long[] maskArray = BitSetConversionUtils.toStorageForm(o.getMask());
            System.arraycopy(maskArray, 0, allMasksArray, o.getOffsetInWords(), maskArray.length);
            if (storeEdgeMasks)
            {
                final long[] edgeMaskArray;
                if (o.tryGetEdgeMask() != null)
                {
                    edgeMaskArray = BitSetConversionUtils.toStorageForm(o.tryGetEdgeMask());
                } else
                {
                    final CannyEdgeDetector edgeDetector = new CannyEdgeDetector();
                    edgeDetector.setSourceMask(o.getMask(), 0, o.getWidth(), o.getHeight());
                    edgeDetector.process();
                    edgeMaskArray =
                            BitSetConversionUtils.toStorageForm(edgeDetector.getEdgeMask(false));
                }
                System.arraycopy(edgeMaskArray, 0, allEdgeMasksArray, o.getOffsetInWords(),
                        edgeMaskArray.length);
            }
        }
        base.writer.writeCompoundArray(getObjectPath(id, INDEX_PREFIX), getIndexType(),
                objects.toArray(new SegmentedObjectBox[objects.size()]),
                HDF5GenericStorageFeatures.GENERIC_DEFLATE);
        base.writer.writeBitField(getObjectPath(id, MASKS_PREFIX),
                BitSetConversionUtils.fromStorageForm(allMasksArray),
                HDF5GenericStorageFeatures.GENERIC_DEFLATE);
        if (storeEdgeMasks)
        {
            base.writer.writeBitField(getObjectPath(id, EDGE_MASKS_PREFIX),
                    BitSetConversionUtils.fromStorageForm(allEdgeMasksArray),
                    HDF5GenericStorageFeatures.GENERIC_DEFLATE);
        }
    }

    @Override
    public ObjectType tryGetObjectType(String objectTypeId)
    {
        return base.tryGetObjectType(objectTypeId);
    }

    @Override
    public ObjectType[] getObjectTypes()
    {
        return base.getObjectTypes();
    }

}
