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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.server;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGeneration;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractClientService;
import ch.systemsx.cisd.openbis.generic.client.web.server.AttachmentRegistrationHelper;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientService;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;

/**
 * The {@link IScreeningClientService} implementation.
 * 
 * @author Christian Ribeaud
 */
@Component(value = ResourceNames.SCREENING_PLUGIN_SERVICE)
public final class ScreeningClientService extends AbstractClientService implements
        IScreeningClientService
{

    @Resource(name = ResourceNames.SCREENING_PLUGIN_SERVER)
    private IScreeningServer screeningServer;

    public ScreeningClientService()
    {
    }

    @Private
    ScreeningClientService(final IScreeningServer screeningServer,
            final IRequestContextProvider requestContextProvider)
    {
        super(requestContextProvider);
        this.screeningServer = screeningServer;
    }

    //
    // AbstractClientService
    //

    @Override
    protected final IServer getServer()
    {
        return screeningServer;
    }

    //
    // IScreeningClientService
    //

    public final SampleGeneration getSampleGenerationInfo(final TechId sampleId, String baseIndexURL)
            throws UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
          final SampleGenerationDTO sampleGenerationDTO =
                    screeningServer.getSampleInfo(sessionToken, sampleId);
            return SampleTranslator.translate(sampleGenerationDTO, baseIndexURL);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerSample(final String sessionKey, final NewSample sample)
            throws UserFailureException
    {

        final String sessionToken = getSessionToken();
        new AttachmentRegistrationHelper()
            {
                @Override
                public void register(List<AttachmentPE> attachments)
                {
                    screeningServer.registerSample(sessionToken, sample, attachments);
                }
            }.process(sessionKey, getHttpSession());
    }
}
