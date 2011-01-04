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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author Franz-Josef Elmer
 */
class GeneralInformationServiceLogger extends AbstractServerLogger implements
        IGeneralInformationService
{
    public GeneralInformationServiceLogger(ISessionManager<Session> sessionManager,
            IInvocationLoggerContext context)
    {
        super(sessionManager, context);
    }

    public String tryToAuthenticateForAllServices(String userID, String userPassword)
    {
        return null;
    }

    public boolean isSessionActive(String sessionToken)
    {
        return false;
    }

    public Map<String, Set<Role>> listNamedRoleSets(String sessionToken)
    {
        logAccess(sessionToken, "list-role-sets");
        return null;
    }

    public List<SpaceWithProjectsAndRoleAssignments> listSpacesWithProjectsAndRoleAssignments(
            String sessionToken, String databaseInstanceCodeOrNull)
    {
        logAccess(sessionToken, "list-spaces", "DATABASE_INSTANCE(%s)", databaseInstanceCodeOrNull);
        return null;
    }

    public int getMajorVersion()
    {
        return 0;
    }

    public int getMinorVersion()
    {
        return 0;
    }

    public List<Sample> searchForSamples(String sessionToken, SearchCriteria searchCriteria)
    {
        logAccess(sessionToken, "search-for-samples", "SEARCH_CRITERIA(%s)", searchCriteria);
        return null;
    }

    public List<DataSet> listDataSets(String sessionToken, List<Sample> samples)
    {
        logAccess(sessionToken, "list-data-sets", "SAMPLES(%s)", samples);
        return null;
    }

    public List<Experiment> listExperiments(String sessionToken, List<Project> projects,
            String experimentType)
    {
        logAccess(sessionToken, "list-experiments", "EXP_TYPE(%s)", experimentType);
        return null;
    }

    public List<DataSet> listDataSetsForSample(String sessionToken, Sample sample,
            boolean areOnlyDirectlyConnectedIncluded)
    {
        logAccess(sessionToken, "list-data-sets", "SAMPLE(%s) INCLUDE-CONNECTED(%s)", sample,
                areOnlyDirectlyConnectedIncluded);
        return null;
    }

    public String getDefaultPutDataStoreBaseURL(String sessionToken)
    {
        logAccess(sessionToken, "get-default-put-data-store-url");
        return null;
    }

    public String tryGetDataStoreBaseURL(String sessionToken, String dataSetCode)
    {
        logAccess(sessionToken, "get-data-store-base-url", "DATA_SET(%s)", dataSetCode);
        return null;
    }

}
