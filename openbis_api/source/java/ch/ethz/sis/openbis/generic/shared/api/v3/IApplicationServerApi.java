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

package ch.ethz.sis.openbis.generic.shared.api.v3;

import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.Deletion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.dataset.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.experiment.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.material.MaterialDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.project.ProjectDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.sample.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.space.SpaceDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetUpdate;
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
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.operation.IOperation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.DataSetSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ExperimentSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.MaterialSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ProjectSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SampleSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SpaceSearchCriterion;
import ch.systemsx.cisd.common.api.IRpcService;

/**
 * @author pkupczyk
 */
public interface IApplicationServerApi extends IRpcService
{
    /**
     * Name of this service for which it is registered as Spring bean
     */
    public static final String INTERNAL_SERVICE_NAME = "application-server_INTERNAL";

    /**
     * Name of this service for which it is registered at the RPC name server.
     */
    public static final String SERVICE_NAME = "application-server";

    /**
     * Application part of the URL to access this service remotely.
     */
    public static final String SERVICE_URL = "/rmi-" + SERVICE_NAME + "-v3";

    public static final String JSON_SERVICE_URL = SERVICE_URL + ".json";

    public String login(String userId, String password);

    public String loginAs(String userId, String password, String asUserId);

    public void logout(String sessionToken);

    public List<? extends IOperationResult> performOperations(String sessionToken, List<? extends IOperation> operations);

    public List<SpacePermId> createSpaces(String sessionToken, List<SpaceCreation> newSpaces);

    public List<ProjectPermId> createProjects(String sessionToken, List<ProjectCreation> newProjects);

    // REPLACES:
    // - ServiceForDataStoreServer.registerExperiment()

    public List<ExperimentPermId> createExperiments(String sessionToken, List<ExperimentCreation> newExperiments);

    // REPLACES:
    // - ServiceForDataStoreServer.registerSamples()
    // - ServiceForDataStoreServer.registerSample()

    public List<SamplePermId> createSamples(String sessionToken, List<SampleCreation> newSamples);

    public List<MaterialPermId> createMaterials(String sessionToken, List<MaterialCreation> newMaterials);

    public void updateSpaces(String sessionToken, List<SpaceUpdate> spaceUpdates);

    public void updateProjects(String sessionToken, List<ProjectUpdate> projectUpdates);

    // REPLACES:
    // - ServiceForDataStoreServer.updateExperiment()

    public void updateExperiments(String sessionToken, List<ExperimentUpdate> experimentUpdates);

    // REPLACES:
    // - ServiceForDataStoreServer.updateSamples()
    // - ServiceForDataStoreServer.updateSample()

    public void updateSamples(String sessionToken, List<SampleUpdate> sampleUpdates);

    // REPLACES:
    // - ServiceForDataStoreServer.updateDataSet()

    public void updateDataSets(String sessionToken, List<DataSetUpdate> dataSetUpdates);

    public void updateMaterials(String sessionToken, List<MaterialUpdate> materialUpdates);

    public Map<ISpaceId, Space> mapSpaces(String sessionToken, List<? extends ISpaceId> spaceIds,
            SpaceFetchOptions fetchOptions);

    public Map<IProjectId, Project> mapProjects(String sessionToken, List<? extends IProjectId> projectIds,
            ProjectFetchOptions fetchOptions);

    // REPLACES:
    // - ServiceForDataStoreServer.tryGetExperiment(ExperimentIdentifier)
    // - ServiceForDataStoreServer.listExperiments(List<ExperimentIdentifier>, ExperimentFetchOptions)
    // - GeneralInformationService.listExperiments(List<String> experimentIdentifiers)

    public Map<IExperimentId, Experiment> mapExperiments(String sessionToken, List<? extends IExperimentId> experimentIds,
            ExperimentFetchOptions fetchOptions);

    // REPLACES:
    // - ServiceForDataStoreServer.tryGetSampleWithExperiment(SampleIdentifier)
    // - ServiceForDataStoreServer.listSamplesByPermId(List<String>)
    // - ServiceForDataStoreServer.tryGetPropertiesOfSample(SampleIdentifier)

    public Map<ISampleId, Sample> mapSamples(String sessionToken, List<? extends ISampleId> sampleIds, SampleFetchOptions fetchOptions);

    public Map<IDataSetId, DataSet> mapDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetFetchOptions fetchOptions);

    public Map<IMaterialId, Material> mapMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialFetchOptions fetchOptions);

    public List<Space> searchSpaces(String sessionToken, SpaceSearchCriterion searchCriterion, SpaceFetchOptions fetchOptions);

    public List<Project> searchProjects(String sessionToken, ProjectSearchCriterion searchCriterion, ProjectFetchOptions fetchOptions);

    // REPLACES:
    // - ServiceForDataStoreServer.listExperimentsForProjects(List<ProjectIdentifier>, ExperimentFetchOptions)
    // - ServiceForDataStoreServer.listExperiments(ProjectIdentifier)
    // - GeneralInformationService.listExperiments(List<Project>, String experimentType)
    // - GeneralInformationService.listExperimentsHavingDataSets(List<Project>, String experimentType) - TODO add "HAVING_DATASETS" criteria
    // - GeneralInformationService.listExperimentsHavingSamples(List<Project>, String experimentType) - TODO add "HAVING_SAMPLES" criteria
    // - GeneralInformationService.searchForExperiments(SearchCriteria)

    public List<Experiment> searchExperiments(String sessionToken, ExperimentSearchCriterion searchCriterion, ExperimentFetchOptions fetchOptions);

    // REPLACES:
    // - ServiceForDataStoreServer.listSamples(ListSampleCriteria)
    // - ServiceForDataStoreServer.listSamplesByCriteria(ListSamplesByPropertyCriteria)
    // - ServiceForDataStoreServer.searchForSamples(SearchCriteria)
    // - GeneralInformationService.searchForSamples(SearchCriteria)
    // - GeneralInformationService.searchForSamples(SearchCriteria, EnumSet<SampleFetchOption>)
    // - GeneralInformationService.listSamplesForExperiment(String experimentIdentifier)
    /**
     * This is the comment
     * 
     * @param sessionToken
     * @param searchCriterion
     * @param fetchOptions
     */
    public List<Sample> searchSamples(String sessionToken, SampleSearchCriterion searchCriterion, SampleFetchOptions fetchOptions);

    public List<DataSet> searchDataSets(String sessionToken, DataSetSearchCriterion searchCriterion, DataSetFetchOptions fetchOptions);

    public List<Material> searchMaterials(String sessionToken, MaterialSearchCriterion searchCriterion, MaterialFetchOptions fetchOptions);

    public void deleteSpaces(String sessionToken, List<? extends ISpaceId> spaceIds, SpaceDeletionOptions deletionOptions);

    public void deleteProjects(String sessionToken, List<? extends IProjectId> projectIds, ProjectDeletionOptions deletionOptions);

    // REPLACES:
    // - IGeneralInformationChangingService.deleteExperiments(List<Long>, String, DeletionType)
    public IDeletionId deleteExperiments(String sessionToken, List<? extends IExperimentId> experimentIds, ExperimentDeletionOptions deletionOptions);

    // REPLACES:
    // - IGeneralInformationChangingService.deleteSamples(List<Long>, String, DeletionType)
    public IDeletionId deleteSamples(String sessionToken, List<? extends ISampleId> sampleIds, SampleDeletionOptions deletionOptions);

    public IDeletionId deleteDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetDeletionOptions deletionOptions);

    public void deleteMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialDeletionOptions deletionOptions);

    // REPLACES:
    // - IGeneralInformationService.listDeletions(EnumSet<DeletionFetchOption>)
    public List<Deletion> listDeletions(String sessionToken, DeletionFetchOptions fetchOptions);

    // REPLACES:
    // - IGeneralInformationChangingService.revertDeletions(List<Long>)
    public void revertDeletions(String sessionToken, List<? extends IDeletionId> deletionIds);

    // REPLACES:
    // - IGeneralInformationChangingService.deletePermanently(List<Long>)
    public void confirmDeletions(String sessionToken, List<? extends IDeletionId> deletionIds);

}
