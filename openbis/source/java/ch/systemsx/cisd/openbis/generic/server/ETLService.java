/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import ch.systemsx.cisd.authentication.DefaultSessionManager;
import ch.systemsx.cisd.authentication.DummyAuthenticationService;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.servlet.RequestContextProviderAdapter;
import ch.systemsx.cisd.openbis.common.conversation.context.ServiceConversationsThreadContext;
import ch.systemsx.cisd.openbis.common.conversation.progress.IServiceConversationProgressListener;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.api.v1.SearchCriteriaToDetailedSearchCriteriaTranslator;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationServiceUtils;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AtomicOperationsPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetCodeCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetCodePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExistingSampleOwnerIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExistingSpaceIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ListSampleCriteriaPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ListSamplesByPropertyPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewExperimentPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewSamplePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewSamplesWithTypePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleOwnerIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SpaceIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ProjectValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleValidator;
import ch.systemsx.cisd.openbis.generic.server.batch.BatchOperationExecutor;
import ch.systemsx.cisd.openbis.generic.server.batch.DataSetBatchUpdate;
import ch.systemsx.cisd.openbis.generic.server.batch.DataSetCheckBeforeBatchUpdate;
import ch.systemsx.cisd.openbis.generic.server.batch.SampleBatchRegistration;
import ch.systemsx.cisd.openbis.generic.server.batch.SampleCheckBeforeUpdate;
import ch.systemsx.cisd.openbis.generic.server.batch.SampleUpdate;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.IServiceConversationClientManagerLocal;
import ch.systemsx.cisd.openbis.generic.server.business.IServiceConversationServerManagerLocal;
import ch.systemsx.cisd.openbis.generic.server.business.bo.EntityCodeGenerator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataSetTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMetaprojectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IProjectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IRoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISpaceBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.experimentlister.ExperimentLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataSetTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.LogMessagePrefixGenerator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchableEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.MetaprojectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.basic.EntityOperationsState;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMetaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyTypeWithVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetShareId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityCollectionForCreationOrUpdate;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityOperationsLogEntryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewRoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DatabaseInstanceTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.EntityPropertyTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator.LoadableFields;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExternalDataManagementSystemTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MetaprojectTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.PersonTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ProjectTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SimpleDataSetHelper;
import ch.systemsx.cisd.openbis.generic.shared.translator.SpaceTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.VocabularyTermTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.VocabularyTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author Franz-Josef Elmer
 */
public class ETLService extends AbstractCommonServer<IETLLIMSService> implements IETLLIMSService
{

    private final IDAOFactory daoFactory;

    private final IDataStoreServiceFactory dssFactory;

    private final TrustedCrossOriginDomainsProvider trustedOriginDomainProvider;

    private final IETLEntityOperationChecker entityOperationChecker;

    private final ISessionManager<Session> sessionManagerForEntityOperation;

    private final IDataStoreServiceRegistrator dataStoreServiceRegistrator;

    private IServiceConversationClientManagerLocal conversationClient;

    private IServiceConversationServerManagerLocal conversationServer;

    public ETLService(IAuthenticationService authenticationService,
            ISessionManager<Session> sessionManager, IDAOFactory daoFactory,
            ICommonBusinessObjectFactory boFactory, IDataStoreServiceFactory dssFactory,
            TrustedCrossOriginDomainsProvider trustedOriginDomainProvider,
            IETLEntityOperationChecker entityOperationChecker,
            IDataStoreServiceRegistrator dataStoreServiceRegistrator)
    {
        this(authenticationService, sessionManager, daoFactory, null, boFactory, dssFactory,
                trustedOriginDomainProvider, entityOperationChecker, dataStoreServiceRegistrator,
                new DefaultSessionManager<Session>(new SessionFactory(),
                        new LogMessagePrefixGenerator(), new DummyAuthenticationService(),
                        new RequestContextProviderAdapter(new IRequestContextProvider()
                            {
                                @Override
                                public HttpServletRequest getHttpServletRequest()
                                {
                                    return null;
                                }
                            }), 30));
    }

    ETLService(IAuthenticationService authenticationService,
            ISessionManager<Session> sessionManager, IDAOFactory daoFactory,
            IPropertiesBatchManager propertiesBatchManager, ICommonBusinessObjectFactory boFactory,
            IDataStoreServiceFactory dssFactory,
            TrustedCrossOriginDomainsProvider trustedOriginDomainProvider,
            IETLEntityOperationChecker entityOperationChecker,
            IDataStoreServiceRegistrator dataStoreServiceRegistrator,
            ISessionManager<Session> sessionManagerForEntityOperation)
    {
        super(authenticationService, sessionManager, daoFactory, propertiesBatchManager, boFactory);
        this.daoFactory = daoFactory;
        this.dssFactory = dssFactory;
        this.trustedOriginDomainProvider = trustedOriginDomainProvider;
        this.entityOperationChecker = entityOperationChecker;
        this.dataStoreServiceRegistrator = dataStoreServiceRegistrator;
        this.sessionManagerForEntityOperation = sessionManagerForEntityOperation;
    }

    @Override
    public IETLLIMSService createLogger(IInvocationLoggerContext context)
    {
        return new ETLServiceLogger(getSessionManager(), context);
    }

    @Override
    public int getVersion()
    {
        return IServer.VERSION;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public DatabaseInstance getHomeDatabaseInstance(final String sessionToken)
    {
        return DatabaseInstanceTranslator.translate(getHomeDatabaseInstance());
    }

    private DatabaseInstancePE getHomeDatabaseInstance()
    {
        return daoFactory.getHomeDatabaseInstance();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void registerDataStoreServer(String sessionToken, DataStoreServerInfo info)
    {
        Session session = getSession(sessionToken);

        String dssSessionToken = info.getSessionToken();
        String dssURL = checkVersion(info, session, dssSessionToken);
        IDataStoreDAO dataStoreDAO = daoFactory.getDataStoreDAO();
        DataStorePE dataStore = dataStoreDAO.tryToFindDataStoreByCode(info.getDataStoreCode());
        if (dataStore == null)
        {
            dataStore = new DataStorePE();
            dataStore.setDatabaseInstance(getHomeDatabaseInstance());
        }
        dataStore.setCode(info.getDataStoreCode());
        dataStore.setDownloadUrl(info.getDownloadUrl());
        dataStore.setRemoteUrl(dssURL);
        dataStore.setSessionToken(dssSessionToken);
        dataStore.setArchiverConfigured(info.isArchiverConfigured());
        dataStore.setServices(new HashSet<DataStoreServicePE>()); // services will be set by the
                                                                  // dataStoreServiceRegistrator
        // setServices(dataStore, info.getServicesDescriptions(), dataStoreDAO);
        dataStoreDAO.createOrUpdateDataStore(dataStore);
        dataStoreServiceRegistrator.setServiceDescriptions(dataStore,
                info.getServicesDescriptions());

        conversationClient.setDataStoreInformation(dssURL, info.getTimeoutInMinutes());
        conversationServer.setDataStoreInformation(info.getDataStoreCode(), dssURL,
                info.getTimeoutInMinutes());
    }

    private String checkVersion(DataStoreServerInfo info, Session session, String dssSessionToken)
    {
        int port = info.getPort();
        String remoteHost = session.getRemoteHost() + ":" + port;
        String dssURL = (info.isUseSSL() ? "https://" : "http://") + remoteHost;
        checkVersion(dssSessionToken, dssURL);
        return dssURL;
    }

    private void checkVersion(String dssSessionToken, final String dssURL)
    {
        final IDataStoreService service = dssFactory.create(dssURL);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Obtain version of Data Store Server at " + dssURL);
        }
        int dssVersion = service.getVersion(dssSessionToken);
        if (IDataStoreService.VERSION != dssVersion)
        {
            String msg =
                    "Data Store Server version is " + dssVersion + " instead of "
                            + IDataStoreService.VERSION;
            notificationLog.error(msg);
            throw new ConfigurationFailureException(msg);
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Data Store Server (version " + dssVersion + ") registered for "
                    + dssURL);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public String createPermId(final String sessionToken) throws UserFailureException
    {
        checkSession(sessionToken); // throws exception if invalid sessionToken
        return daoFactory.getPermIdDAO().createPermId();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public long drawANewUniqueID(String sessionToken) throws UserFailureException
    {
        checkSession(sessionToken);
        return daoFactory.getCodeSequenceDAO().getNextCodeSequenceId();
    }

    @Override
    @RolesAllowed(
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Experiment> listExperiments(String sessionToken,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class)
            List<ExperimentIdentifier> experimentIdentifiers,
            ExperimentFetchOptions experimentFetchOptions)
    {
        if (sessionToken == null)
        {
            throw new IllegalArgumentException("SessionToken was null");
        }
        if (experimentIdentifiers == null)
        {
            throw new IllegalArgumentException("ExperimentIdentifiers were null");
        }
        if (experimentFetchOptions == null)
        {
            throw new IllegalArgumentException("ExperimentFetchOptions were null");
        }

        checkSession(sessionToken);

        if (experimentFetchOptions.isSubsetOf(ExperimentFetchOption.BASIC,
                ExperimentFetchOption.METAPROJECTS))
        {
            ExperimentLister lister =
                    new ExperimentLister(getDAOFactory(), getSession(sessionToken)
                            .getBaseIndexURL());
            return lister.listExperiments(experimentIdentifiers, experimentFetchOptions);
        } else
        {
            List<Experiment> experiments = new ArrayList<Experiment>();
            for (ExperimentIdentifier experimentIdentifier : experimentIdentifiers)
            {
                Experiment experiment = tryToGetExperiment(sessionToken, experimentIdentifier);
                if (experiment != null)
                {
                    experiment.setFetchOptions(new ExperimentFetchOptions(ExperimentFetchOption
                            .values()));
                    experiments.add(experiment);
                }
            }
            return experiments;
        }
    }

    @Override
    @RolesAllowed(
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Experiment> listExperimentsForProjects(String sessionToken,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class)
            List<ProjectIdentifier> projectIdentifiers,
            ExperimentFetchOptions experimentFetchOptions)
    {
        if (sessionToken == null)
        {
            throw new IllegalArgumentException("SessionToken was null");
        }
        if (projectIdentifiers == null)
        {
            throw new IllegalArgumentException("ProjectIdentifiers were null");
        }
        if (experimentFetchOptions == null)
        {
            throw new IllegalArgumentException("ExperimentFetchOptions were null");
        }

        checkSession(sessionToken);

        if (experimentFetchOptions.isSubsetOf(ExperimentFetchOption.BASIC,
                ExperimentFetchOption.METAPROJECTS))
        {
            ExperimentLister lister =
                    new ExperimentLister(daoFactory, getSession(sessionToken).getBaseIndexURL());
            return lister.listExperimentsForProjects(projectIdentifiers, experimentFetchOptions);
        } else
        {
            List<Experiment> experiments = new ArrayList<Experiment>();
            for (ProjectIdentifier projectIdentifier : projectIdentifiers)
            {
                List<Experiment> projectExperiments =
                        listExperiments(sessionToken, projectIdentifier);
                if (projectExperiments != null)
                {
                    for (Experiment projectExperiment : projectExperiments)
                    {
                        if (projectExperiment != null)
                        {
                            projectExperiment.setFetchOptions(new ExperimentFetchOptions(
                                    ExperimentFetchOption.values()));
                            experiments.add(projectExperiment);
                        }
                    }

                }
            }
            return experiments;
        }
    }

    @Override
    @RolesAllowed(
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Experiment tryToGetExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = ExistingSpaceIdentifierPredicate.class)
            ExperimentIdentifier experimentIdentifier) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert experimentIdentifier != null : "Unspecified experiment identifier.";

        final Session session = getSession(sessionToken);
        ExperimentPE experiment = tryToLoadExperimentByIdentifier(session, experimentIdentifier);
        if (experiment == null)
        {
            return null;
        }
        enrichWithProperties(experiment);

        Collection<MetaprojectPE> metaprojectPEs =
                getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                        session.tryGetPerson(), experiment);

        return ExperimentTranslator.translate(experiment, session.getBaseIndexURL(),
                MetaprojectTranslator.translate(metaprojectPEs), LoadableFields.PROPERTIES);
    }

    @Override
    @RolesAllowed(
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @ReturnValueFilter(validatorClass = SampleValidator.class)
    public List<Sample> listSamples(final String sessionToken,
            @AuthorizationGuard(guardClass = ListSampleCriteriaPredicate.class)
            final ListSampleCriteria criteria)
    {
        final Session session = getSession(sessionToken);
        final ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        return sampleLister.list(new ListOrSearchSampleCriteria(criteria));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Sample tryGetSampleWithExperiment(final String sessionToken,
            @AuthorizationGuard(guardClass = ExistingSampleOwnerIdentifierPredicate.class)
            final SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleIdentifier != null : "Unspecified sample identifier.";

        final Session session = getSession(sessionToken);
        SamplePE sample = tryLoadSample(session, sampleIdentifier);
        Collection<MetaprojectPE> metaprojects = Collections.emptySet();
        if (sample != null)
        {
            HibernateUtils.initialize(sample.getProperties());
            enrichWithProperties(sample.getExperiment());
            metaprojects =
                    getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                            session.tryGetPerson(), sample);
        }
        return SampleTranslator.translate(sample, session.getBaseIndexURL(), true, true,
                MetaprojectTranslator.translate(metaprojects));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public SampleIdentifier tryToGetSampleIdentifier(String sessionToken, String samplePermID)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert samplePermID != null : "Unspecified sample perm ID.";

        final SamplePE sample = daoFactory.getSampleDAO().tryToFindByPermID(samplePermID);
        return (sample == null) ? null : sample.getSampleIdentifier();
    }

    private ExperimentPE tryLoadExperimentBySampleIdentifier(final Session session,
            SampleIdentifier sampleIdentifier)
    {
        final SamplePE sample = tryLoadSample(session, sampleIdentifier);
        return sample == null ? null : sample.getExperiment();
    }

    private ExperimentPE tryToLoadExperimentByIdentifier(final Session session,
            ExperimentIdentifier experimentIdentifier)
    {
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        return experimentBO.tryFindByExperimentIdentifier(experimentIdentifier);
    }

    private SamplePE tryLoadSample(final Session session, SampleIdentifier sampleIdentifier)
    {
        SamplePE result = null;
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        try
        {
            sampleBO.tryToLoadBySampleIdentifier(sampleIdentifier);
            result = sampleBO.tryToGetSample();
        } catch (UserFailureException ufe)
        {
            // sample does not exist
        }
        return result;
    }

    private void enrichWithProperties(ExperimentPE experiment)
    {
        if (experiment == null)
        {
            return;
        }
        HibernateUtils.initialize(experiment.getProperties());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public ExperimentType getExperimentType(String sessionToken, String experimentTypeCode)
            throws UserFailureException
    {
        checkSession(sessionToken);

        IEntityTypeDAO entityTypeDAO = getDAOFactory().getEntityTypeDAO(EntityKind.EXPERIMENT);
        EntityTypePE entityType = entityTypeDAO.tryToFindEntityTypeByCode(experimentTypeCode);
        if (entityType == null)
        {
            throw new UserFailureException("No Experiment type found with code '"
                    + experimentTypeCode + "'.");
        }
        assert entityType instanceof ExperimentTypePE : "Not an ExperimentTypePE: " + entityType;
        ExperimentTypePE experimentType = (ExperimentTypePE) entityType;
        HibernateUtils.initialize(experimentType.getExperimentTypePropertyTypes());
        return ExperimentTypeTranslator.translate(experimentType, null);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public SampleType getSampleType(String sessionToken, String sampleTypeCode)
            throws UserFailureException
    {
        checkSession(sessionToken);

        ISampleTypeDAO sampleTypeDAO = getDAOFactory().getSampleTypeDAO();
        SampleTypePE sampleType = sampleTypeDAO.tryFindSampleTypeByCode(sampleTypeCode);
        if (sampleType == null)
        {
            throw new UserFailureException("No sample type found with code '" + sampleTypeCode
                    + "'.");
        }
        HibernateUtils.initialize(sampleType.getSampleTypePropertyTypes());
        return SampleTypeTranslator.translate(sampleType, null);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public DataSetTypeWithVocabularyTerms getDataSetType(String sessionToken, String dataSetTypeCode)
            throws UserFailureException
    {
        checkSession(sessionToken);

        IDataSetTypeDAO dataSetTypeDAO = getDAOFactory().getDataSetTypeDAO();
        DataSetTypePE dataSetType = dataSetTypeDAO.tryToFindDataSetTypeByCode(dataSetTypeCode);
        if (dataSetType == null)
        {
            throw new UserFailureException("No data set type found with code '" + dataSetTypeCode
                    + "'.");
        }
        Set<DataSetTypePropertyTypePE> dataSetTypePropertyTypes =
                dataSetType.getDataSetTypePropertyTypes();
        HibernateUtils.initialize(dataSetTypePropertyTypes);
        DataSetTypeWithVocabularyTerms result = new DataSetTypeWithVocabularyTerms();
        result.setDataSetType(DataSetTypeTranslator.translate(dataSetType, null));
        for (DataSetTypePropertyTypePE dataSetTypePropertyTypePE : dataSetTypePropertyTypes)
        {
            PropertyTypePE propertyTypePE = dataSetTypePropertyTypePE.getPropertyType();
            PropertyTypeWithVocabulary propertyType = new PropertyTypeWithVocabulary();
            propertyType.setCode(propertyTypePE.getCode());
            VocabularyPE vocabulary = propertyTypePE.getVocabulary();
            if (vocabulary != null)
            {
                Set<VocabularyTermPE> terms = vocabulary.getTerms();
                HibernateUtils.initialize(terms);
                propertyType.setTerms(VocabularyTermTranslator.translateTerms(terms));
            }
            result.addPropertyType(propertyType);
        }
        return result;
    }

    @Override
    @RolesAllowed(
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<ExternalData> listDataSetsByExperimentID(final String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class)
            final TechId experimentID) throws UserFailureException
    {
        Session session = getSession(sessionToken);
        IDatasetLister datasetLister = createDatasetLister(session);
        List<ExternalData> datasets = datasetLister.listByExperimentTechId(experimentID, true);
        Collections.sort(datasets);
        return datasets;
    }

    @Override
    @RolesAllowed(
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<ExternalData> listDataSetsBySampleID(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class)
            final TechId sampleId, final boolean showOnlyDirectlyConnected)
            throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        final IDatasetLister datasetLister = createDatasetLister(session);
        final List<ExternalData> datasets =
                datasetLister.listBySampleTechId(sampleId, showOnlyDirectlyConnected);
        Collections.sort(datasets);
        return datasets;
    }

    @Override
    @RolesAllowed(
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<ExternalData> listDataSetsByCode(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class)
            List<String> dataSetCodes) throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        final IDatasetLister datasetLister = createDatasetLister(session);
        return datasetLister.listByDatasetCode(dataSetCodes);
    }

    @Override
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @ReturnValueFilter(validatorClass = ProjectValidator.class)
    public List<Project> listProjects(String sessionToken)
    {
        checkSession(sessionToken);
        final List<ProjectPE> projects = getDAOFactory().getProjectDAO().listProjects();
        Collections.sort(projects);
        return ProjectTranslator.translate(projects);
    }

    @Override
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Experiment> listExperiments(String sessionToken,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class)
            ProjectIdentifier projectIdentifier)
    {
        final Session session = getSession(sessionToken);
        final IExperimentTable experimentTable =
                businessObjectFactory.createExperimentTable(session);
        experimentTable.load(EntityType.ALL_TYPES_CODE, projectIdentifier);
        final List<ExperimentPE> experiments = experimentTable.getExperiments();
        final Collection<MetaprojectAssignmentPE> assignmentPEs =
                getDAOFactory().getMetaprojectDAO().listMetaprojectAssignmentsForEntities(
                        session.tryGetPerson(), experiments, EntityKind.EXPERIMENT);
        Map<Long, Set<Metaproject>> assignments =
                MetaprojectTranslator.translateMetaprojectAssignments(assignmentPEs);
        Collections.sort(experiments);
        return ExperimentTranslator.translate(experiments, session.getBaseIndexURL(), assignments);
    }

    @Override
    @RolesAllowed(
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public IEntityProperty[] tryToGetPropertiesOfTopSampleRegisteredFor(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            final SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleIdentifier != null : "Unspecified sample identifier.";

        final Session session = getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadBySampleIdentifier(sampleIdentifier);
        SamplePE sample = sampleBO.getSample();
        if (sample == null)
        {
            return null;
        }
        SamplePE top = sample.getTop();
        if (top == null)
        {
            top = sample;
        }
        Set<SamplePropertyPE> properties = top.getProperties();
        HibernateUtils.initialize(properties);
        return EntityPropertyTranslator.translate(properties.toArray(new SamplePropertyPE[0]),
                new HashMap<PropertyTypePE, PropertyType>());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void registerEntities(String sessionToken, EntityCollectionForCreationOrUpdate collection)
            throws UserFailureException
    {
        checkSession(sessionToken);

        List<NewExperiment> experiments = collection.getNewExperiments();
        for (NewExperiment experiment : experiments)
        {
            registerExperiment(sessionToken, experiment);
        }

        List<NewExternalData> dataSets = collection.getNewDataSets();
        for (NewExternalData dataSet : dataSets)
        {
            ExperimentIdentifier experimentIdentifier = dataSet.getExperimentIdentifierOrNull();
            if (experimentIdentifier != null)
            {
                registerDataSet(sessionToken, experimentIdentifier, dataSet);
            }
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public long registerExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = NewExperimentPredicate.class)
            NewExperiment experiment) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert experiment != null : "Unspecified new example.";

        final Session session = getSession(sessionToken);
        return registerExperiment(session, experiment);
    }

    private long registerExperiment(final Session session, NewExperiment experiment)
    {
        IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.define(experiment);
        experimentBO.save();
        return experimentBO.getExperiment().getId();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void registerSamples(final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplesWithTypePredicate.class)
            final List<NewSamplesWithTypes> newSamplesWithType, String userIDOrNull)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSession(sessionToken);
        PersonPE registratorOrNull =
                userIDOrNull != null ? getOrCreatePerson(sessionToken, userIDOrNull) : null;
        for (NewSamplesWithTypes samples : newSamplesWithType)
        {
            registerSamples(session, samples, registratorOrNull);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public long registerSample(final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplePredicate.class)
            final NewSample newSample, String userIDOrNull) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert newSample != null : "Unspecified new sample.";

        final Session session = getSession(sessionToken);
        SamplePE samplePE = registerSampleInternal(session, newSample, userIDOrNull);
        return samplePE.getId();
    }

    private PersonPE getOrCreatePerson(String sessionToken, String userID)
    {
        PersonPE person = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userID);
        if (person != null)
        {
            return person;
        }
        List<PersonPE> persons = registerPersons(sessionToken, Collections.singletonList(userID));
        return persons.get(0);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void updateSample(String sessionToken,
            @AuthorizationGuard(guardClass = SampleUpdatesPredicate.class)
            SampleUpdatesDTO updates)
    {
        final Session session = getSession(sessionToken);
        updateSampleInternal(updates, session);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void registerDataSet(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            final SampleIdentifier sampleIdentifier, final NewExternalData externalData)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleIdentifier != null : "Unspecified sample identifier.";

        final Session session = getSession(sessionToken);
        registerDataSetInternal(session, sampleIdentifier, externalData);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void registerDataSet(final String sessionToken,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class)
            final ExperimentIdentifier experimentIdentifier, final NewExternalData externalData)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert experimentIdentifier != null : "Unspecified experiment identifier.";

        final Session session = getSession(sessionToken);
        registerDataSetInternal(session, experimentIdentifier, externalData);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void addPropertiesToDataSet(String sessionToken, List<NewProperty> properties,
            String dataSetCode, @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class)
            final SpaceIdentifier identifier) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSession(sessionToken);
        final IDataBO dataBO = businessObjectFactory.createDataBO(session);
        dataBO.addPropertiesToDataSet(dataSetCode, properties);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void updateShareIdAndSize(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class)
            String dataSetCode, String shareId, long size) throws UserFailureException
    {
        final Session session = getSession(sessionToken);

        IDataDAO dataSetDAO = getDAOFactory().getDataDAO();
        DataPE dataSet = dataSetDAO.tryToFindFullDataSetByCode(dataSetCode, false, false);
        if (dataSet == null)
        {
            throw new UserFailureException("Unknown data set: " + dataSetCode);
        }
        ExternalDataPE externalData = dataSet.tryAsExternalData();
        if (externalData == null)
        {
            throw new UserFailureException("Can't update share id and size of a virtual data set: "
                    + dataSetCode);
        }
        // data sets consisting out of empty folders have a size of 0,
        // but we want the size of a data set to be strictly positive
        long positiveSize = Math.max(1, size);
        externalData.setShareId(shareId);
        externalData.setSize(positiveSize);
        dataSetDAO.updateDataSet(dataSet, session.tryGetPerson());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void updateDataSetStatuses(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class)
            List<String> dataSetCodes, final DataSetArchivingStatus newStatus,
            boolean presentInArchive) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSession(sessionToken);
        final IDataBO dataBO = businessObjectFactory.createDataBO(session);
        dataBO.updateStatuses(dataSetCodes, newStatus, presentInArchive);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public boolean compareAndSetDataSetStatus(String sessionToken, String dataSetCode,
            DataSetArchivingStatus oldStatus, DataSetArchivingStatus newStatus,
            boolean newPresentInArchive) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSession(sessionToken);
        final IDataBO dataBO = businessObjectFactory.createDataBO(session);
        dataBO.loadByCode(dataSetCode);
        return dataBO.compareAndSetDataSetStatus(oldStatus, newStatus, newPresentInArchive);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public int archiveDatasets(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class)
            List<String> datasetCodes, boolean removeFromDataStore)
    {
        return super.archiveDatasets(sessionToken, datasetCodes, removeFromDataStore);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public int unarchiveDatasets(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class)
            List<String> datasetCodes)
    {
        return super.unarchiveDatasets(sessionToken, datasetCodes);
    }

    @Override
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public IDatasetLocationNode tryGetDataSetLocation(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class)
            String dataSetCode) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert dataSetCode != null : "Unspecified data set code.";

        Session session = getSession(sessionToken);
        IDatasetLister lister = businessObjectFactory.createDatasetLister(session);
        return lister.listLocationsByDatasetCode(dataSetCode);
    }

    @Override
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public ExternalData tryGetDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class)
            String dataSetCode) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert dataSetCode != null : "Unspecified data set code.";

        Session session = getSession(sessionToken); // assert authenticated

        IDataBO dataBO = businessObjectFactory.createDataBO(session);
        dataBO.loadByCode(dataSetCode);
        dataBO.enrichWithParentsAndExperiment();
        dataBO.enrichWithChildren();
        dataBO.enrichWithProperties();
        dataBO.enrichWithContainedDataSets();
        DataPE dataPE = dataBO.tryGetData();
        if (null == dataPE)
        {
            return null;
        }
        Collection<MetaprojectPE> metaprojects =
                getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                        session.tryGetPerson(), dataPE);
        return DataSetTranslator.translate(dataPE, session.getBaseIndexURL(),
                MetaprojectTranslator.translate(metaprojects));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void checkInstanceAdminAuthorization(String sessionToken) throws UserFailureException
    {
        checkSession(sessionToken);
        // do nothing, the access rights specified in method annotations are checked by a proxy
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_POWER_USER)
    public void checkSpacePowerUserAuthorization(String sessionToken) throws UserFailureException
    {
        checkSession(sessionToken);
        // do nothing, the access rights specified in method annotations are checked by a proxy
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public void checkDataSetAccess(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class)
            String dataSetCode) throws UserFailureException
    {
        checkSession(sessionToken);
        // do nothing, the access rights specified in method annotations are checked by a proxy
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public void checkDataSetCollectionAccess(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class)
            List<String> dataSetCodes)
    {
        checkSession(sessionToken);
        // do nothing, the access rights specified in method annotations are checked by a proxy
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public void checkSpaceAccess(String sessionToken,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class)
            SpaceIdentifier spaceId)
    {
        checkSession(sessionToken);
        // do nothing, the access rights specified in method annotations are checked by a proxy
    }

    @Override
    @RolesAllowed(
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Sample> listSamplesByCriteria(final String sessionToken,
            @AuthorizationGuard(guardClass = ListSamplesByPropertyPredicate.class)
            final ListSamplesByPropertyCriteria criteria) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert criteria != null : "Unspecified criteria.";

        Session session = getSession(sessionToken);
        ISampleTable sampleTable = businessObjectFactory.createSampleTable(session);
        sampleTable.loadSamplesByCriteria(criteria);
        List<SamplePE> samples = sampleTable.getSamples();

        final Collection<MetaprojectAssignmentPE> assignmentPEs =
                getDAOFactory().getMetaprojectDAO().listMetaprojectAssignmentsForEntities(
                        session.tryGetPerson(), samples, EntityKind.SAMPLE);
        Map<Long, Set<Metaproject>> assignments =
                MetaprojectTranslator.translateMetaprojectAssignments(assignmentPEs);

        return SampleTranslator.translate(samples, "", assignments);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<DataSetShareId> listShareIds(final String sessionToken, String dataStoreCode)
            throws UserFailureException
    {
        Session session = getSession(sessionToken);
        IDatasetLister datasetLister = businessObjectFactory.createDatasetLister(session);
        DataStorePE dataStore = loadDataStore(session, dataStoreCode);
        return datasetLister.listAllDataSetShareIdsByDataStore(dataStore.getId());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<SimpleDataSetInformationDTO> listFileDataSets(final String sessionToken,
            String dataStoreCode) throws UserFailureException
    {
        List<ExternalData> dataSets = loadDataSets(sessionToken, dataStoreCode);
        return SimpleDataSetHelper.filterAndTranslate(dataSets);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<ExternalData> listAvailableDataSets(String sessionToken, String dataStoreCode,
            ArchiverDataSetCriteria criteria)
    {
        Session session = getSession(sessionToken);
        final IDatasetLister datasetLister = createDatasetLister(session);
        return datasetLister.listByArchiverCriteria(dataStoreCode, criteria);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<ExternalData> listDataSets(String sessionToken, String dataStoreCode,
            TrackingDataSetCriteria criteria)
    {
        Session session = getSession(sessionToken);
        getDAOFactory().getHomeDatabaseInstance();
        DataStorePE dataStore =
                getDAOFactory().getDataStoreDAO().tryToFindDataStoreByCode(dataStoreCode);
        if (dataStore == null)
        {
            throw new UserFailureException("Unknown data store: " + dataStoreCode);
        }
        final IDatasetLister datasetLister = createDatasetLister(session);
        List<ExternalData> allDataSets = datasetLister.listByTrackingCriteria(criteria);
        List<ExternalData> result = new ArrayList<ExternalData>();
        for (ExternalData externalData : allDataSets)
        {
            if (dataStoreCode.equals(externalData.getDataStore().getCode()))
            {
                result.add(externalData);
            }
        }
        return result;
    }

    private List<ExternalData> loadDataSets(String sessionToken, String dataStoreCode)
    {
        Session session = getSession(sessionToken);
        DataStorePE dataStore = loadDataStore(session, dataStoreCode);
        IDatasetLister datasetLister = businessObjectFactory.createDatasetLister(session);
        return datasetLister.listByDataStore(dataStore.getId());
    }

    private DataStorePE loadDataStore(Session session, String dataStoreCode)
    {
        DataStorePE dataStore =
                getDAOFactory().getDataStoreDAO().tryToFindDataStoreByCode(dataStoreCode);
        if (dataStore == null)
        {
            throw new UserFailureException(String.format("Unknown data store '%s'", dataStoreCode));
        }
        return dataStore;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<DeletedDataSet> listDeletedDataSets(String sessionToken,
            Long lastSeenDeletionEventIdOrNull, Date maxDeletionDataOrNull)
    {
        checkSession(sessionToken);
        return getDAOFactory().getEventDAO().listDeletedDataSets(lastSeenDeletionEventIdOrNull,
                maxDeletionDataOrNull);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Collection<VocabularyTerm> listVocabularyTerms(String sessionToken, String vocabularyCode)
            throws UserFailureException
    {
        checkSession(sessionToken);
        VocabularyPE vocabularyOrNull =
                getDAOFactory().getVocabularyDAO().tryFindVocabularyByCode(vocabularyCode);
        if (vocabularyOrNull == null)
        {
            throw new UserFailureException(String.format("Vocabulary '%s' not found",
                    vocabularyCode));
        }
        return VocabularyTermTranslator.translateTerms(vocabularyOrNull.getTerms());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Vocabulary tryGetVocabulary(String sessionToken, String vocabularyCode)
    {
        checkSession(sessionToken);
        VocabularyPE vocabularyOrNull =
                getDAOFactory().getVocabularyDAO().tryFindVocabularyByCode(vocabularyCode);

        if (vocabularyOrNull == null)
        {
            return null;
        } else
        {
            return VocabularyTranslator.translate(vocabularyOrNull, true);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<String> generateCodes(String sessionToken, String prefix,
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind entityKind, int number)
    {
        checkSession(sessionToken);
        return new EntityCodeGenerator(daoFactory).generateCodes(prefix, entityKind, number);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<Person> listAdministrators(String sessionToken)
    {
        checkSession(sessionToken);
        // Get all Persons in the DB
        final List<PersonPE> persons = getDAOFactory().getPersonDAO().listPersons();

        // Filter down to the admins
        ArrayList<PersonPE> admins = new ArrayList<PersonPE>();
        for (PersonPE person : persons)
        {
            for (final RoleAssignmentPE roleAssigment : person.getRoleAssignments())
            {
                if (roleAssigment.getDatabaseInstance() != null
                        && roleAssigment.getRole().equals(RoleCode.ADMIN))
                {
                    admins.add(person);
                }
            }
        }
        Collections.sort(admins);
        return PersonTranslator.translate(admins);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Person tryPersonWithUserIdOrEmail(String sessionToken, String useridOrEmail)
    {
        checkSession(sessionToken);

        PersonPE personPE = tryFindPersonForUserIdOrEmail(useridOrEmail);
        return (null != personPE) ? PersonTranslator.translate(personPE) : null;
    }

    private PersonPE tryFindPersonForUserIdOrEmail(String userIdOrEmail)
    {
        if (userIdOrEmail == null)
        {
            return null;
        }

        // First search for a userId match
        IPersonDAO personDao = getDAOFactory().getPersonDAO();
        PersonPE person = personDao.tryFindPersonByUserId(userIdOrEmail);
        if (null != person)
        {
            return person;
        }
        // Didn't find one -- try email
        return personDao.tryFindPersonByEmail(userIdOrEmail);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Sample registerSampleAndDataSet(final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplePredicate.class)
            final NewSample newSample, final NewExternalData externalData, String userIdOrNull)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert newSample != null : "Unspecified new sample.";

        // Register the Sample
        final Session session = getSession(sessionToken);
        SamplePE samplePE = registerSampleInternal(session, newSample, userIdOrNull);

        // Register the data set
        registerDataSetInternal(sessionToken, externalData, samplePE);
        Sample result = SampleTranslator.translate(samplePE, session.getBaseIndexURL(), null);
        return result;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Sample updateSampleAndRegisterDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = SampleUpdatesPredicate.class)
            SampleUpdatesDTO updates, NewExternalData externalData)
    {
        final Session session = getSession(sessionToken);

        // Update the sample
        final ISampleBO sampleBO = updateSampleInternal(updates, session);

        // Register the data set
        final SamplePE samplePE = sampleBO.getSample();
        registerDataSetInternal(sessionToken, externalData, samplePE);

        Collection<MetaprojectPE> metaprojectPEs =
                getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                        session.tryGetPerson(), samplePE);

        Sample result =
                SampleTranslator.translate(samplePE, session.getBaseIndexURL(),
                        MetaprojectTranslator.translate(metaprojectPEs));
        return result;
    }

    private ISampleBO updateSampleInternal(SampleUpdatesDTO updates, final Session session)
    {
        // TODO 2010-12-21, CR: Refactor this into an object, SampleUpdater
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.update(updates);
        sampleBO.save();

        return sampleBO;
    }

    private void registerDataSetInternal(String sessionToken, NewExternalData externalData,
            SamplePE samplePE)
    {
        final Session session = getSession(sessionToken);
        SampleIdentifier sampleIdentifier = samplePE.getSampleIdentifier();
        assert sampleIdentifier != null : "Unspecified sample identifier.";

        ExperimentPE experiment = samplePE.getExperiment();
        if (experiment == null)
        {
            throw new UserFailureException("No experiment found for sample " + sampleIdentifier);
        }
        if (experiment.getDeletion() != null)
        {
            throw new UserFailureException("Data set can not be registered because experiment '"
                    + experiment.getIdentifier() + "' is in trash.");
        }

        final IDataBO dataBO = businessObjectFactory.createDataBO(session);
        SourceType sourceType =
                externalData.isMeasured() ? SourceType.MEASUREMENT : SourceType.DERIVED;
        dataBO.define(externalData, samplePE, sourceType);
        dataBO.save();
        final String dataSetCode = dataBO.getData().getCode();
        assert dataSetCode != null : "Data set code not specified.";
    }

    private SamplePE registerSampleInternal(Session session, NewSample newSample,
            String userIdOrNull)
    {
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.define(newSample);
        if (userIdOrNull != null)
        {
            sampleBO.getSample().setRegistrator(
                    getOrCreatePerson(session.getSessionToken(), userIdOrNull));
        }
        sampleBO.save();
        SamplePE samplePE = sampleBO.getSample();
        return samplePE;
    }

    @Override
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_ETL_SERVER })
    public Space tryGetSpace(String sessionToken,
            @AuthorizationGuard(guardClass = ExistingSpaceIdentifierPredicate.class)
            SpaceIdentifier spaceIdentifier)
    {

        Session session = getSession(sessionToken);
        ISpaceBO spaceBO = businessObjectFactory.createSpaceBO(session);
        SpaceIdentifier identifier =
                new SpaceIdentifier(spaceIdentifier.getDatabaseInstanceCode(),
                        spaceIdentifier.getSpaceCode());
        try
        {
            spaceBO.load(identifier);
            return SpaceTranslator.translate(spaceBO.getSpace());
        } catch (UserFailureException ufe)
        {
            // space does not exist
            return null;
        }
    }

    @Override
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_ETL_SERVER })
    public Project tryGetProject(String sessionToken,
            @AuthorizationGuard(guardClass = ExistingSpaceIdentifierPredicate.class)
            ProjectIdentifier projectIdentifier)
    {
        final Session session = getSession(sessionToken);
        final IProjectBO bo = businessObjectFactory.createProjectBO(session);
        try
        {
            bo.loadByProjectIdentifier(projectIdentifier);
            final ProjectPE project = bo.getProject();
            return ProjectTranslator.translate(project);
        } catch (UserFailureException ufe)
        {
            // project does not exist
            return null;
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Material tryGetMaterial(String sessionToken, MaterialIdentifier materialIdentifier)
    {
        final Session session = getSession(sessionToken);
        final IMaterialBO bo = businessObjectFactory.createMaterialBO(session);
        try
        {
            bo.loadByMaterialIdentifier(materialIdentifier);
            bo.enrichWithProperties();
            MaterialPE materialPE = bo.getMaterial();
            Collection<MetaprojectPE> metaprojectPEs = Collections.emptySet();
            if (materialPE != null)
            {
                metaprojectPEs =
                        getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                                session.tryGetPerson(), materialPE);
            }
            return MaterialTranslator.translate(materialPE,
                    MetaprojectTranslator.translate(metaprojectPEs));
        } catch (UserFailureException ufe)
        {
            // material does not exist
            return null;
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Metaproject tryGetMetaproject(String sessionToken, String name, String ownerId)
    {
        final Session session = getSession(sessionToken);
        final IMetaprojectBO bo = businessObjectFactory.createMetaprojectBO(session);

        bo.loadByMetaprojectId(new MetaprojectIdentifierId(ownerId, name));

        MetaprojectPE pe = bo.getMetaproject();
        return MetaprojectTranslator.translate(pe);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public AtomicEntityOperationResult performEntityOperations(String sessionToken,
            @AuthorizationGuard(guardClass = AtomicOperationsPredicate.class)
            AtomicEntityOperationDetails operationDetails)
    {
        IServiceConversationProgressListener progressListener =
                ServiceConversationsThreadContext.getProgressListener();

        TechId registrationId = operationDetails.getRegistrationIdOrNull();

        EntityOperationsInProgress.getInstance().addRegistrationPending(registrationId);

        String sessionTokenForEntityOperation = null;
        try
        {
            final Session session = getSession(sessionToken);
            final String userId = operationDetails.tryUserIdOrNull();
            boolean authorize = (userId != null);
            Session sessionForEntityOperation = session;
            if (authorize)
            {
                sessionTokenForEntityOperation =
                        sessionManagerForEntityOperation.tryToOpenSession(userId, "dummy password");
                sessionForEntityOperation =
                        sessionManagerForEntityOperation.getSession(sessionTokenForEntityOperation);
                injectPerson(sessionForEntityOperation, userId);
            }

            long spacesCreated =
                    createSpaces(sessionForEntityOperation, operationDetails, progressListener,
                            authorize);

            long materialsCreated =
                    createMaterials(sessionForEntityOperation, operationDetails, progressListener,
                            authorize);

            long projectsCreated =
                    createProjects(sessionForEntityOperation, operationDetails, progressListener,
                            authorize);

            long experimentsCreated =
                    createExperiments(sessionForEntityOperation, operationDetails,
                            progressListener, authorize);

            long experimentsUpdates =
                    updateExperiments(sessionForEntityOperation, operationDetails,
                            progressListener, authorize);

            long samplesCreated =
                    createSamples(sessionForEntityOperation, operationDetails, progressListener,
                            authorize);

            long samplesUpdated =
                    updateSamples(sessionForEntityOperation, operationDetails, progressListener,
                            authorize);

            long dataSetsCreated =
                    createDataSets(sessionForEntityOperation, operationDetails, progressListener,
                            authorize);

            long dataSetsUpdated =
                    updateDataSets(sessionForEntityOperation, operationDetails, progressListener,
                            authorize);

            long materialsUpdates =
                    updateMaterials(sessionForEntityOperation, operationDetails, progressListener,
                            authorize);

            long metaprojectsCreated =
                    createMetaprojects(sessionForEntityOperation, operationDetails,
                            progressListener, authorize);

            long metaprojectsUpdates =
                    updateMetaprojects(sessionForEntityOperation, operationDetails,
                            progressListener, authorize);

            // If the id is not null, the caller wants to persist the fact that the operation was
            // invoked and completed;
            // if the id is null, the caller does not care.
            if (null != registrationId)
            {
                daoFactory.getEntityOperationsLogDAO().addLogEntry(registrationId.getId());
            }

            return new AtomicEntityOperationResult(spacesCreated, projectsCreated,
                    materialsCreated, materialsUpdates, experimentsCreated, experimentsUpdates,
                    samplesCreated, samplesUpdated, dataSetsCreated, dataSetsUpdated,
                    metaprojectsCreated, metaprojectsUpdates);
        } finally
        {
            EntityOperationsInProgress.getInstance().removeRegistrationPending(registrationId);
            if (sessionTokenForEntityOperation != null)
            {
                sessionManagerForEntityOperation.closeSession(sessionTokenForEntityOperation);
            }
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public EntityOperationsState didEntityOperationsSucceed(String token, TechId registrationId)
    {
        if (registrationId == null)
        {
            return EntityOperationsState.NO_OPERATION;
        }

        if (EntityOperationsInProgress.getInstance().isRegistrationPending(registrationId))
        {
            return EntityOperationsState.IN_PROGRESS;
        }

        EntityOperationsLogEntryPE logEntry =
                daoFactory.getEntityOperationsLogDAO().tryFindLogEntry(registrationId.getId());

        if (logEntry != null)
        {
            return EntityOperationsState.OPERATION_SUCCEEDED;
        } else
        {
            return EntityOperationsState.NO_OPERATION;
        }
    }

    private long createSpaces(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        ArrayList<SpacePE> spacePEsCreated = new ArrayList<SpacePE>();
        List<NewSpace> newSpaces = operationDetails.getSpaceRegistrations();
        if (authorize)
        {
            checkSpaceCreationAllowed(session, newSpaces);
        }

        int index = 0;
        for (NewSpace newSpace : newSpaces)
        {
            SpacePE spacePE =
                    registerSpaceInternal(session, newSpace, operationDetails.tryUserIdOrNull());
            spacePEsCreated.add(spacePE);
            progress.update("createSpaces", newSpaces.size(), ++index);
        }
        return index;
    }

    protected void checkSpaceCreationAllowed(Session session, List<NewSpace> newSpaces)
    {
        if (newSpaces != null && newSpaces.isEmpty() == false)
        {
            entityOperationChecker.assertSpaceCreationAllowed(session, newSpaces);
        }
    }

    private long createMaterials(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        MaterialHelper materialHelper =
                new MaterialHelper(session, businessObjectFactory, getDAOFactory(),
                        getPropertiesBatchManager());
        Map<String, List<NewMaterial>> materialRegs = operationDetails.getMaterialRegistrations();
        if (authorize)
        {
            checkMaterialCreationAllowed(session, materialRegs);
        }
        int index = 0;
        for (Entry<String, List<NewMaterial>> newMaterialsEntry : materialRegs.entrySet())
        {
            String materialType = newMaterialsEntry.getKey();
            List<NewMaterial> newMaterials = newMaterialsEntry.getValue();
            materialHelper.registerMaterials(materialType, newMaterials);
            progress.update("createMaterials", materialRegs.size(), ++index);
        }
        return index;
    }

    private long updateMaterials(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        MaterialHelper materialHelper =
                new MaterialHelper(session, businessObjectFactory, getDAOFactory(),
                        getPropertiesBatchManager());

        List<MaterialUpdateDTO> allMaterialUpdates = operationDetails.getMaterialUpdates();

        if (authorize)
        {
            checkMaterialUpdateAllowed(session, allMaterialUpdates);
        }

        materialHelper.updateMaterials(allMaterialUpdates);

        // in material helper call the update of materials - but this has to wait fo change of the
        // material updates to a map
        return allMaterialUpdates.size();
    }

    protected void checkMaterialCreationAllowed(Session session,
            Map<String, List<NewMaterial>> materials)
    {
        if (materials != null && materials.isEmpty() == false)
        {
            entityOperationChecker.assertMaterialCreationAllowed(session, materials);
        }
    }

    protected void checkMaterialUpdateAllowed(Session session,
            List<MaterialUpdateDTO> materialUpdates)
    {
        if (materialUpdates != null && materialUpdates.isEmpty() == false)
        {
            entityOperationChecker.assertMaterialUpdateAllowed(session, materialUpdates);
        }
    }

    private long updateMetaprojects(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        List<MetaprojectUpdatesDTO> updates = operationDetails.getMetaprojectUpdates();

        for (MetaprojectUpdatesDTO update : updates)
        {
            updateMetaprojects(session, update);
        }

        return updates.size();
    }

    private void updateMetaprojects(Session session, MetaprojectUpdatesDTO update)
    {
        IMetaprojectBO metaprojectBO = businessObjectFactory.createMetaprojectBO(session);
        metaprojectBO.loadDataByTechId(update.getMetaprojectId());
        metaprojectBO.setDescription(update.getDescription());

        metaprojectBO.addSamples(update.getAddedSamples());
        metaprojectBO.removeSamples(update.getRemovedSamples());
        metaprojectBO.addDataSets(update.getAddedDataSets());
        metaprojectBO.removeDataSets(update.getRemovedDataSets());
        metaprojectBO.addExperiments(update.getAddedExperiments());
        metaprojectBO.removeExperiments(update.getRemovedExperiments());
        metaprojectBO.addMaterials(update.getAddedMaterials());
        metaprojectBO.removeMaterials(update.getRemovedMaterials());

        metaprojectBO.save();
    }

    private long createMetaprojects(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        final List<NewMetaproject> metaprojectRegistrations =
                operationDetails.getMetaprojectRegistrations();
        int index = 0;
        for (NewMetaproject metaproject : metaprojectRegistrations)
        {
            registerMetaproject(session, metaproject);
            progress.update("createMetaProjects", metaprojectRegistrations.size(), ++index);
        }
        return index;
    }

    private MetaprojectPE registerMetaproject(final Session session, NewMetaproject metaproject)
    {
        IMetaprojectBO metaprojectBO = businessObjectFactory.createMetaprojectBO(session);
        metaprojectBO.define(metaproject);
        metaprojectBO.addSamples(metaproject.getSamples());
        metaprojectBO.addExperiments(metaproject.getExperiments());
        metaprojectBO.addMaterials(metaproject.getMaterials());
        metaprojectBO.addDataSets(metaproject.getDatasets());
        metaprojectBO.save();
        return metaprojectBO.getMetaproject();
    }

    private SpacePE registerSpaceInternal(Session session, NewSpace newSpace,
            String registratorUserIdOrNull)
    {
        // create space
        ISpaceBO groupBO = businessObjectFactory.createSpaceBO(session);
        groupBO.define(newSpace.getCode(), newSpace.getDescription());
        if (registratorUserIdOrNull != null)
        {
            groupBO.getSpace().setRegistrator(
                    getOrCreatePerson(session.getSessionToken(), registratorUserIdOrNull));
        }
        groupBO.save();

        // create ADMIN role assignemnt
        SpacePE space = groupBO.getSpace();
        if (newSpace.getSpaceAdminUserId() != null)
        {
            IRoleAssignmentTable roleTable =
                    businessObjectFactory.createRoleAssignmentTable(session);
            NewRoleAssignment assignment = new NewRoleAssignment();
            SpaceIdentifier spaceIdentifier = new SpaceIdentifier(space.getCode());
            assignment.setSpaceIdentifier(spaceIdentifier);
            assignment.setRole(RoleCode.ADMIN);
            Grantee grantee = Grantee.createPerson(newSpace.getSpaceAdminUserId());
            assignment.setGrantee(grantee);
            roleTable.add(assignment);
            roleTable.save();
        }
        return space;

    }

    private long createProjects(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        ArrayList<ProjectPE> projectPEsCreated = new ArrayList<ProjectPE>();
        List<NewProject> newProjects = operationDetails.getProjectRegistrations();
        if (authorize)
        {
            checkProjectCreationAllowed(session, newProjects);
        }
        int index = 0;
        for (NewProject newProject : newProjects)
        {
            ProjectPE projectPE =
                    registerProjectInternal(session, newProject, operationDetails.tryUserIdOrNull());
            projectPEsCreated.add(projectPE);
            progress.update("createProjects", newProjects.size(), ++index);
        }
        return index;
    }

    protected void checkProjectCreationAllowed(Session session, List<NewProject> newProjects)
    {
        if (newProjects != null && newProjects.isEmpty() == false)
        {
            entityOperationChecker.assertProjectCreationAllowed(session, newProjects);
        }
    }

    private ProjectPE registerProjectInternal(Session session, NewProject newProject,
            String registratorUserIdOrNull)
    {
        IProjectBO projectBO = businessObjectFactory.createProjectBO(session);
        ProjectIdentifier identifier =
                new ProjectIdentifierFactory(newProject.getIdentifier()).createIdentifier();
        projectBO.define(identifier, newProject.getDescription(), null);
        if (registratorUserIdOrNull != null)
        {
            projectBO.getProject().setRegistrator(
                    getOrCreatePerson(session.getSessionToken(), registratorUserIdOrNull));
        }
        projectBO.save();

        return projectBO.getProject();
    }

    private long createSamples(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        List<NewSample> newSamples = operationDetails.getSampleRegistrations();

        if (authorize)
        {
            authorizeSampleCreation(session, newSamples);
        }
        String userIdOrNull = operationDetails.tryUserIdOrNull();
        PersonPE registratorOrNull = tryFindPersonForUserIdOrEmail(userIdOrNull);
        final ISampleTable sampleTable = businessObjectFactory.createSampleTable(session);

        List<List<NewSample>> sampleGroups = splitIntoDependencyGroups(newSamples);

        for (List<NewSample> groupOfSamples : sampleGroups)
        {
            BatchOperationExecutor.executeInBatches(new SampleBatchRegistration(sampleTable,
                    groupOfSamples, registratorOrNull), getBatchSize(operationDetails), progress,
                    "createContainerSamples");
        }
        return newSamples.size();
    }

    /**
     * Splits the samples using the grouping dag into groups, that can be executed in batches one
     * after another, that samples in later batches depend only on the samples from earlier batches
     */
    private List<List<NewSample>> splitIntoDependencyGroups(List<NewSample> newSamples)
    {
        return SampleGroupingDAG.groupByDepencies(newSamples);
    }

    private void authorizeSampleCreation(Session session, List<NewSample> newSamples)
    {
        List<NewSample> instanceSamples = new ArrayList<NewSample>();
        List<NewSample> spaceSamples = new ArrayList<NewSample>();

        for (NewSample newSample : newSamples)
        {
            SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(newSample);
            if (sampleIdentifier.isDatabaseInstanceLevel())
            {
                instanceSamples.add(newSample);
            } else
            {
                spaceSamples.add(newSample);
            }
        }

        checkInstanceSampleCreationAllowed(session, instanceSamples);
        checkSpaceSampleCreationAllowed(session, spaceSamples);
    }

    private void checkInstanceSampleCreationAllowed(Session session, List<NewSample> instanceSamples)
    {
        if (instanceSamples.isEmpty() == false)
        {
            entityOperationChecker.assertInstanceSampleCreationAllowed(session, instanceSamples);
        }
    }

    private void checkSpaceSampleCreationAllowed(Session session, List<NewSample> spaceSamples)
    {
        if (spaceSamples.isEmpty() == false)
        {
            entityOperationChecker.assertSpaceSampleCreationAllowed(session, spaceSamples);
        }
    }

    private long updateSamples(final Session session,
            AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        List<SampleUpdatesDTO> sampleUpdates = operationDetails.getSampleUpdates();
        int sampleUpdateCount = sampleUpdates.size();
        if (sampleUpdateCount < 1)
        {
            return 0;
        }
        progress.update("authorizingSampleUpdates", sampleUpdateCount, 0);
        if (authorize)
        {
            checkSampleUpdatesAllowed(session, sampleUpdates);
        }
        progress.update("authorizingSampleUpdates", sampleUpdateCount, sampleUpdateCount);
        final ISampleTable sampleTable = businessObjectFactory.createSampleTable(session);

        BatchOperationExecutor.executeInBatches(new SampleCheckBeforeUpdate(sampleTable,
                sampleUpdates), getBatchSize(operationDetails), progress,
                "checkSamplesBeforeUpdate");

        BatchOperationExecutor.executeInBatches(new SampleUpdate(sampleTable, sampleUpdates),
                getBatchSize(operationDetails), progress, "updateSamples");

        return sampleUpdateCount;
    }

    private void checkSampleUpdatesAllowed(final Session session,
            List<SampleUpdatesDTO> sampleUpdates)
    {
        List<SampleUpdatesDTO> instanceSamples = new ArrayList<SampleUpdatesDTO>();
        List<SampleUpdatesDTO> spaceSamples = new ArrayList<SampleUpdatesDTO>();
        for (SampleUpdatesDTO sampleUpdate : sampleUpdates)
        {
            SampleIdentifier sampleIdentifier = sampleUpdate.getSampleIdentifier();
            if (sampleIdentifier.isDatabaseInstanceLevel())
            {
                instanceSamples.add(sampleUpdate);
            } else
            {
                spaceSamples.add(sampleUpdate);
            }
        }
        checkInstanceSampleUpdateAllowed(session, instanceSamples);
        checkSpaceSampleUpdateAllowed(session, spaceSamples);
    }

    private void checkInstanceSampleUpdateAllowed(Session session,
            List<SampleUpdatesDTO> instanceSamples)
    {
        if (instanceSamples.isEmpty() == false)
        {
            entityOperationChecker.assertInstanceSampleUpdateAllowed(session, instanceSamples);
        }
    }

    private void checkSpaceSampleUpdateAllowed(Session session, List<SampleUpdatesDTO> spaceSamples)
    {
        if (spaceSamples.isEmpty() == false)
        {
            entityOperationChecker.assertSpaceSampleUpdateAllowed(session, spaceSamples);
        }
    }

    /**
     * This method topologically sorts the data sets to be created and creates them in the necessary
     * order
     */
    private long createDataSets(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        ArrayList<DataPE> dataSetsCreated = new ArrayList<DataPE>();
        @SuppressWarnings("unchecked")
        List<NewExternalData> dataSetRegistrations =
                (List<NewExternalData>) operationDetails.getDataSetRegistrations();
        if (authorize)
        {
            checkDataSetCreationAllowed(session, dataSetRegistrations);
        }
        List<List<NewExternalData>> orderedRegistrations =
                NewExternalDataDAG.groupByDepencies(dataSetRegistrations);

        int total = 0;
        for (List<NewExternalData> dependencyLevel : orderedRegistrations)
        {
            total += dependencyLevel.size();
        }
        int index = 0;
        for (List<NewExternalData> dependencyLevel : orderedRegistrations)
        {
            for (NewExternalData dataSet : dependencyLevel)
            {
                registerDatasetInternal(session, dataSetsCreated, dataSet);
                progress.update("createDataSets", total, ++index);
            }
        }
        return index;
    }

    private void checkDataSetCreationAllowed(Session session,
            List<? extends NewExternalData> dataSets)
    {
        if (dataSets != null && dataSets.isEmpty() == false)
        {
            entityOperationChecker.assertDataSetCreationAllowed(session, dataSets);
        }
    }

    private long updateDataSets(final Session session,
            AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        final List<DataSetBatchUpdatesDTO> dataSetUpdates = operationDetails.getDataSetUpdates();
        final int dataSetUpdatesCount = dataSetUpdates.size();
        if (dataSetUpdatesCount < 1)
        {
            return 0;
        }

        progress.update("authorizingDataSetUpdates", dataSetUpdatesCount, 0);
        if (authorize)
        {
            checkDataSetUpdateAllowed(session, dataSetUpdates);
        }
        progress.update("authorizingDataSetUpdates", dataSetUpdatesCount, dataSetUpdatesCount);
        final IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);

        BatchOperationExecutor.executeInBatches(new DataSetCheckBeforeBatchUpdate(dataSetTable,
                dataSetUpdates), getBatchSize(operationDetails), progress,
                "checkDataSetsBeforeUpdate");

        BatchOperationExecutor.executeInBatches(
                new DataSetBatchUpdate(dataSetTable, dataSetUpdates),
                getBatchSize(operationDetails), progress, "updateDataSets");

        return dataSetUpdatesCount;
    }

    private void checkDataSetUpdateAllowed(Session session, List<DataSetBatchUpdatesDTO> dataSets)
    {
        if (dataSets != null && dataSets.isEmpty() == false)
        {
            entityOperationChecker.assertDataSetUpdateAllowed(session, dataSets);
        }
    }

    private void registerDatasetInternal(final Session session, ArrayList<DataPE> dataSetsCreated,
            NewExternalData dataSet)
    {
        SampleIdentifier sampleIdentifier = dataSet.getSampleIdentifierOrNull();
        IDataBO dataBO;
        if (sampleIdentifier != null)
        {
            dataBO = registerDataSetInternal(session, sampleIdentifier, dataSet);
        } else
        {
            ExperimentIdentifier experimentIdentifier = dataSet.getExperimentIdentifierOrNull();
            dataBO = registerDataSetInternal(session, experimentIdentifier, dataSet);
        }
        dataSetsCreated.add(dataBO.getData());
    }

    private long createExperiments(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        final List<NewExperiment> experimentRegistrations =
                operationDetails.getExperimentRegistrations();
        if (authorize)
        {
            entityOperationChecker
                    .assertExperimentCreationAllowed(session, experimentRegistrations);
        }
        int index = 0;
        for (NewExperiment experiment : experimentRegistrations)
        {
            registerExperiment(session, experiment);
            progress.update("createExperiments", experimentRegistrations.size(), ++index);
        }
        return index;
    }

    private void updateExperiment(Session session, ExperimentUpdatesDTO updates)
    {
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.update(updates);
        experimentBO.save();
    }

    private long updateExperiments(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        List<ExperimentUpdatesDTO> updates = operationDetails.getExperimentUpdates();

        for (ExperimentUpdatesDTO update : updates)
        {
            if (authorize)
            {
                entityOperationChecker.assertExperimentUpdateAllowed(session, update);
            }
            updateExperiment(session, update);
        }

        return updates.size();
    }

    private IDataBO registerDataSetInternal(final Session session,
            SampleIdentifier sampleIdentifier, NewExternalData externalData)
    {
        ExperimentPE experiment = tryLoadExperimentBySampleIdentifier(session, sampleIdentifier);
        if (experiment == null)
        {
            throw new UserFailureException("No experiment found for sample " + sampleIdentifier);
        }
        if (experiment.getDeletion() != null)
        {
            throw new UserFailureException("Data set can not be registered because experiment '"
                    + experiment.getIdentifier() + "' is in trash.");
        }
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadBySampleIdentifier(sampleIdentifier);
        final SamplePE sample = sampleBO.getSample();
        final IDataBO dataBO = businessObjectFactory.createDataBO(session);
        SourceType sourceType =
                externalData.isMeasured() ? SourceType.MEASUREMENT : SourceType.DERIVED;
        dataBO.define(externalData, sample, sourceType);
        dataBO.save();

        boolean isContainer = externalData instanceof NewContainerDataSet;
        if (isContainer)
        {
            dataBO.setContainedDataSets(experiment, (NewContainerDataSet) externalData);
        }

        final String dataSetCode = dataBO.getData().getCode();
        assert dataSetCode != null : "Data set code not specified.";

        return dataBO;
    }

    private IDataBO registerDataSetInternal(final Session session,
            ExperimentIdentifier experimentIdentifier, NewExternalData externalData)
    {
        ExperimentPE experiment = tryToLoadExperimentByIdentifier(session, experimentIdentifier);
        if (experiment.getDeletion() != null)
        {
            throw new UserFailureException("Data set can not be registered because experiment '"
                    + experiment.getIdentifier() + "' is in trash.");
        }
        final IDataBO externalDataBO = businessObjectFactory.createDataBO(session);
        SourceType sourceType =
                externalData.isMeasured() ? SourceType.MEASUREMENT : SourceType.DERIVED;
        externalDataBO.define(externalData, experiment, sourceType);
        externalDataBO.save();

        boolean isContainer = externalData instanceof NewContainerDataSet;
        if (isContainer)
        {
            externalDataBO.setContainedDataSets(experiment, (NewContainerDataSet) externalData);
        }

        final String dataSetCode = externalDataBO.getData().getCode();
        assert dataSetCode != null : "Data set code not specified.";

        return externalDataBO;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<Sample> searchForSamples(String sessionToken, SearchCriteria searchCriteria)
    {
        Session session = getSession(sessionToken);
        DetailedSearchCriteria detailedSearchCriteria =
                SearchCriteriaToDetailedSearchCriteriaTranslator.convert(
                        SearchableEntityKind.SAMPLE, searchCriteria);
        SearchHelper searchHelper =
                new SearchHelper(session, businessObjectFactory, getDAOFactory());
        return searchHelper.searchForSamples(session.getUserName(), detailedSearchCriteria);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<ExternalData> searchForDataSets(String sessionToken, SearchCriteria searchCriteria)
    {
        Session session = getSession(sessionToken);
        DetailedSearchCriteria detailedSearchCriteria =
                SearchCriteriaToDetailedSearchCriteriaTranslator.convert(
                        SearchableEntityKind.DATA_SET, searchCriteria);
        SearchHelper searchHelper =
                new SearchHelper(session, businessObjectFactory, getDAOFactory());
        return searchHelper.searchForDataSets(session.getUserName(), detailedSearchCriteria);
    }

    @Override
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Material> listMaterials(String sessionToken, ListMaterialCriteria criteria,
            boolean withProperties)
    {
        Session session = getSession(sessionToken);
        IMaterialLister lister = businessObjectFactory.createMaterialLister(session);
        ListMaterialCriteria criteriaWithIds = populateMissingTypeId(criteria);
        return lister.list(criteriaWithIds, withProperties);
    }

    private ListMaterialCriteria populateMissingTypeId(ListMaterialCriteria criteria)
    {
        MaterialType materialTypeOrNull = criteria.tryGetMaterialType();
        if (materialTypeOrNull != null && materialTypeOrNull.getId() == null)
        {
            String materialTypeCode = materialTypeOrNull.getCode();
            EntityTypePE typeWithId =
                    daoFactory.getEntityTypeDAO(EntityKind.MATERIAL).tryToFindEntityTypeByCode(
                            materialTypeCode);
            if (typeWithId == null)
            {
                throw UserFailureException.fromTemplate("Invalid material type '%s'",
                        materialTypeCode);
            } else
            {
                MaterialType materialTypeWithId = new MaterialType();
                materialTypeWithId.setId(typeWithId.getId());
                materialTypeWithId.setCode(materialTypeCode);
                return ListMaterialCriteria.createFromMaterialType(materialTypeWithId);
            }
        }

        return criteria;

    }

    @Override
    @SuppressWarnings("deprecation")
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void removeDataSetsPermanently(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class)
            List<String> dataSetCodes, String reason)
    {
        Session session = getSession(sessionToken);
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        permanentlyDeleteDataSets(session, dataSetTable, dataSetCodes, reason, false);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void updateDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetUpdatesPredicate.class)
            DataSetUpdatesDTO dataSetUpdates)
    {
        final Session session = getSession(sessionToken);
        final IDataBO dataSetBO = businessObjectFactory.createDataBO(session);
        dataSetBO.update(dataSetUpdates);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<String> getTrustedCrossOriginDomains(String sessionToken)
    {
        return trustedOriginDomainProvider.getTrustedDomains();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void setStorageConfirmed(String sessionToken, String dataSetCode)
    {
        assert sessionToken != null : "Unspecified session token.";

        final Session session = getSession(sessionToken);

        final IDataBO dataBO = businessObjectFactory.createDataBO(session);

        dataBO.loadByCode(dataSetCode);

        if (false == dataBO.isStorageConfirmed())
        {
            dataBO.setStorageConfirmed();
            daoFactory.getPostRegistrationDAO().addDataSet(dataBO.getData());
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void markSuccessfulPostRegistration(String sessionToken, String dataSetCode)
    {
        assert sessionToken != null : "Unspecified session token.";

        final Session session = getSession(sessionToken);

        final IDataBO dataBO = businessObjectFactory.createDataBO(session);
        dataBO.loadByCode(dataSetCode);
        DataPE data = dataBO.getData();

        if (data != null)
        {
            daoFactory.getPostRegistrationDAO().removeDataSet(data);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<ExternalData> listDataSetsForPostRegistration(String sessionToken,
            String dataStoreCode)
    {
        Session session = getSession(sessionToken);

        // find all datasets for registration
        final IDatasetLister datasetLister = createDatasetLister(session);
        Collection<Long> allDataSetIds =
                daoFactory.getPostRegistrationDAO().listDataSetsForPostRegistration();
        List<ExternalData> allDataSets = datasetLister.listByDatasetIds(allDataSetIds);

        // find datastore
        getDAOFactory().getHomeDatabaseInstance();
        DataStorePE dataStore =
                getDAOFactory().getDataStoreDAO().tryToFindDataStoreByCode(dataStoreCode);
        if (dataStore == null)
        {
            throw new UserFailureException("Unknown data store: " + dataStoreCode);
        }

        // filter datasets by datastore
        List<ExternalData> result = new ArrayList<ExternalData>();
        for (ExternalData externalData : allDataSets)
        {
            if (dataStoreCode.equals(externalData.getDataStore().getCode()))
            {
                result.add(externalData);
            }
        }
        return result;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void heartbeat(String token)
    {
        // do nothing
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public boolean doesUserHaveRole(String token, String user, String roleCode, String spaceOrNull)
    {
        return new AuthorizationServiceUtils(daoFactory, user).doesUserHaveRole(roleCode,
                spaceOrNull);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<String> filterToVisibleDataSets(String token, String user, List<String> dataSetCodes)
    {
        return new AuthorizationServiceUtils(daoFactory, user).filterDataSetCodes(dataSetCodes);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<String> filterToVisibleExperiments(String token, String user,
            List<String> experimentIds)
    {
        return new AuthorizationServiceUtils(daoFactory, user).filterExperimentIds(experimentIds);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<String> filterToVisibleSamples(String token, String user, List<String> sampleIds)
    {
        return new AuthorizationServiceUtils(daoFactory, user).filterSampleIds(sampleIds);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public ExternalDataManagementSystem tryGetExternalDataManagementSystem(String token,
            String externalDataManagementSystemCode)
    {
        checkSession(token);

        ExternalDataManagementSystemPE externalSystem =
                getDAOFactory().getExternalDataManagementSystemDAO()
                        .tryToFindExternalDataManagementSystemByCode(
                                externalDataManagementSystemCode);

        if (externalSystem != null)
        {
            return ExternalDataManagementSystemTranslator.translate(externalSystem);
        } else
        {
            return null;
        }
    }

    private int getBatchSize(AtomicEntityOperationDetails details)
    {
        return details == null || details.getBatchSizeOrNull() == null ? BatchOperationExecutor
                .getDefaultBatchSize() : details.getBatchSizeOrNull();
    }

    public void setConversationClient(IServiceConversationClientManagerLocal conversationClient)
    {
        this.conversationClient = conversationClient;
    }

    public void setConversationServer(IServiceConversationServerManagerLocal conversationServer)
    {
        this.conversationServer = conversationServer;
    }

}
