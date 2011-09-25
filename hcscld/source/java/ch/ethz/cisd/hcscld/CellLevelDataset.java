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

import ch.systemsx.cisd.hdf5.IHDF5Reader;


/**
 * Implementation of {@link ICellLevelDataset}.
 *
 * @author Bernd Rinn
 */
public abstract class CellLevelDataset implements ICellLevelDataset
{
    final IHDF5Reader reader;

    final String datasetCode;

    final WellFieldGeometry geometry;

    CellLevelDataset(IHDF5Reader reader, String datasetCode,
            WellFieldGeometry geometry)
    {
        this.reader = reader;
        this.datasetCode = datasetCode;
        this.geometry = geometry;
    }

    public String getDatasetCode()
    {
        return datasetCode;
    }

    public WellFieldGeometry getGeometry()
    {
        return geometry;
    }

    String getDatasetTypeAttributeName()
    {
        return "datasetType";
    }

    String getNameInDataset(final String name)
    {
        return datasetCode + "_" + name;
    }

    String getObjectPath()
    {
        return getObjectPath(datasetCode);
    }

    String getObjectPath(WellFieldId id)
    {
        return getObjectPath() + "/" + id.createObjectName(null);
    }

    String getObjectPath(WellFieldId id, String prefix)
    {
        return getObjectPath() + "/" + id.createObjectName(prefix);
    }

    String getGeometryObjectPath()
    {
        return getGeometryObjectPath(datasetCode);
    }

    static String getObjectPath(String datasetCode)
    {
        return "DataSet_" + datasetCode;
    }

    static String getGeometryObjectPath(String datasetCode)
    {
        return getObjectPath(datasetCode) + "/geometry";
    }

}
