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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.OperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.dataset.IDeleteDataSetExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.dataset.IMapDataSetByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.dataset.ISearchDataSetExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.dataset.IUpdateDataSetExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.deletion.IConfirmDeletionExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.deletion.IListDeletionExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.deletion.IRevertDeletionExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment.ICreateExperimentExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment.IDeleteExperimentExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment.IMapExperimentByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment.ISearchExperimentExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment.IUpdateExperimentExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.material.IMapMaterialByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.sample.ICreateSampleExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.sample.IDeleteSampleExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.sample.IMapSampleByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.sample.ISearchSampleExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.sample.IUpdateSampleExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.space.ICreateSpaceExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.space.IMapSpaceByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.space.IUpdateSpaceExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.common.IdentityTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.common.MapTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.dataset.DataSetTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.deletion.DeletionTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.experiment.ExperimentTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.material.MaterialTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.sample.SampleTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.space.SpaceTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.utils.ExceptionUtils;
import ch.ethz.sis.openbis.generic.shared.api.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.Deletion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.dataset.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.experiment.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.sample.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
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
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.IDataSetId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.IDeletionId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.IMaterialId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.operation.IOperation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.DataSetSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ExperimentSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SampleSearchCriterion;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
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
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * @author pkupczyk
 */
@Component(IApplicationServerApi.INTERNAL_SERVICE_NAME)
public class ApplicationServerApi extends AbstractServer<IApplicationServerApi> implements
        IApplicationServerApi
{
    @Resource(name = ComponentNames.MANAGED_PROPERTY_EVALUATOR_FACTORY)
    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    @Autowired
    private ICreateSpaceExecutor createSpaceExecutor;

    @Autowired
    private ICreateExperimentExecutor createExperimentExecutor;

    @Autowired
    private ICreateSampleExecutor createSampleExecutor;

    @Autowired
    private IUpdateSpaceExecutor updateSpaceExecutor;

    @Autowired
    private IUpdateExperimentExecutor updateExperimentExecutor;

    @Autowired
    private IUpdateSampleExecutor updateSampleExecutor;

    @Autowired
    private IUpdateDataSetExecutor updateDataSetExecutor;

    @Autowired
    private IMapSpaceByIdExecutor mapSpaceByIdExecutor;

    @Autowired
    private IMapExperimentByIdExecutor mapExperimentByIdExecutor;

    @Autowired
    private IDeleteDataSetExecutor deleteDataSetExecutor;

    @Autowired
    private IMapSampleByIdExecutor mapSampleByIdExecutor;

    @Autowired
    private IMapDataSetByIdExecutor mapDataSetByIdExecutor;

    @Autowired
    private IMapMaterialByIdExecutor mapMaterialByIdExecutor;

    @Autowired
    private ISearchExperimentExecutor searchExperimentExecutor;

    @Autowired
    private ISearchSampleExecutor searchSampleExecutor;

    @Autowired
    private ISearchDataSetExecutor searchDataSetExecutor;

    @Autowired
    private IDeleteExperimentExecutor deleteExperimentExecutor;

    @Autowired
    private IDeleteSampleExecutor deleteSampleExecutor;

    @Autowired
    private IListDeletionExecutor listDeletionExecutor;

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
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
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
    public List<? extends IOperationResult> performOperations(String sessionToken,
            List<? extends IOperation> operations)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_ADMIN, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("REGISTER_SPACE")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SPACE)
    public List<SpacePermId> createSpaces(String sessionToken, List<SpaceCreation> creations)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        try
        {
            return createSpaceExecutor.create(context, creations);
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        }
    }

    @Override
    @Transactional
    @RolesAllowed(
    { RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("WRITE_EXPERIMENT")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.EXPERIMENT)
    public List<ExperimentPermId> createExperiments(String sessionToken,
            List<ExperimentCreation> creations)
    {
        // REPLACES:
        // - ServiceForDataStoreServer.registerExperiment()

        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        try
        {
            return createExperimentExecutor.create(context, creations);
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        }
    }

    @Override
    @Transactional
    @RolesAllowed(
    { RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("WRITE_SAMPLE")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SAMPLE)
    public List<SamplePermId> createSamples(String sessionToken,
            List<SampleCreation> creations)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        try
        {
            return createSampleExecutor.create(context, creations);
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        } finally
        {
            // the clear is necessary, as registering samples involves sql queries, that are not visible to cached PE objects
            getDAOFactory().getSessionFactory().getCurrentSession().clear();
        }
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_ADMIN, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_SPACE")
    @DatabaseUpdateModification(value = ObjectKind.SPACE)
    public void updateSpaces(String sessionToken, List<SpaceUpdate> spaceUpdates)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        try
        {
            updateSpaceExecutor.update(context, spaceUpdates);
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        }
    }

    @Override
    @Transactional
    @RolesAllowed(
    { RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("WRITE_EXPERIMENT")
    @DatabaseUpdateModification(value = ObjectKind.EXPERIMENT)
    public void updateExperiments(String sessionToken, List<ExperimentUpdate> experimentUpdates)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        try
        {
            updateExperimentExecutor.update(context, experimentUpdates);
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        } finally
        {
            // the clear is necessary, as updating experiments involves sql queries, that are not visible to cached PE objects
            getDAOFactory().getSessionFactory().getCurrentSession().clear();
        }
    }

    @Override
    @Transactional
    @RolesAllowed(
    { RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("WRITE_SAMPLE")
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public void updateSamples(String sessionToken, List<SampleUpdate> updates)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        try
        {
            updateSampleExecutor.update(context, updates);
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        } finally
        {
            // the clear is necessary, as registering samples involves sql queries, that are not visible to cached PE objects
            getDAOFactory().getSessionFactory().getCurrentSession().clear();
        }
    }

    @Override
    @Transactional
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("WRITE_DATASET")
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public void updateDataSets(String sessionToken, List<DataSetUpdate> updates)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        try
        {
            updateDataSetExecutor.update(context, updates);
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Map<ISpaceId, Space> mapSpaces(String sessionToken, List<? extends ISpaceId> spaceIds, SpaceFetchOptions fetchOptions)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        Map<ISpaceId, SpacePE> map = mapSpaceByIdExecutor.map(context, spaceIds);

        return new MapTranslator<ISpaceId, ISpaceId, SpacePE, Space>().translate(map, new IdentityTranslator<ISpaceId>(),
                new SpaceTranslator(new TranslationContext(session, managedPropertyEvaluatorFactory),
                        fetchOptions));
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Map<IExperimentId, Experiment> mapExperiments(String sessionToken,
            List<? extends IExperimentId> experimentIds, ExperimentFetchOptions fetchOptions)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        Map<IExperimentId, ExperimentPE> map = mapExperimentByIdExecutor.map(context, experimentIds);

        return new MapTranslator<IExperimentId, IExperimentId, ExperimentPE, Experiment>().translate(map, new IdentityTranslator<IExperimentId>(),
                new ExperimentTranslator(new TranslationContext(session, managedPropertyEvaluatorFactory),
                        fetchOptions));
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Map<ISampleId, Sample> mapSamples(String sessionToken, List<? extends ISampleId> sampleIds,
            SampleFetchOptions fetchOptions)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        Map<ISampleId, SamplePE> map = mapSampleByIdExecutor.map(context, sampleIds);

        return new MapTranslator<ISampleId, ISampleId, SamplePE, Sample>().translate(map, new IdentityTranslator<ISampleId>(),
                new SampleTranslator(new TranslationContext(session, managedPropertyEvaluatorFactory),
                        fetchOptions));
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Map<IDataSetId, DataSet> mapDataSets(String sessionToken, List<? extends IDataSetId> dataSetIds, DataSetFetchOptions fetchOptions)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        Map<IDataSetId, DataPE> map = mapDataSetByIdExecutor.map(context, dataSetIds);

        return new MapTranslator<IDataSetId, IDataSetId, DataPE, DataSet>().translate(map, new IdentityTranslator<IDataSetId>(),
                new DataSetTranslator(new TranslationContext(session, managedPropertyEvaluatorFactory),
                        fetchOptions));
    }

    @Override
    public Map<IMaterialId, Material> mapMaterials(String sessionToken, List<? extends IMaterialId> materialIds, MaterialFetchOptions fetchOptions)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        Map<IMaterialId, MaterialPE> map = mapMaterialByIdExecutor.map(context, materialIds);

        return new MapTranslator<IMaterialId, IMaterialId, MaterialPE, Material>().translate(map, new IdentityTranslator<IMaterialId>(),
                new MaterialTranslator(new TranslationContext(session, managedPropertyEvaluatorFactory),
                        fetchOptions));
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Experiment> searchExperiments(String sessionToken, ExperimentSearchCriterion searchCriterion,
            ExperimentFetchOptions fetchOptions)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        try
        {
            List<ExperimentPE> experiments = searchExperimentExecutor.search(context, searchCriterion);

            Map<ExperimentPE, Experiment> translatedMap =
                    new ExperimentTranslator(new TranslationContext(session, managedPropertyEvaluatorFactory), fetchOptions).translate(experiments);
            return new ArrayList<Experiment>(translatedMap.values());
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Sample> searchSamples(String sessionToken, SampleSearchCriterion searchCriterion, SampleFetchOptions fetchOptions)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        try
        {
            List<SamplePE> samples = searchSampleExecutor.search(context, searchCriterion);

            Map<SamplePE, Sample> translatedMap =
                    new SampleTranslator(new TranslationContext(session, managedPropertyEvaluatorFactory), fetchOptions).translate(samples);
            return new ArrayList<Sample>(translatedMap.values());
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<DataSet> searchDataSets(String sessionToken, DataSetSearchCriterion searchCriterion, DataSetFetchOptions fetchOptions)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        try
        {
            List<DataPE> dataSets = searchDataSetExecutor.search(context, searchCriterion);

            Map<DataPE, DataSet> translatedMap =
                    new DataSetTranslator(new TranslationContext(session, managedPropertyEvaluatorFactory), fetchOptions).translate(dataSets);
            return new ArrayList<DataSet>(translatedMap.values());
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
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        try
        {
            List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion> deletions = listDeletionExecutor.list(context, fetchOptions);
            Map<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion, Deletion> translatedMap =
                    new DeletionTranslator(new TranslationContext(session, managedPropertyEvaluatorFactory), fetchOptions, getDAOFactory())
                            .translate(deletions);
            return new ArrayList<Deletion>(translatedMap.values());
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        }
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
