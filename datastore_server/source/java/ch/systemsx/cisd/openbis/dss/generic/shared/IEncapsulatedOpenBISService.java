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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.EntityOperationsState;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
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
 * This interface is very similar to {@link IETLLIMSService} but <code>sessionToken</code> has been
 * removed from most methods.
 * 
 * @see IETLLIMSService
 * @author Christian Ribeaud
 */
public interface IEncapsulatedOpenBISService extends IEncapsulatedBasicOpenBISService
{

    /**
     * Get the basic version of this service, that will filter the results.
     * 
     * @param userName The user used for filtering.
     */
    @ManagedAuthentication
    public IEncapsulatedBasicOpenBISService getBasicFilteredOpenBISService(String userName);

    /**
     * Tries to get the data set location for the specified data set code, using the ETL server's
     * session token.
     */
    @ManagedAuthentication
    public IDatasetLocationNode tryGetDataSetLocation(final String dataSetCode)
            throws UserFailureException;

    /**
     * Tries to get the data set for the specified data set code, using the ETL server's session
     * token.
     */
    @ManagedAuthentication
    public ExternalData tryGetDataSet(final String dataSetCode) throws UserFailureException;

    /**
     * Tries to get the data set for the specified data set code and specified session.
     */
    @ManagedAuthentication
    public ExternalData tryGetDataSet(final String sessionToken, final String dataSetCode)
            throws UserFailureException;

    /**
     * Checks if the current user has INSTANCE_ADMIN access rights.
     */
    @ManagedAuthentication
    public void checkInstanceAdminAuthorization(String sessionToken) throws UserFailureException;

    /**
     * Checks if the current user has SPACE_POWER_USER access rights.
     */
    @ManagedAuthentication
    public void checkSpacePowerUserAuthorization(String sessionToken) throws UserFailureException;

    /**
     * Checks if the current user has access rights to a dataset with the specified data set code.
     */
    @ManagedAuthentication
    public void checkDataSetAccess(final String sessionToken, final String dataSetCode)
            throws UserFailureException;

    /**
     * Check which of the list of of data sets the current user can access.
     * 
     * @param sessionToken The user's session token.
     * @param dataSetCodes The data set codes the user wants to access.
     */
    @ManagedAuthentication
    public void checkDataSetCollectionAccess(String sessionToken, List<String> dataSetCodes)
            throws UserFailureException;

    /**
     * Tries to get the experiment of specified identifier or <code>null</code> if not found.
     */
    @ManagedAuthentication
    public Experiment tryToGetExperiment(ExperimentIdentifier experimentIdentifier)
            throws UserFailureException;

    /**
     * Tries to get the space of specified identifier or <code>null</code> if not found.
     */
    @ManagedAuthentication
    public Space tryGetSpace(SpaceIdentifier spaceIdentifier) throws UserFailureException;

    /**
     * Tries to get the project of specified identifier or <code>null</code> if not found.
     */
    @ManagedAuthentication
    public Project tryGetProject(ProjectIdentifier projectIdentifier) throws UserFailureException;

    /**
     * Gets all sample in accordance to the specified criteria.
     */
    @ManagedAuthentication
    public List<Sample> listSamples(final ListSampleCriteria criteria) throws UserFailureException;

    /**
     * Gets a sample with the specified identifier. Sample is enriched with properties and the
     * experiment with properties.
     * 
     * @return <code>null</code> if no sample could be found for given <var>sampleIdentifier</var>.
     */
    @ManagedAuthentication
    public Sample tryGetSampleWithExperiment(final SampleIdentifier sampleIdentifier)
            throws UserFailureException;

    /**
     * Tries to get the sample identifier for the sample with specified permanent ID.
     * 
     * @return <code>null</code> if nothing found.
     */
    @ManagedAuthentication
    public SampleIdentifier tryToGetSampleIdentifier(String samplePermID)
            throws UserFailureException;

    /**
     * For given {@link MaterialIdentifier} returns the corresponding {@link Material}.
     */
    @ManagedAuthentication
    public Material tryGetMaterial(MaterialIdentifier materialIdentifier);

    /**
     * For given (@code name} and {@code ownerId} returns the corresponding {@link Metaproject}
     */
    public Metaproject tryGetMetaproject(String name, String ownerId);

    /**
     * Gets the experiment type with assigned property types for the specified experiment type code.
     */
    @ManagedAuthentication
    public ExperimentType getExperimentType(String experimentTypeCode) throws UserFailureException;

    /**
     * Gets the sample type with assigned property types for the specified sample type code.
     */
    @ManagedAuthentication
    public SampleType getSampleType(String sampleTypeCode) throws UserFailureException;

    /**
     * Lists all data sets of the specified experiment ID.
     */
    @ManagedAuthentication
    public List<ExternalData> listDataSetsByExperimentID(long experimentID)
            throws UserFailureException;

    /**
     * Lists all data sets of the specified sample ID.
     * 
     * @param showOnlyDirectlyConnected If <code>true</code> only directly connected data sets are
     *            returned.
     */
    @ManagedAuthentication
    public List<ExternalData> listDataSetsBySampleID(long sampleID,
            boolean showOnlyDirectlyConnected) throws UserFailureException;

    /**
     * Returns all data sets found for specified data set codes.
     * 
     * @return plain data sets without properties, samples, and experiments.
     */
    @ManagedAuthentication
    public List<ExternalData> listDataSetsByCode(List<String> dataSetCodes)
            throws UserFailureException;

    /**
     * Registers the specified experiment.
     * 
     * @return the technical ID of the new experiment
     */
    @ManagedAuthentication
    public long registerExperiment(final NewExperiment experiment) throws UserFailureException;

    /**
     * Registers the specified sample.
     * 
     * @return the technical ID of the new sample
     */
    @ManagedAuthentication
    public long registerSample(final NewSample newSample, String userIDOrNull)
            throws UserFailureException;

    /**
     * Registers samples in batches.
     */
    @ManagedAuthentication
    public void registerSamples(List<NewSamplesWithTypes> newSamples, String userIDOrNull)
            throws UserFailureException;

    /**
     * Updates sample specified by the argument.
     */
    @ManagedAuthentication
    public void updateSample(SampleUpdatesDTO sampleUpdate) throws UserFailureException;

    /**
     * Registers the specified data.
     * <p>
     * As side effect, sets <i>data set code</i> in {@link DataSetInformation#getExtractableData()}.
     * </p>
     */
    @ManagedAuthentication
    public void registerDataSet(final DataSetInformation dataSetInformation,
            final NewExternalData data) throws UserFailureException;

    /**
     * Tries to return the properties of the top sample (e.g. master plate) registered for the
     * specified sample identifier.
     * 
     * @return <code>null</code> if no appropriated sample found. Returns an empty array if a a
     *         sample found with no properties.
     */
    @ManagedAuthentication
    public IEntityProperty[] getPropertiesOfTopSampleRegisteredFor(
            final SampleIdentifier sampleIdentifier) throws UserFailureException;

    /** See {@link IETLLIMSService#listSamplesByCriteria(String, ListSamplesByPropertyCriteria)} */
    @ManagedAuthentication
    public List<Sample> listSamplesByCriteria(final ListSamplesByPropertyCriteria criteria)
            throws UserFailureException;

    /** See {@link IETLLIMSService#listShareIds(String, String)} */
    @ManagedAuthentication
    public List<DataSetShareId> listDataSetShareIds() throws UserFailureException;

    /**
     * Returns informations about all file-content data sets which belong to the calling data store
     * server.
     */
    @ManagedAuthentication
    public List<SimpleDataSetInformationDTO> listDataSets() throws UserFailureException;

    /** @see IETLLIMSService#listDataSets(String, String, TrackingDataSetCriteria) */
    @ManagedAuthentication
    public List<ExternalData> listNewerDataSets(TrackingDataSetCriteria criteria)
            throws UserFailureException;

    /**
     * Creates and returns a new permanent ID that can be used to identify experiments, samples or
     * datasets.
     */
    @ManagedAuthentication
    public String createPermId();

    /**
     * Creates a new unique ID which can be used to create codes which are guaranteed to be unique.
     */
    @ManagedAuthentication
    public long drawANewUniqueID();

    /**
     * Creates a list of specified size with new unique ID for the specified entity kind.
     */
    @ManagedAuthentication
    public List<String> generateCodes(String prefix, EntityKind entityKind, int size);

    /**
     * Returns the version of the service.
     */
    @ManagedAuthentication
    public int getVersion();

    /**
     * Returns the home database instance.
     */
    @ManagedAuthentication
    public DatabaseInstance getHomeDatabaseInstance();

    /**
     * List data sets deleted after the last seen deletion event. If event id is null all deleted
     * datasets will be returned.
     * 
     * @param maxDeletionDateOrNull when specified only lists data sets that have been deleted
     *            before it.
     */
    @ManagedAuthentication
    public List<DeletedDataSet> listDeletedDataSets(Long lastSeenDeletionEventIdOrNull,
            Date maxDeletionDateOrNull);

    /**
     * Updates specified properties of given data set.
     */
    @ManagedAuthentication
    public void updateDataSet(String code, List<NewProperty> properties, SpaceIdentifier space)
            throws UserFailureException;

    /**
     * Updates share id and size of specified data set.
     */
    @ManagedAuthentication
    public void updateShareIdAndSize(String dataSetCode, String shareId, long size)
            throws UserFailureException;

    //
    // Archiving
    //

    /** See {@link IETLLIMSService#listAvailableDataSets(String, String, ArchiverDataSetCriteria)} */
    @ManagedAuthentication
    public List<ExternalData> listAvailableDataSets(ArchiverDataSetCriteria criteria)
            throws UserFailureException;

    /** See {@link IETLLIMSService#archiveDatasets(String, List, boolean)} */
    @ManagedAuthentication
    public void archiveDataSets(List<String> dataSetCodes, boolean removeFromDataStore)
            throws UserFailureException;

    /** See {@link IETLLIMSService#unarchiveDatasets(String, List)} */
    @ManagedAuthentication
    public void unarchiveDataSets(List<String> dataSetCodes) throws UserFailureException;

    /**
     * See
     * {@link IETLLIMSService#updateDataSetStatuses(String, List, DataSetArchivingStatus, boolean)}
     */
    @ManagedAuthentication
    public void updateDataSetStatuses(List<String> codes, DataSetArchivingStatus newStatus,
            boolean presentInArchive) throws UserFailureException;

    /**
     * See
     * {@link IETLLIMSService#updateDataSetStatuses(String, List, DataSetArchivingStatus, boolean)}
     */
    @ManagedAuthentication
    public boolean compareAndSetDataSetStatus(String dataSetCode, DataSetArchivingStatus oldStatus,
            DataSetArchivingStatus newStatus, boolean newPresentInArchive)
            throws UserFailureException;

    /** See {@link IETLLIMSService#checkSpaceAccess(String, SpaceIdentifier)} */
    @ManagedAuthentication
    public void checkSpaceAccess(String sToken, SpaceIdentifier spaceId);

    /**
     * See {@link IETLLIMSService#tryGetSession(String)}
     * 
     * @param sessionToken
     */
    @ManagedAuthentication
    public SessionContextDTO tryGetSession(String sessionToken);

    /**
     * See @{link {@link IETLLIMSService#checkSession(String)}.
     */
    @ManagedAuthentication
    public void checkSession(String sessionToken);

    /**
     * Return a list of users who could be considered administrators. See
     * {@link IETLLIMSService#listAdministrators(String)}
     */
    @ManagedAuthentication
    public List<Person> listAdministrators();

    /**
     * Return the user that matches this username or email. See
     * {@link IETLLIMSService#tryPersonWithUserIdOrEmail(String, String)}
     */
    @ManagedAuthentication
    public Person tryPersonWithUserIdOrEmail(String useridOrEmail);

    /**
     * Register a new sample and data set in one transaction. Return the sample.
     * {@link IETLLIMSService#registerSampleAndDataSet(String, NewSample, NewExternalData, String)}
     */
    @ManagedAuthentication
    public Sample registerSampleAndDataSet(NewSample newSample, NewExternalData externalData,
            String userIdOrNull);

    /**
     * Update a sample and register a data set in one transaction. Return the sample.
     * {@link IETLLIMSService#updateSampleAndRegisterDataSet(String, SampleUpdatesDTO, NewExternalData)}
     */
    @ManagedAuthentication
    public Sample updateSampleAndRegisterDataSet(SampleUpdatesDTO newSample,
            NewExternalData externalData);

    /**
     * {@link IETLLIMSService#performEntityOperations(String, AtomicEntityOperationDetails)}
     */
    @ManagedAuthentication
    public AtomicEntityOperationResult performEntityOperations(
            AtomicEntityOperationDetails operationDetails);

    /**
     * {@link IETLLIMSService#listProjects(String)}
     */
    @ManagedAuthentication
    public List<Project> listProjects();

    /**
     * {@link IETLLIMSService#removeDataSetsPermanently(String, List, String)}
     */
    @ManagedAuthentication
    public void removeDataSetsPermanently(List<String> dataSetCodes, String reason);

    /**
     * {@link IETLLIMSService#updateDataSet(String, DataSetUpdatesDTO)}
     */
    @ManagedAuthentication
    public void updateDataSet(DataSetUpdatesDTO dataSetUpdates);

    /**
     * {@link IETLLIMSService#getTrustedCrossOriginDomains(String)}
     */
    @ManagedAuthentication
    public List<String> getTrustedCrossOriginDomains();

    /**
     * {@link IETLLIMSService#setStorageConfirmed(String, String)}
     */
    @ManagedAuthentication
    public void setStorageConfirmed(String dataSetCode);

    /**
     * {@link IETLLIMSService#markSuccessfulPostRegistration(String, String)}
     */
    @ManagedAuthentication
    public void markSuccessfulPostRegistration(String dataSetCode);

    /**
     * {@link IETLLIMSService#listDataSetsForPostRegistration(String, String)}
     */
    @ManagedAuthentication
    public List<ExternalData> listDataSetsForPostRegistration();

    /**
     * {@link IETLLIMSService#didEntityOperationsSucceed(String, TechId)}
     */
    @ManagedAuthentication
    public EntityOperationsState didEntityOperationsSucceed(TechId registrationId);

    /**
     * {@link IETLLIMSService#heartbeat(String)}
     */
    @ManagedAuthentication
    public void heartbeat();

    /**
     * {@link IETLLIMSService#doesUserHaveRole(String, String, String, String)}
     */
    @ManagedAuthentication
    public boolean doesUserHaveRole(String user, String roleCode, String spaceOrNull);

    /**
     * {@link IETLLIMSService#filterToVisibleDataSets(String, String, List)}
     */
    @ManagedAuthentication
    public List<String> filterToVisibleDataSets(String user, List<String> dataSetCodes);

    /**
     * {@link IETLLIMSService#filterToVisibleExperiments(String, String, List)}
     */
    @ManagedAuthentication
    public List<String> filterToVisibleExperiments(String user, List<String> experimentIds);

    /**
     * {@link IETLLIMSService#filterToVisibleSamples(String, String, List)}
     */
    @ManagedAuthentication
    public List<String> filterToVisibleSamples(String user, List<String> sampleIdentifiers);

    /**
     * For given code returns the corresponding {@link ExternalDataManagementSystem}.
     */
    @ManagedAuthentication
    public ExternalDataManagementSystem tryGetExternalDataManagementSystem(
            String externalDataManagementSystemCode);

}