/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.FactoryBean;

import ch.systemsx.cisd.common.api.client.ServiceFinder;
import ch.systemsx.cisd.common.conversation.ConversationalRmiClient;
import ch.systemsx.cisd.common.conversation.RmiConversationController;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.serviceconversation.ServiceMessage;
import ch.systemsx.cisd.openbis.dss.generic.server.openbisauth.OpenBISSessionHolder;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.OpenBisServiceFactory;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.EntityOperationsState;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetShareId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * A class that encapsulates the {@link IETLLIMSService}.
 * 
 * @author Bernd Rinn
 */
public final class EncapsulatedOpenBISService implements IEncapsulatedOpenBISService, FactoryBean,
        ConversationalRmiClient
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            EncapsulatedOpenBISService.class);

    private final IETLLIMSService service;

    private final RmiConversationController conversationController;

    private Integer version;

    private DatabaseInstance homeDatabaseInstance;

    // this session object is automatically kept up-to-date by an aspect
    private OpenBISSessionHolder session;

    private IShareIdManager shareIdManager;

    public static IETLLIMSService createOpenBisService(String openBISURL, String timeout)
    {
        OpenBisServiceFactory factory =
                new OpenBisServiceFactory(openBISURL, ResourceNames.ETL_SERVICE_URL);
        if (timeout.startsWith("$"))
        {
            return factory.createService();
        }
        return factory.createService(normalizeTimeout(timeout));
    }

    /**
     * Creates a remote version of {@link IGeneralInformationService} for specified URL and time out
     * (in minutes).
     */
    public static IGeneralInformationService createGeneralInformationService(String openBISURL,
            String timeout)
    {
        ServiceFinder finder = new ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
        if (timeout.startsWith("$"))
        {
            return finder.createService(IGeneralInformationService.class, openBISURL);
        }
        return finder.createService(IGeneralInformationService.class, openBISURL,
                normalizeTimeout(timeout));
    }

    private static long normalizeTimeout(String timeout)
    {
        return Integer.parseInt(timeout) * DateUtils.MILLIS_PER_MINUTE;
    }

    public EncapsulatedOpenBISService(IETLLIMSService service, OpenBISSessionHolder sessionHolder,
            String downloadUrl)
    {
        this(service, sessionHolder, downloadUrl, null);
    }

    public EncapsulatedOpenBISService(IETLLIMSService service, OpenBISSessionHolder sessionHolder,
            String downloadUrl, IShareIdManager shareIdManager)
    {
        this.shareIdManager = shareIdManager;
        assert service != null : "Given IETLLIMSService implementation can not be null.";
        assert sessionHolder != null : "Given OpenBISSessionHolder can not be null.";
        this.service = service;
        this.session = sessionHolder;
        this.conversationController =
                new RmiConversationController(downloadUrl
                        + "/datastore_server/encapsulated_openbis_service_conversational_client");
    }

    private IShareIdManager getShareIdManager()
    {
        if (shareIdManager == null)
        {
            shareIdManager = ServiceProvider.getShareIdManager();
        }
        return shareIdManager;
    }

    @Override
    public Object getObject() throws Exception
    {
        return this;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class getObjectType()
    {
        return IEncapsulatedOpenBISService.class;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }

    //
    // IEncapsulatedOpenBISService
    //

    @Override
    public Experiment tryToGetExperiment(ExperimentIdentifier experimentIdentifier)
    {
        assert experimentIdentifier != null : " Unspecified experiment identifier.";
        return service.tryToGetExperiment(session.getToken(), experimentIdentifier);
    }

    @Override
    public Space tryGetSpace(SpaceIdentifier spaceIdentifier) throws UserFailureException
    {
        assert spaceIdentifier != null : "Unspecified space identifier";
        return service.tryGetSpace(session.getToken(), spaceIdentifier);
    }

    @Override
    public Project tryGetProject(ProjectIdentifier projectIdentifier) throws UserFailureException
    {
        assert projectIdentifier != null : "Unspecified project identifier";
        return service.tryGetProject(session.getToken(), projectIdentifier);
    }

    @Override
    public List<Sample> listSamples(ListSampleCriteria criteria)
    {
        assert criteria != null : "Unspecifed criteria.";
        return service.listSamples(session.getToken(), criteria);
    }

    @Override
    public final Sample tryGetSampleWithExperiment(final SampleIdentifier sampleIdentifier)
    {
        assert sampleIdentifier != null : "Given sample identifier can not be null.";
        return service.tryGetSampleWithExperiment(session.getToken(), sampleIdentifier);
    }

    @Override
    public SampleIdentifier tryToGetSampleIdentifier(String samplePermID)
            throws UserFailureException
    {
        return service.tryToGetSampleIdentifier(session.getToken(), samplePermID);
    }

    @Override
    public ExperimentType getExperimentType(String experimentTypeCode) throws UserFailureException
    {
        return service.getExperimentType(session.getToken(), experimentTypeCode);
    }

    @Override
    public Collection<VocabularyTerm> listVocabularyTerms(String vocabularyCode)
            throws UserFailureException
    {
        return service.listVocabularyTerms(session.getToken(), vocabularyCode);
    }

    @Override
    public SampleType getSampleType(String sampleTypeCode) throws UserFailureException
    {
        return service.getSampleType(session.getToken(), sampleTypeCode);
    }

    @Override
    public DataSetTypeWithVocabularyTerms getDataSetType(String dataSetTypeCode)
    {
        return service.getDataSetType(session.getToken(), dataSetTypeCode);
    }

    @Override
    public List<ExternalData> listDataSetsByExperimentID(long experimentID)
            throws UserFailureException
    {
        TechId id = new TechId(experimentID);
        return service.listDataSetsByExperimentID(session.getToken(), id);
    }

    @Override
    public List<ExternalData> listDataSetsBySampleID(long sampleID,
            boolean showOnlyDirectlyConnected)
    {
        TechId id = new TechId(sampleID);
        return service.listDataSetsBySampleID(session.getToken(), id, showOnlyDirectlyConnected);
    }

    @Override
    public List<ExternalData> listDataSetsByCode(List<String> dataSetCodes)
            throws UserFailureException
    {
        return service.listDataSetsByCode(session.getToken(), dataSetCodes);
    }

    @Override
    public long registerExperiment(NewExperiment experiment) throws UserFailureException
    {
        assert experiment != null : "Unspecified experiment.";
        return service.registerExperiment(session.getToken(), experiment);
    }

    @Override
    public void registerSamples(List<NewSamplesWithTypes> newSamples, String userIDOrNull)
            throws UserFailureException
    {
        assert newSamples != null : "Unspecified samples.";

        service.registerSamples(session.getToken(), newSamples, userIDOrNull);
    }

    @Override
    public long registerSample(NewSample newSample, String userIDOrNull)
            throws UserFailureException
    {
        assert newSample != null : "Unspecified sample.";

        return service.registerSample(session.getToken(), newSample, userIDOrNull);
    }

    @Override
    public void updateSample(SampleUpdatesDTO sampleUpdate) throws UserFailureException
    {
        assert sampleUpdate != null : "Unspecified sample.";

        service.updateSample(session.getToken(), sampleUpdate);
    }

    @Override
    public final void registerDataSet(final DataSetInformation dataSetInformation,
            final NewExternalData data)
    {
        assert dataSetInformation != null : "missing sample identifier";
        assert data != null : "missing data";

        SampleIdentifier sampleIdentifier = dataSetInformation.getSampleIdentifier();
        if (sampleIdentifier == null)
        {
            ExperimentIdentifier experimentIdentifier =
                    dataSetInformation.getExperimentIdentifier();
            service.registerDataSet(session.getToken(), experimentIdentifier, data);
        } else
        {
            service.registerDataSet(session.getToken(), sampleIdentifier, data);
        }
        setShareId(data);

        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Registered in openBIS: data set " + dataSetInformation.describe()
                    + ".");
        }
    }

    @Override
    public final void updateDataSet(String code, List<NewProperty> properties, SpaceIdentifier space)
            throws UserFailureException

    {
        assert code != null : "missing data set code";
        assert properties != null : "missing data";
        assert space != null : "space missing";

        service.addPropertiesToDataSet(session.getToken(), properties, code, space);

        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Updated in openBIS: data set " + code + ".");
        }
    }

    @Override
    public void updateShareIdAndSize(String dataSetCode, String shareId, long size)
            throws UserFailureException
    {
        service.updateShareIdAndSize(session.getToken(), dataSetCode, shareId, size);

        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Updated in openBIS: data set " + dataSetCode + ", share Id: "
                    + shareId + ", size: " + size);
        }
    }

    @Override
    public final void updateDataSetStatuses(List<String> codes, DataSetArchivingStatus newStatus,
            boolean presentInArchive) throws UserFailureException

    {
        assert codes != null : "missing data set codes";
        assert newStatus != null : "missing status";

        service.updateDataSetStatuses(session.getToken(), codes, newStatus, presentInArchive);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Updated in openBIS: data sets " + codes + ", status=" + newStatus);
        }
    }

    @Override
    public boolean compareAndSetDataSetStatus(String dataSetCode, DataSetArchivingStatus oldStatus,
            DataSetArchivingStatus newStatus, boolean newPresentInArchive)
            throws UserFailureException
    {
        assert dataSetCode != null : "missing data set codes";
        assert oldStatus != null : "missing old status";
        assert newStatus != null : "missing new status";
        return service.compareAndSetDataSetStatus(session.getToken(), dataSetCode, oldStatus,
                newStatus, newPresentInArchive);
    }

    @Override
    public final IEntityProperty[] getPropertiesOfTopSampleRegisteredFor(
            final SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        assert sampleIdentifier != null : "Given sample identifier can not be null.";
        return service.tryToGetPropertiesOfTopSampleRegisteredFor(session.getToken(),
                sampleIdentifier);
    }

    @Override
    public final List<Sample> listSamplesByCriteria(final ListSamplesByPropertyCriteria criteria)
            throws UserFailureException
    {
        return service.listSamplesByCriteria(session.getToken(), criteria);
    }

    @Override
    public final int getVersion()
    {
        if (version == null)
        {
            version = service.getVersion();
        }
        return version;
    }

    @Override
    public final DatabaseInstance getHomeDatabaseInstance()
    {
        if (homeDatabaseInstance == null)
        {
            homeDatabaseInstance = service.getHomeDatabaseInstance(session.getToken());
        }
        return homeDatabaseInstance;
    }

    @Override
    public final String createDataSetCode()
    {
        return service.createDataSetCode(session.getToken());
    }

    @Override
    public final String createPermId()
    {
        return service.createPermId(session.getToken());
    }

    @Override
    public long drawANewUniqueID()
    {
        return service.drawANewUniqueID(session.getToken());
    }

    @Override
    public ExternalData tryGetDataSet(String dataSetCode) throws UserFailureException
    {
        return service.tryGetDataSet(session.getToken(), dataSetCode);
    }

    @Override
    public ExternalData tryGetDataSet(String sToken, String dataSetCode)
            throws UserFailureException
    {
        return service.tryGetDataSet(sToken, dataSetCode);
    }

    @Override
    public void checkInstanceAdminAuthorization(String sToken) throws UserFailureException
    {
        service.checkInstanceAdminAuthorization(sToken);
    }

    @Override
    public void checkSpacePowerUserAuthorization(String sessionToken) throws UserFailureException
    {
        service.checkSpacePowerUserAuthorization(sessionToken);
    }

    @Override
    public void checkDataSetAccess(String sToken, String dataSetCode) throws UserFailureException
    {
        service.checkDataSetAccess(sToken, dataSetCode);
    }

    @Override
    public void checkDataSetCollectionAccess(String sToken, List<String> dataSetCodes)
            throws UserFailureException
    {
        service.checkDataSetCollectionAccess(sToken, dataSetCodes);
    }

    @Override
    public void checkSpaceAccess(String sToken, SpaceIdentifier spaceId)
    {
        service.checkSpaceAccess(sToken, spaceId);
    }

    @Override
    public List<DataSetShareId> listDataSetShareIds() throws UserFailureException
    {
        List<DataSetShareId> shareIds =
                service.listShareIds(session.getToken(), session.getDataStoreCode());
        for (DataSetShareId dataSetShareId : shareIds)
        {
            if (dataSetShareId.getShareId() == null)
            {
                dataSetShareId
                        .setShareId(ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID);
            }
        }
        return shareIds;
    }

    @Override
    public List<SimpleDataSetInformationDTO> listDataSets() throws UserFailureException
    {
        List<SimpleDataSetInformationDTO> dataSets =
                service.listDataSets(session.getToken(), session.getDataStoreCode());
        for (SimpleDataSetInformationDTO dataSet : dataSets)
        {
            if (dataSet.getDataSetShareId() == null)
            {
                dataSet.setDataSetShareId(ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID);
            }
        }
        return dataSets;
    }

    @Override
    public List<ExternalData> listNewerDataSets(TrackingDataSetCriteria criteria)
            throws UserFailureException
    {
        return service.listDataSets(session.getToken(), session.getDataStoreCode(), criteria);
    }

    @Override
    public List<ExternalData> listAvailableDataSets(ArchiverDataSetCriteria criteria)
            throws UserFailureException
    {
        return service.listAvailableDataSets(session.getToken(), session.getDataStoreCode(),
                criteria);
    }

    @Override
    public List<DeletedDataSet> listDeletedDataSets(Long lastSeenDeletionEventIdOrNull,
            Date maxDeletionDataOrNull)
    {
        return service.listDeletedDataSets(session.getToken(), lastSeenDeletionEventIdOrNull,
                maxDeletionDataOrNull);
    }

    @Override
    public void archiveDataSets(List<String> dataSetCodes, boolean removeFromDataStore)
            throws UserFailureException
    {
        service.archiveDatasets(session.getToken(), dataSetCodes, removeFromDataStore);
    }

    @Override
    public void unarchiveDataSets(List<String> dataSetCodes) throws UserFailureException
    {
        service.unarchiveDatasets(session.getToken(), dataSetCodes);
    }

    @Override
    public SessionContextDTO tryGetSession(String sToken)
    {
        return service.tryGetSession(sToken);
    }

    @Override
    public ExternalData tryGetDataSetForServer(String dataSetCode) throws UserFailureException
    {
        return service.tryGetDataSetForServer(session.getToken(), dataSetCode);
    }

    @Override
    public List<String> generateCodes(String prefix, int size)
    {
        return service.generateCodes(session.getToken(), prefix, size);
    }

    @Override
    public List<Person> listAdministrators()
    {
        return service.listAdministrators(session.getToken());
    }

    @Override
    public Person tryPersonWithUserIdOrEmail(String useridOrEmail)
    {
        return service.tryPersonWithUserIdOrEmail(session.getToken(), useridOrEmail);
    }

    @Override
    public Sample registerSampleAndDataSet(NewSample newSample, NewExternalData externalData,
            String userIdOrNull) throws UserFailureException
    {
        Sample sample =
                service.registerSampleAndDataSet(session.getToken(), newSample, externalData,
                        userIdOrNull);
        setShareId(externalData);
        return sample;
    }

    @Override
    public Sample updateSampleAndRegisterDataSet(SampleUpdatesDTO newSample,
            NewExternalData externalData)
    {
        Sample sample =
                service.updateSampleAndRegisterDataSet(session.getToken(), newSample, externalData);
        setShareId(externalData);
        return sample;
    }

    @Override
    public AtomicEntityOperationResult performEntityOperations(
            AtomicEntityOperationDetails operationDetails)
    {
        IETLLIMSService conversationalService =
                conversationController.getConversationalReference(session.getToken(), service,
                        IETLLIMSService.class);

        AtomicEntityOperationResult operations =
                conversationalService.performEntityOperations(session.getToken(), operationDetails);
        List<? extends NewExternalData> dataSets = operationDetails.getDataSetRegistrations();
        for (NewExternalData dataSet : dataSets)
        {
            setShareId(dataSet);
        }
        return operations;
    }

    @Override
    public void send(ServiceMessage message)
    {
        this.conversationController.process(message);
    }

    private void setShareId(NewExternalData data)
    {
        getShareIdManager().setShareId(data.getCode(), data.getShareId());
    }

    @Override
    public List<Sample> searchForSamples(SearchCriteria searchCriteria)
    {
        return service.searchForSamples(session.getToken(), searchCriteria);
    }

    @Override
    public List<ExternalData> searchForDataSets(SearchCriteria searchCriteria)
    {
        return service.searchForDataSets(session.getToken(), searchCriteria);
    }

    @Override
    public List<Project> listProjects()
    {
        return service.listProjects(session.getToken());
    }

    @Override
    public List<Experiment> listExperiments(ProjectIdentifier projectIdentifier)
    {
        return service.listExperiments(session.getToken(), projectIdentifier);
    }

    @Override
    public Material tryGetMaterial(MaterialIdentifier materialIdentifier)
    {
        return service.tryGetMaterial(session.getToken(), materialIdentifier);
    }

    @Override
    public List<Material> listMaterials(ListMaterialCriteria criteria, boolean withProperties)
    {
        return service.listMaterials(session.getToken(), criteria, withProperties);
    }

    @Override
    public void removeDataSetsPermanently(List<String> dataSetCodes, String reason)
    {
        service.removeDataSetsPermanently(session.getToken(), dataSetCodes, reason);
    }

    @Override
    public void updateDataSet(DataSetUpdatesDTO dataSetUpdates)
    {
        service.updateDataSet(session.getToken(), dataSetUpdates);
    }

    @Override
    public List<String> getTrustedCrossOriginDomains()
    {
        return service.getTrustedCrossOriginDomains(session.getToken());
    }

    @Override
    public void setStorageConfirmed(String dataSetCode)
    {
        service.setStorageConfirmed(session.getToken(), dataSetCode);
    }

    @Override
    public void markSuccessfulPostRegistration(String dataSetCode)
    {
        service.markSuccessfulPostRegistration(session.getToken(), dataSetCode);
    }

    @Override
    public List<ExternalData> listDataSetsForPostRegistration()
    {
        return service.listDataSetsForPostRegistration(session.getToken(),
                session.getDataStoreCode());
    }

    @Override
    public EntityOperationsState didEntityOperationsSucceed(TechId registrationId)
    {
        return service.didEntityOperationsSucceed(session.getToken(), registrationId);
    }

    @Override
    public void heartbeat()
    {
        service.heartbeat(session.getToken());
    }

    @Override
    public boolean doesUserHaveRole(String user, String roleCode, String spaceOrNull)
    {
        return service.doesUserHaveRole(session.getToken(), user, roleCode, spaceOrNull);
    }

}