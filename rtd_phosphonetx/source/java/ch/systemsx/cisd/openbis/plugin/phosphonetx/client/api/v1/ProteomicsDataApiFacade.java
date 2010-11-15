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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.IProteomicsDataService;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.DataStoreServerProcessingPluginInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.MsInjectionDataInfo;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class ProteomicsDataApiFacade implements IProteomicsDataApiFacade
{
    private static final String USER_ROLE_SET = "SPACE_USER";
    private final IProteomicsDataService service;
    private final IGeneralInformationService generalInfoService;
    private final String sessionToken;

    ProteomicsDataApiFacade(IProteomicsDataService service, IGeneralInformationService generalInfoService, String sessionToken)
    {
        this.service = service;
        this.generalInfoService = generalInfoService;
        this.sessionToken = sessionToken;
    }

    public String getSessionToken()
    {
        return sessionToken;
    }

    public List<DataStoreServerProcessingPluginInfo> listDataStoreServerProcessingPluginInfos()
    {
        return service.listDataStoreServerProcessingPluginInfos(sessionToken);
    }

    public List<MsInjectionDataInfo> listRawDataSamples(String userID)
    {
        return service.listRawDataSamples(sessionToken, userID);
    }

    public void processingRawData(String userID, String dataSetProcessingKey,
            long[] rawDataSampleIDs, String dataSetType)
    {
        service.processingRawData(sessionToken, userID, dataSetProcessingKey, rawDataSampleIDs,
                dataSetType);
    }

    public void processDataSets(String userID, String dataSetProcessingKey,
            List<String> dataSetCodes)
    {
        service.processDataSets(sessionToken, userID, dataSetProcessingKey, dataSetCodes);
    }

    public List<Project> listProjects(String userID)
    {
        Map<String, Set<Role>> namedRoleSets = generalInfoService.listNamedRoleSets(sessionToken);
        Set<Role> allowedRoles = namedRoleSets.get(USER_ROLE_SET);
        if (allowedRoles == null)
        {
            throw new IllegalStateException("Role set " + USER_ROLE_SET + " not known.");
        }
        
        List<SpaceWithProjectsAndRoleAssignments> spaces =
                generalInfoService.listSpacesWithProjectsAndRoleAssignments(sessionToken, null);
        List<Project> projects = new ArrayList<Project>();
        for (SpaceWithProjectsAndRoleAssignments space : spaces)
        {
            Set<Role> roles = space.getRoles(userID);
            roles.retainAll(allowedRoles);
            if (roles.isEmpty() == false)
            {
                for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project project : space
                        .getProjects())
                {
                    projects.add(new Project(space.getCode(), project.getCode()));
                }
            }
        }
        return projects;
    }

    public List<Experiment> listSearchExperiments(String userID)
    {
        return service.listSearchExperiments(sessionToken, userID);
    }

    public void processSearchData(String userID, String dataSetProcessingKey,
            long[] searchExperimentIDs)
    {
        service.processSearchData(sessionToken, userID, dataSetProcessingKey, searchExperimentIDs);
    }

    public void logout()
    {
        generalInfoService.logout(sessionToken);
    }
    
}
