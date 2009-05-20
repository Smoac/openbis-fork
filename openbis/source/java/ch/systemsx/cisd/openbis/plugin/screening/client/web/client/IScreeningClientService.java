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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGeneration;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;

/**
 * Service interface for the <i>generic</i> <i>GWT</i> client.
 * <p>
 * Each method should throw {@link UserFailureException}. The authorization framework can throw it
 * when the user has insufficient privileges. If it is not marked, the GWT client will report
 * unexpected exception.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public interface IScreeningClientService extends IClientService
{

    /**
     * For given {@link TechId} returns corresponding {@link SampleGeneration}.
     */
    public SampleGeneration getSampleGenerationInfo(final TechId sampleId, String baseIndexURL)
            throws UserFailureException;

    /**
     * Registers a new sample.
     */
    public void registerSample(final String sessionKey, final NewSample sample)
            throws UserFailureException;
}
