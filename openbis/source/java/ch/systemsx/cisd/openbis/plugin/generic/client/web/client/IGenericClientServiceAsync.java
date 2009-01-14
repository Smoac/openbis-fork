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

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.shared.NewExperiment;
import ch.systemsx.cisd.openbis.generic.client.shared.NewSample;
import ch.systemsx.cisd.openbis.generic.client.shared.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.IClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGeneration;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

/**
 * Asynchronous version of {@link IGenericClientService}.
 * 
 * @author Franz-Josef Elmer
 */
public interface IGenericClientServiceAsync extends IClientServiceAsync
{

    /**
     * @see IGenericClientService#getSampleInfo(String)
     */
    public void getSampleInfo(final String sampleIdentifier,
            AsyncCallback<SampleGeneration> asyncCallback);

    /**
     * @see IGenericClientService#registerSample(NewSample)
     */
    public void registerSample(final NewSample sample, final AsyncCallback<Void> asyncCallback)
            throws UserFailureException;

    /**
     * @see IGenericClientService#getExperimentInfo(String)
     */
    public void getExperimentInfo(String experimentIdentifier,
            final AsyncCallback<Experiment> experimentInfoCallback);

    /**
     * @see IGenericClientService#registerSamples(SampleType, String)
     */
    public void registerSamples(final SampleType sampleType, final String sessionKey,
            final AsyncCallback<List<BatchRegistrationResult>> asyncCallback)
            throws UserFailureException;

    /**
     * @see IGenericClientService#registerExperiment(NewExperiment)
     */
    public void registerExperiment(NewExperiment newExp, AsyncCallback<Void> assyncCallback)
            throws UserFailureException;

}
