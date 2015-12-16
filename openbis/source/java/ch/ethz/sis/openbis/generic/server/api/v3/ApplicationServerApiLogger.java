/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.as.api.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.deletion.Deletion;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.deletion.fetchoptions.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.material.create.MaterialCreation;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.material.delete.MaterialDeletionOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.material.update.MaterialUpdate;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.project.delete.ProjectDeletionOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.project.update.ProjectUpdate;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.service.Service;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.service.fetchoptions.ServiceFetchOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.service.id.IServiceId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.space.delete.SpaceDeletionOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.space.update.SpaceUpdate;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
public class ApplicationServerApiLogger extends AbstractServerLogger implements
        IApplicationServerApi
{
    public ApplicationServerApiLogger(ISessionManager<Session> sessionManager,
            IInvocationLoggerContext context)
    {
        super(sessionManager, context);
    }

    @Override
    public int getMajorVersion()
    {
        return 3;
    }

    @Override
    public int getMinorVersion()
    {
        return 0;
    }

    @Override
    public String login(String userId, String password)
    {
        return null;
    }

    @Override
    public String loginAsAnonymousUser()
    {
        return null;
    }

    @Override
    public String loginAs(String userId, String password, String asUser)
    {
        return null;
    }

    @Override
    public List<SpacePermId> createSpaces(String sessionToken, List<SpaceCreation> newSpaces)
    {
        logAccess(sessionToken, "create-spaces", "NEW_SPACES(%s)", abbreviate(newSpaces));
        return null;
    }

    @Override
    public List<ProjectPermId> createProjects(String sessionToken, List<ProjectCreation> newProjects)
    {
        logAccess(sessionToken, "create-projects", "NEW_PROJECTS(%s)", abbreviate(newProjects));
        return null;
    }

    @Override
    public List<MaterialPermId> createMaterials(String sessionToken, List<MaterialCreation> newMaterials)
    {
        logAccess(sessionToken, "create-materials", "NEW_MATERIALS(%s)", abbreviate(newMaterials));
        return null;
    }

    @Override
    public List<ExperimentPermId> createExperiments(String sessionToken, List<ExperimentCreation> newExperiments)
    {
        logAccess(sessionToken, "create-experiments", "NEW_EXPERIMENTS(%s)", abbreviate(newExperiments));
        return null;
    }

    @Override
    public List<SamplePermId> createSamples(String sessionToken, List<SampleCreation> newSamples)
    {
        logAccess(sessionToken, "create-samples", "NEW_SAMPLES(%s)", abbreviate(newSamples));
        return null;
    }

    @Override
    public List<DataSetPermId> createDataSets(String sessionToken, List<DataSetCreation> newDataSets)
    {
        logAccess(sessionToken, "create-data-sets", "NEW_DATA_SETS(%s)", abbreviate(newDataSets));
        return null;
    }

    @Override
    public void updateSpaces(String sessionToken, List<SpaceUpdate> spaceUpdates)
    {
        logAccess(sessionToken, "update-spaces", "SPACE_UPDATES(%s)", abbreviate(spaceUpdates));
    }

    @Override
    public void updateProjects(String sessionToken, List<ProjectUpdate> projectUpdates)
    {
        logAccess(sessionToken, "update-projects", "PROJECT_UPDATES(%s)", abbreviate(projectUpdates));
    }

    @Override
    public void updateExperiments(String sessionToken, List<ExperimentUpdate> experimentUpdates)
    {
        logAccess(sessionToken, "update-experiments", "EXPERIMENT_UPDATES(%s)", abbreviate(experimentUpdates));
    }

    @Override
    public void updateSamples(String sessionToken, List<SampleUpdate> sampleUpdates)
    {
        logAccess(sessionToken, "update-samples", "SAMPLE_UPDATES(%s)", abbreviate(sampleUpdates));
    }

    @Override
    public void updateDataSets(String sessionToken, List<DataSetUpdate> dataSetUpdates)
    {
        logAccess(sessionToken, "update-data-sets", "DATA_SET_UPDATES(%s)", abbreviate(dataSetUpdates));
    }

    @Override
    public void updateMaterials(String sessionToken, List<MaterialUpdate> materialUpdates)
    {
        logAccess(sessionToken, "update-materials", "MATERIAL_UPDATES(%s)", abbreviate(materialUpdates));
    }

    @Override
    public Map<ISpaceId, Space> mapSpaces(String sessionToken, List<? extends ISpaceId> spaceIds, SpaceFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "map-spaces", "SPACE_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(spaceIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IProjectId, Project> mapProjects(String sessionToken, List<? extends IProjectId> projectIds, ProjectFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "map-projects", "PROJECT_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(projectIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IExperimentId, Experiment> mapExperiments(String sessionToken, List<? extends IExperimentId> experimentIds,
            ExperimentFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "map-experiments", "EXPERIMENT_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(experimentIds), fetchOptions);
        return null;
    }

    @Override
    public Map<ISampleId, Sample> mapSamples(String sessionToken,
            List<? extends ISampleId> sampleIds, SampleFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "map-samples", "SAMPLE_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(sampleIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IMaterialId, Material> mapMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "map-materials", "MATERIAL_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(materialIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IDataSetId, DataSet> mapDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "map-data-sets", "DATA_SET_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(dataSetIds), fetchOptions);
        return null;
    }

    @Override
    public SearchResult<Space> searchSpaces(String sessionToken, SpaceSearchCriteria searchCriteria, SpaceFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-for-spaces", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<Project> searchProjects(String sessionToken, ProjectSearchCriteria searchCriteria, ProjectFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-for-projects", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<Experiment> searchExperiments(String sessionToken, ExperimentSearchCriteria searchCriteria,
            ExperimentFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-for-experiments", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<Sample> searchSamples(String sessionToken, SampleSearchCriteria searchCriteria, SampleFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-for-samples", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<DataSet> searchDataSets(String sessionToken, DataSetSearchCriteria searchCriteria, DataSetFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-for-data-sets", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<Material> searchMaterials(String sessionToken, MaterialSearchCriteria searchCriteria, MaterialFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-for-materials", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public void deleteSpaces(String sessionToken, List<? extends ISpaceId> spaceIds, SpaceDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-spaces", "SPACE_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(spaceIds), deletionOptions);
    }

    @Override
    public void deleteProjects(String sessionToken, List<? extends IProjectId> projectIds, ProjectDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-projects", "PROJECT_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(projectIds), deletionOptions);
    }

    @Override
    public IDeletionId deleteExperiments(String sessionToken, List<? extends IExperimentId> experimentIds, ExperimentDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-experiments", "EXPERIMENT_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(experimentIds), deletionOptions);
        return null;
    }

    @Override
    public IDeletionId deleteSamples(String sessionToken, List<? extends ISampleId> sampleIds, SampleDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-samples", "SAMPLE_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(sampleIds), deletionOptions);
        return null;
    }

    @Override
    public IDeletionId deleteDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-data-sets", "DATA_SET_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(dataSetIds), deletionOptions);
        return null;
    }

    @Override
    public void deleteMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-materials", "MATERIAL_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(materialIds), deletionOptions);
    }

    @Override
    public List<Deletion> listDeletions(String sessionToken, DeletionFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "list-deletions", "FETCH_OPTIONS(%s)", fetchOptions);
        return null;
    }

    @Override
    public void revertDeletions(String sessionToken, List<? extends IDeletionId> deletionIds)
    {
        logAccess(sessionToken, "revert-deletions", "DELETION_IDS(%s)", abbreviate(deletionIds));
    }

    @Override
    public void confirmDeletions(String sessionToken, List<? extends IDeletionId> deletionIds)
    {
        logAccess(sessionToken, "confirm-deletions", "DELETION_IDS(%s)", abbreviate(deletionIds));
    }

    @Override
    public List<Service> listServices(String sessionToken, ServiceFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "list-services", "FETCH_OPTIONS(%s)", fetchOptions);
        return null;
    }

    @Override
    public Serializable executeService(String sessionToken, IServiceId serviceId, Map<String, String> parameters)
    {
        logAccess(sessionToken, "execute-service", "SERVICE_ID(%s) PARAMETERS(%)", serviceId, parameters);
        return null;
    }

}
