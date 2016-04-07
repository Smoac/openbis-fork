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

package ch.systemsx.cisd.openbis.plugin.proteomics.shared;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.dto.MsInjectionSample;

/**
 * @author Franz-Josef Elmer
 */
public interface IProteomicsDataServiceInternal extends IServer
{
    /**
     * Returns all samples of type MS_INJECTION in group MS_DATA which have a parent sample which
     * the specified user is allow to read.
     */
    @Transactional
    public List<MsInjectionSample> listRawDataSamples(String sessionToken);

    /**
     * Returns all samples of type MS_INJECTION in group MS_DATA which have a parent sample.
     */
    @Transactional
    public List<MsInjectionSample> listAllRawDataSamples(String sessionToken);
    
    @Deprecated
    @Transactional
    public void processRawData(String sessionToken, String dataSetProcessingKey,
            long[] rawDataSampleIDs, String dataSetType);

    @Transactional
    public void processDataSets(String sessionToken, String dataSetProcessingKey,
            List<String> dataSetCodes);

    @Transactional
    public List<Experiment> listExperiments(String sessionToken, String experimentTypeCode);

    @Transactional
    public List<AbstractExternalData> listDataSetsByExperiment(String sessionToken, TechId experimentID);

    @Transactional
    public void processProteinResultDataSets(String sessionToken, String dataSetProcessingKey,
            String experimentTypeCode, long[] searchExperimentIDs);

}
