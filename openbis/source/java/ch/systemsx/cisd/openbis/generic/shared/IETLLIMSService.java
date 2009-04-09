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

import javax.servlet.http.HttpServletRequest;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.authorization.ISessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleOwnerIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * <b>LIMS</b> <i>Web Service</i> interface for the <b>ETL</b> (<i>Extract, Transform, Load</i>)
 * server.
 * 
 * @author Christian Ribeaud
 */
public interface IETLLIMSService extends IWebService, ISessionProvider
{
    /**
     * Returns the home database instance.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.ETL_SERVER)
    public DatabaseInstancePE getHomeDatabaseInstance(final String sessionToken);
    
    /**
     * Registers a Data Store Server for the specified port and DSS session token. 
     * It should be reachable via this port and the DSS session token should be used when invoking
     * methods. Note, that the host can be inferred by the asking {@link HttpServletRequest}.
     */
    @Transactional
    @RolesAllowed(RoleSet.ETL_SERVER)
    public void registerDataStoreServer(String sessionToken, int port, String dssSessionToken);

    /**
     * Gets an {@link ExperimentPE} object specified by experiment ID and sample code.
     * 
     * @param sessionToken the user authentication token. Must not be <code>null</code>.
     * @param sampleIdentifier an identifier which uniquely identifies the sample.
     * @return <code>null</code> if no experiment could be found for given <var>sampleIdentifier</var>.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.ETL_SERVER)
    public ExperimentPE tryToGetBaseExperiment(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            final SampleIdentifier sampleIdentifier) throws UserFailureException;

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
    public SamplePropertyPE[] tryToGetPropertiesOfTopSampleRegisteredFor(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            final SampleIdentifier sampleIdentifier) throws UserFailureException;

    /**
     * Registers the specified data.
     * 
     * @param sessionToken The user authentication token. Must not be <code>null</code>.
     * @param sampleIdentifier an identifier which uniquely identifies the sample.
     * @param externalData Data set to be registered. It is assumed that the attributes
     *            <code>location</code>, <code>fileFormatType</code>, <code>dataSetType</code>,
     *            and <code>locatorType</code> are not-<code>null</code>.
     * @throws UserFailureException if given data set code could not be found in the persistence
     *             layer.
     */
    @Transactional
    @RolesAllowed(RoleSet.ETL_SERVER)
    public void registerDataSet(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            final SampleIdentifier sampleIdentifier, final ExternalData externalData)
            throws UserFailureException;
    
    /**
     * Tries to return the data set specified by its code.
     */
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    public ExternalDataPE tryGetDataSet(String sessionToken, String dataSetCode)
            throws UserFailureException;

    /**
     * Creates and returns a unique code for a new data set.
     */
    @Transactional
    @RolesAllowed(RoleSet.ETL_SERVER)
    public String createDataSetCode(final String sessionToken) throws UserFailureException;

}