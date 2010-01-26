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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server;

import java.util.List;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.generic.server.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IRawDataServiceInternal;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class RawDataServiceInternalLogger extends AbstractServerLogger implements IRawDataServiceInternal
{

    RawDataServiceInternalLogger(ISessionManager<Session> sessionManager,
            boolean invocationSuccessful, long elapsedTime)
    {
        super(sessionManager, invocationSuccessful, elapsedTime);
    }

    public List<Sample> listRawDataSamples(String sessionToken)
    {
        logAccess(sessionToken, "list_raw_data_samples");
        return null;
    }
    
    public void processRawData(String sessionToken, String dataSetProcessingKey, long[] rawDataSampleIDs)
    {
        int numberOfDataSets = rawDataSampleIDs == null ? 0 : rawDataSampleIDs.length;
        logAccess(sessionToken, "copy_raw_data", "NUMBER_OF_DATA_SETS(%s)", numberOfDataSets);
    }

}
