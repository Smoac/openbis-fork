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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private final Set<ObjectType> objectTypesWritten;

    CellLevelSegmentationWritableDataset(final IHDF5Writer writer, final String datasetCode,
            final ImageQuantityStructure quantityStructure, final ImageGeometry imageGeometry,
            final HDF5EnumerationType hdf5KindEnum, final boolean storeEdgeMasks)
    {
        super(writer, datasetCode, quantityStructure, imageGeometry, FORMAT_TYPE,
                CURRENT_FORMAT_VERSION_NUMBER);
        this.base =
                new CellLevelBaseWritableDataset(writer, datasetCode, objectTypeStore,
                        quantityStructure, hdf5KindEnum, CellLevelDatasetType.SEGMENTATION,
                        FORMAT_TYPE, CURRENT_FORMAT_VERSION_NUMBER);
        this.storeEdgeMasks = storeEdgeMasks;
        this.objectTypesWritten = new HashSet<ObjectType>();
        writer.writeCompound(getImageGeometryObjectPath(), imageGeometry);
    }

    @Override
    public ICellLevelFeatureWritableDataset toFeatureDataset()
    {
        return (ICellLevelFeatureWritableDataset) super.toFeatureDataset();
    }

    @Override
    public ICellLevelTrackingWritableDataset toTrackingDataset()
    {
        return (ICellLevelTrackingWritableDataset) super.toTrackingDataset();
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

    @Override
    public ObjectType addObjectType(String id) throws UniqueViolationException
    {
        return base.addObjectType(id);
    }

    @Override
    public ObjectType addObjectType(String id, ObjectNamespace group)
            throws UniqueViolationException
    {
        return base.addObjectType(id, group);
    }

    @Override
    public ObjectNamespace addObjectNamespace(String id)
    {
        return base.addObjectNamespace(id);
    }

    @Override
    public void setTimeSeriesSequenceAnnotation(HDF5TimeDurationArray timeValues)
    {
        base.setTimeSeriesSequenceAnnotation(timeValues);
    }

    @Override
    public void setDepthScanSequenceAnnotation(DepthScanAnnotation zValues)
    {
        base.setDepthScanSequenceAnnotation(zValues);
    }

    @Override
    public void setCustomSequenceAnnotation(String[] customSequenceDescriptions)
    {
        base.setCustomSequenceAnnotation(customSequenceDescriptions);
    }

    @Override
    public void setPlateBarcode(String plateBarcode)
    {
        base.setPlateBarcode(plateBarcode);
    }

    @Override
    public void setParentDatasetCode(String parentDatasetCode)
    {
        base.setParentDatasetCode(parentDatasetCode);
    }

    @Override
    public void addDatasetAnnotation(String annotationKey, String annotation)
    {
        base.addDatasetAnnotation(annotationKey, annotation);
    }

    @Override
    public void writeImageSegmentation(ImageId id, ObjectType objectType,
            List<SegmentedObject> objects)
    {
        base.checkCompatible(objectType);
        objectType.getObjectNamespace().checkNumberOfSegmentedObjects(
                getImageQuantityStructure(), id, objects.size());
        int idx = 0;
        for (SegmentedObject so : objects)
        {
            so.setObjectTypeOrNull(objectType);
            so.setObjectIndex(idx++);
        }
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
        idx = 0;
        for (SegmentedObject o : objects)
        {
            final long[] maskArray = BitSetConversionUtils.toStorageForm(o.getMask());
            if (maskArray.length > o.getSizeInWords())
            {
                throw new ArrayIndexOutOfBoundsException(String.format(
                        "Object %d: largest bit of mask (%d) > size of bounding box (%d)", idx, o
                                .getMask().length(), o.getSizeInPixels()));
            }
            ++idx;
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
        base.writer.compound().writeArray(getObjectPath(id, INDEX_PREFIX, objectType.getId()),
                getIndexType(), objects.toArray(new SegmentedObjectBox[objects.size()]),
                HDF5GenericStorageFeatures.GENERIC_DEFLATE);
        base.writer.bool().writeBitField(getObjectPath(id, MASKS_PREFIX, objectType.getId()),
                BitSetConversionUtils.fromStorageForm(allMasksArray),
                HDF5GenericStorageFeatures.GENERIC_DEFLATE);
        if (storeEdgeMasks)
        {
            base.writer.bool().writeBitField(getObjectPath(id, EDGE_MASKS_PREFIX, objectType.getId()),
                    BitSetConversionUtils.fromStorageForm(allEdgeMasksArray),
                    HDF5GenericStorageFeatures.GENERIC_DEFLATE);
        }
        objectTypesWritten.add(objectType);
    }

    @Override
    public ObjectType tryGetObjectType(String objectTypeId)
    {
        return base.tryGetObjectType(objectTypeId);
    }

    @Override
    public Collection<ObjectType> getObjectTypes()
    {
        return base.getObjectTypes();
    }

    IDatasetVerifyer getVerifyer()
    {
        return new IDatasetVerifyer()
            {
                @Override
                public String verify()
                {
                    final Set<ObjectType> objectTypesNotWritten =
                            new HashSet<ObjectType>(objectTypeStore.getObjectTypes());
                    objectTypesNotWritten.removeAll(objectTypesWritten);
                    if (objectTypesNotWritten.isEmpty() == false)
                    {
                        final StringBuilder b = new StringBuilder();
                        b.append("Object types not written: ");
                        for (ObjectType objectType : objectTypesNotWritten)
                        {
                            b.append(objectType.getId());
                            b.append(", ");
                        }
                        b.setLength(b.length() - 2);
                        b.append(".");
                        return b.toString();
                    }
                    final Set<ObjectNamespace> emptyCompanionGroups =
                            new HashSet<ObjectNamespace>();
                    for (ObjectNamespace cgroup : objectTypeStore.getObjectNamespaces())
                    {
                        if (cgroup.getObjectTypes().isEmpty())
                        {
                            emptyCompanionGroups.add(cgroup);
                        }
                    }
                    if (emptyCompanionGroups.isEmpty() == false)
                    {
                        final StringBuilder b = new StringBuilder();
                        b.append("Empty object namespaces: ");
                        for (ObjectNamespace cgroup : emptyCompanionGroups)
                        {
                            b.append(cgroup.getId());
                            b.append(", ");
                        }
                        b.setLength(b.length() - 2);
                        b.append(".");
                        return b.toString();
                    }
                    return null;
                }

                @Override
                public String getDatasetCode()
                {
                    return datasetCode;
                }
            };
    }

}
