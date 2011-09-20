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
            WellFieldGeometry geometry, final HDF5EnumerationType hdf5KindEnum,
            final Class<? extends Enum<?>> enumTypeClass)
    {
        this(writer, datasetCode, geometry, hdf5KindEnum, CellLevelBaseWritableDataset.addEnum(
                writer, datasetCode, enumTypeClass));
    }

    CellLevelClassificationWritableDataset(IHDF5Writer writer, String datasetCode,
            WellFieldGeometry geometry, final HDF5EnumerationType hdf5KindEnum,
            final List<String> options)
    {
        this(writer, datasetCode, geometry, hdf5KindEnum, CellLevelBaseWritableDataset.addEnum(
                writer, datasetCode, "Classification", options));
    }

    private CellLevelClassificationWritableDataset(IHDF5Writer writer, String datasetCode,
            WellFieldGeometry geometry, final HDF5EnumerationType hdf5KindEnum,
            final HDF5EnumerationType hdf5EnumType)
    {
        super(writer, datasetCode, geometry);
        this.base =
                new CellLevelBaseWritableDataset(writer, datasetCode, geometry, hdf5KindEnum,
                        CellLevelDatasetType.CLASSIFICATION);
        this.hdf5EnumType = hdf5EnumType;
    }

    @Override
    public ICellLevelFeatureWritableDataset tryAsFeatureDataset()
    {
        return null;
    }

    @Override
    public ICellLevelSegmentationWritableDataset tryAsSegmentationDataset()
    {
        return null;
    }

    @Override
    public ICellLevelClassificationWritableDataset tryAsClassificationDataset()
    {
        return this;
    }

    public HDF5EnumerationType addEnum(String name, List<String> values)
    {
        return base.addEnum(name, values);
    }

    public HDF5EnumerationType addEnum(Class<? extends Enum<?>> enumClass)
    {
        return base.addEnum(enumClass);
    }

    public void writeClassification(WellFieldId id, Enum<?>[] classifications)
    {
        final int[] ordinals = new int[classifications.length];
        for (int i = 0; i < classifications.length; ++i)
        {
            ordinals[i] = classifications[i].ordinal();
        }
        base.writer.writeEnumArray(getObjectPath(id), new HDF5EnumerationValueArray(hdf5EnumType,
                ordinals), HDF5IntStorageFeatures.createFromGeneric(CellLevelBaseWritableDataset
                .getStorageFeatures(classifications.length)));
    }

    public void writeClassification(WellFieldId id, String[] classifications)
    {
        base.writer.writeEnumArray(getObjectPath(id), new HDF5EnumerationValueArray(hdf5EnumType,
                classifications), HDF5IntStorageFeatures
                .createFromGeneric(CellLevelBaseWritableDataset
                        .getStorageFeatures(classifications.length)));
    }

    public void writeClassification(WellFieldId id, int[] classificationOrdinals)
    {
        base.writer.writeEnumArray(getObjectPath(id), new HDF5EnumerationValueArray(hdf5EnumType,
                classificationOrdinals), HDF5IntStorageFeatures
                .createFromGeneric(CellLevelBaseWritableDataset
                        .getStorageFeatures(classificationOrdinals.length)));
    }

}
