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

package ch.systemsx.cisd.openbis.plugin.proteomics.server;

import java.util.List;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.IProteomicsDataServiceInternal;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.dto.MsInjectionSample;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class ProteomicsDataServiceInternalLogger extends AbstractServerLogger implements
        IProteomicsDataServiceInternal
{

    ProteomicsDataServiceInternalLogger(ISessionManager<Session> sessionManager,
            IInvocationLoggerContext context)
    {
        super(sessionManager, context);
    }

    @Override
    public List<MsInjectionSample> listRawDataSamples(String sessionToken)
    {
        logAccess(sessionToken, "list_raw_data_samples");
        return null;
    }
    
    @Override
    public List<MsInjectionSample> listAllRawDataSamples(String sessionToken)
    {
        logAccess(sessionToken, "list_all_raw_data_samples");
        return null;
    }
    
    @Override
    public void processRawData(String sessionToken, String dataSetProcessingKey,
            long[] rawDataSampleIDs, String dataSetType)
    {
        int numberOfDataSets = rawDataSampleIDs == null ? 0 : rawDataSampleIDs.length;
        logAccess(sessionToken, "copy_raw_data", "NUMBER_OF_DATA_SETS(%s), DATA_SET_TYPE(%s)",
                numberOfDataSets, dataSetType);
    }

    @Override
    public void processDataSets(String sessionToken, String dataSetProcessingKey,
            List<String> dataSetCodes)
    {
        logAccess(sessionToken, "process_data_sets",
                "DSS_PROCESSING_PLUGIN(%s) NUMBER_OF_DATA_SETS(%s)", dataSetProcessingKey,
                dataSetCodes.size());
    }

    @Override
    public List<Experiment> listExperiments(String sessionToken, String experimentTypeCode)
    {
        logAccess(sessionToken, "list_search_experiments", "EXPERIMENT_TYPE(%s)", experimentTypeCode);
        return null;
    }

    @Override
    public List<AbstractExternalData> listDataSetsByExperiment(String sessionToken, TechId experimentID)
    {
        logAccess(sessionToken, "list_data_sets_by_experiments", "EXPERIMENT_ID(%s)", experimentID);
        return null;
    }

    @Override
    public void processProteinResultDataSets(String sessionToken, String dataSetProcessingKey,
            String experimentTypeCode, long[] searchExperimentIDs)
    {
        int experimentCount = searchExperimentIDs == null ? 0 : searchExperimentIDs.length;
        logAccess(sessionToken, "process_protein_result_data_sets",
                "DSS_PROCESSING_PLUGIN(%s) EXPERIMENT_TYPE(%s) NUMBER_OF_EXPERIMENTS(%s)",
                dataSetProcessingKey, experimentTypeCode, experimentCount);
    }

}
