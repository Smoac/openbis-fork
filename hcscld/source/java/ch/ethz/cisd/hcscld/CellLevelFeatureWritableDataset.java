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

import java.util.Collection;
import java.util.List;

import ch.ethz.cisd.hcscld.CellLevelBaseWritableDataset.IObjectNamespaceBasedFlushable;
import ch.ethz.cisd.hcscld.CellLevelBaseWritableDataset.ObjectNamespaceContainer;
import ch.systemsx.cisd.hdf5.HDF5CompoundMappingHints;
import ch.systemsx.cisd.hdf5.HDF5CompoundMemberMapping;
import ch.systemsx.cisd.hdf5.HDF5CompoundType;
import ch.systemsx.cisd.hdf5.HDF5EnumerationType;
import ch.systemsx.cisd.hdf5.HDF5EnumerationValue;
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
            final ImageQuantityStructure geometry, final HDF5CompoundMappingHints hintsOrNull,
            final HDF5EnumerationType hdf5KindEnum)
    {
        super(writer, datasetCode, geometry, hintsOrNull, FORMAT_TYPE,
                CURRENT_FORMAT_VERSION_NUMBER);
        this.base =
                new CellLevelBaseWritableDataset(writer, datasetCode, objectTypeStore, geometry,
                        getFlushable(writer), hdf5KindEnum, CellLevelDatasetType.FEATURES,
                        FORMAT_TYPE, CURRENT_FORMAT_VERSION_NUMBER);
    }

    private IObjectNamespaceBasedFlushable getFlushable(final IHDF5Writer writer)
    {
        return new IObjectNamespaceBasedFlushable()
            {
                public void flush(ObjectNamespaceContainer namespaceTypeContainer)
                {
                    final HDF5CompoundType<FeatureGroupDescriptor> featureGroupCompoundType =
                            writer.getCompoundType(
                                    getObjectPath(DATASET_TYPE_DIR, "FeatureGroupDescriptor"),
                                    FeatureGroupDescriptor.class,
                                    HDF5CompoundMemberMapping.mapping("id").dimensions(new int[]
                                        { 100 }),
                                    HDF5CompoundMemberMapping.mapping("namespaceId").enumType(
                                            namespaceTypeContainer.objectNamespacesType));
                    final FeatureGroupDescriptor[] descriptors =
                            new FeatureGroupDescriptor[featureGroups.size()];
                    int idx = 0;
                    for (FeatureGroup fg : featureGroups.values())
                    {
                        descriptors[idx++] =
                                new FeatureGroupDescriptor(fg.getId(), new HDF5EnumerationValue(
                                        namespaceTypeContainer.objectNamespacesType, fg
                                                .getNamespace().getId()));
                    }
                    writer.writeCompoundArray(getFeatureGroupsFilename(), featureGroupCompoundType,
                            descriptors);
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

    public ObjectType addObjectType(String id) throws UniqueViolationException
    {
        return base.addObjectType(id);
    }

    public ObjectType addObjectType(String id, ObjectNamespace group)
            throws UniqueViolationException
    {
        return base.addObjectType(id, group);
    }

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

    public IFeaturesDefinition createFeaturesDefinition()
    {
        return new FeaturesDefinition(this);
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

    FeatureGroup addFeatureGroupInternal(final String id, final FeaturesDefinition features)
    {
        final String idUpperCase = id.toUpperCase();
        final HDF5CompoundType<Object[]> type =
                base.writer.getCompoundType(getFeatureGroupTypePath(idUpperCase), Object[].class,
                        features.getMembers(hintsOrNull));
        final FeatureGroup featureGroup =
                new FeatureGroup(idUpperCase, features.getNamespace(), type);
        addFeatureGroupToInternalList(featureGroup);
        return featureGroup;
    }

    public void writeFeatures(ImageId id, IFeatureGroup featureGroup, Object[][] featureValues)
    {
        final FeatureGroup fg = (FeatureGroup) featureGroup;
        fg.getNamespace().checkNumberOfSegmentedObjects(getImageQuantityStructure(), id,
                featureValues.length);
        base.writer.writeCompoundArray(
                fg.getObjectPath(id),
                fg.getType(),
                featureValues,
                CellLevelBaseWritableDataset.getStorageFeatures(featureValues.length
                        * fg.getType().getRecordSize()));
    }

    public void writeFeatures(ImageId id, Object[][] featureValues)
    {
        checkDefaultFeatureGroup();
        final FeatureGroup fg = getFirstFeatureGroup();
        fg.getNamespace().checkNumberOfSegmentedObjects(getImageQuantityStructure(), id,
                featureValues.length);
        base.writer.writeCompoundArray(
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
                public String verify()
                {
                    return null;
                }

                public String getDatasetCode()
                {
                    return datasetCode;
                }
            };
    }

}
