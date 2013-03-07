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

import ch.systemsx.cisd.hdf5.HDF5EnumerationType;
import ch.systemsx.cisd.hdf5.HDF5EnumerationValueArray;
import ch.systemsx.cisd.hdf5.HDF5IntStorageFeatures;
import ch.systemsx.cisd.hdf5.HDF5TimeDurationArray;
import ch.systemsx.cisd.hdf5.IHDF5Writer;

/**
 * Implementation of {@link ICellLevelClassificationWritableDataset}.
 * 
 * @author Bernd Rinn
 */
class CellLevelClassificationWritableDataset extends CellLevelClassificationDataset implements
        ICellLevelClassificationWritableDataset
{
    private final CellLevelBaseWritableDataset base;

    private final HDF5EnumerationType hdf5EnumType;

    CellLevelClassificationWritableDataset(IHDF5Writer writer, String datasetCode,
            ImageQuantityStructure geometry, final HDF5EnumerationType hdf5KindEnum,
            final Class<? extends Enum<?>> enumTypeClass)
    {
        this(writer, datasetCode, geometry, hdf5KindEnum, CellLevelBaseWritableDataset.addEnum(
                writer, datasetCode, enumTypeClass));
    }

    CellLevelClassificationWritableDataset(IHDF5Writer writer, String datasetCode,
            ImageQuantityStructure geometry, final HDF5EnumerationType hdf5KindEnum,
            final List<String> options)
    {
        this(writer, datasetCode, geometry, hdf5KindEnum, CellLevelBaseWritableDataset.addEnum(
                writer, datasetCode, "Classification", options));
    }

    private CellLevelClassificationWritableDataset(IHDF5Writer writer, String datasetCode,
            ImageQuantityStructure geometry, final HDF5EnumerationType hdf5KindEnum,
            final HDF5EnumerationType hdf5EnumType)
    {
        super(writer, datasetCode, geometry, FORMAT_TYPE, CURRENT_FORMAT_VERSION_NUMBER);
        this.base =
                new CellLevelBaseWritableDataset(writer, datasetCode, objectTypeStore, geometry,
                        hdf5KindEnum, CellLevelDatasetType.CLASSIFICATION, FORMAT_TYPE,
                        CURRENT_FORMAT_VERSION_NUMBER);
        this.hdf5EnumType = hdf5EnumType;
    }

    @Override
    public ICellLevelFeatureWritableDataset toFeatureDataset()
    {
        return (ICellLevelFeatureWritableDataset) super.toFeatureDataset();
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
    public ICellLevelClassificationWritableDataset toClassificationDataset()
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

    public HDF5EnumerationType addEnum(String name, List<String> values)
    {
        return base.addEnum(name, values);
    }

    public HDF5EnumerationType addEnum(Class<? extends Enum<?>> enumClass)
    {
        return base.addEnum(enumClass);
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
    public void writeClassification(ImageId id, ObjectNamespace namespace, Enum<?>[] classifications)
    {
        namespace.checkNumberOfSegmentedObjects(getImageQuantityStructure(), id,
                classifications.length);
        final int[] ordinals = new int[classifications.length];
        for (int i = 0; i < classifications.length; ++i)
        {
            ordinals[i] = classifications[i].ordinal();
        }
        base.writer.enumeration().writeArray(getClassPath(id, namespace),
                new HDF5EnumerationValueArray(hdf5EnumType, ordinals), HDF5IntStorageFeatures
                        .createFromGeneric(CellLevelBaseWritableDataset
                                .getStorageFeatures(classifications.length)));
    }

    @Override
    public void writeClassification(ImageId id, ObjectNamespace namespace, String[] classifications)
    {
        namespace.checkNumberOfSegmentedObjects(getImageQuantityStructure(), id,
                classifications.length);
        base.writer.enumeration().writeArray(getClassPath(id, namespace),
                new HDF5EnumerationValueArray(hdf5EnumType, classifications),
                HDF5IntStorageFeatures.createFromGeneric(CellLevelBaseWritableDataset
                        .getStorageFeatures(classifications.length)));
    }

    @Override
    public void writeClassification(ImageId id, ObjectNamespace namespace,
            int[] classificationOrdinals)
    {
        namespace.checkNumberOfSegmentedObjects(getImageQuantityStructure(), id,
                classificationOrdinals.length);
        base.writer.enumeration().writeArray(getClassPath(id, namespace),
                new HDF5EnumerationValueArray(hdf5EnumType, classificationOrdinals),
                HDF5IntStorageFeatures.createFromGeneric(CellLevelBaseWritableDataset
                        .getStorageFeatures(classificationOrdinals.length)));
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
