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

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * This interface is very similar to {@link IETLLIMSService} but <code>sessionToken</code> has
 * been removed from most methods.
 * 
 * @see IETLLIMSService
 * @author Christian Ribeaud
 */
public interface IEncapsulatedOpenBISService
{
    /**
     * Registers this Data Set Server at its openBIS Server.
     */
    public void registerAtOpenBIS();
    
    /**
     * Tries to get the data set for the specified data set code.
     */
    public ExternalDataPE tryGetDataSet(final String sessionToken, final String dataSetCode)
            throws UserFailureException;
    
    /**
     * For given <var>dataSetInfo</var> returns the <code>BaseExperiment</code> object.
     */
    public ExperimentPE getBaseExperiment(final SampleIdentifier sampleIdentifier)
            throws UserFailureException;

    /**
     * Registers the specified data.
     * <p>
     * As side effect, sets <i>data set code</i> in {@link DataSetInformation#getExtractableData()}.
     * </p>
     */
    public void registerDataSet(final DataSetInformation dataSetInformation,
            final String procedureTypeCode, final ExternalData data) throws UserFailureException;

    /**
     * Tries to return the properties of the top sample (e.g. master plate) registered for the
     * specified sample identifier.
     * 
     * @return <code>null</code> if no appropriated sample found. Returns an empty array if a a
     *         sample found with no properties.
     */
    public SamplePropertyPE[] getPropertiesOfTopSampleRegisteredFor(
            final SampleIdentifier sampleIdentifier) throws UserFailureException;

    /**
     * Creates and returns a unique code for a new data set.
     */
    public String createDataSetCode();

    /**
     * Returns the version of the service.
     */
    public int getVersion();
    
    /**
     * Returns the home database instance.
     */
    public DatabaseInstancePE getHomeDatabaseInstance();
}