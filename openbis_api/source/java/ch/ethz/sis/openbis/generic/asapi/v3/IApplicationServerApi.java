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

package ch.ethz.sis.openbis.generic.asapi.v3;

import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.archive.DataSetArchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unarchive.DataSetUnarchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.DataStore;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.fetchoptions.DataStoreFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.Deletion;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.fetchoptions.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.search.DeletionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.create.ExternalDmsCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.delete.ExternalDmsDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.fetchoptions.ExternalDmsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.ExternalDmsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.update.ExternalDmsUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.MaterialDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.MaterialUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.ObjectKindModification;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.fetchoptions.ObjectKindModificationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.search.ObjectKindModificationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionResults;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.delete.OperationExecutionDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.IOperationExecutionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.search.OperationExecutionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.update.OperationExecutionUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.delete.ProjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.CustomASServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.ICustomASServiceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.CustomASServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.delete.SpaceDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.SpaceUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.delete.TagDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.TagSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.update.TagUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyTermCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete.VocabularyTermDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.VocabularyTermUpdate;
import ch.systemsx.cisd.common.annotation.TechPreview;
import ch.systemsx.cisd.common.api.IRpcService;

/**
 * @author pkupczyk
 */
public interface IApplicationServerApi extends IRpcService
{
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

    public String loginAsAnonymousUser();

    public void logout(String sessionToken);

    public SessionInformation getSessionInformation(String sessionToken);

    public List<SpacePermId> createSpaces(String sessionToken, List<SpaceCreation> newSpaces);

    public List<ProjectPermId> createProjects(String sessionToken, List<ProjectCreation> newProjects);

    public List<ExperimentPermId> createExperiments(String sessionToken, List<ExperimentCreation> newExperiments);

    public List<EntityTypePermId> createExperimentTypes(String sessionToken, List<ExperimentTypeCreation> newExperimentTypes);

    public List<SamplePermId> createSamples(String sessionToken, List<SampleCreation> newSamples);

    public List<EntityTypePermId> createSampleTypes(String sessionToken, List<SampleTypeCreation> newSampleTypes);

    public List<DataSetPermId> createDataSets(String sessionToken, List<DataSetCreation> newDataSets);

    public List<EntityTypePermId> createDataSetTypes(String sessionToken, List<DataSetTypeCreation> newDataSetTypes);

    public List<MaterialPermId> createMaterials(String sessionToken, List<MaterialCreation> newMaterials);

    public List<EntityTypePermId> createMaterialTypes(String sessionToken, List<MaterialTypeCreation> newMaterialTypes);

    public List<VocabularyTermPermId> createVocabularyTerms(String sessionToken, List<VocabularyTermCreation> newVocabularyTerms);

    public List<TagPermId> createTags(String sessionToken, List<TagCreation> newTags);

    @TechPreview
    public List<ExternalDmsPermId> createExternalDataManagementSystems(String sessionToken,
            List<ExternalDmsCreation> newExternalDataManagementSystems);

    public void updateSpaces(String sessionToken, List<SpaceUpdate> spaceUpdates);

    public void updateProjects(String sessionToken, List<ProjectUpdate> projectUpdates);

    public void updateExperiments(String sessionToken, List<ExperimentUpdate> experimentUpdates);

    public void updateSamples(String sessionToken, List<SampleUpdate> sampleUpdates);

    public void updateDataSets(String sessionToken, List<DataSetUpdate> dataSetUpdates);

    public void updateMaterials(String sessionToken, List<MaterialUpdate> materialUpdates);

    @TechPreview
    public void updateExternalDataManagementSystems(String sessionToken, List<ExternalDmsUpdate> externalDmsUpdates);

    public void updateVocabularyTerms(String sessionToken, List<VocabularyTermUpdate> vocabularyTermUpdates);

    public void updateTags(String sessionToken, List<TagUpdate> tagUpdates);

    public void updateOperationExecutions(String sessionToken, List<OperationExecutionUpdate> executionUpdates);

    public Map<ISpaceId, Space> getSpaces(String sessionToken, List<? extends ISpaceId> spaceIds,
            SpaceFetchOptions fetchOptions);

    public Map<IProjectId, Project> getProjects(String sessionToken, List<? extends IProjectId> projectIds,
            ProjectFetchOptions fetchOptions);

    public Map<IExperimentId, Experiment> getExperiments(String sessionToken, List<? extends IExperimentId> experimentIds,
            ExperimentFetchOptions fetchOptions);

    public Map<ISampleId, Sample> getSamples(String sessionToken, List<? extends ISampleId> sampleIds, SampleFetchOptions fetchOptions);

    public Map<IDataSetId, DataSet> getDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetFetchOptions fetchOptions);

    public Map<IMaterialId, Material> getMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialFetchOptions fetchOptions);

    public Map<IVocabularyTermId, VocabularyTerm> getVocabularyTerms(String sessionToken, List<? extends IVocabularyTermId> vocabularyTermIds,
            VocabularyTermFetchOptions fetchOptions);

    public Map<ITagId, Tag> getTags(String sessionToken, List<? extends ITagId> tagIds, TagFetchOptions fetchOptions);

    @TechPreview
    public Map<IExternalDmsId, ExternalDms> getExternalDataManagementSystems(String sessionToken, List<? extends IExternalDmsId> externalDmsIds,
            ExternalDmsFetchOptions fetchOptions);

    public Map<IOperationExecutionId, OperationExecution> getOperationExecutions(String sessionToken,
            List<? extends IOperationExecutionId> executionIds, OperationExecutionFetchOptions fetchOptions);

    public SearchResult<Space> searchSpaces(String sessionToken, SpaceSearchCriteria searchCriteria, SpaceFetchOptions fetchOptions);

    public SearchResult<Project> searchProjects(String sessionToken, ProjectSearchCriteria searchCriteria, ProjectFetchOptions fetchOptions);

    public SearchResult<Experiment> searchExperiments(String sessionToken, ExperimentSearchCriteria searchCriteria,
            ExperimentFetchOptions fetchOptions);

    public SearchResult<ExperimentType> searchExperimentTypes(String sessionToken, ExperimentTypeSearchCriteria searchCriteria,
            ExperimentTypeFetchOptions fetchOptions);

    public SearchResult<Sample> searchSamples(String sessionToken, SampleSearchCriteria searchCriteria, SampleFetchOptions fetchOptions);

    public SearchResult<SampleType> searchSampleTypes(String sessionToken, SampleTypeSearchCriteria searchCriteria,
            SampleTypeFetchOptions fetchOptions);

    public SearchResult<DataSet> searchDataSets(String sessionToken, DataSetSearchCriteria searchCriteria, DataSetFetchOptions fetchOptions);

    public SearchResult<DataSetType> searchDataSetTypes(String sessionToken, DataSetTypeSearchCriteria searchCriteria,
            DataSetTypeFetchOptions fetchOptions);

    public SearchResult<Material> searchMaterials(String sessionToken, MaterialSearchCriteria searchCriteria, MaterialFetchOptions fetchOptions);

    @TechPreview
    public SearchResult<ExternalDms> searchExternalDataManagementSystems(String sessionToken, ExternalDmsSearchCriteria searchCriteria,
            ExternalDmsFetchOptions fetchOptions);

    public SearchResult<MaterialType> searchMaterialTypes(String sessionToken, MaterialTypeSearchCriteria searchCriteria,
            MaterialTypeFetchOptions fetchOptions);

    public SearchResult<VocabularyTerm> searchVocabularyTerms(String sessionToken, VocabularyTermSearchCriteria searchCriteria,
            VocabularyTermFetchOptions fetchOptions);

    public SearchResult<Tag> searchTags(String sessionToken, TagSearchCriteria searchCriteria, TagFetchOptions fetchOptions);

    public SearchResult<CustomASService> searchCustomASServices(String sessionToken, CustomASServiceSearchCriteria searchCriteria,
            CustomASServiceFetchOptions fetchOptions);

    public SearchResult<ObjectKindModification> searchObjectKindModifications(String sessionToken,
            ObjectKindModificationSearchCriteria searchCriteria, ObjectKindModificationFetchOptions fetchOptions);

    public SearchResult<GlobalSearchObject> searchGlobally(String sessionToken, GlobalSearchCriteria searchCriteria,
            GlobalSearchObjectFetchOptions fetchOptions);

    public SearchResult<OperationExecution> searchOperationExecutions(String sessionToken, OperationExecutionSearchCriteria searchCriteria,
            OperationExecutionFetchOptions fetchOptions);

    public SearchResult<DataStore> searchDataStores(String sessionToken, DataStoreSearchCriteria searchCriteria, DataStoreFetchOptions fetchOptions);

    public void deleteSpaces(String sessionToken, List<? extends ISpaceId> spaceIds, SpaceDeletionOptions deletionOptions);

    public void deleteProjects(String sessionToken, List<? extends IProjectId> projectIds, ProjectDeletionOptions deletionOptions);

    public IDeletionId deleteExperiments(String sessionToken, List<? extends IExperimentId> experimentIds, ExperimentDeletionOptions deletionOptions);

    public IDeletionId deleteSamples(String sessionToken, List<? extends ISampleId> sampleIds, SampleDeletionOptions deletionOptions);

    public IDeletionId deleteDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetDeletionOptions deletionOptions);

    public void deleteMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialDeletionOptions deletionOptions);

    public void deleteVocabularyTerms(String sessionToken, List<? extends IVocabularyTermId> termIds, VocabularyTermDeletionOptions deletionOptions);

    @TechPreview
    public void deleteExternalDataManagementSystems(String sessionToken, List<? extends IExternalDmsId> externalDmsIds,
            ExternalDmsDeletionOptions deletionOptions);

    public void deleteTags(String sessionToken, List<? extends ITagId> tagIds, TagDeletionOptions deletionOptions);

    public void deleteOperationExecutions(String sessionToken, List<? extends IOperationExecutionId> executionIds,
            OperationExecutionDeletionOptions deletionOptions);

    public SearchResult<Deletion> searchDeletions(String sessionToken, DeletionSearchCriteria searchCriteria, DeletionFetchOptions fetchOptions);

    public void revertDeletions(String sessionToken, List<? extends IDeletionId> deletionIds);

    public void confirmDeletions(String sessionToken, List<? extends IDeletionId> deletionIds);

    public Object executeCustomASService(String sessionToken, ICustomASServiceId serviceId, CustomASServiceExecutionOptions options);

    public void archiveDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetArchiveOptions options);

    public void unarchiveDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetUnarchiveOptions options);

    public IOperationExecutionResults executeOperations(String sessionToken, List<? extends IOperation> operations,
            IOperationExecutionOptions options);

    public Map<String, String> getServerInformation(String sessionToken);

    @TechPreview
    public List<String> createPermIdStrings(String sessionToken, int amount);
}
