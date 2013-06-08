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

import java.io.Flushable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import ch.ethz.sis.hcscld.ImageQuantityStructure.SequenceType;
import ch.systemsx.cisd.hdf5.HDF5EnumerationType;
import ch.systemsx.cisd.hdf5.HDF5EnumerationValueArray;
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
    private static final int SIZE_LIMIT_DEFLATE = 8192;

    /**
     * A container for the HDF5 enumeration types for object types and object namespaces.
     */
    static final class ObjectNamespaceContainer
    {
        final HDF5EnumerationType objectTypesType;

        final HDF5EnumerationType objectNamespacesType;

        ObjectNamespaceContainer(HDF5EnumerationType objectTypesType,
                HDF5EnumerationType objectNamespacesType)
        {
            this.objectTypesType = objectTypesType;
            this.objectNamespacesType = objectNamespacesType;
        }
    }

    interface IObjectNamespaceBasedFlushable
    {
        public void flush(ObjectNamespaceContainer namespaceTypeContainer);
    }

    final IHDF5Writer writer;

    final IObjectNamespaceBasedFlushable flushableOrNull;

    private final CellLevelDatasetTypeDescriptor datasetTypeDescriptor;

    CellLevelBaseWritableDataset(final IHDF5Writer writer, final String datasetCode,
            final ObjectTypeStore objectTypeStore, ImageQuantityStructure quantityStructure,
            final HDF5EnumerationType hdf5KindEnum, final CellLevelDatasetType datasetType,
            final String formatType, final int formatVersionNumber)
    {
        this(writer, datasetCode, objectTypeStore, quantityStructure, null, hdf5KindEnum,
                datasetType, formatType, formatVersionNumber);
    }

    CellLevelBaseWritableDataset(final IHDF5Writer writer, final String datasetCode,
            final ObjectTypeStore objectTypeStore, ImageQuantityStructure quantityStructure,
            final IObjectNamespaceBasedFlushable flushableOrNull,
            final HDF5EnumerationType hdf5KindEnum, final CellLevelDatasetType datasetType,
            final String formatType, final int formatVersionNumber)
    {
        super(writer, datasetCode, objectTypeStore, quantityStructure, formatVersionNumber);
        this.writer = writer;
        this.flushableOrNull = flushableOrNull;
        writer.file().addFlushable(new Flushable()
            {
                @Override
                public void flush() throws IOException
                {
                    ObjectNamespaceContainer container = persistObjectNamespaces();
                    if (flushableOrNull != null)
                    {
                        flushableOrNull.flush(container);
                    }
                }
            });
        this.datasetTypeDescriptor =
                new CellLevelDatasetTypeDescriptor(datasetType, formatType, formatVersionNumber,
                        this);
        writer.object().createGroup(getObjectPath());
        writer.compound().setAttr(getObjectPath(), getDatasetTypeAttributeName(),
                datasetTypeDescriptor);
        writer.time().setAttr(getObjectPath(), getCreationTimestampDatasetAttributeName(),
                System.currentTimeMillis());
        writer.writeCompound(getImageQuantityStructureObjectPath(), quantityStructure);
        writer.object().createGroup(getDatasetAnnotationObjectPath());
    }

    void run(IImageRunnable runnable, Object stateOrNull)
    {
        ImageRunner.run(quantityStructure, runnable, stateOrNull);
    }

    @Override
    public CellLevelDatasetType getType()
    {
        return datasetTypeDescriptor.getDatasetType();
    }

    HDF5EnumerationType addEnum(String name, List<String> values)
    {
        return addEnum(writer, datasetCode, name, values);
    }

    HDF5EnumerationType addEnumGlobal(String name, Class<? extends Enum<?>> enumClass)
    {
        return writer.enumeration().getType(name, getEnumOptions(enumClass));
    }

    HDF5EnumerationType addEnum(Class<? extends Enum<?>> enumClass)
    {
        return addEnum(writer, datasetCode, enumClass);
    }

    static HDF5EnumerationType addEnum(IHDF5Writer writer, String datasetCode, String name,
            List<String> values)
    {
        return writer.enumeration().getType(datasetCode + "_" + name,
                values.toArray(new String[values.size()]));
    }

    static HDF5EnumerationType addEnum(IHDF5Writer writer, String datasetCode,
            Class<? extends Enum<?>> enumClass)
    {
        return writer.enumeration().getType(datasetCode + "_" + enumClass.getSimpleName(),
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

    static boolean shouldDeflate(int size)
    {
        return size >= SIZE_LIMIT_DEFLATE;
    }

    static HDF5GenericStorageFeatures getStorageFeatures(int size)
    {
        if (shouldDeflate(size))
        {
            return HDF5GenericStorageFeatures.GENERIC_SHUFFLE_DEFLATE;
        } else
        {
            return HDF5GenericStorageFeatures.GENERIC_COMPACT;
        }
    }

    @Override
    public ICellLevelFeatureWritableDataset toFeatureDataset()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ICellLevelClassificationWritableDataset toClassificationDataset()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ICellLevelSegmentationWritableDataset toSegmentationDataset()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ICellLevelTrackingDataset toTrackingDataset() throws WrongDatasetTypeException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectType addObjectType(String id) throws UniqueViolationException
    {
        final ObjectType result =
                objectTypeStore.addObjectType(id, objectTypeStore.addObjectNamespace(id));
        return result;
    }

    @Override
    public ObjectType addObjectType(String id, ObjectNamespace group)
            throws UniqueViolationException
    {
        final ObjectType result = objectTypeStore.addObjectType(id, group);
        return result;
    }

    @Override
    public ObjectNamespace addObjectNamespace(String id)
    {
        return objectTypeStore.addObjectNamespace(id);
    }

    void checkCompatible(ObjectType objectType) throws WrongObjectTypeException
    {
        if (writer.file().getFile().equals(objectType.getFile()) == false
                && getDatasetCode().equals(objectType.getDatasetCode()) == false)
        {
            throw new WrongObjectTypeException(getDatasetCode(), writer.file().getFile(),
                    objectType.getDatasetCode(), objectType.getFile());
        }
    }

    /**
     * Persist object types and object namespaces to the HDF5 file.
     */
    ObjectNamespaceContainer persistObjectNamespaces()
    {
        final HDF5EnumerationType objectNamespacesType;
        if (objectTypeStore.hasObjectNamespaces())
        {
            objectNamespacesType =
                    writer.enumeration().getType(getObjectNamespacesObjectPath(),
                            toString(objectTypeStore.getObjectNamespaces()));
        } else
        {
            objectNamespacesType = null;
        }
        final HDF5EnumerationType objectTypesType;
        if (objectTypeStore.hasObjectTypes())
        {
            objectTypesType =
                    writer.enumeration().getType(getObjectTypesObjectPath(),
                            toString(objectTypeStore.getObjectTypes()));
            for (ObjectNamespace cgroup : objectTypeStore.getObjectNamespaces())
            {
                final String path = getObjectTypeCompanionGroupObjectPath(cgroup.getId());
                writer.enumeration().writeArray(
                        path,
                        new HDF5EnumerationValueArray(objectTypesType, toString(cgroup
                                .getObjectTypes())));
            }
        } else
        {
            objectTypesType = null;
        }
        return new ObjectNamespaceContainer(objectTypesType, objectNamespacesType);
    }

    private String[] toString(Collection<? extends IId> objectTypes)
    {
        final String[] options = new String[objectTypes.size()];
        int idx = 0;
        for (IId ot : objectTypes)
        {
            options[idx++] = ot.getId();
        }
        return options;
    }

    @Override
    public void setTimeSeriesSequenceAnnotation(HDF5TimeDurationArray timeValues)
    {
        checkSequenceLength(timeValues.getLength());
        checkTimeSeriesSequence();
        writer.writeTimeDurationArray(getTimeSeriesSequenceAnnotationObjectPath(), timeValues);
    }

    @Override
    public void setDepthScanSequenceAnnotation(DepthScanAnnotation zValues)
    {
        checkSequenceLength(zValues.getZValues().length);
        checkDepthScanSequence();
        final String objectPath = getDepthScanSequenceAnnotationObjectPath();
        writer.float64().writeArray(objectPath, zValues.getZValues());
        writer.string().setAttr(objectPath, "unit", zValues.getUnit());
    }

    @Override
    public void setCustomSequenceAnnotation(String[] customDescriptions)
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

    private void checkTimeSeriesSequence() throws WrongSequenceTypeException
    {
        if (quantityStructure.getSequenceType() != SequenceType.TIMESERIES
                && quantityStructure.getSequenceType() != SequenceType.TIMESERIES_DEPTHSCAN)
        {
            throw new WrongSequenceTypeException(datasetCode, new SequenceType[]
                { SequenceType.TIMESERIES, SequenceType.TIMESERIES_DEPTHSCAN },
                    quantityStructure.getSequenceType());
        }
    }

    private void checkDepthScanSequence() throws WrongSequenceTypeException
    {
        if (quantityStructure.getSequenceType() != SequenceType.DEPTHSSCAN
                && quantityStructure.getSequenceType() != SequenceType.TIMESERIES_DEPTHSCAN)
        {
            throw new WrongSequenceTypeException(datasetCode, new SequenceType[]
                { SequenceType.DEPTHSSCAN, SequenceType.TIMESERIES_DEPTHSCAN },
                    quantityStructure.getSequenceType());
        }
    }

    private void checkCustomSequence() throws WrongSequenceTypeException
    {
        if (quantityStructure.getSequenceType() == SequenceType.NONE)
        {
            throw new WrongSequenceTypeException(datasetCode, new SequenceType[]
                { SequenceType.TIMESERIES, SequenceType.DEPTHSSCAN,
                        SequenceType.TIMESERIES_DEPTHSCAN, SequenceType.CUSTOM },
                    quantityStructure.getSequenceType());
        }
    }

    @Override
    public void setPlateBarcode(String plateBarcode)
    {
        writer.string().setAttr(getObjectPath(), getPlateBarcodeAttributeName(), plateBarcode);
    }

    @Override
    public void setParentDatasetCode(String parentDatasetCode)
    {
        writer.string().setAttr(getObjectPath(), getParentDatasetAttributeName(),
                parentDatasetCode);
    }

    @Override
    public void addDatasetAnnotation(String annotationKey, String annotation)
    {
        writer.string().write(getDatasetAnnotationObjectPath(annotationKey), annotation);
    }

}
