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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivizationStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

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
    public ExternalData tryGetDataSet(final String sessionToken, final String dataSetCode)
            throws UserFailureException;

    /**
     * Checks if the current user has access rights to a dataset with the specified data set code.
     */
    public void checkDataSetAccess(final String sessionToken, final String dataSetCode)
            throws UserFailureException;

    /**
     * Tries to get the experiment of specified identifier or <code>null</code> if not found.
     */
    public Experiment tryToGetExperiment(ExperimentIdentifier experimentIdentifier)
            throws UserFailureException;

    /**
     * Gets all sample in accordance to the specified criteria.
     */
    public List<Sample> listSamples(final ListSampleCriteria criteria) throws UserFailureException;

    /**
     * Gets a sample with the specified identifier. Sample is enriched with properties and the
     * experiment with properties.
     * 
     * @return <code>null</code> if no sample could be found for given <var>sampleIdentifier</var>.
     */
    public Sample tryGetSampleWithExperiment(final SampleIdentifier sampleIdentifier)
            throws UserFailureException;

    /**
     * Gets the sample type with assigned property types for the specified sample type code.
     */
    public SampleType getSampleType(String sampleTypeCode) throws UserFailureException;

    /**
     * Returns the data set type together with assigned property types for the specified data set
     * type code.
     */
    public DataSetTypeWithVocabularyTerms getDataSetType(String dataSetTypeCode);

    /**
     * Lists all data sets of the specified sample ID.
     * 
     * @param showOnlyDirectlyConnected If <code>true</code> only directly connected data sets are
     *            returned.
     */
    public List<ExternalData> listDataSetsBySampleID(long sampleID,
            boolean showOnlyDirectlyConnected) throws UserFailureException;

    /**
     * Registers the specified experiment.
     * 
     * @return the technical ID of the new experiment
     */
    public long registerExperiment(final NewExperiment experiment) throws UserFailureException;

    /**
     * Registers the specified sample.
     * 
     * @return the technical ID of the new sample
     */
    public long registerSample(final NewSample newSample, String userIDOrNull)
            throws UserFailureException;

    /**
     * Registers the specified data.
     * <p>
     * As side effect, sets <i>data set code</i> in {@link DataSetInformation#getExtractableData()}.
     * </p>
     */
    public void registerDataSet(final DataSetInformation dataSetInformation,
            final NewExternalData data) throws UserFailureException;

    /**
     * Tries to return the properties of the top sample (e.g. master plate) registered for the
     * specified sample identifier.
     * 
     * @return <code>null</code> if no appropriated sample found. Returns an empty array if a a
     *         sample found with no properties.
     */
    public IEntityProperty[] getPropertiesOfTopSampleRegisteredFor(
            final SampleIdentifier sampleIdentifier) throws UserFailureException;

    /** See {@link IETLLIMSService#listSamplesByCriteria(String, ListSamplesByPropertyCriteria)} */
    public List<Sample> listSamplesByCriteria(final ListSamplesByPropertyCriteria criteria)
            throws UserFailureException;

    /** See {@link IETLLIMSService#listDataSets(String, String)} */
    public List<SimpleDataSetInformationDTO> listDataSets() throws UserFailureException;

    /**
     * Creates and returns a unique code for a new data set.
     */
    public String createDataSetCode();
    
    /**
     * Creates a new unique ID which can be used to create codes which are guaranteed to be
     * unique.
     */
    public long drawANewUniqueID();

    /**
     * Returns the version of the service.
     */
    public int getVersion();

    /**
     * Returns the home database instance.
     */
    public DatabaseInstance getHomeDatabaseInstance();

    /**
     * List data sets deleted after the last seen deletion event. If event id is null all deleted
     * datasets will be returned.
     */
    public List<DeletedDataSet> listDeletedDataSets(Long lastSeenDeletionEventIdOrNull);

    /**
     * Updates specified properties of given data set.
     */
    public void updateDataSet(String code, List<NewProperty> properties, SpaceIdentifier space)
            throws UserFailureException;

    /**
     * Updates status of given data set.
     */
    public void updateDataSetStatus(String code, DataSetArchivizationStatus newStatus)
            throws UserFailureException;
}