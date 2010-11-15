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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.api.v1;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.DataStoreServerProcessingPluginInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.MsInjectionDataInfo;

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
    public String getSessionToken();

    /**
     * Returns all samples of type MS_INJECTION in space MS_DATA which have a parent sample which
     * the specified user is allow to read.
     */
    public List<MsInjectionDataInfo> listRawDataSamples(String userID);

    /**
     * Lists all processing plugins on DSS.
     */
    public List<DataStoreServerProcessingPluginInfo> listDataStoreServerProcessingPluginInfos();

    /**
     * Processes the data sets of specified samples by the DSS processing plug-in of specified key
     * for the specified user. Only the most recent data sets of specified type are processed.
     */
    @Deprecated
    public void processingRawData(String userID, String dataSetProcessingKey,
            long[] rawDataSampleIDs, String dataSetType);
    
    public void processDataSets(String userID, String dataSetProcessingKey, List<String> dataSetCodes);

    /**
     * Returns all projects where the specified user has USER access rights.
     */
    public List<Project> listProjects(String userID);

    /**
     * Returns all experiments of type <tt>MS_SEARCH</tt> which the specified user is allowed to
     * read.
     */
    public List<Experiment> listSearchExperiments(String userID);

    /**
     * Processes the data sets of specified experiments of type <tt>MS_SEARCH</tt> by the DSS
     * processing plug-in of specified key for the specified user. It will be checked if the
     * experiments are of search experiments and if the user has USER access rights.
     */
    public void processSearchData(String userID, String dataSetProcessingKey,
            long[] searchExperimentIDs);
    

    /**
     * Logs current user out.
     */
    public void logout();

}
