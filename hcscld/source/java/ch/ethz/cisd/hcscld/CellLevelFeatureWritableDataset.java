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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ch.ethz.cisd.hcscld.CellLevelBaseWritableDataset.IObjectNamespaceBasedFlushable;
import ch.ethz.cisd.hcscld.CellLevelBaseWritableDataset.ObjectNamespaceContainer;
import ch.systemsx.cisd.base.mdarray.MDFloatArray;
import ch.systemsx.cisd.base.mdarray.MDIntArray;
import ch.systemsx.cisd.hdf5.HDF5CompoundMappingHints;
import ch.systemsx.cisd.hdf5.HDF5CompoundType;
import ch.systemsx.cisd.hdf5.HDF5EnumerationType;
import ch.systemsx.cisd.hdf5.HDF5EnumerationValue;
import ch.systemsx.cisd.hdf5.HDF5FloatStorageFeatures;
import ch.systemsx.cisd.hdf5.HDF5IntStorageFeatures;
import ch.systemsx.cisd.hdf5.HDF5TimeDurationArray;
import ch.systemsx.cisd.hdf5.IHDF5Writer;

/**
 * A writable dataset for cell-level features.
 * 
 * @author Bernd Rinn
 */
class CellLevelFeatureWritableDataset extends CellLevelFeatureDataset implements
        ICellLevelFeatureWritableDataset
{
    private final CellLevelBaseWritableDataset base;

    CellLevelFeatureWritableDataset(final IHDF5Writer writer, final String datasetCode,
            final ImageQuantityStructure quantityStructure,
            final HDF5CompoundMappingHints hintsOrNull, final HDF5EnumerationType hdf5KindEnum)
    {
        super(writer, datasetCode, quantityStructure, hintsOrNull, FORMAT_TYPE,
                CURRENT_FORMAT_VERSION_NUMBER);
        this.base =
                new CellLevelBaseWritableDataset(writer, datasetCode, objectTypeStore,
                        quantityStructure, getFlushable(), hdf5KindEnum,
                        CellLevelDatasetType.FEATURES, FORMAT_TYPE, CURRENT_FORMAT_VERSION_NUMBER);
    }

    private IObjectNamespaceBasedFlushable getFlushable()
    {
        return new IObjectNamespaceBasedFlushable()
            {
                @Override
                public void flush(ObjectNamespaceContainer namespaceTypeContainer)
                {
                    if (featureGroups.size() == 0)
                    {
                        return;
                    }
                    final HDF5CompoundType<FeatureGroupDescriptor> featureGroupCompoundType =
                            base.writer.compound().getInferredType(
                                    getObjectPath(DATASET_TYPE_DIR, "FeatureGroupDescriptor"),
                                    FeatureGroupDescriptor.class,
                                    new HDF5CompoundMappingHints().enumTypeMapping("namespaceId",
                                            namespaceTypeContainer.objectNamespacesType));
                    final FeatureGroupDescriptor[] descriptors =
                            new FeatureGroupDescriptor[featureGroups.size()];
                    int idx = 0;
                    for (FeatureGroup fg : featureGroups.values())
                    {
                        descriptors[idx++] =
                                new FeatureGroupDescriptor(fg.getId(), new HDF5EnumerationValue(
                                        namespaceTypeContainer.objectNamespacesType, fg
                                                .getNamespace().getId()), fg.getDataType());
                    }
                    Arrays.sort(descriptors);
                    base.writer.compound().writeArray(getFeatureGroupsFilename(),
                            featureGroupCompoundType, descriptors);
                }
            };
    }

    @Override
    public ICellLevelFeatureWritableDataset toFeatureDataset()
    {
        return this;
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
        return (ICellLevelTrackingWritableDataset) super.toTrackingDataset();
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
    public IFeaturesDefinition createFeaturesDefinition(ObjectNamespace namespace)
    {
        return new FeaturesDefinition(this, namespace);
    }

    IFeatureGroup addFeatureGroup(final String id, final IFeaturesDefinition features)
    {
        if (id.equals(DEFAULT_FEATURE_GROUP_NAME))
        {
            throw new IllegalArgumentException("Feature group name '" + DEFAULT_FEATURE_GROUP_NAME
                    + "' is reserved.");
        }
        return addFeatureGroupInternal(id, (FeaturesDefinition) features);
    }

    void addFeatureGroupToInternalList(String key, FeatureGroup newFeatureGroup)
    {
        if (featureGroups.containsKey(key))
        {
            throw new IllegalArgumentException("Duplicate feature group code '"
                    + newFeatureGroup.getId() + "'.");
        }
        featureGroups.put(key, newFeatureGroup);
    }

    FeatureGroup addFeatureGroupInternal(final String id, final FeaturesDefinition features)
    {
        final String idUpperCase = id.toUpperCase();
        final HDF5CompoundType<Object[]> type =
                base.writer.compound().getType(getFeatureGroupTypePath(idUpperCase),
                        Object[].class, features.getMembers(hintsOrNull));
        final FeatureGroup featureGroup = new FeatureGroup(id, features.getNamespace(), type);
        addFeatureGroupToInternalList(idUpperCase, featureGroup);
        return featureGroup;
    }

    @Override
    public void writeFeatures(ImageId id, IFeatureGroup featureGroup, Object[][] featureValues)
    {
        final FeatureGroup fg = (FeatureGroup) featureGroup;
        fg.getNamespace().checkNumberOfSegmentedObjects(getImageQuantityStructure(), id,
                featureValues.length);
        switch (featureGroup.getDataType())
        {
            case FLOAT32:
                base.writer.float32().writeMDArray(
                        fg.getObjectPath(id),
                        toFloatArray(featureValues),
                        HDF5FloatStorageFeatures.createFromGeneric(CellLevelBaseWritableDataset
                                .getStorageFeatures(featureValues.length
                                        * fg.getType().getRecordSize())));
                break;
            case INT32:
                base.writer.int32().writeMDArray(
                        fg.getObjectPath(id),
                        toIntArray(featureValues),
                        HDF5IntStorageFeatures.createFromGeneric(CellLevelBaseWritableDataset
                                .getStorageFeatures(featureValues.length
                                        * fg.getType().getRecordSize())));
                break;
            default:
                base.writer.compound().writeArray(
                        fg.getObjectPath(id),
                        fg.getType(),
                        featureValues,
                        CellLevelBaseWritableDataset.getStorageFeatures(featureValues.length
                                * fg.getType().getRecordSize()));
                break;

        }
    }

    private MDFloatArray toFloatArray(Object[][] featureValues)
    {
        final MDFloatArray result = new MDFloatArray(new int[]
            { featureValues.length, (featureValues.length > 0) ? featureValues[0].length : 0 });
        int idxObj = 0;
        for (Object[] vector : featureValues)
        {
            int idxFeature = 0;
            for (Object value : vector)
            {
                result.set(((Number) value).floatValue(), idxObj, idxFeature);
                ++idxFeature;
            }
            ++idxObj;
        }
        return result;
    }

    private MDIntArray toIntArray(Object[][] featureValues)
    {
        final MDIntArray result = new MDIntArray(new int[]
            { featureValues.length, (featureValues.length > 0) ? featureValues[0].length : 0 });
        int idxObj = 0;
        for (Object[] vector : featureValues)
        {
            int idxFeature = 0;
            for (Object value : vector)
            {
                result.set(((Number) value).intValue(), idxObj, idxFeature);
                ++idxFeature;
            }
            ++idxObj;
        }
        return result;
    }

    @Override
    public void writeFeatures(ImageId id, Object[][] featureValues)
    {
        checkDefaultFeatureGroup();
        final FeatureGroup fg = getFirstFeatureGroup();
        fg.getNamespace().checkNumberOfSegmentedObjects(getImageQuantityStructure(), id,
                featureValues.length);
        base.writer.compound().writeArray(
                fg.getObjectPath(id),
                fg.getType(),
                featureValues,
                CellLevelBaseWritableDataset.getStorageFeatures(featureValues.length
                        * fg.getType().getRecordSize()));
    }

    private void checkDefaultFeatureGroup() throws IllegalStateException
    {
        if (usesDefaultFeatureGroup() == false)
        {
            throw new IllegalStateException(
                    "Cannot use 'writeFeatures(WellFieldId, FeaturesDefinition, Object[][])'"
                            + " on a dataset with non-default feature groups defined.");
        }
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
