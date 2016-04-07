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

package ch.systemsx.cisd.openbis.plugin.proteomics.client.api.v1;

import java.util.List;

import ch.systemsx.cisd.common.api.retry.Retry;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.api.v1.dto.DataStoreServerProcessingPluginInfo;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.api.v1.dto.MsInjectionDataInfo;

/**
 * Facade for openBIS proteomics data service to be used by a proteomics pipeline server like p-grade.
 * 
 * @author Franz-Josef Elmer
 */
public interface IProteomicsDataApiFacade
{
    /**
     * Return the session token for the logged-in user.
     */
    @Retry
    public String getSessionToken();

    /**
     * Returns all samples of type MS_INJECTION in space MS_DATA which have a parent sample which
     * the specified user is allow to read.
     */
    @Retry
    public List<MsInjectionDataInfo> listRawDataSamples(String userID);

    /**
     * Returns all samples of type MS_INJECTION in space MS_DATA which have a parent sample.
     */
    @Retry
    public List<MsInjectionDataInfo> listAllRawDataSamples(String userID);
    
    /**
     * Lists all processing plugins on DSS.
     */
    @Retry
    public List<DataStoreServerProcessingPluginInfo> listDataStoreServerProcessingPluginInfos();

    /**
     * Processes the data sets of specified samples by the DSS processing plug-in of specified key
     * for the specified user. Only the most recent data sets of specified type are processed.
     */
    @Deprecated
    public void processingRawData(String userID, String dataSetProcessingKey,
            long[] rawDataSampleIDs, String dataSetType);
    
    /**
     * Processes the specified data sets by the DSS processing plug-in of specified key for the
     * specified user. Implementations should check that the specified user is allowed to read
     * specified data sets.
     */
    public void processDataSets(String userID, String dataSetProcessingKey, List<String> dataSetCodes);

    /**
     * Returns all projects where the specified user has USER access rights.
     */
    @Retry
    public List<Project> listProjects(String userID);

    /**
     * Returns all experiments of type <tt>MS_SEARCH</tt> which the specified user is allowed to
     * read.
     */
    @Retry
    public List<Experiment> listSearchExperiments(String userID);

    /**
     * Returns all experiments of specified type which the specified user is allowed to read.
     */
    @Retry
    public List<Experiment> listExperiments(String sessionToken, String userID, String experimentTypeCode);
    
    /**
     * Returns all data sets of specified experiment which the specified user is allowed to read.
     */
    @Retry
    public List<DataSet> listDataSetsByExperiment(String userID, long experimentID);
    
    /**
     * Processes the data sets of specified experiments of type <tt>MS_SEARCH</tt> by the DSS
     * processing plug-in of specified key for the specified user. It will be checked if the
     * experiments are of search experiments and if the user has USER access rights.
     */
    @Deprecated
    public void processSearchData(String userID, String dataSetProcessingKey,
            long[] searchExperimentIDs);
    
    /**
     * Processes the data sets of specified experiments by the DSS
     * processing plug-in of specified key for the specified user. It will be checked if the
     * experiments are of specified type and if the user has USER access rights.
     */
    public void processProteinResultDataSets(String sessionToken, String userID,
            String dataSetProcessingKey, String experimentTypeCode, long[] experimentIDs);
    
    /**
     * Logs current user out.
     */
    public void logout();

}
