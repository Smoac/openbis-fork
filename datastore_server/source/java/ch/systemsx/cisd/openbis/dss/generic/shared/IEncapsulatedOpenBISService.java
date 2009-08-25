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

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * This interface is very similar to {@link IETLLIMSService} but <code>sessionToken</code> has been
 * removed from most methods.
 * 
 * @see IETLLIMSService
 * @author Christian Ribeaud
 */
public interface IEncapsulatedOpenBISService
{
    /**
     * Tries to get the data set for the specified data set code.
     */
    public ExternalDataPE tryGetDataSet(final String sessionToken, final String dataSetCode)
            throws UserFailureException;

    /**
     * Gets a sample with the specified identifier. Sample is enriched with properties and the
     * experiment with properties.
     * 
     * @return <code>null</code> if no sample could be found for given <var>sampleIdentifier</var>.
     */
    public SamplePE tryGetSampleWithExperiment(final SampleIdentifier sampleIdentifier)
            throws UserFailureException;

    /**
     * Registers the specified data.
     * <p>
     * As side effect, sets <i>data set code</i> in {@link DataSetInformation#getExtractableData()}.
     * </p>
     */
    public void registerDataSet(final DataSetInformation dataSetInformation, final NewExternalData data)
            throws UserFailureException;

    /**
     * Tries to return the properties of the top sample (e.g. master plate) registered for the
     * specified sample identifier.
     * 
     * @return <code>null</code> if no appropriated sample found. Returns an empty array if a a
     *         sample found with no properties.
     */
    public SamplePropertyPE[] getPropertiesOfTopSampleRegisteredFor(
            final SampleIdentifier sampleIdentifier) throws UserFailureException;

    /** See {@link IETLLIMSService#listSamplesByCriteria(String, ListSamplesByPropertyCriteria)} */
    public List<String> listSamplesByCriteria(final ListSamplesByPropertyCriteria criteria)
            throws UserFailureException;

    /** See {@link IETLLIMSService#listDataSets(String, String)} */
    public List<SimpleDataSetInformationDTO> listDataSets() throws UserFailureException;

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

    public SamplePE tryToGetSampleWithProperty(String string, GroupIdentifier groupIdentifier,
            String parameterName);
}