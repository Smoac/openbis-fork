/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server;

import java.util.List;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReference;

/**
 * @author Franz-Josef Elmer
 */
public class PhosphoNetXServerLogger extends AbstractServerLogger implements IPhosphoNetXServer
{
    PhosphoNetXServerLogger(final ISessionManager<Session> sessionManager,
            final boolean invocationSuccessful, final long elapsedTime)
    {
        super(sessionManager, invocationSuccessful, elapsedTime);
    }

    public SampleGenerationDTO getSampleInfo(String sessionToken, SampleIdentifier sampleIdentifier)
            throws UserFailureException
    {
        logAccess(sessionToken, "get_sample_info", "CODE(%s)", sampleIdentifier);
        return null;
    }

    public SampleGenerationDTO getSampleInfo(String sessionToken, TechId sampleId)
            throws UserFailureException
    {
        logAccess(sessionToken, "get_sample_info", "ID(%s)", sampleId);
        return null;
    }

    public void registerSample(String sessionToken, NewSample newSample,
            List<AttachmentPE> attachments)
    {
        logTracking(sessionToken, "register_sample", "SAMPLE_TYPE(%s) SAMPLE(%s) ATTACHMENTS(%s)",
                newSample.getSampleType(), newSample.getIdentifier(), attachments.size());
    }

    public List<ProteinReference> listProteinsByExperiment(String sessionToken,
            TechId experimentId, double falseDiscoveryRate) throws UserFailureException
    {
        logAccess(sessionToken, "list_proteins_by_experiment", "ID(%s) FDR(%s)", experimentId,
                falseDiscoveryRate);
        return null;
    }

}
