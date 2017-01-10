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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.archive.ArchiveDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.archive.DataSetArchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.CreateDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.CreateDataSetsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DeleteDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DeleteDataSetsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.get.GetDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.get.GetDataSetsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.SearchDataSetTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.SearchDataSetTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.SearchDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.SearchDataSetsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unarchive.DataSetUnarchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unarchive.UnarchiveDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.UpdateDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.Deletion;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.confirm.ConfirmDeletionsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.fetchoptions.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.revert.RevertDeletionsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.search.DeletionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.search.SearchDeletionsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.search.SearchDeletionsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.CreateExperimentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.CreateExperimentsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.DeleteExperimentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.DeleteExperimentsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.get.GetExperimentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.get.GetExperimentsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.SearchExperimentTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.SearchExperimentTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.SearchExperimentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.SearchExperimentsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.UpdateExperimentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.SearchGloballyOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.SearchGloballyOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.CreateMaterialsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.CreateMaterialsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.DeleteMaterialsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.MaterialDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.get.GetMaterialsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.get.GetMaterialsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.SearchMaterialTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.SearchMaterialTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.SearchMaterialsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.SearchMaterialsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.MaterialUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.UpdateMaterialsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.ObjectKindModification;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.fetchoptions.ObjectKindModificationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.search.ObjectKindModificationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.search.SearchObjectKindModificationsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.search.SearchObjectKindModificationsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionResults;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionResults;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.delete.DeleteOperationExecutionsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.delete.OperationExecutionDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.get.GetOperationExecutionsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.get.GetOperationExecutionsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.IOperationExecutionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.search.OperationExecutionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.search.SearchOperationExecutionsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.search.SearchOperationExecutionsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.update.OperationExecutionUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.update.UpdateOperationExecutionsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.CreateProjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.CreateProjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.delete.DeleteProjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.delete.ProjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.get.GetProjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.get.GetProjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.SearchProjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.SearchProjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.UpdateProjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.CreateSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.CreateSamplesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.DeleteSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.DeleteSamplesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.get.GetSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.get.GetSamplesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SearchSampleTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SearchSampleTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SearchSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SearchSamplesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.UpdateSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ExecuteCustomASServiceOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ExecuteCustomASServiceOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.CustomASServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.ICustomASServiceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.CustomASServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchCustomASServicesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchCustomASServicesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.get.GetSessionInformationOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.get.GetSessionInformationOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.CreateSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.CreateSpacesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.delete.DeleteSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.delete.SpaceDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.get.GetSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.get.GetSpacesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SearchSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SearchSpacesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.SpaceUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.UpdateSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.CreateTagsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.CreateTagsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.delete.DeleteTagsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.delete.TagDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.get.GetTagsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.get.GetTagsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.SearchTagsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.SearchTagsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.TagSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.update.TagUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.update.UpdateTagsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.CreateVocabularyTermsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.CreateVocabularyTermsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyTermCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete.DeleteVocabularyTermsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete.VocabularyTermDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.get.GetVocabularyTermsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.get.GetVocabularyTermsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.SearchVocabularyTermsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.SearchVocabularyTermsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.UpdateVocabularyTermsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.VocabularyTermUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.OperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.IExecuteOperationExecutor;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * @author pkupczyk
 */
@Component(ApplicationServerApi.INTERNAL_SERVICE_NAME)
public class ApplicationServerApi extends AbstractServer<IApplicationServerApi> implements
        IApplicationServerApi
{
    /**
     * Name of this service for which it is registered as Spring bean
     */
    public static final String INTERNAL_SERVICE_NAME = "application-server_INTERNAL";

    @Autowired
    private IExecuteOperationExecutor executeOperationsExecutor;

    // Default constructor needed by Spring
    public ApplicationServerApi()
    {
    }

    ApplicationServerApi(IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IOpenBisSessionManager sessionManager, IDAOFactory daoFactory,
            IPropertiesBatchManager propertiesBatchManager,
            ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin)
    {
        super(sessionManager, daoFactory, propertiesBatchManager, sampleTypeSlaveServerPlugin,
                dataSetTypeSlaveServerPlugin);
    }

    @Override
    @Transactional
    public String login(String userId, String password)
    {
        SessionContextDTO session = tryAuthenticate(userId, password);
        return session == null ? null : session.getSessionToken();
    }

    @Override
    @Transactional
    public String loginAsAnonymousUser()
    {
        SessionContextDTO session = tryAuthenticateAnonymously();
        return session == null ? null : session.getSessionToken();
    }

    @Override
    @Transactional
    public String loginAs(String userId, String password, String asUserId)
    {
        SessionContextDTO session = tryAuthenticateAs(userId, password, asUserId);
        return session == null ? null : session.getSessionToken();
    }

    @Override
    @Transactional
    public List<SpacePermId> createSpaces(String sessionToken, List<SpaceCreation> creations)
    {
        CreateSpacesOperationResult result = executeOperation(sessionToken, new CreateSpacesOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<ProjectPermId> createProjects(String sessionToken, List<ProjectCreation> creations)
    {
        CreateProjectsOperationResult result = executeOperation(sessionToken, new CreateProjectsOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<ExperimentPermId> createExperiments(String sessionToken,
            List<ExperimentCreation> creations)
    {
        CreateExperimentsOperationResult result = executeOperation(sessionToken, new CreateExperimentsOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<SamplePermId> createSamples(String sessionToken,
            List<SampleCreation> creations)
    {
        CreateSamplesOperationResult result = executeOperation(sessionToken, new CreateSamplesOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<DataSetPermId> createDataSets(String sessionToken, List<DataSetCreation> creations)
    {
        CreateDataSetsOperationResult result = executeOperation(sessionToken, new CreateDataSetsOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<MaterialPermId> createMaterials(String sessionToken, List<MaterialCreation> creations)
    {
        CreateMaterialsOperationResult result = executeOperation(sessionToken, new CreateMaterialsOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<VocabularyTermPermId> createVocabularyTerms(String sessionToken, List<VocabularyTermCreation> creations)
    {
        CreateVocabularyTermsOperationResult result = executeOperation(sessionToken, new CreateVocabularyTermsOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public List<TagPermId> createTags(String sessionToken, List<TagCreation> creations)
    {
        CreateTagsOperationResult result = executeOperation(sessionToken, new CreateTagsOperation(creations));
        return result.getObjectIds();
    }

    @Override
    @Transactional
    public void updateSpaces(String sessionToken, List<SpaceUpdate> updates)
    {
        executeOperation(sessionToken, new UpdateSpacesOperation(updates));
    }

    @Override
    @Transactional
    public void updateProjects(String sessionToken, List<ProjectUpdate> updates)
    {
        executeOperation(sessionToken, new UpdateProjectsOperation(updates));
    }

    @Override
    @Transactional
    public void updateExperiments(String sessionToken, List<ExperimentUpdate> updates)
    {
        executeOperation(sessionToken, new UpdateExperimentsOperation(updates));
    }

    @Override
    @Transactional
    public void updateSamples(String sessionToken, List<SampleUpdate> updates)
    {
        executeOperation(sessionToken, new UpdateSamplesOperation(updates));
    }

    @Override
    @Transactional
    public void updateMaterials(String sessionToken, List<MaterialUpdate> updates)
    {
        executeOperation(sessionToken, new UpdateMaterialsOperation(updates));
    }

    @Override
    @Transactional
    public void updateDataSets(String sessionToken, List<DataSetUpdate> updates)
    {
        executeOperation(sessionToken, new UpdateDataSetsOperation(updates));
    }

    @Override
    @Transactional
    public void updateVocabularyTerms(String sessionToken, List<VocabularyTermUpdate> updates)
    {
        executeOperation(sessionToken, new UpdateVocabularyTermsOperation(updates));
    }

    @Override
    @Transactional
    public void updateTags(String sessionToken, List<TagUpdate> updates)
    {
        executeOperation(sessionToken, new UpdateTagsOperation(updates));
    }

    @Override
    @Transactional
    public void updateOperationExecutions(String sessionToken, List<OperationExecutionUpdate> updates)
    {
        executeOperation(sessionToken, new UpdateOperationExecutionsOperation(updates));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<ISpaceId, Space> getSpaces(String sessionToken, List<? extends ISpaceId> spaceIds, SpaceFetchOptions fetchOptions)
    {
        GetSpacesOperationResult result = executeOperation(sessionToken, new GetSpacesOperation(spaceIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IProjectId, Project> getProjects(String sessionToken, List<? extends IProjectId> projectIds, ProjectFetchOptions fetchOptions)
    {
        GetProjectsOperationResult result = executeOperation(sessionToken, new GetProjectsOperation(projectIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IExperimentId, Experiment> getExperiments(String sessionToken,
            List<? extends IExperimentId> experimentIds, ExperimentFetchOptions fetchOptions)
    {
        GetExperimentsOperationResult result = executeOperation(sessionToken, new GetExperimentsOperation(experimentIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<ISampleId, Sample> getSamples(String sessionToken, List<? extends ISampleId> sampleIds,
            SampleFetchOptions fetchOptions)
    {
        GetSamplesOperationResult result = executeOperation(sessionToken, new GetSamplesOperation(sampleIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IDataSetId, DataSet> getDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetFetchOptions fetchOptions)
    {
        GetDataSetsOperationResult result = executeOperation(sessionToken, new GetDataSetsOperation(dataSetIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IMaterialId, Material> getMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialFetchOptions fetchOptions)
    {
        GetMaterialsOperationResult result = executeOperation(sessionToken, new GetMaterialsOperation(materialIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<IVocabularyTermId, VocabularyTerm> getVocabularyTerms(String sessionToken, List<? extends IVocabularyTermId> vocabularyTermIds,
            VocabularyTermFetchOptions fetchOptions)
    {
        GetVocabularyTermsOperationResult result = executeOperation(sessionToken, new GetVocabularyTermsOperation(vocabularyTermIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<ITagId, Tag> getTags(String sessionToken, List<? extends ITagId> tagIds, TagFetchOptions fetchOptions)
    {
        GetTagsOperationResult result = executeOperation(sessionToken, new GetTagsOperation(tagIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<Space> searchSpaces(String sessionToken, SpaceSearchCriteria searchCriteria, SpaceFetchOptions fetchOptions)
    {
        SearchSpacesOperationResult result = executeOperation(sessionToken, new SearchSpacesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<Project> searchProjects(String sessionToken, ProjectSearchCriteria searchCriteria, ProjectFetchOptions fetchOptions)
    {
        SearchProjectsOperationResult result = executeOperation(sessionToken, new SearchProjectsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<Experiment> searchExperiments(String sessionToken, ExperimentSearchCriteria searchCriteria,
            ExperimentFetchOptions fetchOptions)
    {
        SearchExperimentsOperationResult result = executeOperation(sessionToken, new SearchExperimentsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<ExperimentType> searchExperimentTypes(String sessionToken, ExperimentTypeSearchCriteria searchCriteria,
            ExperimentTypeFetchOptions fetchOptions)
    {
        SearchExperimentTypesOperationResult result =
                executeOperation(sessionToken, new SearchExperimentTypesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<Sample> searchSamples(String sessionToken, SampleSearchCriteria searchCriteria, SampleFetchOptions fetchOptions)
    {
        SearchSamplesOperationResult result = executeOperation(sessionToken, new SearchSamplesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<SampleType> searchSampleTypes(String sessionToken, SampleTypeSearchCriteria searchCriteria,
            SampleTypeFetchOptions fetchOptions)
    {
        SearchSampleTypesOperationResult result = executeOperation(sessionToken, new SearchSampleTypesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<DataSet> searchDataSets(String sessionToken, DataSetSearchCriteria searchCriteria, DataSetFetchOptions fetchOptions)
    {
        SearchDataSetsOperationResult result = executeOperation(sessionToken, new SearchDataSetsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<DataSetType> searchDataSetTypes(String sessionToken, DataSetTypeSearchCriteria searchCriteria,
            DataSetTypeFetchOptions fetchOptions)
    {
        SearchDataSetTypesOperationResult result = executeOperation(sessionToken, new SearchDataSetTypesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<Material> searchMaterials(String sessionToken, MaterialSearchCriteria searchCriteria, MaterialFetchOptions fetchOptions)
    {
        SearchMaterialsOperationResult result = executeOperation(sessionToken, new SearchMaterialsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<MaterialType> searchMaterialTypes(String sessionToken, MaterialTypeSearchCriteria searchCriteria,
            MaterialTypeFetchOptions fetchOptions)
    {
        SearchMaterialTypesOperationResult result = executeOperation(sessionToken, new SearchMaterialTypesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<VocabularyTerm> searchVocabularyTerms(String sessionToken, VocabularyTermSearchCriteria searchCriteria,
            VocabularyTermFetchOptions fetchOptions)
    {
        SearchVocabularyTermsOperationResult result =
                executeOperation(sessionToken, new SearchVocabularyTermsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<Tag> searchTags(String sessionToken, TagSearchCriteria searchCriteria, TagFetchOptions fetchOptions)
    {
        SearchTagsOperationResult result = executeOperation(sessionToken, new SearchTagsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional
    public void deleteSpaces(String sessionToken, List<? extends ISpaceId> spaceIds, SpaceDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteSpacesOperation(spaceIds, deletionOptions));
    }

    @Override
    @Transactional
    public void deleteProjects(String sessionToken, List<? extends IProjectId> projectIds, ProjectDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteProjectsOperation(projectIds, deletionOptions));
    }

    @Override
    @Transactional
    public IDeletionId deleteExperiments(String sessionToken, List<? extends IExperimentId> experimentIds, ExperimentDeletionOptions deletionOptions)
    {
        DeleteExperimentsOperationResult result = executeOperation(sessionToken, new DeleteExperimentsOperation(experimentIds, deletionOptions));
        return result.getDeletionId();
    }

    @Override
    @Transactional
    public IDeletionId deleteSamples(String sessionToken, List<? extends ISampleId> sampleIds, SampleDeletionOptions deletionOptions)
    {
        DeleteSamplesOperationResult result = executeOperation(sessionToken, new DeleteSamplesOperation(sampleIds, deletionOptions));
        return result.getDeletionId();
    }

    @Override
    @Transactional
    public IDeletionId deleteDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetDeletionOptions deletionOptions)
    {
        DeleteDataSetsOperationResult result = executeOperation(sessionToken, new DeleteDataSetsOperation(dataSetIds, deletionOptions));
        return result.getDeletionId();
    }

    @Override
    @Transactional
    public void deleteMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteMaterialsOperation(materialIds, deletionOptions));
    }

    @Override
    @Transactional
    public void deleteVocabularyTerms(String sessionToken, List<? extends IVocabularyTermId> termIds, VocabularyTermDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteVocabularyTermsOperation(termIds, deletionOptions));
    }

    @Override
    @Transactional
    public void deleteTags(String sessionToken, List<? extends ITagId> tagIds, TagDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteTagsOperation(tagIds, deletionOptions));
    }

    @Override
    @Transactional
    public void deleteOperationExecutions(String sessionToken, List<? extends IOperationExecutionId> executionIds,
            OperationExecutionDeletionOptions deletionOptions)
    {
        executeOperation(sessionToken, new DeleteOperationExecutionsOperation(executionIds, deletionOptions));
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResult<Deletion> searchDeletions(String sessionToken, DeletionSearchCriteria searchCriteria, DeletionFetchOptions fetchOptions)
    {
        SearchDeletionsOperationResult result = executeOperation(sessionToken, new SearchDeletionsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional
    public void revertDeletions(String sessionToken, List<? extends IDeletionId> deletionIds)
    {
        executeOperation(sessionToken, new RevertDeletionsOperation(deletionIds));
    }

    @Override
    @Transactional
    public void confirmDeletions(String sessionToken, List<? extends IDeletionId> deletionIds)
    {
        executeOperation(sessionToken, new ConfirmDeletionsOperation(deletionIds));
    }

    @Override
    @Transactional
    public SearchResult<CustomASService> searchCustomASServices(String sessionToken, CustomASServiceSearchCriteria searchCriteria,
            CustomASServiceFetchOptions fetchOptions)
    {
        SearchCustomASServicesOperationResult result =
                executeOperation(sessionToken, new SearchCustomASServicesOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional
    public SearchResult<ObjectKindModification> searchObjectKindModifications(String sessionToken,
            ObjectKindModificationSearchCriteria searchCriteria, ObjectKindModificationFetchOptions fetchOptions)
    {
        SearchObjectKindModificationsOperationResult result =
                executeOperation(sessionToken, new SearchObjectKindModificationsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional
    public Object executeCustomASService(String sessionToken, ICustomASServiceId serviceId, CustomASServiceExecutionOptions options)
    {
        ExecuteCustomASServiceOperationResult result = executeOperation(sessionToken, new ExecuteCustomASServiceOperation(serviceId, options));
        return result.getResult();
    }

    @Override
    @Transactional
    public SearchResult<GlobalSearchObject> searchGlobally(String sessionToken, GlobalSearchCriteria searchCriteria,
            GlobalSearchObjectFetchOptions fetchOptions)
    {
        SearchGloballyOperationResult result = executeOperation(sessionToken, new SearchGloballyOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @Override
    @Transactional
    public void archiveDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetArchiveOptions options)
    {
        executeOperation(sessionToken, new ArchiveDataSetsOperation(dataSetIds, options));
    }

    @Override
    @Transactional
    public void unarchiveDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetUnarchiveOptions options)
    {
        executeOperation(sessionToken, new UnarchiveDataSetsOperation(dataSetIds, options));
    }

    @Override
    @Transactional(readOnly = true)
    public SessionInformation getSessionInformation(String sessionToken)
    {
        GetSessionInformationOperationResult result = executeOperation(sessionToken, new GetSessionInformationOperation());
        return result.getSessionInformation();
    }

    @Override
    @Transactional
    public IOperationExecutionResults executeOperations(String sessionToken, List<? extends IOperation> operations,
            IOperationExecutionOptions options)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);
        return executeOperationsExecutor.execute(context, operations, options);
    }

    @Override
    @Transactional
    public Map<IOperationExecutionId, OperationExecution> getOperationExecutions(String sessionToken,
            List<? extends IOperationExecutionId> executionIds,
            OperationExecutionFetchOptions fetchOptions)
    {
        GetOperationExecutionsOperationResult result =
                executeOperation(sessionToken, new GetOperationExecutionsOperation(executionIds, fetchOptions));
        return result.getObjectMap();
    }

    @Override
    @Transactional
    public SearchResult<OperationExecution> searchOperationExecutions(String sessionToken, OperationExecutionSearchCriteria searchCriteria,
            OperationExecutionFetchOptions fetchOptions)
    {
        SearchOperationExecutionsOperationResult result =
                executeOperation(sessionToken, new SearchOperationExecutionsOperation(searchCriteria, fetchOptions));
        return result.getSearchResult();
    }

    @SuppressWarnings("unchecked")
    private <T extends IOperationResult> T executeOperation(String sessionToken, IOperation operation)
    {
        SynchronousOperationExecutionResults results =
                (SynchronousOperationExecutionResults) executeOperations(sessionToken, Arrays.asList(operation),
                        new SynchronousOperationExecutionOptions());
        return (T) results.getResults().get(0);
    }

    @Override
    public Map<String, String> getServerInformation(String sessionToken)
    {
        return new HashMap<String, String>();
    }

    @Override
    public IApplicationServerApi createLogger(IInvocationLoggerContext context)
    {
        return new ApplicationServerApiLogger(sessionManager, context);
    }

    @Override
    public int getMajorVersion()
    {
        return 3;
    }

    @Override
    public int getMinorVersion()
    {
        return 1;
    }
}
