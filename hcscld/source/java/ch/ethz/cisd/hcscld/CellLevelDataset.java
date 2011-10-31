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

import java.util.HashSet;
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
    final IHDF5Reader reader;

    final String datasetCode;

    final ImageQuantityStructure quantityStructure;

    CellLevelDataset(IHDF5Reader reader, String datasetCode,
            ImageQuantityStructure quantityStructure)
    {
        this.reader = reader;
        this.datasetCode = datasetCode;
        this.quantityStructure = quantityStructure;
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

    String getDatasetTypeAttributeName()
    {
        return "datasetType";
    }

    String getPlateBarcodeAttributeName()
    {
        return "plateBarcode";
    }

    String getParentDatasetAttributeName()
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

    String getObjectPath(ImageId id)
    {
        return getObjectPath(id, null);
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

    String getObjectPath(ImageId id, String prefix)
    {
        quantityStructure.checkInBounds(id);
        return getObjectPath() + "/" + id.createObjectName(prefix);
    }

    String getImageQuantityStructureObjectPath()
    {
        return getImageQuantityStructureObjectPath(datasetCode);
    }

    static String getDatasetPath(String datasetCode)
    {
        return "DataSet_" + datasetCode;
    }

    static String getImageQuantityStructureObjectPath(String datasetCode)
    {
        return getDatasetPath(datasetCode) + "/structure";
    }

}
