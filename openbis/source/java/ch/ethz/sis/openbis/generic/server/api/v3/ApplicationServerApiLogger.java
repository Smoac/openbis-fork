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

import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.shared.api.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.deletion.Deletion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.deletion.dataset.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.deletion.experiment.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.deletion.material.MaterialDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.deletion.project.ProjectDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.deletion.sample.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.deletion.space.SpaceDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.MaterialCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.MaterialUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.ProjectCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.ProjectUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.Space;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.SpaceCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.SpaceUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.deletion.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.project.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.IDataSetId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.IDeletionId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.IMaterialId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.MaterialPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SearchResult;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SpaceSearchCriteria;
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
    public String loginAs(String userId, String password, String asUser)
    {
        return null;
    }

    @Override
    public List<SpacePermId> createSpaces(String sessionToken, List<SpaceCreation> newSpaces)
    {
        logAccess(sessionToken, "create-spaces", "NEW_SPACES(%s)", newSpaces);
        return null;
    }

    @Override
    public List<ProjectPermId> createProjects(String sessionToken, List<ProjectCreation> newProjects)
    {
        logAccess(sessionToken, "create-projects", "NEW_PROJECTS(%s)", newProjects);
        return null;
    }

    @Override
    public List<MaterialPermId> createMaterials(String sessionToken, List<MaterialCreation> newMaterials)
    {
        logAccess(sessionToken, "create-materials", "NEW_MATERIALS(%s)", newMaterials);
        return null;
    }

    @Override
    public List<ExperimentPermId> createExperiments(String sessionToken, List<ExperimentCreation> newExperiments)
    {
        logAccess(sessionToken, "create-experiments", "NEW_EXPERIMENTS(%s)", newExperiments);
        return null;
    }

    @Override
    public List<SamplePermId> createSamples(String sessionToken, List<SampleCreation> newSamples)
    {
        logAccess(sessionToken, "create-samples", "NEW_SAMPLES(%s)", newSamples);
        return null;
    }

    @Override
    public void updateSpaces(String sessionToken, List<SpaceUpdate> spaceUpdates)
    {
        logAccess(sessionToken, "update-spaces", "SPACE_UPDATES(%s)", spaceUpdates);
    }

    @Override
    public void updateProjects(String sessionToken, List<ProjectUpdate> projectUpdates)
    {
        logAccess(sessionToken, "update-projects", "PROJECT_UPDATES(%s)", projectUpdates);
    }

    @Override
    public void updateExperiments(String sessionToken, List<ExperimentUpdate> experimentUpdates)
    {
        logAccess(sessionToken, "update-experiments", "EXPERIMENT_UPDATES(%s)", experimentUpdates);
    }

    @Override
    public void updateSamples(String sessionToken, List<SampleUpdate> sampleUpdates)
    {
        logAccess(sessionToken, "update-samples", "SAMPLE_UPDATES(%s)", sampleUpdates);
    }

    @Override
    public void updateDataSets(String sessionToken, List<DataSetUpdate> dataSetUpdates)
    {
        logAccess(sessionToken, "update-data-sets", "DATA_SET_UPDATES(%s)", dataSetUpdates);
    }

    @Override
    public void updateMaterials(String sessionToken, List<MaterialUpdate> materialUpdates)
    {
        logAccess(sessionToken, "update-materials", "MATERIAL_UPDATES(%s)", materialUpdates);
    }

    @Override
    public Map<ISpaceId, Space> mapSpaces(String sessionToken, List<? extends ISpaceId> spaceIds, SpaceFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "map-spaces", "SPACE_IDS(%s) FETCH_OPTIONS(%s)", spaceIds, fetchOptions);
        return null;
    }

    @Override
    public Map<IProjectId, Project> mapProjects(String sessionToken, List<? extends IProjectId> projectIds, ProjectFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "map-projects", "PROJECT_IDS(%s) FETCH_OPTIONS(%s)", projectIds, fetchOptions);
        return null;
    }

    @Override
    public Map<IExperimentId, Experiment> mapExperiments(String sessionToken, List<? extends IExperimentId> experimentIds,
            ExperimentFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "map-experiments", "EXPERIMENT_IDS(%s) FETCH_OPTIONS(%s)", experimentIds, fetchOptions);
        return null;
    }

    @Override
    public Map<ISampleId, Sample> mapSamples(String sessionToken,
            List<? extends ISampleId> sampleIds, SampleFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "map-samples", "SAMPLE_IDS(%s) FETCH_OPTIONS(%s)", sampleIds, fetchOptions);
        return null;
    }

    @Override
    public Map<IMaterialId, Material> mapMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "map-materials", "MATERIAL_IDS(%s) FETCH_OPTIONS(%s)", materialIds, fetchOptions);
        return null;
    }

    @Override
    public Map<IDataSetId, DataSet> mapDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "map-data-sets", "DATA_SET_IDS(%s) FETCH_OPTIONS(%s)", dataSetIds, fetchOptions);
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
        logAccess(sessionToken, "delete-spaces", "SPACE_IDS(%s) DELETION_OPTIONS(%s)", spaceIds, deletionOptions);
    }

    @Override
    public void deleteProjects(String sessionToken, List<? extends IProjectId> projectIds, ProjectDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-projects", "PROJECT_IDS(%s) DELETION_OPTIONS(%s)", projectIds, deletionOptions);
    }

    @Override
    public IDeletionId deleteExperiments(String sessionToken, List<? extends IExperimentId> experimentIds, ExperimentDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-experiments", "EXPERIMENT_IDS(%s) DELETION_OPTIONS(%s)", experimentIds, deletionOptions);
        return null;
    }

    @Override
    public IDeletionId deleteSamples(String sessionToken, List<? extends ISampleId> sampleIds, SampleDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-samples", "SAMPLE_IDS(%s) DELETION_OPTIONS(%s)", sampleIds, deletionOptions);
        return null;
    }

    @Override
    public IDeletionId deleteDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-data-sets", "DATA_SET_IDS(%s) DELETION_OPTIONS(%s)", dataSetIds, deletionOptions);
        return null;
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
        logAccess(sessionToken, "revert-deletions", "DELETION_IDS(%s)", deletionIds);
    }

    @Override
    public void confirmDeletions(String sessionToken, List<? extends IDeletionId> deletionIds)
    {
        logAccess(sessionToken, "confirm-deletions", "DELETION_IDS(%s)", deletionIds);
    }

    @Override
    public void deleteMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-materials", "MATERIAL_IDS(%s) DELETION_OPTIONS(%s)", materialIds, deletionOptions);
    }

}
