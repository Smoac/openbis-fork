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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
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

    @Override
    public String tryToAuthenticateForAllServices(String userID, String userPassword)
    {
        return null;
    }

    @Override
    public boolean isSessionActive(String sessionToken)
    {
        return false;
    }

    @Override
    public Map<String, Set<Role>> listNamedRoleSets(String sessionToken)
    {
        logAccess(sessionToken, "list-role-sets");
        return null;
    }

    @Override
    public List<SpaceWithProjectsAndRoleAssignments> listSpacesWithProjectsAndRoleAssignments(
            String sessionToken, String databaseInstanceCodeOrNull)
    {
        logAccess(sessionToken, "list-spaces", "DATABASE_INSTANCE(%s)", databaseInstanceCodeOrNull);
        return null;
    }

    @Override
    public int getMajorVersion()
    {
        return 0;
    }

    @Override
    public int getMinorVersion()
    {
        return 0;
    }

    @Override
    public List<Sample> listSamplesForExperiment(String sessionToken,
            String experimentIdentifierString)
    {
        logAccess(sessionToken, "list-samples-for-experiment", "EXPERIMENT_IDENTIFIER(%s)",
                experimentIdentifierString);
        return null;
    }

    @Override
    public List<Sample> searchForSamples(String sessionToken, SearchCriteria searchCriteria)
    {
        logAccess(sessionToken, "search-for-samples", "SEARCH_CRITERIA(%s)", searchCriteria);
        return null;
    }

    @Override
    public List<Sample> searchForSamples(String sessionToken, SearchCriteria searchCriteria,
            EnumSet<SampleFetchOption> fetchOptions)
    {
        logAccess(sessionToken, "search-for-samples", "SEARCH_CRITERIA(%s) FETCH_OPTIONS(%s)",
                searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public List<DataSet> listDataSets(String sessionToken, List<Sample> samples)
    {
        logAccess(sessionToken, "list-data-sets", "SAMPLES(%s)", abbreviate(samples));
        return null;
    }

    @Override
    public List<Experiment> listExperiments(String sessionToken, List<Project> projects,
            String experimentType)
    {
        logAccess(sessionToken, "list-experiments", "EXP_TYPE(%s)", experimentType);
        return null;
    }

    @Override
    public List<Experiment> listExperimentsHavingSamples(String sessionToken,
            List<Project> projects, String experimentType)
    {
        logAccess(sessionToken, "list-experiments-having-samples", "EXP_TYPE(%s)", experimentType);
        return null;
    }

    @Override
    public List<Experiment> listExperimentsHavingDataSets(String sessionToken,
            List<Project> projects, String experimentType)
    {
        logAccess(sessionToken, "list-experiments-having-data-sets", "EXP_TYPE(%s)", experimentType);
        return null;
    }

    @Override
    public List<DataSet> listDataSetsForSample(String sessionToken, Sample sample,
            boolean areOnlyDirectlyConnectedIncluded)
    {
        logAccess(sessionToken, "list-data-sets", "SAMPLE(%s) INCLUDE-CONNECTED(%s)", sample,
                areOnlyDirectlyConnectedIncluded);
        return null;
    }

    @Override
    public String getDefaultPutDataStoreBaseURL(String sessionToken)
    {
        logAccess(sessionToken, "get-default-put-data-store-url");
        return null;
    }

    @Override
    public String tryGetDataStoreBaseURL(String sessionToken, String dataSetCode)
    {
        logAccess(sessionToken, "get-data-store-base-url", "DATA_SET(%s)", dataSetCode);
        return null;
    }

    @Override
    public List<DataSetType> listDataSetTypes(String sessionToken)
    {
        logAccess(sessionToken, "list-data-set-types");
        return null;
    }

    @Override
    public HashMap<Vocabulary, List<VocabularyTerm>> getVocabularyTermsMap(String sessionToken)
    {
        logAccess(sessionToken, "get-vocabulary-terms-map");
        return null;
    }

    @Override
    public List<DataSet> listDataSets(String sessionToken, List<Sample> samples,
            EnumSet<Connections> connectionsToGet)
    {
        logAccess(sessionToken, "list-data-sets", "SAMPLES(%s) CONNECTIONS(%s)",
                abbreviate(samples), connectionsToGet);
        return null;
    }

    @Override
    public List<DataSet> getDataSetMetaData(String sessionToken, List<String> dataSetCodes)
    {
        logAccess(sessionToken, "get-data-set-meta-data", "DATA_SETS(%s)", abbreviate(dataSetCodes));
        return null;
    }

    @Override
    public List<DataSet> getDataSetMetaData(String sessionToken, List<String> dataSetCodes,
            EnumSet<DataSetFetchOption> fetchOptions)
    {
        logAccess(sessionToken, "get-data-set-meta-data",
                "DATA_SETS(%s), DATA_SETS_FETCH_OPTIONS(%s)", abbreviate(dataSetCodes),
                fetchOptions);
        return null;
    }

    @Override
    public List<DataSet> searchForDataSets(String sessionToken, SearchCriteria searchCriteria)
    {
        logAccess(sessionToken, "search-for-data-sets", "SEARCH_CRITERIA(%s)", searchCriteria);
        return null;
    }

    @Override
    public List<Experiment> listExperiments(String sessionToken, List<String> experimentIdentifiers)
    {
        logAccess(sessionToken, "list-experiments", "EXPERIMENT_IDENTIFIERS(%s)",
                abbreviate(experimentIdentifiers));
        return null;
    }

    @Override
    public List<Project> listProjects(String sessionToken)
    {
        logAccess(sessionToken, "list-projects");
        return null;
    }

    @Override
    public List<Project> listProjectsForUser(String sessionToken, String userId)
    {
        logAccess(sessionToken, "list-projects-for-user", "USER_ID(%s)", userId);
        return null;
    }

    @Override
    public List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary> listVocabularies(
            String sessionToken)
    {
        logAccess(sessionToken, "list-vocabularies");
        return null;
    }

    @Override
    public List<DataSet> listDataSetsForExperiments(String sessionToken,
            List<Experiment> experiments, EnumSet<Connections> connectionsToGet)
    {
        logAccess(sessionToken, "list-data-sets-for-experiments",
                "EXPERIMENTS(%s) CONNECTIONS(%s)", abbreviate(experiments), connectionsToGet);
        return null;
    }

    @Override
    public List<Material> getMaterialByCodes(String sessionToken,
            List<MaterialIdentifier> materialIdentifier)
    {
        logAccess(sessionToken, "get-material-by-codes", "MATERIAL_IDENTIFIERS(%s)",
                abbreviate(materialIdentifier));

        return null;
    }

    @Override
    public List<Material> searchForMaterials(String sessionToken, SearchCriteria searchCriteria)
    {
        logAccess(sessionToken, "search-for-materials", "SEARCH_CRITERIA(%s)", searchCriteria);
        return null;
    }
}
