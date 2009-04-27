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
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Material;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGeneration;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Asynchronous version of {@link IGenericClientService}.
 * 
 * @author Franz-Josef Elmer
 */
public interface IGenericClientServiceAsync extends IClientServiceAsync
{
    /**
     * @see IGenericClientService#getSampleGenerationInfo(String)
     */
    public void getSampleGenerationInfo(final String sampleIdentifier,
            AsyncCallback<SampleGeneration> asyncCallback);

    /**
     * @see IGenericClientService#getSampleInfo(String)
     */
    public void getSampleInfo(final String sampleIdentifier, AsyncCallback<Sample> asyncCallback);

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
     * @see IGenericClientService#getExperimentInfo(String)
     */
    public void getExperimentInfo(String experimentIdentifier,
            final AsyncCallback<Experiment> experimentInfoCallback);

    /**
     * @see IGenericClientService#getMaterialInfo(String)
     */
    public void getMaterialInfo(String materialIdentifier,
            final AsyncCallback<Material> materialInfoCallback);

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
            final AsyncCallback<Void> asyncCallback) throws UserFailureException;

    /**
     * @see IGenericClientService#updateMaterial(String, List, Date)
     */
    public void updateMaterial(final String materialIdentifier, List<MaterialProperty> properties,
            Date version, final AsyncCallback<Void> asyncCallback) throws UserFailureException;

    /**
     * @see IGenericClientService#updateSample(String, String, List, ExperimentIdentifier, Date)
     */
    public void updateSample(String sessionKey, final String sampleIdentifier,
            List<SampleProperty> properties, ExperimentIdentifier experimentIdentifierOrNull,
            Date version, final AsyncCallback<Void> asyncCallback) throws UserFailureException;

    /**
     * @see IGenericClientService#updateDataSet(String, List, Date)
     */
    public void updateDataSet(final String dataSetIdentifier, List<DataSetProperty> properties,
            Date version, final AsyncCallback<Void> asyncCallback) throws UserFailureException;

}
