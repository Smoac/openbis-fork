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

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleGeneration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Asynchronous version of {@link IGenericClientService}.
 * 
 * @author Franz-Josef Elmer
 */
public interface IGenericClientServiceAsync extends IClientServiceAsync
{
    /**
     * @see IGenericClientService#getSampleGenerationInfo(TechId, String)
     */
    public void getSampleGenerationInfo(final TechId sampleId, String baseIndexURL,
            AsyncCallback<SampleGeneration> asyncCallback);

    /**
     * @see IGenericClientService#getSampleInfo(TechId, String)
     */
    public void getSampleInfo(final TechId sampleId, String baseIndexURL,
            AsyncCallback<Sample> asyncCallback);

    /**
     * @see IGenericClientService#registerSample(String, NewSample)
     */
    public void registerSample(final String sessionKey, final NewSample sample,
            final AsyncCallback<Void> asyncCallback) throws UserFailureException;

    /**
     * @see IGenericClientService#registerSamples(SampleType, String, String)
     */
    public void registerSamples(final SampleType sampleType, final String sessionKey,
            String defaultGroupIdentifier,
            final AsyncCallback<List<BatchRegistrationResult>> asyncCallback)
            throws UserFailureException;

    /**
     * @see IGenericClientService#getExperimentInfo(String, String)
     */
    public void getExperimentInfo(String experimentIdentifier, String baseIndexURL,
            final AsyncCallback<Experiment> experimentInfoCallback);

    /**
     * @see IGenericClientService#getExperimentInfo(TechId, String)
     */
    public void getExperimentInfo(TechId experimentId, String baseIndexURL,
            final AsyncCallback<Experiment> experimentInfoCallback);

    /**
     * @see IGenericClientService#getMaterialInfo(TechId)
     */
    public void getMaterialInfo(TechId materialId,
            final AsyncCallback<Material> materialInfoCallback);

    /**
     * @see IGenericClientService#getDataSetInfo(TechId, String)
     */
    public void getDataSetInfo(TechId datasetTechId, String baseIndexURL,
            final AsyncCallback<ExternalData> datasetInfoCallback);

    /**
     * @see IGenericClientService#registerExperiment(String,String,NewExperiment)
     */
    public void registerExperiment(final String attachmentsSessionKey, String samplesSessionKey,
            NewExperiment newExp, AsyncCallback<Void> assyncCallback) throws UserFailureException;

    /**
     * @see IGenericClientService#registerMaterials(MaterialType, String)
     */
    public void registerMaterials(MaterialType materialType, String sessionKey,
            final AsyncCallback<List<BatchRegistrationResult>> asyncCallback);

    /**
     * @see IGenericClientService#updateExperiment(ExperimentUpdates)
     */
    public void updateExperiment(ExperimentUpdates experimentUpdates,
            final AsyncCallback<ExperimentUpdateResult> asyncCallback) throws UserFailureException;

    /**
     * @see IGenericClientService#updateMaterial(TechId, List, Date)
     */
    public void updateMaterial(final TechId materialId, List<IEntityProperty> properties,
            Date version, final AsyncCallback<Date> asyncCallback) throws UserFailureException;

    /**
     * @see IGenericClientService#updateSample(SampleUpdates)
     */
    public void updateSample(SampleUpdates updates, final AsyncCallback<Date> asyncCallback)
            throws UserFailureException;

    /**
     * @see IGenericClientService#updateDataSet(DataSetUpdates)
     */
    public void updateDataSet(DataSetUpdates updates, final AsyncCallback<Date> asyncCallback)
            throws UserFailureException;

}
