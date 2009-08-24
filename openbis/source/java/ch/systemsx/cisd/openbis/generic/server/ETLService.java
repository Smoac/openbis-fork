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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.SimpleDataSetHelper;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAttachmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataSetTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatastoreServiceDescriptions;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcessingInstructionDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author Franz-Josef Elmer
 */
public class ETLService extends AbstractServer<IETLService> implements IETLService
{
    @Private
    static final String PROCESSING_PATH = "$processing-path-for-";

    @Private
    static final String PROCESSING_PATH_TEMPLATE = PROCESSING_PATH + "%s";

    @Private
    static final String PROCESSING_PARAMETERS_TEMPLATE = "$processing-parameters-for-%s";

    @Private
    static final String PROCESSING_DESCRIPTION_TEMPLATE = "$processing-description-for-%s";

    private static final String ENCODING = "utf-8";

    private final ISessionManager<Session> sessionManager;

    private final IDAOFactory daoFactory;

    private final ICommonBusinessObjectFactory boFactory;

    private final IDataStoreServiceFactory dssFactory;

    public ETLService(ISessionManager<Session> sessionManager, IDAOFactory daoFactory,
            ICommonBusinessObjectFactory boFactory, IDataStoreServiceFactory dssFactory)
    {
        super(sessionManager, daoFactory);
        this.sessionManager = sessionManager;
        this.daoFactory = daoFactory;
        this.boFactory = boFactory;
        this.dssFactory = dssFactory;
    }

    public IETLService createLogger(final boolean invocationSuccessful, final long elapsedTime)
    {
        return new ETLServiceLogger(getSessionManager(), invocationSuccessful, elapsedTime);
    }

    @Override
    public int getVersion()
    {
        return IServer.VERSION;
    }

    public DatabaseInstancePE getHomeDatabaseInstance(String sessionToken)
    {
        return daoFactory.getHomeDatabaseInstance();
    }

    public void registerDataStoreServer(String sessionToken, DataStoreServerInfo info)
    {
        Session session = sessionManager.getSession(sessionToken);

        String dssSessionToken = info.getSessionToken();
        String remoteHost = session.getRemoteHost();
        int port = info.getPort();
        final String dssURL = "https://" + remoteHost + ":" + port;
        checkVersion(dssSessionToken, dssURL);
        IDataStoreDAO dataStoreDAO = daoFactory.getDataStoreDAO();
        DataStorePE dataStore = dataStoreDAO.tryToFindDataStoreByCode(info.getDataStoreCode());
        if (dataStore == null)
        {
            dataStore = new DataStorePE();
            dataStore.setDatabaseInstance(getHomeDatabaseInstance(sessionToken));
        }
        dataStore.setCode(info.getDataStoreCode());
        dataStore.setDownloadUrl(info.getDownloadUrl());
        dataStore.setRemoteUrl(dssURL);
        dataStore.setSessionToken(dssSessionToken);
        setServices(dataStore, info.getServicesDescriptions(), dataStoreDAO);
        dataStoreDAO.createOrUpdateDataStore(dataStore);
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
        sessionManager.getSession(sessionToken); // throws exception if invalid sessionToken
        return daoFactory.getPermIdDAO().createPermId();
    }

    public SamplePE tryGetSampleWithExperiment(String sessionToken,
            SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleIdentifier != null : "Unspecified sample identifier.";

        final Session session = sessionManager.getSession(sessionToken);
        SamplePE sample = tryLoadSample(session, sampleIdentifier);
        if (sample != null)
        {
            HibernateUtils.initialize(sample.getProperties());
            enrichWithPropertiesAndProcessingInstructions(sample.getExperiment());
        }
        return sample;
    }

    private ExperimentPE tryLoadExperimentBySampleIdentifier(final Session session,
            SampleIdentifier sampleIdentifier)
    {
        final SamplePE sample = tryLoadSample(session, sampleIdentifier);
        return sample == null ? null : sample.getExperiment();
    }

    private SamplePE tryLoadSample(final Session session, SampleIdentifier sampleIdentifier)
    {
        final ISampleBO sampleBO = boFactory.createSampleBO(session);
        sampleBO.tryToLoadBySampleIdentifier(sampleIdentifier);
        final SamplePE sample = sampleBO.tryToGetSample();
        return sample;
    }

    private void enrichWithPropertiesAndProcessingInstructions(ExperimentPE experiment)
    {
        if (experiment == null)
        {
            return;
        }
        HibernateUtils.initialize(experiment.getProperties());
        final List<ProcessingInstructionDTO> instructions =
                new ArrayList<ProcessingInstructionDTO>();
        final IAttachmentDAO experimentAttachmentDAO = daoFactory.getAttachmentDAO();
        final List<AttachmentPE> attachments = experimentAttachmentDAO.listAttachments(experiment);
        for (final AttachmentPE attachment : attachments)
        {
            final String fileName = attachment.getFileName();
            if (fileName.startsWith(PROCESSING_PATH))
            {
                final ProcessingInstructionDTO processingInstruction =
                        new ProcessingInstructionDTO();
                BeanUtils.fillBean(ProcessingInstructionDTO.class, processingInstruction,
                        attachment);
                processingInstruction.setProcedureTypeCode(fileName.substring(PROCESSING_PATH
                        .length()));
                instructions.add(processingInstruction);
            }
        }
        for (final ProcessingInstructionDTO instruction : instructions)
        {
            final String procedureType = instruction.getProcedureTypeCode();
            instruction.setPath(loadText(experiment, PROCESSING_PATH_TEMPLATE, procedureType));
            instruction.setDescription(loadText(experiment, PROCESSING_DESCRIPTION_TEMPLATE,
                    procedureType));
            instruction.setParameters(load(experiment, PROCESSING_PARAMETERS_TEMPLATE,
                    procedureType));
        }
        experiment.setProcessingInstructions(instructions.toArray(new ProcessingInstructionDTO[0]));
    }

    private final String createKey(final String template, final String procedureTypeCode)
    {
        return String.format(template, procedureTypeCode);
    }

    private final String createText(final byte[] textBytes)
    {
        try
        {
            return textBytes == null ? null : new String(textBytes, ENCODING);
        } catch (UnsupportedEncodingException ex)
        {
            throw new EnvironmentFailureException("Unsupported character encoding: " + ENCODING);
        }
    }

    private final String loadText(final ExperimentPE experiment, final String template,
            final String procedureTypeCode)
    {
        final byte[] value = load(experiment, template, procedureTypeCode);
        return createText(value);
    }

    private final byte[] load(final ExperimentPE experiment, final String template,
            final String procedureTypeCode)
    {
        final String key = createKey(template, procedureTypeCode);
        final IAttachmentDAO experimentAttachmentDAO = daoFactory.getAttachmentDAO();
        final AttachmentPE attachment =
                experimentAttachmentDAO.tryFindAttachmentByOwnerAndFileName(experiment, key);
        if (attachment != null)
        {
            return attachment.getAttachmentContent().getValue();
        }
        return null;
    }

    public SamplePropertyPE[] tryToGetPropertiesOfTopSampleRegisteredFor(String sessionToken,
            SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleIdentifier != null : "Unspecified sample identifier.";

        final Session session = sessionManager.getSession(sessionToken);
        final ISampleBO sampleBO = boFactory.createSampleBO(session);
        sampleBO.loadBySampleIdentifier(sampleIdentifier);
        SamplePE sample = sampleBO.getSample();
        if (sample == null)
        {
            return null;
        }
        SamplePE top = sample.getTop();
        if (top == null)
        {
            return new SamplePropertyPE[0];
        }
        HibernateUtils.initialize(top.getProperties());
        return top.getProperties().toArray(new SamplePropertyPE[0]);
    }

    public void registerDataSet(String sessionToken, SampleIdentifier sampleIdentifier,
            NewExternalData externalData) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleIdentifier != null : "Unspecified sample identifier.";

        final Session session = sessionManager.getSession(sessionToken);
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
        final ISampleBO sampleBO = boFactory.createSampleBO(session);
        sampleBO.loadBySampleIdentifier(sampleIdentifier);
        final SamplePE cellPlate = sampleBO.getSample();
        final IExternalDataBO externalDataBO = boFactory.createExternalDataBO(session);
        SourceType sourceType =
                externalData.isMeasured() ? SourceType.MEASUREMENT : SourceType.DERIVED;
        externalDataBO.define(externalData, cellPlate, sourceType);
        externalDataBO.save();
        final String dataSetCode = externalDataBO.getExternalData().getCode();
        assert dataSetCode != null : "Data set code not specified.";
    }

    public ExternalDataPE tryGetDataSet(String sessionToken, String dataSetCode)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert dataSetCode != null : "Unspecified data set code.";

        Session session = sessionManager.getSession(sessionToken); // assert authenticated

        IExternalDataBO externalDataBO = boFactory.createExternalDataBO(session);
        externalDataBO.loadByCode(dataSetCode);
        externalDataBO.enrichWithParentsAndExperiment();
        return externalDataBO.getExternalData();
    }

    public List<String> listSamplesByCriteria(String sessionToken,
            ListSamplesByPropertyCriteria criteria) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert criteria != null : "Unspecified criteria.";

        Session session = sessionManager.getSession(sessionToken);
        ISampleTable sampleTable = boFactory.createSampleTable(session);
        sampleTable.loadSamplesByCriteria(criteria);
        return extractCodes(sampleTable.getSamples());
    }

    private static List<String> extractCodes(List<SamplePE> samples)
    {
        List<String> codes = new ArrayList<String>();
        for (SamplePE sample : samples)
        {
            codes.add(sample.getCode());
        }
        return codes;
    }

    public List<SimpleDataSetInformationDTO> listDataSets(String sessionToken, String dataStoreCode)
            throws UserFailureException
    {
        Session session = sessionManager.getSession(sessionToken);
        DataStorePE dataStore =
                getDAOFactory().getDataStoreDAO().tryToFindDataStoreByCode(dataStoreCode);
        if (dataStore == null)
        {
            throw new UserFailureException(String.format("Unknown data store '%s'", dataStoreCode));
        }
        IExternalDataTable dataSetTable = boFactory.createExternalDataTable(session);
        dataSetTable.loadByDataStore(dataStore);
        return SimpleDataSetHelper.translate(dataSetTable.getExternalData());
    }
}
