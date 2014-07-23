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

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.ethz.sis.openbis.generic.server.api.v3.authorization.predicate.ListExperimentCreationPredicate;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.OperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment.ICreateExperimentExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment.IListExperimentByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment.IUpdateExperimentExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.sample.ICreateSampleExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.sample.IListSampleByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.sample.IUpdateSampleExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.collection.ListTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.experiment.ExperimentTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.sample.SampleTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.search.EntityAttributeProviderFactory;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.search.ISearchCriterionTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.search.SearchCriterionTranslatorFactory;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.search.SearchTranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.search.SearchCriterionTranslationResult;
import ch.ethz.sis.openbis.generic.server.api.v3.utils.ExceptionUtils;
import ch.ethz.sis.openbis.generic.server.api.v3.validator.entity.experiment.ExperimentValidator;
import ch.ethz.sis.openbis.generic.shared.api.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.operation.IOperation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ExperimentSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SampleSearchCriterion;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.search.ExperimentSearchManager;
import ch.systemsx.cisd.openbis.generic.server.business.search.SampleSearchManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
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
    @Resource(name = ComponentNames.MANAGED_PROPERTY_EVALUATOR_FACTORY)
    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    private ICommonBusinessObjectFactory businessObjectFactory;

    @Resource(name = ComponentNames.RELATIONSHIP_SERVICE)
    private IRelationshipService relationshipService;

    @Autowired
    private IListExperimentByIdExecutor listExperimentByIdExecutor;

    @Autowired
    private IListSampleByIdExecutor listSampleByIdExecutor;

    @Autowired
    private ICreateSampleExecutor createSampleExecutor;

    @Autowired
    private IUpdateSampleExecutor updateSampleExecutor;

    @Autowired
    private ICreateExperimentExecutor createExperimentExecutor;

    @Autowired
    private IUpdateExperimentExecutor updateExperimentExecutor;

    // Default constructor needed by Spring
    public ApplicationServerApi()
    {
    }

    public ApplicationServerApi(IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            ICommonBusinessObjectFactory businessObjectFactory, IRelationshipService relationshipService,
            IOpenBisSessionManager sessionManager, IDAOFactory daoFactory,
            IPropertiesBatchManager propertiesBatchManager,
            ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin)
    {
        super(sessionManager, daoFactory, propertiesBatchManager, sampleTypeSlaveServerPlugin,
                dataSetTypeSlaveServerPlugin);
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
        this.businessObjectFactory = businessObjectFactory;
        this.relationshipService = relationshipService;
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
    @RolesAllowed(
    { RoleWithHierarchy.SPACE_USER })
    @DatabaseCreateOrDeleteModification(value = ObjectKind.EXPERIMENT)
    public List<ExperimentPermId> createExperiments(String sessionToken,
            @AuthorizationGuard(guardClass = ListExperimentCreationPredicate.class)
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
    public List<SamplePermId> createSamples(String sessionToken, List<SampleCreation> creations)
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
    @Transactional(readOnly = true)
    @RolesAllowed(
    { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @ReturnValueFilter(validatorClass = ExperimentValidator.class)
    public List<Experiment> listExperiments(String sessionToken,
            List<? extends IExperimentId> experimentIds, ExperimentFetchOptions fetchOptions)
    {
        if (experimentIds == null)
        {
            throw new IllegalArgumentException("Experiment ids cannot be null");
        }
        if (fetchOptions == null)
        {
            throw new IllegalArgumentException("Fetch options cannot be null");
        }

        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        List<ExperimentPE> experiments = listExperimentByIdExecutor.list(context, experimentIds);

        return new ListTranslator().translate(experiments, new ExperimentTranslator(
                new TranslationContext(session), managedPropertyEvaluatorFactory, fetchOptions));
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(
    { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    // TODO shall we still use database instance for validation ? @ReturnValueFilter(validatorClass
    // = SampleValidator.class)
    public List<Sample> listSamples(String sessionToken, List<? extends ISampleId> sampleIds,
            SampleFetchOptions fetchOptions)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        List<SamplePE> samples = listSampleByIdExecutor.list(context, sampleIds);

        return new ListTranslator().translate(samples, new SampleTranslator(
                new TranslationContext(session), managedPropertyEvaluatorFactory, fetchOptions));
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(
    { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @ReturnValueFilter(validatorClass = ExperimentValidator.class)
    public List<Experiment> searchExperiments(String sessionToken, ExperimentSearchCriterion searchCriterion,
            ExperimentFetchOptions fetchOptions)
    {
        Session session = getSession(sessionToken);

        ISearchCriterionTranslator translator =
                new SearchCriterionTranslatorFactory(getDAOFactory(), new EntityAttributeProviderFactory()).getTranslator(searchCriterion);
        SearchCriterionTranslationResult translationResult = translator.translate(new SearchTranslationContext(session), searchCriterion);

        ExperimentSearchManager searchManager =
                new ExperimentSearchManager(getDAOFactory().getHibernateSearchDAO(),
                        getBusinessObjectFactory().createExperimentTable(session));

        Collection<Long> experimentIds =
                searchManager.searchForExperimentIDs(session.getUserName(), translationResult.getCriteria());

        List<ExperimentPE> experiments = getDAOFactory().getExperimentDAO().listByIDs(experimentIds);

        return new ListTranslator().translate(experiments, new ExperimentTranslator(
                new TranslationContext(session), managedPropertyEvaluatorFactory, fetchOptions));
    }

    @Override
    @Transactional(readOnly = true)
    @RolesAllowed(
    { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    // TODO shall we still use database instance for validation ? @ReturnValueFilter(validatorClass
    // = SampleValidator.class)
    public List<Sample> searchSamples(String sessionToken, SampleSearchCriterion searchCriterion, SampleFetchOptions fetchOptions)
    {
        Session session = getSession(sessionToken);

        ISearchCriterionTranslator translator =
                new SearchCriterionTranslatorFactory(getDAOFactory(), new EntityAttributeProviderFactory()).getTranslator(searchCriterion);
        SearchCriterionTranslationResult translationResult = translator.translate(new SearchTranslationContext(session), searchCriterion);

        SampleSearchManager searchManager =
                new SampleSearchManager(getDAOFactory().getHibernateSearchDAO(),
                        getBusinessObjectFactory().createSampleLister(session));

        Collection<Long> sampleIds =
                searchManager.searchForSampleIDs(session.getUserName(), translationResult.getCriteria());

        List<SamplePE> samples = getDAOFactory().getSampleDAO().listByIDs(sampleIds);

        return new ListTranslator().translate(samples, new SampleTranslator(
                new TranslationContext(session), managedPropertyEvaluatorFactory, fetchOptions));
    }

    @Override
    public IApplicationServerApi createLogger(IInvocationLoggerContext context)
    {
        return new ApplicationServerApiLogger(sessionManager, context);
    }

    private ICommonBusinessObjectFactory getBusinessObjectFactory()
    {
        return businessObjectFactory;
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
