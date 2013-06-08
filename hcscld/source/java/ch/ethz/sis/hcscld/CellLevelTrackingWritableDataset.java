/*
 * Copyright 2012-2013 ETH Zuerich, Scientific IT Services
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

import static ch.systemsx.cisd.hdf5.HDF5CompoundMemberMapping.mapping;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.ethz.sis.hcscld.CellLevelBaseWritableDataset.IObjectNamespaceBasedFlushable;
import ch.ethz.sis.hcscld.CellLevelBaseWritableDataset.ObjectNamespaceContainer;
import ch.systemsx.cisd.hdf5.HDF5CompoundType;
import ch.systemsx.cisd.hdf5.HDF5EnumerationType;
import ch.systemsx.cisd.hdf5.HDF5IntStorageFeatures;
import ch.systemsx.cisd.hdf5.HDF5TimeDurationArray;
import ch.systemsx.cisd.hdf5.IHDF5Writer;

/**
 * Implementation of {@link ICellLevelTrackingWritableDataset}.
 * 
 * @author Bernd Rinn
 */
class CellLevelTrackingWritableDataset extends CellLevelTrackingDataset implements
        ICellLevelTrackingWritableDataset
{
    private final CellLevelBaseWritableDataset base;

    private final Set<ObjectTrackingType> objectTrackingTypes;

    CellLevelTrackingWritableDataset(final IHDF5Writer writer, final String datasetCode,
            final ImageQuantityStructure quantityStructure, final HDF5EnumerationType hdf5KindEnum)
    {
        super(writer, datasetCode, quantityStructure, FORMAT_TYPE, CURRENT_FORMAT_VERSION_NUMBER);
        this.base =
                new CellLevelBaseWritableDataset(writer, datasetCode, objectTypeStore,
                        quantityStructure, getFlushable(), hdf5KindEnum,
                        CellLevelDatasetType.TRACKING, FORMAT_TYPE, CURRENT_FORMAT_VERSION_NUMBER);
        this.objectTrackingTypes = new HashSet<ObjectTrackingType>();
    }

    private IObjectNamespaceBasedFlushable getFlushable()
    {
        return new IObjectNamespaceBasedFlushable()
            {
                @Override
                public void flush(ObjectNamespaceContainer namespaceTypeContainer)
                {
                    final HDF5EnumerationType namespacesEnumType =
                            namespaceTypeContainer.objectNamespacesType;
                    final ObjectTrackingType[] values =
                            objectTrackingTypes.toArray(new ObjectTrackingType[objectTrackingTypes
                                    .size()]);
                    for (ObjectTrackingType type : values)
                    {
                        type.setNamespacesEnumType(namespacesEnumType);
                    }
                    Arrays.sort(values);
                    final HDF5CompoundType<ObjectTrackingType> type =
                            base.writer.compound().getType(
                                    getObjectPath(DATASET_TYPE_DIR, "ObjectTrackingType"),
                                    ObjectTrackingType.class,
                                    mapping("parentObjectNamespace").fieldName(
                                            "parentObjectNamespaceEnum").enumType(
                                            namespacesEnumType),
                                    mapping("parentImageSequenceIdx"),
                                    mapping("childObjectNamespace").fieldName(
                                            "childObjectNamespaceEnum")
                                            .enumType(namespacesEnumType),
                                    mapping("childImageSequenceIdx"));
                    base.writer.compound().writeArray(
                            getObjectPath(OBJECT_TRACKING_TYPE_DATASET_NAME), type, values);
                }
            };
    }

    @Override
    public ICellLevelFeatureWritableDataset toFeatureDataset()
    {
        return (ICellLevelFeatureWritableDataset) super.toClassificationDataset();
    }

    @Override
    public ICellLevelClassificationWritableDataset toClassificationDataset()
    {
        return (ICellLevelClassificationWritableDataset) super.toClassificationDataset();
    }

    @Override
    public ICellLevelSegmentationWritableDataset toSegmentationDataset()
    {
        return (ICellLevelSegmentationWritableDataset) super.toSegmentationDataset();
    }

    @Override
    public ICellLevelTrackingWritableDataset toTrackingDataset()
    {
        return this;
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

    HDF5EnumerationType addEnum(String name, List<String> values)
    {
        return base.addEnum(name, values);
    }

    HDF5EnumerationType addEnum(Class<? extends Enum<?>> enumClass)
    {
        return base.addEnum(enumClass);
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
    public ObjectTrackingType createObjectTrackingType(ObjectNamespace parentNamespace,
            int parentSequenceId, ObjectNamespace childNamespace, int childSequenceId)
    {
        final ObjectTrackingType trackingType =
                new ObjectTrackingType(this, parentNamespace, parentSequenceId, childNamespace,
                        childSequenceId);
        return trackingType;
    }

    @Override
    public ObjectTrackingType createObjectTrackingType(ObjectNamespace parentObjectNamespace,
            ObjectNamespace childObjectNamespace)
    {
        return createObjectTrackingType(parentObjectNamespace, 0, childObjectNamespace, 0);
    }

    @Override
    public ObjectTrackingType createObjectTrackingType(ObjectNamespace objectNamespace,
            int parentImageSequenceId, int childImageSequenceId)
    {
        return createObjectTrackingType(objectNamespace, parentImageSequenceId, objectNamespace,
                childImageSequenceId);
    }

    @Override
    public void writeObjectTracking(ImageSequenceId id, ObjectTrackingType type,
            ObjectTrackingBuilder tracking)
    {
        objectTrackingTypes.add(type);
        final String objectPath = type.getObjectPath(id);
        final HDF5IntStorageFeatures features;
        if (CellLevelBaseWritableDataset.shouldDeflate(tracking.getTotalSizeInBytes()))
        {
            features = HDF5IntStorageFeatures.INT_AUTO_SCALING_DEFLATE;
        } else
        {
            features = HDF5IntStorageFeatures.INT_COMPACT;
        }
        switch (tracking.getStorageForm())
        {
            case UNSIGNED_BYTE:
            {
                base.writer.uint8().createMDArray(objectPath, tracking.getLinking().dimensions(),
                        features);
                break;
            }
            case UNSIGNED_SHORT:
            {
                base.writer.uint16().createMDArray(objectPath, tracking.getLinking().dimensions(),
                        features);
                break;
            }
            case UNSIGNED_INT:
            {
                base.writer.uint32().createMDArray(objectPath, tracking.getLinking().dimensions(),
                        features);
                break;
            }
        }
        base.writer.uint32().writeMDArrayBlock(objectPath, tracking.getLinking(), new long[]
            { 0, 0 });
    }

    @Override
    public ObjectTrackingTypes getObjectTrackingTypes()
    {
        final ObjectTrackingType[] array =
                objectTrackingTypes.toArray(new ObjectTrackingType[objectTrackingTypes.size()]);
        Arrays.sort(array);
        return new ObjectTrackingTypes(Arrays.asList(array));
    }

    IDatasetVerifyer getVerifyer()
    {
        return new IDatasetVerifyer()
            {
                @Override
                public String verify()
                {
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
