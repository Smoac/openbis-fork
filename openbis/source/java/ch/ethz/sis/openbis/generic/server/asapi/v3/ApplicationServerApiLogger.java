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

package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.fetchoptions.ExternalDmsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
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
    public List<EntityTypePermId> createMaterialTypes(String sessionToken, List<MaterialTypeCreation> newMaterialTypes)
    {
        logAccess(sessionToken, "create-material-types", "NEW_MATERIAL_TYPES(%s)", abbreviate(newMaterialTypes));
        return null;
    }

    @Override
    public List<ExperimentPermId> createExperiments(String sessionToken, List<ExperimentCreation> newExperiments)
    {
        logAccess(sessionToken, "create-experiments", "NEW_EXPERIMENTS(%s)", abbreviate(newExperiments));
        return null;
    }

    @Override
    public List<EntityTypePermId> createExperimentTypes(String sessionToken, List<ExperimentTypeCreation> newExperimentTypes)
    {
        logAccess(sessionToken, "create-experiment-types", "NEW_EXPERIMENT_TYPES(%s)", abbreviate(newExperimentTypes));
        return null;
    }

    @Override
    public List<SamplePermId> createSamples(String sessionToken, List<SampleCreation> newSamples)
    {
        logAccess(sessionToken, "create-samples", "NEW_SAMPLES(%s)", abbreviate(newSamples));
        return null;
    }

    @Override
    public List<EntityTypePermId> createSampleTypes(String sessionToken, List<SampleTypeCreation> newSampleTypes)
    {
        logAccess(sessionToken, "create-sample-types", "NEW_SAMPLE_TYPES(%s)", abbreviate(newSampleTypes));
        return null;
    }

    @Override
    public List<DataSetPermId> createDataSets(String sessionToken, List<DataSetCreation> newDataSets)
    {
        logAccess(sessionToken, "create-data-sets", "NEW_DATA_SETS(%s)", abbreviate(newDataSets));
        return null;
    }

    @Override
    public List<EntityTypePermId> createDataSetTypes(String sessionToken, List<DataSetTypeCreation> newDataSetTypes)
    {
        logAccess(sessionToken, "create-data-set-types", "NEW_DATA_SET_TYPES(%s)", abbreviate(newDataSetTypes));
        return null;
    }

    @Override
    public List<VocabularyTermPermId> createVocabularyTerms(String sessionToken, List<VocabularyTermCreation> newVocabularyTerms)
    {
        logAccess(sessionToken, "create-vocabulary-terms", "NEW_VOCABULARY_TERMS(%s)", abbreviate(newVocabularyTerms));
        return null;
    }

    @Override
    public List<TagPermId> createTags(String sessionToken, List<TagCreation> newTags)
    {
        logAccess(sessionToken, "create-tags", "NEW_TAGS(%s)", abbreviate(newTags));
        return null;
    }

    @Override
    public List<ExternalDmsPermId> createExternalDataManagementSystems(String sessionToken,
            List<ExternalDmsCreation> newExternalDataManagementSystems)
    {
        logAccess(sessionToken, "create-external-data-management-systems", "NEW_EXTERNAL_DATA_MANAGEMENT_SYSTEMS(%s)",
                abbreviate(newExternalDataManagementSystems));
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
    public void updateVocabularyTerms(String sessionToken, List<VocabularyTermUpdate> vocabularyTermUpdates)
    {
        logAccess(sessionToken, "update-vocabulary-terms", "VOCABULARY_TERM_UPDATES(%s)", abbreviate(vocabularyTermUpdates));
    }

    @Override
    public void updateTags(String sessionToken, List<TagUpdate> tagUpdates)
    {
        logAccess(sessionToken, "update-tags", "TAG_UPDATES(%s)", abbreviate(tagUpdates));
    }

    @Override
    public void updateOperationExecutions(String sessionToken, List<OperationExecutionUpdate> executionUpdates)
    {
        logAccess(sessionToken, "update-operation-executions", "OPERATION_EXECUTION_UPDATES(%s)", abbreviate(executionUpdates));
    }

    @Override
    public Map<ISpaceId, Space> getSpaces(String sessionToken, List<? extends ISpaceId> spaceIds, SpaceFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-spaces", "SPACE_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(spaceIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IProjectId, Project> getProjects(String sessionToken, List<? extends IProjectId> projectIds, ProjectFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-projects", "PROJECT_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(projectIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IExperimentId, Experiment> getExperiments(String sessionToken, List<? extends IExperimentId> experimentIds,
            ExperimentFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-experiments", "EXPERIMENT_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(experimentIds), fetchOptions);
        return null;
    }

    @Override
    public Map<ISampleId, Sample> getSamples(String sessionToken,
            List<? extends ISampleId> sampleIds, SampleFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-samples", "SAMPLE_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(sampleIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IMaterialId, Material> getMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-materials", "MATERIAL_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(materialIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IVocabularyTermId, VocabularyTerm> getVocabularyTerms(String sessionToken, List<? extends IVocabularyTermId> vocabularyTermIds,
            VocabularyTermFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-vocabulary-terms", "VOCABULARY_TERM_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(vocabularyTermIds), fetchOptions);
        return null;
    }

    @Override
    public Map<ITagId, Tag> getTags(String sessionToken, List<? extends ITagId> tagIds, TagFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-tags", "TAG_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(tagIds), fetchOptions);
        return null;
    }

    @Override
    public Map<IExternalDmsId, ExternalDms> getExternalDataManagementSystems(String sessionToken, List<? extends IExternalDmsId> externalDmsIds,
            ExternalDmsFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-external-data-management-systems", "EXTERNAL_DMS_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(externalDmsIds),
                fetchOptions);
        return null;
    }

    @Override
    public Map<IDataSetId, DataSet> getDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-data-sets", "DATA_SET_IDS(%s) FETCH_OPTIONS(%s)", abbreviate(dataSetIds), fetchOptions);
        return null;
    }

    @Override
    public SearchResult<Space> searchSpaces(String sessionToken, SpaceSearchCriteria searchCriteria, SpaceFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-spaces", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<Project> searchProjects(String sessionToken, ProjectSearchCriteria searchCriteria, ProjectFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-projects", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<Experiment> searchExperiments(String sessionToken, ExperimentSearchCriteria searchCriteria,
            ExperimentFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-experiments", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<ExperimentType> searchExperimentTypes(String sessionToken, ExperimentTypeSearchCriteria searchCriteria,
            ExperimentTypeFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-experiment-types", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<Sample> searchSamples(String sessionToken, SampleSearchCriteria searchCriteria, SampleFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-samples", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<SampleType> searchSampleTypes(String sessionToken, SampleTypeSearchCriteria searchCriteria,
            SampleTypeFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-sample-types", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<DataSet> searchDataSets(String sessionToken, DataSetSearchCriteria searchCriteria, DataSetFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-data-sets", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<DataSetType> searchDataSetTypes(String sessionToken, DataSetTypeSearchCriteria searchCriteria,
            DataSetTypeFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-data-set-types", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<Material> searchMaterials(String sessionToken, MaterialSearchCriteria searchCriteria, MaterialFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-materials", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<MaterialType> searchMaterialTypes(String sessionToken, MaterialTypeSearchCriteria searchCriteria,
            MaterialTypeFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-material-types", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<VocabularyTerm> searchVocabularyTerms(String sessionToken, VocabularyTermSearchCriteria searchCriteria,
            VocabularyTermFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-vocabulary-terms", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<Tag> searchTags(String sessionToken, TagSearchCriteria searchCriteria, TagFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-tags", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
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
    public void deleteVocabularyTerms(String sessionToken, List<? extends IVocabularyTermId> termIds, VocabularyTermDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-vocabulary-terms", "VOCABULARY_TERM_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(termIds), deletionOptions);
    }

    @Override
    public void deleteTags(String sessionToken, List<? extends ITagId> tagIds, TagDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-tags", "TAG_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(tagIds), deletionOptions);
    }

    @Override
    public void deleteOperationExecutions(String sessionToken, List<? extends IOperationExecutionId> executionIds,
            OperationExecutionDeletionOptions deletionOptions)
    {
        logAccess(sessionToken, "delete-operation-executions", "EXECUTION_IDS(%s) DELETION_OPTIONS(%s)", abbreviate(executionIds), deletionOptions);
    }

    @Override
    public SearchResult<Deletion> searchDeletions(String sessionToken, DeletionSearchCriteria searchCriteria, DeletionFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-deletions", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
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
    public SearchResult<CustomASService> searchCustomASServices(String sessionToken, CustomASServiceSearchCriteria searchCriteria,
            CustomASServiceFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-custom-as-services", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<ObjectKindModification> searchObjectKindModifications(String sessionToken,
            ObjectKindModificationSearchCriteria searchCriteria, ObjectKindModificationFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-object-kind-modifications", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public Object executeCustomASService(String sessionToken, ICustomASServiceId serviceId, CustomASServiceExecutionOptions options)
    {
        logAccess(sessionToken, "execute-custom-as-service", "SERVICE_ID(%s) EXECUTION_OPTIONS(%s)", serviceId, options);
        return null;
    }

    @Override
    public SearchResult<GlobalSearchObject> searchGlobally(String sessionToken, GlobalSearchCriteria searchCriteria,
            GlobalSearchObjectFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-globally", "SEARCH_CRITERIA:\n%s\nFETCH_OPTIONS:\n%s\n", searchCriteria, fetchOptions);
        return null;
    }

    @Override
    public void archiveDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetArchiveOptions options)
    {
        logAccess(sessionToken, "archive-data-sets", "DATA_SET_IDS(%s) ARCHIVE_OPTIONS(%s)", abbreviate(dataSetIds), options);
    }

    @Override
    public void unarchiveDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetUnarchiveOptions options)
    {
        logAccess(sessionToken, "unarchive-data-sets", "DATA_SET_IDS(%s) UNARCHIVE_OPTIONS(%s)", abbreviate(dataSetIds), options);
    }

    @Override
    public SessionInformation getSessionInformation(String sessionToken)
    {
        logAccess(sessionToken, "session-info");
        return null;
    }

    @Override
    public IOperationExecutionResults executeOperations(String sessionToken, List<? extends IOperation> operations,
            IOperationExecutionOptions options)
    {
        logAccess(sessionToken, "execute-operations", "OPERATIONS(%s) EXECUTION_OPTIONS(%s)", operations, options);
        return null;
    }

    @Override
    public Map<String, String> getServerInformation(String sessionToken)
    {
        logAccess(sessionToken, "server-info");
        return null;
    }

    @Override
    public Map<IOperationExecutionId, OperationExecution> getOperationExecutions(String sessionToken,
            List<? extends IOperationExecutionId> executionIds, OperationExecutionFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "get-operation-executions", "EXECUTION_IDS(%s) FETCH_OPTIONS(%s)", executionIds, fetchOptions);
        return null;
    }

    @Override
    public SearchResult<OperationExecution> searchOperationExecutions(String sessionToken, OperationExecutionSearchCriteria searchCriteria,
            OperationExecutionFetchOptions fetchOptions)
    {
        logAccess(sessionToken, "search-operation-executions", "SEARCH_CRITERIA(%s) FETCH_OPTIONS(%s)", searchCriteria, fetchOptions);
        return null;
    }

}
