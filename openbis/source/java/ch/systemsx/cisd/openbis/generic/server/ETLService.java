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
import java.util.Set;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IProjectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IRoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataSetTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyTypeWithVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetShareId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatastoreServiceDescriptions;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityCollectionForCreationOrUpdate;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
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
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DatabaseInstanceTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.EntityPropertyTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator.LoadableFields;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.GroupTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.PersonTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ProjectTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SimpleDataSetHelper;
import ch.systemsx.cisd.openbis.generic.shared.translator.VocabularyTermTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author Franz-Josef Elmer
 */
public class ETLService extends AbstractCommonServer<IETLService> implements IETLService
{
    private final IDAOFactory daoFactory;

    private final IDataStoreServiceFactory dssFactory;

    public ETLService(IAuthenticationService authenticationService,
            ISessionManager<Session> sessionManager, IDAOFactory daoFactory,
            ICommonBusinessObjectFactory boFactory, IDataStoreServiceFactory dssFactory)
    {
        this(authenticationService, sessionManager, daoFactory, null, boFactory, dssFactory);
    }

    ETLService(IAuthenticationService authenticationService,
            ISessionManager<Session> sessionManager, IDAOFactory daoFactory,
            IPropertiesBatchManager propertiesBatchManager, ICommonBusinessObjectFactory boFactory,
            IDataStoreServiceFactory dssFactory)
    {
        super(authenticationService, sessionManager, daoFactory, propertiesBatchManager, boFactory);
        this.daoFactory = daoFactory;
        this.dssFactory = dssFactory;
    }

    public IETLService createLogger(IInvocationLoggerContext context)
    {
        return new ETLServiceLogger(getSessionManager(), context);
    }

    @Override
    public int getVersion()
    {
        return IServer.VERSION;
    }

    public DatabaseInstance getHomeDatabaseInstance(String sessionToken)
    {
        return DatabaseInstanceTranslator.translate(getHomeDatabaseInstance());
    }

    private DatabaseInstancePE getHomeDatabaseInstance()
    {
        return daoFactory.getHomeDatabaseInstance();
    }

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
        setServices(dataStore, info.getServicesDescriptions(), dataStoreDAO);
        dataStoreDAO.createOrUpdateDataStore(dataStore);
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

    private void setServices(DataStorePE dataStore, DatastoreServiceDescriptions serviceDescs,
            IDataStoreDAO dataStoreDAO)
    {
        // Clean services first and save the result.
        // In general it should happen automatically, because services are annotated with
        // "DELETE_ORPHANS".
        // But hibernate does the orphans deletion at the flush time, and insertion of new services
        // is performed before.
        // So if it happens that services with the same keys are registered, we have a unique
        // constraint violation. This is a recognized hibernate bug HHH-2421.
        dataStore.setServices(new HashSet<DataStoreServicePE>());
        dataStoreDAO.createOrUpdateDataStore(dataStore);

        Set<DataStoreServicePE> dataStoreServices = createDataStoreServices(serviceDescs);
        dataStore.setServices(dataStoreServices);
    }

    private Set<DataStoreServicePE> createDataStoreServices(
            DatastoreServiceDescriptions serviceDescriptions)
    {
        Set<DataStoreServicePE> services = new HashSet<DataStoreServicePE>();

        Set<DataStoreServicePE> processing =
                createDataStoreServices(serviceDescriptions.getProcessingServiceDescriptions(),
                        DataStoreServiceKind.PROCESSING);
        services.addAll(processing);

        Set<DataStoreServicePE> queries =
                createDataStoreServices(serviceDescriptions.getReportingServiceDescriptions(),
                        DataStoreServiceKind.QUERIES);
        services.addAll(queries);

        return services;
    }

    private Set<DataStoreServicePE> createDataStoreServices(
            List<DatastoreServiceDescription> serviceDescriptions, DataStoreServiceKind serviceKind)
    {
        Set<DataStoreServicePE> services = new HashSet<DataStoreServicePE>();
        for (DatastoreServiceDescription desc : serviceDescriptions)
        {
            DataStoreServicePE service = new DataStoreServicePE();
            service.setKey(desc.getKey());
            service.setLabel(desc.getLabel());
            service.setKind(serviceKind);
            Set<DataSetTypePE> datasetTypes = extractDatasetTypes(desc.getDatasetTypeCodes(), desc);
            service.setDatasetTypes(datasetTypes);
            service.setReportingPluginTypeOrNull(desc.tryReportingPluginType());
            services.add(service);
        }
        return services;
    }

    private Set<DataSetTypePE> extractDatasetTypes(String[] datasetTypeCodes,
            DatastoreServiceDescription serviceDescription)
    {
        Set<DataSetTypePE> datasetTypes = new HashSet<DataSetTypePE>();
        Set<String> missingCodes = new HashSet<String>();
        IDataSetTypeDAO dataSetTypeDAO = daoFactory.getDataSetTypeDAO();
        for (String datasetTypeCode : datasetTypeCodes)
        {
            DataSetTypePE datasetType = dataSetTypeDAO.tryToFindDataSetTypeByCode(datasetTypeCode);
            if (datasetType == null)
            {
                missingCodes.add(datasetTypeCode);
            } else
            {
                datasetTypes.add(datasetType);
            }
        }
        if (missingCodes.size() > 0)
        {
            notifyDataStoreServerMisconfiguration(missingCodes, serviceDescription);
        }
        return datasetTypes;
    }

    private void notifyDataStoreServerMisconfiguration(Set<String> missingCodes,
            DatastoreServiceDescription serviceDescription)
    {
        String missingCodesText = CollectionUtils.abbreviate(missingCodes, -1);
        notificationLog.warn(String.format("The Datastore Server Plugin '%s' is misconfigured. "
                + "It refers to the dataset types which do not exist in openBIS: %s",
                serviceDescription.toString(), missingCodesText));
    }

    public String createDataSetCode(String sessionToken) throws UserFailureException
    {
        return createPermId(sessionToken);
    }

    public String createPermId(String sessionToken) throws UserFailureException
    {
        checkSession(sessionToken); // throws exception if invalid sessionToken
        return daoFactory.getPermIdDAO().createPermId();
    }

    public long drawANewUniqueID(String sessionToken) throws UserFailureException
    {
        checkSession(sessionToken);
        return daoFactory.getCodeSequenceDAO().getNextCodeSequenceId();
    }

    public Experiment tryToGetExperiment(String sessionToken,
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
        return ExperimentTranslator.translate(experiment, session.getBaseIndexURL(),
                LoadableFields.PROPERTIES);
    }

    public List<Sample> listSamples(String sessionToken, ListSampleCriteria criteria)
    {
        final Session session = getSession(sessionToken);
        final ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        return sampleLister.list(new ListOrSearchSampleCriteria(criteria));
    }

    public Sample tryGetSampleWithExperiment(String sessionToken, SampleIdentifier sampleIdentifier)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleIdentifier != null : "Unspecified sample identifier.";

        final Session session = getSession(sessionToken);
        SamplePE sample = tryLoadSample(session, sampleIdentifier);
        if (sample != null)
        {
            HibernateUtils.initialize(sample.getProperties());
            enrichWithProperties(sample.getExperiment());
        }
        return SampleTranslator.translate(sample, session.getBaseIndexURL());
    }

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

    public List<ExternalData> listDataSetsByExperimentID(String sessionToken, TechId experimentID)
            throws UserFailureException
    {
        Session session = getSession(sessionToken);
        IDatasetLister datasetLister = createDatasetLister(session);
        List<TechId> experimentTechIDs = Collections.singletonList(experimentID);
        List<ExternalData> datasets = datasetLister.listByExperimentTechIds(experimentTechIDs);
        Collections.sort(datasets);
        return datasets;
    }

    public List<ExternalData> listDataSetsBySampleID(final String sessionToken,
            final TechId sampleId, final boolean showOnlyDirectlyConnected)
    {
        final Session session = getSession(sessionToken);
        final IDatasetLister datasetLister = createDatasetLister(session);
        final List<ExternalData> datasets =
                datasetLister.listBySampleTechId(sampleId, showOnlyDirectlyConnected);
        Collections.sort(datasets);
        return datasets;
    }

    public List<ExternalData> listDataSetsByCode(String sessionToken, List<String> dataSetCodes)
    {
        final Session session = getSession(sessionToken);
        final IDatasetLister datasetLister = createDatasetLister(session);
        return datasetLister.listByDatasetCode(dataSetCodes);
    }

    public List<Project> listProjects(String sessionToken)
    {
        checkSession(sessionToken);
        final List<ProjectPE> projects = getDAOFactory().getProjectDAO().listProjects();
        Collections.sort(projects);
        return ProjectTranslator.translate(projects);
    }

    public List<Experiment> listExperiments(String sessionToken, ProjectIdentifier projectIdentifier)
    {
        final Session session = getSession(sessionToken);
        final IExperimentTable experimentTable =
                businessObjectFactory.createExperimentTable(session);
        experimentTable.load(EntityType.ALL_TYPES_CODE, projectIdentifier);
        final List<ExperimentPE> experiments = experimentTable.getExperiments();
        Collections.sort(experiments);
        return ExperimentTranslator.translate(experiments, session.getBaseIndexURL());
    }

    public IEntityProperty[] tryToGetPropertiesOfTopSampleRegisteredFor(String sessionToken,
            SampleIdentifier sampleIdentifier) throws UserFailureException
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

    public long registerExperiment(String sessionToken, NewExperiment experiment)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert experiment != null : "Unspecified new example.";

        final Session session = getSession(sessionToken);
        IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.define(experiment);
        experimentBO.save();
        return experimentBO.getExperiment().getId();
    }

    public void registerSamples(String sessionToken,
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

    public long registerSample(String sessionToken, NewSample newSample, String userIDOrNull)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert newSample != null : "Unspecified new sample.";

        SamplePE samplePE = registerSampleInternal(sessionToken, newSample, userIDOrNull);
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

    public void updateSample(String sessionToken, SampleUpdatesDTO updates)
    {
        final Session session = getSession(sessionToken);
        updateSampleInternal(updates, session);
    }

    public void registerDataSet(String sessionToken, SampleIdentifier sampleIdentifier,
            NewExternalData externalData) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleIdentifier != null : "Unspecified sample identifier.";

        final Session session = getSession(sessionToken);
        registerDataSetInternal(session, sampleIdentifier, externalData);
    }

    public void registerDataSet(String sessionToken, ExperimentIdentifier experimentIdentifier,
            NewExternalData externalData) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert experimentIdentifier != null : "Unspecified experiment identifier.";

        final Session session = getSession(sessionToken);
        registerDataSetInternal(session, experimentIdentifier, externalData);
    }

    public void deleteDataSet(String sessionToken, String dataSetCode, String reason)
            throws UserFailureException
    {
        Session session = getSession(sessionToken);
        IDataDAO dataSetDAO = getDAOFactory().getDataDAO();
        DataPE dataSet = dataSetDAO.tryToFindFullDataSetByCode(dataSetCode, false, false);
        if (dataSet != null && dataSet.isExternalData())
        {
            DataSetTypePE dataSetType = dataSet.getDataSetType();
            IDataSetTypeSlaveServerPlugin plugin = getDataSetTypeSlaveServerPlugin(dataSetType);
            plugin.deleteDataSets(session, Collections.singletonList(dataSet), reason);
        }
    }

    public void addPropertiesToDataSet(String sessionToken, List<NewProperty> properties,
            String dataSetCode, SpaceIdentifier space) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSession(sessionToken);
        final IDataBO dataBO = businessObjectFactory.createDataBO(session);
        dataBO.addPropertiesToDataSet(dataSetCode, properties);
    }

    public void updateShareIdAndSize(String sessionToken, String dataSetCode, String shareId,
            long size)
    {
        checkSession(sessionToken);

        IDataDAO dataSetDAO = getDAOFactory().getDataDAO();
        DataPE dataSet = dataSetDAO.tryToFindFullDataSetByCode(dataSetCode, false, false);
        if (dataSet == null)
        {
            throw new UserFailureException("Unknown data set " + dataSetCode);
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
        dataSetDAO.updateDataSet(dataSet);
    }

    public void updateDataSetStatuses(String sessionToken, List<String> dataSetCodes,
            DataSetArchivingStatus newStatus, boolean presentInArchive) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSession(sessionToken);
        final IDataBO dataBO = businessObjectFactory.createDataBO(session);
        dataBO.updateStatuses(dataSetCodes, newStatus, presentInArchive);
    }

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

    public ExternalData tryGetDataSet(String sessionToken, String dataSetCode)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert dataSetCode != null : "Unspecified data set code.";

        Session session = getSession(sessionToken); // assert authenticated

        IDataBO dataBO = businessObjectFactory.createDataBO(session);
        dataBO.loadByCode(dataSetCode);
        dataBO.enrichWithParentsAndExperiment();
        dataBO.enrichWithProperties();
        dataBO.enrichWithContainedDataSets();
        DataPE dataPE = dataBO.tryGetData();
        if (null == dataPE)
            return null;
        return DataSetTranslator.translate(dataPE, session.getBaseIndexURL());
    }

    public void checkInstanceAdminAuthorization(String sessionToken) throws UserFailureException
    {
        checkSession(sessionToken);
        // do nothing, the access rights specified in method annotations are checked by a proxy
    }

    public void checkSpacePowerUserAuthorization(String sessionToken) throws UserFailureException
    {
        checkSession(sessionToken);
        // do nothing, the access rights specified in method annotations are checked by a proxy
    }

    public void checkDataSetAccess(String sessionToken, String dataSetCode)
            throws UserFailureException
    {
        checkSession(sessionToken);
        // do nothing, the access rights specified in method annotations are checked by a proxy
    }

    public void checkDataSetCollectionAccess(String sessionToken, List<String> dataSetCodes)
    {
        checkSession(sessionToken);
        // do nothing, the access rights specified in method annotations are checked by a proxy
    }

    public void checkSpaceAccess(String sessionToken, SpaceIdentifier spaceId)
            throws UserFailureException
    {
        checkSession(sessionToken);
        // do nothing, the access rights specified in method annotations are checked by a proxy
    }

    public List<Sample> listSamplesByCriteria(String sessionToken,
            ListSamplesByPropertyCriteria criteria) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert criteria != null : "Unspecified criteria.";

        Session session = getSession(sessionToken);
        ISampleTable sampleTable = businessObjectFactory.createSampleTable(session);
        sampleTable.loadSamplesByCriteria(criteria);
        return SampleTranslator.translate(sampleTable.getSamples(), "");
    }

    public List<DataSetShareId> listShareIds(String sessionToken, String dataStore)
            throws UserFailureException
    {
        List<ExternalData> dataSets = loadDataSets(sessionToken, dataStore);
        ArrayList<DataSetShareId> shareIds = new ArrayList<DataSetShareId>();
        for (ExternalData dataSet : dataSets)
        {
            DataSetShareId dataSetShareId = new DataSetShareId();
            dataSetShareId.setDataSetCode(dataSet.getCode());
            DataSet ds = dataSet.tryGetAsDataSet();
            if (ds != null)
            {
                dataSetShareId.setShareId(ds.getShareId());
            }
            shareIds.add(dataSetShareId);
        }
        return shareIds;
    }

    public List<SimpleDataSetInformationDTO> listDataSets(String sessionToken, String dataStoreCode)
            throws UserFailureException
    {
        List<ExternalData> dataSets = loadDataSets(sessionToken, dataStoreCode);
        return SimpleDataSetHelper.translate(dataSets);
    }

    public List<ExternalData> listAvailableDataSets(String sessionToken, String dataStoreCode,
            ArchiverDataSetCriteria criteria)
    {
        Session session = getSession(sessionToken);
        final IDatasetLister datasetLister = createDatasetLister(session);
        return datasetLister.listByArchiverCriteria(dataStoreCode, criteria);
    }

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
        IDatasetLister datasetLister = businessObjectFactory.createDatasetLister(session);
        DataStorePE dataStore =
                getDAOFactory().getDataStoreDAO().tryToFindDataStoreByCode(dataStoreCode);
        if (dataStore == null)
        {
            throw new UserFailureException(String.format("Unknown data store '%s'", dataStoreCode));
        }
        return datasetLister.listByDataStore(dataStore.getId());
    }

    public List<DeletedDataSet> listDeletedDataSets(String sessionToken,
            Long lastSeenDeletionEventIdOrNull, Date maxDeletionDataOrNull)
    {
        checkSession(sessionToken);
        return getDAOFactory().getEventDAO().listDeletedDataSets(lastSeenDeletionEventIdOrNull,
                maxDeletionDataOrNull);
    }

    public ExternalData tryGetDataSetForServer(String sessionToken, String dataSetCode)
            throws UserFailureException
    {
        return tryGetDataSet(sessionToken, dataSetCode);
    }

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

    public List<String> generateCodes(String sessionToken, String prefix, int number)
    {
        checkSession(sessionToken);
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < number; i++)
        {
            result.add(prefix + daoFactory.getCodeSequenceDAO().getNextCodeSequenceId());
        }
        return result;
    }

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

    public Person tryPersonWithUserIdOrEmail(String sessionToken, String useridOrEmail)
    {
        checkSession(sessionToken);
        // First search for a userId match
        IPersonDAO personDao = getDAOFactory().getPersonDAO();
        PersonPE person = personDao.tryFindPersonByUserId(useridOrEmail);
        if (null != person)
        {
            return PersonTranslator.translate(person);
        }
        // Didn't find one -- try email
        person = personDao.tryFindPersonByEmail(useridOrEmail);
        if (null != person)
        {
            return PersonTranslator.translate(person);
        }
        return null;
    }

    public Sample registerSampleAndDataSet(String sessionToken, NewSample newSample,
            NewExternalData externalData, String userIdOrNull) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert newSample != null : "Unspecified new sample.";

        // Register the Sample
        SamplePE samplePE = registerSampleInternal(sessionToken, newSample, userIdOrNull);

        // Register the data set
        registerDataSetInternal(sessionToken, externalData, samplePE);

        final Session session = getSession(sessionToken);
        Sample result =
                SampleTranslator.translate(Collections.singletonList(samplePE),
                        session.getBaseIndexURL()).get(0);
        return result;
    }

    public Sample updateSampleAndRegisterDataSet(String sessionToken, SampleUpdatesDTO updates,
            NewExternalData externalData)
    {
        final Session session = getSession(sessionToken);

        // Update the sample
        final ISampleBO sampleBO = updateSampleInternal(updates, session);

        // Register the data set
        final SamplePE samplePE = sampleBO.getSample();
        registerDataSetInternal(sessionToken, externalData, samplePE);

        Sample result =
                SampleTranslator.translate(Collections.singletonList(samplePE),
                        session.getBaseIndexURL()).get(0);
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
        if (experiment.getInvalidation() != null)
        {
            throw new UserFailureException("Data set can not be registered because experiment '"
                    + experiment.getIdentifier() + "' is invalid.");
        }

        final IDataBO dataBO = businessObjectFactory.createDataBO(session);
        SourceType sourceType =
                externalData.isMeasured() ? SourceType.MEASUREMENT : SourceType.DERIVED;
        dataBO.define(externalData, samplePE, sourceType);
        dataBO.save();
        final String dataSetCode = dataBO.getData().getCode();
        assert dataSetCode != null : "Data set code not specified.";
    }

    private SamplePE registerSampleInternal(String sessionToken, NewSample newSample,
            String userIdOrNull)
    {
        final Session session = getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.define(newSample);
        if (userIdOrNull != null)
        {
            sampleBO.getSample().setRegistrator(getOrCreatePerson(sessionToken, userIdOrNull));
        }
        sampleBO.save();
        SamplePE samplePE = sampleBO.getSample();
        return samplePE;
    }

    public Space tryGetSpace(String sessionToken, SpaceIdentifier spaceIdentifier)
    {

        Session session = getSession(sessionToken);
        IGroupBO groupBO = businessObjectFactory.createGroupBO(session);
        GroupIdentifier identifier =
                new GroupIdentifier(spaceIdentifier.getDatabaseInstanceCode(),
                        spaceIdentifier.getSpaceCode());
        try
        {
            groupBO.load(identifier);
            return GroupTranslator.translate(groupBO.getGroup());
        } catch (UserFailureException ufe)
        {
            // space does not exist
            return null;
        }

    }

    public Project tryGetProject(String sessionToken, ProjectIdentifier projectIdentifier)
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

    public AtomicEntityOperationResult performEntityOperations(String sessionToken,
            AtomicEntityOperationDetails operationDetails)
    {

        List<Space> spacesCreated = createSpaces(sessionToken, operationDetails);

        List<Project> projectsCreated = createProjects(sessionToken, operationDetails);

        List<Experiment> experimentsCreated = createExperiments(sessionToken, operationDetails);

        List<Sample> samplesCreated = createSamples(sessionToken, operationDetails);

        List<Sample> samplesUpdated = updateSamples(sessionToken, operationDetails);

        List<ExternalData> dataSetsCreated = createDataSets(sessionToken, operationDetails);

        return new AtomicEntityOperationResult(spacesCreated, projectsCreated, experimentsCreated,
                samplesUpdated, samplesCreated, dataSetsCreated);
    }

    private List<Space> createSpaces(String sessionToken,
            AtomicEntityOperationDetails operationDetails)
    {
        ArrayList<SpacePE> spacePEsCreated = new ArrayList<SpacePE>();
        List<NewSpace> newSpaces = operationDetails.getSpaceRegistrations();
        for (NewSpace newSpace : newSpaces)
        {
            SpacePE spacePE =
                    registerSpaceInternal(sessionToken, newSpace,
                            operationDetails.tryUserIdOrNull());
            spacePEsCreated.add(spacePE);
        }
        return GroupTranslator.translate(spacePEsCreated);
    }

    private SpacePE registerSpaceInternal(String sessionToken, NewSpace newSpace,
            String registratorUserIdOrNull)
    {
        // create space
        Session session = getSession(sessionToken);
        IGroupBO groupBO = businessObjectFactory.createGroupBO(session);
        groupBO.define(newSpace.getCode(), newSpace.getDescription());
        if (registratorUserIdOrNull != null)
        {
            groupBO.getGroup().setRegistrator(
                    getOrCreatePerson(sessionToken, registratorUserIdOrNull));
        }
        groupBO.save();

        // create ADMIN role assignemnt
        SpacePE space = groupBO.getGroup();
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

    private List<Project> createProjects(String sessionToken,
            AtomicEntityOperationDetails operationDetails)
    {
        ArrayList<ProjectPE> projectPEsCreated = new ArrayList<ProjectPE>();
        List<NewProject> newProjects = operationDetails.getProjectRegistrations();
        for (NewProject newProject : newProjects)
        {
            ProjectPE projectPE =
                    registerProjectInternal(sessionToken, newProject,
                            operationDetails.tryUserIdOrNull());
            projectPEsCreated.add(projectPE);
        }
        return ProjectTranslator.translate(projectPEsCreated);
    }

    private ProjectPE registerProjectInternal(String sessionToken, NewProject newProject,
            String registratorUserIdOrNull)
    {
        Session session = getSession(sessionToken);
        IProjectBO projectBO = businessObjectFactory.createProjectBO(session);
        ProjectIdentifier identifier =
                new ProjectIdentifierFactory(newProject.getIdentifier()).createIdentifier();
        projectBO.define(identifier, newProject.getDescription(), null);
        if (registratorUserIdOrNull != null)
        {
            projectBO.getProject().setRegistrator(
                    getOrCreatePerson(sessionToken, registratorUserIdOrNull));
        }
        projectBO.save();

        return projectBO.getProject();
    }

    private List<Sample> createSamples(String sessionToken,
            AtomicEntityOperationDetails operationDetails)
    {
        ArrayList<SamplePE> samplePEsCreated = new ArrayList<SamplePE>();
        List<NewSample> newSamples = operationDetails.getSampleRegistrations();
        for (NewSample newSample : newSamples)
        {
            SamplePE samplePE =
                    registerSampleInternal(sessionToken, newSample,
                            operationDetails.tryUserIdOrNull());
            samplePEsCreated.add(samplePE);
        }
        final Session session = getSession(sessionToken);
        return SampleTranslator.translate(samplePEsCreated, session.getBaseIndexURL());
    }

    private List<Sample> updateSamples(String sessionToken,
            AtomicEntityOperationDetails operationDetails)
    {
        final Session session = getSession(sessionToken);
        ArrayList<SamplePE> samplePEsUpdated = new ArrayList<SamplePE>();
        List<SampleUpdatesDTO> sampleUpdates = operationDetails.getSampleUpdates();
        for (SampleUpdatesDTO sampleUpdate : sampleUpdates)
        {
            SamplePE samplePE = updateSampleInternal(sampleUpdate, session).getSample();
            samplePEsUpdated.add(samplePE);
        }
        return SampleTranslator.translate(samplePEsUpdated, session.getBaseIndexURL());
    }

    private List<ExternalData> createDataSets(String sessionToken,
            AtomicEntityOperationDetails operationDetails)
    {
        final Session session = getSession(sessionToken);
        ArrayList<DataPE> dataSetsCreated = new ArrayList<DataPE>();
        ArrayList<? extends NewExternalData> dataSetRegistrations =
                operationDetails.getDataSetRegistrations();
        for (NewExternalData dataSet : dataSetRegistrations)
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
        return DataSetTranslator.translate(dataSetsCreated, "", session.getBaseIndexURL());
    }

    private ArrayList<Experiment> createExperiments(String sessionToken,
            AtomicEntityOperationDetails operationDetails)
    {
        ArrayList<Experiment> experimentsCreated = new ArrayList<Experiment>();
        List<NewExperiment> experimentRegistrations = operationDetails.getExperimentRegistrations();
        for (NewExperiment experiment : experimentRegistrations)
        {
            registerExperiment(sessionToken, experiment);
            ExperimentIdentifier experimentIdentifier =
                    new ExperimentIdentifierFactory(experiment.getIdentifier()).createIdentifier();
            experimentsCreated.add(tryToGetExperiment(sessionToken, experimentIdentifier));
        }
        return experimentsCreated;
    }

    private IDataBO registerDataSetInternal(final Session session,
            SampleIdentifier sampleIdentifier, NewExternalData externalData)
    {
        ExperimentPE experiment = tryLoadExperimentBySampleIdentifier(session, sampleIdentifier);
        if (experiment == null)
        {
            throw new UserFailureException("No experiment found for sample " + sampleIdentifier);
        }
        if (experiment.getInvalidation() != null)
        {
            throw new UserFailureException("Data set can not be registered because experiment '"
                    + experiment.getIdentifier() + "' is invalid.");
        }
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadBySampleIdentifier(sampleIdentifier);
        final SamplePE cellPlate = sampleBO.getSample();
        final IDataBO dataBO = businessObjectFactory.createDataBO(session);
        SourceType sourceType =
                externalData.isMeasured() ? SourceType.MEASUREMENT : SourceType.DERIVED;
        dataBO.define(externalData, cellPlate, sourceType);
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
        if (experiment.getInvalidation() != null)
        {
            throw new UserFailureException("Data set can not be registered because experiment '"
                    + experiment.getIdentifier() + "' is invalid.");
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

}
