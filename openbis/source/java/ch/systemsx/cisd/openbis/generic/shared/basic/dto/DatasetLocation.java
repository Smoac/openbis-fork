/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;

/**
 * Encapsulates location and code of a data set in a store.
 * 
 * @author Piotr Buczek
 */
public class DatasetLocation implements IDatasetLocation, ISerializable, ICodeHolder
{
    private static final long serialVersionUID = 1L;

    private String datasetCode;

    private String dataSetLocation;

    public String getDataSetLocation()
    {
        return dataSetLocation;
    }

    public void setDataSetLocation(String dataSetLocation)
    {
        this.dataSetLocation = dataSetLocation;
    }

    public String getDataSetCode()
    {
        return datasetCode;
    }

    public void setDatasetCode(String datasetCode)
    {
        this.datasetCode = datasetCode;
    }

    @Override
    public String toString()
    {
        return "Dataset[" + datasetCode + "], location[" + dataSetLocation + "]";
    }

    public String getCode()
    {
        return getDataSetCode();
    }

}
