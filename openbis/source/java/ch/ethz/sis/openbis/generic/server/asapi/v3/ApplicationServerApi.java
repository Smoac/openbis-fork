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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.archive.DataSetArchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialCreation;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.deletion.TagDeletionOptions;
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
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IArchiveDataSetMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IConfirmDeletionMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ICreateDataSetMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ICreateExperimentMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ICreateMaterialMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ICreateProjectMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ICreateSampleMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ICreateSpaceMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ICreateTagMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ICreateVocabularyTermMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IDeleteDataSetMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IDeleteExperimentMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IDeleteMaterialMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IDeleteProjectMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IDeleteSampleMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IDeleteSpaceMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IDeleteTagMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IDeleteVocabularyTermMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IExecuteCustomASServiceMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IGetDataSetMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IGetExperimentMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IGetMaterialMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IGetProjectMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IGetSampleMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IGetSessionInformationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IGetSpaceMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IGetTagMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IGetVocabularyTermMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IGlobalSearchMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IRevertDeletionMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ISearchCustomASServiceMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ISearchDataSetMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ISearchDataSetTypeMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ISearchDeletionMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ISearchExperimentMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ISearchExperimentTypeMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ISearchMaterialMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ISearchMaterialTypeMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ISearchObjectKindModificationMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ISearchProjectMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ISearchSampleMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ISearchSampleTypeMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ISearchSpaceMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ISearchTagMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.ISearchVocabularyTermMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IUnarchiveDataSetMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IUpdateDataSetMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IUpdateExperimentMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IUpdateMaterialMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IUpdateProjectMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IUpdateSampleMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IUpdateSpaceMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IUpdateTagMethodExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method.IUpdateVocabularyTermMethodExecutor;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseUpdateModification;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.V3SampleCreationPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.V3SampleUpdatePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.V3SampleDeletePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.V3SpaceUpdatePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.V3SpaceDeletePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.V3ExperimentCreationPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.V3ExperimentUpdatePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.V3ExperimentDeletePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.V3ProjectCreationPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.V3ProjectUpdatePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.V3ProjectDeletePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.V3DataSetUpdatePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.V3DataSetIdPredicate;

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
    private ICreateSpaceMethodExecutor createSpaceExecutor;

    @Autowired
    private ICreateProjectMethodExecutor createProjectExecutor;

    @Autowired
    private ICreateExperimentMethodExecutor createExperimentExecutor;

    @Autowired
    private ICreateSampleMethodExecutor createSampleExecutor;

    @Autowired
    private ICreateDataSetMethodExecutor createDataSetExecutor;

    @Autowired
    private ICreateMaterialMethodExecutor createMaterialExecutor;

    @Autowired
    private ICreateVocabularyTermMethodExecutor createVocabularyTermExecutor;

    @Autowired
    private ICreateTagMethodExecutor createTagExecutor;

    @Autowired
    private IUpdateSpaceMethodExecutor updateSpaceExecutor;

    @Autowired
    private IUpdateProjectMethodExecutor updateProjectExecutor;

    @Autowired
    private IUpdateExperimentMethodExecutor updateExperimentExecutor;

    @Autowired
    private IUpdateSampleMethodExecutor updateSampleExecutor;

    @Autowired
    private IUpdateDataSetMethodExecutor updateDataSetExecutor;

    @Autowired
    private IUpdateMaterialMethodExecutor updateMaterialExecutor;

    @Autowired
    private IUpdateVocabularyTermMethodExecutor updateVocabularyTermExecutor;

    @Autowired
    private IUpdateTagMethodExecutor updateTagExecutor;

    @Autowired
    private IGetSpaceMethodExecutor getSpaceExecutor;

    @Autowired
    private IGetProjectMethodExecutor getProjectExecutor;

    @Autowired
    private IGetExperimentMethodExecutor getExperimentExecutor;

    @Autowired
    private IGetSampleMethodExecutor getSampleExecutor;

    @Autowired
    private IGetDataSetMethodExecutor getDataSetExecutor;

    @Autowired
    private IGetMaterialMethodExecutor getMaterialExecutor;

    @Autowired
    private IGetVocabularyTermMethodExecutor getVocabularyTermExecutor;

    @Autowired
    private IGetTagMethodExecutor getTagExecutor;

    @Autowired
    private ISearchSpaceMethodExecutor searchSpaceExecutor;

    @Autowired
    private ISearchProjectMethodExecutor searchProjectExecutor;

    @Autowired
    private ISearchExperimentMethodExecutor searchExperimentExecutor;

    @Autowired
    private ISearchExperimentTypeMethodExecutor searchExperimentTypeExecutor;

    @Autowired
    private ISearchSampleMethodExecutor searchSampleExecutor;

    @Autowired
    private ISearchSampleTypeMethodExecutor searchSampleTypeExecutor;

    @Autowired
    private ISearchDataSetMethodExecutor searchDataSetExecutor;

    @Autowired
    private ISearchDataSetTypeMethodExecutor searchDataSetTypeExecutor;

    @Autowired
    private ISearchMaterialMethodExecutor searchMaterialExecutor;

    @Autowired
    private ISearchMaterialTypeMethodExecutor searchMaterialTypeExecutor;

    @Autowired
    private ISearchVocabularyTermMethodExecutor searchVocabularyTermExecutor;

    @Autowired
    private ISearchTagMethodExecutor searchTagExecutor;

    @Autowired
    private ISearchCustomASServiceMethodExecutor searchCustomASServiceExecutor;

    @Autowired
    private ISearchObjectKindModificationMethodExecutor searchObjectKindModificationExecutor;

    @Autowired
    private IDeleteSpaceMethodExecutor deleteSpaceExecutor;

    @Autowired
    private IDeleteProjectMethodExecutor deleteProjectExecutor;

    @Autowired
    private IDeleteExperimentMethodExecutor deleteExperimentExecutor;

    @Autowired
    private IDeleteSampleMethodExecutor deleteSampleExecutor;

    @Autowired
    private IDeleteDataSetMethodExecutor deleteDataSetExecutor;

    @Autowired
    private IDeleteMaterialMethodExecutor deleteMaterialExecutor;

    @Autowired
    private IDeleteVocabularyTermMethodExecutor deleteVocabularyTermExecutor;

    @Autowired
    private IDeleteTagMethodExecutor deleteTagExecutor;

    @Autowired
    private ISearchDeletionMethodExecutor searchDeletionExecutor;

    @Autowired
    private IRevertDeletionMethodExecutor revertDeletionExecutor;

    @Autowired
    private IConfirmDeletionMethodExecutor confirmDeletionExecutor;

    @Autowired
    private IExecuteCustomASServiceMethodExecutor executeCustomASServiceExecutor;

    @Autowired
    private IGlobalSearchMethodExecutor globalSearchExecutor;

    @Autowired
    private IArchiveDataSetMethodExecutor archiveDataSetExecutor;

    @Autowired
    private IUnarchiveDataSetMethodExecutor unarchiveDataSetExecutor;

    @Autowired
    private IGetSessionInformationExecutor getSessionInformationExecutor;

    // Default constructor needed by Spring
    public ApplicationServerApi()
    {
    }

    public ApplicationServerApi(IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
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
    @RolesAllowed({ RoleWithHierarchy.SPACE_ADMIN, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_SPACE")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SPACE)
    public List<SpacePermId> createSpaces(String sessionToken, List<SpaceCreation> creations)
    {
        return createSpaceExecutor.create(sessionToken, creations);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_PROJECT")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.PROJECT)
    public List<ProjectPermId> createProjects(String sessionToken, @AuthorizationGuard(guardClass = V3ProjectCreationPredicate.class) List<ProjectCreation> creations)
    {
        return createProjectExecutor.create(sessionToken, creations);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_EXPERIMENT")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.EXPERIMENT)
    public List<ExperimentPermId> createExperiments(String sessionToken, @AuthorizationGuard(guardClass = V3ExperimentCreationPredicate.class) List<ExperimentCreation> creations)
    {
        return createExperimentExecutor.create(sessionToken, creations);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_SAMPLE")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SAMPLE)
    public List<SamplePermId> createSamples(String sessionToken, @AuthorizationGuard(guardClass = V3SampleCreationPredicate.class) List<SampleCreation> creations)
    {
        return createSampleExecutor.create(sessionToken, creations);
    }

    @Override
    @Transactional
    // @RolesAllowed intentionally omitted. Authorization is done in CreateDataSetMethodExecutor.
    @Capability("CREATE_DATASET") 
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DATA_SET)
    public List<DataSetPermId> createDataSets(String sessionToken, List<DataSetCreation> creations)
    {
        return createDataSetExecutor.create(sessionToken, creations);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("CREATE_MATERIAL")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.MATERIAL)
    public List<MaterialPermId> createMaterials(String sessionToken, List<MaterialCreation> creations)
    {
        return createMaterialExecutor.create(sessionToken, creations);
    }

    @Override
    @Transactional
    // @RolesAllowed and @Capability are checked later depending whether an official or unofficial term is created
    @DatabaseCreateOrDeleteModification(value = ObjectKind.VOCABULARY_TERM)
    public List<VocabularyTermPermId> createVocabularyTerms(String sessionToken, List<VocabularyTermCreation> creations)
    {
        return createVocabularyTermExecutor.create(sessionToken, creations);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_TAG")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.METAPROJECT)
    public List<TagPermId> createTags(String sessionToken, List<TagCreation> creations)
    {
        return createTagExecutor.create(sessionToken, creations);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_ADMIN, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_SPACE")
    @DatabaseUpdateModification(value = ObjectKind.SPACE)
    public void updateSpaces(String sessionToken, @AuthorizationGuard(guardClass = V3SpaceUpdatePredicate.class) List<SpaceUpdate> updates)
    {
        updateSpaceExecutor.update(sessionToken, updates);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_PROJECT")
    @DatabaseUpdateModification(value = ObjectKind.PROJECT)
    public void updateProjects(String sessionToken, @AuthorizationGuard(guardClass = V3ProjectUpdatePredicate.class) List<ProjectUpdate> updates)
    {
        updateProjectExecutor.update(sessionToken, updates);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_EXPERIMENT")
    @DatabaseUpdateModification(value = ObjectKind.EXPERIMENT)
    public void updateExperiments(String sessionToken, @AuthorizationGuard(guardClass = V3ExperimentUpdatePredicate.class) List<ExperimentUpdate> updates)
    {
        updateExperimentExecutor.update(sessionToken, updates);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_SAMPLE")
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public void updateSamples(String sessionToken, @AuthorizationGuard(guardClass = V3SampleUpdatePredicate.class) List<SampleUpdate> updates)
    {
        updateSampleExecutor.update(sessionToken, updates);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("UPDATE_MATERIAL")
    @DatabaseUpdateModification(value = ObjectKind.MATERIAL)
    public void updateMaterials(String sessionToken, List<MaterialUpdate> updates)
    {
        updateMaterialExecutor.update(sessionToken, updates);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_DATASET")
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public void updateDataSets(String sessionToken, @AuthorizationGuard(guardClass = V3DataSetUpdatePredicate.class) List<DataSetUpdate> updates)
    {
        updateDataSetExecutor.update(sessionToken, updates);
    }

    @Override
    @Transactional
    // @RolesAllowed and @Capability are checked later depending whether an official or unofficial term is updated
    @DatabaseUpdateModification(value = ObjectKind.VOCABULARY_TERM)
    public void updateVocabularyTerms(String sessionToken, List<VocabularyTermUpdate> vocabularyTermUpdates)
    {
        updateVocabularyTermExecutor.update(sessionToken, vocabularyTermUpdates);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_TAG")
    @DatabaseUpdateModification(value = ObjectKind.METAPROJECT)
    public void updateTags(String sessionToken, List<TagUpdate> tagUpdates)
    {
        updateTagExecutor.update(sessionToken, tagUpdates);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Map<ISpaceId, Space> getSpaces(String sessionToken, List<? extends ISpaceId> spaceIds, SpaceFetchOptions fetchOptions)
    {
        return getSpaceExecutor.get(sessionToken, spaceIds, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Map<IProjectId, Project> getProjects(String sessionToken, List<? extends IProjectId> projectIds, ProjectFetchOptions fetchOptions)
    {
        return getProjectExecutor.get(sessionToken, projectIds, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Map<IExperimentId, Experiment> getExperiments(String sessionToken,
            List<? extends IExperimentId> experimentIds, ExperimentFetchOptions fetchOptions)
    {
        return getExperimentExecutor.get(sessionToken, experimentIds, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Map<ISampleId, Sample> getSamples(String sessionToken, List<? extends ISampleId> sampleIds,
            SampleFetchOptions fetchOptions)
    {
        return getSampleExecutor.get(sessionToken, sampleIds, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Map<IDataSetId, DataSet> getDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetFetchOptions fetchOptions)
    {
        return getDataSetExecutor.get(sessionToken, dataSetIds, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Map<IMaterialId, Material> getMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialFetchOptions fetchOptions)
    {
        return getMaterialExecutor.get(sessionToken, materialIds, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Map<IVocabularyTermId, VocabularyTerm> getVocabularyTerms(String sessionToken, List<? extends IVocabularyTermId> vocabularyTermIds,
            VocabularyTermFetchOptions fetchOptions)
    {
        return getVocabularyTermExecutor.get(sessionToken, vocabularyTermIds, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Map<ITagId, Tag> getTags(String sessionToken, List<? extends ITagId> tagIds, TagFetchOptions fetchOptions)
    {
        return getTagExecutor.get(sessionToken, tagIds, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public SearchResult<Space> searchSpaces(String sessionToken, SpaceSearchCriteria searchCriteria, SpaceFetchOptions fetchOptions)
    {
        return searchSpaceExecutor.search(sessionToken, searchCriteria, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public SearchResult<Project> searchProjects(String sessionToken, ProjectSearchCriteria searchCriteria, ProjectFetchOptions fetchOptions)
    {
        return searchProjectExecutor.search(sessionToken, searchCriteria, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public SearchResult<Experiment> searchExperiments(String sessionToken, ExperimentSearchCriteria searchCriteria,
            ExperimentFetchOptions fetchOptions)
    {
        return searchExperimentExecutor.search(sessionToken, searchCriteria, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public SearchResult<ExperimentType> searchExperimentTypes(String sessionToken, ExperimentTypeSearchCriteria searchCriteria,
            ExperimentTypeFetchOptions fetchOptions)
    {
        return searchExperimentTypeExecutor.search(sessionToken, searchCriteria, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public SearchResult<Sample> searchSamples(String sessionToken, SampleSearchCriteria searchCriteria, SampleFetchOptions fetchOptions)
    {
        return searchSampleExecutor.search(sessionToken, searchCriteria, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public SearchResult<SampleType> searchSampleTypes(String sessionToken, SampleTypeSearchCriteria searchCriteria,
            SampleTypeFetchOptions fetchOptions)
    {
        return searchSampleTypeExecutor.search(sessionToken, searchCriteria, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public SearchResult<DataSet> searchDataSets(String sessionToken, DataSetSearchCriteria searchCriteria, DataSetFetchOptions fetchOptions)
    {
        return searchDataSetExecutor.search(sessionToken, searchCriteria, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public SearchResult<DataSetType> searchDataSetTypes(String sessionToken, DataSetTypeSearchCriteria searchCriteria,
            DataSetTypeFetchOptions fetchOptions)
    {
        return searchDataSetTypeExecutor.search(sessionToken, searchCriteria, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public SearchResult<Material> searchMaterials(String sessionToken, MaterialSearchCriteria searchCriteria, MaterialFetchOptions fetchOptions)
    {
        return searchMaterialExecutor.search(sessionToken, searchCriteria, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public SearchResult<MaterialType> searchMaterialTypes(String sessionToken, MaterialTypeSearchCriteria searchCriteria,
            MaterialTypeFetchOptions fetchOptions)
    {
        return searchMaterialTypeExecutor.search(sessionToken, searchCriteria, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public SearchResult<VocabularyTerm> searchVocabularyTerms(String sessionToken, VocabularyTermSearchCriteria searchCriteria,
            VocabularyTermFetchOptions fetchOptions)
    {
        return searchVocabularyTermExecutor.search(sessionToken, searchCriteria, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public SearchResult<Tag> searchTags(String sessionToken, TagSearchCriteria searchCriteria, TagFetchOptions fetchOptions)
    {
        return searchTagExecutor.search(sessionToken, searchCriteria, fetchOptions);
    }

    @Override
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.SPACE, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.SPACE_ADMIN, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_SPACE")
    public void deleteSpaces(String sessionToken, @AuthorizationGuard(guardClass = V3SpaceDeletePredicate.class) List<? extends ISpaceId> spaceIds, SpaceDeletionOptions deletionOptions)
    {
        deleteSpaceExecutor.delete(sessionToken, spaceIds, deletionOptions);
    }

    @Override
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.PROJECT, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_PROJECT")
    public void deleteProjects(String sessionToken, @AuthorizationGuard(guardClass = V3ProjectDeletePredicate.class) List<? extends IProjectId> projectIds, ProjectDeletionOptions deletionOptions)
    {
        deleteProjectExecutor.delete(sessionToken, projectIds, deletionOptions);
    }

    @Override
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.EXPERIMENT, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_EXPERIMENT")
    public IDeletionId deleteExperiments(String sessionToken, @AuthorizationGuard(guardClass = V3ExperimentDeletePredicate.class) List<? extends IExperimentId> experimentIds, ExperimentDeletionOptions deletionOptions)
    {
        return deleteExperimentExecutor.delete(sessionToken, experimentIds, deletionOptions);
    }

    @Override
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.SAMPLE, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_SAMPLE")
    public IDeletionId deleteSamples(String sessionToken, @AuthorizationGuard(guardClass = V3SampleDeletePredicate.class) List<? extends ISampleId> sampleIds, SampleDeletionOptions deletionOptions)
    {
        return deleteSampleExecutor.delete(sessionToken, sampleIds, deletionOptions);
    }

    @Override
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.DATA_SET, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_DATASET")
    public IDeletionId deleteDataSets(String sessionToken, @AuthorizationGuard(guardClass = V3DataSetIdPredicate.class) List<? extends IDataSetId> dataSetIds, DataSetDeletionOptions deletionOptions)
    {
        return deleteDataSetExecutor.delete(sessionToken, dataSetIds, deletionOptions);
    }

    @Override
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.MATERIAL, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("DELETE_MATERIAL")
    public void deleteMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialDeletionOptions deletionOptions)
    {
        deleteMaterialExecutor.delete(sessionToken, materialIds, deletionOptions);
    }

    @Override
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.VOCABULARY_TERM, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_VOCABULARY_TERM")
    public void deleteVocabularyTerms(String sessionToken, List<? extends IVocabularyTermId> termIds, VocabularyTermDeletionOptions deletionOptions)
    {
        deleteVocabularyTermExecutor.delete(sessionToken, termIds, deletionOptions);
    }

    @Override
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.METAPROJECT, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_TAG")
    public void deleteTags(String sessionToken, List<? extends ITagId> tagIds, TagDeletionOptions deletionOptions)
    {
        deleteTagExecutor.delete(sessionToken, tagIds, deletionOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public SearchResult<Deletion> searchDeletions(String sessionToken, DeletionSearchCriteria searchCriteria, DeletionFetchOptions fetchOptions)
    {
        return searchDeletionExecutor.searchDeletions(sessionToken, searchCriteria, fetchOptions);
    }

    @Override
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DELETION)
    @DatabaseUpdateModification(value = { ObjectKind.EXPERIMENT, ObjectKind.SAMPLE, ObjectKind.DATA_SET })
    @RolesAllowed({ RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("REVERT_DELETION")
    public void revertDeletions(String sessionToken, List<? extends IDeletionId> deletionIds)
    {
        revertDeletionExecutor.revert(sessionToken, deletionIds);
    }

    @Override
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.DELETION, ObjectKind.EXPERIMENT, ObjectKind.SAMPLE, ObjectKind.DATA_SET })
    @RolesAllowed({ RoleWithHierarchy.SPACE_ADMIN, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CONFIRM_DELETION")
    public void confirmDeletions(String sessionToken, List<? extends IDeletionId> deletionIds)
    {
        confirmDeletionExecutor.confirm(sessionToken, deletionIds);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("SEARCH_CUSTOM_AS_SERVICES")
    public SearchResult<CustomASService> searchCustomASServices(String sessionToken, CustomASServiceSearchCriteria searchCriteria,
            CustomASServiceFetchOptions fetchOptions)
    {
        return searchCustomASServiceExecutor.search(sessionToken, searchCriteria, fetchOptions);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("SEARCH_OBJECT_KIND_MODIFICATION_SERVICES")
    public SearchResult<ObjectKindModification> searchObjectKindModifications(String sessionToken,
            ObjectKindModificationSearchCriteria searchCriteria, ObjectKindModificationFetchOptions fetchOptions)
    {
        return searchObjectKindModificationExecutor.search(sessionToken, searchCriteria, fetchOptions);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("EXECUTE_CUSTOM_AS_SERVICE")
    public Object executeCustomASService(String sessionToken, ICustomASServiceId serviceId, CustomASServiceExecutionOptions options)
    {
        return executeCustomASServiceExecutor.executeService(sessionToken, serviceId, options);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("SEARCH_GLOBALLY")
    public SearchResult<GlobalSearchObject> searchGlobally(String sessionToken, GlobalSearchCriteria searchCriteria,
            GlobalSearchObjectFetchOptions fetchOptions)
    {
        return globalSearchExecutor.search(sessionToken, searchCriteria, fetchOptions);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("ARCHIVE_DATASET")
    public void archiveDataSets(String sessionToken, @AuthorizationGuard(guardClass = V3DataSetIdPredicate.class) List<? extends IDataSetId> dataSetIds, DataSetArchiveOptions options)
    {
        archiveDataSetExecutor.archive(sessionToken, dataSetIds, options);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UNARCHIVE_DATASET")
    public void unarchiveDataSets(String sessionToken, @AuthorizationGuard(guardClass = V3DataSetIdPredicate.class) List<? extends IDataSetId> dataSetIds, DataSetUnarchiveOptions options)
    {
        unarchiveDataSetExecutor.unarchive(sessionToken, dataSetIds, options);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public SessionInformation getSessionInformation(String sessionToken)
    {
        return getSessionInformationExecutor.getSessionInformation(sessionToken);
    }

    @Override
    public Map<String, String> getServerInformation(String sessionToken)
    {
        checkSession(sessionToken);
        Map<String, String> info = new HashMap<String, String>();
        info.put("api-version", getMajorVersion() + "." + getMinorVersion());
//        info.put("project-samples-enabled", Boolean.toString(isProjectSamplesEnabled(null)));
        info.put("archiving-configured", Boolean.toString(isArchivingConfigured(null)));
        info.put("enabled-technologies", configurer.getResolvedProps().getProperty(Constants.ENABLED_MODULES_KEY));
        // String disabledText = tryGetDisabledText();
        // if (disabledText != null)
        // {
        // info.put("server-disabled-info", disabledText);
        // }
        return info;
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
        return 2;
    }
}
