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
                new CellLevelBaseWritableDataset(writer, datasetCode, geometry, hdf5KindEnum,
                        CellLevelDatasetType.CLASSIFICATION, FORMAT_TYPE, CURRENT_FORMAT_VERSION_NUMBER);
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
    public ICellLevelClassificationWritableDataset toClassificationDataset()
    {
        return this;
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

    public HDF5EnumerationType addEnum(String name, List<String> values)
    {
        return base.addEnum(name, values);
    }

    public HDF5EnumerationType addEnum(Class<? extends Enum<?>> enumClass)
    {
        return base.addEnum(enumClass);
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

    public void writeClassification(ImageId id, Enum<?>[] classifications)
    {
        base.persistObjectTypes();
        final int[] ordinals = new int[classifications.length];
        for (int i = 0; i < classifications.length; ++i)
        {
            ordinals[i] = classifications[i].ordinal();
        }
        base.writer.writeEnumArray(getObjectPath(id), new HDF5EnumerationValueArray(hdf5EnumType,
                ordinals), HDF5IntStorageFeatures.createFromGeneric(CellLevelBaseWritableDataset
                .getStorageFeatures(classifications.length)));
    }

    public void writeClassification(ImageId id, String[] classifications)
    {
        base.persistObjectTypes();
        base.writer.writeEnumArray(getObjectPath(id), new HDF5EnumerationValueArray(hdf5EnumType,
                classifications), HDF5IntStorageFeatures
                .createFromGeneric(CellLevelBaseWritableDataset
                        .getStorageFeatures(classifications.length)));
    }

    public void writeClassification(ImageId id, int[] classificationOrdinals)
    {
        base.persistObjectTypes();
        base.writer.writeEnumArray(getObjectPath(id), new HDF5EnumerationValueArray(hdf5EnumType,
                classificationOrdinals), HDF5IntStorageFeatures
                .createFromGeneric(CellLevelBaseWritableDataset
                        .getStorageFeatures(classificationOrdinals.length)));
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
