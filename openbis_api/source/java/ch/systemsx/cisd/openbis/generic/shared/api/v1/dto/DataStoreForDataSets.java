/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * The data store URL for a list of data sets.
 *
 * @author Bernd Rinn
 */
@JsonObject("DataStoreForDataSets")
public class DataStoreForDataSets implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private final String dataStoreBaseURL;
    
    private final String[] dataSetCodes;

    public DataStoreForDataSets(String dataStoreBaseURL, String[] dataSetCodes)
    {
        this.dataStoreBaseURL = dataStoreBaseURL;
        this.dataSetCodes = dataSetCodes;
    }

    /**
     * The base URL of the data store (can be used for download from a client).
     */
    public String getDataStoreBaseURL()
    {
        return dataStoreBaseURL;
    }

    /**
     * The list of data sets that can be found in this data store.
     */
    public List<String> getDataSetCodes()
    {
        return Arrays.asList(dataSetCodes);
    }

}
