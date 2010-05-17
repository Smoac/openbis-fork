/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Information about a processing plugin on a Data Store Server (DSS).
 *
 * @author Franz-Josef Elmer
 */
public class DataStoreServerProcessingPluginInfo implements Serializable
{

    private static final long serialVersionUID = 1L;
    private final String key;
    private final String label;
    private final List<String> datasetTypeCodes;

    /**
     * Create a new instance for specified key, label and data set type codes.
     */
    public DataStoreServerProcessingPluginInfo(String key, String label, List<String> datasetTypeCodes)
    {
        this.key = key;
        this.label = label;
        this.datasetTypeCodes = Collections.unmodifiableList(new ArrayList<String>(datasetTypeCodes));
    }

    /**
     * Returns a unique key of the plugin. 
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Returns a human readable name of the plugin.
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * Returns a list data set type codes for all data sets for which the plugin is available.
     */
    public List<String> getDatasetTypeCodes()
    {
        return datasetTypeCodes;
    }
}
