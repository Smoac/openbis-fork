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

package ch.systemsx.cisd.openbis.generic.shared;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.conversation.ConversationalRmiServer;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.authorization.ISessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AtomicOperationsPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.DataSetCodeCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.DataSetCodePredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.DataSetUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.ExistingSampleOwnerIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.ExistingSpaceIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.ListSampleCriteriaPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.ListSamplesByPropertyPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NewExperimentPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NewSamplePredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NewSamplesWithTypePredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleOwnerIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SpaceIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.ProjectValidator;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.SampleValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.EntityOperationsState;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentFetchOptions;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetShareId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityCollectionForCreationOrUpdate;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * <b>LIMS</b> <i>Web Service</i> interface for the <b>ETL</b> (<i>Extract, Transform, Load</i>)
 * server.
 * 
 * @author Christian Ribeaud
 */
public interface IETLLIMSService extends IServer, ISessionProvider, ConversationalRmiServer
{
    /**
     * Returns the home database instance.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public DatabaseInstance getHomeDatabaseInstance(final String sessionToken);

    /**
     * Registers a Data Store Server for the specified info.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void registerDataStoreServer(String sessionToken, DataStoreServerInfo dataStoreServerInfo);

    /**
     * Returns the specified experiment or <code>null</code> if not found.
     * 
     * @param sessionToken the user authentication token. Must not be <code>null</code>.
     * @param experimentIdentifier an identifier which uniquely identifies the experiment.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Experiment tryToGetExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = ExistingSpaceIdentifierPredicate.class)
            ExperimentIdentifier experimentIdentifier) throws UserFailureException;

    /**
     * For given {@link MaterialIdentifier} returns the corresponding {@link Material}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Material tryGetMaterial(String sessionToken, MaterialIdentifier materialIdentifier);

    /**
     * Tries to get the identifier of sample with specified permanent ID.
     * 
     * @return <code>null</code> if nothing found.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public SampleIdentifier tryToGetSampleIdentifier(String sessionToken, String samplePermID)
            throws UserFailureException;

    /**
     * Returns the ExperimentType together with assigned property types for specified experiment
     * type code.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public ExperimentType getExperimentType(String sessionToken, String experimentTypeCode)
            throws UserFailureException;

    /**
     * Gets a sample with the specified identifier. Sample is enriched with properties and the
     * experiment with properties.
     * 
     * @param sessionToken the user authentication token. Must not be <code>null</code>.
     * @param sampleIdentifier an identifier which uniquely identifies the sample.
     * @return <code>null</code> if no sample attached to an experiment could be found for given
     *         <var>sampleIdentifier</var>.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Sample tryGetSampleWithExperiment(final String sessionToken,
            @AuthorizationGuard(guardClass = ExistingSampleOwnerIdentifierPredicate.class)
            final SampleIdentifier sampleIdentifier) throws UserFailureException;

    /**
     * Returns a list of terms belonging to given vocabulary.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Collection<VocabularyTerm> listVocabularyTerms(String sessionToken, String vocabulary)
            throws UserFailureException;

    /**
     * Returns the SampleType together with assigned property types for specified sample type code.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public SampleType getSampleType(String sessionToken, String sampleTypeCode)
            throws UserFailureException;

    /**
     * Returns the data set type together with assigned property types for specified data set type
     * code.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public DataSetTypeWithVocabularyTerms getDataSetType(String sessionToken, String dataSetTypeCode)
            throws UserFailureException;

    /**
     * For given experiment {@link TechId} returns the corresponding list of {@link ExternalData}.
     * 
     * @return a sorted list of {@link ExternalData}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<ExternalData> listDataSetsByExperimentID(final String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class)
            final TechId experimentID) throws UserFailureException;

    /**
     * For given sample {@link TechId} returns the corresponding list of {@link ExternalData}.
     * 
     * @return a sorted list of {@link ExternalData}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<ExternalData> listDataSetsBySampleID(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class)
            final TechId sampleId, final boolean showOnlyDirectlyConnected)
            throws UserFailureException;

    /**
     * Returns all data sets found for specified data set codes.
     * 
     * @return plain data sets without properties, samples, and experiments.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<ExternalData> listDataSetsByCode(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class)
            List<String> dataSetCodes) throws UserFailureException;

    /**
     * Lists samples using given configuration.
     * 
     * @return a sorted list of {@link Sample}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @ReturnValueFilter(validatorClass = SampleValidator.class)
    public List<Sample> listSamples(final String sessionToken,
            @AuthorizationGuard(guardClass = ListSampleCriteriaPredicate.class)
            final ListSampleCriteria criteria);

    /**
     * Tries to return the properties of the top sample (e.g. master plate) registered for the
     * specified sample code. If sample has no top sample, its own properties are returned.
     * 
     * @param sessionToken the user authentication token. Must not be <code>null</code>.
     * @param sampleIdentifier an identifier which uniquely identifies the sample.
     * @return <code>null</code> if no appropriated sample found. Returns an empty array if a a
     *         sample found with no properties.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public IEntityProperty[] tryToGetPropertiesOfTopSampleRegisteredFor(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            final SampleIdentifier sampleIdentifier) throws UserFailureException;

    /**
     * Registers/updates various entities in one transaction.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @DatabaseCreateOrDeleteModification(value =
        { ObjectKind.EXPERIMENT, ObjectKind.SAMPLE, ObjectKind.DATA_SET })
    public void registerEntities(String sessionToken, EntityCollectionForCreationOrUpdate collection)
            throws UserFailureException;

    /**
     * Registers experiment.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.EXPERIMENT)
    public long registerExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = NewExperimentPredicate.class)
            NewExperiment experiment) throws UserFailureException;

    /**
     * Registers samples in batches.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public void registerSamples(final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplesWithTypePredicate.class)
            final List<NewSamplesWithTypes> newSamplesWithType, String userIdOrNull)
            throws UserFailureException;

    /**
     * Registers a new sample.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SAMPLE)
    public long registerSample(final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplePredicate.class)
            final NewSample newSample, String userIDOrNull) throws UserFailureException;

    /**
     * Saves changed sample.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public void updateSample(String sessionToken,
            @AuthorizationGuard(guardClass = SampleUpdatesPredicate.class)
            SampleUpdatesDTO updates);

    /**
     * Registers the specified data connected to a sample.
     * 
     * @param sessionToken The user authentication token. Must not be <code>null</code>.
     * @param sampleIdentifier an identifier which uniquely identifies the sample.
     * @param externalData Data set to be registered. It is assumed that the attributes
     *            <code>location</code>, <code>fileFormatType</code>, <code>dataSetType</code>, and
     *            <code>locatorType</code> are not-<code>null</code>.
     * @throws UserFailureException if given data set code could not be found in the persistence
     *             layer.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DATA_SET)
    public void registerDataSet(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            final SampleIdentifier sampleIdentifier, final NewExternalData externalData)
            throws UserFailureException;

    /**
     * Registers the specified data connected to an experiment.
     * 
     * @param sessionToken The user authentication token. Must not be <code>null</code>.
     * @param experimentIdentifier an identifier which uniquely identifies the experiment.
     * @param externalData Data set to be registered. It is assumed that the attributes
     *            <code>location</code>, <code>fileFormatType</code>, <code>dataSetType</code>, and
     *            <code>locatorType</code> are not-<code>null</code>.
     * @throws UserFailureException if given data set code could not be found in the persistence
     *             layer.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DATA_SET)
    public void registerDataSet(final String sessionToken,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class)
            final ExperimentIdentifier experimentIdentifier, final NewExternalData externalData)
            throws UserFailureException;

    /**
     * Checks that the user of specified session has INSTANCE_ADMIN access rights.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void checkInstanceAdminAuthorization(String sessionToken) throws UserFailureException;

    /**
     * Checks that the user of specified session has SPACE_POWER_USER access rights.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_POWER_USER)
    public void checkSpacePowerUserAuthorization(String sessionToken) throws UserFailureException;

    /**
     * Does nothing besides checking that the current user has rights to access the content of the
     * dataset.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public void checkDataSetAccess(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class)
            String dataSetCode) throws UserFailureException;

    /**
     * Check if the current user can access all the data sets in the list
     * 
     * @param sessionToken The user's session token.
     * @param dataSetCodes The data set codes the user wants to access.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public void checkDataSetCollectionAccess(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class)
            List<String> dataSetCodes);

    /**
     * Tries to return the data set specified by its code.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public ExternalData tryGetDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class)
            String dataSetCode) throws UserFailureException;

    /**
     * Creates and returns a unique code for a new data set. TODO KE: 2011-04-19 remove this method.
     * It is equivalent to "createPermId()"
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public String createDataSetCode(final String sessionToken) throws UserFailureException;

    /**
     * Create and return a new permanent id that can be used to identify samples, experiments etc.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public String createPermId(final String sessionToken) throws UserFailureException;

    /**
     * Draw a new unique ID. The returned value can be used as a part of a code for samples,
     * experiments etc. which is guaranteed to be unique.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public long drawANewUniqueID(String sessionToken) throws UserFailureException;

    /**
     * Lists samples codes filtered by specified criteria, see {@link ListSamplesByPropertyCriteria}
     * to see the details.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Sample> listSamplesByCriteria(final String sessionToken,
            @AuthorizationGuard(guardClass = ListSamplesByPropertyPredicate.class)
            final ListSamplesByPropertyCriteria criteria) throws UserFailureException;

    /**
     * Lists share ids of all data sets belonging to chosen data store (even the ones in trash!).
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<DataSetShareId> listShareIds(final String sessionToken, String dataStore)
            throws UserFailureException;

    /**
     * Lists data sets belonging to chosen data store.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<SimpleDataSetInformationDTO> listDataSets(final String sessionToken,
            String dataStore) throws UserFailureException;

    /**
     * List data sets deleted after specified date.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<DeletedDataSet> listDeletedDataSets(String sessionToken,
            Long lastSeenDeletionEventIdOrNull, Date maxDeletionDataOrNull);

    /**
     * List 'AVAILABLE' data sets (not locked) that match given criteria.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<ExternalData> listAvailableDataSets(String sessionToken, String dataStoreCode,
            ArchiverDataSetCriteria criteria);

    /**
     * List data sets from specified store which are younger then the specified one.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<ExternalData> listDataSets(String sessionToken, String dataStoreCode,
            TrackingDataSetCriteria criteria);

    /**
     * List all experiments for a given project identifier.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Experiment> listExperiments(String sessionToken,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class)
            ProjectIdentifier projectIdentifier);

    /**
     * List experiments for a given list of experiment identifiers.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Experiment> listExperiments(String sessionToken,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class)
            List<ExperimentIdentifier> experimentIdentifiers,
            ExperimentFetchOptions experimentFetchOptions);

    /**
     * List experiments for a given list of project identifiers.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Experiment> listExperimentsForProjects(String sessionToken,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class)
            List<ProjectIdentifier> projectIdentifiers,
            ExperimentFetchOptions experimentFetchOptions);

    /**
     * List all projects that the user can see.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @ReturnValueFilter(validatorClass = ProjectValidator.class)
    public List<Project> listProjects(String sessionToken);

    /**
     * Lists materials using given criteria.
     * 
     * @return a sorted list of {@link Material}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Material> listMaterials(String sessionToken, ListMaterialCriteria criteria,
            boolean withProperties);

    /**
     * Adds specified properties of given data set. Properties defined before will not be updated.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public void addPropertiesToDataSet(String sessionToken, List<NewProperty> properties,
            String dataSetCode, @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class)
            final SpaceIdentifier identifier) throws UserFailureException;

    /**
     * Updates share id and size of specified data set.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public void updateShareIdAndSize(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class)
            String dataSetCode, String shareId, long size) throws UserFailureException;

    /**
     * Updates status of given data sets.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public void updateDataSetStatuses(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class)
            List<String> dataSetCodes, final DataSetArchivingStatus newStatus,
            boolean presentInArchive) throws UserFailureException;

    /**
     * Set the status for a given dataset to the given new status value if the current status equals
     * an expected value.
     * 
     * @return true if the update is successful, false if the current status is different than
     *         <code>oldStatus</code>.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public boolean compareAndSetDataSetStatus(String token, String dataSetCode,
            DataSetArchivingStatus oldStatus, DataSetArchivingStatus newStatus,
            boolean newPresentInArchive) throws UserFailureException;

    /**
     * Schedules archiving of specified data sets.
     * 
     * @param removeFromDataStore when set to <code>true</code> the data sets will be removed from
     *            the data store after a successful archiving operation.
     * @return number of data sets scheduled for archiving.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public int archiveDatasets(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class)
            List<String> datasetCodes, boolean removeFromDataStore);

    /**
     * Schedules unarchiving of specified data sets.
     * 
     * @return number of data sets scheduled for unarchiving.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public int unarchiveDatasets(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class)
            List<String> datasetCodes);

    /**
     * Check if the user has USER access on the space
     * 
     * @param sessionToken The user's session token.
     * @param spaceId The id for the space the user wants to access
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public void checkSpaceAccess(String sessionToken,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class)
            SpaceIdentifier spaceId);

    /**
     * For the ETL Server to get data sets.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public ExternalData tryGetDataSetForServer(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class)
            String dataSetCode) throws UserFailureException;

    /**
     * Returns a list of unique codes.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<String> generateCodes(String sessionToken, String prefix, int number);

    /**
     * Returns a list users who could be considered administrators.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<Person> listAdministrators(String sessionToken);

    /**
     * Search for the person that matches the given userId or email. The search first tries to find
     * a userId match; if none was found, it searches for an Email match.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Person tryPersonWithUserIdOrEmail(String sessionToken, String useridOrEmail);

    /**
     * Registers a sample and data set connected to that sample in one transaction.
     * 
     * @param sessionToken The user authentication token. Must not be <code>null</code>.
     * @param newSample The new sample to register.
     * @param externalData Data set to be registered. It is assumed that the attributes
     *            <code>location</code>, <code>fileFormatType</code>, <code>dataSetType</code>, and
     *            <code>locatorType</code> are not-<code>null</code>.
     * @param userIdOrNull The user id on whose behalf we are registering the sample
     * @throws UserFailureException if given data set code could not be found in the persistence
     *             layer.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @DatabaseCreateOrDeleteModification(value =
        { ObjectKind.SAMPLE, ObjectKind.DATA_SET })
    public Sample registerSampleAndDataSet(final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplePredicate.class)
            final NewSample newSample, final NewExternalData externalData, String userIdOrNull)
            throws UserFailureException;

    /**
     * Updates a sample and registers a data set connected to that sample in one transaction.
     * 
     * @param sessionToken The user authentication token. Must not be <code>null</code>.
     * @param updates The sample updates to apply
     * @param externalData Data set to be registered. It is assumed that the attributes
     *            <code>location</code>, <code>fileFormatType</code>, <code>dataSetType</code>, and
     *            <code>locatorType</code> are not-<code>null</code>.
     * @throws UserFailureException if given data set code could not be found in the persistence
     *             layer.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DATA_SET)
    public Sample updateSampleAndRegisterDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = SampleUpdatesPredicate.class)
            SampleUpdatesDTO updates, NewExternalData externalData);

    /**
     * Updates a sample and registers a data set connected to that sample in one transaction.
     * 
     * @param sessionToken The user authentication token. Must not be <code>null</code>.
     * @param operationDetails A DTO containing information about the entities to change / register.
     * @throws UserFailureException if given data set code could not be found in the persistence
     *             layer.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @DatabaseUpdateModification(value =
        { ObjectKind.SAMPLE, ObjectKind.EXPERIMENT, ObjectKind.DATA_SET })
    @DatabaseCreateOrDeleteModification(value =
        { ObjectKind.SPACE, ObjectKind.PROJECT, ObjectKind.SAMPLE, ObjectKind.EXPERIMENT,
                ObjectKind.DATA_SET })
    public AtomicEntityOperationResult performEntityOperations(String sessionToken,
            @AuthorizationGuard(guardClass = AtomicOperationsPredicate.class)
            AtomicEntityOperationDetails operationDetails);

    /**
     * Tries to return the space specified by its identifier.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_ETL_SERVER })
    public Space tryGetSpace(String sessionToken,
            @AuthorizationGuard(guardClass = ExistingSpaceIdentifierPredicate.class)
            SpaceIdentifier spaceIdentifier);

    /**
     * Tries to return the project specified by its identifier.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_ETL_SERVER })
    public Project tryGetProject(String sessionToken,
            @AuthorizationGuard(guardClass = ExistingSpaceIdentifierPredicate.class)
            ProjectIdentifier projectIdentifier);

    /**
     * Search for samples matching the provided criteria.
     * 
     * @param sessionToken The user authentication token. Must not be <code>null</code>.
     * @param searchCriteria The criteria for samples.
     * @return A collection of samples matching the search criteria.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<Sample> searchForSamples(String sessionToken, SearchCriteria searchCriteria);

    /**
     * Search for data sets matching the provided criteria.
     * 
     * @param sessionToken The user authentication token. Must not be <code>null</code>.
     * @param searchCriteria The criteria for data sets.
     * @return A collection of data sets matching the search criteria.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<ExternalData> searchForDataSets(String sessionToken, SearchCriteria searchCriteria);

    /**
     * permanently deletes a list of data sets.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @DatabaseUpdateModification(value =
        { ObjectKind.SAMPLE, ObjectKind.EXPERIMENT })
    @DatabaseCreateOrDeleteModification(value =
        { ObjectKind.DATA_SET })
    public void removeDataSetsPermanently(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class)
            List<String> dataSetCodes, String reason);

    /**
     * updates a data set.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @DatabaseUpdateModification(value =
        { ObjectKind.EXPERIMENT, ObjectKind.SAMPLE, ObjectKind.DATA_SET })
    public void updateDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetUpdatesPredicate.class)
            DataSetUpdatesDTO dataSetUpdates);

    /**
     * Returns a list of configured trusted domains which can host external shared web resources.
     * Typically these are lightweight webapps that integrate with openBIS via JSON-RPC services.
     * <p>
     * Can return empty list.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<String> getTrustedCrossOriginDomains(String sessionToken);

    /**
     * Marks the storage of dataset as confirmed. Adds the given dataset to post-registration queue
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @DatabaseUpdateModification(value =
        { ObjectKind.DATA_SET })
    @DatabaseCreateOrDeleteModification(value =
        { ObjectKind.POSTREGISTRATION_QUEUE })
    public void setStorageConfirmed(String sessionToken, String dataSetCode);

    /**
     * Informs that the post-registration task for a given dataset was performed, and it should be
     * removed from the post-registration queue.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @DatabaseCreateOrDeleteModification(value =
        { ObjectKind.POSTREGISTRATION_QUEUE })
    public void markSuccessfulPostRegistration(String token, String dataSetCode);

    /**
     * Gets the list of all datasets, which are in the post-registration queue.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<ExternalData> listDataSetsForPostRegistration(String token, String dataStoreCode);

    /**
     * Return true if the log indicates that the performEntityOperations invocation for the given
     * registrationId succeeded.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public EntityOperationsState didEntityOperationsSucceed(String token, TechId registrationId);

    /**
     * Method that does nothing. Use it to check if the connection to the server is working.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void heartbeat(String token);

    /**
     * Check whether the specified user has the given role
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public boolean doesUserHaveRole(String token, String user, String roleCode, String spaceOrNull);

    /**
     * Filter list of datasets to only those visible by the given user
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<String> filterToVisibleDataSets(String token, String user, List<String> dataSetCodes);

    /**
     * Filter list of experiments to only those visible by the given user
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<String> filterToVisibleExperiments(String token, String user,
            List<String> experimentIds);

    /**
     * Filter list of samples to only those visible by the given user
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<String> filterToVisibleSamples(String token, String user,
            List<String> samplesIndentifiers);

}
