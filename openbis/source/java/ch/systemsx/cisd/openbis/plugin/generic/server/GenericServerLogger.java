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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import java.util.List;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.shared.NewExperiment;
import ch.systemsx.cisd.openbis.generic.client.shared.NewSample;
import ch.systemsx.cisd.openbis.generic.client.shared.SampleType;
import ch.systemsx.cisd.openbis.generic.server.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * Logger class for {@link GenericServer} which creates readable logs of method invocations.
 * 
 * @author Franz-Josef Elmer
 */
final class GenericServerLogger extends AbstractServerLogger implements IGenericServer
{
    /**
     * Creates an instance for the specified session manager and invocation status. The session
     * manager is used to retrieve user information which will be a part of the log message.
     */
    GenericServerLogger(final ISessionManager<Session> sessionManager,
            final boolean invocationSuccessful)
    {
        super(sessionManager, invocationSuccessful);
    }

    //
    // IGenericServer
    //

    public final SampleGenerationDTO getSampleInfo(final String sessionToken,
            final SampleIdentifier identifier)
    {
        logAccess(sessionToken, "get_sample_info", "IDENTIFIER(%s)", identifier);
        return null;
    }

    public void registerSample(final String sessionToken, final NewSample newSample)
    {
        logTracking(sessionToken, "register_sample", "SAMPLE_TYPE(%s) SAMPLE(%S)", newSample
                .getSampleType(), newSample.getIdentifier());
    }

    public ExperimentPE getExperimentInfo(final String sessionToken,
            final ExperimentIdentifier identifier)
    {
        logAccess(sessionToken, "get_experiment_info", "IDENTIFIER(%s)", identifier);
        return null;
    }

    public AttachmentPE getExperimentFileAttachment(final String sessionToken,
            final ExperimentIdentifier experimentIdentifier, final String filename,
            final int version) throws UserFailureException
    {
        logAccess(sessionToken, "get_attachment", "EXPERIMENT_IDENTIFIER(%s) FILE(%s) VERSION(%s)",
                experimentIdentifier, filename, version);
        return null;
    }

    public final void registerSamples(final String sessionToken, final SampleType sampleType,
            final List<NewSample> newSamples) throws UserFailureException
    {
        logAccess(sessionToken, "register_samples", "SAMPLE_TYPE(%s) SAMPLES(%s)", sampleType,
                CollectionUtils.abbreviate(newSamples, 20));
    }

    public void registerExperiment(String sessionToken, NewExperiment experiment)
    {
        logTracking(sessionToken, "register_experiment", "EXPERIMENT_TYPE(%s) EXPERIMENT(%S)",
                experiment.getExperimentTypeCode(), experiment.getIdentifier());

    }
}
