/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client;

import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleUpdateResult;

/**
 * Service interface for the generic GWT client.
 * <p>
 * Each method should throw {@link UserFailureException}. The authorization framework can throw it
 * when the user has insufficient privileges. If it is not marked, the GWT client will report
 * unexpected exception.
 * </p>
 * 
 * @author Franz-Josef Elmer
 */
public interface IGenericClientService extends IClientService
{

    /**
     * For given {@link TechId} returns corresponding {@link SampleParentWithDerived}.
     */
    public SampleParentWithDerived getSampleGenerationInfo(final TechId sampleId)
            throws UserFailureException;

    /**
     * For given {@link TechId} returns corresponding {@link Sample}.
     */
    public Sample getSampleInfo(final TechId sampleId) throws UserFailureException;

    /**
     * Registers a new sample.
     */
    public void registerSample(final String sessionKey, final NewSample sample)
            throws UserFailureException;

    /**
     * Registers new samples from files which have been previously uploaded.
     * <p>
     * Uploaded files can be found as session attribute under given <var>sessionKey</var>.
     * </p>
     */
    public List<BatchRegistrationResult> registerSamples(final SampleType sampleType,
            final String sessionKey, String defaultGroupIdentifier) throws UserFailureException;

    /**
     * Updates samples from files which have been previously uploaded.
     * <p>
     * Uploaded files can be found as session attribute under given <var>sessionKey</var>.
     * </p>
     */
    public List<BatchRegistrationResult> updateSamples(final SampleType sampleType,
            final String sessionKey, String defaultGroupIdentifier) throws UserFailureException;

    /**
     * For given {@link TechId} returns corresponding {@link Material}.
     */
    public Material getMaterialInfo(final TechId materialId) throws UserFailureException;

    /**
     * For given {@link TechId} returns corresponding {@link ExternalData}.
     */
    public ExternalData getDataSetInfo(final TechId datasetId) throws UserFailureException;

    /**
     * Registers a new experiment.
     */
    public void registerExperiment(final String attachmentsSessionKey,
            final String samplesSessionKey, final NewExperiment experiment)
            throws UserFailureException;

    /**
     * Registers new materials from files which have been previously uploaded.
     * <p>
     * Uploaded files can be found as session attribute under given <var>sessionKey</var>.
     * </p>
     */
    public List<BatchRegistrationResult> registerMaterials(final MaterialType materialType,
            final String sessionKey) throws UserFailureException;

    /**
     * Updates experiment.
     */
    public ExperimentUpdateResult updateExperiment(ExperimentUpdates experimentUpdates)
            throws UserFailureException;

    /**
     * Updates material.
     */
    public Date updateMaterial(final TechId materialId, List<IEntityProperty> properties,
            Date version) throws UserFailureException;

    /**
     * Updates sample.
     */
    public SampleUpdateResult updateSample(SampleUpdates updates) throws UserFailureException;

    /**
     * Updates data set.
     */
    public DataSetUpdateResult updateDataSet(DataSetUpdates updates) throws UserFailureException;

    /**
     * Updates data sets from files which have been previously uploaded.
     * <p>
     * Uploaded files can be found as session attribute under given <var>sessionKey</var>.
     * </p>
     */
    public List<BatchRegistrationResult> updateDataSets(final DataSetType dataSetType,
            final String sessionKey) throws UserFailureException;
}
