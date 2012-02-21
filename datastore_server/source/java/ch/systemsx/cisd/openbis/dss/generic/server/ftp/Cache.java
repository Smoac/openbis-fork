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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;

/**
 * Helper class to cache objects retrieved from remote services. Used by
 * {@link FtpPathResolverContext}.
 * 
 * @author Franz-Josef Elmer
 */
public class Cache
{
    private final Map<String, DataSet> dataSets = new HashMap<String, DataSet>();
    private final Map<String, ExternalData> externalData = new HashMap<String, ExternalData>();
    private final Map<String, Experiment> experiments = new HashMap<String, Experiment>();
    
    void putDataSet(DataSet dataSet)
    {
        dataSets.put(dataSet.getCode(), dataSet);
    }
    
    DataSet getDataSet(String dataSetCode)
    {
        return dataSets.get(dataSetCode);
    }

    ExternalData getExternalData(String code)
    {
        return externalData.get(code);
    }

    void putDataSet(ExternalData dataSet)
    {
        externalData.put(dataSet.getCode(), dataSet);
    }

    Experiment getExperiment(String experimentId)
    {
        return experiments.get(experimentId);
    }

    void putDataSet(Experiment experiment)
    {
        experiments.put(experiment.getIdentifier(), experiment);
    }
    
}