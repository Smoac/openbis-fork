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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.hdf5.HDF5TimeDurationArray;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

/**
 * Implementation of {@link ICellLevelDataset}.
 * 
 * @author Bernd Rinn
 */
abstract class CellLevelDataset implements ICellLevelDataset
{
    static final String DATASET_TYPE_DIR = "__DATA_TYPES__";

    static final String NUMBER_OF_ELEMENTS_ATTRIBUTE = "numberOfSegmentationElements";

    final IHDF5Reader reader;

    final String datasetCode;

    final ImageQuantityStructure quantityStructure;

    final int formatVersionNumber;

    final ObjectTypeStore objectTypeStore;

    CellLevelDataset(IHDF5Reader reader, String datasetCode, ObjectTypeStore objectTypeStore,
            ImageQuantityStructure quantityStructure, int formatVersionNumber)
    {
        this.reader = reader;
        this.datasetCode = datasetCode;
        this.quantityStructure = quantityStructure;
        this.formatVersionNumber = formatVersionNumber;
        this.objectTypeStore = objectTypeStore;
    }

    CellLevelDataset(IHDF5Reader reader, String datasetCode,
            ImageQuantityStructure quantityStructure, int formatVersionNumber)
    {
        this.reader = reader;
        this.datasetCode = datasetCode;
        this.quantityStructure = quantityStructure;
        this.formatVersionNumber = formatVersionNumber;
        this.objectTypeStore = readObjectTypeStore();
    }

    /**
     * Reads the object types from the HDF5 file.
     */
    private ObjectTypeStore readObjectTypeStore()
    {
        final ObjectTypeStore result = new ObjectTypeStore(reader.getFile(), getDatasetCode());
        if (reader.isDataType(getObjectNamespacesObjectPath()) == false)
        {
            return result;
        }
        final List<String> objectTypeCompanionGroups =
                reader.getEnumType(getObjectNamespacesObjectPath()).getValues();
        for (String id : objectTypeCompanionGroups)
        {
            result.addObjectTypeCompanionGroup(id);
        }
        if (reader.isDataType(getObjectTypesObjectPath()) == false)
        {
            return result;
        }
        for (String cgId : objectTypeCompanionGroups)
        {
            final String cgObjectPath = getObjectTypeCompanionGroupObjectPath(cgId);
            for (String otId : reader.readEnumArray(cgObjectPath).getValues())
            {
                final int numberOfElements =
                        reader.getIntAttribute(cgObjectPath, NUMBER_OF_ELEMENTS_ATTRIBUTE);
                final ObjectNamespace cgroup = result.tryGetObjectNamespace(cgId);
                cgroup.setOrCheckNumberOfSegmentedObjects(numberOfElements);
                result.addObjectType(otId, cgroup);
            }
        }
        return result;
    }

    public String getDatasetCode()
    {
        return datasetCode;
    }

    public ImageQuantityStructure getImageQuantityStructure()
    {
        return quantityStructure;
    }

    public HDF5TimeDurationArray tryGetTimeSeriesSequenceAnnotation()
    {
        final String objectPath = getTimeSeriesSequenceAnnotationObjectPath();
        return reader.exists(objectPath) ? reader.readTimeDurationArray(objectPath) : null;
    }

    public DepthScanAnnotation tryGetDepthScanSequenceAnnotation()
    {
        final String objectPath = getDepthScanSequenceAnnotationObjectPath();
        final double[] zValues =
                reader.exists(objectPath) ? reader.readDoubleArray(objectPath) : null;
        if (zValues == null)
        {
            return null;
        }
        final String unit = reader.getStringAttribute(objectPath, "unit");
        return new DepthScanAnnotation(unit, zValues);
    }

    public String[] tryGetCustomSequenceAnnotation()
    {
        final String objectPath = getCustomSequenceAnnotationObjectPath();
        return reader.exists(objectPath) ? reader.readStringArray(objectPath) : null;
    }

    public String tryGetPlateBarcode()
    {
        final String objectPath = getObjectPath();
        final String plateBarcodeAttributeName = getPlateBarcodeAttributeName();
        return reader.hasAttribute(objectPath, plateBarcodeAttributeName) ? reader
                .getStringAttribute(objectPath, plateBarcodeAttributeName) : null;
    }

    public String tryGetParentDatasetCode()
    {
        final String objectPath = getObjectPath();
        final String parentDatasetAttributeName = getParentDatasetAttributeName();
        return reader.hasAttribute(objectPath, parentDatasetAttributeName) ? reader
                .getStringAttribute(objectPath, parentDatasetAttributeName) : null;
    }

    public ObjectType tryGetObjectType(String objectTypeId)
    {
        return objectTypeStore.tryGetObjectType(objectTypeId);
    }

    public ObjectType getObjectType(String objectTypeId) throws IllegalArgumentException
    {
        final ObjectType objectType = objectTypeStore.tryGetObjectType(objectTypeId);
        if (objectType == null)
        {
            throw new IllegalArgumentException("Dataset '" + datasetCode
                    + "' doesn't have an object type '" + objectTypeId + "'.");
        }
        return objectType;
    }

    public Collection<ObjectType> getObjectTypes()
    {
        return objectTypeStore.getObjectTypes();
    }

    public ObjectNamespace tryGetObjectNamespace(String objectNamespaceId)
    {
        return objectTypeStore.tryGetObjectNamespace(objectNamespaceId);
    }

    public ObjectNamespace getObjectNamespace(String objectNamespaceId)
    {
        final ObjectNamespace objectNamespace = objectTypeStore.tryGetObjectNamespace(objectNamespaceId);
        if (objectNamespace == null)
        {
            throw new IllegalArgumentException("Dataset '" + datasetCode
                    + "' doesn't have an object namespace '" + objectNamespace + "'.");
        }
        return objectNamespace;
    }

    public Collection<ObjectNamespace> getObjectNamespaces()
    {
        return objectTypeStore.getObjectNamespaces();
    }

    String getObjectTypeCompanionGroupObjectPath(String id)
    {
        return getObjectPath(DATASET_TYPE_DIR, String.format("ObjectTypeCompanionGroup__%s", id));
    }

    String getObjectTypesObjectPath()
    {
        return getObjectPath(DATASET_TYPE_DIR, "Enum_ObjectTypes");
    }

    String getObjectNamespacesObjectPath()
    {
        return getObjectPath(DATASET_TYPE_DIR, "Enum_ObjectNamespaces");
    }

    public Set<String> getDatasetAnnotationKeys()
    {
        return new HashSet<String>(reader.getGroupMembers(getDatasetAnnotationObjectPath()));
    }

    public String tryGetDatasetAnnotation(String annotationKey)
    {
        final String datasetAnnotationObjectPath = getDatasetAnnotationObjectPath(annotationKey);
        return reader.exists(datasetAnnotationObjectPath) ? reader
                .readString(datasetAnnotationObjectPath) : null;
    }

    public int getFormatVersionNumber()
    {
        return formatVersionNumber;
    }

    void checkFormat(String expectedFormatType, String foundFormatType)
    {
        if (expectedFormatType.equals(foundFormatType) == false)
        {
            throw new WrongDatasetFormatException(datasetCode, expectedFormatType, foundFormatType);
        }

    }

    static String getDatasetTypeAttributeName()
    {
        return "datasetType";
    }

    static String getPlateBarcodeAttributeName()
    {
        return "plateBarcode";
    }

    static String getParentDatasetAttributeName()
    {
        return "parentDatasets";
    }

    String getDataTypeName(final String dataTypeName)
    {
        return datasetCode + "_" + dataTypeName;
    }

    String getObjectPath()
    {
        return getDatasetPath(datasetCode);
    }

    String getObjectPath(final String name)
    {
        return getDatasetPath(datasetCode) + "/" + name;
    }

    String getObjectPath(final String dir, final String name)
    {
        return getDatasetPath(datasetCode) + "/" + dir + "/" + name;
    }

    String getTimeSeriesSequenceAnnotationObjectPath()
    {
        return getObjectPath("timeSeriesSequenceAnnotation");
    }

    String getDepthScanSequenceAnnotationObjectPath()
    {
        return getObjectPath("depthScanSequenceAnnotation");
    }

    String getCustomSequenceAnnotationObjectPath()
    {
        return getObjectPath("customSequenceAnnotation");
    }

    String getDatasetAnnotationObjectPath()
    {
        return getObjectPath("datasetAnnotations");
    }

    String getDatasetAnnotationObjectPath(String annotationKey)
    {
        return getObjectPath("datasetAnnotations", annotationKey);
    }

    String getObjectPath(ImageId id, String... prefixes)
    {
        quantityStructure.checkInBounds(id);
        return getObjectPath() + "/" + id.createObjectName(prefixes);
    }

    String getImageQuantityStructureObjectPath()
    {
        return getImageQuantityStructureObjectPath(datasetCode);
    }

    static String getDatasetPath(String datasetCode)
    {
        return "/Dataset_" + datasetCode;
    }

    static String getImageQuantityStructureObjectPath(String datasetCode)
    {
        return getDatasetPath(datasetCode) + "/structure";
    }

}
