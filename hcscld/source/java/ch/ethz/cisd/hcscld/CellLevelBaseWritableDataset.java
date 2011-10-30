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

import ch.ethz.cisd.hcscld.ImageQuantityStructure.SequenceType;
import ch.systemsx.cisd.hdf5.HDF5EnumerationType;
import ch.systemsx.cisd.hdf5.HDF5EnumerationValue;
import ch.systemsx.cisd.hdf5.HDF5GenericStorageFeatures;
import ch.systemsx.cisd.hdf5.HDF5TimeDurationArray;
import ch.systemsx.cisd.hdf5.IHDF5Writer;

/**
 * The mix-in class for writable dataset classes.
 * 
 * @author Bernd Rinn
 */
class CellLevelBaseWritableDataset extends CellLevelDataset implements ICellLevelWritableDataset
{
    final IHDF5Writer writer;

    private final CellLevelDatasetType datasetType;

    CellLevelBaseWritableDataset(final IHDF5Writer writer, final String datasetCode,
            final ImageQuantityStructure quantityStructure, final HDF5EnumerationType hdf5KindEnum,
            final CellLevelDatasetType datasetType)
    {
        super(writer, datasetCode, quantityStructure);
        this.writer = writer;
        this.datasetType = datasetType;
        quantityStructure.writeToDataset(this);
        writer.setEnumAttribute(getObjectPath(), getDatasetTypeAttributeName(),
                new HDF5EnumerationValue(hdf5KindEnum, datasetType.ordinal()));
    }

    void run(IImageRunnable runnable, Object stateOrNull)
    {
        ImageRunner.run(quantityStructure, runnable, stateOrNull);
    }

    public CellLevelDatasetType getType()
    {
        return datasetType;
    }

    HDF5EnumerationType addEnum(String name, List<String> values)
    {
        return addEnum(writer, datasetCode, name, values);
    }

    HDF5EnumerationType addEnumGlobal(String name, Class<? extends Enum<?>> enumClass)
    {
        return writer.getEnumType(name, getEnumOptions(enumClass));
    }

    HDF5EnumerationType addEnum(Class<? extends Enum<?>> enumClass)
    {
        return addEnum(writer, datasetCode, enumClass);
    }

    static HDF5EnumerationType addEnum(IHDF5Writer writer, String datasetCode, String name,
            List<String> values)
    {
        return writer.getEnumType(datasetCode + "_" + name,
                values.toArray(new String[values.size()]));
    }

    static HDF5EnumerationType addEnum(IHDF5Writer writer, String datasetCode,
            Class<? extends Enum<?>> enumClass)
    {
        return writer.getEnumType(datasetCode + "_" + enumClass.getSimpleName(),
                getEnumOptions(enumClass));
    }

    static String[] getEnumOptions(Class<? extends Enum<?>> enumClass)
    {
        final Enum<?>[] constants = enumClass.getEnumConstants();
        final String[] options = new String[constants.length];
        for (int i = 0; i < options.length; ++i)
        {
            options[i] = constants[i].name();
        }
        return options;
    }

    static HDF5GenericStorageFeatures getStorageFeatures(int size)
    {
        if (size < 8000)
        {
            return HDF5GenericStorageFeatures.GENERIC_COMPACT;
        } else
        {
            return HDF5GenericStorageFeatures.GENERIC_DEFLATE;
        }
    }

    public ICellLevelFeatureWritableDataset toFeatureDataset()
    {
        throw new UnsupportedOperationException();
    }

    public ICellLevelClassificationWritableDataset toClassificationDataset()
    {
        throw new UnsupportedOperationException();
    }

    public ICellLevelSegmentationWritableDataset toSegmentationDataset()
    {
        throw new UnsupportedOperationException();
    }

    public void addTimeSeriesSequenceAnnotation(HDF5TimeDurationArray timeValues)
    {
        checkSequenceLength(timeValues.getLength());
        checkTimeSeriesSequence();
        writer.writeTimeDurationArray(getTimeSeriesSequenceAnnotationObjectPath(), timeValues);
    }

    public void addDepthScanSequenceAnnotation(DepthScanAnnotation zValues)
    {
        checkSequenceLength(zValues.getZValues().length);
        checkDepthScanSequence();
        final String objectPath = getDepthScanSequenceAnnotationObjectPath();
        writer.writeDoubleArray(objectPath, zValues.getZValues());
        writer.setStringAttribute(objectPath, "unit", zValues.getUnit());
    }

    public void addCustomSequenceAnnotation(String[] customDescriptions)
    {
        checkSequenceLength(customDescriptions.length);
        checkCustomSequence();
        writer.writeStringArray(getCustomSequenceAnnotationObjectPath(), customDescriptions);
    }

    private void checkSequenceLength(int sequenceLength)
    {
        if (quantityStructure.getSequenceLength() != sequenceLength)
        {
            throw new IllegalArgumentException("Wrong sequence length [expected: "
                    + quantityStructure.getSequenceLength() + ", found: " + sequenceLength + "]");
        }
    }

    private void checkTimeSeriesSequence() throws IllegalArgumentException
    {
        if (quantityStructure.getSequenceType() != SequenceType.TIMESERIES
                && quantityStructure.getSequenceType() != SequenceType.TIMESERIES_DEPTHSCAN)
        {
            throw new IllegalArgumentException("Sequence of type "
                    + quantityStructure.getSequenceType()
                    + " does not allow time series annotation.");
        }
    }

    private void checkDepthScanSequence() throws IllegalArgumentException
    {
        if (quantityStructure.getSequenceType() != SequenceType.DEPTHSSCAN
                && quantityStructure.getSequenceType() != SequenceType.TIMESERIES_DEPTHSCAN)
        {
            throw new IllegalArgumentException("Sequence of type "
                    + quantityStructure.getSequenceType()
                    + " does not allow depth scan annotation.");
        }
    }

    private void checkCustomSequence() throws IllegalArgumentException
    {
        if (quantityStructure.getSequenceType() == SequenceType.NONE)
        {
            throw new IllegalArgumentException(
                    "This dataset is not a sequence and thus doesn't allow custom annotations.");
        }
    }
}
