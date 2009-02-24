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

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGeneration;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Service interface for the generic GWT client.
 * 
 * @author Franz-Josef Elmer
 */
public interface IGenericClientService extends IClientService
{

    /**
     * For given <var>sampleIdentifier</var> returns corresponding {@link Sample}.
     */
    public SampleGeneration getSampleInfo(final String sampleIdentifier)
            throws UserFailureException;

    /**
     * Registers a new sample.
     */
    public void registerSample(final NewSample sample) throws UserFailureException;

    /**
     * Registers new samples from files which have been previously uploaded.
     * <p>
     * Uploaded files can be found as session attribute under given <var>sessionKey</var>.
     * </p>
     */
    public List<BatchRegistrationResult> registerSamples(final SampleType sampleType,
            final String sessionKey) throws UserFailureException;

    /**
     * For given <var>experimentIdentifier</var> returns corresponding {@link Experiment}.
     */
    public Experiment getExperimentInfo(final String experimentIdentifier)
            throws UserFailureException;

    /**
     * Registers a new experiment.
     */
    public void registerExperiment(final String sessionKey, final NewExperiment experiment)
            throws UserFailureException;

    /**
     * Registers new materials from files which have been previously uploaded.
     * <p>
     * Uploaded files can be found as session attribute under given <var>sessionKey</var>.
     * </p>
     */
    public List<BatchRegistrationResult> registerMaterials(final MaterialType materialType,
            final String sessionKey) throws UserFailureException;
}
