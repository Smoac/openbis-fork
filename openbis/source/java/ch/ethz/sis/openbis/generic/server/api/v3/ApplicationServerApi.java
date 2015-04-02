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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.OperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.dataset.IDeleteDataSetExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.deletion.IConfirmDeletionExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.deletion.IRevertDeletionExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment.IDeleteExperimentExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.material.IDeleteMaterialExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.ICreateExperimentMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.ICreateMaterialMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.ICreateProjectMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.ICreateSampleMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.ICreateSpaceMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.IListDeletionMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.IMapDataSetMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.IMapExperimentMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.IMapMaterialMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.IMapProjectMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.IMapSampleMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.IMapSpaceMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.ISearchDataSetMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.ISearchExperimentMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.ISearchMaterialMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.ISearchProjectMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.ISearchSampleMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.ISearchSpaceMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.IUpdateDataSetMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.IUpdateExperimentMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.IUpdateMaterialMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.IUpdateProjectMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.IUpdateSampleMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.method.IUpdateSpaceMethodExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.project.IDeleteProjectExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.sample.IDeleteSampleExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.space.IDeleteSpaceExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.utils.ExceptionUtils;
import ch.ethz.sis.openbis.generic.shared.api.v3.IApplicationServerApi;
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
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.DataSetSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ExperimentSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.MaterialSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ProjectSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SampleSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SpaceSearchCriterion;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
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
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * @author pkupczyk
 */
@Component(IApplicationServerApi.INTERNAL_SERVICE_NAME)
public class ApplicationServerApi extends AbstractServer<IApplicationServerApi> implements
        IApplicationServerApi
{
    @Autowired
    private ICreateSpaceMethodExecutor createSpaceExecutor;

    @Autowired
    private ICreateProjectMethodExecutor createProjectExecutor;

    @Autowired
    private ICreateExperimentMethodExecutor createExperimentExecutor;

    @Autowired
    private ICreateSampleMethodExecutor createSampleExecutor;

    @Autowired
    private ICreateMaterialMethodExecutor createMaterialExecutor;

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
    private IMapSpaceMethodExecutor mapSpaceExecutor;

    @Autowired
    private IMapProjectMethodExecutor mapProjectExecutor;

    @Autowired
    private IMapExperimentMethodExecutor mapExperimentExecutor;

    @Autowired
    private IMapSampleMethodExecutor mapSampleExecutor;

    @Autowired
    private IMapDataSetMethodExecutor mapDataSetExecutor;

    @Autowired
    private IMapMaterialMethodExecutor mapMaterialExecutor;

    @Autowired
    private ISearchSpaceMethodExecutor searchSpaceExecutor;

    @Autowired
    private ISearchProjectMethodExecutor searchProjectExecutor;

    @Autowired
    private ISearchExperimentMethodExecutor searchExperimentExecutor;

    @Autowired
    private ISearchSampleMethodExecutor searchSampleExecutor;

    @Autowired
    private ISearchDataSetMethodExecutor searchDataSetExecutor;

    @Autowired
    private ISearchMaterialMethodExecutor searchMaterialExecutor;

    @Autowired
    private IDeleteSpaceExecutor deleteSpaceExecutor;

    @Autowired
    private IDeleteProjectExecutor deleteProjectExecutor;

    @Autowired
    private IDeleteExperimentExecutor deleteExperimentExecutor;

    @Autowired
    private IDeleteSampleExecutor deleteSampleExecutor;

    @Autowired
    private IDeleteDataSetExecutor deleteDataSetExecutor;

    @Autowired
    private IDeleteMaterialExecutor deleteMaterialExecutor;

    @Autowired
    private IListDeletionMethodExecutor listDeletionExecutor;

    @Autowired
    private IRevertDeletionExecutor revertDeletionExecutor;

    @Autowired
    private IConfirmDeletionExecutor confirmDeletionExecutor;

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
    public String loginAs(String userId, String password, String asUserId)
    {
        SessionContextDTO session = tryAuthenticateAs(userId, password, asUserId);
        return session == null ? null : session.getSessionToken();
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_ADMIN, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("REGISTER_SPACE")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SPACE)
    public List<SpacePermId> createSpaces(String sessionToken, List<SpaceCreation> creations)
    {
        return createSpaceExecutor.create(sessionToken, creations);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("REGISTER_PROJECT")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.PROJECT)
    public List<ProjectPermId> createProjects(String sessionToken, List<ProjectCreation> creations)
    {
        return createProjectExecutor.create(sessionToken, creations);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("WRITE_EXPERIMENT")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.EXPERIMENT)
    public List<ExperimentPermId> createExperiments(String sessionToken,
            List<ExperimentCreation> creations)
    {
        return createExperimentExecutor.create(sessionToken, creations);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("WRITE_SAMPLE")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SAMPLE)
    public List<SamplePermId> createSamples(String sessionToken,
            List<SampleCreation> creations)
    {
        return createSampleExecutor.create(sessionToken, creations);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("WRITE_MATERIAL")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.MATERIAL)
    public List<MaterialPermId> createMaterials(String sessionToken, List<MaterialCreation> creations)
    {
        return createMaterialExecutor.create(sessionToken, creations);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_ADMIN, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_SPACE")
    @DatabaseUpdateModification(value = ObjectKind.SPACE)
    public void updateSpaces(String sessionToken, List<SpaceUpdate> updates)
    {
        updateSpaceExecutor.update(sessionToken, updates);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("WRITE_PROJECT")
    @DatabaseUpdateModification(value = ObjectKind.PROJECT)
    public void updateProjects(String sessionToken, List<ProjectUpdate> updates)
    {
        updateProjectExecutor.update(sessionToken, updates);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("WRITE_EXPERIMENT")
    @DatabaseUpdateModification(value = ObjectKind.EXPERIMENT)
    public void updateExperiments(String sessionToken, List<ExperimentUpdate> updates)
    {
        updateExperimentExecutor.update(sessionToken, updates);
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("WRITE_SAMPLE")
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public void updateSamples(String sessionToken, List<SampleUpdate> updates)
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
    @Capability("WRITE_DATASET")
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public void updateDataSets(String sessionToken, List<DataSetUpdate> updates)
    {
        updateDataSetExecutor.update(sessionToken, updates);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Map<ISpaceId, Space> mapSpaces(String sessionToken, List<? extends ISpaceId> spaceIds, SpaceFetchOptions fetchOptions)
    {
        return mapSpaceExecutor.map(sessionToken, spaceIds, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Map<IProjectId, Project> mapProjects(String sessionToken, List<? extends IProjectId> projectIds, ProjectFetchOptions fetchOptions)
    {
        return mapProjectExecutor.map(sessionToken, projectIds, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Map<IExperimentId, Experiment> mapExperiments(String sessionToken,
            List<? extends IExperimentId> experimentIds, ExperimentFetchOptions fetchOptions)
    {
        return mapExperimentExecutor.map(sessionToken, experimentIds, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Map<ISampleId, Sample> mapSamples(String sessionToken, List<? extends ISampleId> sampleIds,
            SampleFetchOptions fetchOptions)
    {
        return mapSampleExecutor.map(sessionToken, sampleIds, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Map<IDataSetId, DataSet> mapDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetFetchOptions fetchOptions)
    {
        return mapDataSetExecutor.map(sessionToken, dataSetIds, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Map<IMaterialId, Material> mapMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialFetchOptions fetchOptions)
    {
        return mapMaterialExecutor.map(sessionToken, materialIds, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Space> searchSpaces(String sessionToken, SpaceSearchCriterion searchCriterion, SpaceFetchOptions fetchOptions)
    {
        return searchSpaceExecutor.search(sessionToken, searchCriterion, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Project> searchProjects(String sessionToken, ProjectSearchCriterion searchCriterion, ProjectFetchOptions fetchOptions)
    {
        return searchProjectExecutor.search(sessionToken, searchCriterion, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Experiment> searchExperiments(String sessionToken, ExperimentSearchCriterion searchCriterion,
            ExperimentFetchOptions fetchOptions)
    {
        return searchExperimentExecutor.search(sessionToken, searchCriterion, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Sample> searchSamples(String sessionToken, SampleSearchCriterion searchCriterion, SampleFetchOptions fetchOptions)
    {
        return searchSampleExecutor.search(sessionToken, searchCriterion, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<DataSet> searchDataSets(String sessionToken, DataSetSearchCriterion searchCriterion, DataSetFetchOptions fetchOptions)
    {
        return searchDataSetExecutor.search(sessionToken, searchCriterion, fetchOptions);
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Material> searchMaterials(String sessionToken, MaterialSearchCriterion searchCriterion, MaterialFetchOptions fetchOptions)
    {
        return searchMaterialExecutor.search(sessionToken, searchCriterion, fetchOptions);
    }

    @Override
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.SPACE, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.SPACE_ADMIN, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_SPACE")
    public void deleteSpaces(String sessionToken, List<? extends ISpaceId> spaceIds, SpaceDeletionOptions deletionOptions)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        try
        {
            deleteSpaceExecutor.delete(context, spaceIds, deletionOptions);
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        }
    }

    @Override
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.PROJECT, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_PROJECT")
    public void deleteProjects(String sessionToken, List<? extends IProjectId> projectIds, ProjectDeletionOptions deletionOptions)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        try
        {
            deleteProjectExecutor.delete(context, projectIds, deletionOptions);
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        }
    }

    @Override
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.EXPERIMENT, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_EXPERIMENT")
    public IDeletionId deleteExperiments(String sessionToken, List<? extends IExperimentId> experimentIds, ExperimentDeletionOptions deletionOptions)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        try
        {
            return deleteExperimentExecutor.delete(context, experimentIds, deletionOptions);
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        } finally
        {
            // the clear is necessary, as deleting experiments involves sql queries, that are not visible to cached PE objects
            getDAOFactory().getSessionFactory().getCurrentSession().clear();
        }
    }

    @Override
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.DATA_SET, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_DATA_SET")
    public IDeletionId deleteDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetDeletionOptions deletionOptions)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);
        try
        {
            return deleteDataSetExecutor.delete(context, dataSetIds, deletionOptions);
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        } finally
        {
            getDAOFactory().getSessionFactory().getCurrentSession().clear();
        }
    }

    @Override
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.MATERIAL, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("DELETE_MATERIAL")
    public void deleteMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialDeletionOptions deletionOptions)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);
        try
        {
            deleteMaterialExecutor.delete(context, materialIds, deletionOptions);
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        } finally
        {
            getDAOFactory().getSessionFactory().getCurrentSession().clear();
        }
    }

    @Override
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.SAMPLE, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_SAMPLE")
    public IDeletionId deleteSamples(String sessionToken, List<? extends ISampleId> sampleIds, SampleDeletionOptions deletionOptions)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        try
        {
            return deleteSampleExecutor.delete(context, sampleIds, deletionOptions);
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        } finally
        {
            // the clear is necessary, as deleting samples involves sql queries, that are not visible to cached PE objects
            getDAOFactory().getSessionFactory().getCurrentSession().clear();
        }
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Deletion> listDeletions(String sessionToken, DeletionFetchOptions fetchOptions)
    {
        return listDeletionExecutor.listDeletions(sessionToken, fetchOptions);
    }

    @Override
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DELETION)
    @DatabaseUpdateModification(value = { ObjectKind.EXPERIMENT, ObjectKind.SAMPLE, ObjectKind.DATA_SET })
    @RolesAllowed({ RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("RESTORE")
    public void revertDeletions(String sessionToken, List<? extends IDeletionId> deletionIds)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        try
        {
            revertDeletionExecutor.revert(context, deletionIds);
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        } finally
        {
            getDAOFactory().getSessionFactory().getCurrentSession().clear();
        }
    }

    @Override
    @Transactional
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.DELETION, ObjectKind.EXPERIMENT, ObjectKind.SAMPLE, ObjectKind.DATA_SET })
    @RolesAllowed({ RoleWithHierarchy.SPACE_ADMIN, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("PURGE")
    public void confirmDeletions(String sessionToken, List<? extends IDeletionId> deletionIds)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        try
        {
            confirmDeletionExecutor.confirm(context, deletionIds);
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        } finally
        {
            getDAOFactory().getSessionFactory().getCurrentSession().clear();
        }
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
        return 0;
    }

}
