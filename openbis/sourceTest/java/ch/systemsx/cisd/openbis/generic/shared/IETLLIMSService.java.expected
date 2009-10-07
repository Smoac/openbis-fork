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

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.authorization.ISessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.GroupIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.ListSampleCriteriaPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.ListSamplesByPropertyPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleOwnerIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * <b>LIMS</b> <i>Web Service</i> interface for the <b>ETL</b> (<i>Extract, Transform, Load</i>)
 * server.
 * 
 * @author Christian Ribeaud
 */
public interface IETLLIMSService extends IServer, ISessionProvider
{
    /**
     * Returns the home database instance.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.ETL_SERVER)
    public DatabaseInstance getHomeDatabaseInstance(final String sessionToken);

    /**
     * Registers a Data Store Server for the specified info.
     */
    @Transactional
    @RolesAllowed(RoleSet.ETL_SERVER)
    public void registerDataStoreServer(String sessionToken, DataStoreServerInfo dataStoreServerInfo);

    /**
     * Returns the specified experiment or <code>null</code> if not found. 
     * 
     * @param sessionToken the user authentication token. Must not be <code>null</code>.
     * @param experimentIdentifier an identifier which uniquely identifies the experiment.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.ETL_SERVER)
    public Experiment tryToGetExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class)
            ExperimentIdentifier experimentIdentifier) throws UserFailureException;
    
    /**
     * Gets a sample with the specified identifier. Sample is enriched with properties and the
     * experiment with properties.
     * 
     * @param sessionToken the user authentication token. Must not be <code>null</code>.
     * @param sampleIdentifier an identifier which uniquely identifies the sample.
     * @return <code>null</code> if no sample could be found for given <var>sampleIdentifier</var>.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.ETL_SERVER)
    public Sample tryGetSampleWithExperiment(
            final String sessionToken,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class) final SampleIdentifier sampleIdentifier)
            throws UserFailureException;

    /**
     * Lists samples using given configuration.
     * 
     * @return a sorted list of {@link Sample}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.ETL_SERVER)
    public List<Sample> listSamples(
            final String sessionToken,
            @AuthorizationGuard(guardClass = ListSampleCriteriaPredicate.class) final ListSampleCriteria criteria);

    /**
     * Tries to return the properties of the top sample (e.g. master plate) registered for the
     * specified sample code.
     * 
     * @param sessionToken the user authentication token. Must not be <code>null</code>.
     * @param sampleIdentifier an identifier which uniquely identifies the sample.
     * @return <code>null</code> if no appropriated sample found. Returns an empty array if a a
     *         sample found with no properties.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.ETL_SERVER)
    public IEntityProperty[] tryToGetPropertiesOfTopSampleRegisteredFor(
            final String sessionToken,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class) final SampleIdentifier sampleIdentifier)
            throws UserFailureException;

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
    @RolesAllowed(RoleSet.ETL_SERVER)
    public void registerDataSet(
            final String sessionToken,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class) final SampleIdentifier sampleIdentifier,
            final NewExternalData externalData) throws UserFailureException;

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
    @RolesAllowed(RoleSet.ETL_SERVER)
    public void registerDataSet(
            final String sessionToken,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class) final ExperimentIdentifier experimentIdentifier,
            final NewExternalData externalData) throws UserFailureException;

    /**
     * Tries to return the data set specified by its code.
     */
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    public ExternalData tryGetDataSet(String sessionToken, String dataSetCode)
            throws UserFailureException;

    /**
     * Creates and returns a unique code for a new data set.
     */
    @Transactional
    @RolesAllowed(RoleSet.ETL_SERVER)
    public String createDataSetCode(final String sessionToken) throws UserFailureException;

    /**
     * Lists samples codes filtered by specified criteria, see {@link ListSamplesByPropertyCriteria}
     * to see the details.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.ETL_SERVER)
    public List<Sample> listSamplesByCriteria(
            final String sessionToken,
            @AuthorizationGuard(guardClass = ListSamplesByPropertyPredicate.class) final ListSamplesByPropertyCriteria criteria)
            throws UserFailureException;

    /**
     * Lists data sets belonging to chosen data store.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.ETL_SERVER)
    public List<SimpleDataSetInformationDTO> listDataSets(final String sessionToken,
            String dataStore) throws UserFailureException;

    /**
     * List data sets deleted after specified date.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.ETL_SERVER)
    public List<DeletedDataSet> listDeletedDataSets(String sessionToken,
            Long lastSeenDeletionEventIdOrNull);

}
